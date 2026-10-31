#!/usr/bin/env python3
# /// script
# requires-python = ">=3.10"
# dependencies = ["fonttools==4.68.0"]
# ///
"""Build the Blackfyre fonts bundled with the fork (OpenSpec change blackfyre-theme).

Downloads Archivo and JetBrains Mono from google/fonts at a pinned commit, verifies
their SHA-256 sums, and writes to app/ui/src/main/res/font/:

- archivo.ttf: Archivo instanced at wdth=100 (variable wght axis kept);
- jetbrains_mono.ttf: JetBrains Mono, unchanged;
- bf_mono_caps.ttf: "BF Mono Caps", JetBrains Mono with every lowercase code point
  mapped to its uppercase glyph, so labels display in capitals while the text keeps
  its case.

The OFL texts are copied to app/ui/src/main/assets/licenses/. Output is
deterministic: running the task twice produces byte-identical files.

Run through mise: `mise run fonts`.
"""

from __future__ import annotations

import argparse
import hashlib
import io
import sys
import urllib.request
from pathlib import Path

from fontTools.ttLib import TTFont
from fontTools.varLib import instancer

GOOGLE_FONTS_COMMIT = "6c1dbe27dcf7d817d4b62cec033ba77fa8e2a5ec"

SOURCES = {
    "archivo": (
        "ofl/archivo/Archivo%5Bwdth,wght%5D.ttf",
        "0e094a7d3c7c4c25cf1310c4b30014f1dae9332220b1c2c88f4fa996f0b05053",
    ),
    "archivo_ofl": (
        "ofl/archivo/OFL.txt",
        "108b4e57c9c796d3d38d0428ca7ee39de47ad93187302718d9b2d8864b9b716b",
    ),
    "jetbrains_mono": (
        "ofl/jetbrainsmono/JetBrainsMono%5Bwght%5D.ttf",
        "48715a42ec242c21e9f02692891e147d022299a52e48d5e413e1a942193ffeda",
    ),
    "jetbrains_mono_ofl": (
        "ofl/jetbrainsmono/OFL.txt",
        "b2fe5e8987594e9ffd1d2ca52a2f5d73eb8335243893c5d6254b5ad69269591d",
    ),
}

REPO_ROOT = Path(__file__).resolve().parent.parent
FONT_DIR = REPO_ROOT / "app/ui/src/main/res/font"
LICENSE_DIR = REPO_ROOT / "app/ui/src/main/assets/licenses"

CAPS_FAMILY = "BF Mono Caps"
CAPS_PS_FAMILY = "BFMonoCaps"
# Name IDs that identify the font; copyright, trademark, designer and licence
# records (0, 7-14) are left as they are.
RENAMED_NAME_IDS = {1, 3, 4, 6, 16, 17, 21, 25}


class BuildError(Exception):
    pass


def fetch(key: str, source_dir: Path | None) -> bytes:
    path, expected = SOURCES[key]
    if source_dir is not None:
        data = (source_dir / path.replace("%5B", "[").replace("%5D", "]")).read_bytes()
        origin = str(source_dir)
    else:
        url = f"https://raw.githubusercontent.com/google/fonts/{GOOGLE_FONTS_COMMIT}/{path}"
        with urllib.request.urlopen(url, timeout=60) as response:
            data = response.read()
        origin = url
    actual = hashlib.sha256(data).hexdigest()
    if actual != expected:
        raise BuildError(f"checksum mismatch for {origin}: expected {expected}, got {actual}")
    return data


def load(data: bytes) -> TTFont:
    # recalcTimestamp=False keeps head.modified from the source, for reproducible output.
    return TTFont(io.BytesIO(data), recalcTimestamp=False)


def save(font: TTFont, target: Path) -> bytes:
    buffer = io.BytesIO()
    font.save(buffer)
    data = buffer.getvalue()
    target.write_bytes(data)
    return data


def remap_lowercase_to_uppercase(font: TTFont) -> int:
    """Point every lowercase code point at the glyph of its single-character uppercase."""
    remapped = 0
    for table in font["cmap"].tables:
        if not table.isUnicode() or table.format == 14:
            continue
        mapping = table.cmap
        for code_point in list(mapping):
            char = chr(code_point)
            if not char.islower():
                continue
            upper = char.upper()
            if len(upper) != 1 or upper == char:
                continue
            upper_glyph = mapping.get(ord(upper))
            if upper_glyph is None:
                continue
            if mapping[code_point] != upper_glyph:
                mapping[code_point] = upper_glyph
                remapped += 1
    return remapped


