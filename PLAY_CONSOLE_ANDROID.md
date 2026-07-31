# Voxera — выпуск Android в Google Play Console

## Что загрузить в «Наборы App Bundle»

Файл **Android App Bundle (`.aab`)**, не APK:

```
app/build/outputs/bundle/release/app-release.aab
```

Имя может отличаться, главное — расширение **`.aab`** и тип сборки **release**.

---

## Шаг 1. Подготовка на вашем компьютере (один раз)

### 1.1 Секреты приложения

```text
secrets.properties          ← из secrets.properties.example
app/google-services.json    ← из Firebase Console
```

В `secrets.properties`:
- `VOXERA_API_TOKEN` — прод-токен API
- `GOOGLE_WEB_CLIENT_ID` — Web Client ID из Firebase/Google Cloud (для Google Sign-In)

### 1.2 Upload keystore (один раз, сохраните пароли!)

В Android Studio: **Build → Generate Signed App Bundle / APK → Android App Bundle → Create new...**

Или из терминала (JDK из Android Studio):

```bat
keytool -genkeypair -v -keystore release.keystore -alias voxera -keyalg RSA -keysize 2048 -validity 10000
```

Скопируйте `keystore.properties.example` → `keystore.properties` и укажите пути/пароли.

### 1.3 SHA-1 для Firebase (после создания keystore)

```bat
keytool -list -v -keystore release.keystore -alias voxera
```

Добавьте **SHA-1** (и SHA-256) в Firebase → Project settings → Android app `com.vanoprojects.voxera`.  
При необходимости скачайте обновлённый `google-services.json`.

---

## Шаг 2. Сборка AAB

### Android Studio

**Build → Generate Signed App Bundle / APK** → App Bundle → release keystore → **release**.

### Gradle (из корня проекта)

```bat
gradlew.bat bundleRelease
```

Проверка: файл `app/build/outputs/bundle/release/app-release.aab` существует.

Текущие версии в `app/build.gradle.kts`:
- `applicationId`: `com.vanoprojects.voxera`
- `versionCode`: **1** (увеличивайте на +1 перед каждым новым загрузом в Play)
- `versionName`: **0.1.0**

---

## Шаг 3. Play Console — внутреннее тестирование (как на вашем скрине)

1. **Тестирование и выпуск → Внутреннее тестирование → Создать выпуск**
2. В блок **«Наборы App Bundle»** → **Загрузить** → выберите `app-release.aab`
3. Дождитесь обработки (обычно 1–5 минут)
4. Заполните **«Примечания к выпуску»** (что нового — можно кратко: «Первый внутренний билд»)
5. **Далее → Просмотреть → Запустить выпуск**

### Тестировщики

**Тестирование → Внутреннее тестирование → Тестировщики** — добавьте email Google-аккаунтов.  
По ссылке из консоли тестировщики установят приложение из Play (не через APK).

---

## Шаг 4. Что нужно от вас в консоли (до production)

| Раздел | Что сделать |
|--------|-------------|
| **Верификация аккаунта** | Документы / D-U-N-S для организации (если ещё не пройдено) |
| **Политика приложения** | Контент, целевая аудитория, реклама (да/нет) |
| **Безопасность данных** | Микрофон, аккаунты, Firebase — указать сбор и цели |
| **Политика конфиденциальности** | URL (обязательно для RECORD_AUDIO и auth) |
| **Магазин → Основная информация** | Описание, иконка 512×512, скриншоты (мин. 2), feature graphic 1024×500 |
| **Release → Production** | После успешного internal/closed testing |

---

## Чеклист перед загрузкой AAB

- [ ] `secrets.properties` с прод-значениями
- [ ] `app/google-services.json` для `com.vanoprojects.voxera`
- [ ] `keystore.properties` + `release.keystore` (не в git)
- [ ] SHA-1 **upload**-ключа в Firebase
- [ ] SHA-1 **App signing** из Play Console → Setup → App integrity → App signing key certificate → в Firebase (иначе Google Sign-In на телефоне из Play молча падает: аккаунт выбирается, idToken = null)
- [ ] `bundleRelease` успешно
- [ ] Package name в консоли: **`com.vanoprojects.voxera`**
- [ ] На устройстве проверены: запись, API, Google Sign-In, onboarding

---

## Частые ошибки

| Ошибка | Решение |
|--------|---------|
| Неверная подпись | AAB подписан upload-keystore из `keystore.properties` |
| Version code уже использован | Увеличить `versionCode` в `build.gradle.kts` |
| Package name не совпадает | Должен быть `com.vanoprojects.voxera` |
| Google Sign-In не работает в release | SHA-1 release keystore в Firebase |
