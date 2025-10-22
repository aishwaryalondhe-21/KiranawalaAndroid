# Clear App Cache & Debug Store Visibility

## Quick Solution Steps:

### 1. Clear App Cache (Do this FIRST)
```bash
# Uninstall the app completely from your device/emulator
adb uninstall com.kiranawalaandroid

# OR manually: Go to Settings > Apps > Kiranawala > Uninstall
```

### 2. Rebuild and Install Fresh
```bash
./gradlew clean
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 3. Check Logcat for Debug Info
Look for these logs in Android Studio Logcat:
- "Found X total stores in database"
- "Store: [name] | Status: [status] | Open: [true/false]"
- "Store: [name] | Distance: Xkm | Within 50km: [true/false]"
- "Found X active stores after filtering"

## What I Changed:

### Repository Debug Logging
- Now fetches ALL stores first and logs their status
- Shows distance calculations for each store
- Logs filtering decisions

### ViewModel Radius
- Temporarily increased radius from 5km to 50km
- This eliminates distance as a factor

## Expected Results:
After clearing cache, you should see all 6 stores because:
1. ✅ All stores in DB are ACTIVE and is_open=true
2. ✅ 50km radius includes all of Pune area
3. ✅ Fresh install eliminates cached data

## If Still Only 3 Stores:
Check logcat for:
1. "Found 6 total stores" - confirms DB connection
2. "Found 6 active stores after filtering" - confirms no status issues
3. Distance logs - confirms no location filtering issues
4. Any error messages

## Restore Normal Behavior:
Once confirmed working, change radius back to 5.0km in StoreListViewModel.kt