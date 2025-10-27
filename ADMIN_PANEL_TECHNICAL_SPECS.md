# Kiranawala Web Admin Panel - Technical Specifications

## 1. Technology Stack Details

### Frontend Stack
```json
{
  "framework": "Next.js 14.0+",
  "language": "TypeScript 5.0+",
  "ui_library": "shadcn/ui",
  "styling": "Tailwind CSS 3.3+",
  "state_management": {
    "server_state": "TanStack Query 5.0+",
    "client_state": "Zustand 4.4+",
    "forms": "React Hook Form 7.0+"
  },
  "validation": "Zod 3.22+",
  "icons": "Lucide React 0.263+",
  "charts": "Recharts 2.10+",
  "notifications": "Sonner 1.2+",
  "tables": "TanStack Table 8.0+",
  "date_handling": "date-fns 2.30+",
  "file_upload": "react-dropzone 14.2+",
  "http_client": "Supabase JS Client 2.38+"
}
```

### Backend Stack
```json
{
  "database": "Supabase PostgreSQL",
  "auth": "Supabase Auth",
  "storage": "Supabase Storage",
  "realtime": "Supabase Realtime",
  "api": "Supabase PostgREST"
}
```

### Development Tools
```json
{
  "package_manager": "npm 10.0+ or pnpm 8.0+",
  "linter": "ESLint 8.0+",
  "formatter": "Prettier 3.0+",
  "testing": {
    "unit": "Jest 29.0+",
    "component": "React Testing Library 14.0+",
    "e2e": "Playwright 1.40+"
  },
  "version_control": "Git 2.40+",
  "deployment": "Vercel"
}
```

---

## 2. Database Schema Extensions

### New Tables Required

#### store_admins
```sql
CREATE TABLE store_admins (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  store_id UUID NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
  email TEXT NOT NULL UNIQUE,
  full_name TEXT NOT NULL,
  role TEXT NOT NULL DEFAULT 'OWNER' CHECK (role IN ('OWNER', 'MANAGER', 'STAFF')),
  is_active BOOLEAN DEFAULT true,
  last_login TIMESTAMPTZ,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_store_admins_store_id ON store_admins(store_id);
CREATE INDEX idx_store_admins_email ON store_admins(email);
```

#### audit_logs
```sql
CREATE TABLE audit_logs (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  admin_id UUID NOT NULL REFERENCES store_admins(id) ON DELETE SET NULL,
  store_id UUID NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
  action TEXT NOT NULL,
  entity_type TEXT NOT NULL,
  entity_id UUID,
  changes JSONB,
  ip_address TEXT,
  user_agent TEXT,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_audit_logs_store_id ON audit_logs(store_id);
CREATE INDEX idx_audit_logs_admin_id ON audit_logs(admin_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at DESC);
```

#### order_status_history
```sql
CREATE TABLE order_status_history (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
  old_status TEXT,
  new_status TEXT NOT NULL,
  changed_by UUID REFERENCES store_admins(id) ON DELETE SET NULL,
  reason TEXT,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_order_status_history_order_id ON order_status_history(order_id);
```

### RLS Policies for Admin Access

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

CREATE POLICY "Admins can view own store products"
ON products FOR SELECT
USING (
  store_id IN (
    SELECT store_id FROM store_admins 
    WHERE id = auth.uid() AND is_active = true
  )
);

