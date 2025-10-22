-- Kiranawala Phase 3 & 4: Store Browsing and Shopping Cart
-- Database Schema for Supabase PostgreSQL

-- ============================================
-- STORES TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS stores (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    address TEXT NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    contact TEXT NOT NULL,
    logo_url TEXT,
    rating DECIMAL(2,1) DEFAULT 4.5 CHECK (rating >= 0 AND rating <= 5),
    minimum_order_value DECIMAL(10,2) DEFAULT 100.00,
    delivery_fee DECIMAL(10,2) DEFAULT 30.00,
    estimated_delivery_time INTEGER DEFAULT 30, -- in minutes
    is_open BOOLEAN DEFAULT true,
    subscription_status TEXT DEFAULT 'ACTIVE' CHECK (subscription_status IN ('ACTIVE', 'EXPIRED', 'SUSPENDED')),
    subscription_start_date TIMESTAMPTZ,
    subscription_end_date TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- PRODUCTS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS products (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    store_id UUID NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    stock_quantity INTEGER NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    image_url TEXT,
    category TEXT DEFAULT 'General',
    is_available BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- ORDERS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS orders (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    store_id UUID NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    total_amount DECIMAL(10,2) NOT NULL CHECK (total_amount >= 0),
    delivery_fee DECIMAL(10,2) DEFAULT 30.00,
    status TEXT DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'CANCELLED', 'FAILED')),
    payment_status TEXT DEFAULT 'PENDING' CHECK (payment_status IN ('PENDING', 'PAID', 'FAILED', 'REFUNDED')),
    delivery_address TEXT NOT NULL,
    customer_phone TEXT NOT NULL,
    customer_name TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- ORDER_ITEMS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS order_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    product_name TEXT NOT NULL, -- Snapshot of product name at time of order
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- INDEXES FOR PERFORMANCE
-- ============================================
CREATE INDEX IF NOT EXISTS idx_products_store_id ON products(store_id);
CREATE INDEX IF NOT EXISTS idx_products_category ON products(category);
CREATE INDEX IF NOT EXISTS idx_products_available ON products(is_available);
CREATE INDEX IF NOT EXISTS idx_orders_customer_id ON orders(customer_id);
CREATE INDEX IF NOT EXISTS idx_orders_store_id ON orders(store_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_stores_subscription_status ON stores(subscription_status);
CREATE INDEX IF NOT EXISTS idx_stores_is_open ON stores(is_open);

-- ============================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- ============================================

-- Enable RLS on all tables
ALTER TABLE stores ENABLE ROW LEVEL SECURITY;
ALTER TABLE products ENABLE ROW LEVEL SECURITY;
ALTER TABLE orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE order_items ENABLE ROW LEVEL SECURITY;

-- Stores: Public read access for active stores
CREATE POLICY "Anyone can view active stores" 
    ON stores FOR SELECT 
    USING (subscription_status = 'ACTIVE');

-- Products: Public read access for available products in active stores
CREATE POLICY "Anyone can view available products" 
    ON products FOR SELECT 
    USING (
        is_available = true 
        AND EXISTS (
            SELECT 1 FROM stores 
            WHERE stores.id = products.store_id 
            AND stores.subscription_status = 'ACTIVE'
        )
    );

-- Orders: Users can only view their own orders
CREATE POLICY "Users can view own orders" 
    ON orders FOR SELECT 
    USING (auth.uid() = customer_id);

CREATE POLICY "Users can create own orders" 
    ON orders FOR INSERT 
    WITH CHECK (auth.uid() = customer_id);

-- Order Items: Users can view items from their own orders
CREATE POLICY "Users can view own order items" 
    ON order_items FOR SELECT 
    USING (
        EXISTS (
            SELECT 1 FROM orders 
            WHERE orders.id = order_items.order_id 
            AND orders.customer_id = auth.uid()
        )
    );

-- ============================================
-- TRIGGERS FOR AUTO-UPDATE TIMESTAMPS
-- ============================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply trigger to stores
CREATE TRIGGER update_stores_updated_at
    BEFORE UPDATE ON stores
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Apply trigger to products
CREATE TRIGGER update_products_updated_at
    BEFORE UPDATE ON products
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Apply trigger to orders
CREATE TRIGGER update_orders_updated_at
    BEFORE UPDATE ON orders
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- SAMPLE DATA FOR TESTING
-- ============================================

-- Insert sample stores
INSERT INTO stores (name, address, latitude, longitude, contact, minimum_order_value, delivery_fee, is_open) VALUES
('Sharma Kirana Store', '123 MG Road, Pune', 18.5204, 73.8567, '+919876543210', 150.00, 30.00, true),
('Patel General Store', '456 FC Road, Pune', 18.5314, 73.8446, '+919876543211', 100.00, 25.00, true),
('Gupta Provision Store', '789 Deccan, Pune', 18.5167, 73.8560, '+919876543212', 200.00, 40.00, true);

-- Insert sample products for first store
INSERT INTO products (store_id, name, description, price, stock_quantity, category, image_url) 
SELECT 
    id,
    'Tata Salt',
    '1kg pack of iodized salt',
    20.00,
    100,
    'Groceries',
    null
FROM stores WHERE name = 'Sharma Kirana Store'
LIMIT 1;

INSERT INTO products (store_id, name, description, price, stock_quantity, category) 
SELECT 
    id,
    'Amul Milk',
    '1 liter full cream milk',
    60.00,
    50,
    'Dairy'
FROM stores WHERE name = 'Sharma Kirana Store'
LIMIT 1;

INSERT INTO products (store_id, name, description, price, stock_quantity, category) 
SELECT 
    id,
    'Britannia Bread',
    'Whole wheat bread 400g',
    40.00,
    30,
    'Bakery'
FROM stores WHERE name = 'Sharma Kirana Store'
LIMIT 1;

-- Add more products for variety
INSERT INTO products (store_id, name, description, price, stock_quantity, category) 
SELECT 
    id,
    'Fortune Oil',
    'Sunflower oil 1 liter',
    150.00,
    40,
    'Groceries'
FROM stores WHERE name = 'Sharma Kirana Store'
LIMIT 1;

INSERT INTO products (store_id, name, description, price, stock_quantity, category) 
SELECT 
    id,
    'Maggi Noodles',
    '2-minute noodles pack of 12',
    144.00,
    60,
    'Instant Food'
FROM stores WHERE name = 'Sharma Kirana Store'
LIMIT 1;

-- ============================================
-- VERIFICATION QUERIES
-- ============================================

-- Check stores
-- SELECT * FROM stores;

-- Check products
-- SELECT p.*, s.name as store_name FROM products p JOIN stores s ON p.store_id = s.id;

-- Check RLS policies
-- SELECT * FROM pg_policies WHERE tablename IN ('stores', 'products', 'orders', 'order_items');
