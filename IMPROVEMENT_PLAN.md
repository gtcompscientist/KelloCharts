# KelloCharts Improvement Plan

**Version:** 3.0.0 → 3.1.0
**Goal:** Remove legacy code, optimize architecture, improve performance
**Created:** 2025-10-25

---

## 📋 Overview

This plan addresses technical debt, performance bottlenecks, and code quality issues identified after the Compose modernization. The improvements are organized by priority and impact.

### Summary of Improvements
- [ ] **Phase 1: Remove Legacy View-Based Code** (~4,872 lines, 31 files)
- [ ] **Phase 2: Fix Critical Bugs** (Touch selection, viewport issues)
- [ ] **Phase 3: Performance Optimization** (Large datasets, memory allocation)
- [ ] **Phase 4: Architecture Improvements** (Code organization, constants)
- [ ] **Phase 5: Code Quality** (Documentation, error handling, testing)

### Impact Metrics
- **Code Reduction:** ~4,872 lines (~30% of codebase)
- **Performance Gain:** 10-100x on large datasets (with culling)
- **Memory Reduction:** 50-90% (with object pooling/caching)
- **Maintenance:** Easier debugging, better developer experience

---

## Phase 1: Remove Legacy View-Based Code

**Goal:** Eliminate all outdated View-based chart code superseded by Compose implementation.
**Impact:** ~4,872 lines removed, 31 files deleted, cleaner codebase
**Priority:** HIGH (reduces maintenance burden, prevents confusion)

### 1.1 Remove View-Based Chart Classes (7 files, ~350 lines)

Located in: `kellocharts/src/main/java/co/csadev/kellocharts/view/`

- [ ] Delete `AbstractChartView.kt` (base class for View charts)
- [ ] Delete `LineChartView.kt` (superseded by `compose/LineChart.kt`)
- [ ] Delete `ColumnChartView.kt` (superseded by `compose/ColumnChart.kt`)
- [ ] Delete `PieChartView.kt` (superseded by `compose/PieChart.kt`)
- [ ] Delete `BubbleChartView.kt` (superseded by `compose/BubbleChart.kt`)
- [ ] Delete `PreviewLineChartView.kt` (advanced feature, deferred)
- [ ] Delete `PreviewColumnChartView.kt` (advanced feature, deferred)
- [ ] Delete `ComboLineColumnChartView.kt` (can compose separate charts)

**Verification:** Ensure no sample app or library code references these classes.

### 1.2 Remove Old Canvas-Based Renderers (10 files, ~3,500 lines)

Located in: `kellocharts/src/main/java/co/csadev/kellocharts/renderer/`

- [ ] Delete `AbstractChartRenderer.kt` (base for Canvas renderers)
- [ ] Delete `LineChartRenderer.kt` (superseded by `compose/renderer/ComposeLineChartRenderer.kt`)
- [ ] Delete `ColumnChartRenderer.kt` (superseded by `compose/renderer/ComposeColumnChartRenderer.kt`)
- [ ] Delete `PieChartRenderer.kt` (superseded by `compose/renderer/ComposePieChartRenderer.kt`)
- [ ] Delete `BubbleChartRenderer.kt` (superseded by `compose/renderer/ComposeBubbleChartRenderer.kt`)
- [ ] Delete `AxesRenderer.kt` (superseded by `compose/renderer/ComposeAxesRenderer.kt`)
- [ ] Delete `ComboLineColumnChartRenderer.kt`
- [ ] Delete `PreviewLineChartRenderer.kt`
- [ ] Delete `PreviewColumnChartRenderer.kt`
- [ ] Delete `ChartLabelRenderer.kt` (if exists)

**Note:** These use `android.graphics.Canvas`, `Paint`, `Path` APIs instead of Compose `DrawScope`.

### 1.3 Remove Old Animation System (5 files, ~300 lines)

Located in: `kellocharts/src/main/java/co/csadev/kellocharts/animation/`

- [ ] Delete `ChartDataAnimator.kt` (interface)
- [ ] Delete `ChartDataAnimatorV14.kt` (uses `ValueAnimator`, superseded by `compose/animation/`)
- [ ] Delete `ChartViewportAnimator.kt` (interface)
- [ ] Delete `ChartViewportAnimatorV14.kt` (uses `ValueAnimator`)
- [ ] Delete `PieChartRotationAnimatorV14.kt` (uses `ValueAnimator`)
- [ ] Delete `DummyChartAnimator.kt` (if exists - no-op implementation)

**Superseded by:** `compose/animation/ChartAnimations.kt` using Compose animation APIs

### 1.4 Remove View-Based Touch Handling (6 files, ~627 lines)

Located in: `kellocharts/src/main/java/co/csadev/kellocharts/touch/`

