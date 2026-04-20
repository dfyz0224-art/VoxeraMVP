"""
Regenerate ios/Voxera/Generated/StringPack*.swift from Strings.kt companion blocks.
Parses val Ru/En/Zh/Kz = Strings( ... ) with key = "value" (concatenation with + joined).
"""
import re
from pathlib import Path

root = Path(__file__).resolve().parents[2]
kt = (root / "app/src/main/java/com/vanoprojects/voxera/ui/strings/Strings.kt").read_text(encoding="utf-8")


def extract_block(lang: str) -> str:
    start_pat = f"val {lang} = Strings("
    start = kt.find(start_pat)
    if start < 0:
        raise SystemExit(f"missing {lang}")
    start += len(start_pat)
    depth = 1
    i = start
    while i < len(kt) and depth > 0:
        c = kt[i]
        if c == "(":
            depth += 1
        elif c == ")":
            depth -= 1
        i += 1
    return kt[start : i - 1]


def parse_entries(block: str) -> dict[str, str]:
    """Collect key = quoted value entries; join lines ending with +."""
    out: dict[str, str] = {}
    # Normalize: merge Kotlin string concat lines (ending with +)
    lines = block.splitlines()
    merged: list[str] = []
    buf = ""
    for line in lines:
        s = line.strip()
        if not s:
            continue
        if buf:
            buf += " " + s
        else:
            buf = s
        if buf.rstrip().endswith("+"):
            buf = buf.rstrip()[:-1].strip()
        else:
            merged.append(buf)
            buf = ""
    if buf:
        merged.append(buf)
    for line in merged:
        # key = "value"
        m = re.match(r'^(\w+)\s*=\s*"(.*)"\s*,?\s*$', line)
        if m:
            key, val = m.group(1), m.group(2)
            val = (
                val.replace("\\n", "\n")
                .replace('\\"', '"')
                .replace("\\t", "\t")
            )
            out[key] = val
            continue
        # multiline """
        m = re.match(r'^(\w+)\s*=\s*"""(.*?)"""\s*,?\s*$', line, re.DOTALL)
        if m:
            out[m.group(1)] = m.group(2).strip()
            continue
    return out


def swift_escape(s: str) -> str:
    return (
        s.replace("\\", "\\\\")
        .replace('"', '\\"')
        .replace("\n", "\\n")
        .replace("\r", "")
    )


def emit_extension(lang: str, suffix: str, data: dict[str, str], field_order: list[str]) -> str:
    lines_out = [f"extension AppStrings {{", f"  static var {suffix}: AppStrings {{", "    AppStrings("]
    parts = []
    for k in field_order:
        v = data.get(k, "MISSING")
        if v == "MISSING":
            print("WARN missing", lang, k)
            v = ""
        parts.append(f"      {k}: \"{swift_escape(v)}\"")
    lines_out.append(",\n".join(parts))
    lines_out.append("    )")
    lines_out.append("  }")
    lines_out.append("}")
    return "\n".join(lines_out) + "\n"


# Kotlin multiline + concat not handled by parse_entries — filled explicitly
OVERRIDES: dict[str, dict[str, str]] = {
    "Ru": {
        "consentCardSummary": (
            "Кратко: VOICElab обрабатывает аудио и технические данные в приложении Voxera для предоставления анализа. "
            "Это не медицинский диагноз. Полный текст политики — по ссылке ниже; при желании откройте и прочитайте, затем отметьте пункты."
        ),
        "emostateParamHintFallback": (
            "Показатель отражает один из аспектов голосового профиля в текущем контексте. "
            "Смотрите на значения в совокупности с остальными метриками."
        ),
        "emostateGeneralDisclaimerBody": (
            "Результаты основаны на анализе голоса и отражают текущее состояние.\n"
            "Значения могут меняться в зависимости от самочувствия и контекста."
        ),
    },
    "En": {
        "consentCardSummary": (
            "In short: VOICElab processes audio and technical data you provide in the Voxera app to deliver the service. "
            "This is not a medical diagnosis. The full policy is available via the link below — open it if you wish, then check the boxes to continue."
        ),
        "emostateParamHintFallback": (
            "This metric reflects one aspect of your voice profile in the current context. "
            "Interpret it together with the other metrics."
        ),
        "emostateGeneralDisclaimerBody": (
            "Results are based on voice analysis and reflect your current state.\n"
            "Values may change with how you feel and the situation."
        ),
    },
    "Zh": {
        "consentCardSummary": (
            "简要说明：VOICElab 会在 Voxera 应用中处理您提供的音频与技术数据以提供服务。"
            "这不是医学诊断。完整政策见下方链接；您可随时打开阅读，然后勾选以继续。"
        ),
        "emostateParamHintFallback": "该指标反映当前语境下声音特征的一个方面。请结合其他指标综合理解。",
        "emostateGeneralDisclaimerBody": "结果基于语音分析，反映当前状态。\n数值可能随身体状况与场景而变化。",
    },
    "Kz": {
        "consentCardSummary": (
            "Қысқаша: VOICElab Voxera қолданбасында сіз берген аудио және техникалық деректерді қызмет көрсету үшін өңдейді. "
            "Бұл медициналық диагноз емес. Толық саясат төмендегі сілтемеде; қалағанда оқыңыз, содан кейін белгілерді қойыңыз."
        ),
        "emostateParamHintFallback": (
            "Бұл көрсеткіс дауыстық профильдің ағымдағы контекстегі бір жақтарын көрсетеді. "
            "Басқа метрикалармен бірге қараңыз."
        ),
        "emostateGeneralDisclaimerBody": (
            "Нәтижелер дауысты талдауға негізделген және ағымдағы күйді көрсетеді.\n"
            "Мәндер денсаулық пен жағдайға байланысты өзгеруі мүмкін."
        ),
    },
}


def main():
    # Field order from data class
    lines = kt.splitlines()
    dcs = next(i for i, l in enumerate(lines) if l.strip().startswith("data class Strings("))
    dce = next(i for i in range(dcs + 1, len(lines)) if lines[i].strip() == ") {")
    field_order: list[str] = []
    for line in lines[dcs + 1 : dce]:
        m = re.match(r"^\s+val\s+(\w+)\s*:\s*String", line)
        if m:
            field_order.append(m.group(1))
    print("field_order", len(field_order))

    gen = root / "ios/Voxera/Generated"
    gen.mkdir(parents=True, exist_ok=True)
    for lang, suffix in [("Ru", "ru"), ("En", "en"), ("Zh", "zh"), ("Kz", "kz")]:
        block = extract_block(lang)
        data = parse_entries(block)
        data.update(OVERRIDES.get(lang, {}))
        print(lang, "parsed keys", len(data))
        text = emit_extension(lang, suffix, data, field_order)
        fname = {"Ru": "StringPackRU.swift", "En": "StringPackEN.swift", "Zh": "StringPackZH.swift", "Kz": "StringPackKZ.swift"}[lang]
        (gen / fname).write_text(text, encoding="utf-8")


if __name__ == "__main__":
    main()
