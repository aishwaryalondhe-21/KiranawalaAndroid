# Supabase Database Documentation

## Database Schema

### 1. Customers Table

**Primary Key:** `id` (UUID) - References `auth.users(id)`  
**Unique Constraint:** `phone` (text)

```sql
CREATE TABLE public.customers (
  id uuid NOT NULL,
  phone text NOT NULL UNIQUE,
  name text NOT NULL,
  address text,
  latitude double precision DEFAULT 0,
  longitude double precision DEFAULT 0,
  created_at timestamp with time zone DEFAULT now(),
  updated_at timestamp with time zone DEFAULT now(),
  CONSTRAINT customers_pkey PRIMARY KEY (id),
  CONSTRAINT customers_id_fkey FOREIGN KEY (id) REFERENCES auth.users(id),
  CONSTRAINT customers_phone_unique UNIQUE (phone)
);
```

**Important Notes:**
- ✅ **No email field** - Removed completely as authentication is phone-based only
- ✅ **Phone is unique** - Each customer must have a unique phone number
- ✅ **UUID primary key** - Required for Supabase Auth integration
- ✅ **Simple address field** - Stores customer's delivery address as text

### 2. Stores Table

```sql
CREATE TABLE public.stores (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  name text NOT NULL,
  address text NOT NULL,
  latitude double precision NOT NULL,
  longitude double precision NOT NULL,
  contact text NOT NULL,
  logo_url text,
  rating numeric DEFAULT 4.5 CHECK (rating >= 0::numeric AND rating <= 5::numeric),
  minimum_order_value numeric DEFAULT 100.00,
  delivery_fee numeric DEFAULT 30.00,
  estimated_delivery_time integer DEFAULT 30,
  is_open boolean DEFAULT true,
  subscription_status text DEFAULT 'ACTIVE'::text,
  subscription_start_date timestamp with time zone,
  subscription_end_date timestamp with time zone,
  created_at timestamp with time zone DEFAULT now(),
  updated_at timestamp with time zone DEFAULT now(),
  CONSTRAINT stores_pkey PRIMARY KEY (id)
);
```

### 3. Products Table

```sql
CREATE TABLE public.products (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  store_id uuid NOT NULL,
  name text NOT NULL,
  description text,
  price numeric NOT NULL CHECK (price >= 0::numeric),
  stock_quantity integer NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
  image_url text,
  category text DEFAULT 'General'::text,
  is_available boolean DEFAULT true,
  created_at timestamp with time zone DEFAULT now(),
  updated_at timestamp with time zone DEFAULT now(),
  CONSTRAINT products_pkey PRIMARY KEY (id),
  CONSTRAINT products_store_id_fkey FOREIGN KEY (store_id) REFERENCES public.stores(id)
);
```

### 4. Orders Table

```sql
CREATE TABLE public.orders (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  customer_id uuid NOT NULL,
  store_id uuid NOT NULL,
  total_amount numeric NOT NULL CHECK (total_amount >= 0::numeric),
  delivery_fee numeric DEFAULT 30.00,
  status text DEFAULT 'PENDING'::text,
  payment_status text DEFAULT 'PENDING'::text,
  delivery_address text NOT NULL,
  customer_phone text NOT NULL,
  customer_name text NOT NULL,
  created_at timestamp with time zone DEFAULT now(),
  updated_at timestamp with time zone DEFAULT now(),
  CONSTRAINT orders_pkey PRIMARY KEY (id),
  CONSTRAINT orders_customer_id_fkey FOREIGN KEY (customer_id) REFERENCES auth.users(id),
  CONSTRAINT orders_store_id_fkey FOREIGN KEY (store_id) REFERENCES public.stores(id)
);
```

### 5. Order Items Table

```sql
CREATE TABLE public.order_items (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  order_id uuid NOT NULL,
  product_id uuid NOT NULL,
  product_name text NOT NULL,
  quantity integer NOT NULL CHECK (quantity > 0),
  price numeric NOT NULL CHECK (price >= 0::numeric),
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT order_items_pkey PRIMARY KEY (id),
  CONSTRAINT order_items_order_id_fkey FOREIGN KEY (order_id) REFERENCES public.orders(id),
  CONSTRAINT order_items_product_id_fkey FOREIGN KEY (product_id) REFERENCES public.products(id)
);
```

## Recent Database Changes (Phase 7 & 8)

### 1. Email Field Removal

