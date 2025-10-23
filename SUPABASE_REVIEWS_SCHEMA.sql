-- ============================================================================
-- Kiranawala - Store Reviews System Database Schema
-- Phase 3: Store Reviews and Ratings
-- ============================================================================

-- Enable UUID extension if not already enabled
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================================
-- Table: store_reviews
-- Stores customer reviews and ratings for stores
-- ============================================================================

CREATE TABLE IF NOT EXISTS store_reviews (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    store_id UUID NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    customer_id UUID NOT NULL,
    customer_name TEXT NOT NULL,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ============================================================================
-- Indexes for Performance
-- ============================================================================

-- Index on store_id for fast lookup of all reviews for a store
CREATE INDEX IF NOT EXISTS idx_store_reviews_store_id ON store_reviews(store_id);

-- Index on customer_id for finding user's reviews
CREATE INDEX IF NOT EXISTS idx_store_reviews_customer_id ON store_reviews(customer_id);

-- Index on created_at for sorting by date
CREATE INDEX IF NOT EXISTS idx_store_reviews_created_at ON store_reviews(created_at DESC);

-- Index on rating for sorting by rating
CREATE INDEX IF NOT EXISTS idx_store_reviews_rating ON store_reviews(rating DESC);

-- Composite index for one review per customer per store check
CREATE UNIQUE INDEX IF NOT EXISTS idx_store_reviews_unique_customer_store 
    ON store_reviews(store_id, customer_id);

-- ============================================================================
-- Row Level Security (RLS)
-- ============================================================================

-- Enable RLS on the table
ALTER TABLE store_reviews ENABLE ROW LEVEL SECURITY;

-- Policy: Anyone can view reviews
CREATE POLICY "Anyone can view reviews" ON store_reviews
    FOR SELECT 
    USING (true);

-- Policy: Authenticated users can insert their own reviews
CREATE POLICY "Users can insert own reviews" ON store_reviews
    FOR INSERT 
    WITH CHECK (auth.uid()::text = customer_id);

-- Policy: Users can update their own reviews
CREATE POLICY "Users can update own reviews" ON store_reviews
    FOR UPDATE 
    USING (auth.uid()::text = customer_id);

-- Policy: Users can delete their own reviews
CREATE POLICY "Users can delete own reviews" ON store_reviews
    FOR DELETE 
    USING (auth.uid()::text = customer_id);

-- ============================================================================
-- Trigger: Update updated_at timestamp
-- ============================================================================

CREATE OR REPLACE FUNCTION update_store_reviews_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_store_reviews_timestamp
    BEFORE UPDATE ON store_reviews
    FOR EACH ROW
    EXECUTE FUNCTION update_store_reviews_updated_at();

-- ============================================================================
-- Function: Calculate and return average rating for a store
-- ============================================================================

CREATE OR REPLACE FUNCTION get_store_average_rating(p_store_id UUID)
RETURNS NUMERIC AS $$
DECLARE
    avg_rating NUMERIC;
BEGIN
    SELECT COALESCE(AVG(rating), 0.0)
    INTO avg_rating
    FROM store_reviews
    WHERE store_id = p_store_id;
    
    RETURN ROUND(avg_rating, 1);
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- Function: Get review count for a store
-- ============================================================================

CREATE OR REPLACE FUNCTION get_store_review_count(p_store_id UUID)
RETURNS INTEGER AS $$
DECLARE
    review_count INTEGER;
BEGIN
    SELECT COUNT(*)
    INTO review_count
    FROM store_reviews
    WHERE store_id = p_store_id;
    
    RETURN review_count;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- Verification Queries
-- ============================================================================

-- Uncomment to verify setup:
-- SELECT * FROM store_reviews LIMIT 10;
-- SELECT get_store_average_rating('your-store-id-here');
-- SELECT get_store_review_count('your-store-id-here');

-- ============================================================================
-- Sample Data (Optional - for testing)
-- ============================================================================

-- Uncomment to insert sample reviews for testing:
/*
INSERT INTO store_reviews (store_id, customer_id, customer_name, rating, comment) VALUES
    ('store-id-1', 'customer-id-1', 'John Doe', 5, 'Excellent service and fresh products!'),
    ('store-id-1', 'customer-id-2', 'Jane Smith', 4, 'Good quality, but delivery was a bit slow.'),
    ('store-id-1', 'customer-id-3', 'Mike Johnson', 5, 'Best kirana store in the area!');
*/

-- ============================================================================
-- End of Schema
-- ============================================================================
