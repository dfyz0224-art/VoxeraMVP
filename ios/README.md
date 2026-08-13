# Voxera iOS

SwiftUI-приложение, паритетно Android (`app/`). Минимум: **iOS 17**, **Xcode 16+** (пакет LiquidGlass собирается только на Swift 6).

## Сборка

Рекомендуется [XcodeGen](https://github.com/yonaskolb/XcodeGen) (macOS):

```bash
brew install xcodegen
cd ios
xcodegen generate
open Voxera.xcodeproj
```

После `git pull`, если в проект добавлялись или удалялись `.swift` под `Voxera/`, снова выполните `xcodegen generate` в каталоге `ios`, иначе Xcode может не увидеть новые файлы и ругаться (например, `Cannot find 'HistoryView' in scope`). Если `.xcodeproj` уже в репозитории — достаточно **File → Packages → Resolve Package Versions** и Clean Build.

Новые экраны (паритет Android): подписки в Settings, форма входа с подтверждением пароля / «Гостевой режим», кнопка «График состояний» на Result.

**API / TestFlight:** нужен файл **`ios/Voxera/Secrets.xcconfig`** (не Android `secrets.properties`!). Скопируйте `Secrets.xcconfig.example` → `Secrets.xcconfig`:

```text
VOXERA_API_TOKEN = ваш_токен_как_в_Android
```

Перед каждым **Archive → TestFlight** файл должен существовать на Mac — build phase `Generate API Token` вшивает токен в приложение. Без него Release-сборка упадёт с ошибкой или покажет `noToken` на телефоне.  
В **Debug** можно временно задать `VOXERA_API_TOKEN` в **Scheme → Run → Environment Variables** (на TestFlight это не действует).

**Debug:** кнопка **«Тест»** на экране записи копирует `Resources/audio_test.ogg` (как Android `assets/audio_test.ogg`) и сразу идёт в анализ.

Или создайте в Xcode новый проект **iOS App** и перетащите папку `Voxera/` с теми же таргет‑настройками, что в `project.yml`.

## Секреты

1. Скопируйте `Voxera/Secrets.xcconfig.example` → `Voxera/Secrets.xcconfig` и пропишите `VOXERA_API_TOKEN` (как `VOXERA_API_TOKEN` в Android `secrets.properties`).
2. Скопируйте из Firebase Console **`GoogleService-Info.plist`** в `Voxera/` (iOS-приложение в той же Firebase-проекте, что Android).
3. В Xcode: **Signing & Capabilities** — свой Team, **Automatically manage signing**.

## Зависимости

- **[LiquidGlass](https://github.com/BarredEwe/LiquidGlass)** (SPM) — кнопка записи; уже прописан в проекте. После `git pull`: **File → Packages → Resolve Package Versions**.

## Ассеты

В `Voxera/Assets.xcassets` уже лежат `bg_stars`, `bg_light`, карточки режимов (`parent_2`, `universal_2`, `deep_2`) — скопированы из Android `res/drawable`. При желании добавьте `ic_voxera_logo_text`, `ic_mic_2`; иначе показываются текст «VOXERA» и системные SF Symbols.

App Store / TestFlight: в `AppIcon.appiconset` лежит `AppIcon-1024.png` (как Play icon). После `git pull` в Xcode откройте **Assets → AppIcon** и убедитесь, что слот заполнен; затем Archive → Upload.

## Тексты «О приложении»

Краткие секции и 15 слайдов полного описания генерируются из `AboutPresentationData.kt` скриптом:

`python ios/scripts/emit_about_swift.py`
