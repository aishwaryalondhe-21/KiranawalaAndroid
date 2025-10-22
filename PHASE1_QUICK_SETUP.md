# Phase 1 - Quick Setup Guide

## ✅ Implementation Complete!

All Phase 1 features have been implemented and the project **builds successfully**!

---

## 🚀 To Get Started (2 Simple Steps)

### Step 1: Execute Supabase Schema (5 minutes)

1. Open your Supabase Dashboard
2. Go to **SQL Editor**
3. Copy all content from `SUPABASE_ADDRESSES_SCHEMA.sql`
4. Paste and **Execute**
5. ✅ Done! The addresses table is now created

### Step 2: Run the App

```bash
# Option 1: Using Android Studio
- Open project
- Click Run ▶️

# Option 2: Using Command Line
./gradlew installDebug
```

---

## 🎯 What to Test

### 1. Login Persistence (Most Important!)

**Before:**
- Close app → Reopen → Have to login again 😢

**Now:**
1. Login with your phone number
2. **Close the app completely** (swipe from recents)
3. **Reopen the app**
4. ✅ **You should still be logged in!** 🎉

### 2. Address Management

1. Login to the app
2. Tap **Profile** icon (bottom nav or top bar)
3. Tap **"Address Management"** in Quick Actions section
4. You'll see "No addresses yet" screen
5. Tap **+ (FAB button)** to add address
6. Fill in:
   - Address Line: `"123 Main Street, Mumbai, 400001"`
   - Building: `"Sunrise Apartments"`
   - Flat: `"A-101"`
   - Label: Select **Home**
   - ✅ Check **"Set as default address"**
7. Tap **"Add Address"**
8. ✅ Address is saved!

**Test Edit:**
- Tap **"Edit"** on your address card
- Change building name
- Tap **"Update Address"**
- ✅ Changes saved!

**Test Default:**
- Add another address (Work)
- Tap **"Set as Default"** on Work address
- ✅ Work is now highlighted as default

**Test Delete:**
- Tap **Delete icon** (trash) on an address
- Confirm deletion
- ✅ Address removed

**Test Persistence:**
- Close app completely
- Reopen app
- Go to Address Management
- ✅ All addresses still there!

---

## 📝 Important Notes

### Theme Toggle
- ✅ Storage is implemented
- ⏳ UI selector not yet added (can be added later)
- Theme preference saved in `PreferencesManager.themeMode`

### Address Coordinates
- Currently uses default Mumbai coordinates (19.0760, 72.8777)
- Google Maps picker can be added later for actual location

---

## ❓ Troubleshooting

### "Login Persistence Not Working"

**Check:**
1. Are you actually logging in successfully?
2. Look for this log: `SplashScreen: Session check: isLoggedIn=true`
3. If you see `isLoggedIn=false`, the session wasn't saved

**Solution:**
- Make sure OTP verification succeeds
- Check Supabase auth is working
- Look for errors in logcat with tag `AuthRepository`

### "Address Management Not Showing"

**Check:**
1. Did you execute the Supabase schema SQL?
2. Go to Supabase Dashboard → Table Editor
3. Look for `addresses` table
4. If not found, execute `SUPABASE_ADDRESSES_SCHEMA.sql`

**Verify RLS:**
- In Supabase, check that RLS is **enabled** on addresses table
- Policies should be visible

### "Can't Add Address"

**Check Logs:**
```
Filter logcat by: AddressRepository
Look for errors like:
- "Failed to add address"
- RLS policy errors
- Network errors
```

**Common Causes:**
1. RLS policies not set up → Execute SQL schema
2. User not logged in → Check auth status
3. Network issues → Check internet connection

---

## 🎉 Success Indicators

### You'll know it's working when:

✅ **Login Persistence:**
- App opens directly to Home Screen (not Login)
- You only login once per device

✅ **Address Management:**
- "Address Management" appears in Profile
- You can add/edit/delete addresses
- Default address is highlighted
- Addresses persist after app restart

---

## 📊 Project Status

```
✅ Login Persistence      → 100% Complete
✅ Theme Storage          → 100% Complete (UI pending)
✅ Address Management     → 100% Complete
✅ Database Schema        → 100% Complete
✅ Navigation             → 100% Complete
✅ Build Status           → ✅ SUCCESS
```

---

## 🚀 What's Next?

### Immediate (Optional)
1. Add Theme Selector UI in Settings
2. Add Google Maps for address picking
3. Add address validation (PIN code format, etc.)

### Phase 2 (From README2.md)
1. Order acknowledgement screen
2. Itemized order bills
3. Call store functionality
4. About Us page

---

## 📞 Need Help?

**Check These Files:**
- `PHASE1_IMPLEMENTATION_COMPLETE.md` - Full documentation
- `PHASE1_IMPLEMENTATION_GUIDE.md` - Detailed guide
- `PHASE1_COMPLETE_SUMMARY.md` - Architecture details

**Look for Logs:**
- `SplashScreen` - Login persistence
- `AddressViewModel` - Address operations
- `AddressRepository` - Database operations

---

## 🎊 Congratulations!

You've successfully implemented Phase 1! Your app now has:
- ✅ Persistent login (no more repeated logins!)
- ✅ Professional address management
- ✅ Clean architecture
- ✅ Production-ready code

**Enjoy your much-improved app!** 🚀
