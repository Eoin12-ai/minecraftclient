#!/usr/bin/env python3
"""
Find locals that shadow an enclosing local -- which Java rejects and javac
cannot tell us here, because Minecraft is not on the classpath.

A build was lost to exactly this: glass() declared `band` for the width of a
strip and then again as a for-loop counter inside it. Sibling scopes reusing a
name are fine and very common, so a naive "is this name declared twice" check
is all false positives. The rule that matters is Java's own: a local may not
share a name with one still in scope around it.

So this walks the braces keeping a stack of scopes, exactly as the compiler
does, and reports only a declaration whose name is already live on that stack.

Usage: javalocals.py <file.java> [...]
"""
import pathlib
import re
import sys

DECL = re.compile(
    r"\b(?:final\s+)?(?:int|long|float|double|boolean|char|byte|short|String|var)"
    r"(?:\s*\[\s*\])?\s+(\w+)\s*(?==|;|:)")
FOR = re.compile(r"\bfor\s*\(")
STRIP = (
    (re.compile(r"/\*.*?\*/", re.S), lambda m: " " * (m.end() - m.start())),
    (re.compile(r"//[^\n]*"), lambda m: " " * (m.end() - m.start())),
    (re.compile(r'"(?:[^"\\\n]|\\.)*"'), lambda m: '""'),
    (re.compile(r"'(?:[^'\\\n]|\\.)'"), lambda m: "''"),
)


def blanked(src: str) -> str:
    """Comments and literals gone, offsets and line numbers intact."""
    for pattern, repl in STRIP:
        src = pattern.sub(repl, src)
    return src


def check(path: pathlib.Path) -> list[str]:
    src = blanked(path.read_text())
    found = []

    # scopes[0] is the class body. A for-header's variables live in a scope of
    # their own that wraps the loop body, so the header scope and the body's
    # braces have to be treated as one -- getting that wrong is what made the
    # first version of this report every method after the first loop.
    scopes: list[dict[str, int]] = [{}]
    merge_next_brace = False

    def line_of(pos: int) -> int:
        return src.count("\n", 0, pos) + 1

    def declare(name: str, pos: int) -> None:
        for scope in scopes[:-1]:
            if name in scope:
                found.append("%s:%d: %r shadows the one at line %d"
                             % (path, line_of(pos), name, scope[name]))
                return
        scopes[-1][name] = line_of(pos)

    i = 0
    while i < len(src):
        ch = src[i]

        if ch == "{":
            if merge_next_brace:
                merge_next_brace = False          # the for-header scope is this one
            else:
                scopes.append({})
            i += 1
            continue

        if ch == "}":
            if len(scopes) > 1:
                scopes.pop()
            i += 1
            continue

        if FOR.match(src, i):
            depth, j = 0, src.index("(", i)
            header = j
            while j < len(src):
                if src[j] == "(":
                    depth += 1
                elif src[j] == ")":
                    depth -= 1
                    if depth == 0:
                        break
                j += 1

            scopes.append({})
            for m in DECL.finditer(src, header, j):
                declare(m.group(1), m.start())

            rest = src[j + 1:]
            if rest.lstrip().startswith("{"):
                merge_next_brace = True
                i = j + 1 + (len(rest) - len(rest.lstrip()))
                continue

            # a braceless body: the scope ends with the single statement
            end = src.find(";", j)
            if end == -1:
                end = len(src) - 1
            for m in DECL.finditer(src, j + 1, end):
                declare(m.group(1), m.start())
            scopes.pop()
            i = end + 1
            continue

        m = DECL.match(src, i)
        if m:
            declare(m.group(1), m.start())
            i = m.end()
            continue

        i += 1

    return found


def main() -> int:
    targets = [pathlib.Path(a) for a in sys.argv[1:]] or list(
        pathlib.Path("src/main/java").rglob("*.java"))
    problems = []
    for path in targets:
        problems += check(path)
    for line in problems:
        print(line)
    print("javalocals: %s (%d files)" % ("clean" if not problems else
                                         "%d shadowed" % len(problems), len(targets)))
    return 1 if problems else 0


if __name__ == "__main__":
    sys.exit(main())
