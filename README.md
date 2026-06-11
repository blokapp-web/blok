# BLOK

**Your phone. Your rules.**

BLOK is a physical app blocker for Android. Tap an NFC tag to lock distracting apps — tap again to unlock. The physical action is the point: friction you can't swipe away.

🌐 **Website:** https://blok-app.com
📦 **Download:** grab the latest APK from [Releases](../../releases)

## Why BLOK

- **Physical toggle** — any standard NFC tag (NTAG213/215/216 stickers, cards, keychains) becomes your commitment device. No proprietary hardware.
- **100% offline** — no accounts, no cloud, no analytics, no trackers. Everything stays on your device.
- **Focus spaces** — separate blocking profiles for study, work, sleep.
- **Progress tracking** — time saved and blocked attempts, computed on-device.
- **Emergency unlocks** — limited escapes for genuine urgency.
- **Notification blocking** — optional, hides notifications from blocked apps while a block is active.
- **Free forever** — no ads, no subscriptions, no in-app purchases.

## Why open source

An app that can lock your phone should never be a black box. Every line of BLOK is public so you can verify exactly what it does — and what it doesn't: nothing leaves your device. See the [privacy policy](https://blok-app.com/privacy.html).

## How it works

1. **Choose your apps** — select what steals your focus.
2. **Record any NFC tag** — done from inside the app in seconds.
3. **Tap to lock, tap to unlock** — BLOK intercepts blocked apps via Android's Accessibility Service (used solely to detect the foreground app; it never reads screen content).

## Building from source

Requirements: JDK 17, Android SDK 35.

```bash
# debug build (no signing needed)
./gradlew assembleDebug

# release build — create keystore.properties in the project root first:
#   storeFile=your-key.jks
#   storePassword=...
#   keyAlias=...
#   keyPassword=...
./gradlew assembleRelease
```

## Tech

Kotlin · Jetpack Compose · Room · Accessibility Service · NFC (NDEF)

Min SDK 26 (Android 8.0) · Target SDK 35

## License

[GPL-3.0](LICENSE) — free to use, study and modify; derivatives must stay open.

## Contact

blokapp.cont@hotmail.com
