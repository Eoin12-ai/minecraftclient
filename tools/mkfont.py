#!/usr/bin/env python3
"""
Draw the Kryptic typeface and emit a TrueType file.

Every glyph here is a skeleton: a handful of polylines through the middle of
each stroke, at a single weight. That is the whole design decision. A drawn
typeface varies its weight along a curve and tapers its terminals, and doing
that from code means fighting the code rather than drawing; a monoline
geometric face is a thing you *can* describe as coordinates, so it is what
this describes.

How a skeleton becomes an outline: each segment is emitted as its own
rectangle and each interior corner as its own small polygon, all wound the
same way. TrueType fills by the non-zero rule, so overlapping same-wound
contours union without anyone computing a boolean. That is why there are no
curve operators and no path arithmetic below -- the rasteriser does the
merging.

Curves are chamfered rather than round. At 7-10px a curve is two or three
pixels of grey; a 45-degree cut lands on the pixel grid and stays crisp, and
it reads as deliberate rather than as a low-resolution circle.
"""

import math
import sys
from fontTools.fontBuilder import FontBuilder
from fontTools.pens.ttGlyphPen import TTGlyphPen

UPM = 1000
CAP = 700          # height of A
XH = 500           # height of x
ASC = 720          # height of b, d, h, k, l
DESC = -200        # depth of g, p, q, y
WEIGHT = 100       # stroke thickness
GAP = 46           # space either side of the ink
JOIN_SIDES = 12    # polygon used to fill a corner
ASCENT = 790       # clears the tallest ink; see setupHorizontalHeader
DESCENT = -260

# ---------------------------------------------------------------- the shapes
#
# Each entry is (strokes, dots). A stroke is a list of points; a trailing
# True means close it back to the start. A dot is (x, y, radius) and is the
# only way to draw something with no length -- a full stop, a tittle.

G = {}


def g(char, strokes=(), dots=(), advance=None, light=(), fills=()):
    """
    light: strokes drawn at a reduced weight. Only for shapes whose counters are
    too small to survive the full stroke -- the rings of a percent sign enclose
    about a fifth of the area an O does, so at one weight they fill in solid.
    """
    G[char] = (list(strokes), list(dots), advance, list(light), list(fills))


g(" ", advance=270)

# -- capitals ----------------------------------------------------------------
g("A", [[(0, 0), (170, 700), (310, 700), (480, 0)], [(58, 240), (422, 240)]])
g("B", [[(0, 0), (0, 700)],
        [(0, 700), (280, 700), (400, 580), (400, 500), (280, 380), (0, 380)],
        [(0, 380), (310, 380), (440, 250), (440, 130), (310, 0), (0, 0)]])
g("C", [[(440, 560), (320, 700), (130, 700), (0, 570), (0, 130), (130, 0), (320, 0), (440, 140)]])
g("D", [[(0, 0), (0, 700)], [(0, 700), (310, 700), (460, 550), (460, 150), (310, 0), (0, 0)]])
g("E", [[(420, 700), (0, 700), (0, 0), (420, 0)], [(0, 360), (350, 360)]])
g("F", [[(420, 700), (0, 700), (0, 0)], [(0, 360), (350, 360)]])
g("G", [[(440, 570), (320, 700), (130, 700), (0, 570), (0, 130), (130, 0),
         (330, 0), (470, 150), (470, 400), (280, 400)]])
g("H", [[(0, 700), (0, 0)], [(460, 700), (460, 0)], [(0, 360), (460, 360)]])
g("I", [[(0, 700), (0, 0)]])
g("J", [[(360, 700), (360, 140), (240, 0), (110, 0), (0, 120), (0, 190)]])
g("K", [[(0, 700), (0, 0)], [(440, 700), (0, 330)], [(170, 470), (450, 0)]])
g("L", [[(0, 700), (0, 0), (420, 0)]])
g("M", [[(0, 0), (0, 700), (270, 300), (540, 700), (540, 0)]])
g("N", [[(0, 0), (0, 700), (460, 0), (460, 700)]])
g("O", [[(0, 150), (150, 0), (320, 0), (470, 150), (470, 550), (320, 700), (150, 700), (0, 550), True]])
g("P", [[(0, 0), (0, 700)], [(0, 700), (300, 700), (440, 570), (440, 450), (300, 320), (0, 320)]])
g("Q", [[(0, 150), (150, 0), (320, 0), (470, 150), (470, 550), (320, 700), (150, 700), (0, 550), True],
        [(300, 190), (490, -40)]])
