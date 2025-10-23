# Phase 3: Store Reviews System - Implementation Plan

## Overview
Implement a comprehensive store reviews and ratings system allowing customers to rate and review stores, view all reviews with sorting, and automatically update store ratings.

**Phase:** 3 of 7  
**Objective:** Introduce store ratings and reviews  
**Status:** 📋 Planning Complete - Ready for Implementation

---

## Requirements (from README2.md)

### Features to Implement:
1. ✅ Add ratings and comments for stores
2. ✅ View all reviews with sorting (recent, top-rated)
3. ✅ Auto-update store average rating from reviews

### Deliverables:
- Reviews module (frontend + backend)
- Updated Store Detail screen with "Reviews" tab

---

## Architecture Design

### Clean Architecture Layers:

```
┌─────────────────────────────────────────┐
│  UI Layer (Jetpack Compose)             │
│  - ReviewsListScreen                     │
│  - AddReviewDialog (enhanced)            │
│  - ReviewCard                            │
│  - StoreDetailScreen (with tabs)         │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│  ViewModel Layer                         │
│  - ReviewsViewModel                      │
│    • StateFlow<ReviewsState>             │
│    • Sorting logic (recent/top-rated)    │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│  Use Cases Layer                         │
│  - AddStoreReviewUseCase                 │
│  - GetStoreReviewsUseCase                │
│  - DeleteStoreReviewUseCase              │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│  Repository Layer                        │
│  - StoreReviewRepository (interface)     │
│  - StoreReviewRepositoryImpl             │
│    • Supabase (remote)                   │
│    • Room (local cache)                  │
└─────────────────────────────────────────┘
```

---

## Database Schema

### Supabase Table: `store_reviews`

```sql
CREATE TABLE store_reviews (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    store_id UUID NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    customer_id UUID NOT NULL,
    customer_name TEXT NOT NULL,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Indexes for performance
CREATE INDEX idx_store_reviews_store_id ON store_reviews(store_id);
CREATE INDEX idx_store_reviews_customer_id ON store_reviews(customer_id);
CREATE INDEX idx_store_reviews_created_at ON store_reviews(created_at DESC);
CREATE INDEX idx_store_reviews_rating ON store_reviews(rating DESC);

-- Enable Row Level Security
ALTER TABLE store_reviews ENABLE ROW LEVEL SECURITY;

-- Policies
CREATE POLICY "Anyone can view reviews" ON store_reviews
    FOR SELECT USING (true);

CREATE POLICY "Users can insert own reviews" ON store_reviews
    FOR INSERT WITH CHECK (auth.uid()::text = customer_id);

CREATE POLICY "Users can update own reviews" ON store_reviews
    FOR UPDATE USING (auth.uid()::text = customer_id);

CREATE POLICY "Users can delete own reviews" ON store_reviews
    FOR DELETE USING (auth.uid()::text = customer_id);
```

### Room Entity: `StoreReviewEntity`

```kotlin
@Entity(tableName = "store_reviews")
data class StoreReviewEntity(
    @PrimaryKey val id: String,
    val storeId: String,
    val customerId: String,
    val customerName: String,
    val rating: Int, // 1-5
    val comment: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
```

---

## Domain Models

### StoreReview

```kotlin
package com.kiranawala.domain.models

import kotlinx.datetime.LocalDateTime

data class StoreReview(
    val id: String,
    val storeId: String,
    val customerId: String,
    val customerName: String,
    val rating: Int, // 1-5 stars
    val comment: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
```

---

## Implementation Steps

### Step 1: Domain Layer (Models, Repository Interface, Use Cases)
**Files to Create:**
1. `domain/models/StoreReview.kt` - Core domain model
2. `domain/repositories/StoreReviewRepository.kt` - Interface
3. `domain/use_cases/review/AddStoreReviewUseCase.kt`
4. `domain/use_cases/review/GetStoreReviewsUseCase.kt`
5. `domain/use_cases/review/DeleteStoreReviewUseCase.kt`

**Estimated Time:** 30 minutes

---

### Step 2: Data Layer (Entities, DAOs, Repository Implementation)
**Files to Create:**
1. `data/local/entities/StoreReviewEntity.kt`
2. `data/local/dao/StoreReviewDao.kt`
3. `data/repositories/StoreReviewRepositoryImpl.kt`
4. `data/remote/dto/StoreReviewDto.kt` (for Supabase)

