"""Add new Swift sources to Voxera.xcodeproj/project.pbxproj."""
import re
import uuid
from pathlib import Path

pbx = Path(__file__).resolve().parents[1] / "Voxera.xcodeproj" / "project.pbxproj"
text = pbx.read_text(encoding="utf-8")


def uid() -> str:
    return uuid.uuid4().hex[:24].upper()


files = [
    ("AuthCardContent.swift", "Screens"),
    ("SubscriptionsView.swift", "Screens"),
    ("CredentialStore.swift", "Services"),
]

# Skip if already present
files = [(n, g) for n, g in files if f"/* {n} */" not in text]
if not files:
    print("already present")
    raise SystemExit(0)

entries = []
for name, group in files:
    entries.append((name, group, uid(), uid()))

bf_block = "".join(
    f"\t\t{bf} /* {name} in Sources */ = {{isa = PBXBuildFile; fileRef = {fr} /* {name} */; }};\n"
    for name, group, fr, bf in entries
)
text = text.replace("/* End PBXBuildFile section */", bf_block + "/* End PBXBuildFile section */")

fr_block = "".join(
    "\t\t{fr} /* {name} */ = {{isa = PBXFileReference; lastKnownFileType = sourcecode.swift; "
    'path = {name}; sourceTree = "<group>"; }};\n'.format(fr=fr, name=name)
    for name, group, fr, bf in entries
)
text = text.replace("/* End PBXFileReference section */", fr_block + "/* End PBXFileReference section */")

for name, group, fr, bf in entries:
    if group == "Screens":
        needle = "AF5013032A96D4A7E838E0B6 /* AuthView.swift */,"
        text = text.replace(needle, needle + f"\n\t\t\t\t{fr} /* {name} */,", 1)

services_pat = re.compile(
    r"(C68A7EA58B79E4E28FF0AA85 /\* Services \*/ = \{\s*isa = PBXGroup;\s*children = \()\n",
    re.M,
)
m = services_pat.search(text)
if m:
    insert = m.group(1) + "\n"
    for name, group, fr, bf in entries:
        if group == "Services":
            insert += f"\t\t\t\t{fr} /* {name} */,\n"
    text = text[: m.start(1)] + insert + text[m.end() :]

for name, group, fr, bf in entries:
    needle = "2FA08539A83EE845C644097E /* AuthView.swift in Sources */,"
    text = text.replace(needle, needle + f"\n\t\t\t\t{bf} /* {name} in Sources */,", 1)

pbx.write_text(text, encoding="utf-8")
print("added", [e[0] for e in entries])