g("R", [[(0, 0), (0, 700)],
        [(0, 700), (300, 700), (430, 580), (430, 450), (300, 330), (0, 330)],
        [(240, 330), (450, 0)]])
g("S", [[(430, 570), (310, 700), (130, 700), (0, 590), (0, 480), (120, 370),
         (320, 370), (440, 250), (440, 130), (310, 0), (120, 0), (10, 120)]])
g("T", [[(0, 700), (440, 700)], [(220, 700), (220, 0)]])
g("U", [[(0, 700), (0, 150), (140, 0), (320, 0), (460, 150), (460, 700)]])
g("V", [[(0, 700), (200, 0), (280, 0), (480, 700)]])
g("W", [[(0, 700), (130, 0), (210, 0), (340, 470), (470, 0), (550, 0), (680, 700)]])
g("X", [[(0, 700), (460, 0)], [(0, 0), (460, 700)]])
g("Y", [[(0, 700), (230, 360), (460, 700)], [(230, 360), (230, 0)]])
g("Z", [[(0, 700), (440, 700), (0, 0), (440, 0)]])

# -- lower case --------------------------------------------------------------
g("a", [[(400, 380), (280, 500), (120, 500), (0, 380), (0, 120), (120, 0), (280, 0), (400, 120)],
        [(400, 500), (400, 0)]])
g("b", [[(0, 720), (0, 0)],
        [(0, 380), (120, 500), (280, 500), (400, 380), (400, 120), (280, 0), (120, 0), (0, 120)]])
g("c", [[(400, 380), (280, 500), (120, 500), (0, 380), (0, 120), (120, 0), (280, 0), (400, 120)]])
g("d", [[(400, 720), (400, 0)],
        [(400, 380), (280, 500), (120, 500), (0, 380), (0, 120), (120, 0), (280, 0), (400, 120)]])
g("e", [[(0, 250), (400, 250), (400, 380), (280, 500), (120, 500), (0, 380),
         (0, 120), (120, 0), (290, 0), (400, 110)]])
g("f", [[(300, 650), (230, 720), (140, 720), (60, 640), (60, 0)], [(0, 500), (260, 500)]])
g("g", [[(400, 380), (280, 500), (120, 500), (0, 380), (0, 120), (120, 0), (280, 0), (400, 120)],
        [(400, 500), (400, -70), (300, -200), (140, -200), (30, -120)]])
g("h", [[(0, 720), (0, 0)], [(0, 380), (120, 500), (280, 500), (400, 380), (400, 0)]])
g("i", [[(0, 500), (0, 0)]], [(0, 650, 55)])
g("j", [[(180, 500), (180, -70), (80, -200), (0, -160)]], [(180, 650, 55)])
g("k", [[(0, 720), (0, 0)], [(380, 500), (30, 220)], [(150, 320), (400, 0)]])
g("l", [[(0, 720), (0, 90), (80, 0), (160, 0)]])
g("m", [[(0, 500), (0, 0)],
        [(0, 380), (110, 500), (230, 500), (330, 380), (330, 0)],
        [(330, 380), (440, 500), (560, 500), (660, 380), (660, 0)]])
g("n", [[(0, 500), (0, 0)], [(0, 380), (120, 500), (280, 500), (400, 380), (400, 0)]])
g("o", [[(0, 380), (120, 500), (300, 500), (420, 380), (420, 120), (300, 0), (120, 0), (0, 120), True]])
g("p", [[(0, 500), (0, -200)],
        [(0, 380), (120, 500), (280, 500), (400, 380), (400, 120), (280, 0), (120, 0), (0, 120)]])
g("q", [[(400, 500), (400, -200)],
        [(400, 380), (280, 500), (120, 500), (0, 380), (0, 120), (120, 0), (280, 0), (400, 120)]])
g("r", [[(0, 500), (0, 0)], [(0, 380), (120, 500), (280, 500), (330, 450)]])
g("s", [[(370, 400), (280, 500), (110, 500), (0, 400), (0, 330), (110, 250),
         (270, 250), (380, 170), (380, 100), (270, 0), (100, 0), (10, 90)]])
