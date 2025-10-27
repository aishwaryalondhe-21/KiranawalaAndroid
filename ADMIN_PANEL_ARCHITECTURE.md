# Kiranawala Web Admin Panel - Architecture Document

## Executive Summary

This document outlines the comprehensive architecture for a production-ready web-based admin panel for the Kiranawala grocery store application. The admin panel will enable store owners to manage orders, inventory, and their store operations efficiently.

---

## 1. Current System Analysis

### 1.1 Existing Android Application Overview

**Technology Stack:**
- **Language:** Kotlin 1.9.20
- **UI Framework:** Jetpack Compose + Material Design 3
- **Architecture:** Clean Architecture with MVVM pattern
- **Backend:** Supabase (PostgreSQL + Auth + Storage)
- **Local Database:** Room 2.6.1
- **Dependency Injection:** Hilt 2.48
- **Async Operations:** Kotlin Coroutines + Flow

**Application Type:** Multi-store grocery ordering platform for customers

### 1.2 Database Schema (Supabase PostgreSQL)

#### Core Tables

**1. customers**
```sql
- id (UUID, PK, references auth.users)
- phone (TEXT, UNIQUE, NOT NULL)
- name (TEXT, NOT NULL)
- address (TEXT)
- latitude (DOUBLE PRECISION)
- longitude (DOUBLE PRECISION)
- created_at (TIMESTAMPTZ)
- updated_at (TIMESTAMPTZ)
```

**2. stores**
```sql
- id (UUID, PK)
- name (TEXT, NOT NULL)
- address (TEXT, NOT NULL)
- latitude (DOUBLE PRECISION, NOT NULL)
- longitude (DOUBLE PRECISION, NOT NULL)
- contact (TEXT, NOT NULL)
- logo_url (TEXT)
- rating (DECIMAL(2,1), DEFAULT 4.5)
- minimum_order_value (DECIMAL(10,2), DEFAULT 100.00)
- delivery_fee (DECIMAL(10,2), DEFAULT 30.00)
- estimated_delivery_time (INTEGER, DEFAULT 30)
- is_open (BOOLEAN, DEFAULT true)
- subscription_status (TEXT, DEFAULT 'ACTIVE') -- ACTIVE, EXPIRED, SUSPENDED
- subscription_start_date (TIMESTAMPTZ)
- subscription_end_date (TIMESTAMPTZ)
- created_at (TIMESTAMPTZ)
- updated_at (TIMESTAMPTZ)
```

**3. products**
```sql
- id (UUID, PK)
- store_id (UUID, FK -> stores.id)
- name (TEXT, NOT NULL)
- description (TEXT)
- price (DECIMAL(10,2), NOT NULL)
- stock_quantity (INTEGER, NOT NULL, DEFAULT 0)
- image_url (TEXT)
- category (TEXT, DEFAULT 'General')
- is_available (BOOLEAN, DEFAULT true)
- created_at (TIMESTAMPTZ)
- updated_at (TIMESTAMPTZ)
```

**4. orders**
```sql
- id (UUID, PK)
- customer_id (UUID, FK -> auth.users.id)
- store_id (UUID, FK -> stores.id)
- total_amount (DECIMAL(10,2), NOT NULL)
- delivery_fee (DECIMAL(10,2), DEFAULT 30.00)
- status (TEXT, DEFAULT 'PENDING') -- PENDING, PROCESSING, COMPLETED, CANCELLED, FAILED
- payment_status (TEXT, DEFAULT 'PENDING') -- PENDING, PAID, FAILED, REFUNDED
- delivery_address (TEXT, NOT NULL)
- customer_phone (TEXT, NOT NULL)
- customer_name (TEXT, NOT NULL)
- created_at (TIMESTAMPTZ)
- updated_at (TIMESTAMPTZ)
```

