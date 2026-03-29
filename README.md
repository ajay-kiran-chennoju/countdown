# ProgressWidget

This is a clean, minimal, responsive Android home screen widget built with Jetpack Glance that visually represents progress over a dynamically configurable number of days.

## Architectural Choices 

1. **Pixel-Perfect Auto-scaling Grid via Canvas Bitmap Generation**:
   The prompt specifically requested that the Android widget be responsive, maintaining consistent scaling for 365+ items without lag or scrolling. Since `RemoteViews` layouts (which Jetpack Glance wraps) have a strict structural depth and node limit mapping 365 distinct layouts on-the-fly would fail and lag intensely on standard devices.
   Instead, this widget hooks into Glance's `SizeMode.Exact` to determine the perfect layout size, statically calculates the grid dimensions natively so there are absolutely no edge-clipping issues, and draws an optimized uncompressed Bitmap. This passes down an absolute minimal layout tree (just an ImageView), ensuring silky smooth Home Screen resizing performance while meeting all pixel-spacing symmetry constraints.

2. **Ultra-Minimal Adaptive Colors**:
   Follows light/dark system settings as fallbacks when custom HEX codes are not supplied, defaulting to distinct visually complementary shades. Background is totally 100% transparent.

3. **Fully Interactive**:
   Clicking the grid triggers the underlying App container to modify HEX values, current progression, and totals to instantly see updates on the home screen.

To build it:
Open the `Widget` folder in Android Studio. Wait for it to sync Gradle, then press 'Run' to install it on your device/emulator!