g("t", [[(80, 720), (80, 110), (180, 0), (280, 60)], [(0, 500), (230, 500)]])
g("u", [[(0, 500), (0, 120), (120, 0), (280, 0), (400, 120), (400, 500)], [(400, 120), (400, 0)]])
g("v", [[(0, 500), (170, 0), (250, 0), (420, 500)]])
g("w", [[(0, 500), (110, 0), (190, 0), (300, 330), (410, 0), (490, 0), (600, 500)]])
g("x", [[(0, 500), (400, 0)], [(0, 0), (400, 500)]])
g("y", [[(0, 500), (210, 40)], [(420, 500), (140, -200)]])
g("z", [[(0, 500), (380, 500), (0, 0), (380, 0)]])

# -- figures -----------------------------------------------------------------
#
# Zero is drawn narrower than O rather than slashed. A slash is two extra
# pixels of ink through the middle of the counter, which at HUD sizes closes
# it up; the width difference survives the downscale where the slash does not.
g("0", [[(0, 150), (140, 0), (280, 0), (420, 150), (420, 550), (280, 700), (140, 700), (0, 550), True]])
g("1", [[(170, 700), (170, 0)], [(20, 540), (170, 700)], [(20, 0), (320, 0)]])
g("2", [[(0, 550), (140, 700), (300, 700), (430, 560), (430, 450), (0, 0), (430, 0)]])
g("3", [[(0, 560), (140, 700), (300, 700), (430, 570), (430, 470), (320, 370),
         (430, 250), (430, 130), (300, 0), (140, 0), (0, 140)],
        [(180, 370), (330, 370)]])
g("4", [[(340, 700), (0, 220), (460, 220)], [(340, 700), (340, 0)]])
g("5", [[(410, 700), (50, 700), (20, 410), (290, 410), (430, 280), (430, 130), (300, 0), (140, 0), (0, 140)]])
g("6", [[(370, 640), (250, 700), (140, 700), (0, 560), (0, 140), (130, 0), (290, 0),
         (430, 140), (430, 250), (300, 390), (140, 390), (0, 250)]])
g("7", [[(0, 700), (420, 700), (150, 0)]])
g("8", [[(120, 700), (310, 700), (430, 590), (430, 480), (310, 370), (120, 370), (0, 480), (0, 590), True],
        [(120, 370), (310, 370), (430, 250), (430, 120), (310, 0), (120, 0), (0, 120), (0, 250), True]])
g("9", [[(60, 60), (180, 0), (290, 0), (430, 140), (430, 560), (300, 700), (140, 700),
         (0, 560), (0, 450), (130, 310), (290, 310), (430, 450)]])

# -- punctuation and symbols -------------------------------------------------
g(".", [], [(0, 55, 55)])
g(",", [[(40, 70), (-40, -150)]])
g(":", [], [(0, 55, 55), (0, 380, 55)])
g(";", [[(40, 70), (-40, -150)]], [(0, 380, 55)])
g("!", [[(0, 700), (0, 180)]], [(0, 55, 55)])
g("?", [[(0, 560), (130, 700), (240, 700), (360, 580), (360, 480), (180, 320), (180, 210)]],
       [(180, 55, 55)])
g("'", [[(0, 700), (0, 530)]])
g('"', [[(0, 700), (0, 530)], [(130, 700), (130, 530)]])
g("(", [[(180, 720), (40, 520), (40, 100), (180, -100)]])
g(")", [[(0, 720), (140, 520), (140, 100), (0, -100)]])
g("[", [[(180, 720), (30, 720), (30, -100), (180, -100)]])
g("]", [[(0, 720), (150, 720), (150, -100), (0, -100)]])
g("{", [[(220, 720), (120, 720), (120, 380), (20, 310), (120, 240), (120, -100), (220, -100)]])
g("}", [[(0, 720), (100, 720), (100, 380), (200, 310), (100, 240), (100, -100), (0, -100)]])
g("-", [[(0, 330), (280, 330)]])
g("_", [[(0, -150), (440, -150)]])
g("=", [[(0, 430), (340, 430)], [(0, 230), (340, 230)]])
g("+", [[(0, 330), (360, 330)], [(180, 150), (180, 510)]])
g("*", [[(150, 700), (150, 400)], [(20, 450), (280, 650)], [(20, 650), (280, 450)]])
g("/", [[(0, -60), (360, 720)]])
g("\\", [[(0, 720), (360, -60)]])
g("|", [[(0, -100), (0, 720)]])
g("<", [[(340, 560), (0, 290), (340, 20)]])
g(">", [[(0, 560), (340, 290), (0, 20)]])
g("#", [[(120, 0), (180, 700)], [(300, 0), (360, 700)],
        [(20, 220), (440, 220)], [(40, 470), (460, 470)]])
