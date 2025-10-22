# Phone OTP Authentication - Quick Setup Guide

## ✅ Current Status
Phone OTP authentication is **fully implemented and working** for testing!

## 🚀 Quick Start

### 1. Supabase Configuration (Already Done)
- **Project URL:** `https://fnblhmddgregqfafqkeh.supabase.co`
- **Anon Key:** Configured in `app/build.gradle.kts`
- **Phone Provider:** Enabled in Supabase dashboard

### 2. Test Phone Number
- **Phone:** `+919307393578`
- **OTP:** `123456` (hardcoded for testing)
- **Valid Until:** October 21, 2025

### 3. Testing Flow
1. Open the app
2. Enter phone number: `+919307393578`
3. Check "I'm a new user" (if signing up)
4. Enter name (for new users)
5. Click "Send OTP"
6. Enter OTP: `123456`
7. Click "Verify OTP"
8. ✅ You're logged in!

## 📱 How It Works

### Authentication Flow
```
User enters phone → Send OTP → Supabase validates phone
                                ↓
User enters OTP ← OTP sent ← Supabase sends test OTP (123456)
       ↓
Verify OTP → Supabase validates → Create session → Login success
```

### Code Implementation
- **Repository:** `AuthRepositoryImpl.kt`
  - `sendOTP()` - Uses `signInWith(OTP)` with `createUser = true`
  - `verifyOTP()` - Verifies OTP and creates user session
- **UI:** `PhoneAuthScreen.kt` - Unified login/signup screen
- **ViewModel:** `AuthViewModel.kt` - Handles auth state

## 🔧 Key Configuration

### build.gradle.kts
```kotlin
buildConfigField("String", "SUPABASE_URL", "\"https://fnblhmddgregqfafqkeh.supabase.co\"")
buildConfigField("String", "SUPABASE_ANON_KEY", "\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\"")
```

### Supabase Dashboard Settings
1. **Authentication** → **Providers** → **Phone**
2. ✅ Enable Phone provider
3. ✅ Enable "Allow new users to sign up"
4. ✅ Test phone number: `+919307393578=123456`

## ⚠️ Important Notes

### Common Typo Warning
**Wrong URL:** `fnblhmddqregqfafqkeh` (has 'q' instead of 'g')  
**Correct URL:** `fnblhmddgregqfafqkeh` (has 'g')

### Why signInWith instead of signUpWith?
- `signUpWith(OTP)` is blocked by Supabase with "Signups not allowed for otp"
- `signInWith(OTP)` with `createUser = true` works for both login AND signup
- This is the recommended approach for phone OTP authentication

### Test Mode vs Production
**Test Mode (Current):**
- No SMS sent
- Use hardcoded OTP: `123456`
- Works until October 21, 2025

**Production Mode (Future):**
- Configure Twilio or MessageBird in Supabase
- Real SMS sent to users
- Remove test phone numbers
- Users receive actual OTP codes

## 🐛 Troubleshooting

### Error: "Unable to resolve host"
**Cause:** Typo in Supabase URL  
**Solution:** Verify URL is `fnblhmddgregqfafqkeh` (with 'g', not 'q')

### Error: "Signups not allowed for otp"
**Cause:** Using `signUpWith(OTP)`  
**Solution:** Use `signInWith(OTP)` with `createUser = true`

### OTP not received
**Cause:** Using test mode without SMS provider  
**Solution:** Enter hardcoded OTP `123456` from Supabase dashboard

### Build fails after changes
**Solution:**
1. Uninstall app from device
2. Run `./gradlew clean`
3. Run `./gradlew assembleDebug`
4. Install fresh APK

## 📊 Database Schema

```sql
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
```

## 🎯 Next Steps for Production

1. **Configure SMS Provider:**
   - Sign up for Twilio or MessageBird
   - Get API credentials
   - Configure in Supabase → Authentication → Providers → Phone

2. **Update Phone Settings:**
   - Remove test phone numbers
   - Configure SMS template
   - Set rate limits

3. **Test with Real Numbers:**
   - Test with your own phone number
   - Verify SMS delivery
   - Test OTP expiration

4. **Monitor Usage:**
   - Check Supabase dashboard for auth metrics
   - Monitor SMS costs
   - Set up alerts for failures

## ✅ Success Checklist

- [x] Supabase project created
- [x] Phone provider enabled
- [x] Test phone number configured
- [x] App connects to Supabase
- [x] OTP sending works
- [x] OTP verification works
- [x] User session created
- [x] Local database synced
- [ ] SMS provider configured (for production)
- [ ] Real phone numbers tested (for production)

## 📚 Related Documentation

- **[SUPABASE_SETUP.md](SUPABASE_SETUP.md)** - Complete Supabase setup guide
- **[README.md](README.md)** - Project overview
- **[IMPLEMENTATION_STATUS.md](IMPLEMENTATION_STATUS.md)** - Detailed status

---

**Status:** ✅ Phone OTP Authentication Working!  
**Last Updated:** October 20, 2025