**Files to Modify:**
1. `data/local/db/AppDatabase.kt` - Add StoreReviewEntity
2. `di/DatabaseModule.kt` - Provide StoreReviewDao
3. `di/RepositoryModule.kt` - Provide StoreReviewRepository

**Estimated Time:** 1 hour

---

### Step 3: Update Store Rating Logic
**Files to Modify:**
1. `data/repositories/StoreRepositoryImpl.kt`
   - Add method to recalculate rating from reviews
   - Update rating after adding/deleting review

**Estimated Time:** 20 minutes

---

### Step 4: Presentation Layer (ViewModel)
**Files to Create:**
1. `presentation/viewmodels/ReviewsViewModel.kt`
   - State management for reviews
   - Sorting logic (recent, top-rated)
   - Add/delete review actions

**State Classes:**
```kotlin
sealed class ReviewsState {
    object Loading : ReviewsState()
    data class Success(val reviews: List<StoreReview>) : ReviewsState()
    data class Empty(val message: String) : ReviewsState()
    data class Error(val message: String) : ReviewsState()
}

enum class ReviewsSortOption {
    RECENT,      // Sort by created_at DESC
    TOP_RATED    // Sort by rating DESC, then created_at DESC
}
```

**Estimated Time:** 45 minutes

---

### Step 5: UI Components
**Files to Create:**
1. `presentation/components/review/ReviewCard.kt` - Display single review
2. `presentation/components/review/AddReviewDialog.kt` - Enhanced with comment field
3. `presentation/components/review/ReviewSortOptions.kt` - Sort dropdown/chips

**ReviewCard Features:**
- Customer name and date
- Star rating display (1-5 stars)
- Comment text
- Delete button (if user is owner)

**AddReviewDialog Features:**
- Star rating selector (1-5)
- Comment text field (multiline, optional)
- Character limit (500 chars)
- Submit/Cancel buttons

**Estimated Time:** 1 hour

---

### Step 6: Update StoreDetailScreen with Tabs
**Files to Modify:**
1. `presentation/screens/store/StoreDetailScreen.kt`

**Changes:**
- Add TabRow with "Products" and "Reviews" tabs
- State management for selected tab
- Conditional rendering:
  - Tab 0: Existing product grid
  - Tab 1: Reviews list
- Show reviews count badge on Reviews tab

**Estimated Time:** 45 minutes

---

### Step 7: Reviews List Screen
**Files to Create:**
1. `presentation/screens/store/ReviewsListContent.kt`

**Features:**
- Display all reviews for a store
- Sort dropdown (Recent / Top Rated)
- Empty state when no reviews
- Loading state
- Error state with retry
- Pull-to-refresh
- FAB for "Add Review"

**Estimated Time:** 45 minutes

---

### Step 8: Database Setup
**Files to Create:**
1. `SUPABASE_REVIEWS_SCHEMA.sql` - SQL script for creating table

**Manual Step:**
- Run SQL in Supabase dashboard

**Estimated Time:** 10 minutes

---

### Step 9: Integration & Testing
**Testing Checklist:**
- [ ] Add review with rating only
- [ ] Add review with rating and comment
- [ ] View reviews list (empty state)
- [ ] View reviews list (with data)
- [ ] Sort by Recent
- [ ] Sort by Top Rated
- [ ] Delete own review
- [ ] Store rating updates after adding review
- [ ] Store rating updates after deleting review
- [ ] Offline caching works
- [ ] Reviews sync when online
- [ ] Long comments display properly
- [ ] Customer name displays correctly
- [ ] Date formatting is readable

**Estimated Time:** 1 hour

---

## Total Estimated Time: 6-7 hours

---

## Key Design Decisions

### 1. Rating Scale
- **Decision:** 1-5 star system (standard)
- **Reason:** Familiar to users, easy to aggregate

### 2. Comment Field
- **Decision:** Optional, max 500 characters
- **Reason:** Not all users want to write comments; limit prevents abuse

### 3. Review Ownership
- **Decision:** One review per customer per store
- **Reason:** Prevents spam; can be updated later
- **Implementation:** Use UNIQUE constraint on (store_id, customer_id) or check in repository