g("%", [[(20, 0), (540, 700)]],
   light=[[(0, 570), (70, 700), (190, 700), (260, 570), (190, 440), (70, 440), True],
          [(300, 260), (370, 390), (490, 390), (560, 260), (490, 130), (370, 130), True]])
g("&", [[(500, 0), (150, 390), (150, 580), (250, 700), (360, 700), (450, 605), (450, 520),
         (60, 195), (60, 100), (175, 0), (310, 0), (460, 165)]])
g("@", [[(440, 190), (310, 190), (240, 260), (240, 360), (310, 430), (440, 430), (440, 180),
         (490, 140), (580, 190), (610, 290), (610, 400), (490, 560), (300, 560), (120, 470),
         (60, 300), (130, 140), (300, 40), (470, 40)]])
g("$", [[(400, 570), (300, 660), (120, 660), (10, 570), (10, 470), (120, 380),
         (300, 380), (410, 290), (410, 170), (300, 80), (120, 80), (20, 170)],
        [(210, 760), (210, -20)]])
g("^", [[(0, 480), (170, 700), (340, 480)]])
g("~", [[(0, 300), (100, 400), (200, 300), (300, 200), (400, 300)]])
g("`", [[(0, 700), (110, 590)]])

# extras the menu and HUD actually use
g("°", [[(0, 600), (60, 700), (140, 700), (200, 600), (140, 500), (60, 500), True]])
g("·", [], [(0, 330, 55)])
g("•", [], [(0, 330, 90)])
g("—", [[(0, 330), (560, 330)]])
g("–", [[(0, 330), (380, 330)]])
g("×", [[(40, 120), (340, 460)], [(40, 460), (340, 120)]])
g("✓", [[(0, 300), (150, 80), (420, 620)]])
g("→", [[(0, 330), (520, 330)], [(350, 160), (520, 330), (350, 500)]])
g("←", [[(0, 330), (520, 330)], [(170, 160), (0, 330), (170, 500)]])
g("↑", [[(210, 0), (210, 700)], [(40, 530), (210, 700), (380, 530)]])
g("↓", [[(210, 700), (210, 0)], [(40, 170), (210, 0), (380, 170)]])

g("…", [], [(0, 55, 55), (190, 55, 55), (380, 55, 55)])
g("»", [[(0, 530), (150, 330), (0, 130)], [(190, 530), (340, 330), (190, 130)]])
g("«", [[(150, 530), (0, 330), (150, 130)], [(340, 530), (190, 330), (340, 130)]])
g("\u221e", light=[[(0, 325), (75, 460), (200, 460), (275, 325), (200, 190), (75, 190), True],
                   [(285, 325), (360, 460), (485, 460), (560, 325), (485, 190), (360, 190), True]])
g("\u2605", fills=[[(350, 690), (437, 460), (683, 448), (491, 294), (556, 57), (350, 192), (144, 57), (209, 294), (17, 448), (263, 460)]])


# ------------------------------------------------------------- the stroker

def _ngon(cx, cy, r, sides):
    """A polygon standing in for a circle, wound the same way as everything else."""
    return [(cx + r * math.cos(-2.0 * math.pi * i / sides),
             cy + r * math.sin(-2.0 * math.pi * i / sides)) for i in range(sides)]


def _stroke(points, closed, half):
    """One polyline to a set of clockwise contours: a slab per segment, a disc per corner."""
    contours = []
    pairs = list(zip(points, points[1:]))
    if closed:
        pairs.append((points[-1], points[0]))

    for (ax, ay), (bx, by) in pairs:
        dx, dy = bx - ax, by - ay
        length = math.hypot(dx, dy)
        if length < 1e-9:
            continue
        nx, ny = -dy / length * half, dx / length * half
        contours.append([(ax + nx, ay + ny), (bx + nx, by + ny),
                         (bx - nx, by - ny), (ax - nx, ay - ny)])

    # Corners only. The ends of an open stroke stay square -- a flat terminal
    # is part of the look, and a rounded one at this weight just reads blurry.
    corners = points if closed else points[1:-1]
    for cx, cy in corners:
        contours.append(_ngon(cx, cy, half, JOIN_SIDES))

    return contours


