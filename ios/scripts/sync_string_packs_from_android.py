# -*- coding: utf-8 -*-
"""Sync iOS StringPack* from Android Strings.kt + StringsUkKa.kt using AppStrings field order."""
import re
from pathlib import Path

root = Path(__file__).resolve().parents[2]
kt_path = root / "app/src/main/java/com/vanoprojects/voxera/ui/strings/Strings.kt"
uk_ka_path = root / "app/src/main/java/com/vanoprojects/voxera/ui/strings/StringsUkKa.kt"
app_strings_path = root / "ios/Voxera/AppStrings.swift"
gen = root / "ios/Voxera/Generated"


def extract_block(text: str, start_pat: str) -> str:
    start = text.find(start_pat)
    if start < 0:
        raise SystemExit(f"missing {start_pat}")
    start += len(start_pat)
    depth = 1
    i = start
    while i < len(text) and depth > 0:
        c = text[i]
        if c == "(":
            depth += 1
        elif c == ")":
            depth -= 1
        i += 1
    return text[start : i - 1]


def parse_entries(block: str) -> dict[str, str]:
    out: dict[str, str] = {}
    # Join Kotlin string concatenations: key = "a" + "b" possibly across lines
    # Also: key =\n  "a" +\n  "b"
    text = block
    # Collapse whitespace between + and next quote
    text = re.sub(r'"\s*\+\s*"', "", text)  # adjacent " + " -> join contents later via simpler approach
    # Better: find key = then collect quoted segments until comma/newline-not-quote
    i = 0
    while True:
        m = re.search(r"(\w+)\s*=", text[i:])
        if not m:
            break
        key = m.group(1)
        pos = i + m.end()
        # skip whitespace
        while pos < len(text) and text[pos] in " \t\r\n":
            pos += 1
        if pos >= len(text):
            break
        if text[pos] == '"':
            parts: list[str] = []
            while pos < len(text) and text[pos] == '"':
                pos += 1
                buf = []
                while pos < len(text):
                    c = text[pos]
                    if c == "\\" and pos + 1 < len(text):
                        n = text[pos + 1]
                        if n == "n":
                            buf.append("\n")
                        elif n == '"':
                            buf.append('"')
                        elif n == "t":
                            buf.append("\t")
                        elif n == "\\":
                            buf.append("\\")
                        else:
                            buf.append(n)
                        pos += 2
                        continue
                    if c == '"':
                        pos += 1
                        break
                    buf.append(c)
                    pos += 1
                parts.append("".join(buf))
                while pos < len(text) and text[pos] in " \t\r\n":
                    pos += 1
                if pos < len(text) and text[pos] == "+":
                    pos += 1
                    while pos < len(text) and text[pos] in " \t\r\n":
                        pos += 1
                    continue
                break
            out[key] = "".join(parts)
            i = pos
            continue
        # skip unknown assignment forms
        i = pos + 1
    return out


def swift_escape(s: str) -> str:
    return (
        s.replace("\\", "\\\\")
        .replace('"', '\\"')
        .replace("\n", "\\n")
        .replace("\r", "")
    )


def emit_extension(suffix: str, data: dict[str, str], field_order: list[str]) -> str:
    parts = []
    for k in field_order:
        v = data.get(k, "")
        if k not in data:
            print("WARN missing", suffix, k)
        parts.append(f'      {k}: "{swift_escape(v)}"')
    return (
        "extension AppStrings {\n"
        f"  static var {suffix}: AppStrings {{\n"
        "    AppStrings(\n"
        + ",\n".join(parts)
        + "\n    )\n  }\n}\n"
    )


def emit_struct(field_order: list[str]) -> str:
    body = "\n".join(f"  let {k}: String" for k in field_order)
    return f"struct AppStrings: Equatable {{\n{body}\n}}\n"


def main():
    kt = kt_path.read_text(encoding="utf-8")
    uk_ka = uk_ka_path.read_text(encoding="utf-8")

    # AppStrings field order = Kotlin data class fields that exist in current iOS + new language labels
    lines = kt.splitlines()
    dcs = next(i for i, l in enumerate(lines) if l.strip().startswith("data class Strings("))
    dce = next(i for i in range(dcs + 1, len(lines)) if lines[i].strip() == ") {")
    kt_fields: list[str] = []
    for line in lines[dcs + 1 : dce]:
        m = re.match(r"^\s+val\s+(\w+)\s*:\s*String", line)
        if m:
            kt_fields.append(m.group(1))

    # Keep iOS-compatible subset: existing AppStrings fields + parity fields from Android
    existing = re.findall(r"^\s+let\s+(\w+)\s*:\s*String", app_strings_path.read_text(encoding="utf-8"), re.M)
    wanted = set(existing) | {
        "languageUk",
        "languageKa",
        "goHome",
        "analyzeRetry",
        "authForgotPassword",
        "authResetEmailSent",
        "authVerifyEmailSent",
        "authEmailNotVerified",
        "authResendVerification",
    }
    # Preserve Kotlin order for common fields, append any missing wanted at end
    field_order = [f for f in kt_fields if f in wanted]
    for f in existing:
        if f not in field_order:
            field_order.append(f)
    for f in (
        "languageUk",
        "languageKa",
        "goHome",
        "analyzeRetry",
        "authForgotPassword",
        "authResetEmailSent",
        "authVerifyEmailSent",
        "authEmailNotVerified",
        "authResendVerification",
    ):
        if f not in field_order:
            field_order.append(f)

    packs = {}
    for lang, suffix in [("Ru", "ru"), ("En", "en"), ("Zh", "zh"), ("Kz", "kz")]:
        data = parse_entries(extract_block(kt, f"val {lang} = Strings("))
        packs[suffix] = data

    ru = packs["ru"]
    uk_overrides = parse_entries(extract_block(uk_ka, "internal fun buildUkStrings(): Strings = Strings.Ru.copy("))
    ka_overrides = parse_entries(extract_block(uk_ka, "internal fun buildKaStrings(): Strings = Strings.En.copy("))
    packs["uk"] = {**ru, **uk_overrides}
    packs["ka"] = {**packs["en"], **ka_overrides}

    gen.mkdir(parents=True, exist_ok=True)
    app_strings_path.write_text(emit_struct(field_order), encoding="utf-8")
    print("AppStrings fields", len(field_order))

    names = {
        "ru": "StringPackRU.swift",
        "en": "StringPackEN.swift",
        "zh": "StringPackZH.swift",
        "kz": "StringPackKZ.swift",
        "uk": "StringPackUK.swift",
        "ka": "StringPackKA.swift",
    }
    for suffix, fname in names.items():
        text = emit_extension(suffix, packs[suffix], field_order)
        (gen / fname).write_text(text, encoding="utf-8")
        print("wrote", fname, "keys", len(packs[suffix]))


if __name__ == "__main__":
    main()
