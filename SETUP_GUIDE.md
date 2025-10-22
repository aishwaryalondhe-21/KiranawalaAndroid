# Kiranawala Android App - Quick Setup Guide

## 🚀 Quick Start (5 Minutes)

### Step 1: Configure Supabase

1. **Create Supabase Project:**
   - Visit [supabase.com](https://supabase.com)
   - Click "New Project"
   - Fill in project details
   - Wait for project to be ready

2. **Get Your Credentials:**
   - Go to Project Settings → API
   - Copy **Project URL** (looks like: `https://xxxxx.supabase.co`)
   - Copy **anon/public key** (starts with `eyJ...`)

3. **Update Build Configuration:**
   
   Open `app/build.gradle.kts` and find these lines (around line 23-24):
   
   ```kotlin
   buildConfigField("String", "SUPABASE_URL", "\"https://your-project.supabase.co\"")
   buildConfigField("String", "SUPABASE_ANON_KEY", "\"your-anon-key-here\"")
   ```
   
   Replace with your actual values:
   
   ```kotlin
   buildConfigField("String", "SUPABASE_URL", "\"https://xxxxx.supabase.co\"")
   buildConfigField("String", "SUPABASE_ANON_KEY", "\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\"")
   ```

### Step 2: Create Database Schema

In Supabase SQL Editor, run:

```sql
-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create customers table
CREATE TABLE customers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email TEXT UNIQUE NOT NULL,
    phone TEXT NOT NULL,
    name TEXT NOT NULL,
    address TEXT DEFAULT '',
    latitude DOUBLE PRECISION DEFAULT 0.0,
    longitude DOUBLE PRECISION DEFAULT 0.0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create index on email for faster lookups
CREATE INDEX idx_customers_email ON customers(email);

-- Enable Row Level Security (RLS)
ALTER TABLE customers ENABLE ROW LEVEL SECURITY;

-- Create policy to allow users to read their own data
CREATE POLICY "Users can read own data" ON customers
    FOR SELECT USING (auth.uid() = id);

-- Create policy to allow users to insert their own data
CREATE POLICY "Users can insert own data" ON customers
    FOR INSERT WITH CHECK (auth.uid() = id);

-- Create policy to allow users to update their own data
CREATE POLICY "Users can update own data" ON customers
    FOR UPDATE USING (auth.uid() = id);
```

### Step 3: Enable Email Authentication

1. Go to **Authentication → Providers** in Supabase
2. Enable **Email** provider
3. Configure email templates (optional)
4. Disable email confirmation for testing (optional):
   - Go to **Authentication → Settings**
   - Turn off "Enable email confirmations"

### Step 4: Build and Run

1. **Open in Android Studio:**
   ```bash
   # Navigate to project
   cd c:\Kiranawala\KiranawalaAndroid
   
   # Open with Android Studio
   studio .
   ```

2. **Sync Gradle:**
   - Android Studio will prompt to sync
   - Click "Sync Now"
   - Wait for dependencies to download

3. **Run the App:**
   - Select an emulator or connect a device
   - Click Run button (▶️) or press `Shift + F10`
   - App should launch successfully!

---

## 📱 Testing the App

### Test Flow:

1. **Splash Screen:**
   - App shows "Kiranawala" logo
   - Auto-navigates to Login after 2 seconds

2. **Create Account:**
   - Click "Sign Up" on login screen
   - Fill in:
     - Full Name: `Test User`
     - Email: `test@example.com`
     - Phone: `9876543210`
     - Password: `Test@123` (min 8 chars)
     - Confirm Password: `Test@123`
   - Click "Sign Up"
   - Should navigate to Home screen

3. **Login:**
   - Use the same credentials
   - Email: `test@example.com`
   - Password: `Test@123`
   - Click "Login"
   - Should navigate to Home screen

4. **Verify in Supabase:**
   - Go to Supabase → Authentication → Users
   - You should see your test user
   - Go to Table Editor → customers
   - You should see customer data

---

## 🔧 Troubleshooting

### Build Errors

**Error: "Cannot find BuildConfig"**
- Solution: Sync Gradle and rebuild
- Make sure `buildFeatures { buildConfig = true }` is in build.gradle.kts

**Error: "Supabase credentials not found"**
- Solution: Check that you updated the BuildConfig fields correctly
- Rebuild the project after changing

### Runtime Errors

**Error: "Network error" or "Connection failed"**
- Check internet connection
- Verify Supabase URL is correct
- Check if Supabase project is active

**Error: "Invalid credentials"**
- Make sure email authentication is enabled in Supabase
- Check if email confirmation is disabled for testing
- Verify user exists in Supabase dashboard

**Error: "App crashes on startup"**
- Check Logcat for errors
- Verify all dependencies are synced
- Clean and rebuild project

### Common Issues

**Issue: Email already exists**
- Delete user from Supabase dashboard
- Or use a different email

**Issue: Password too weak**
- Use at least 8 characters
- Include letters and numbers

**Issue: Phone validation fails**
- Use 10-digit Indian phone number
- Format: 9876543210 (no spaces or +91)

---

## 🎯 What's Working

✅ **Authentication:**
- User registration
- User login
- Session management
- Token storage

✅ **Validation:**
- Email format validation
- Phone number validation (10 digits)
- Password strength validation
- Confirm password matching

✅ **UI/UX:**
- Material Design 3
- Loading states
- Error messages
- Smooth navigation

✅ **Data Persistence:**
- User data saved locally
- Auth token encrypted storage
- Room database ready

---

## 📋 Next Steps After Setup

Once the app is running successfully:

1. **Test All Features:**
   - Create multiple test accounts
   - Test login/logout
   - Verify data in Supabase

2. **Proceed to Phase 3:**
   - Implement store browsing
   - Add product catalog
   - Create store detail screens

3. **Optional Enhancements:**
   - Add forgot password
   - Implement OTP verification
   - Add biometric authentication
   - Enable auto-login

---

## 📞 Need Help?

### Check These First:
1. ✅ Supabase credentials are correct
2. ✅ Database schema is created
3. ✅ Email auth is enabled
4. ✅ Internet connection is working
5. ✅ Gradle sync completed successfully

### Debugging Tips:
- Check Logcat for errors (filter by "Kiranawala")
- Use `KiranaLogger` tags: AuthRepository, AuthViewModel
- Verify Supabase dashboard for user creation
- Test API calls in Supabase API docs

---

## ✨ Success Checklist

Before moving to Phase 3, verify:

- [ ] App builds without errors
- [ ] Splash screen shows and navigates
- [ ] Can create new account
- [ ] Can login with created account
- [ ] User data appears in Supabase
- [ ] Navigation works correctly
- [ ] Error messages display properly
- [ ] Loading states work
- [ ] App doesn't crash

---

**Ready to build? Let's go! 🚀**

If everything is set up correctly, you should have a fully functional authentication system ready for the next phase of development.