- [ ] Delete `ChartTouchHandler.kt` (282 lines - uses `GestureDetector`, `ScaleGestureDetector`)
- [ ] Delete `ChartScroller.kt` (124 lines - handles scrolling)
- [ ] Delete `ChartZoomer.kt` (81 lines - handles zoom)
- [ ] Delete `ZoomerCompat.kt` (140 lines - uses `DecelerateInterpolator`)
- [ ] Delete `PieChartTouchHandler.kt` (pie-specific touch)
- [ ] Delete `PreviewChartTouchHandler.kt` (preview-specific touch)

**Superseded by:** `compose/gesture/ChartGestures.kt` (330 lines, 48% code reduction)

### 1.5 Remove Hack/Workaround Classes (2 files, ~63 lines)

Located in: `kellocharts/src/main/java/co/csadev/kellocharts/util/`

- [ ] Delete `HackyViewPager.kt` (exception catching for ViewPager - only needed for View charts)
- [ ] Delete `HackyDrawerLayout.kt` (exception catching for DrawerLayout - only needed for View charts)

**Reason:** These were workarounds for View-based sample app, no longer needed with Compose Navigation.

### 1.6 Clean Up Resources

Located in: `kellocharts/src/main/res/`

- [ ] Remove legacy styles from `values/styles.xml`:
  - [ ] `<style name="AppTheme" parent="Theme.AppCompat.Light.DarkActionBar">`
  - [ ] Any other AppCompat-related styles
- [ ] Remove unused strings from `values/strings.xml`:
  - [ ] `title_activity_line_chart`
  - [ ] `title_activity_column_chart`
  - [ ] `title_activity_pie_chart`
  - [ ] `title_activity_bubble_chart`
  - [ ] `title_activity_preview_line_chart`
  - [ ] `title_activity_preview_column_chart`
  - [ ] `title_activity_combo_line_column_chart`
  - [ ] `title_activity_line_column_dependency`
  - [ ] `title_activity_good_bad`
  - [ ] `title_activity_tempo_chart`
  - [ ] `title_activity_speed_chart`
  - [ ] `title_activity_view_pager_charts`
  - [ ] `title_activity_about`
- [ ] Audit and remove unused drawable resources (if any remain)
- [ ] Remove any unused color resources specific to View system

### 1.7 Update Dependencies

Located in: `kellocharts/build.gradle`

- [ ] Remove AppCompat dependency (if present): `androidx.appcompat:appcompat`
- [ ] Remove RecyclerView dependency (if present): `androidx.recyclerview:recyclerview`
- [ ] Remove ViewPager dependency (if present): `androidx.viewpager:viewpager`
- [ ] Remove ConstraintLayout dependency (if present): `androidx.constraintlayout:constraintlayout`
- [ ] Remove Material Components dependency (if present): `com.google.android.material:material`
- [ ] Keep only Compose dependencies and core Android libraries

### 1.8 Final Cleanup

- [ ] Run `./gradlew clean` to remove build artifacts
- [ ] Search codebase for imports of deleted classes
- [ ] Fix any remaining references
- [ ] Update proguard rules if necessary
- [ ] Run `./gradlew build` to verify compilation
- [ ] Commit with message: "Remove legacy View-based code (~4,872 lines)"

---

## Phase 2: Fix Critical Bugs

**Goal:** Fix functionality-breaking bugs identified in Compose implementation.
**Impact:** Touch selection works correctly, proper coordinate handling
**Priority:** CRITICAL (broken features)

### 2.1 Fix Touch Selection with Viewport

**Issue:** Touch selection breaks when chart is zoomed or panned.
**Root Cause:** `getValueAtPosition()` uses empty Viewport instead of actual viewport.

- [ ] Fix `ComposeLineChartRenderer.kt` (Line 330):
  ```kotlin
  // BEFORE:
  override fun getValueAtPosition(position: Offset): SelectedValue? {
      // ...
      val pointOffset = pointToOffset(point, Viewport(), size) // BUG
  }

  // AFTER:
  override fun getValueAtPosition(position: Offset, viewport: Viewport): SelectedValue? {
      // ...
      val pointOffset = pointToOffset(point, viewport, size) // FIX
  }
  ```
- [ ] Fix `ComposeColumnChartRenderer.kt` (Line 262) - same issue
- [ ] Fix `ComposeBubbleChartRenderer.kt` (Line 113) - same issue
- [ ] Fix `ComposePieChartRenderer.kt` - verify selection logic
- [ ] Update `ComposeChartRenderer` interface to require viewport parameter:
  ```kotlin
  fun getValueAtPosition(position: Offset, viewport: Viewport): SelectedValue?
  ```
