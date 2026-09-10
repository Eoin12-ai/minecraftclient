#!/usr/bin/env python3
"""
Check that every module referenced through ModuleManager still exists.

Deleting a module leaves dangling references to its FIELD -- and those are
invisible to a local javac, because Minecraft is not on the classpath here:
the import errors stop compilation before javac ever resolves the field. CI
sees the real error because CI has the mappings. That gap let a broken build
reach CI twice in a row.

So this does not compile anything. It reads the public fields off
ModuleManager, finds every variable declared as a ModuleManager anywhere in
the source, and checks each `<var>.<member>` against what the class actually
declares. It is narrow on purpose: it catches exactly the mistake that has
actually shipped, and it cannot be fooled by a missing classpath.

Usage: python3 tools/modulerefs.py
"""
import pathlib
import re
import sys

MANAGER = pathlib.Path("src/main/java/dev/kryptic/module/ModuleManager.java")


def main() -> int:
    src = MANAGER.read_text()
    # some fields are `public final X y;` -- the modifier is optional
    fields = set(re.findall(
        r"^\s*public\s+(?:final\s+)?\w+(?:<[^>]*>)?\s+(\w+)\s*;", src, re.M))
    methods = set(re.findall(r"^\s*public\s+[\w<>\[\], ?.]+\s+(\w+)\s*\(", src, re.M))
    known = fields | methods
    if not fields:
        print("modulerefs: found no fields on ModuleManager -- refusing to pass vacuously")
        return 1

    bad = []
    for path in pathlib.Path("src/main/java").rglob("*.java"):
        if path == MANAGER:
            continue
        text = path.read_text()
        holders = set(re.findall(r"\bModuleManager\s+(\w+)\s*[=;),]", text))
        if not holders:
            continue

        # A name can be reused for something else in another scope in the same
        # file -- ConfigManager has a JsonObject also called `modules`. Only
        # trust a holder whose name is not re-declared as some other type.
        holders = {h for h in holders
                   if not re.search(rf"\b(?!ModuleManager\b)[A-Z]\w*\s+{re.escape(h)}\s*[=;),]", text)}
        for line_no, line in enumerate(text.splitlines(), 1):
            for holder in holders:
                for member in re.findall(rf"\b{re.escape(holder)}\.(\w+)", line):
                    if member not in known:
                        bad.append(f"{path}:{line_no}: {holder}.{member} "
                                   f"is not declared on ModuleManager")

    if bad:
        print(f"modulerefs: {len(bad)} dangling reference(s):")
        for b in sorted(set(bad)):
            print("  " + b)
        return 1

    print(f"modulerefs: clean ({len(fields)} module fields, {len(methods)} methods)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
