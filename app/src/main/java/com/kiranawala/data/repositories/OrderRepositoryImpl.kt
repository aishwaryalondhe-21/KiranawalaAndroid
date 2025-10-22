package com.kiranawala.data.repositories

import com.kiranawala.data.local.dao.OrderDao
import com.kiranawala.data.local.entities.OrderEntity
import com.kiranawala.data.local.entities.OrderItemEntity
import com.kiranawala.data.local.preferences.PreferencesManager
import com.kiranawala.domain.models.Order
import com.kiranawala.domain.models.OrderItem
import com.kiranawala.domain.repositories.OrderRepository
import com.kiranawala.utils.logger.KiranaLogger
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import java.util.UUID
import javax.inject.Inject

@Serializable
data class OrderDto(
    val id: String? = null,
    val customer_id: String,
    val store_id: String,
    val store_name: String = "",
    val total_amount: Double,
    val delivery_fee: Double = 30.0,
    val status: String = "PENDING",
    val payment_status: String = "PENDING",
    val delivery_address: String,
    val customer_phone: String,
    val customer_name: String,
    val created_at: String? = null,
    val updated_at: String? = null
)

@Serializable
data class OrderItemDto(
    val id: String? = null,
    val order_id: String,
    val product_id: String,
    val product_name: String,
    val quantity: Int,
    val price: Double
)

/**
 * Order repository implementation
 */