- [ ] Update all chart composables to pass viewport to getValueAtPosition:
  ```kotlin
  // In LineChart.kt, ColumnChart.kt, etc.
  getValueAtPosition = { offset ->
      renderer.getValueAtPosition(offset, viewportState.currentViewport)
  }
  ```
- [ ] Test touch selection:
  - [ ] With default viewport (no zoom/pan)
  - [ ] After zooming in
  - [ ] After panning left/right
  - [ ] After zooming and panning combined

### 2.2 Fix Animation State Creation

**Issue:** Creating new `mutableStateOf` on every animation frame.
**File:** `ChartAnimations.kt` (Line 130)

- [ ] Replace inefficient state creation:
  ```kotlin
  // BEFORE:
  return remember(animatedLeft.value, animatedTop.value, animatedRight.value, animatedBottom.value) {
      mutableStateOf(
          Viewport(...)
      )
  }

  // AFTER:
  return derivedStateOf {
      Viewport(
          animatedLeft.value,
          animatedTop.value,
          animatedRight.value,
          animatedBottom.value
      )
  }
  ```
- [ ] Test viewport animations still work smoothly
- [ ] Verify no regressions in zoom/pan animations

### 2.3 Fix Color Creation Bug in AxesRenderer

**Issue:** Using `color.hashCode()` instead of ARGB value for Paint.color.
**File:** `ComposeAxesRenderer.kt` (Line 259)

- [ ] Fix text rendering color:
  ```kotlin
  // BEFORE:
  val paint = android.graphics.Paint().apply {
      this.color = color.hashCode()  // WRONG!
      // ...
  }

  // AFTER:
  val paint = android.graphics.Paint().apply {
      this.color = color.toArgb()  // CORRECT
      // ...
  }
  ```
- [ ] Test axis labels render with correct colors in light/dark themes

---

## Phase 3: Performance Optimization

**Goal:** Optimize rendering for large datasets and reduce memory allocation.
**Impact:** 10-100x performance improvement on large datasets, 50-90% memory reduction
**Priority:** HIGH (scalability, user experience)

### 3.1 Implement Viewport-Based Culling

**Issue:** Rendering all data points even if off-screen (99% waste on zoomed charts).
**Impact:** 10-100x improvement on large datasets.

#### 3.1.1 LineChartRenderer Culling

File: `ComposeLineChartRenderer.kt`

- [ ] Add culling helper method:
  ```kotlin
  private fun isPointInViewport(point: PointValue, viewport: Viewport): Boolean {
      return point.x >= viewport.left && point.x <= viewport.right &&
             point.y >= viewport.bottom && point.y <= viewport.top
  }
  ```
- [ ] Apply culling in drawPoints:
  ```kotlin
  // BEFORE:
  line.values.forEach { point ->
      drawCircle(...)
  }

  // AFTER:
  line.values.filter { isPointInViewport(it, viewport) }.forEach { point ->
      drawCircle(...)
  }
  ```
- [ ] Apply culling in value selection (getValueAtPosition)
- [ ] Test with 10,000 data points, viewport showing 100

#### 3.1.2 ColumnChartRenderer Culling

File: `ComposeColumnChartRenderer.kt`

- [ ] Add column visibility check:
  ```kotlin
  private fun isColumnInViewport(columnIndex: Int, viewport: Viewport): Boolean {
      val columnX = columnIndex.toFloat()
      return columnX >= viewport.left && columnX <= viewport.right
  }
  ```
- [ ] Apply culling in draw method
- [ ] Test with 1,000+ columns

#### 3.1.3 BubbleChartRenderer Culling

File: `ComposeBubbleChartRenderer.kt`

- [ ] Add bubble visibility check (include radius in bounds)
- [ ] Apply culling in draw method
- [ ] Test with 1,000+ bubbles

#### 3.1.4 PieChartRenderer Optimization

File: `ComposePieChartRenderer.kt`

- [ ] Pie charts show all slices, but can optimize arc calculations
- [ ] Cache arc paths for unchanged data
- [ ] Test with 100+ slices

### 3.2 Implement Color Caching

**Issue:** Creating Color objects in tight loops (1000+ allocations per frame).
**Impact:** 50-70% reduction in GC pressure.

- [ ] Create color cache in base renderer or utility:
  ```kotlin
  // In a new file: ColorCache.kt
  object ColorCache {
      private val cache = mutableMapOf<Int, Color>()

      fun get(colorInt: Int): Color {
          return cache.getOrPut(colorInt) { Color(colorInt) }
      }

      fun clear() {
          cache.clear()
      }
  }
  ```
