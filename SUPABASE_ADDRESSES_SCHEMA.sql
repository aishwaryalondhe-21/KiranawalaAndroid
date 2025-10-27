-- ============================================
-- Kiranawala v2.0 - Addresses Table Schema
-- Phase 1: Multiple Addresses per User
-- ============================================

-- Create addresses table (v2 schema)
CREATE TABLE IF NOT EXISTS public.addresses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    address_type TEXT NOT NULL DEFAULT 'HOME', -- HOME, WORK, OTHER
    formatted_address TEXT NOT NULL,
    address_line1 TEXT NOT NULL,
    address_line2 TEXT,
    city TEXT NOT NULL,
    state TEXT NOT NULL,
    pincode TEXT NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    receiver_name TEXT NOT NULL,
    receiver_phone TEXT NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Optional: constrain address_type to allowed values
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'addresses_address_type_check'
    ) THEN
        ALTER TABLE public.addresses
            ADD CONSTRAINT addresses_address_type_check
            CHECK (address_type IN ('HOME', 'WORK', 'OTHER'));
    END IF;
END $$;

-- Create index for user_id lookup
CREATE INDEX IF NOT EXISTS idx_addresses_user_id ON public.addresses(user_id);

-- Create index for default address lookup
CREATE INDEX IF NOT EXISTS idx_addresses_user_default ON public.addresses(user_id, is_default);

-- ============================================
-- Row Level Security (RLS) Policies
-- ============================================

-- Enable RLS
ALTER TABLE public.addresses ENABLE ROW LEVEL SECURITY;

-- Policy: Users can view their own addresses
CREATE POLICY "Users can view own addresses"
ON public.addresses
FOR SELECT
USING (auth.uid() = user_id);

-- Policy: Users can insert their own addresses
CREATE POLICY "Users can insert own addresses"
ON public.addresses
FOR INSERT
WITH CHECK (auth.uid() = user_id);

-- Policy: Users can update their own addresses
CREATE POLICY "Users can update own addresses"
ON public.addresses
FOR UPDATE
USING (auth.uid() = user_id)
WITH CHECK (auth.uid() = user_id);

-- Policy: Users can delete their own addresses
CREATE POLICY "Users can delete own addresses"
ON public.addresses
FOR DELETE
USING (auth.uid() = user_id);

-- ============================================
-- Trigger: Auto-update updated_at timestamp
-- ============================================

CREATE OR REPLACE FUNCTION public.handle_addresses_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER addresses_updated_at
    BEFORE UPDATE ON public.addresses
    FOR EACH ROW
    EXECUTE FUNCTION public.handle_addresses_updated_at();

-- ============================================
-- Trigger: Ensure only one default address per user
-- ============================================

CREATE OR REPLACE FUNCTION public.ensure_single_default_address()
RETURNS TRIGGER AS $$
BEGIN
    -- If setting this address as default, unset all other defaults for this user
    IF NEW.is_default = true THEN
        UPDATE public.addresses
        SET is_default = false
        WHERE user_id = NEW.user_id 
        AND id != NEW.id
        AND is_default = true;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER addresses_single_default
    BEFORE INSERT OR UPDATE ON public.addresses
    FOR EACH ROW
    EXECUTE FUNCTION public.ensure_single_default_address();

-- ============================================
-- Sample Data (Optional - for testing)
-- ============================================

-- Note: Replace 'YOUR_USER_UUID' with actual user UUID from auth.users
/*
INSERT INTO public.addresses (
    user_id,
    address_type,
    formatted_address,
    address_line1,
    address_line2,
    city,
    state,
    pincode,
    latitude,
    longitude,
    receiver_name,
    receiver_phone,
    is_default
)
VALUES 
    (
        'YOUR_USER_UUID',
        'HOME',
        '123 Main Street, Mumbai, Maharashtra 400001',
        '123 Main Street',
        'Fort',
        'Mumbai',
        'Maharashtra',
        '400001',
        19.0760,
        72.8777,
        'Aishwarya Sharma',
        '+919999999999',
        true
    ),
    (
        'YOUR_USER_UUID',
        'WORK',
        '456 Office Road, Bengaluru, Karnataka 560001',
        '456 Office Road',
        'Business District',
        'Bengaluru',
        'Karnataka',
        '560001',
        12.9716,
        77.5946,
        'Aishwarya Sharma',
        '+919988887777',
        false
    );
*/
