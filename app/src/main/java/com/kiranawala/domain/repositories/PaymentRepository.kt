package com.kiranawala.domain.repositories

import com.kiranawala.domain.models.Result

/**
 * Payment repository interface
 * Defines contract for payment-related operations
 */
interface PaymentRepository {
    /**
     * Initiate payment for an order
     * @param orderId Order ID
     * @param amount Payment amount
     * @return Result containing payment ID or error
     */
    suspend fun initiatePayment(orderId: String, amount: Double): Result<String>
    
    /**
     * Verify payment status
     * @param paymentId Payment ID
     * @param orderId Order ID
     * @return Result indicating payment success or error
     */
    suspend fun verifyPayment(paymentId: String, orderId: String): Result<Boolean>
    
    /**
     * Get payment status
     * @param paymentId Payment ID
     * @return Result containing payment status or error
     */
    suspend fun getPaymentStatus(paymentId: String): Result<String>
    
    /**
     * Process refund for cancelled order
     * @param orderId Order ID
     * @param amount Refund amount
     * @return Result indicating refund success or error
     */
    suspend fun processRefund(orderId: String, amount: Double): Result<Boolean>
}
