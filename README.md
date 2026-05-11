# Gestures

Gestures is a polished, multi-feature Android utility app that combines visual progress tracking with intelligent battery automation.

## Features

### 1. Visual Progress Tracker Widget
- **Dynamic Grid**: A pixel-perfect, responsive home screen widget built with Jetpack Glance.
- **Auto-scaling**: Efficiently renders 365+ days using a native Canvas bitmap approach to ensure zero lag.
- **Customizable**: Fully configurable start/end dates and color schemes.

### 2. Intelligent Battery Tracker
- **Automation Events**: Configure triggers for charger connection, disconnection, and battery level thresholds.
- **TTS Announcements**: Custom Text-To-Speech messages for every event.
- **Background Reliability**: Uses a foreground service to ensure real-time monitoring on Android 12+.
- **Minimal UI**: Modern, Material 3 card-based interface with expandable settings.

## Architectural Choices 

1. **Jetpack Compose & Material 3**: Entire UI is built with Compose for a reactive, modern experience.
2. **DataStore Persistence**: Lightweight and safe data management for all automation settings.
3. **Foreground Service**: Ensures foreground-safe handling of battery broadcasts for consistent performance.
4. **Glance Widget**: Leverages Jetpack Glance for high-performance home screen widgets.

## Development

- **Build System**: Gradle Kotlin DSL.
- **CI/CD**: GitHub Actions configured to build and export `Gestures.apk` and `Gestures.aab`.

To build: Open the project in Android Studio, sync Gradle, and run on your device!

