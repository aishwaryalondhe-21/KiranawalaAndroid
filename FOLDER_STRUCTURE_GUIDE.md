# Kiranawala Project - Folder Structure Guide

## 📁 New Project Organization

The Kiranawala project is now organized into separate folders for the Android app and the web admin panel:

```
c:\Kiranawala\KiranawalaAndroid\
│
├── 📱 Android App (Existing)
│   ├── app/                          # Android app source code
│   │   ├── src/
│   │   │   ├── main/
│   │   │   │   ├── java/com/kiranawala/
│   │   │   │   │   ├── domain/
│   │   │   │   │   ├── data/
│   │   │   │   │   ├── presentation/
│   │   │   │   │   └── ...
│   │   │   │   └── res/
│   │   │   └── test/
│   │   ├── build.gradle.kts
│   │   └── proguard-rules.pro
│   ├── gradle/                       # Gradle wrapper
│   ├── build.gradle.kts              # Root build config
│   ├── settings.gradle.kts           # Gradle settings
│   ├── gradlew                       # Gradle wrapper script
│   ├── gradlew.bat                   # Gradle wrapper (Windows)
│   ├── local.properties              # Local configuration
│   └── README.md                     # Android app documentation
│
├── 🌐 Admin Panel (New)
│   └── admin-panel/
│       ├── docs/                     # Documentation
│       │   ├── ADMIN_PANEL_ARCHITECTURE.md
│       │   ├── ADMIN_PANEL_IMPLEMENTATION_PLAN.md
│       │   ├── ADMIN_PANEL_TECHNICAL_SPECS.md
│       │   ├── ADMIN_PANEL_QUICK_REFERENCE.md
│       │   ├── ADMIN_PANEL_EXECUTIVE_SUMMARY.md
│       │   └── ADMIN_PANEL_ARCHITECTURE_DIAGRAMS.md
│       ├── app/                      # Next.js App Router (to be created)
│       │   ├── (auth)/
│       │   │   ├── login/
│       │   │   └── forgot-password/
│       │   ├── (dashboard)/
│       │   │   ├── orders/
│       │   │   ├── products/
│       │   │   ├── analytics/
│       │   │   └── store/
│       │   ├── api/
│       │   ├── layout.tsx
│       │   └── page.tsx
│       ├── components/               # React components (to be created)
│       │   ├── ui/
│       │   ├── layouts/
│       │   ├── orders/
│       │   ├── products/
│       │   └── analytics/
│       ├── features/                 # Feature modules (to be created)
│       │   ├── orders/
│       │   ├── products/
│       │   └── analytics/
│       ├── lib/                      # Utilities (to be created)
│       │   ├── supabase/
│       │   ├── validations/
│       │   └── utils/
│       ├── hooks/                    # Custom hooks (to be created)
│       ├── types/                    # TypeScript types (to be created)
│       ├── services/                 # Business logic (to be created)
│       ├── store/                    # Zustand stores (to be created)
│       ├── styles/                   # Global styles (to be created)
│       ├── README.md                 # Admin panel documentation
│       ├── package.json              # Dependencies (to be created)
│       ├── tsconfig.json             # TypeScript config (to be created)
│       ├── tailwind.config.ts        # Tailwind config (to be created)
│       ├── next.config.js            # Next.js config (to be created)
│       └── middleware.ts             # Next.js middleware (to be created)
│
└── 📄 Root Level Files (Existing)
    ├── README.md                     # Main project README
    ├── SUPABASE.md                   # Supabase documentation
    ├── SUPABASE_SCHEMA_PHASE3_4.sql  # Database schema
    ├── SUPABASE_ADDRESSES_SCHEMA.sql
    ├── SUPABASE_REVIEWS_SCHEMA.sql
    ├── .git/                         # Git repository
    └── ... (other existing files)
```

---

## 🎯 Folder Organization Benefits

### 1. **Clear Separation of Concerns**
- **Android App:** All mobile application code in one place
- **Admin Panel:** All web admin dashboard code in separate folder
- **Easy to maintain:** Each project has its own dependencies, configuration, and build process

### 2. **Independent Development**
- Teams can work on Android and admin panel simultaneously
- No conflicts between different technology stacks
- Separate CI/CD pipelines for each project

### 3. **Scalability**
- Easy to add new projects (e.g., customer web app, delivery app)
- Clear structure for future expansion
- Monorepo-like organization without monorepo complexity

### 4. **Documentation**
- Each project has its own README
- Admin panel has comprehensive documentation in `docs/` folder
- Easy for new team members to understand structure

---

## 📂 Admin Panel Folder Structure Details

### `admin-panel/docs/`
Contains all documentation files:
- **ADMIN_PANEL_ARCHITECTURE.md** - Complete architecture design
- **ADMIN_PANEL_IMPLEMENTATION_PLAN.md** - 7-phase implementation roadmap
- **ADMIN_PANEL_TECHNICAL_SPECS.md** - Technical specifications and code examples
- **ADMIN_PANEL_QUICK_REFERENCE.md** - Quick lookup guide for developers
- **ADMIN_PANEL_EXECUTIVE_SUMMARY.md** - Executive overview for stakeholders
- **ADMIN_PANEL_ARCHITECTURE_DIAGRAMS.md** - Visual architecture diagrams

### `admin-panel/app/`
Next.js App Router structure:
- `(auth)/` - Public authentication routes (login, forgot password)
- `(dashboard)/` - Protected dashboard routes (orders, products, analytics, store)
- `api/` - API routes for backend functionality
- `layout.tsx` - Root layout component
- `page.tsx` - Root page component

