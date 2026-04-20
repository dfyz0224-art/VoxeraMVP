"""Emit AppStrings from Kotlin: lines between data class ( and first ) { — only lines with 'val x: String'."""
import re
from pathlib import Path

root = Path(__file__).resolve().parents[2]
lines = (root / "app/src/main/java/com/vanoprojects/voxera/ui/strings/Strings.kt").read_text(encoding="utf-8").splitlines()

dc_start = next(i for i, l in enumerate(lines) if l.strip().startswith("data class Strings("))
dc_end = next(i for i in range(dc_start + 1, len(lines)) if lines[i].strip() == ") {")

fields: list[str] = []
for line in lines[dc_start + 1 : dc_end]:
    s = line.strip()
    if not (s.startswith("val ") and ": String" in s):
        continue
    # Avoid matching "value..." lines that aren't declarations
    if not re.match(r"^val\s+\w+\s*:\s*String", s):
        continue
    if s.endswith(","):
        s = s[:-1].strip()
    # val name: String
    rest = s[4:].split(":")[0].strip()
    fields.append(rest)

out = ["struct AppStrings: Equatable {"]
out.extend(f"  let {f}: String" for f in fields)
out.append("}")
(root / "ios/Voxera/AppStrings.swift").write_text("\n".join(out) + "\n", encoding="utf-8")
print("fields", len(fields))
