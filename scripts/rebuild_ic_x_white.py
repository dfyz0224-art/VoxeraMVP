"""Whiten ic_x_glow -> ic_x_white: same glow, no blue, transparent bg, soft edges."""
from pathlib import Path
from PIL import Image, ImageFilter

ROOT = Path(__file__).resolve().parents[1]
DRAW = ROOT / "app" / "src" / "main" / "res" / "drawable"
SRC = DRAW / "ic_x_glow.png"
OUT = DRAW / "ic_x_white.png"


def whiten(src: Image.Image) -> Image.Image:
    src = src.convert("RGBA")
    w, h = src.size
    px = src.load()
    out = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    opx = out.load()
    for y in range(h):
        for x in range(w):
            r, g, b, _a = px[x, y]
            v = max(r, g, b)
            if v <= 2:
                continue
            opx[x, y] = (255, 255, 255, v)
    r, g, b, a = out.split()
    a = a.filter(ImageFilter.GaussianBlur(radius=1.2))
    return Image.merge("RGBA", (r, g, b, a))


def update_play_icon(white: Image.Image) -> None:
    store_dir = ROOT / "store-assets"
    if not store_dir.exists():
        return
    store = store_dir / "play_icon_512.png"
    bg = Image.new("RGBA", (512, 512), (0, 31, 92, 255))
    pad = int(512 * 0.12)
    inner = 512 - 2 * pad
    layer = white.resize((inner, inner), Image.Resampling.LANCZOS)
    bg.alpha_composite(layer, (pad, pad))
    bg.convert("RGB").save(store, "PNG")
    print("updated", store)


def main() -> None:
    src = Image.open(SRC)
    print("source", SRC.name, src.size)
    white = whiten(src)
    white.save(OUT, "PNG")
    print("wrote", OUT, white.size, OUT.stat().st_size)
    update_play_icon(white)


if __name__ == "__main__":
    main()
