# Kiranawala App - Design Specifications & Visual Reference

## 🎨 Design Philosophy
Modern, clean grocery shopping interface inspired by Blinkit/Zepto with unique Kiranawala branding. Emphasis on:
- **Clarity**: Clear visual hierarchy and readable typography
- **Consistency**: Unified design language across all screens
- **Familiarity**: Grocery-centric patterns users recognize
- **Modernity**: 2025 design trends with rounded corners and soft shadows

---

## 📏 Layout Specifications

### Screen Structure
```
┌─────────────────────────────────┐
│  Top Bar (64dp)                 │ ← Surface color, 20sp bold title
├─────────────────────────────────┤
│                                 │
│  Content Area                   │ ← Background color
│  (LazyColumn with 16dp padding) │
│                                 │
│  ┌───────────────────────────┐ │
│  │  Card (16dp radius)       │ │ ← 2-4dp elevation
│  │  Content (16dp padding)   │ │
│  └───────────────────────────┘ │
│                                 │
│  [16dp vertical spacing]        │
│                                 │
│  ┌───────────────────────────┐ │
│  │  Card                     │ │
│  └───────────────────────────┘ │
│                                 │
└─────────────────────────────────┘
```

### Card Anatomy
```
┌────────────────────────────────────┐
│  [16dp]                      [16dp]│
│                                    │
│  Icon (48dp)   Title (Bold)        │ ← 16sp, SemiBold
│  [Background   Subtitle            │ ← 14sp, Regular
│   Primary      (Secondary color)   │
│   Container]                       │
│                                    │
│  [Divider if needed]               │
│                                    │
│  [16dp]                      [16dp]│
└────────────────────────────────────┘
```

---

## 🎨 Color System

### Light Mode Palette
```
Background:    #F8F9FA  ████████  Light gray background
Surface:       #FFFFFF  ████████  Pure white cards
Primary:       #2E7D32  ████████  Forest green
Secondary:     #FFC107  ████████  Amber gold
OnSurface:     #1C1C1C  ████████  Near black text
OnSurfaceVar:  #616161  ████████  Gray secondary text
Divider:       #E0E0E0  ████████  Light gray lines
```

### Dark Mode Palette
```
Background:    #0B0E0C  ████████  Dark green-black
Surface:       #1A1F1B  ████████  Dark olive surface
Primary:       #34C759  ████████  Bright green
Secondary:     #FFD54F  ████████  Light gold
OnSurface:     #FFFFFF  ████████  Pure white text
OnSurfaceVar:  #A0A0A0  ████████  Light gray secondary
Divider:       #2A2A2A  ████████  Dark gray lines
```

### Status Colors (Universal)
```
Success:       #34C759  ████████  Green (Delivered, Completed)
Warning:       #FFB300  ████████  Amber (Pending, Processing)
Error:         #D32F2F  ████████  Red (Cancelled, Error)
Info:          #2196F3  ████████  Blue (Information)
```

---

## 📝 Typography System

### Font Family
**Plus Jakarta Sans** - Modern, clean, highly readable

### Type Scale
```
Display Large     57sp  ExtraBold  Line 64sp   Hero sections
Display Medium    45sp  ExtraBold  Line 52sp   Large headings

Headline Large    32sp  ExtraBold  Line 40sp   Section headers
Headline Medium   28sp  ExtraBold  Line 36sp   Store names (cards)
Headline Small    24sp  Bold       Line 32sp   Page titles

Title Large       22sp  Bold       Line 28sp   Card titles
Title Medium      16sp  SemiBold   Line 24sp   Item titles
Title Small       14sp  SemiBold   Line 20sp   Labels

Body Large        16sp  Medium     Line 24sp   Primary content
Body Medium       14sp  Medium     Line 20sp   Secondary content
Body Small        12sp  Regular    Line 16sp   Captions

Label Large       14sp  Bold       Line 20sp   Button text
Label Medium      12sp  Bold       Line 16sp   Chip text
Label Small       11sp  Medium     Line 16sp   Micro text
```

---

## 🔘 Component Specifications

### Buttons
```
Primary Button (ModernActionButton)
┌───────────────────────────────────┐
│  [Icon 20dp]  Text (16sp Bold)    │  56dp height
└───────────────────────────────────┘  16dp corner radius
                                       Primary color fill
                                       2-6dp elevation

Outlined Button (ModernOutlinedButton)
┌───────────────────────────────────┐
│  [Icon 20dp]  Text (16sp Bold)    │  56dp height
└───────────────────────────────────┘  16dp corner radius
                                       2dp border
                                       Transparent fill

Payment Method Button (Special Case)
┌─────────────────────┬─────────────┐
│ [Icon]  COD        │ [Icon]  UPI  │  56dp height
└─────────────────────┴─────────────┘  16dp corner radius
    Selected: Primary fill              Equal width (weight 1f)
    Unselected: Border only             12dp gap between
```

