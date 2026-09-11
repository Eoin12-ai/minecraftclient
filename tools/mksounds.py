"""
Cut ten UI sounds out of three source recordings.

Each role gets a segment chosen from the RMS envelope of its source rather than
an arbitrary offset, so a cut starts on a transient and ends in silence. Pairs
(open/close, on/off) are the same segment played in opposite directions -- that
is what makes them read as a pair instead of two unrelated noises.

Peaks are targeted per role, not normalised flat: hover fires every time the
cursor crosses a row and has to sit under everything else, a toast has to carry
over the game.
"""
import os, struct, subprocess, wave

import sys

import imageio_ffmpeg              # pip install imageio-ffmpeg
FF = imageio_ffmpeg.get_ffmpeg_exe()

# argv[1]: a directory holding swish.wav, woosh.wav and sweep.wav -- the three
# source recordings decoded to mono 44.1k. argv[2]: where the .ogg files go,
# normally src/main/resources/assets/krypticclient/sounds/ui.
SRC = sys.argv[1] if len(sys.argv) > 1 else "sources"
OUT = sys.argv[2] if len(sys.argv) > 2 else "src/main/resources/assets/krypticclient/sounds/ui"
os.makedirs(OUT, exist_ok=True)
SR = 44100

# role -> (source, start, end, reverse, pitch, target_peak, fade_in, fade_out)
CUTS = {
    # the woosh: a long swell into one impact, then a tail. Panel-scale motion.
    "gui_open":   ("woosh", 0.34, 0.98, False, 1.06, 0.72, 0.045, 0.170),
    "gui_close":  ("woosh", 0.34, 0.98, True,  1.06, 0.66, 0.008, 0.200),
    # the impact alone, without the swell -- a toast is an event, not a motion
    "notify_on":  ("woosh", 0.60, 0.95, False, 1.28, 0.62, 0.006, 0.140),
    "notify_off": ("woosh", 0.60, 0.95, True,  0.96, 0.58, 0.006, 0.150),
    # the sweep: fast, bright, two-peaked. State changes.
    "toggle_on":  ("sweep", 0.12, 0.40, False, 1.18, 0.70, 0.005, 0.090),
    "toggle_off": ("sweep", 0.12, 0.40, True,  0.92, 0.66, 0.005, 0.090),
    # one peak of it, pitched into a tick. Fires continuously while dragging.
    "slider":     ("sweep", 0.24, 0.30, False, 1.90, 0.30, 0.002, 0.020),
    # the swish: three airy bounces. Discrete ticks in the menu.
    "hover":      ("swish", 0.02, 0.14, False, 1.55, 0.22, 0.003, 0.045),
    "select":     ("swish", 0.39, 0.58, False, 1.25, 0.52, 0.004, 0.070),
    "keybind":    ("swish", 0.78, 1.06, False, 1.00, 0.62, 0.004, 0.110),
}


def render(role, gain, dest):
    src, s, e, rev, pitch, _peak, fi, fo = CUTS[role]
    dur = (e - s) / pitch                      # asetrate changes length with pitch
    chain = [f"atrim=start={s}:end={e}", "asetpts=N/SR/TB"]
    if rev:
        chain.append("areverse")
    chain += [f"asetrate={SR}*{pitch}", f"aresample={SR}", "asetpts=N/SR/TB"]
    # The resampler's output is not exactly (e-s)/pitch long, so a fade-out
    # aimed at the nominal end can be truncated mid-ramp -- which is a click,
    # and is exactly what happened to the 29ms slider tick. Land the fade a
    # few ms early and pad the tail so the file always ends on real silence.
    fo = min(fo, dur * 0.5)
    chain.append(f"afade=t=in:st=0:d={fi}")
    chain.append(f"afade=t=out:st={max(0.0, dur - fo - 0.004):.5f}:d={fo}")
    chain.append("apad=pad_dur=0.006")
    if gain != 1.0:
        chain.append(f"volume={gain:.5f}")
    cmd = [FF, "-hide_banner", "-v", "error", "-i", os.path.join(SRC, src + ".wav"),
           "-filter:a", ",".join(chain), "-ac", "1", "-ar", str(SR)]
    if dest.endswith(".ogg"):
        cmd += ["-c:a", "libvorbis", "-q:a", "5"]
    else:
        cmd += ["-c:a", "pcm_s16le"]
    cmd += ["-y", dest]
    subprocess.run(cmd, check=True)
    return dur


def peak_of(path):
    w = wave.open(path)
    n = w.getnframes()
    s = struct.unpack("<%dh" % n, w.readframes(n))
    w.close()
    return (max(abs(v) for v in s) / 32768.0) if n else 0.0


report = []
for role in CUTS:
    probe = os.path.join(OUT, role + ".probe.wav")
    dur = render(role, 1.0, probe)
    raw = peak_of(probe)
    os.remove(probe)
    gain = (CUTS[role][5] / raw) if raw > 1e-6 else 1.0
    final = os.path.join(OUT, role + ".ogg")
    render(role, gain, final)
    # confirm the encoded file, not the intention
    chk = os.path.join(OUT, role + ".chk.wav")
    subprocess.run([FF, "-hide_banner", "-v", "error", "-i", final,
                    "-c:a", "pcm_s16le", "-y", chk], check=True)
    w = wave.open(chk); real_dur = w.getnframes() / w.getframerate(); w.close()
    got = peak_of(chk)
    os.remove(chk)
    report.append((role, CUTS[role][0], real_dur, got, os.path.getsize(final)))

print(f"{'role':<11} {'source':<6} {'dur':>7} {'peak':>6} {'bytes':>7}")
for r in report:
    print(f"{r[0]:<11} {r[1]:<6} {r[2]:>6.3f}s {r[3]:>6.3f} {r[4]:>7}")