CREATE POLICY "Admins can update own store products"
ON products FOR UPDATE
USING (
  store_id IN (
    SELECT store_id FROM store_admins 
    WHERE id = auth.uid() AND is_active = true
  )
);
```

---

## 3. API Endpoints Structure

### Authentication Endpoints
```
POST   /api/auth/login              - Admin login
POST   /api/auth/logout             - Admin logout
POST   /api/auth/refresh            - Refresh session
GET    /api/auth/me                 - Get current admin
POST   /api/auth/forgot-password    - Request password reset
POST   /api/auth/reset-password     - Reset password
```

### Order Endpoints
```
GET    /api/orders                  - List orders (with filters)
GET    /api/orders/:id              - Get order details
PATCH  /api/orders/:id/status       - Update order status
POST   /api/orders/:id/cancel       - Cancel order
GET    /api/orders/analytics/daily  - Daily order analytics
GET    /api/orders/export           - Export orders (CSV/PDF)
```

### Product Endpoints
```
GET    /api/products                - List products (with filters)
GET    /api/products/:id            - Get product details
POST   /api/products                - Create product
PATCH  /api/products/:id            - Update product
DELETE /api/products/:id            - Delete product
POST   /api/products/bulk-import    - Bulk import products
PATCH  /api/products/:id/stock      - Update stock
```

### Store Endpoints
```
GET    /api/store                   - Get store info
PATCH  /api/store                   - Update store info
POST   /api/store/logo              - Upload store logo
GET    /api/store/analytics         - Store analytics
```

### Category Endpoints
```
GET    /api/categories              - List categories
POST   /api/categories              - Create category
PATCH  /api/categories/:id          - Update category
DELETE /api/categories/:id          - Delete category
```

---

## 4. Component Architecture

### Core Components

#### Layout Components
```
components/
├── layouts/
│   ├── DashboardLayout.tsx         # Main dashboard layout
│   ├── AuthLayout.tsx              # Auth pages layout
│   ├── Sidebar.tsx                 # Navigation sidebar
│   ├── TopNav.tsx                  # Top navigation bar
│   └── Footer.tsx                  # Footer component
```

#### Feature Components
```
components/
├── orders/
│   ├── OrdersTable.tsx             # Orders listing table
│   ├── OrderDetails.tsx            # Order details view
│   ├── OrderStatusBadge.tsx        # Status indicator
│   ├── OrderFilters.tsx            # Filter controls
│   └── OrderActions.tsx            # Action buttons
├── products/
│   ├── ProductsTable.tsx           # Products listing
│   ├── ProductForm.tsx             # Add/edit form
│   ├── ProductImage.tsx            # Image upload
│   └── StockUpdate.tsx             # Stock management
├── analytics/
│   ├── DashboardCards.tsx          # KPI cards
│   ├── OrderChart.tsx              # Order trends
│   ├── RevenueChart.tsx            # Revenue chart
│   └── CategoryChart.tsx           # Category breakdown
```

#### UI Components (shadcn/ui)
```
components/ui/
├── button.tsx
├── card.tsx
├── dialog.tsx
├── form.tsx
├── input.tsx
├── select.tsx
├── table.tsx
├── tabs.tsx
├── toast.tsx
├── dropdown-menu.tsx
├── pagination.tsx
└── ... (other shadcn components)
```

---

## 5. State Management Strategy

### Server State (TanStack Query)
```typescript
// hooks/useOrders.ts
export function useOrders(filters?: OrderFilters) {
  return useQuery({
    queryKey: ['orders', filters],
    queryFn: () => orderService.fetchOrders(filters),
    staleTime: 5 * 60 * 1000, // 5 minutes
    refetchInterval: 30 * 1000, // 30 seconds
  });
}

// hooks/useOrderDetails.ts
export function useOrderDetails(orderId: string) {
  return useQuery({
    queryKey: ['orders', orderId],
    queryFn: () => orderService.fetchOrderById(orderId),
  });
}

// hooks/useUpdateOrderStatus.ts
export function useUpdateOrderStatus() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: UpdateStatusData) => 
      orderService.updateOrderStatus(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['orders'] });
    },
  });
}
```

### Client State (Zustand)
```typescript
// store/uiStore.ts
interface UIState {
  sidebarOpen: boolean;
  theme: 'light' | 'dark';
  notifications: Notification[];
  
  toggleSidebar: () => void;
  setTheme: (theme: 'light' | 'dark') => void;
  addNotification: (notification: Notification) => void;
  removeNotification: (id: string) => void;
}

export const useUIStore = create<UIState>((set) => ({
  sidebarOpen: true,
  theme: 'light',
  notifications: [],
  
  toggleSidebar: () => set((state) => ({ 
    sidebarOpen: !state.sidebarOpen 
  })),
  // ... other methods
}));
```

---

## 6. Type Definitions

### Core Types
```typescript
// types/index.ts

// Order Types
export interface Order {
  id: string;
  customerId: string;
  storeId: string;
  totalAmount: number;
  deliveryFee: number;
  status: OrderStatus;
  paymentStatus: PaymentStatus;
  deliveryAddress: string;
  customerPhone: string;
  customerName: string;
  items: OrderItem[];
  createdAt: Date;
  updatedAt: Date;
}

export type OrderStatus = 
  | 'PENDING' 
  | 'PROCESSING' 
  | 'COMPLETED' 
  | 'CANCELLED' 
  | 'FAILED';

export type PaymentStatus = 
  | 'PENDING' 
  | 'PAID' 
  | 'FAILED' 
  | 'REFUNDED';

// Product Types
export interface Product {
  id: string;
  storeId: string;
  name: string;
  description: string;
  price: number;
  stockQuantity: number;
  imageUrl?: string;
  category: string;
  isAvailable: boolean;
  createdAt: Date;
  updatedAt: Date;
}

// Store Admin Types
export interface StoreAdmin {
  id: string;
  storeId: string;
  email: string;
  fullName: string;
  role: 'OWNER' | 'MANAGER' | 'STAFF';
  isActive: boolean;
  lastLogin?: Date;
  createdAt: Date;
  updatedAt: Date;
}

// Filter Types
export interface OrderFilters {
  status?: OrderStatus;
  dateFrom?: Date;
  dateTo?: Date;
  customerName?: string;
  minAmount?: number;
  maxAmount?: number;
  page?: number;
  limit?: number;
}
```

---

## 7. Service Layer

### Order Service
```typescript
// services/orderService.ts
export class OrderService {
  async fetchOrders(filters?: OrderFilters): Promise<Order[]> {
    // Implementation
  }