### 4. Rating Calculation
- **Decision:** Simple average of all ratings
- **Reason:** Easy to understand and compute
- **Future:** Can add weighted average based on recency

### 5. Offline Support
- **Decision:** Cache reviews locally with Room
- **Reason:** Consistent with app's offline-first architecture
- **Limitation:** New reviews require online connection

### 6. Delete Permission
- **Decision:** Only review author can delete their own review
- **Reason:** Security and data integrity
- **Implementation:** Check customerId matches current user

---

## UI/UX Considerations

### Review Card Design
- **Avatar:** Use first letter of customer name in a circular badge
- **Rating:** Show filled/outlined stars (Material Icons)
- **Date:** Relative time (e.g., "2 days ago") using kotlinx.datetime
- **Comment:** Expand/collapse if longer than 3 lines

### Empty State
- Icon: Star outline
- Message: "No reviews yet. Be the first to review this store!"
- Action: "Write a Review" button

### Loading State
- Shimmer effect for review cards
- Skeleton screens

### Error State
- Error icon with message
- "Retry" button

---

## Potential Challenges & Solutions

### Challenge 1: Rating Recalculation Performance
**Problem:** Calculating average from many reviews can be slow  
**Solution:** 
- Use Supabase aggregate function: `SELECT AVG(rating) FROM store_reviews WHERE store_id = ?`
- Cache result in Store model
- Update only when review added/deleted

### Challenge 2: One Review Per User Enforcement
**Problem:** Prevent duplicate reviews from same customer  
**Solution:**
- Add UNIQUE constraint in Supabase: `UNIQUE(store_id, customer_id)`
- Handle conflict in repository (upsert instead of insert)
- Show "Update Review" instead of "Add Review" if exists

### Challenge 3: Review Spam Prevention
**Problem:** Users might submit multiple reviews  
**Solution:**
- Enforce one review per user per store
- Character limit on comments
- Future: Add reporting/moderation system

### Challenge 4: Offline Add Review
**Problem:** Reviews need server to update store rating  
**Solution:**
- Show pending state for offline reviews
- Sync when online
- Update store rating on backend via trigger/function

---

## Future Enhancements (Out of Scope for Phase 3)

- [ ] Edit review functionality
- [ ] Reply to reviews (store owner)
- [ ] Report inappropriate reviews
- [ ] Review images/photos
- [ ] Helpful/Not Helpful votes
- [ ] Verified purchase badge
- [ ] Review moderation dashboard

---

## Files Summary

### New Files (22 total):
**Domain (5):**
1. `domain/models/StoreReview.kt`
2. `domain/repositories/StoreReviewRepository.kt`
3. `domain/use_cases/review/AddStoreReviewUseCase.kt`
4. `domain/use_cases/review/GetStoreReviewsUseCase.kt`
5. `domain/use_cases/review/DeleteStoreReviewUseCase.kt`

**Data (4):**
6. `data/local/entities/StoreReviewEntity.kt`
7. `data/local/dao/StoreReviewDao.kt`
8. `data/remote/dto/StoreReviewDto.kt`
9. `data/repositories/StoreReviewRepositoryImpl.kt`

**Presentation (9):**
10. `presentation/viewmodels/ReviewsViewModel.kt`
11. `presentation/components/review/ReviewCard.kt`
12. `presentation/components/review/AddReviewDialog.kt`
13. `presentation/components/review/ReviewSortOptions.kt`
14. `presentation/screens/store/ReviewsListContent.kt`

**SQL (1):**
15. `SUPABASE_REVIEWS_SCHEMA.sql`

### Modified Files (5):
1. `data/local/db/AppDatabase.kt`
2. `di/DatabaseModule.kt`
3. `di/RepositoryModule.kt`
4. `data/repositories/StoreRepositoryImpl.kt`
5. `presentation/screens/store/StoreDetailScreen.kt`

---

## Success Criteria

✅ Users can add reviews with rating (1-5) and optional comment  
✅ Users can view all reviews for a store  
✅ Reviews can be sorted by Recent or Top Rated  
✅ Store rating automatically updates from review average  
✅ Users can delete their own reviews  
✅ Reviews are cached locally for offline viewing  
✅ UI follows Material Design 3 guidelines  
✅ No breaking changes to existing functionality

---

**Ready to Begin Implementation** 🚀  
**Follow steps 1-9 in order for systematic development**
