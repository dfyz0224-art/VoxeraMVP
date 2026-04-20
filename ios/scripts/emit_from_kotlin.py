"""Emit AppStrings.swift from Kotlin data class Strings only (not companion)."""
import re
from pathlib import Path

root = Path(__file__).resolve().parents[2]
lines = (root / "app/src/main/java/com/vanoprojects/voxera/ui/strings/Strings.kt").read_text(encoding="utf-8").splitlines()

dc_start = next(i for i, l in enumerate(lines) if l.strip().startswith("data class Strings("))
dc_end = next(i for i in range(dc_start + 1, len(lines)) if lines[i].strip() == ") {")

fields: list[str] = []
for line in lines[dc_start + 1 : dc_end]:
    m = re.match(r"^\s+val\s+(\w+)\s*:\s*String", line)
    if m:
        fields.append(m.group(1))

out = ["struct AppStrings: Equatable {"]
out.extend(f"  let {f}: String" for f in fields)
out.append("}")
(root / "ios/Voxera/AppStrings.swift").write_text("\n".join(out) + "\n", encoding="utf-8")
print("data_class_lines", dc_start + 1, "to", dc_end + 1, "fields", len(fields))