**5. order_items**
```sql
- id (UUID, PK)
- order_id (UUID, FK -> orders.id)
- product_id (UUID, FK -> products.id)
- product_name (TEXT, NOT NULL) -- Snapshot at order time
- quantity (INTEGER, NOT NULL)
- price (DECIMAL(10,2), NOT NULL) -- Snapshot at order time
- created_at (TIMESTAMPTZ)
```

**6. addresses**
```sql
- id (UUID, PK)
- user_id (UUID, FK -> auth.users.id)
- address_type (TEXT) -- HOME, WORK, OTHER
- formatted_address (TEXT, NOT NULL)
- address_line1 (TEXT, NOT NULL)
- address_line2 (TEXT)
- city (TEXT, NOT NULL)
- state (TEXT, NOT NULL)
- pincode (TEXT, NOT NULL)
- latitude (DOUBLE PRECISION, NOT NULL)
- longitude (DOUBLE PRECISION, NOT NULL)
- receiver_name (TEXT, NOT NULL)
- receiver_phone (TEXT, NOT NULL)
- is_default (BOOLEAN, DEFAULT false)
- created_at (TIMESTAMPTZ)
- updated_at (TIMESTAMPTZ)
```

**7. store_reviews**
```sql
- id (UUID, PK)
- store_id (UUID, FK -> stores.id)
- customer_id (UUID, NOT NULL)
- customer_name (TEXT, NOT NULL)
- rating (INTEGER, 1-5)
- comment (TEXT)
- created_at (TIMESTAMPTZ)
- updated_at (TIMESTAMPTZ)
```

### 1.3 Order Management Workflow

**Order Status Lifecycle:**
```
PENDING → PROCESSING → COMPLETED
   ↓
CANCELLED / FAILED
```

**Status Definitions:**
- **PENDING:** Order placed by customer, awaiting store acceptance
- **PROCESSING:** Store accepted and is preparing the order
- **COMPLETED:** Order delivered successfully
- **CANCELLED:** Order cancelled (by customer or store)
- **FAILED:** Order failed due to technical/business reasons

**Payment Status:**
- **PENDING:** Payment not yet processed
- **PAID:** Payment successful
- **FAILED:** Payment failed
- **REFUNDED:** Payment refunded

### 1.4 Authentication & Authorization

**Current System (Customer App):**
- Phone OTP-based authentication via Supabase Auth
- No email required
- Session management via Supabase + local DataStore

**Row Level Security (RLS) Policies:**
- Customers can only view/manage their own orders
- Stores are publicly viewable (if subscription is ACTIVE)
- Products are publicly viewable (if available and store is active)
- Reviews are publicly viewable, but only owners can edit/delete

### 1.5 Business Logic Insights

**Key Features:**
1. **Multi-Store Support:** Platform supports multiple independent stores
2. **Subscription-Based:** Stores have subscription status (ACTIVE/EXPIRED/SUSPENDED)
3. **Location-Based Discovery:** Stores discovered based on customer location
4. **Real-Time Sync:** Orders sync in real-time with Supabase
5. **Offline Support:** Android app caches data locally with Room
6. **Inventory Management:** Stock tracking with availability flags
7. **Category-Based Organization:** Products organized by categories

---

## 2. Web Admin Panel Requirements

### 2.1 Target Users

**Primary Users:** Local shopkeepers/store owners (non-technical)

**User Characteristics:**
- Limited technical expertise
- Need simple, intuitive interfaces
- Primarily use desktop/tablet devices
- Require quick access to critical functions
- Need clear visual feedback and guidance

### 2.2 Core Functional Requirements

#### A. Order Management
1. **View Orders**
   - Real-time order list with filters (status, date range, customer)
   - Order details view with customer info, items, totals
   - Visual status indicators
   - Search and sort capabilities

2. **Update Order Status**
   - Accept/Reject pending orders
   - Mark orders as processing
   - Mark orders as completed
   - Cancel orders with reason
   - Status change notifications

3. **Order Analytics**
   - Daily/weekly/monthly order counts
   - Revenue tracking
   - Popular products
   - Peak order times

