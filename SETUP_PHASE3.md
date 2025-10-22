# Phase 3 Setup Guide - Quick Start

## 🚀 Quick Setup (5 minutes)

### Step 1: Run Database Schema in Supabase

1. **Open Supabase Dashboard**
   - Go to: https://supabase.com/dashboard
   - Select your project: `fnblhmddgregqfafqkeh`

2. **Open SQL Editor**
   - Click **SQL Editor** in the left sidebar
   - Click **New Query**

3. **Copy and Run Schema**
   - Open file: `SUPABASE_SCHEMA_PHASE3_4.sql`
   - Copy ALL contents
   - Paste into SQL Editor
   - Click **Run** (or press Ctrl+Enter)

4. **Verify Tables Created**
   - Go to **Table Editor** in left sidebar
   - You should see:
     - ✅ stores (3 sample stores)
     - ✅ products (15 sample products)
     - ✅ orders (empty)
     - ✅ order_items (empty)

### Step 2: Build and Run the App

1. **Clean and Build**
   ```bash
   cd c:\Kiranawala\KiranawalaAndroid
   .\gradlew.bat clean assembleDebug
   ```

2. **Install on Device**
   - Uninstall old version first
   - Install new APK from: `app/build/outputs/apk/debug/app-debug.apk`

3. **Test the App**
   - Login with test phone: `+919307393578`
   - OTP: `123456`
   - You should see the Store List screen with 3 stores

---

## 📱 Testing Flow

### 1. Store List Screen
- ✅ See 3 nearby stores (Pune area)
- ✅ Search for "Sharma" - should find 1 store
- ✅ Search for "Patel" - should find 1 store
- ✅ Click on any store to view details

### 2. Store Detail Screen
- ✅ See store information (name, rating, address)
- ✅ See minimum order value, delivery fee, time
- ✅ See products list (5 products per store)
- ✅ Search products (try "Milk", "Salt", "Bread")
- ✅ Filter by category (Groceries, Dairy, Bakery, etc.)
- ✅ Click "Add" button on products (cart not implemented yet)

---

## 🗺️ Sample Data Overview

### Stores Created
1. **Sharma Kirana Store**
   - Location: MG Road, Pune
   - Min Order: ₹150
   - Delivery: ₹30
   - Products: 5 items

2. **Patel General Store**
   - Location: FC Road, Pune
   - Min Order: ₹100
   - Delivery: ₹25
   - Products: 5 items (auto-created by schema)

3. **Gupta Provision Store**
   - Location: Deccan, Pune
   - Min Order: ₹200
   - Delivery: ₹40
   - Products: 5 items (auto-created by schema)

### Sample Products (in Sharma Kirana Store)
1. Tata Salt - ₹20 (Groceries)
2. Amul Milk - ₹60 (Dairy)
3. Britannia Bread - ₹40 (Bakery)
4. Fortune Oil - ₹150 (Groceries)
5. Maggi Noodles - ₹144 (Instant Food)

---

## 🔍 Verification Queries

Run these in Supabase SQL Editor to verify data:

```sql
-- Check stores
SELECT id, name, address, minimum_order_value, is_open 
FROM stores;

-- Check products
SELECT p.name, p.price, p.category, s.name as store_name 
FROM products p 
JOIN stores s ON p.store_id = s.id;

-- Check RLS policies
SELECT tablename, policyname, cmd 
FROM pg_policies 
WHERE tablename IN ('stores', 'products');
```

---

## 🐛 Troubleshooting

### Issue: No stores showing
**Solution:**
- Check internet connection
- Verify Supabase URL is correct in `build.gradle.kts`
- Check Logcat for errors
- Verify RLS policies allow public read access

### Issue: Products not loading
**Solution:**
- Verify products table has data
- Check store_id foreign keys are correct
- Ensure `is_available = true` in products table

### Issue: Search not working
**Solution:**
- Check Logcat for errors
- Verify Postgrest is installed in Supabase
- Test search queries directly in Supabase SQL Editor

### Issue: Build errors
**Solution:**
```bash
.\gradlew.bat clean
.\gradlew.bat assembleDebug
```

---

## 📊 Expected Logcat Output

When app loads successfully, you should see:

```
D/StoreRepository: Fetching nearby stores for lat=18.5204, lon=73.8567, radius=5.0 km
D/StoreRepository: Found 3 nearby stores
D/StoreListViewModel: Loading nearby stores
```

When clicking on a store:

```
D/StoreRepository: Fetching store by ID: <store-id>
D/ProductRepository: Fetching products for store: <store-id>
D/ProductRepository: Found 5 products for store <store-id>
```

---

## ✅ Success Criteria

Phase 3 is working correctly if:

- [x] App shows 3 stores on home screen
- [x] Store cards display name, rating, address, min order, delivery fee
- [x] Search finds stores by name
- [x] Clicking store opens detail screen
- [x] Store detail shows store info and products
- [x] Product search works
- [x] Category filter works
- [x] No crashes or errors in Logcat

---

## 🎯 Next Steps

After verifying Phase 3 works:

1. **Test offline mode** - Turn off internet, app should show cached data
2. **Test search** - Try various search queries
3. **Test filters** - Filter products by different categories
4. **Review UI** - Check if design matches Material Design 3
5. **Move to Phase 4** - Implement shopping cart and checkout

---

## 📞 Support

If you encounter issues:

1. Check `PHASE3_IMPLEMENTATION_SUMMARY.md` for detailed info
2. Review Logcat output for errors
3. Verify Supabase dashboard shows correct data
4. Check `SUPABASE_SETUP.md` for common issues

---

**Setup Time:** ~5 minutes  
**Last Updated:** October 20, 2025  
**Status:** Ready for Testing ✅
