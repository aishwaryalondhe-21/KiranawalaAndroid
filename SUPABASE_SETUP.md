# Supabase Integration - Phone OTP Authentication Setup Guide

## Current Status
- ✅ Supabase dependencies added (v2.1.3)
- ✅ Hilt DI configured (v2.50)
- ✅ Auth repository implemented with phone OTP authentication
- ✅ ViewModels updated with Hilt injection
- ✅ Phone OTP authentication fully working
- ✅ Build successful and app running

## Required Supabase Configuration

### Step 1: Create Supabase Project
1. Go to https://supabase.com
2. Create a new project
3. Get your project URL and anon key from Settings > API

### Step 2: Update BuildConfig
Replace the placeholder values in `app/build.gradle.kts`:
```kotlin
buildConfigField("String", "SUPABASE_URL", "\"https://fnblhmddgregqfafqkeh.supabase.co\"")
buildConfigField("String", "SUPABASE_ANON_KEY", "\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZuYmxobWRkZ3JlZ3FmYWZxa2VoIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjA3MjI5OTQsImV4cCI6MjA3NjI5ODk5NH0.CPmWxu5-VYKDhVlQGC5C8btnKpW_SeWPfp3vT19EbEc\"")
```

**Important:** Make sure the URL is exactly correct. Common typo: `fnblhmddqregqfafqkeh` (wrong) vs `fnblhmddgregqfafqkeh` (correct)

### Step 3: Database Schema (Run in Supabase SQL Editor)

```sql
-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Customers table (syncs with auth.users) - Phone-based authentication
CREATE TABLE customers (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    phone TEXT UNIQUE NOT NULL,
    address TEXT,
    latitude DOUBLE PRECISION DEFAULT 0.0,
    longitude DOUBLE PRECISION DEFAULT 0.0,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Enable Row Level Security
ALTER TABLE customers ENABLE ROW LEVEL SECURITY;

-- Policy: Users can only read/update their own data
CREATE POLICY "Users can view own customer data" 
    ON customers FOR SELECT 
    USING (auth.uid() = id);

CREATE POLICY "Users can update own customer data" 
    ON customers FOR UPDATE 
    USING (auth.uid() = id);

CREATE POLICY "Users can insert own customer data" 
    ON customers FOR INSERT 
    WITH CHECK (auth.uid() = id);

-- Function to automatically create customer profile on signup (Phone-based)
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.customers (id, name, phone)
    VALUES (
        NEW.id,
        COALESCE(NEW.raw_user_meta_data->>'name', 'User'),
        COALESCE(NEW.raw_user_meta_data->>'phone', NEW.phone, '')
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Trigger to create customer profile on user signup
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW
    EXECUTE FUNCTION public.handle_new_user();

-- Updated at trigger
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_customers_updated_at
    BEFORE UPDATE ON customers
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
```

### Step 4: Configure Phone Authentication in Supabase Dashboard

1. Go to **Authentication** → **Providers** → **Phone**
2. **Enable Phone provider**
3. **Enable "Allow new users to sign up"**
4. For testing, add test phone numbers:
   - Format: `+919307393578=123456` (phone=otp)
   - Set expiry date for test OTPs
5. For production, configure SMS provider (Twilio/MessageBird)

## Phase 1: Phone OTP Authentication ✅
- [x] Phone number input with validation
- [x] OTP sending via Supabase
- [x] OTP verification
- [x] Session management
- [x] Logout functionality
- [x] Local session persistence
- [x] Test OTP support for development

## Phase 2: Authorization ✅
- [x] User profile management
- [x] Role-based access (via RLS policies)
- [x] Secure token storage
- [x] Auto session refresh

## Implementation Details

### Files Modified/Created:
1. **DI Modules**
   - `SupabaseModule.kt` - Provides Supabase client with GoTrue auth
   - `AppModule.kt` - Core app dependencies
   - `RepositoryModule.kt` - Repository bindings

2. **Repository**
   - `AuthRepositoryImpl.kt` - Phone OTP auth implementation with Supabase
     - `sendOTP()` - Sends OTP using `signInWith(OTP)` with `createUser = true`
     - `verifyOTP()` - Verifies OTP and creates user session
     - Session management and profile sync

3. **ViewModels**
   - `AuthViewModel.kt` - Handles phone auth UI state and events

4. **UI Screens**
   - `PhoneAuthScreen.kt` - Unified phone authentication screen
     - Phone number input with country code
     - OTP input and verification
     - New user name collection
     - Loading states and error handling

5. **Configuration**
   - `app/build.gradle.kts` - Supabase URL and anon key configuration
   - `AndroidManifest.xml` - Cleartext traffic enabled for debugging

### Key Features Implemented:
- ✅ Phone number validation (E.164 format)
- ✅ Automatic session persistence
- ✅ Local database sync with Room
- ✅ Error handling with user-friendly messages
- ✅ Loading states during OTP operations
- ✅ Form validation for phone and OTP
- ✅ Secure token storage
- ✅ Test OTP support for development

## Common Issues and Solutions:

### Issue 1: "Unable to resolve host" error
**Cause:** Typo in Supabase URL
**Solution:** Verify URL is exactly `fnblhmddgregqfafqkeh` (not `fnblhmddqregqfafqkeh`)

### Issue 2: "Signups not allowed for otp" error
**Cause:** Using `signUpWith(OTP)` which is blocked by Supabase
**Solution:** Use `signInWith(OTP)` with `createUser = true` parameter

### Issue 3: OTP not received
**Cause:** Using test phone numbers without SMS provider
**Solution:** For testing, use hardcoded OTP from Supabase dashboard. For production, configure Twilio/MessageBird.

### Issue 4: Build fails after clean
**Cause:** Old generated files cached
**Solution:** Uninstall app from device, clean build, and reinstall fresh APK

## Testing Checklist:
- [x] User can enter phone number with country code
- [x] OTP is sent successfully (test mode)
- [x] User can verify OTP with test code (123456)
- [x] New user can enter name during signup
- [x] Session persists across app restarts
- [x] User can logout
- [x] Error messages display correctly
- [x] Loading states work properly

## Production Deployment Steps:
1. Configure Twilio or MessageBird in Supabase
2. Remove test phone numbers
3. Update RLS policies for production data
4. Test with real phone numbers
5. Monitor authentication metrics in Supabase dashboard
