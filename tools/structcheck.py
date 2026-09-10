#!/usr/bin/env python3
"""
Compile the client without Minecraft on the classpath and report only the
errors that are real.

Minecraft is not available locally -- CI does the remapping -- so a bare javac
produces thousands of "cannot find symbol" errors for every Minecraft type. The
obvious filter is to drop that whole category, and that filter is what let a
broken build reach CI twice: deleting a module leaves dangling references to
its FIELD, and a dangling field reference is also "cannot find symbol".

javac says which is which. Each error carries a `symbol:` and a `location:`
line, and a reference into our own code names a dev.kryptic type as its
location. So the rule is not "drop cannot-find-symbol", it is "drop
cannot-find-symbol whose location is not ours".

Usage: python3 tools/structcheck.py [outdir]
Exit code is 1 when anything real is found, so it can gate a commit.
"""
import pathlib
import re
import subprocess
import sys
import tempfile

OURS = "dev.kryptic"


def main() -> int:
    out = sys.argv[1] if len(sys.argv) > 1 else tempfile.mkdtemp()
    sources = [str(p) for p in pathlib.Path("src/main/java").rglob("*.java")]
    with tempfile.NamedTemporaryFile("w", suffix=".args", delete=False) as f:
        f.write("\n".join(sources))
        argfile = f.name

    proc = subprocess.run(
        ["javac", "-Xmaxerrs", "100000", "-nowarn", "-proc:none", "-d", out, f"@{argfile}"],
        capture_output=True, text=True)
    pathlib.Path(argfile).unlink(missing_ok=True)

    lines = proc.stderr.splitlines()
    real = []
    for i, line in enumerate(lines):
        m = re.match(r"^(src/.*?):(\d+): error: (.*)$", line)
        if not m:
            continue
        path, lineno, message = m.groups()

        if "cannot find symbol" in message:
            # look ahead for the symbol/location pair javac prints under it
            window = "\n".join(lines[i:i + 6])
            loc = re.search(r"^\s*location:\s*(.*)$", window, re.M)
            sym = re.search(r"^\s*symbol:\s*(.*)$", window, re.M)
            located = loc.group(1) if loc else ""
            # A missing Minecraft type is expected here; a missing member of one
            # of our own types is a genuine dangling reference.
            if OURS not in located:
                continue
            message = f"cannot find symbol -- {sym.group(1) if sym else '?'} in {located}"
        elif "package" in message and "does not exist" in message:
            continue
        elif "cannot access" in message:
            continue

        real.append(f"{path}:{lineno}: {message}")

    if real:
        print(f"{len(real)} real error(s):")
        for r in real:
            print("  " + r)
        return 1

    print("structcheck: clean")
    return 0


if __name__ == "__main__":
    sys.exit(main())
