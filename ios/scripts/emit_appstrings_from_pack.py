"""Emit AppStrings.swift from StringPackRU.swift (one key per line)."""
import re
from pathlib import Path

root = Path(__file__).resolve().parents[2]
lines = (root / "ios/Voxera/Generated/StringPackRU.swift").read_text(encoding="utf-8").splitlines()
fields: list[str] = []
for line in lines:
    line_stripped_right = line.rstrip()
    if not (line_stripped_right.endswith('",') or line_stripped_right.endswith('")')):
        continue
    m = re.match(r"^      (\w+):\s*\"", line)
    if m:
        fields.append(m.group(1))

if len(fields) != len(set(fields)):
    dup = [f for f in fields if fields.count(f) > 1]
    raise SystemExit(f"duplicates: {set(dup)}")

out = ["struct AppStrings: Equatable {"]
out.extend(f"  let {f}: String" for f in fields)
out.append("}")
(root / "ios/Voxera/AppStrings.swift").write_text("\n".join(out) + "\n", encoding="utf-8")
print("fields", len(fields))
