import re
from pathlib import Path

lines = Path(
    r"c:/cursors/compose/VoxeraComposeMvp/app/src/main/java/com/vanoprojects/voxera/ui/strings/Strings.kt"
).read_text(encoding="utf-8").splitlines()
n = 0
for i, l in enumerate(lines):
    if re.match(r"^\s+val\s+\w+\s*:\s*String", l):
        n += 1
        if n <= 5 or n > 95:
            print(i + 1, l[:70])
print("total", n)