### Cards
```
Modern Card
┌────────────────────────────────────┐
│  Content Area                      │  Full width
│  (16dp padding all sides)          │  16dp corner radius
│                                    │  2-4dp elevation
│  [Optional dividers]               │  Surface color
└────────────────────────────────────┘

Expandable Card (Special)
┌────────────────────────────────────┐
│  Header (always visible)           │  Tap to expand
│  ├─ Store Name (18sp Bold)         │  Spring animation
│  ├─ Order Info                     │  Smooth transition
│  └─ Total (20sp Bold Primary)      │
│  ▼ Expand indicator                │
├────────────────────────────────────┤ ← Divider when expanded
│  Expanded Content                  │
│  (Only when expanded)              │
│  └─ Action Button                  │
└────────────────────────────────────┘
```

### Status Chips
```
┌──────────────────┐
│ [Icon 16dp] Text │  6dp vertical padding
└──────────────────┘  12dp horizontal padding
                     12dp corner radius
                     Icon + text 6dp gap
                     Background: Status color 20% opacity
                     Text/Icon: Full status color
```

### Avatars
```
    ┌────────┐
    │   AB   │  80dp diameter
    │        │  Circle shape
    └────────┘  3dp white border
                Primary container background
                Large initials text (32sp)
```

---

## 📐 Spacing System

### Padding & Margins
```
Micro:     4dp   - Icon spacing, tight gaps
Small:     8dp   - Compact spacing
Medium:    12dp  - Standard item spacing
Default:   16dp  - Page padding, card padding
Large:     20dp  - Section spacing
XLarge:    24dp  - Major section breaks
XXLarge:   32dp  - Screen padding (empty states)
```

### Elevation (Shadows)
```
Level 0:   0dp   - Flat, no shadow
Level 1:   2dp   - Subtle lift (default cards)
Level 2:   4dp   - Medium lift (focused elements)
Level 3:   8dp   - High lift (sticky elements, dialogs)
```

---

## 🎭 Interaction States

### Button States
```
Normal:     Default colors, 2dp elevation
Pressed:    Darker tint, 6dp elevation
Disabled:   50% opacity, 0dp elevation
```

### Card States
```
Static:     2dp elevation
Hover:      3dp elevation (if applicable)
Pressed:    4dp elevation (if clickable)
```

### Text Fields (if used)
```
Normal:     Outlined, neutral border
Focused:    Primary color border, 2dp
Error:      Error color border
Success:    Success color border
```

---

## 🌈 Gradient Specifications

### Offer Cards
```
Gradient 1: Primary → Secondary
  #2E7D32 → #FFC107 (Light)
  #34C759 → #FFD54F (Dark)
  
Gradient 2: Cool Teal
  #00B2A9 → #0097A7
  
Gradient 3: Warm Pink
  #FF6B6B → #F06292

Direction: Horizontal (left to right)
Text Color: White (#FFFFFF)
CTA Button: White background, gradient text
```

---

## 🎬 Animation Specifications

### Transitions
```
Expandable Cards:
  - Spring animation
  - Damping ratio: Medium bouncy
  - Stiffness: Low
  - Duration: ~300ms

Fade In/Out:
  - Duration: 200ms
  - Easing: FastOutSlowIn

Scale:
  - From: 0.95
  - To: 1.0
  - Duration: 150ms
```

### Scrolling Behavior
```
- Smooth LazyColumn scrolling
- No fixed headers (unless cart summary)
- Natural scroll physics
- Overscroll bounce effect
```

---

## 📱 Screen-Specific Layouts

### Profile Screen
```
┌─────────────────────────────────────┐
│ ← Profile              [Edit]       │ Top Bar
├─────────────────────────────────────┤
│  ┌─────────────────────────────────┐│
│  │ [Avatar] Name              [✓]  ││ Profile Card
│  │         Phone                   ││
│  │         Email                   ││
│  └─────────────────────────────────┘│
│                                     │
│  Quick Access                       │ Section Header
│  ┌──────────────┬──────────────────┐│
│  │ [Icon]       │ [Icon]           ││ Grid Cards
│  │ Orders       │ Addresses        ││
│  └──────────────┴──────────────────┘│
│                                     │
│  Preferences                        │ Section Header
│  ┌─────────────────────────────────┐│
│  │ [Icon] Notifications       →    ││
│  ├─────────────────────────────────┤│ Settings Card
│  │ [Icon] App Settings        →    ││
│  ├─────────────────────────────────┤│
│  │ [Icon] Security            →    ││
│  └─────────────────────────────────┘│
│                                     │
│  [Logout Button]                    │
└─────────────────────────────────────┘
```

