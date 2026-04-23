# Voxera iOS

SwiftUI-приложение, паритетно Android (`app/`). Минимум: **iOS 17**, **Xcode 15+**.

## Сборка

Рекомендуется [XcodeGen](https://github.com/yonaskolb/XcodeGen) (macOS):

```bash
brew install xcodegen
cd ios
xcodegen generate
open Voxera.xcodeproj
```

После `git pull`, если в проект добавлялись или удалялись `.swift` под `Voxera/`, снова выполните `xcodegen generate` в каталоге `ios`, иначе Xcode может не увидеть новые файлы и ругаться (например, `Cannot find 'StatisticsView' in scope`).

Или создайте в Xcode новый проект **iOS App** и перетащите папку `Voxera/` с теми же таргет‑настройками, что в `project.yml`.

## Секреты

1. Скопируйте `Voxera/Secrets.xcconfig.example` → `Voxera/Secrets.xcconfig` и пропишите `VOXERA_API_TOKEN` (как `VOXERA_API_TOKEN` в Android `secrets.properties`).
2. Скопируйте из Firebase Console **`GoogleService-Info.plist`** в `Voxera/` (iOS-приложение в той же Firebase-проекте, что Android).
3. В Xcode: **Signing & Capabilities** — свой Team, **Automatically manage signing**.

## Зависимости (опционально)

Firebase Auth и Google Sign-In можно подключить через SPM, когда понадобятся; в текущем `project.yml` внешних пакетов нет.

## Ассеты

В `Voxera/Assets.xcassets` уже лежат `bg_stars`, `bg_light`, карточки режимов (`parent_2`, `universal_2`, `deep_2`) — скопированы из Android `res/drawable`. При желании добавьте `ic_voxera_logo_text`, `ic_mic_2`; иначе показываются текст «VOXERA» и системные SF Symbols.

Для релиза в App Store добавьте `1024×1024` в `AppIcon.appiconset`.

## Тексты «О приложении»

Краткие секции и 15 слайдов полного описания генерируются из `AboutPresentationData.kt` скриптом:

`python ios/scripts/emit_about_swift.py`