LIGHT = 0.60          # how much of the weight the light strokes carry


def contours_for(name):
    strokes, dots, _, light, fills = G[name]
    out = []

    for stroke, half in ([(s, WEIGHT / 2.0) for s in strokes]
                         + [(s, WEIGHT * LIGHT / 2.0) for s in light]):
        closed = len(stroke) > 1 and stroke[-1] is True
        points = [p for p in stroke if p is not True]
        if len(points) == 1:
            out.append(_ngon(points[0][0], points[0][1], half, JOIN_SIDES))
        else:
            out.extend(_stroke(points, closed, half))

    for dx, dy, r in dots:
        out.append(_ngon(dx, dy, r, JOIN_SIDES))

    # A solid shape, for the handful of marks that are areas rather than
    # strokes. Wound clockwise like everything else so it unions the same way.
    out.extend([list(f) for f in fills])

    return out


def glyph_name(char):
    """A PostScript-safe name. Letters keep theirs; everything else goes by code point."""
    if char == " ":
        return "space"
    if char.isascii() and char.isalpha():
        return char
    return "uni%04X" % ord(char)


def notdef():
    """A hollow box, so a missing glyph looks missing rather than looking like a space."""
    return _stroke([(60, 0), (60, 660), (400, 660), (400, 0)], True, WEIGHT / 2.0)


# ----------------------------------------------------------------- assembly

def build(path):
    order = [".notdef"]
    cmap = {}
    glyphs = {}
    metrics = {}

    pen = TTGlyphPen(None)
    for contour in notdef():
        pen.moveTo(contour[0])
        for point in contour[1:]:
            pen.lineTo(point)
        pen.closePath()
    glyphs[".notdef"] = pen.glyph()
    metrics[".notdef"] = (520, 60)

    for char in G:
        name = glyph_name(char)

        contours = contours_for(char)
        fixed = G[char][2]

        if not contours:
            glyphs[name] = TTGlyphPen(None).glyph()
            metrics[name] = (fixed or 270, 0)
        else:
            xs = [p[0] for c in contours for p in c]
            shift = GAP - min(xs)
            width = max(xs) - min(xs)

            pen = TTGlyphPen(None)
            for contour in contours:
                moved = [(round(x + shift), round(y)) for x, y in contour]
                pen.moveTo(moved[0])
                for point in moved[1:]:
                    pen.lineTo(point)
                pen.closePath()

            glyphs[name] = pen.glyph()
            metrics[name] = (fixed or round(width + 2 * GAP), GAP)

        order.append(name)
        cmap[ord(char)] = name

    fb = FontBuilder(UPM, isTTF=True)
    fb.setupGlyphOrder(order)
    fb.setupCharacterMap(cmap)
    fb.setupGlyf(glyphs)
    fb.setupHorizontalMetrics(metrics)
    # Ascent has to clear the tallest ink (an l is ASC plus half a stroke) and
    # the midpoint of ascent..descent has to land near Minecraft Ten's, because
    # NanoVG centres text on that midpoint and the two faces must sit on the
    # same line when you switch between them.
    fb.setupHorizontalHeader(ascent=ASCENT, descent=DESCENT, lineGap=0)
    fb.setupNameTable({
        "familyName": "Kryptic",
        "styleName": "Regular",
        "uniqueFontIdentifier": "Kryptic Regular; generated by tools/mkfont.py",
        "fullName": "Kryptic Regular",
        "psName": "Kryptic-Regular",
        "version": "Version 1.000",
        "copyright": "Kryptic Client. Released into the public domain (CC0 1.0).",
        "licenseDescription": "Public domain (CC0 1.0). No rights reserved.",
    })
    fb.setupOS2(sTypoAscender=ASCENT, sTypoDescender=DESCENT, sTypoLineGap=0,
                usWinAscent=ASCENT, usWinDescent=abs(DESCENT),
                sxHeight=XH, sCapHeight=CAP, achVendID="KRYP")
    fb.setupPost()
    fb.save(path)
    return len(order)


if __name__ == "__main__":
    target = sys.argv[1] if len(sys.argv) > 1 else "Kryptic-Regular.ttf"
    print("%d glyphs -> %s" % (build(target), target))
