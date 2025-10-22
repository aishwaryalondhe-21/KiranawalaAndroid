-- Kiranawala Store Visibility Diagnostic & Fix
-- Run this in your Supabase SQL Editor

-- Step 1: Check current store status
SELECT 
    id,
    name,
    subscription_status,
    is_open,
    CASE 
        WHEN subscription_status = 'ACTIVE' AND is_open = true THEN 'VISIBLE ✅'
        WHEN subscription_status != 'ACTIVE' THEN 'HIDDEN - Status: ' || subscription_status
        WHEN is_open != true THEN 'HIDDEN - Store Closed'
        ELSE 'HIDDEN - Unknown'
    END as visibility_status
FROM stores 
ORDER BY name;

-- Step 2: Fix all stores to be visible
UPDATE stores 
SET 
    subscription_status = 'ACTIVE',
    is_open = true,
    updated_at = NOW()
WHERE subscription_status != 'ACTIVE' OR is_open != true;

-- Step 3: Verify the fix
SELECT 
    COUNT(*) as total_stores,
    COUNT(*) FILTER (WHERE subscription_status = 'ACTIVE' AND is_open = true) as visible_stores
FROM stores;

-- Step 4: List all visible stores (what the app will show)
SELECT 
    id,
    name,
    address,
    subscription_status,
    is_open,
    created_at
FROM stores 
WHERE subscription_status = 'ACTIVE' AND is_open = true
ORDER BY name;