def rename_family(font: TTFont, old_family: str, old_ps_family: str) -> None:
    for record in font["name"].names:
        if record.nameID not in RENAMED_NAME_IDS and record.nameID < 256:
            continue
        text = record.toUnicode()
        renamed = text.replace(old_family, CAPS_FAMILY).replace(old_ps_family, CAPS_PS_FAMILY)
        if renamed != text:
            record.string = renamed


def check(condition: bool, message: str) -> None:
    if not condition:
        raise BuildError(f"check failed: {message}")


def axes(font: TTFont) -> list[str]:
    return [axis.axisTag for axis in font["fvar"].axes] if "fvar" in font else []


def verify(archivo: TTFont, mono: TTFont, caps: TTFont) -> None:
    check(axes(archivo) == ["wght"], f"archivo axes are {axes(archivo)}, expected ['wght']")
    check(axes(mono) == ["wght"], f"jetbrains_mono axes are {axes(mono)}, expected ['wght']")
    check(axes(caps) == ["wght"], f"bf_mono_caps axes are {axes(caps)}, expected ['wght']")
    cmap = caps.getBestCmap()
    for lower, upper in (("a", "A"), ("z", "Z"), ("é", "É"), ("ő", "Ő"), ("ű", "Ű")):
        check(
            cmap.get(ord(lower)) is not None and cmap.get(ord(lower)) == cmap.get(ord(upper)),
            f"bf_mono_caps maps {lower!r} to {cmap.get(ord(lower))}, {upper!r} to {cmap.get(ord(upper))}",
        )
    check(mono.getBestCmap()[ord("a")] != mono.getBestCmap()[ord("A")], "jetbrains_mono was remapped")
    family = caps["name"].getDebugName(1)
    check(family == CAPS_FAMILY, f"bf_mono_caps family name is {family!r}")
    names = [record.toUnicode() for record in caps["name"].names if record.nameID in RENAMED_NAME_IDS]
    check(not any("JetBrains" in name for name in names), "bf_mono_caps still carries a JetBrains Mono name")


def build(source_dir: Path | None) -> None:
    archivo = load(fetch("archivo", source_dir))
    archivo_width = next(axis for axis in archivo["fvar"].axes if axis.axisTag == "wdth")
    check(
        archivo_width.minValue <= 100 <= archivo_width.maxValue,
        f"archivo wdth range {archivo_width.minValue}-{archivo_width.maxValue} excludes 100",
    )
    archivo = instancer.instantiateVariableFont(archivo, {"wdth": 100}, updateFontNames=False)

    mono_data = fetch("jetbrains_mono", source_dir)
    mono = load(mono_data)
    caps = load(mono_data)
    remapped = remap_lowercase_to_uppercase(caps)
    rename_family(caps, "JetBrains Mono", "JetBrainsMono")

    verify(archivo, mono, caps)

    FONT_DIR.mkdir(parents=True, exist_ok=True)
    LICENSE_DIR.mkdir(parents=True, exist_ok=True)
    outputs = {
        FONT_DIR / "archivo.ttf": save(archivo, FONT_DIR / "archivo.ttf"),
        FONT_DIR / "jetbrains_mono.ttf": save(mono, FONT_DIR / "jetbrains_mono.ttf"),
        FONT_DIR / "bf_mono_caps.ttf": save(caps, FONT_DIR / "bf_mono_caps.ttf"),
    }
    for key, name in (("archivo_ofl", "archivo-OFL.txt"), ("jetbrains_mono_ofl", "jetbrains_mono-OFL.txt")):
        target = LICENSE_DIR / name
        target.write_bytes(fetch(key, source_dir))
        outputs[target] = target.read_bytes()

    # The saved caps cut must keep its checks after a round trip through the file.
    verify(load(outputs[FONT_DIR / "archivo.ttf"]), mono, load(outputs[FONT_DIR / "bf_mono_caps.ttf"]))

    print(f"bf_mono_caps: {remapped} code points remapped to capitals")
    for target, data in outputs.items():
        print(f"{hashlib.sha256(data).hexdigest()}  {target.relative_to(REPO_ROOT)}")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    parser.add_argument(
        "--source-dir",
        type=Path,
        help="read the google/fonts files from a local checkout instead of downloading them "
        "(checksums are still verified)",
    )
    args = parser.parse_args()
    try:
        build(args.source_dir)
    except BuildError as error:
        print(f"error: {error}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
