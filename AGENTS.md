# Consumo Electrico — AGENTS.md

## Build & run

```
gradlew assembleDebug                # debug APK
gradlew assembleRelease              # release APK (minifyEnabled=false, R8 strict mode off)
gradlew test                         # JUnit 4 unit tests
gradlew testDebugUnitTest            # debug unit tests only
gradlew connectedAndroidTest         # instrumented tests on device/emulator
```

Gradle 9.4.1 wrapper, AGP 9.2.1, Kotlin 2.4.0, JVM 17.

## Architecture

| Aspect | Detail |
|---|---|
| DI | **Kodein** (`org.kodein.di:kodein-di:7.32.0`) — not Hilt/Dagger. Defined in `BaseApp.kt`. |
| Database | **Room 2.8.4** (`fallbackToDestructiveMigration()`, file `app_ceh.db`) + **KSP** for codegen. |
| Legacy | Realm model classes in `realm/` package — migration from old version, not active. |
| UI | Hybrid: Jetpack **Compose** (`screens/` package) + **Fragments** with XML view binding (`ui2/` package). Navigation Component with SafeArgs. |
| State | `ElectricViewModel` (~600 lines, main business logic) uses `StateFlow` / `MutableLiveData`. Kodein provides `ViewModelFactory`. |
| Background | WorkManager `ReadReminderWorker` every 12h. |
| Billing | Google Play Billing `9.1.0` for ad removal. AdMob banners + app open ads. |
| Firebase | Analytics, Ads, Crashlytics, Remote Config. `google-services.json` committed. |

## Codegen & build steps

- **Room DAOs/entities** → KSP (`com.google.devtools.ksp`)
- **Moshi JSON adapters** → KSP (`moshi-kotlin-codegen`)
- **Navigation SafeArgs** → `androidx.navigation.safeargs.kotlin`
- **Firebase** → `google-services` plugin + `crashlytics` plugin
- BuildConfig fields `APP_BILLING_PUB_KEY` and `MERCHANT_ID` from `gradle.properties`
- Multi-dex enabled (`MultiDexApplication`)

## Style & lint

- **No formatter configured** — no ktlint, detekt, or `.editorconfig`. Match existing style manually.
- `lint.abortOnError = false`, `lint.checkReleaseBuilds = false`, release disables `MissingTranslation`
- Lint warnings never fail the build.

## Testing quirks

- Unit tests: `app/src/test/java/` (JUnit 4.13.2)
- Instrumented tests: `app/src/androidTest/java/` (AndroidX Test + JUnit 4)
- Compose `ui-test-junit4` available but **no Compose UI test files found**
- No CI workflows configured

## Security notes

- `app/fabric.properties` (Crashlytics API secret) and `app/google-services.json` are **committed** — avoid adding new secrets
- `app/consumoelectrico.jks` debug keystore is committed
- `.gitignore` excludes `/local.properties` and IDE files only

## Package layout

```
com.nicrosoft.consumoelectrico/
  BaseApp.kt               — Kodein DI module
  MainKt.kt                — Compose entry (?)
  data/                    — Room DB, DAOs, entities, data classes
  realm/                   — Legacy Realm models (read-only, migration only)
  screens/                 — Compose screens (main, periods, readings, states, theme)
  ui/                      — Old Fragment screens (settings, calculator, external destinations)
  ui2/                     — Newer Fragment screens (electric list, readings, periods, detail, stats)
  ui2/compose/             — Compose widgets embedded in Fragments
  ui2/adapters/            — RecyclerView adapters (Groupie)
  utils/                   — Extensions, colors, helpers, charts, backup/CSV/notification
  utils/workers/           — WorkManager workers
  viewmodels/              — ElectricViewModel
```

## Key libraries (non-default)

- `MPAndroidChart` v3.1.0 — charting
- `Lottie` 6.7.1 — animations
- `EasyPrefs` — SharedPreferences wrapper
- `Moshi` 1.15.2 — JSON (KSP codegen)
- `OpenCSV` 5.12.0 — CSV export/import
- `Groupie` 2.10.1 — RecyclerView adapter
- `Material Dialogs` (afollestad) 3.3.0
- `ThreeTenABP` 1.4.9 — java.time backport
- `Joda-Time` 2.14.2
- `CircularProgressIndicator` (antonKozyriatskyi) 1.3.0
- `Kodein DI` 7.32.0
- `EasyValidation` 1.0.4

## Design system

Unified M3 palette across XML and Compose in `screens/ui/theme/`:

| Role | Light | Dark |
|---|---|---|
| Primary (Blue) | `#005FB0` | `#AAC7FF` |
| Secondary (Teal) | `#006874` | `#4FDAE8` |
| Tertiary (Amber) | `#E1A000` | `#FFDEA1` |
| Surface/Bg | `#FBFDFF` | `#1A1C1E` |

- **Font**: Source Sans Pro (`res/font/source_sans_pro.xml`, Google Fonts download). Applied via `fontFamily` in `styles.xml` and `defaultFontFamily` in Compose `Type.kt`.
- **Compose screens** (`screens/` package) force `darkTheme = false, dynamicColor = false` in fragment callers. Dark palette exists but is not active.
- **M2/M3 coexistence**: `ui2/compose/main/` uses Material 2 (`androidx.compose.material`). Newer `screens/` uses Material 3 (`androidx.compose.material3`). They coexist — keep M3 for new Compose code.
- **Gradients** in `utils/colors.kt`: `indigoGradient` used for meter card headers (not the old `pinkGradient`).
- **Cards**: 16dp rounded corners, 2dp elevation in Compose (`CardDefaults.cardElevation`). Sidebar accent bar (70dp, primary/secondary color). Use `surface` for container color, not `surfaceVariant`.
- **Status bar**: Set to `colorScheme.primary` in Compose `Theme.kt`.
- **No `values-night/` directory** — XML views are light-mode only.

## External navigation

Custom `Navigator` subclasses for share, rate, Telegram, export, import — see `ui/` external destinations.

## i18n

Values in `values/` (default), `values-es/` (Spanish), `values-pt-rBR/` (Brazilian Portuguese). App name: "Consumo electrico".