### Order History Screen
```
┌─────────────────────────────────────┐
│ ← Order History        [Refresh]    │ Top Bar
├─────────────────────────────────────┤
│  15 Orders                 [⟳]      │ Header
│                                     │
│  ┌─────────────────────────────────┐│
│  │ Store Name           [Completed]││ Collapsed Card
│  │ #ABC123                         ││
│  │ [Bag icon] 5 items  [Cal] Date  ││
│  │                         ₹500.00 ││
│  │             [▼ Tap to expand]   ││
│  └─────────────────────────────────┘│
│                                     │
│  ┌─────────────────────────────────┐│
│  │ Store Name           [Pending]  ││ Expanded Card
│  │ #DEF456                         ││
│  │ [Bag icon] 3 items  [Cal] Date  ││
│  │                         ₹300.00 ││
│  ├─────────────────────────────────┤│
│  │ Order Items                     ││
│  │ × 2  Product Name       ₹100.00 ││
│  │ × 1  Product Name       ₹200.00 ││
│  │                                 ││
│  │ [View Full Details]             ││
│  │             [▲ Collapse]        ││
│  └─────────────────────────────────┘│
└─────────────────────────────────────┘
```

### Cart Screen
```
┌─────────────────────────────────────┐
│ ← Shopping Cart           [Clear]   │ Top Bar
├─────────────────────────────────────┤
│  ┌─────────────────────────────────┐│
│  │ [Store Icon] Store Name         ││ Store Card
│  │             5 items in cart     ││
│  └─────────────────────────────────┘│
│                                     │
│  ┌─────────────────────────────────┐│
│  │ Product Name                    ││ Cart Item Card
│  │ ₹50.00                          ││
│  │                    [- 2 +]      ││ Quantity
│  │                         ₹100.00 ││ Subtotal
│  │                      [Remove]   ││
│  └─────────────────────────────────┘│
│                                     │
│  [More items...]                    │
│                                     │
├═════════════════════════════════════┤ Sticky Bottom
│  ┌─────────────────────────────────┐│
│  │ Bill Summary                    ││
│  │ Subtotal              ₹450.00   ││
│  │ Delivery Fee           ₹50.00   ││
│  │ ───────────────────────────     ││
│  │ Total                 ₹500.00   ││
│  └─────────────────────────────────┘│
│                                     │
│  Payment Method                     │
│  ┌─────────────┬───────────────────┐│
│  │ [💵] COD   │ [💳] UPI          ││ Dual Toggle
│  └─────────────┴───────────────────┘│
│                                     │
│  [Proceed to Checkout →]            │ Primary Button
└─────────────────────────────────────┘
```

---

## ✅ Quality Checklist

### Visual Consistency
- [ ] All cards use 16dp corner radius
- [ ] All buttons are 56dp height
- [ ] Page padding is 16dp consistently
- [ ] Shadows are 2-4dp elevation
- [ ] Icons are properly sized (16dp, 24dp, 40dp)

### Color Usage
- [ ] Primary color used for main actions
- [ ] Secondary color for highlights/accents
- [ ] OnSurface for primary text
- [ ] OnSurfaceVariant for secondary text
- [ ] Status colors only for status indicators

### Typography
- [ ] Headings use Bold/ExtraBold weights
- [ ] Body text uses Medium weight
- [ ] Labels use Bold weight
- [ ] Line heights maintain 1.4x ratio
- [ ] Font sizes from defined scale

### Accessibility
- [ ] Touch targets minimum 48dp
- [ ] Color contrast ratio > 4.5:1
- [ ] All icons have contentDescription
- [ ] Text readable without zoom
- [ ] Interactive states clearly visible

### Animations
- [ ] Smooth 60fps scrolling
- [ ] Spring animations for cards
- [ ] No janky transitions
- [ ] Loading states animate properly
- [ ] Fade transitions are subtle

---

## 📸 Screenshot Reference Points

For documentation/marketing, capture:
1. **Light Mode**: Profile, Order History, Cart
2. **Dark Mode**: Same screens
3. **Interactions**: Expanded order card, payment selection
4. **States**: Empty cart, loading, error states
5. **Details**: Status chips, buttons, typography samples

---

## 🛠️ Developer Guidelines

### DO:
✅ Use MaterialTheme.colorScheme for all colors
✅ Use MaterialTheme.typography for all text
✅ Apply consistent spacing (4dp, 8dp, 12dp, 16dp)
✅ Use modern components from ModernUIComponents.kt
✅ Test both light and dark modes
✅ Add contentDescription to all icons

### DON'T:
❌ Hardcode colors (use theme colors)
❌ Mix pixel sizes (use dp)
❌ Skip elevation on cards
❌ Forget to handle empty/loading/error states
❌ Use arbitrary font sizes (use typography scale)
❌ Create fixed headers (keep natural scroll)

---

**Version**: 1.0.0  
**Last Updated**: 2025-10-25  
**Designer**: Kiranawala Design Team