- [ ] Use in `ComposeLineChartRenderer.kt`:
  ```kotlin
  // BEFORE:
  val lineColor = Color(line.color)
  val pointColor = Color(line.pointColor)

  // AFTER:
  val lineColor = ColorCache.get(line.color)
  val pointColor = ColorCache.get(line.pointColor)
  ```
- [ ] Apply to all renderers (Column, Pie, Bubble, Axes)
- [ ] Clear cache when theme changes
- [ ] Measure memory allocation before/after

### 3.3 Implement Path Caching

**Issue:** Creating new Path objects for every point marker.
**Impact:** Reduce object allocations by 90% for point markers.

- [ ] Create reusable path factory:
  ```kotlin
  // In a new file: PathCache.kt
  object ShapePathCache {
      private val diamondPath = Path()
      private val squarePath = Path()

      fun getDiamondPath(radius: Float): Path {
          diamondPath.reset()
          diamondPath.moveTo(0f, -radius)
          diamondPath.lineTo(radius, 0f)
          diamondPath.lineTo(0f, radius)
          diamondPath.lineTo(-radius, 0f)
          diamondPath.close()
          return diamondPath
      }

      fun getSquarePath(radius: Float): Path {
          squarePath.reset()
          val side = radius * 1.414f
          squarePath.addRect(
              Rect(-side/2, -side/2, side/2, side/2)
          )
          return squarePath
      }
  }
  ```
- [ ] Use in LineChartRenderer with `withTransform`:
  ```kotlin
  line.values.forEach { point ->
      val offset = pointToOffset(point, viewport, size)
      withTransform({
          translate(offset.x, offset.y)
      }) {
          when (line.shape) {
              ValueShape.DIAMOND -> {
                  val path = ShapePathCache.getDiamondPath(pointRadius)
                  drawPath(path, color = pointColor)
              }
          }
      }
  }
  ```
- [ ] Apply to all shape types
- [ ] Test rendering correctness

### 3.4 Optimize Data Changed Callbacks

**Issue:** Empty `onDataChanged()` methods - no caching implemented.
**Impact:** Reduce redundant calculations on every draw.

#### 3.4.1 LineChartRenderer Caching

- [ ] Add caching fields:
  ```kotlin
  private var cachedLinePaths: MutableMap<Int, Path>? = null
  private var cachedAreaPaths: MutableMap<Int, Path>? = null
  ```
- [ ] Implement onDataChanged:
  ```kotlin
  override fun onDataChanged() {
      cachedLinePaths = null
      cachedAreaPaths = null
  }
  ```
- [ ] Build paths lazily:
  ```kotlin
  private fun getLinePath(lineIndex: Int, line: Line): Path {
      if (cachedLinePaths == null) {
          cachedLinePaths = mutableMapOf()
      }
      return cachedLinePaths!![lineIndex] ?: buildLinePath(line).also {
          cachedLinePaths!![lineIndex] = it
      }
  }
  ```
- [ ] Test cache invalidation on data change

#### 3.4.2 Apply to Other Renderers

- [ ] Implement caching in ColumnChartRenderer
- [ ] Implement caching in PieChartRenderer
- [ ] Implement caching in BubbleChartRenderer

### 3.5 Optimize Dimension Conversions

**Issue:** Repeated dp.toPx() conversions in draw loop.
**Impact:** Reduce conversion overhead by 100%.

- [ ] Cache converted dimensions:
  ```kotlin
  private var cachedStrokeWidthPx: Float = 0f
  private var cachedPointRadiusPx: Float = 0f

  override fun onSizeChanged(size: Size, contentRect: Rect) {
      super.onSizeChanged(size, contentRect)
      // Cache conversions once
      cachedStrokeWidthPx = data.lines.firstOrNull()?.strokeWidth?.dp?.toPx() ?: 2.dp.toPx()
      cachedPointRadiusPx = 4.dp.toPx()
  }
  ```
- [ ] Use cached values in draw method
- [ ] Apply to all renderers

### 3.6 Add Performance Benchmarks

- [ ] Create benchmark module (if not exists)
- [ ] Add benchmark for rendering 1,000 points
- [ ] Add benchmark for rendering 10,000 points
- [ ] Add benchmark for rendering 100,000 points
- [ ] Measure before optimization (baseline)
- [ ] Measure after each optimization
- [ ] Document performance improvements

---

## Phase 4: Architecture Improvements

**Goal:** Improve code organization, reduce duplication, extract constants.
**Impact:** Better maintainability, easier onboarding, consistent behavior
**Priority:** MEDIUM (code quality, maintainability)

### 4.1 Extract Chart Layout Constants

**Issue:** Magic numbers duplicated across multiple files.
**Impact:** Single source of truth, easier to adjust layout.

