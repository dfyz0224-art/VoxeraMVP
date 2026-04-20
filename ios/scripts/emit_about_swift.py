"""Emit ios/Voxera/About/AboutContent.swift from Android AboutPresentationData.kt"""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
KT = ROOT / "app/src/main/java/com/vanoprojects/voxera/ui/screens/AboutPresentationData.kt"

SLIDE_RE = re.compile(
    r'AboutSlide\(\s*title\s*=\s*"((?:[^"\\]|\\.)*)"\s*,\s*body\s*=\s*"((?:[^"\\]|\\.)*)"\s*\)',
    re.DOTALL,
)


def kotlin_unescape(s: str) -> str:
    return (
        s.replace("\\n", "\n")
        .replace("\\t", "\t")
        .replace('\\"', '"')
        .replace("\\\\", "\\")
    )


def swift_escape(s: str) -> str:
    return s.replace("\\", "\\\\").replace('"', '\\"').replace("\n", "\\n")


def extract_slides_between(text: str, start_marker: str, end_marker: str) -> list[tuple[str, str]]:
    i = text.find(start_marker)
    if i < 0:
        raise SystemExit(f"start not found: {start_marker}")
    j = text.find(end_marker, i + 1)
    if j < 0:
        raise SystemExit(f"end not found: {end_marker}")
    block = text[i:j]
    out = []
    for m in SLIDE_RE.finditer(block):
        out.append((kotlin_unescape(m.group(1)), kotlin_unescape(m.group(2))))
    return out


def balanced_paren_span(s: str, open_index: int) -> tuple[str, int]:
    if open_index >= len(s) or s[open_index] != "(":
        raise ValueError("expected '('")
    depth = 0
    for i in range(open_index, len(s)):
        c = s[i]
        if c == "(":
            depth += 1
        elif c == ")":
            depth -= 1
            if depth == 0:
                return s[open_index : i + 1], i + 1
    raise ValueError("unbalanced")


def extract_blocks_inner(section_chunk: str) -> str:
    key = "blocks = listOf("
    i = section_chunk.find(key)
    if i < 0:
        raise SystemExit("blocks = listOf not found in section")
    open_idx = i + len(key) - 1
    span, _ = balanced_paren_span(section_chunk, open_idx)
    return span[1:-1]


def parse_short_section_blocks(inner: str) -> list[tuple[str, list]]:
    blocks: list[tuple[str, list]] = []
    pos = 0
    while pos < len(inner):
        mws = re.match(r"[\s,]*", inner[pos:])
        if mws:
            pos += mws.end()
        if pos >= len(inner):
            break
        if inner.startswith("AboutBlock.Paragraph(", pos):
            open_idx = pos + len("AboutBlock.Paragraph")
            span, _ = balanced_paren_span(inner, open_idx)
            inner_p = span[1:-1].strip()
            mm = re.match(r'"((?:[^"\\]|\\.)*)"', inner_p)
            if not mm:
                raise SystemExit(f"bad Paragraph: {inner_p[:80]}")
            blocks.append(("p", [kotlin_unescape(mm.group(1))]))
            pos += len("AboutBlock.Paragraph") + len(span)
            continue
        if inner.startswith("AboutBlock.Bullets(", pos):
            open_idx = pos + len("AboutBlock.Bullets")
            span, _ = balanced_paren_span(inner, open_idx)
            bullets_inner = span[1:-1]
            lo = bullets_inner.find("listOf(")
            if lo < 0:
                raise SystemExit("Bullets without listOf")
            open2 = lo + bullets_inner[lo:].index("(")
            list_span, _ = balanced_paren_span(bullets_inner, open2)
            list_inner = list_span[1:-1]
            lines = []
            for sm in re.finditer(r'"((?:[^"\\]|\\.)*)"', list_inner):
                lines.append(kotlin_unescape(sm.group(1)))
            blocks.append(("b", lines))
            pos += len("AboutBlock.Bullets") + len(span)
            continue
        raise SystemExit(f"unknown block at pos {pos}: {inner[pos : pos + 60]!r}")
    return blocks


def split_short_sections(about_short_body: str) -> list[str]:
    parts = re.split(r"\n  AboutShortSection\(\n", about_short_body)
    return [p for p in parts if "title =" in p]


