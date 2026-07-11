# Voxera — публичные legal-страницы

| Файл | Назначение | URL после GitHub Pages |
|------|------------|------------------------|
| `privacy.html` | Политика конфиденциальности | `…/privacy.html` |
| `account-deletion.html` | Запрос на удаление аккаунта (Play Data safety) | `…/account-deletion.html` |
| `index.html` | Оглавление | `…/` |

Текст политики синхронизирован с `app/.../legal/PrivacyPolicyContent.kt`.

## Публикация через GitHub Pages

1. Закоммитьте и запушьте папку `docs/` в репозиторий `VoxeraMVP`.
2. GitHub → **Settings → Pages**.
3. **Source:** Deploy from a branch.
4. **Branch:** `master` → папка **`/docs`** → **Save**.
5. Через 1–3 минуты:

   ```
   https://dfyz0224-art.github.io/VoxeraMVP/privacy.html
   https://dfyz0224-art.github.io/VoxeraMVP/account-deletion.html
   ```

6. В Play Console:
   - **Политика конфиденциальности** → URL `privacy.html`
   - **Безопасность данных → URL удаления аккаунта** → URL `account-deletion.html`

## Локальная проверка

Откройте `docs/account-deletion.html` или `docs/privacy.html` в браузере.
