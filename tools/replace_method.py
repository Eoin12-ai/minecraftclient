#!/usr/bin/env python3
"""
Replace exactly one Java method, by name, without slicing the file.

Twice now an edit has replaced "everything between marker A and marker B" and
silently taken neighbouring methods with it -- renderNvg once, fit() again --
each time costing a failed build. Slicing between two markers is only safe when
you already know what lives between them, and the whole reason for reaching for
it is that you do not.

This finds the method by its declaration, walks braces to its real end, and
swaps only that. It refuses when the name is ambiguous or absent, so a rename
or a typo fails loudly instead of deleting something.

Usage: replace_method.py <file> <method-name> <new-body-file>
"""
import pathlib
import re
import sys


def close_paren(src: str, open_at: int) -> int:
    """Index just past the ) that matches the ( at open_at."""
    depth, i = 0, open_at
    while i < len(src):
        if src[i] == "(":
            depth += 1
        elif src[i] == ")":
            depth -= 1
            if depth == 0:
                return i + 1
        i += 1
    raise SystemExit("replace_method: unbalanced parentheses")


def is_declaration(src: str, match: "re.Match") -> bool:
    """
    True for `void glass(...) {`, false for the call `glass(ctx, x0, ...);`.

    The name-plus-paren pattern cannot tell those apart -- an indented call
    reads as `<something> <name>(` just as a declaration does, which is why
    asking for `glass` matched six places and the tool refused. What separates
    them is on the other side: a declaration's parameter list is followed by a
    body, a call's is followed by a semicolon or an operator.
    """
    after = src[close_paren(src, src.index("(", match.end() - 1)):]
    after = re.sub(r"^\s*(?:throws\s+[\w.,\s]+)?", "", after)
    return after.startswith("{")


def span(src: str, name: str) -> tuple[int, int]:
    decls = [m for m in re.finditer(
        r"^[ \t]*(?:@\w+[^\n]*\n)*[ \t]*(?:public|private|protected|static|final|abstract|synchronized|\s)*"
        r"[\w<>\[\],.?\s]+\s" + re.escape(name) + r"\s*\(", src, re.M)]
    decls = [m for m in decls if is_declaration(src, m)]
    if len(decls) != 1:
        raise SystemExit(f"replace_method: {name!r} matched {len(decls)} declarations; refusing")

    start = decls[0].start()
    # the doc comment directly above belongs to the method
    head = src.rfind("/**", 0, start)
    if head != -1 and src.find("*/", head) < start and not src[src.find("*/", head) + 2:start].strip():
        start = head
        while start > 0 and src[start - 1] in " \t":
            start -= 1

    brace = src.index("{", decls[0].end() - 1)
    depth, i = 0, brace
    while i < len(src):
        if src[i] == "{":
            depth += 1
        elif src[i] == "}":
            depth -= 1
            if depth == 0:
                return start, i + 1
        i += 1

    raise SystemExit(f"replace_method: unbalanced braces in {name!r}")


def main() -> int:
    if len(sys.argv) != 4:
        raise SystemExit(__doc__)

    path = pathlib.Path(sys.argv[1])
    src = path.read_text()
    start, end = span(src, sys.argv[2])
    body = pathlib.Path(sys.argv[3]).read_text().rstrip("\n")
    path.write_text(src[:start] + body + src[end:])
    print(f"replace_method: {sys.argv[2]} ({end - start} chars -> {len(body)})")
    return 0


if __name__ == "__main__":
    sys.exit(main())