#### B. Inventory Management
1. **Product CRUD Operations**
   - Add new products with images
   - Edit product details (name, price, description, category)
   - Update stock quantities
   - Toggle product availability
   - Bulk operations (import/export)

2. **Category Management**
   - Create/edit/delete categories
   - Assign products to categories
   - Category-wise inventory view

3. **Stock Alerts**
   - Low stock warnings
   - Out-of-stock notifications
   - Stock history tracking

#### C. Store Profile Management
1. **Store Information**
   - Edit store name, address, contact
   - Update operating hours (is_open status)
   - Set minimum order value
   - Configure delivery fee
   - Update estimated delivery time

2. **Store Media**
   - Upload/update store logo
   - Manage store images

### 2.3 Non-Functional Requirements

1. **Performance**
   - Page load time < 2 seconds
   - Real-time order updates (< 5 second latency)
   - Support 100+ concurrent store admins
   - Optimistic UI updates

2. **Security**
   - Secure authentication (store owner accounts)
   - Role-based access control
   - Data encryption in transit (HTTPS)
   - Session timeout after inactivity
   - Audit logging for critical actions

3. **Usability**
   - Mobile-responsive design (tablet support)
   - Intuitive navigation (< 3 clicks to any feature)
   - Clear error messages
   - Undo capabilities for critical actions
   - Keyboard shortcuts for power users

4. **Reliability**
   - 99.9% uptime
   - Graceful error handling
   - Offline detection with user feedback
   - Data validation on client and server

5. **Scalability**
   - Support 1000+ stores
   - Handle 10,000+ products per store
   - Efficient pagination for large datasets

---

## 3. Recommended Technology Stack

### 3.1 Frontend Framework

**Recommendation: Next.js 14+ (App Router)**

**Rationale:**
- **React-based:** Industry standard with massive ecosystem
- **Server-Side Rendering (SSR):** Better SEO and initial load performance
- **API Routes:** Built-in backend API capabilities
- **File-based Routing:** Intuitive and scalable
- **TypeScript Support:** Type safety for production code
- **Optimized Performance:** Automatic code splitting, image optimization
- **Production-Ready:** Used by enterprise applications globally

**Alternative:** Vite + React (if pure SPA is preferred)

### 3.2 UI Component Library

**Recommendation: shadcn/ui + Tailwind CSS**

**Rationale:**
- **Modern & Beautiful:** Professional admin dashboard aesthetics
- **Accessible:** WCAG compliant components
- **Customizable:** Full control over component styling
- **Copy-Paste Components:** No npm bloat, own the code
- **Tailwind Integration:** Utility-first CSS for rapid development
- **TypeScript Native:** Full type safety

**Alternative:** Material-UI (MUI) for Material Design consistency

### 3.3 State Management

**Recommendation: TanStack Query (React Query) + Zustand**

**Rationale:**
- **TanStack Query:** 
  - Server state management (API calls, caching, synchronization)
  - Automatic background refetching
  - Optimistic updates
  - Built-in loading/error states
  
- **Zustand:**
  - Client state management (UI state, user preferences)
  - Minimal boilerplate
  - TypeScript-friendly
  - DevTools support

**Alternative:** Redux Toolkit (if team prefers Redux patterns)

### 3.4 Backend Integration

**Recommendation: Supabase JavaScript Client**

**Rationale:**
- **Existing Infrastructure:** Leverage current Supabase setup
- **Real-Time Subscriptions:** Live order updates
- **Row Level Security:** Secure data access
- **Built-in Auth:** Extend existing auth system
- **Type Generation:** Auto-generate TypeScript types from schema
- **Storage Integration:** Handle image uploads

### 3.5 Form Management

**Recommendation: React Hook Form + Zod**

**Rationale:**
- **React Hook Form:** Performant, minimal re-renders
- **Zod:** TypeScript-first schema validation
- **Type Safety:** End-to-end type safety
- **Error Handling:** Built-in validation error management

### 3.6 Data Visualization

**Recommendation: Recharts**

