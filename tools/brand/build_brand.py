#!/usr/bin/env python3
"""Regenerate every Mew launcher / splash / wordmark asset from the mascot master.

Inputs (tools/brand/source):
  mew_mascot_master.png  transparent ghost-with-headphones mascot (concept A)
  mew_glyph_master.png   one-colour glyph of the same ghost, used for the themed icon
  mew_wordmark.path      "Mew" lettering traced from docs/mew-banner.png (see trace_wordmark.py)

Outputs: the default launcher icon, the five alternate icons offered in Settings > Appearance
(same ghost, tinted background), their picker previews, the iOS app icons, the Android 12
splash icon and the vector wordmark.

Run from the repo root:  python3 tools/brand/build_brand.py
Needs: pillow, numpy, scipy, potracer (pip install pillow numpy scipy potracer)
"""
from __future__ import annotations

import math
from pathlib import Path

import numpy as np
import scipy.ndimage as nd
from PIL import Image, ImageDraw, ImageFilter

ROOT = Path(__file__).resolve().parents[2]
SRC = Path(__file__).resolve().parent / "source"
RES = ROOT / "composeApp/src/androidMain/res"
COMPOSE_RES = ROOT / "composeApp/src/commonMain/composeResources/drawable"

DENSITIES = {"mdpi": 1.0, "hdpi": 1.5, "xhdpi": 2.0, "xxhdpi": 3.0, "xxxhdpi": 4.0}
CREAM = "#F5EBDD"

# Adaptive icon geometry (dp). The foreground layer is 108dp; launchers mask to 72dp and
# guarantee only the centred 66dp circle. The mascot silhouette is fitted inside that circle.
ADAPTIVE_DP = 108
SAFE_RADIUS_DP = 33.0
# Android 12 splash: icon-without-background canvas is 288dp, content must sit inside a
# 192dp circle (radius 96dp). Leave a little air so it doesn't feel cramped.
SPLASH_DP = 288
SPLASH_RADIUS_DP = 88.0
SPLASH_PX = 1080  # drawable-nodpi, rendered by the system scaled to 288dp


def load_mascot() -> Image.Image:
    """Clean the generated master: kill the faint halo and stray specks, crop to the body."""
    im = Image.open(SRC / "mew_mascot_master.png").convert("RGBA")
    a = np.asarray(im).astype(np.float32)
    alpha = a[..., 3]
    lab, n = nd.label(alpha > 8)
    if n:
        sizes = nd.sum(alpha > 8, lab, range(1, n + 1))
        main = int(np.argmax(sizes)) + 1
        alpha = np.where(lab == main, alpha, 0)
    # Remap so the anti-aliased edge stays but the near-invisible halo disappears.
    alpha = np.clip((alpha - 28.0) / (235.0 - 28.0), 0, 1) * 255.0
    a[..., 3] = alpha
    out = Image.fromarray(a.astype(np.uint8), "RGBA")
    return out.crop(out.getbbox())


def fit_scale(mascot: Image.Image, radius_px: float, y_bias: float) -> tuple[float, tuple[float, float]]:
    """Largest scale where every visible pixel lies inside a circle of radius_px.

    Returns (scale, centre offset of the bbox) with the silhouette optically centred
    (centroid of the opaque area, nudged down slightly by y_bias of the height).
    """
    alpha = np.asarray(mascot)[..., 3] > 40
    ys, xs = np.nonzero(alpha)
    cx, cy = xs.mean(), ys.mean()
    h = mascot.height
    cy += y_bias * h
    r = np.sqrt((xs - cx) ** 2 + (ys - cy) ** 2).max()
    return radius_px / r, (cx, cy)


def place(mascot: Image.Image, canvas_px: int, radius_px: float, y_bias: float = 0.0) -> Image.Image:
    scale, (cx, cy) = fit_scale(mascot, radius_px, y_bias)
    w, h = round(mascot.width * scale), round(mascot.height * scale)
    m = mascot.resize((w, h), Image.LANCZOS)
    out = Image.new("RGBA", (canvas_px, canvas_px), (0, 0, 0, 0))
    out.alpha_composite(m, (round(canvas_px / 2 - cx * scale), round(canvas_px / 2 - cy * scale)))
    return out


