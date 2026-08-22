# Unlist — Android Client

Native Android client for **Unlist**, a used-car inspection tool for the Indian market. Paste a Spinny or Cars24 listing URL and get back a normalised fault report, a red-flag severity score, and (for two to four listings) a side-by-side comparison with a recommended pick.

## Download

Grab the latest signed APK from the [Releases page](https://github.com/sharma-sudh/unlist-android/releases/latest), or the [v1.0.0 release directly](https://github.com/sharma-sudh/unlist-android/releases/tag/v1.0.0).

You'll need to allow "install from unknown sources" for your browser/file manager, since this isn't distributed through the Play Store.

## Features

- **Analyse** — submit a Spinny or Cars24 URL and get a structured fault report with a red-flag score
- **Compare** — line up 2–4 listings and see a recommended verdict
- **Share** — generate a permalink for any report (falls back to a web page if the recipient doesn't have the app installed, opens directly in-app if they do, via Android App Links)
- **Saved reports** — bookmark reports to revisit later
- **Google Sign-In** — auth via Credential Manager, backed by the FastAPI backend

## Tech stack

- Kotlin, Jetpack Compose, Material 3
- Navigation Compose
- Retrofit + OkHttp + Gson (snake_case ↔ camelCase mapping handled automatically)
- Credential Manager + Google Identity Services for sign-in
- AndroidX Security Crypto for secure token storage
- Coil for image loading
- Kotlin Coroutines

## Project structure

```
app/src/main/java/com/sudh/unlist/
├── auth/          # Google sign-in helper, token manager
├── components/    # Reusable Compose UI pieces
├── dto/           # Request/response models for the backend API
├── navigation/    # NavGraph and screen routes
├── network/       # Retrofit service, client, auth interceptor
├── repository/    # Auth, report, and saved-report repositories
├── screens/       # Home, Login, Report, Saved, Settings
├── ui/theme/      # Color, typography, theming
├── util/          # Constants, formatting, deep links, red-flag logic
└── viewmodel/     # One ViewModel per screen
```

## Setup

1. Clone the repo and open it in Android Studio (a recent Ladybug/Koala release or newer).
2. The backend base URL and Google **Web** OAuth client ID are wired in as `BuildConfig` fields in `app/build.gradle.kts`. Update them there if you're pointing at your own backend or OAuth client:
   ```kotlin
   buildConfigField("String", "BASE_URL", "\"https://your-backend-url/\"")
   buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"your-web-client-id.apps.googleusercontent.com\"")
   ```
3. Sign-in uses the **Web** client ID (not the Android client) — Credential Manager needs it to mint an ID token the backend can verify. The Android OAuth client in Google Cloud Console just needs your debug/release SHA-1 registered.
4. Build and run:
   ```bash
   ./gradlew assembleDebug
   ```

## Requirements

- minSdk 28, targetSdk / compileSdk 37
- JDK 17

## Backend

This is the client half of Unlist. The FastAPI backend (scraping, fault classification, report generation, sharing) lives in a [separate repository](https://github.com/sharma-sudh/unlist-backend).