### `admin-panel/components/`
Reusable React components:
- `ui/` - shadcn/ui components (buttons, inputs, cards, etc.)
- `layouts/` - Layout components (DashboardLayout, Sidebar, TopNav)
- `orders/` - Order-related components
- `products/` - Product-related components
- `analytics/` - Analytics-related components

### `admin-panel/features/`
Feature modules with their own structure:
- `orders/` - Order management feature
- `products/` - Product management feature
- `analytics/` - Analytics feature

Each feature module contains:
- `api/` - React Query hooks
- `components/` - Feature-specific components
- `hooks/` - Feature-specific hooks
- `services/` - Business logic
- `types/` - TypeScript types

### `admin-panel/lib/`
Utility functions and helpers:
- `supabase/` - Supabase client and utilities
- `validations/` - Zod validation schemas
- `utils/` - Helper functions

### `admin-panel/hooks/`
Custom React hooks:
- `useAuth.ts` - Authentication hook
- `useOrders.ts` - Orders hook
- `useProducts.ts` - Products hook
- `useStore.ts` - Store hook
- `useNotifications.ts` - Notifications hook

### `admin-panel/types/`
TypeScript type definitions:
- `index.ts` - Main types export
- `auth.ts` - Authentication types
- `orders.ts` - Order types
- `products.ts` - Product types
- `store.ts` - Store types
- `analytics.ts` - Analytics types

### `admin-panel/services/`
Business logic services:
- `authService.ts` - Authentication logic
- `orderService.ts` - Order management logic
- `productService.ts` - Product management logic
- `storeService.ts` - Store management logic
- `analyticsService.ts` - Analytics logic

### `admin-panel/store/`
Zustand state management stores:
- `authStore.ts` - Authentication state
- `uiStore.ts` - UI state (sidebar, theme, etc.)
- `notificationStore.ts` - Notifications state

### `admin-panel/styles/`
Global styles:
- `globals.css` - Global styles
- `variables.css` - CSS variables

---

## 🚀 Getting Started with Admin Panel

### Step 1: Navigate to Admin Panel Folder
```bash
cd admin-panel
```

### Step 2: Install Dependencies (Phase 1)
```bash
npm install
```

### Step 3: Review Documentation
Start with these files in order:
1. `docs/ADMIN_PANEL_EXECUTIVE_SUMMARY.md` - Overview
2. `docs/ADMIN_PANEL_ARCHITECTURE.md` - Architecture details
3. `docs/ADMIN_PANEL_IMPLEMENTATION_PLAN.md` - Implementation roadmap
4. `docs/ADMIN_PANEL_TECHNICAL_SPECS.md` - Technical details
5. `docs/ADMIN_PANEL_QUICK_REFERENCE.md` - Quick reference

### Step 4: Set Up Environment
Create `.env.local`:
```
NEXT_PUBLIC_SUPABASE_URL=your_supabase_url
NEXT_PUBLIC_SUPABASE_ANON_KEY=your_supabase_anon_key
```

### Step 5: Start Development
```bash
npm run dev
```

---

## 📊 Project Statistics

### Android App
- **Language:** Kotlin
- **Framework:** Jetpack Compose
- **Database:** Supabase
- **Status:** Production

### Admin Panel
- **Language:** TypeScript
- **Framework:** Next.js 14
- **Database:** Supabase (shared)
- **Status:** Planning & Documentation Complete
- **Documentation Files:** 6 comprehensive documents
- **Timeline:** 8-12 weeks for full implementation

---

## 🔄 Workflow

### For Android App Development
```bash
# Navigate to root (Android app is at root level)
cd c:\Kiranawala\KiranawalaAndroid

# Build Android app
./gradlew build

# Run tests
./gradlew test
```

### For Admin Panel Development
```bash
# Navigate to admin panel
cd c:\Kiranawala\KiranawalaAndroid\admin-panel

# Install dependencies
npm install

# Start development server
npm run dev

# Build for production
npm run build

# Run tests
npm test
```

---

## 📝 Documentation Files Location

All admin panel documentation is in:
```
admin-panel/docs/
├── ADMIN_PANEL_ARCHITECTURE.md
├── ADMIN_PANEL_IMPLEMENTATION_PLAN.md
├── ADMIN_PANEL_TECHNICAL_SPECS.md
├── ADMIN_PANEL_QUICK_REFERENCE.md
├── ADMIN_PANEL_EXECUTIVE_SUMMARY.md
└── ADMIN_PANEL_ARCHITECTURE_DIAGRAMS.md
```

---

## 🎯 Next Steps

1. **Review Documentation**
   - Start with Executive Summary
   - Review Architecture document
   - Check Implementation Plan

2. **Set Up Development Environment**
   - Install Node.js 18+
   - Install npm or pnpm
   - Clone repository

3. **Begin Phase 1 Implementation**
   - Create Next.js project
   - Set up Supabase integration
   - Implement authentication

4. **Establish CI/CD Pipeline**
   - Set up GitHub Actions
   - Configure Vercel deployment
   - Set up monitoring

---

## 📞 Support

For questions about the folder structure or documentation:
- Check `admin-panel/README.md` for admin panel overview
- Check `admin-panel/docs/ADMIN_PANEL_QUICK_REFERENCE.md` for quick lookup
- Review relevant documentation file for detailed information

---

**Last Updated:** 2025-10-27  
**Status:** Folder structure organized and documented  
**Version:** 1.0

