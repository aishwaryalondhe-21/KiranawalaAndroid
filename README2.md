# 🛒 Kiranawala App – Version 2.0 (Zomato-Style Upgrade)

## 🚀 Overview
Kiranawala v2.0 is the next major update to the existing multi-store grocery marketplace.  
This version enhances design, usability, and functionality — taking inspiration from **Zomato’s structure and UI** — to deliver a modern, customer-friendly kirana shopping experience.

**Tech Stack:** Kotlin + Jetpack Compose + Supabase + Hilt + Room  
**Architecture:** MVVM + Repository Pattern + Clean Architecture  

---

## ✨ New & Enhanced Features in v2.0

### 🔐 Authentication & Persistence
- Login once and stay logged in automatically (persistent session)  
- Secure session storage using Supabase refresh tokens  
- Auto-login on app launch if session is valid  
- Manual logout clears all stored credentials  

### 🗺️ Address Management
- Map-based address picker (Google Maps SDK)  
- Fields for building name and flat number  
- Multiple addresses per user  
- Default address selection and persistence  

### 📦 Order Experience
- “Thank you for ordering” acknowledgement screen  
- Itemized order bill in order history (product name, quantity, price)  
- Option to call the store directly from order page  
- About Us page  

### 💬 Store Reviews System
- Add ratings and comments for stores  
- View all reviews with sorting (recent, top-rated)  
- Store rating auto-updated from user reviews  

### 💳 Payments & Billing
- UPI payment integration (GPay / PhonePe / Paytm via PSP)  
- Verified payment status (PAID / FAILED / REFUNDED)  
- Payment reference stored in order details  
- Payment audit log for all transactions  

### 🔔 Notifications
- Push notifications for order status updates  
- Separate permissions for Order, Offer, and General notifications  
- Settings screen to enable/disable categories  

### 🎨 UI & Personalization
- Light and dark theme options  
- Zomato-style visual redesign (store cards, icons, animations)  
- Smooth navigation and micro-interactions  

### 🧾 Legal & Info Pages
- About Us page  
- Privacy Policy  
- Terms & Conditions  

---

## 🧱 Database Changes (Supabase)
### New Tables
- `addresses` – multiple addresses per user with lat/lng and label  
- `store_reviews` – ratings and comments for stores  
- `payments` – UPI/PSP transaction records  
- `order_tracking` – order location and status timeline  

### Updated Tables
- `orders` – added `payment_reference`, `paid_at`, `tracking_lat`, `tracking_lng`  
- `stores` – rating auto-aggregated from reviews  

---

## 📆 Phased Development Roadmap

### 🟢 Phase 1 — Core Enhancements
**Objective:** Improve core experience and session persistence  
**Features:**
- Map-based address picker (Google Maps SDK)  
- Multiple addresses per user  
- Building/flat number fields  
- Default address selection  
- Persistent login with Supabase session restoration  
- Theme toggle (Light / Dark mode)

**Deliverables:**
- Address management module  
- Persistent session handling  
- Updated Settings screen with theme selector  

---

### 🟠 Phase 2 — Order Flow & Acknowledgement
**Objective:** Refine checkout and order detail flow  
**Features:**
- “Thank you for ordering” acknowledgement screen  
- Itemized bills in order history (products + prices)  
- Call Store option  
- About Us page  

**Deliverables:**
- Enhanced order confirmation flow  
- Updated order history UI  
- Store contact integration  

---

### 🟣 Phase 3 — Store Reviews System
**Objective:** Introduce store ratings and reviews  
**Features:**
- Add and view store reviews  
- Filter and sort by date/rating  
- Auto-update store average rating  

**Deliverables:**
- Reviews module (frontend + backend)  
- Updated Store Detail screen with “Reviews” tab  

---

### 🔵 Phase 4 — Payments Integration
**Objective:** Add verified UPI payments  
**Features:**
- UPI payment via PSP (Razorpay / Cashfree / Paytm)  
- Payment verification and status updates  
- Store payment reference in order details  

**Deliverables:**
- Payment SDK integration  
- Verified transaction flow  
- Updated checkout and payment UI  

---

### 🟢 Phase 5 — Order Tracking & Notifications
**Objective:** Add real-time map tracking and push alerts  
**Features:**
- Map-based order tracking (store → delivery path)  
- Status timeline (Placed → Processing → Out for Delivery → Delivered)  
- FCM push notifications for order updates  
- Notification permissions and settings  

**Deliverables:**
- Order tracking map module  
- FCM integration for order updates  
- Notification preferences in Settings  

---

### 🟡 Phase 6 — Legal & Informational Pages
**Objective:** Compliance and transparency  
**Features:**
- About Us page  
- Privacy Policy page  
- Terms & Conditions page  

**Deliverables:**
- Static content pages integrated in Settings  
- Backend endpoints for editable text content  

---

### 🔴 Phase 7 — Final Optimization & QA
**Objective:** Stabilization and production readiness  
**Tasks:**
- Comprehensive testing (manual + automated)  
- Bug fixes and UI performance optimization  
- Offline mode validation  
- Release preparation for Play Store  

**Deliverables:**
- Stable production build (v2.0)  
- Updated documentation and changelog  

---

## ✅ Summary of Phases
| Phase | Focus | Key Outcome |
|:------|:------|:------------|
| 1 | Core Enhancements | Address module, login persistence, theme toggle |
| 2 | Order Flow | Acknowledgement screen, itemized bills |
| 3 | Reviews System | Store ratings & comments |
| 4 | Payments | Verified UPI transactions |
| 5 | Tracking & Notifications | Map tracking + push alerts |
| 6 | Legal Pages | Privacy, T&C, About Us |
| 7 | QA & Optimization | Stable release build |

---

## ⚙️ Non-Functional Requirements
- **Performance:** API response < 500 ms, smooth scrolling & UI animations  
- **Security:** HTTPS + JWT; encrypted token storage  
- **Scalability:** Supports 10 000+ concurrent users  
- **Availability:** Target uptime ≥ 99 %  
- **Usability:** Minimal steps to checkout, modern Material 3 UI  
- **Offline Support:** Cached store/products; orders require online  
- **Maintainability:** Modular MVVM architecture  
- **Accessibility:** Font scaling, content descriptions for all UI elements  

---

## 🗺️ UI Screens Overview
1. Splash / Login / OTP  
2. Home (Store List)  
3. Store Details + Reviews  
4. Cart / Checkout / Payment  
5. Order Acknowledgement  
6. Order Tracking (Map + Timeline)  
7. Order History (Itemized)  
8. Profile / Settings / Addresses  
9. About Us / Privacy Policy / T&C  

---

## 🧭 Future Enhancements
- Loyalty / Rewards system  
- In-app chat between customer and store  
- Delivery personnel tracking module  
- Multi-language support  
- Web dashboard for store owners  

---

## 🏁 Status
- ✅ Phase 1–2 in progress  
- 🧩 Phases 3–4 next planned  
- 🛠️ Estimated Release: **v2.0 Stable — Q4 2025**

---

## 📄 Summary
Kiranawala v2.0 transforms the existing grocery marketplace into a polished, Zomato-style experience — combining powerful backend features with a visually rich, easy-to-use frontend for customers and kirana stores alike.
