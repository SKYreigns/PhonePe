# PayDemo — Android demo payments app

PayDemo is an independent educational Android application inspired by common Indian UPI app interaction patterns. It is not PhonePe and is not affiliated with PhonePe. All payments, account data, balances and offers are fictional. No money is transferred and no bank/UPI network is contacted.

## Project

- Kotlin + Jetpack Compose + Material 3
- MVVM-style ViewModel and repository separation
- Navigation Compose
- Room persistence for transactions and notifications
- CameraX + Google ML Kit QR scanning
- Local-only simulated payment outcomes
- Min SDK 26 / target SDK 36

## Open and build

1. Install Android Studio with Android SDK Platform 36 and JDK 17.
2. Open the `PayDemo` folder in Android Studio.
3. Allow Gradle sync to resolve dependencies.
4. Connect an Android 8.0+ device or start an emulator.
5. Run the `app` configuration.
6. To create a debug APK, run `./gradlew assembleDebug` after generating the Gradle wrapper from Android Studio (File → Sync Project with Gradle Files / Gradle wrapper setup), or use **Build → Build Bundle(s) / APK(s) → Build APK(s)**.

The current delivery environment did not contain Android SDK Platform tools, an emulator, or a working Gradle installation, and outbound package downloads were unavailable. Therefore an APK could not be compiled or device-tested in this session. The project source is packaged for opening and building in Android Studio; do not treat the APK as verified until the build and device tests pass.

## Demo QR

The project includes `app/src/main/res/drawable-nodpi/demo_qr.png`. Scan it from another screen/device or print it. It encodes this documented test payload:

```text
paydemo://pay?name=Rahul%20Sharma&upi=rahul%40demobank&bank=Demo%20Bank
```

Only `paydemo://pay` QR payloads with an `@demobank` UPI ID are accepted. Arbitrary UPI QR codes are rejected. The scanner also supports selecting a QR image from the gallery.

## Demo payment flow

Home → Scan any QR → recipient → amount → confirmation → success/failure receipt. The confirmation screen has a normal simulated success action and a separate **Simulate failure** action. Both outcomes are written to Room and appear in History. No UPI PIN, OTP, password or bank credential is requested.

## Included screens

- Home with scan entry, transfer/recharge/service grids and demo offer carousel
- QR scanner with camera permission and gallery image selection
- Recipient, amount, confirmation and result screens
- Success and failure receipts with share action
- Persistent transaction history, filters and transaction detail
- Profile, demo balance, notifications, insurance and wealth placeholders

## Validation

Unit tests are included for demo QR validation and amount validation in `app/src/test`. They have not been executed in this environment because Gradle/Android SDK are unavailable. Camera scanning and persistence must be verified on a real device/emulator after building.

## Package layout

`data`, `database`, `model`, `repository`, `ui`, `navigation` (routes currently composed in `ui/PayDemoApp.kt`), `scanner`, `payments`, `history` (history UI currently composed in `ui/PayDemoApp.kt`), `profile`, `notifications`, `components`, `theme`, and `utils` are organized under `com.example.paymentdemo` as the project grows.