- [ ] Create `ChartLayoutConstants.kt`:
  ```kotlin
  package co.csadev.kellocharts.compose.common

  /**
   * Layout constants used across chart components.
   */
  object ChartLayoutConstants {
      /** Margin when axis is present (dp) */
      const val MARGIN_WITH_AXIS = 60f

      /** Margin when axis is absent (dp) */
      const val MARGIN_WITHOUT_AXIS = 10f

      /** Top margin with axis (dp) */
      const val MARGIN_TOP_WITH_AXIS = 40f

      /** Bottom margin with axis (dp) */
      const val MARGIN_BOTTOM_WITH_AXIS = 40f

      /** Viewport padding as percentage of data range */
      const val VIEWPORT_PADDING_RATIO = 0.1f
  }
  ```
- [ ] Replace magic numbers in `LineChart.kt` (lines 210-213)
- [ ] Replace magic numbers in `ColumnChart.kt` (lines 218-221)
- [ ] Replace magic numbers in `PieChart.kt` (lines 165-168)
- [ ] Replace magic numbers in `BubbleChart.kt` (lines 197-200)
- [ ] Search for "60f" and "10f" to find remaining usages

### 4.2 Extract Chart Rendering Constants

- [ ] Create `ChartRenderingConstants.kt`:
  ```kotlin
  package co.csadev.kellocharts.compose.renderer

  /**
   * Rendering constants for chart drawing.
   */
  object ChartRenderingConstants {
      /** Touch tolerance radius (dp) */
      const val TOUCH_TOLERANCE_DP = 24

      /** Default column corner radius (dp) */
      const val COLUMN_CORNER_RADIUS_DP = 2

      /** Grid line opacity */
      const val GRID_LINE_ALPHA = 0.2f

      /** Default bubble base size (dp) */
      const val BUBBLE_BASE_SIZE_DP = 50

      /** Default line stroke width (dp) */
      const val DEFAULT_STROKE_WIDTH_DP = 2

      /** Default point radius (dp) */
      const val DEFAULT_POINT_RADIUS_DP = 4
  }
  ```
- [ ] Replace magic numbers in `ComposeLineChartRenderer.kt`
- [ ] Replace magic numbers in `ComposeColumnChartRenderer.kt`
- [ ] Replace magic numbers in `ComposeBubbleChartRenderer.kt`
- [ ] Replace magic numbers in `ComposeAxesRenderer.kt`

### 4.3 Extract Animation Constants

- [ ] Update `ChartAnimationDefaults.kt`:
  ```kotlin
  object ChartAnimationDefaults {
      // Duration constants
      const val DEFAULT_DURATION_MS = 500
      const val FAST_DURATION_MS = 200
      const val SLOW_DURATION_MS = 800

      // Spring constants
      const val DEFAULT_DAMPING_RATIO = 0.8f
      const val DEFAULT_STIFFNESS = 300f

      // Animation specs (use constants above)
      val spring: SpringSpec<Float> = spring(
          dampingRatio = DEFAULT_DAMPING_RATIO,
          stiffness = DEFAULT_STIFFNESS
      )
      val tween: TweenSpec<Float> = tween(durationMillis = DEFAULT_DURATION_MS)
      val fast: TweenSpec<Float> = tween(durationMillis = FAST_DURATION_MS)
      val slow: TweenSpec<Float> = tween(durationMillis = SLOW_DURATION_MS)
  }
  ```

### 4.4 Remove Duplicate Code

**Issue:** Duplicate methods in renderers.

- [ ] Fix `ComposeBubbleChartRenderer.kt` (Lines 97 and 142):
  - [ ] Remove duplicate `calculateBubbleRadius()` method
  - [ ] Keep only one implementation
  - [ ] Verify tests pass

### 4.5 Add Missing Stability Annotations

**Issue:** Missing @Immutable and @Stable annotations cause unnecessary recomposition.

- [ ] Add @Immutable to `ChartCommonComponents.kt`:
  ```kotlin
  @Immutable
  data class LegendItemData(
      val label: String,
      val color: Color
  )
  ```
- [ ] Add @Stable to `GestureConfig`:
  ```kotlin
  @Stable
  data class GestureConfig(
      val zoomEnabled: Boolean = true,
      val scrollEnabled: Boolean = true,
      val selectionEnabled: Boolean = true
  )
  ```
- [ ] Audit all data classes used in Composables
- [ ] Add appropriate annotations

### 4.6 Improve Text Rendering Architecture

**Issue:** Using nativeCanvas instead of Compose TextMeasurer.
**Impact:** Better Compose integration, testability.