**Rationale:**
- **React-Native:** Built for React
- **Responsive:** Mobile-friendly charts
- **Customizable:** Full styling control
- **Lightweight:** Smaller bundle size than alternatives

**Alternative:** Chart.js with react-chartjs-2

### 3.7 Additional Libraries

- **Date Handling:** date-fns (lightweight alternative to moment.js)
- **Icons:** Lucide React (modern, tree-shakeable)
- **Notifications:** Sonner (beautiful toast notifications)
- **Tables:** TanStack Table (headless table library)
- **File Upload:** react-dropzone
- **Image Optimization:** Next.js Image component

---

## 4. Application Architecture

### 4.1 Architecture Pattern

**Pattern: Feature-Based Clean Architecture**

```
src/
├── app/                          # Next.js App Router
│   ├── (auth)/                   # Auth routes group
│   │   ├── login/
│   │   └── layout.tsx
│   ├── (dashboard)/              # Protected routes group
│   │   ├── orders/
│   │   ├── inventory/
│   │   ├── profile/
│   │   └── layout.tsx
│   └── api/                      # API routes (if needed)
├── components/                   # Shared UI components
│   ├── ui/                       # shadcn/ui components
│   ├── forms/                    # Form components
│   ├── layouts/                  # Layout components
│   └── charts/                   # Chart components
├── features/                     # Feature modules
│   ├── orders/
│   │   ├── components/
│   │   ├── hooks/
│   │   ├── services/
│   │   ├── types/
│   │   └── utils/
│   ├── inventory/
│   └── store-profile/
├── lib/                          # Core utilities
│   ├── supabase/                 # Supabase client & helpers
│   ├── auth/                     # Auth utilities
│   ├── validations/              # Zod schemas
│   └── utils/                    # Helper functions
├── hooks/                        # Shared React hooks
├── types/                        # TypeScript types
├── constants/                    # App constants
└── styles/                       # Global styles
```

### 4.2 Data Flow Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    UI Components                         │
│              (React Components + shadcn/ui)              │
└────────────────────┬────────────────────────────────────┘
                     │
                     ↓
┌─────────────────────────────────────────────────────────┐
│                 Custom Hooks Layer                       │
│         (useOrders, useProducts, useStore)               │
│              (React Query + Zustand)                     │
└────────────────────┬────────────────────────────────────┘
                     │
                     ↓
┌─────────────────────────────────────────────────────────┐
│                  Service Layer                           │
│        (orderService, productService, etc.)              │
│           (Business Logic & API Calls)                   │
└────────────────────┬────────────────────────────────────┘
                     │
                     ↓
┌─────────────────────────────────────────────────────────┐
│              Supabase Client Layer                       │
│         (Database, Auth, Storage, Realtime)              │
└────────────────────┬────────────────────────────────────┘
                     │
                     ↓
┌─────────────────────────────────────────────────────────┐
│                 Supabase Backend                         │
│          (PostgreSQL + Auth + Storage)                   │
└─────────────────────────────────────────────────────────┘
```

---

## 5. Authentication & Authorization Strategy

### 5.1 Store Admin Authentication

**Approach: Separate Admin User System**

Since the current system uses phone OTP for customers, we need a separate authentication mechanism for store admins.

**Recommended Solution:**

1. **Create Admin Users Table:**
```sql
CREATE TABLE store_admins (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  store_id UUID NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
  email TEXT NOT NULL UNIQUE,
  password_hash TEXT NOT NULL, -- Hashed via Supabase Auth
  full_name TEXT NOT NULL,
  role TEXT DEFAULT 'OWNER', -- OWNER, MANAGER, STAFF
  is_active BOOLEAN DEFAULT true,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);
