# Voxera — публичная политика конфиденциальности

HTML-версия текста из `app/.../legal/PrivacyPolicyContent.kt` для Google Play Console и App Store.

## Публикация через GitHub Pages

1. Закоммитьте и запушьте папку `docs/` в репозиторий `VoxeraMVP`.
2. GitHub → **Settings → Pages**.
3. **Source:** Deploy from a branch.
4. **Branch:** `master` (или `main`) → папка **`/docs`** → Save.
5. Через 1–3 минуты страница будет доступна по адресу:

   ```
   https://dfyz0224-art.github.io/VoxeraMVP/privacy.html
   ```

   (если репозиторий или организация другие — замените `dfyz0224-art` и `VoxeraMVP`).

6. Этот URL вставьте в Play Console → **Политика конфиденциальности**.

## Локальная проверка

Откройте `docs/privacy.html` в браузере (двойной клик или Live Server).

## Синхронизация с приложением

При изменении текста в `PrivacyPolicyContent.kt` обновите `docs/privacy.html` вручную (или скриптом `ios/scripts/extract_privacy_txt.py` + конвертация в HTML).