**Date:** October 21, 2025  
**Reason:** App uses phone-based authentication only, no email required

```sql
-- Remove email column if it exists
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name = 'customers' AND column_name = 'email') THEN
        ALTER TABLE public.customers DROP COLUMN email;
    END IF;
END $$;
```

### 2. Duplicate Phone Number Cleanup

**Issue:** Multiple customer records with the same phone number  
**Solution:** Keep most recent record per phone, update order references

```sql
-- Step 1: View duplicates
SELECT phone, COUNT(*) as count, 
       ARRAY_AGG(id::text ORDER BY created_at DESC) as ids,
       ARRAY_AGG(name ORDER BY created_at DESC) as names,
       ARRAY_AGG(created_at ORDER BY created_at DESC) as creation_dates
FROM public.customers 
WHERE phone IS NOT NULL AND phone != ''
GROUP BY phone 
HAVING COUNT(*) > 1;

-- Step 2: Update orders to reference kept customer
WITH ranked_customers AS (
  SELECT id, phone, name, created_at,
         ROW_NUMBER() OVER (PARTITION BY phone ORDER BY created_at DESC) as rn
  FROM public.customers
  WHERE phone IS NOT NULL AND phone != ''
),
duplicates_to_delete AS (
  SELECT id FROM ranked_customers WHERE rn > 1
)
UPDATE public.orders 
SET customer_id = (
  SELECT id FROM ranked_customers 
  WHERE phone = (
    SELECT phone FROM public.customers 
    WHERE id = orders.customer_id
  ) AND rn = 1
)
WHERE customer_id IN (SELECT id FROM duplicates_to_delete);

-- Step 3: Delete duplicate customer records
WITH ranked_customers AS (
  SELECT id, phone, name, created_at,
         ROW_NUMBER() OVER (PARTITION BY phone ORDER BY created_at DESC) as rn
  FROM public.customers
  WHERE phone IS NOT NULL AND phone != ''
)
DELETE FROM public.customers 
WHERE id IN (
  SELECT id FROM ranked_customers WHERE rn > 1
);
```

### 3. Phone Unique Constraint Enforcement

```sql
-- Add unique constraint on phone
DO $$
DECLARE
    duplicate_count INTEGER;
BEGIN
    -- Check if duplicates still exist
    SELECT COUNT(*) INTO duplicate_count
    FROM (
        SELECT phone
        FROM public.customers 
        GROUP BY phone 
        HAVING COUNT(*) > 1
    ) duplicates;
    
    IF duplicate_count > 0 THEN
        RAISE NOTICE 'Cannot add unique constraint - % duplicate phone numbers still exist', duplicate_count;
        RETURN;
    END IF;
    
    -- Drop old constraint if exists
    IF EXISTS (SELECT 1 FROM information_schema.table_constraints 
               WHERE constraint_name = 'customers_phone_key' 
               AND table_name = 'customers') THEN
        ALTER TABLE public.customers DROP CONSTRAINT customers_phone_key;
    END IF;
    
    -- Add new unique constraint
    ALTER TABLE public.customers ADD CONSTRAINT customers_phone_unique UNIQUE (phone);
    
    RAISE NOTICE 'Successfully added unique constraint on phone column';
    
EXCEPTION
    WHEN unique_violation THEN
        RAISE NOTICE 'Cannot add unique constraint - duplicates still exist';
    WHEN others THEN
        RAISE NOTICE 'Error adding constraint: %', SQLERRM;
END $$;
```

### 4. Performance Indexes

```sql
-- Index on phone for fast lookups
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_customers_phone 
ON public.customers (phone);

-- Partial index on active customers with phone
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_customers_phone_active 
ON public.customers (phone) 
WHERE phone IS NOT NULL AND phone != '';
```

### 5. Helper Functions

```sql
-- Function to get customer by phone
CREATE OR REPLACE FUNCTION get_customer_by_phone(phone_number text)
RETURNS TABLE (
    id uuid,
    phone text,
    name text,
    address text,
    latitude double precision,
    longitude double precision,
    created_at timestamp with time zone,
    updated_at timestamp with time zone
) 
LANGUAGE sql
STABLE
AS $$
    SELECT id, phone, name, address, latitude, longitude, created_at, updated_at
    FROM public.customers 
    WHERE customers.phone = phone_number;
$$;
```

### 6. Updated Triggers

