# Oasis Browser

Oasis Browser is a privacy-focused Android WebView browser built around a rail-first, one-handed interface. Navigation, tabs, search, QR scanning, refresh, bookmarks, and browser tools live in a compact rail on the left or right, leaving the webpage as the main surface.

**Developer:** Alzimer Ahmed  
**Contact:** alzimerahmed84@gmail.com  
**Application ID:** `com.alzimerahmed.oasisbrowser`

## Features

- WebView browsing with tabs and tab overview.
- Vertical URL/search rail with an expanded URL editor; left- or right-side rail positioning.
- Tampermonkey-compatible userscript manager (import from local `.js` files or HTTPS URLs).
- Adblocker with host-based blocking, uBlock-style cosmetic filtering, custom filter rules, and an element picker.
- Malware Scanner for downloads, with local definitions and optional VirusTotal scanning (your own API key).
- Per-site permission controls for location, camera, microphone, notifications, clipboard, and more.
- Cookie Manager with per-site viewing, editing, and deletion.
- QR code scanning, find in page, screenshots, downloads, history, and bookmarks.
- Homepage customization: wallpapers, bookmark shortcuts, custom motto, date/time display, and sanitized custom HTML/CSS.
- Light, dark, and true AMOLED-black themes with multiple accent palettes and Material Design 3 settings.
- Incognito browsing, Do Not Track, WebRTC configuration, and clearing of cache/history/cookies/storage.
- Text-to-speech accessibility support and custom local font import.
- Translations for 20+ languages.
- Pull-to-refresh, undo closed tab, and configurable close-tab focus behavior.
- Quick search engine switcher directly from the address bar.
- Reading list with offline reader mode and reading progress.
- Tab groups for organizing the tab drawer.
- Optional fingerprint randomization (canvas/WebGL noise) and HTTP-to-HTTPS upgrade.
- Clean-link copy that strips tracking parameters, and per-site zoom persistence.
- Collections for saving pages with notes.
- Optional video gesture controls (double-tap fullscreen) and a bundled variable font for web content.
- Predictive back gesture support and reduced-motion-aware animations.

## Permissions

Declared permissions are limited to the browser's core operation:

- `INTERNET` for web access.
- `ACCESS_NETWORK_STATE` for connectivity state.
- `POST_NOTIFICATIONS` for download and scan notifications.

Requested only when needed:

- `CAMERA` for QR scanning and optional WebRTC video capture.
- `RECORD_AUDIO` and `MODIFY_AUDIO_SETTINGS` for optional WebRTC audio capture.
- `ACCESS_FINE_LOCATION` and `ACCESS_COARSE_LOCATION` for website location requests when enabled.

## Build

Requirements:

- Android Studio or the Android SDK command-line tools.
- JDK 17 or newer.
- An Android SDK with compile SDK 36 installed.

Windows PowerShell:

```powershell
.\gradlew.bat :app:assembleOasisBrowserDebug
.\gradlew.bat :app:assembleOasisBrowserRelease
```

Linux/macOS:

```bash
./gradlew :app:assembleOasisBrowserDebug
./gradlew :app:assembleOasisBrowserRelease
```

Unit tests:

```bash
./gradlew :app:testOasisBrowserDebugUnitTest
```

Generated APKs are written under `app/build/outputs/apk/`.

## License

Oasis Browser is licensed under the Mozilla Public License 2.0. See [LICENSE](LICENSE).