```

2. **Use Supabase Email/Password Auth:**
   - Store admins log in with email/password
   - Separate from customer phone auth
   - Leverage Supabase Auth for security

3. **Role-Based Access Control (RBAC):**
   - **OWNER:** Full access to all features
   - **MANAGER:** Order management + inventory (no store settings)
   - **STAFF:** View-only access

### 5.2 RLS Policies for Admin Access

```sql
-- Store admins can only access their own store's data
CREATE POLICY "Admins can view own store orders"
ON orders FOR SELECT
USING (
  store_id IN (
    SELECT store_id FROM store_admins 
    WHERE id = auth.uid() AND is_active = true
  )
);

CREATE POLICY "Admins can update own store orders"
ON orders FOR UPDATE
USING (
  store_id IN (
    SELECT store_id FROM store_admins 
    WHERE id = auth.uid() AND is_active = true
  )
);
```

---

## 6. Key Technical Decisions

### 6.1 Real-Time Order Updates

**Implementation: Supabase Realtime Subscriptions**

```typescript
// Subscribe to new orders for the store
const subscription = supabase
  .channel('orders')
  .on(
    'postgres_changes',
    {
      event: 'INSERT',
      schema: 'public',
      table: 'orders',
      filter: `store_id=eq.${storeId}`
    },
    (payload) => {
      // Handle new order notification
      showNotification('New Order Received!');
      queryClient.invalidateQueries(['orders']);
    }
  )
  .subscribe();
```

### 6.2 Image Upload Strategy

**Implementation: Supabase Storage**

- **Product Images:** `products/{store_id}/{product_id}/{filename}`
- **Store Logos:** `stores/{store_id}/logo.{ext}`
- **Public Access:** Enable public URLs for images
- **Optimization:** Use Next.js Image component for automatic optimization

### 6.3 Offline Handling

**Strategy: Optimistic UI + Error Recovery**

- Show immediate feedback for user actions
- Queue failed requests for retry
- Display clear offline indicators
- Sync when connection restored

### 6.4 Performance Optimization

1. **Pagination:** Implement cursor-based pagination for large datasets
2. **Caching:** Aggressive caching with React Query (5-minute stale time)
3. **Code Splitting:** Route-based code splitting via Next.js
4. **Image Optimization:** Next.js Image component with lazy loading
5. **Database Indexes:** Ensure proper indexes on frequently queried columns

---

## 7. Security Considerations

1. **Input Validation:** Validate all inputs on client and server (Zod schemas)
2. **SQL Injection Prevention:** Use Supabase parameterized queries
3. **XSS Protection:** Sanitize user-generated content
4. **CSRF Protection:** Next.js built-in CSRF protection
5. **Rate Limiting:** Implement rate limiting on API routes
6. **Audit Logging:** Log all critical actions (order updates, inventory changes)
7. **Session Management:** Secure session cookies, automatic timeout
8. **HTTPS Only:** Enforce HTTPS in production

---

## 8. Deployment Strategy

**Recommended Platform: Vercel**

**Rationale:**
- **Next.js Native:** Built by Next.js creators
- **Zero Configuration:** Deploy with git push
- **Global CDN:** Fast worldwide performance
- **Automatic HTTPS:** SSL certificates included
- **Preview Deployments:** Test before production
- **Environment Variables:** Secure secret management

**Alternative:** Netlify, AWS Amplify, or self-hosted

---

## 9. Monitoring & Analytics

1. **Error Tracking:** Sentry for error monitoring
2. **Performance Monitoring:** Vercel Analytics or Google Analytics
3. **User Analytics:** PostHog or Mixpanel
4. **Uptime Monitoring:** UptimeRobot or Pingdom
5. **Database Monitoring:** Supabase built-in monitoring

---

## 10. Accessibility & Internationalization

1. **Accessibility (a11y):**
   - WCAG 2.1 Level AA compliance
   - Keyboard navigation support
   - Screen reader compatibility
   - High contrast mode support

2. **Internationalization (i18n):**
   - Support for English and Hindi (Phase 1)
   - Use next-intl for translations
   - RTL support for future languages

---

**Document Version:** 1.0  
**Last Updated:** 2025-10-27  
**Author:** Kiranawala Development Team

