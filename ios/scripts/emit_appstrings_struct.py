import re
from pathlib import Path

root = Path(__file__).resolve().parents[2]
src = root / "ios/Voxera/Generated/StringPackRU.swift"
lines = src.read_text(encoding="utf-8").splitlines()
fields: list[str] = []
seen: set[str] = set()
for ln in lines:
    m = re.match(r'^      (\w+):\s*\"', ln)  # 6 spaces, value starts with "
    if not m:
        continue
    name = m.group(1)
    if name in seen:
        continue
    seen.add(name)
    fields.append(name)
out = ["struct AppStrings: Equatable {"]
out.extend(f"  let {f}: String" for f in fields)
out.append("}")
(root / "ios/Voxera/AppStrings.swift").write_text("\n".join(out) + "\n", encoding="utf-8")
print("fields", len(fields))