def load_glyph() -> Image.Image:
    """Themed-icon (Android 13+) source: the one-colour ghost glyph. Only alpha matters,
    the launcher tints it. Eyes, smile and headphone gaps are transparent cut-outs."""
    im = Image.open(SRC / "mew_glyph_master.png").convert("RGBA")
    alpha = np.asarray(im)[..., 3].astype(np.float32)
    lab, n = nd.label(alpha > 8)
    if n:
        sizes = nd.sum(alpha > 8, lab, range(1, n + 1))
        keep = [i + 1 for i, sz in enumerate(sizes) if sz > 0.002 * alpha.size]
        alpha = np.where(np.isin(lab, keep), alpha, 0)
    alpha = np.clip((alpha - 28.0) / (235.0 - 28.0), 0, 1) * 255.0
    out = Image.new("RGBA", im.size, (255, 255, 255, 0))
    out.putalpha(Image.fromarray(alpha.astype(np.uint8), "L"))
    return out.crop(out.getbbox())


# Radial background stops (centre, middle, edge) per icon. "original" is the default icon;
# the others are the alternate icons in AppIconOption and share the same ghost foreground.
Stops = list[tuple[float, tuple[int, int, int]]]
VARIANTS: dict[str, Stops] = {
    "original": [(0.0, (0x3A, 0x31, 0x29)), (0.5, (0x17, 0x14, 0x12)), (1.0, (0x0A, 0x09, 0x08))],
    "arctic_blue": [(0.0, (0x1E, 0x5C, 0xB8)), (0.5, (0x0B, 0x25, 0x52)), (1.0, (0x04, 0x0C, 0x1E))],
    "emerald": [(0.0, (0x13, 0x8A, 0x5A)), (0.5, (0x07, 0x3A, 0x27)), (1.0, (0x02, 0x14, 0x0D))],
    "rose_gold": [(0.0, (0xB8, 0x55, 0x75)), (0.5, (0x55, 0x22, 0x35)), (1.0, (0x1E, 0x0A, 0x12))],
    "copper": [(0.0, (0xB8, 0x62, 0x2E)), (0.5, (0x55, 0x2A, 0x12)), (1.0, (0x1E, 0x0D, 0x05))],
    "graphite": [(0.0, (0x6E, 0x76, 0x82)), (0.5, (0x2E, 0x33, 0x3A)), (1.0, (0x10, 0x12, 0x15))],
}
# iOS asset catalog set per variant (AppIconOption.platformName; "original" is the primary icon).
IOS_SETS = {
    "original": "AppIcon",
    "arctic_blue": "AppIconArcticBlue",
    "emerald": "AppIconEmerald",
    "rose_gold": "AppIconRoseGold",
    "copper": "AppIconCopper",
    "graphite": "AppIconGraphite",
}
# Retired Nuvio Enhanced alternate icons that iOS still registers in Info.plist. They show the
# default Mew icon so nobody who picked one keeps a Nuvio icon.
IOS_LEGACY_SETS = ["IconAurora", "IconChrome", "IconEmerald", "IconEnhanced", "IconGear", "IconMonochrome", "IconNeon"]
IOS_ASSETS = ROOT / "iosApp/iosApp/Assets.xcassets"


def background_name(variant: str) -> str:
    return "ic_mew_launcher_background" if variant == "original" else f"ic_mew_launcher_background_{variant}"


def launcher_name(variant: str) -> str:
    return "ic_launcher" if variant == "original" else f"ic_launcher_{variant}"