class OrderRepositoryImpl @Inject constructor(
    private val orderDao: OrderDao,
    private val supabase: Postgrest,
    private val preferencesManager: PreferencesManager
) : OrderRepository {
    
    companion object {
        private const val TAG = "OrderRepository"
    }
    
    override suspend fun placeOrder(order: Order): String {
        KiranaLogger.d(TAG, "Placing order for store ${order.storeId}")
        
        try {
            // Save order to Supabase
            val orderDto = OrderDto(
                customer_id = order.customerId,
                store_id = order.storeId,
                total_amount = order.totalAmount,
                delivery_fee = order.deliveryFee,
                status = order.status,
                delivery_address = order.deliveryAddress,
                customer_phone = order.customerPhone,
                customer_name = order.customerName
            )
            
            val insertedOrder = supabase["orders"]
                .insert(orderDto) {
                    select(Columns.ALL)
                }
                .decodeSingle<OrderDto>()
            
            val orderId = insertedOrder.id ?: UUID.randomUUID().toString()
            KiranaLogger.d(TAG, "Order saved to Supabase with ID: $orderId")
            
            // Save order items to Supabase
            val orderItemDtos = order.items.map { item ->
                OrderItemDto(
                    order_id = orderId,
                    product_id = item.productId,
                    product_name = item.productName,
                    quantity = item.quantity,
                    price = item.price
                )
            }
            
            supabase["order_items"]
                .insert(orderItemDtos)
            
            KiranaLogger.d(TAG, "Order items saved to Supabase")
            
            // Also save locally for offline access
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val orderEntity = OrderEntity(
                id = orderId,
                customerId = order.customerId,
                storeId = order.storeId,
                totalAmount = order.totalAmount,
                status = order.status,
                createdAt = now,
                updatedAt = now
            )
            orderDao.insertOrder(orderEntity)
            
            val orderItemEntities = order.items.map { item ->
                OrderItemEntity(
                    id = UUID.randomUUID().toString(),
                    orderId = orderId,
                    productId = item.productId,
                    quantity = item.quantity,
                    price = item.price
                )
            }
            orderDao.insertOrderItems(orderItemEntities)
            
            return orderId
        } catch (e: Exception) {
            KiranaLogger.e(TAG, "Failed to save order to Supabase", e)
            throw e
        }
    }
    
    override suspend fun getOrderById(orderId: String): Order? {
        KiranaLogger.d(TAG, "Getting order $orderId")
        
        try {
            // Fetch from Supabase for complete data
            val orderDto = supabase["orders"]
                .select(Columns.ALL) {
                    filter { eq("id", orderId) }
                }
                .decodeSingle<OrderDto>()
            
            val items = supabase["order_items"]
                .select(Columns.ALL) {
                    filter { eq("order_id", orderId) }
                }
                .decodeList<OrderItemDto>()
            
            // Fetch store name
            val storeName = try {
                val store = supabase["stores"]
                    .select(Columns.raw("name")) {
                        filter { eq("id", orderDto.store_id) }
                    }
                    .decodeSingle<Map<String, String>>()
                store["name"] ?: orderDto.store_id
            } catch (e: Exception) {
                orderDto.store_id
            }
            
            val createdAt = orderDto.created_at?.let { Instant.parse(it).toLocalDateTime(TimeZone.UTC) }
                ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val updatedAt = orderDto.updated_at?.let { Instant.parse(it).toLocalDateTime(TimeZone.UTC) }
                ?: createdAt
            
            return Order(
                id = orderId,
                customerId = orderDto.customer_id,
                storeId = orderDto.store_id,
                storeName = storeName,
                totalAmount = orderDto.total_amount,
                deliveryFee = orderDto.delivery_fee,
                status = orderDto.status,
                items = items.map { itemDto ->
                    OrderItem(
                        id = itemDto.id ?: UUID.randomUUID().toString(),
                        orderId = orderId,
                        productId = itemDto.product_id,
                        productName = itemDto.product_name,
                        quantity = itemDto.quantity,
                        price = itemDto.price
                    )
                },
                deliveryAddress = orderDto.delivery_address,
                customerPhone = orderDto.customer_phone,
                customerName = orderDto.customer_name,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        } catch (e: Exception) {
            KiranaLogger.e(TAG, "Failed to fetch order from Supabase, trying local", e)
            // Fallback to local cache
            val orderEntity = orderDao.getOrder(orderId) ?: return null
            val orderItems = orderDao.getOrderItems(orderId)
            
            return Order(
                id = orderEntity.id,
                customerId = orderEntity.customerId,
                storeId = orderEntity.storeId,
                storeName = orderEntity.storeId,
                totalAmount = orderEntity.totalAmount,
                deliveryFee = 30.0,
                status = orderEntity.status,
                items = orderItems.map { item ->
                    OrderItem(
                        id = item.id,
                        orderId = item.orderId,
                        productId = item.productId,
                        productName = item.productId,
                        quantity = item.quantity,
                        price = item.price
                    )
                },
                deliveryAddress = "",
                customerPhone = "",
                customerName = "",
                createdAt = orderEntity.createdAt,
                updatedAt = orderEntity.updatedAt
            )
        }
    }
    
    override fun getCustomerOrders(customerId: String): Flow<List<Order>> = flow {
        KiranaLogger.d(TAG, "Fetching orders for customer $customerId")

        val phone = preferencesManager.getUserPhone().first()
        if (phone.isBlank()) {
            KiranaLogger.w(TAG, "No stored phone for current user; returning local cache")
            emit(mapOrdersFromCache(customerId, fallbackPhone = ""))
            return@flow
        }

        val remoteOrders = try {
            fetchOrdersFromSupabase(customerId, phone)
        } catch (e: Exception) {
            KiranaLogger.e(TAG, "Failed to fetch orders from Supabase", e)
            emptyList()
        }

        if (remoteOrders.isNotEmpty()) {
            cacheOrders(remoteOrders)
            emit(remoteOrders)
        } else {
            emit(mapOrdersFromCache(customerId, fallbackPhone = phone))
        }
    }
    
    override suspend fun cancelOrder(orderId: String) {
        KiranaLogger.d(TAG, "Cancelling order $orderId")
        orderDao.updateOrderStatus(orderId, "CANCELLED")
    }

    private suspend fun fetchOrdersFromSupabase(customerId: String, phone: String): List<Order> {
        KiranaLogger.d(TAG, "Fetching remote orders for customerId=$customerId phone=$phone")

        val phoneVariants = buildList {
            add(phone)
            val digitsOnly = phone.filter { it.isDigit() }
            add(digitsOnly)
            if (digitsOnly.startsWith("91") && digitsOnly.length > 2) {
                add(digitsOnly.removePrefix("91"))
            }
            if (digitsOnly.startsWith("0") && digitsOnly.length > 1) {
                add(digitsOnly.trimStart('0'))
            }
        }.distinct().filter { it.isNotBlank() }

        var orderDtos = supabase["orders"]
            .select(Columns.ALL) {
                filter {
                    eq("customer_id", customerId)
                }
            }
            .decodeList<OrderDto>()

        if (orderDtos.isEmpty()) {
            for (variant in phoneVariants) {
                orderDtos = supabase["orders"]
                    .select(Columns.ALL) {
                        filter {
                            eq("customer_phone", variant)
                        }
                    }
                    .decodeList<OrderDto>()
                if (orderDtos.isNotEmpty()) {
                    KiranaLogger.d(TAG, "Found orders using phone variant $variant")
                    break
                }
            }
        }

        if (orderDtos.isEmpty()) return emptyList()

        return orderDtos.mapNotNull { dto ->
            val orderId = dto.id ?: return@mapNotNull null

            val items = supabase["order_items"]
                .select(Columns.ALL) {
                    filter {
                        eq("order_id", orderId)
                    }
                }
                .decodeList<OrderItemDto>()

            val createdAt = dto.created_at?.let { Instant.parse(it).toLocalDateTime(TimeZone.UTC) }
                ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val updatedAt = dto.updated_at?.let { Instant.parse(it).toLocalDateTime(TimeZone.UTC) }
                ?: createdAt

            Order(
                id = orderId,
                customerId = dto.customer_id,
                storeId = dto.store_id,
                storeName = dto.store_name.ifBlank { dto.store_id },
                totalAmount = dto.total_amount,
                deliveryFee = dto.delivery_fee,
                status = dto.status,
                items = items.map { itemDto ->
                    val itemId = itemDto.id ?: "$orderId-${itemDto.product_id}"
                    OrderItem(
                        id = itemId,
                        orderId = orderId,
                        productId = itemDto.product_id,
                        productName = itemDto.product_name,
                        quantity = itemDto.quantity,
                        price = itemDto.price
                    )
                },
                deliveryAddress = dto.delivery_address,
                customerPhone = dto.customer_phone,
                customerName = dto.customer_name,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        }.sortedByDescending { it.createdAt }
    }

    private suspend fun cacheOrders(orders: List<Order>) {
        orders.forEach { order ->
            val entity = OrderEntity(
                id = order.id,
                customerId = order.customerId,
                storeId = order.storeId,
                totalAmount = order.totalAmount,
                status = order.status,
                createdAt = order.createdAt,
                updatedAt = order.updatedAt
            )
            orderDao.insertOrder(entity)

            val itemEntities = order.items.map { item ->
                val itemId = item.id.ifBlank { "${order.id}-${item.productId}" }
                OrderItemEntity(
                    id = itemId,
                    orderId = order.id,
                    productId = item.productId,
                    quantity = item.quantity,
                    price = item.price
                )
            }
            orderDao.insertOrderItems(itemEntities)
        }
    }

    private suspend fun mapOrdersFromCache(customerId: String, fallbackPhone: String): List<Order> {
        val cachedOrders = orderDao.getCustomerOrders(customerId)
        return cachedOrders.map { orderEntity ->
            val items = orderDao.getOrderItems(orderEntity.id)
            Order(
                id = orderEntity.id,
                customerId = orderEntity.customerId,
                storeId = orderEntity.storeId,
                storeName = orderEntity.storeId,
                totalAmount = orderEntity.totalAmount,
                deliveryFee = 30.0,
                status = orderEntity.status,
                items = items.map { item ->
                    OrderItem(
                        id = item.id,
                        orderId = item.orderId,
                        productId = item.productId,
                        productName = item.productId,
                        quantity = item.quantity,
                        price = item.price
                    )
                },
                deliveryAddress = "",
                customerPhone = fallbackPhone,
                customerName = "",
                createdAt = orderEntity.createdAt,
                updatedAt = orderEntity.updatedAt
            )
        }
    }
}