- [ ] Add TextMeasurer parameter to axes renderer constructor:
  ```kotlin
  class ComposeAxesRenderer(
      private var axisXBottom: Axis? = null,
      private var axisYLeft: Axis? = null,
      private var axisXTop: Axis? = null,
      private var axisYRight: Axis? = null,
      private val textMeasurer: TextMeasurer
  )
  ```
- [ ] Replace nativeCanvas.drawText with Compose drawText:
  ```kotlin
  private fun DrawScope.drawAxisLabel(
      text: String,
      x: Float,
      y: Float,
      color: Color,
      alignRight: Boolean = false
  ) {
      val textLayoutResult = textMeasurer.measure(
          text = AnnotatedString(text),
          style = TextStyle(
              fontSize = labelTextSize,
              color = color
          )
      )

      drawText(
          textLayoutResult,
          topLeft = Offset(
              if (alignRight) x - textLayoutResult.size.width else x,
              y - textLayoutResult.size.height / 2
          )
      )
  }
  ```
- [ ] Update all chart composables to create and pass TextMeasurer
- [ ] Remove nativeCanvas usage
- [ ] Test text rendering in light/dark themes

### 4.7 Implement PathEffect Conversion

**Issue:** PathEffect conversion returns null (dashed lines don't work).
**File:** `ComposeLineChartRenderer.kt` (Lines 383-388)

- [ ] Implement proper PathEffect conversion:
  ```kotlin
  private fun convertPathEffect(effect: android.graphics.PathEffect): PathEffect? {
      return when (effect) {
          is android.graphics.DashPathEffect -> {
              // Extract intervals and phase from DashPathEffect
              // Compose uses PathEffect.dashPathEffect(intervals, phase)
              PathEffect.dashPathEffect(
                  intervals = floatArrayOf(10f, 5f), // TODO: Extract from effect
                  phase = 0f
              )
          }
          is android.graphics.CornerPathEffect -> {
              PathEffect.cornerPathEffect(radius = 10f) // TODO: Extract radius
          }
          else -> null
      }
  }
  ```
- [ ] Or remove PathEffect support entirely if not used
- [ ] Document limitations
- [ ] Test dashed lines if implemented

---

## Phase 5: Code Quality & Testing

**Goal:** Improve documentation, error handling, and test coverage.
**Impact:** Easier debugging, better developer experience, reliability
**Priority:** MEDIUM (long-term quality)

### 5.1 Add Documentation

#### 5.1.1 Complex Algorithm Documentation

- [ ] Document offset calculation in `ComposeColumnChartRenderer.kt` (Lines 222-230):
  ```kotlin
  /**
   * Calculate the horizontal offset for a subcolumn in grouped mode.
   *
   * Centers all subcolumns around the column center point.
   * For 3 subcolumns with width W:
   *   - Total width: 3W
   *   - Start offset: -1.5W (left edge)
   *   - Subcolumn 0: -1.5W + 0.5W = -W (centered at -W)
   *   - Subcolumn 1: -1.5W + 1.5W = 0 (centered at origin)
   *   - Subcolumn 2: -1.5W + 2.5W = +W (centered at +W)
   *
   * @param subcolumnIndex Index of the subcolumn (0-based)
   * @param totalSubcolumns Total number of subcolumns in this column
   * @param subcolumnWidth Width of each subcolumn
   * @return Horizontal offset from column center
   */
  private fun calculateSubcolumnOffset(...)
  ```
- [ ] Document viewport calculations in renderers
- [ ] Document coordinate transformation math

#### 5.1.2 Public API Documentation

- [ ] Audit all public composables for KDoc
- [ ] Add @param and @return tags
- [ ] Add usage examples
- [ ] Document edge cases and limitations

#### 5.1.3 Architecture Documentation

- [ ] Create `ARCHITECTURE.md` documenting:
  - [ ] Package structure
  - [ ] Renderer architecture
  - [ ] State management patterns
  - [ ] Gesture handling flow
  - [ ] Animation system
  - [ ] Theme system

### 5.2 Add Error Handling

#### 5.2.1 Input Validation

- [ ] Add validation in renderers:
  ```kotlin
  private fun DrawScope.drawStraightLine(line: Line, viewport: Viewport, size: Size) {
      if (line.values.isEmpty()) {
          Log.w(TAG, "Attempted to draw line with no values")
          return
      }
      if (line.values.size < 2) {
          Log.d(TAG, "Line has only one point, skipping line drawing")
          return
      }
      // ... rest of method
  }
  ```
- [ ] Add companion object with TAG in each renderer
- [ ] Log warnings for edge cases
- [ ] Validate viewport bounds

#### 5.2.2 Graceful Degradation

- [ ] Handle null/empty data gracefully
- [ ] Handle extreme zoom levels (very large/small viewports)
- [ ] Handle negative dimensions
- [ ] Add fallback rendering for unsupported features

### 5.3 Add Unit Tests

#### 5.3.1 Model Tests

- [ ] Test chart data immutability
- [ ] Test data class equality
- [ ] Test data class copy behavior

#### 5.3.2 Calculation Tests

- [ ] Test viewport calculations with various data ranges
- [ ] Test coordinate transformations (pointToOffset, offsetToPoint)
- [ ] Test culling logic (isPointInViewport)
- [ ] Test subcolumn offset calculations
- [ ] Test bubble radius calculations

#### 5.3.3 State Tests

- [ ] Test ViewportState zoom behavior
- [ ] Test ViewportState pan behavior
- [ ] Test ViewportState reset behavior
- [ ] Test ViewportState bounds clamping

#### 5.3.4 Formatter Tests

- [ ] Test value formatters with various inputs
- [ ] Test edge cases (zero, negative, very large numbers)
- [ ] Test null handling

### 5.4 Add Compose UI Tests

- [ ] Set up Compose testing infrastructure
- [ ] Add test utilities and helpers
- [ ] Test LineChart rendering:
  - [ ] Renders with valid data
  - [ ] Renders empty state
  - [ ] Handles selection
- [ ] Test ColumnChart rendering
- [ ] Test PieChart rendering
- [ ] Test BubbleChart rendering
- [ ] Test gesture handling (zoom, pan, select)
- [ ] Test animation completion

### 5.5 Add Performance Tests

- [ ] Create benchmark for 100 points
- [ ] Create benchmark for 1,000 points
- [ ] Create benchmark for 10,000 points
- [ ] Create benchmark for 100,000 points
- [ ] Measure frame time
- [ ] Measure memory allocation
- [ ] Measure GC frequency
- [ ] Compare before/after optimization

### 5.6 Implement TODOs

- [ ] Review all TODO comments in codebase
- [ ] Implement center text drawing in PieChart:
  ```kotlin
  // In ComposePieChartRenderer.kt
  private fun DrawScope.drawCenterText(
      centerText1: CharArray?,
      centerText2: CharArray?,
      center: Offset,
      textMeasurer: TextMeasurer
  ) {
      if (centerText1 == null) return

      val text1 = String(centerText1)
      val layout1 = textMeasurer.measure(
          text = AnnotatedString(text1),
          style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
      )

      drawText(
          layout1,
          topLeft = Offset(
              center.x - layout1.size.width / 2,
              center.y - layout1.size.height - 4.dp.toPx()
          )
      )

      if (centerText2 != null) {
          val text2 = String(centerText2)
          val layout2 = textMeasurer.measure(
              text = AnnotatedString(text2),
              style = TextStyle(fontSize = 12.sp)
          )

          drawText(
              layout2,
              topLeft = Offset(
                  center.x - layout2.size.width / 2,
                  center.y + 4.dp.toPx()
              )
          )
      }
  }
  ```
- [ ] Track unimplemented TODOs in backlog
- [ ] Prioritize for future releases

---

## Phase 6: Additional Improvements (Optional)

**Goal:** Nice-to-have improvements for future releases.
**Priority:** LOW (can be deferred)

### 6.1 Advanced Features

- [ ] Implement PreviewLineChart composable
- [ ] Implement PreviewColumnChart composable
- [ ] Implement ComboLineColumnChart composable
- [ ] Add crosshair on value selection
- [ ] Add value labels on hover/selection
- [ ] Add zooming to selection feature
- [ ] Add data point annotations

### 6.2 Accessibility

- [ ] Add content descriptions to charts
- [ ] Support TalkBack navigation
- [ ] Add semantic properties for screen readers
- [ ] Support high contrast mode
- [ ] Add keyboard navigation
- [ ] Test with accessibility scanner

### 6.3 Developer Experience

- [ ] Add Compose preview providers for charts
- [ ] Create sample data generators
- [ ] Add debug overlay showing viewport bounds
- [ ] Add performance metrics overlay
- [ ] Create interactive playground app

### 6.4 Advanced Animations

- [ ] Add spring-based data value changes
- [ ] Add enter/exit animations for data points
- [ ] Add path morphing animations
- [ ] Add coordinated multi-chart animations

---

## Success Metrics

### Code Quality Metrics
- [ ] Code reduction: Remove ~4,872 lines (30% reduction)
- [ ] Test coverage: Achieve 70%+ code coverage
- [ ] Documentation: 100% public API documented
- [ ] Zero warnings in build output
- [ ] Zero TODOs in main code paths

### Performance Metrics
- [ ] 10,000 points render in <16ms (60 FPS)
- [ ] 100,000 points render with culling in <16ms
- [ ] Memory allocation reduced by 50%+
- [ ] GC frequency reduced by 70%+
- [ ] Smooth 60 FPS animations

### Bug Metrics
- [ ] Zero critical bugs (touch selection fixed)
- [ ] Zero high-priority bugs
- [ ] All edge cases handled gracefully

---

## Testing Checklist

After completing improvements, verify:

### Functional Testing
- [ ] All chart types render correctly
- [ ] Touch selection works in all zoom/pan states
- [ ] Zoom gesture works smoothly
- [ ] Pan gesture works smoothly
- [ ] Rotation gesture works (pie chart)
- [ ] Animations complete without jank
- [ ] Theme switching works
- [ ] Light/dark mode renders correctly

### Performance Testing
- [ ] Test with 100 data points
- [ ] Test with 1,000 data points
- [ ] Test with 10,000 data points
- [ ] Test with 100,000 data points
- [ ] Profile with Android Studio Profiler
- [ ] Verify no memory leaks
- [ ] Verify smooth scrolling

### Edge Case Testing
- [ ] Empty data (0 points)
- [ ] Single data point
- [ ] All positive values
- [ ] All negative values
- [ ] Mixed positive/negative
- [ ] Zero values
- [ ] Very large values (millions)
- [ ] Very small values (decimals)
- [ ] Extreme zoom in
- [ ] Extreme zoom out

### Regression Testing
- [ ] Sample app runs without crashes
- [ ] All demo screens work
- [ ] Navigation works
- [ ] Theme persistence works
- [ ] Build succeeds
- [ ] All existing tests pass

---

## Commit Strategy

Organize commits by phase for easy review and potential revert:

### Phase 1: Legacy Code Removal
- Commit 1: "Remove View-based chart classes (7 files)"
- Commit 2: "Remove Canvas-based renderers (10 files)"
- Commit 3: "Remove old animation system (5 files)"
- Commit 4: "Remove View-based touch handling (6 files)"
- Commit 5: "Remove hack/workaround classes (2 files)"
- Commit 6: "Clean up legacy resources"
- Commit 7: "Update dependencies, remove View libraries"

### Phase 2: Critical Bug Fixes
- Commit 8: "Fix touch selection with viewport bug"
- Commit 9: "Fix animation state creation bug"
- Commit 10: "Fix color creation bug in axes renderer"

### Phase 3: Performance Optimizations
- Commit 11: "Implement viewport-based culling for all renderers"
- Commit 12: "Implement color caching"
- Commit 13: "Implement path caching"
- Commit 14: "Implement onDataChanged caching"
- Commit 15: "Optimize dimension conversions"

### Phase 4: Architecture Improvements
- Commit 16: "Extract chart layout constants"
- Commit 17: "Extract chart rendering constants"
- Commit 18: "Remove duplicate code, add stability annotations"
- Commit 19: "Improve text rendering with TextMeasurer"
- Commit 20: "Implement PathEffect conversion"

### Phase 5: Code Quality
- Commit 21: "Add documentation for complex algorithms"
- Commit 22: "Add error handling and validation"
- Commit 23: "Add unit tests for calculations"
- Commit 24: "Add Compose UI tests"
- Commit 25: "Add performance benchmarks"

---

## Timeline Estimate

**Total Effort:** ~3-4 weeks (1 developer)

- **Phase 1:** 2-3 days (mostly deletion, verification)
- **Phase 2:** 2-3 days (critical bug fixes, testing)
- **Phase 3:** 5-7 days (performance optimization, profiling)
- **Phase 4:** 3-4 days (refactoring, constants extraction)
- **Phase 5:** 5-7 days (documentation, testing, quality)

**Parallel Work:** Phases 4 and 5 can overlap with Phase 3.

---

## Risk Mitigation

### Risks
1. **Breaking Changes:** Removing code might break undiscovered dependencies
2. **Performance Regressions:** Optimizations might introduce bugs
3. **Testing Overhead:** Comprehensive testing takes time

### Mitigation Strategies
- [ ] Create feature branch for improvements
- [ ] Commit frequently with clear messages
- [ ] Run full test suite after each phase
- [ ] Keep legacy code in separate branch temporarily
- [ ] Use feature flags for risky changes
- [ ] Profile before and after each optimization
- [ ] Get code review for critical changes

---

## Maintenance Plan

After completing improvements:

- [ ] Update README.md with new architecture
- [ ] Update CHANGELOG.md with improvements
- [ ] Create GitHub release for v3.1.0
- [ ] Update documentation site
- [ ] Publish to Maven Central
- [ ] Write blog post about improvements
- [ ] Monitor crash reports for regressions

---

**Last Updated:** 2025-10-25
**Status:** Planning Phase
**Owner:** Development Team