def background(px: int, stops: Stops = VARIANTS["original"]) -> Image.Image:
    """Raster twin of the vector background, for legacy (<26) icons, previews and iOS."""
    s = px / ADAPTIVE_DP
    yy, xx = np.mgrid[0:px, 0:px].astype(np.float32) / s
    d = np.sqrt((xx - 54) ** 2 + (yy - 40) ** 2) / 78.0
    out = np.zeros((px, px, 3), np.float32)
    d = np.clip(d, 0, 1)
    for (t0, c0), (t1, c1) in zip(stops, stops[1:]):
        m = (d >= t0) & (d <= t1)
        f = ((d - t0) / (t1 - t0))[m][:, None]
        out[m] = np.array(c0) * (1 - f) + np.array(c1) * f
    return Image.fromarray(out.astype(np.uint8), "RGB").convert("RGBA")


def background_xml(stops: Stops) -> str:
    items = "\n".join(
        f'                <item android:offset="{t:g}" android:color="#FF{r:02X}{g:02X}{b:02X}" />'
        for t, (r, g, b) in stops
    )
    return BACKGROUND_XML.replace("{items}", items)


BACKGROUND_XML = """<?xml version="1.0" encoding="utf-8"?>
<!-- Generated by tools/brand/build_brand.py. Edit the script, not this file. -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:aapt="http://schemas.android.com/aapt"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path android:pathData="M0,0h108v108h-108z">
        <aapt:attr name="android:fillColor">
            <gradient
                android:type="radial"
                android:centerX="54"
                android:centerY="40"
                android:gradientRadius="78">
{items}
            </gradient>
        </aapt:attr>
    </path>
</vector>
"""

ADAPTIVE_XML = """<?xml version="1.0" encoding="utf-8"?>
<!-- Generated by tools/brand/build_brand.py. -->
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/{background}" />
    <foreground android:drawable="@mipmap/ic_mew_launcher_foreground" />
    <monochrome android:drawable="@mipmap/ic_mew_launcher_monochrome" />
</adaptive-icon>
"""


def full_bleed(mascot: Image.Image, px: int, stops: Stops) -> Image.Image:
    """The visible 72dp of the adaptive icon as an opaque square (previews, iOS, legacy)."""
    full = px * ADAPTIVE_DP // 72  # draw the 108dp layers, keep the visible 72dp
    bg = background(full, stops)
    bg.alpha_composite(place(mascot, full, SAFE_RADIUS_DP * full / ADAPTIVE_DP, 0.02))
    off = (full - px) // 2
    return bg.crop((off, off, off + px, off + px))


def legacy_icon(mascot: Image.Image, px: int, round_mask: bool, stops: Stops = VARIANTS["original"]) -> Image.Image:
    """Pre-Oreo launcher icon (48dp): background + foreground, masked."""
    ss = px * 4
    tile = full_bleed(mascot, ss, stops)
    mask = Image.new("L", (ss, ss), 0)
    d = ImageDraw.Draw(mask)
    inset = round(ss * 0.04)
    if round_mask:
        d.ellipse((inset, inset, ss - inset, ss - inset), fill=255)
    else:
        d.rounded_rectangle((inset, inset, ss - inset, ss - inset), radius=round(ss * 0.22), fill=255)
    out = Image.new("RGBA", (ss, ss), (0, 0, 0, 0))
    out.paste(tile, (0, 0), mask)
    return out.resize((px, px), Image.LANCZOS)


NOTIFICATION_XML = """<?xml version="1.0" encoding="utf-8"?>
<!-- Generated by tools/brand/build_brand.py from mew_glyph_master.png. Edit the script, not this file. -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="@android:color/white"
        android:fillType="evenOdd"
        android:pathData="{path}" />
</vector>
"""