```sql
-- Auto-update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply trigger to customers table
DROP TRIGGER IF EXISTS update_customers_updated_at ON public.customers;
CREATE TRIGGER update_customers_updated_at
    BEFORE UPDATE ON public.customers
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
```

## Row Level Security (RLS) Policies

### Customers Table RLS

```sql
-- Enable RLS
ALTER TABLE public.customers ENABLE ROW LEVEL SECURITY;

-- Users can view own profile
CREATE POLICY "Users can view own profile" ON public.customers
    FOR SELECT USING (auth.uid() = id);

-- Users can update own profile
CREATE POLICY "Users can update own profile" ON public.customers
    FOR UPDATE USING (auth.uid() = id);

-- Users can insert own profile
CREATE POLICY "Users can insert own profile" ON public.customers
    FOR INSERT WITH CHECK (auth.uid() = id);
```

### Orders Table RLS

```sql
-- Enable RLS
ALTER TABLE public.orders ENABLE ROW LEVEL SECURITY;

-- Users can view own orders
CREATE POLICY "Users can view own orders" ON public.orders
    FOR SELECT USING (auth.uid() = customer_id);

-- Users can insert own orders
CREATE POLICY "Users can insert own orders" ON public.orders
    FOR INSERT WITH CHECK (auth.uid() = customer_id);
```

### Order Items Table RLS

```sql
-- Enable RLS
ALTER TABLE public.order_items ENABLE ROW LEVEL SECURITY;

-- Users can view order items for their orders
CREATE POLICY "Users can view own order items" ON public.order_items
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM public.orders 
            WHERE orders.id = order_items.order_id 
            AND orders.customer_id = auth.uid()
        )
    );

-- Users can insert order items for their orders
CREATE POLICY "Users can insert own order items" ON public.order_items
    FOR INSERT WITH CHECK (
        EXISTS (
            SELECT 1 FROM public.orders 
            WHERE orders.id = order_items.order_id 
            AND orders.customer_id = auth.uid()
        )
    );
```

## Verification Queries

```sql
-- Verify no duplicate phones
SELECT phone, COUNT(*) as count
FROM public.customers 
GROUP BY phone 
HAVING COUNT(*) > 1;

-- Verify unique constraint exists
SELECT constraint_name, constraint_type
FROM information_schema.table_constraints
WHERE table_name = 'customers' AND constraint_name LIKE '%phone%';

-- Show customer statistics
SELECT COUNT(*) as total_customers,
       COUNT(DISTINCT phone) as unique_phones,
       COUNT(*) - COUNT(DISTINCT phone) as duplicates
FROM public.customers;

-- Show recent customers
SELECT phone, name, address, created_at
FROM public.customers
ORDER BY created_at DESC
LIMIT 10;
```

## Android App Integration

### Customer Profile Management

**Location:** `app/src/main/java/com/kiranawala/data/repositories/ProfileRepositoryImpl.kt`

**Features:**
- ✅ Fetch user profile from Supabase Auth & Customer table
- ✅ Update customer name and address
- ✅ Phone number is read-only (managed by Supabase Auth)
- ✅ No email field in UI or data models
- ✅ Placeholder support ("Enter Name", "Enter Address")

**Data Flow:**
1. User authenticates with phone OTP (Supabase Auth)
2. Customer record created/fetched from `customers` table
3. Profile data loaded in ProfileScreen via `ProfileRepositoryImpl`
4. Edits saved to `customers` table via `CustomerDao`
5. Real-time sync with Supabase

### Key Classes

- **UserProfile** (`domain/models/UserProfile.kt`) - Profile data model without email
- **ProfileRepository** (`domain/repositories/ProfileRepository.kt`) - Profile operations interface
- **ProfileRepositoryImpl** (`data/repositories/ProfileRepositoryImpl.kt`) - Supabase integration
- **ProfileEditScreen** (`presentation/screens/profile/ProfileEditScreen.kt`) - Edit UI

## Migration Notes

### From Email-Based to Phone-Only

1. **Removed Fields:**
   - `customers.email` column
   - All email references in code

2. **Authentication:**
   - Uses Supabase Phone OTP only
   - No email verification required

3. **Profile Updates:**
   - Name and address are editable
   - Phone number is read-only (identity field)

4. **Data Consistency:**
   - Phone numbers are unique across all customers
   - No duplicate phone numbers allowed
   - Order history linked via `customer_id` (UUID)

## Support

For database issues or schema changes, refer to:
- [Supabase Documentation](https://supabase.com/docs)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- Project README.md for app-specific details