def parse_short_lang(kt: str, start_marker: str, end_marker: str) -> list[tuple[str, list]]:
    i = kt.find(start_marker)
    if i < 0:
        raise SystemExit(f"missing {start_marker}")
    j = kt.find(end_marker, i + 1)
    if j < 0:
        raise SystemExit(f"missing {end_marker}")
    body = kt[i:j]
    sections: list[tuple[str, list]] = []
    for raw in split_short_sections(body):
        tm = re.search(r'title\s*=\s*"((?:[^"\\]|\\.)*)"', raw)
        if not tm:
            continue
        title = kotlin_unescape(tm.group(1))
        bi = raw.find("blocks = listOf(")
        if bi < 0:
            continue
        section_from_blocks = raw[bi:]
        inner = extract_blocks_inner(section_from_blocks)
        blocks = parse_short_section_blocks(inner)
        sections.append((title, blocks))
    return sections


def extract_slides_kz_tail(text: str) -> list[tuple[str, str]]:
    start = text.find("private fun slidesKz()")
    if start < 0:
        raise SystemExit("slidesKz not found")
    block = text[start:]
    out = []
    for m in SLIDE_RE.finditer(block):
        out.append((kotlin_unescape(m.group(1)), kotlin_unescape(m.group(2))))
    return out


def emit_short_section_swift(sections: list[tuple[str, list]], indent: str) -> list[str]:
    lines: list[str] = []
    lines.append(f"{indent}return [")
    for title, blocks in sections:
        lines.append(f'{indent}  AboutShortSectionData(')
        lines.append(f'{indent}    title: "{swift_escape(title)}",')
        lines.append(f"{indent}    blocks: [")
        for kind, payload in blocks:
            if kind == "p":
                lines.append(f'{indent}      .paragraph("{swift_escape(payload[0])}"),')
            else:
                lines.append(f"{indent}      .bullets([")
                for line in payload:
                    lines.append(f'{indent}        "{swift_escape(line)}",')
                lines.append(f"{indent}      ]),")
        lines.append(f"{indent}    ]")
        lines.append(f"{indent}  ),")
    lines.append(f"{indent}]")
    return lines


def main() -> None:
    kt = KT.read_text(encoding="utf-8")
    slides = {
        "ru": extract_slides_between(kt, "private fun slidesRu()", "private fun slidesEn()"),
        "en": extract_slides_between(kt, "private fun slidesEn()", "private fun slidesZh()"),
        "zh": extract_slides_between(kt, "private fun slidesZh()", "private fun slidesKz()"),
        "kz": extract_slides_kz_tail(kt),
    }
    short = {
        "ru": parse_short_lang(kt, "private fun aboutShortRu()", "private fun aboutShortEn()"),
        "en": parse_short_lang(kt, "private fun aboutShortEn()", "private fun aboutShortZh()"),
        "zh": parse_short_lang(kt, "private fun aboutShortZh()", "private fun aboutShortKz()"),
        "kz": parse_short_lang(kt, "private fun aboutShortKz()", "// --- Полное описание"),
    }

    for lang, items in slides.items():
        print("slides", lang, len(items))
    for lang, items in short.items():
        print("short", lang, len(items))

    lines: list[str] = [
        "// Generated by ios/scripts/emit_about_swift.py — do not edit by hand.",
        "import Foundation",
        "",
        "enum AboutBlockData: Equatable {",
        "  case paragraph(String)",
        "  case bullets([String])",
        "}",
        "",
        "struct AboutShortSectionData: Equatable {",
        "  let title: String",
        "  let blocks: [AboutBlockData]",
        "}",
        "",
        "struct AboutSlideData: Equatable {",
        "  let title: String",
        "  let body: String",
        "}",
        "",
        "enum AboutContent {",
        "  static func shortSections(for language: AppLanguage) -> [AboutShortSectionData] {",
        "    switch language {",
    ]
    for lang_key, app_lang in [("ru", ".ru"), ("en", ".en"), ("zh", ".zh"), ("kz", ".kz")]:
        lines.append(f"    case {app_lang}:")
        lines.extend(emit_short_section_swift(short[lang_key], "      "))
        lines.append("    ")
    lines.append("    }")
    lines.append("  }")
    lines.append("")
    lines.append("  static func presentationSlides(for language: AppLanguage) -> [AboutSlideData] {")
    lines.append("    switch language {")
    for lang_key, app_lang in [("ru", ".ru"), ("en", ".en"), ("zh", ".zh"), ("kz", ".kz")]:
        items = slides[lang_key]
        lines.append(f"    case {app_lang}:")
        lines.append("      return [")
        for title, body in items:
            lines.append(
                f'        AboutSlideData(title: "{swift_escape(title)}", body: "{swift_escape(body)}"),'
            )
        lines.append("      ]")
    lines.append("    }")
    lines.append("  }")
    lines.append("}")
    lines.append("")

    out_dir = ROOT / "ios/Voxera/About"
    out_dir.mkdir(parents=True, exist_ok=True)
    out_path = out_dir / "AboutContent.swift"
    out_path.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print("Wrote", out_path)


if __name__ == "__main__":
    main()