def notification_vector(glyph: Image.Image) -> str:
    """Status-bar icon: the glyph silhouette traced to a 24dp vector, content inside 22dp."""
    import potrace  # potracer

    work = 480
    canvas = Image.new("L", (work, work), 0)
    g = glyph.split()[3]
    scale = (work * 22 / 24) / max(g.size)
    g = g.resize((round(g.width * scale), round(g.height * scale)), Image.LANCZOS)
    canvas.paste(g, ((work - g.width) // 2, (work - g.height) // 2))
    bitmap = potrace.Bitmap(np.asarray(canvas) <= 127)  # potracer fills False pixels
    unit = 24 / work
    fmt = lambda p: f"{p.x * unit:.2f},{p.y * unit:.2f}"
    parts = []
    for curve in bitmap.trace(turdsize=40, alphamax=1.0, opticurve=True, opttolerance=0.2):
        parts.append("M" + fmt(curve.start_point))
        for seg in curve.segments:
            if seg.is_corner:
                parts.append("L" + fmt(seg.c) + " L" + fmt(seg.end_point))
            else:
                parts.append("C" + fmt(seg.c1) + " " + fmt(seg.c2) + " " + fmt(seg.end_point))
        parts.append("Z")
    return NOTIFICATION_XML.replace("{path}", " ".join(parts))


def wordmark_svg() -> str:
    w, h, *rest = (SRC / "mew_wordmark.path").read_text().split(maxsplit=2)
    d = rest[0].strip()
    return (
        f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {w} {h}">'
        f'<path fill-rule="evenodd" fill="{CREAM}" d="{d}"/></svg>'
    )


def main() -> None:
    mascot = load_mascot()
    glyph = load_glyph()

    # Adaptive launcher layers.
    for name, s in DENSITIES.items():
        px = round(ADAPTIVE_DP * s)
        fg = place(mascot, px, SAFE_RADIUS_DP * s, 0.02)
        fg.save(RES / f"mipmap-{name}/ic_mew_launcher_foreground.webp", lossless=True)
        place(glyph, px, SAFE_RADIUS_DP * 0.92 * s, 0.02).save(RES / f"mipmap-{name}/ic_mew_launcher_monochrome.webp", lossless=True)
        lpx = round(48 * s)
        for variant, stops in VARIANTS.items():
            legacy_icon(mascot, lpx, False, stops).save(RES / f"mipmap-{name}/{launcher_name(variant)}.webp", lossless=True)
        legacy_icon(mascot, lpx, True).save(RES / f"mipmap-{name}/ic_launcher_round.webp", lossless=True)
    for variant, stops in VARIANTS.items():
        (RES / f"drawable/{background_name(variant)}.xml").write_text(background_xml(stops))
        adaptive = ADAPTIVE_XML.replace("{background}", background_name(variant))
        (RES / "mipmap-anydpi-v26" / f"{launcher_name(variant)}.xml").write_text(adaptive)
    (RES / "mipmap-anydpi-v26/ic_launcher_round.xml").write_text(
        ADAPTIVE_XML.replace("{background}", background_name("original"))
    )

    # Icon picker previews (Settings > Appearance > App icon); the picker clips the corners.
    for variant, stops in VARIANTS.items():
        preview = full_bleed(mascot, 1024, stops).resize((256, 256), Image.LANCZOS)
        preview.convert("RGB").save(COMPOSE_RES / f"app_icon_{variant}.png", optimize=True)

    # iOS: 1024px opaque squares; iOS applies its own corner mask.
    for variant, stops in VARIANTS.items():
        icon = full_bleed(mascot, 1024, stops).convert("RGB")
        icon.save(IOS_ASSETS / f"{IOS_SETS[variant]}.appiconset/app-icon-1024.png", optimize=True)
        if variant == "original":
            for legacy in IOS_LEGACY_SETS:
                folder = IOS_ASSETS / f"{legacy}.appiconset"
                for png in folder.glob("*.png"):
                    icon.save(png, optimize=True)

    # Android 12+ system splash icon (luq parity: one nodpi bitmap on the 288dp canvas). After it,
    # the app plays luq's intro (AppIntroContent.kt: wordmark spring-in with drifting glow).
    splash = place(mascot, SPLASH_PX, SPLASH_RADIUS_DP * SPLASH_PX / SPLASH_DP, 0.0)
    (RES / "drawable-nodpi").mkdir(exist_ok=True)
    splash.save(RES / "drawable-nodpi/mew_splash_icon.webp", lossless=True)

    (RES / "drawable/ic_notification_small.xml").write_text(notification_vector(glyph))
    (COMPOSE_RES / "mew_brand_logo.svg").write_text(wordmark_svg())
    print("Mew brand assets regenerated.")


if __name__ == "__main__":
    main()
