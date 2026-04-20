import re
from pathlib import Path

root = Path(__file__).resolve().parents[2]
kt = (
    root / "app/src/main/java/com/vanoprojects/voxera/legal/PrivacyPolicyContent.kt"
).read_text(encoding="utf-8")
m = re.search(r' = """(.*?)"""\.trimIndent\(\)', kt, re.S)
if not m:
    raise SystemExit("block not found")
out = root / "ios/Voxera/Resources/PrivacyPolicyFull.txt"
out.parent.mkdir(parents=True, exist_ok=True)
out.write_text(m.group(1).strip(), encoding="utf-8")
print("wrote", out, "chars", out.stat().st_size)
