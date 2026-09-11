#!/usr/bin/env python3
"""
Every Identifier must be lowercase, or Minecraft rejects it.

An Identifier's namespace allows [a-z0-9_.-] and its path allows those plus
'/'. Uppercase is not in either set. Nothing warns you: a resource whose id
does not parse is simply not there, and the thing that wanted it -- a font
provider, a texture, a sound -- silently has nothing.

That is what made every font in this client draw as the missing-glyph box.
All twelve providers pointed at files named Inter-Bold.ttf, MinecraftTen.ttf
and so on, so Identifier.CODEC rejected all twelve, every font was left with no
providers, and every character came out as a rectangle. It had never worked,
including for the fonts that were here before.

Checks both halves of the problem: the ids written in font provider JSON, and
the ids built in Java by Identifier.of(namespace, path).

Usage: identifiers.py
"""
import json
import pathlib
import re
import sys

NAMESPACE = re.compile(r"^[a-z0-9_.-]+$")
PATH = re.compile(r"^[a-z0-9_./-]+$")
JAVA_ID = re.compile(r"""Identifier\.of\(\s*"([^"]*)"\s*(?:,\s*"([^"]*)"\s*)?\)""")


def bad(ident: str) -> str | None:
    if ":" in ident:
        namespace, path = ident.split(":", 1)
    else:
        namespace, path = "minecraft", ident
    if not NAMESPACE.match(namespace):
        return f"namespace {namespace!r} is not [a-z0-9_.-]"
    if not PATH.match(path):
        return f"path {path!r} is not [a-z0-9_./-]"
    return None


def main() -> int:
    problems = []

    fonts = pathlib.Path("src/main/resources/assets")
    for path in fonts.rglob("font/*.json"):
        try:
            data = json.loads(path.read_text())
        except ValueError as error:
            problems.append(f"{path}: not valid JSON: {error}")
            continue

        for provider in data.get("providers", []):
            ref = provider.get("file")
            if not ref:
                continue

            why = bad(ref)
            if why:
                problems.append(f"{path}: file {ref!r} -- {why}")
                continue

            # and it has to actually be there, under the namespace's own folder
            namespace, rest = ref.split(":", 1) if ":" in ref else ("minecraft", ref)
            on_disk = fonts / namespace / "font" / rest
            if not on_disk.exists():
                problems.append(f"{path}: file {ref!r} -- no such file at {on_disk}")

    for source in pathlib.Path("src/main/java").rglob("*.java"):
        text = source.read_text(encoding="utf-8", errors="replace")
        for match in JAVA_ID.finditer(text):
            first, second = match.group(1), match.group(2)
            # a variable second argument is not a literal and cannot be checked
            ident = first if second is None else f"{first}:{second}"
            why = bad(ident)
            if why:
                line = text.count("\n", 0, match.start()) + 1
                problems.append(f"{source}:{line}: Identifier {ident!r} -- {why}")

    for problem in problems:
        print(problem)
    print("identifiers: %s" % ("clean" if not problems else f"{len(problems)} invalid"))
    return 1 if problems else 0


if __name__ == "__main__":
    sys.exit(main())