  async fetchOrderById(id: string): Promise<Order> {
    // Implementation
  }

  async updateOrderStatus(
    orderId: string, 
    status: OrderStatus
  ): Promise<void> {
    // Implementation
  }

  async cancelOrder(orderId: string, reason: string): Promise<void> {
    // Implementation
  }

  async exportOrders(format: 'csv' | 'pdf'): Promise<Blob> {
    // Implementation
  }
}
```

### Product Service
```typescript
// services/productService.ts
export class ProductService {
  async fetchProducts(filters?: ProductFilters): Promise<Product[]> {
    // Implementation
  }

  async createProduct(data: CreateProductData): Promise<Product> {
    // Implementation
  }

  async updateProduct(id: string, data: UpdateProductData): Promise<Product> {
    // Implementation
  }

  async deleteProduct(id: string): Promise<void> {
    // Implementation
  }

  async updateStock(id: string, quantity: number): Promise<void> {
    // Implementation
  }

  async uploadImage(file: File): Promise<string> {
    // Implementation
  }
}
```

---

## 8. Real-Time Implementation

### Supabase Realtime Subscriptions
```typescript
// hooks/useOrderSubscription.ts
export function useOrderSubscription(storeId: string) {
  const queryClient = useQueryClient();

  useEffect(() => {
    const subscription = supabase
      .channel(`orders:${storeId}`)
      .on(
        'postgres_changes',
        {
          event: '*',
          schema: 'public',
          table: 'orders',
          filter: `store_id=eq.${storeId}`,
        },
        (payload) => {
          queryClient.invalidateQueries({ queryKey: ['orders'] });
          
          if (payload.eventType === 'INSERT') {
            showNotification('New order received!');
          }
        }
      )
      .subscribe();

    return () => {
      subscription.unsubscribe();
    };
  }, [storeId, queryClient]);
}
```

---

## 9. Error Handling Strategy

### Error Types
```typescript
// types/errors.ts
export class AppError extends Error {
  constructor(
    public code: string,
    public message: string,
    public statusCode: number = 500
  ) {
    super(message);
  }
}

export class ValidationError extends AppError {
  constructor(message: string) {
    super('VALIDATION_ERROR', message, 400);
  }
}

export class AuthenticationError extends AppError {
  constructor(message: string = 'Authentication failed') {
    super('AUTH_ERROR', message, 401);
  }
}

export class AuthorizationError extends AppError {
  constructor(message: string = 'Access denied') {
    super('AUTHZ_ERROR', message, 403);
  }
}
```

### Error Handling Hook
```typescript
// hooks/useErrorHandler.ts
export function useErrorHandler() {
  const { addNotification } = useUIStore();

  return (error: unknown) => {
    if (error instanceof AppError) {
      addNotification({
        type: 'error',
        message: error.message,
      });
    } else if (error instanceof Error) {
      addNotification({
        type: 'error',
        message: error.message,
      });
    } else {
      addNotification({
        type: 'error',
        message: 'An unexpected error occurred',
      });
    }
  };
}
```

---

## 10. Performance Optimization

### Image Optimization
```typescript
// Use Next.js Image component
import Image from 'next/image';

<Image
  src={product.imageUrl}
  alt={product.name}
  width={200}
  height={200}
  priority={false}
  loading="lazy"
/>
```

### Code Splitting
```typescript
// Dynamic imports for heavy components
const AnalyticsDashboard = dynamic(
  () => import('@/components/analytics/Dashboard'),
  { loading: () => <LoadingSkeleton /> }
);
```

### Caching Strategy
```typescript
// React Query cache configuration
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000,
      gcTime: 10 * 60 * 1000,
      retry: 1,
    },
  },
});
```

---

## 11. Security Implementation

### Input Validation
```typescript
// lib/validations/order.ts
import { z } from 'zod';

export const updateOrderStatusSchema = z.object({
  orderId: z.string().uuid(),
  status: z.enum(['PENDING', 'PROCESSING', 'COMPLETED', 'CANCELLED', 'FAILED']),
  reason: z.string().optional(),
});

export type UpdateOrderStatusInput = z.infer<typeof updateOrderStatusSchema>;
```

### CSRF Protection
```typescript
// Next.js built-in CSRF protection via SameSite cookies
// Configure in next.config.js
```

### Rate Limiting
```typescript
// middleware/rateLimit.ts
import { Ratelimit } from '@upstash/ratelimit';

const ratelimit = new Ratelimit({
  redis: Redis.fromEnv(),
  limiter: Ratelimit.slidingWindow(10, '1 h'),
});
```

---

**Document Version:** 1.0  
**Last Updated:** 2025-10-27

