"""
Extract AppStrings initializer bodies from Android Strings.kt (Ru/En/Zh/Kz blocks).
Run from repo root: python ios/scripts/extract_strings_to_swift.py
Outputs ios/Voxera/Generated/StringPackRU.swift etc.
"""
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
STRINGS_KT = ROOT / "app/src/main/java/com/vanoprojects/voxera/ui/strings/Strings.kt"
OUT_DIR = ROOT / "ios/Voxera/Generated"

FIELD_RE = re.compile(r"^\s*val\s+(\w+)\s*:\s*String")


def parse_block(content: str, block_name: str) -> dict[str, str]:
    """Parse Kotlin Strings( key = "...", ...) within val BlockName = Strings(..."""
    start = content.find(f"val {block_name} = Strings(")
    if start < 0:
        raise SystemExit(f"Block {block_name} not found")
    depth = 0
    i = content.find("Strings(", start)
    i = content.find("(", i) + 1
    depth = 1
    buf = []
    in_string = False
    esc = False
    key = None
    result: dict[str, str] = {}
    j = i
    while j < len(content) and depth > 0:
        c = content[j]
        if not in_string:
            if c == "(":
                depth += 1
                j += 1
                continue
            if c == ")":
                depth -= 1
                if depth == 0:
                    break
                j += 1
                continue
            m = re.match(r"\s*(\w+)\s*=", content[j:])
            if m:
                key = m.group(1)
                j += m.end()
                rest = content[j:].lstrip()
                if rest.startswith('"'):
                    in_string = True
                    buf = []
                    esc = False
                    j = j + len(content[j:]) - len(rest) + 1
                    continue
                # multiline string starts with """
                if rest.startswith('"""'):
                    in_string = True
                    buf = []
                    esc = False
                    j = j + len(content[j:]) - len(rest) + 3
                    continue
            j += 1
            continue
        # inside string "..."
        if content.startswith('"""', j - 3) if j >= 3 else False:
            pass
        # simplified: handle "..." only (Kotlin uses + for concat)
        j += 1
    return result  # stub — full parser is heavy


# Simpler approach: regex extract key = "value" lines only (single-line values)
def parse_simple_block(content: str, block_name: str) -> dict[str, str]:
    start = content.find(f"val {block_name} = Strings(")
    end = content.find("\n        )", start)
    if end < 0:
        end = content.find("\n    )", start)
    block = content[start:end]
    out: dict[str, str] = {}
    for m in re.finditer(r"(\w+)\s*=\s*\"((?:[^\"\\]|\\.)*)\"", block, re.DOTALL):
        out[m.group(1)] = m.group(2).replace("\\n", "\n")
    # multiline """ ... """
    for m in re.finditer(r"(\w+)\s*=\s*\"\"\"(.*?)\"\"\"", block, re.DOTALL):
        out[m.group(1)] = m.group(2).strip().replace("\\n", "\n")
    return out


def main():
    text = STRINGS_KT.read_text(encoding="utf-8")
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    order = parse_simple_block(text, "Ru")
    if len(order) < 50:
        print("Warning: few keys parsed", len(order))
    # emit Swift
    for lang, block in [("RU", "Ru"), ("EN", "En"), ("ZH", "Zh"), ("KZ", "Kz")]:
        data = parse_simple_block(text, block)
        lines = ["extension AppStrings {", f"  static var {block.lower()}: AppStrings {{"]
        lines.append("    AppStrings(")
        # field order from first block
        keys = list(order.keys()) if order else list(data.keys())
        if not keys:
            keys = sorted(data.keys())
        parts = []
        for k in keys:
            if k not in data:
                continue
            v = data[k].replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
            parts.append(f"      {k}: \"{v}\"")
        lines.append(",\n".join(parts))
        lines.append("    )")
        lines.append("  }")
        lines.append("}")
        out = OUT_DIR / f"StringPack{lang}.swift"
        out.write_text("\n".join(lines) + "\n", encoding="utf-8")
        print(lang, len(data), out)


if __name__ == "__main__":
    main()
