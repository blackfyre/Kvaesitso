#!/usr/bin/env python3
"""Swap Material 3 controls in app/ui for the fork's Blackfyre keycaps (blackfyre-components).

The fork's composables in de.mm20.launcher2.ui.component.blackfyre have the same names and
signatures as the Material 3 ones, so a call site switches by its import alone. This script
rewrites those imports, and adds the fork import to files that import `androidx.compose.material3.*`
and call a swapped control. It is idempotent; run it after every upstream merge
(`mise run controls`). `--revert` restores the Material imports.

BlackfyreControlImportsTest (app/ui/src/test) fails while a call site is unswapped and keeps this
list in step: keep SWAPPED here and there identical.
"""
import argparse
import pathlib
import re
import sys

SWAPPED = [
    "AlertDialog",
    "AssistChip",
    "Button",
    "FilledIconButton",
    "FilledTonalButton",
    "FilledTonalIconButton",
    "FilterChip",
    "FloatingActionButton",
    "InputChip",
    "OutlinedButton",
    "SmallFloatingActionButton",
    "ToggleButton",
]
M3 = "androidx.compose.material3"
FORK = "de.mm20.launcher2.ui.component.blackfyre"
ROOT = pathlib.Path(__file__).resolve().parent.parent / "app/ui/src/main/java"
FORK_DIR = ROOT / FORK.replace(".", "/")


def calls(source: str, name: str) -> bool:
    return re.search(r"(?<![\w.])" + name + r"\s*[({]", source) is not None


def swap(source: str) -> str:
    for name in SWAPPED:
        source = re.sub(rf"^import {re.escape(M3)}\.{name}$", f"import {FORK}.{name}", source, flags=re.M)
    if re.search(rf"^import {re.escape(M3)}\.\*$", source, flags=re.M):
        missing = [n for n in SWAPPED if calls(source, n) and not re.search(rf"^import {re.escape(FORK)}\.{n}$", source, flags=re.M)]
        if missing:
            lines = "".join(f"import {FORK}.{n}\n" for n in missing)
            source = re.sub(rf"^(import {re.escape(M3)}\.\*\n)", r"\1" + lines, source, count=1, flags=re.M)
    return source


def revert(source: str) -> str:
    star = re.search(rf"^import {re.escape(M3)}\.\*$", source, flags=re.M) is not None
    for name in SWAPPED:
        replacement = "" if star else f"import {M3}.{name}\n"
        source = re.sub(rf"^import {re.escape(FORK)}\.{name}\n", replacement, source, flags=re.M)
    return source


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--revert", action="store_true", help="restore the Material 3 imports")
    args = parser.parse_args()
    changed = []
    for path in sorted(ROOT.rglob("*.kt")):
        if FORK_DIR in path.parents:
            continue
        text = path.read_text(encoding="utf-8")
        new = revert(text) if args.revert else swap(text)
        if new != text:
            path.write_text(new, encoding="utf-8")
            changed.append(path.relative_to(ROOT.parents[4]))
    for path in changed:
        print(path)
    print(f"{len(changed)} file(s) changed", file=sys.stderr)
    return 0


if __name__ == "__main__":
    sys.exit(main())
