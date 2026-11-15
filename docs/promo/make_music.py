"""Genera la música de fondo del video promocional de Minicash.

Síntesis propia con numpy (sin muestras ni licencias de terceros): pad cálido,
piano eléctrico en arpegio, bajo suave y percusión ligera. 96 BPM, 32 s.
Uso: python make_music.py salida.wav
"""
import sys
import numpy as np
import wave

SR = 44100
BPM = 96
BEAT = 60 / BPM
BARS = 13  # 13 compases * 2.5 s = 32.5 s
DUR = BARS * 4 * BEAT
N = int(DUR * SR)
rng = np.random.default_rng(7)


def hz(midi):
    return 440.0 * 2 ** ((midi - 69) / 12)


def adsr(n, a, d, s, r):
    t = np.arange(n) / SR
    env = np.ones(n) * s
    ai = int(a * SR)
    di = int(d * SR)
    ri = int(r * SR)
    if ai:
        env[:ai] = np.linspace(0, 1, ai)
    if di:
        env[ai:ai + di] = np.linspace(1, s, di)[: max(0, min(di, n - ai))]
    if ri and ri < n:
        env[-ri:] *= np.linspace(1, 0, ri)
    return env


mix = np.zeros((2, N))


def add(sig, start, pan=0.0, gain=1.0):
    i = int(start * SR)
    if i >= N:
        return
    sig = sig[: N - i]
    l = gain * (1 - max(0, pan))
    r = gain * (1 + min(0, pan))
    mix[0, i:i + len(sig)] += sig * l
    mix[1, i:i + len(sig)] += sig * r


def pad(notes, start, length):
    n = int(length * SR)
    t = np.arange(n) / SR
    out = np.zeros(n)
    for m in notes:
        for det in (-0.07, 0.0, 0.07):
            f = hz(m) * 2 ** (det / 12)
            out += np.sin(2 * np.pi * f * t) + 0.25 * np.sin(2 * np.pi * 2 * f * t)
    out *= adsr(n, 1.0, 0.3, 0.8, 1.2) / (len(notes) * 3)
    return out


def epiano(m, length=1.4):
    n = int(length * SR)
    t = np.arange(n) / SR
    f = hz(m)
    mod = np.sin(2 * np.pi * f * 14 * t) * np.exp(-t * 9) * 0.6
    tone = np.sin(2 * np.pi * f * t + mod) + 0.3 * np.sin(2 * np.pi * 2 * f * t) * np.exp(-t * 3)
    return tone * np.exp(-t * 3.2) * (1 - np.exp(-t * 400))


def bass(m, length):
    n = int(length * SR)
    t = np.arange(n) / SR
    f = hz(m)
    return (np.sin(2 * np.pi * f * t) + 0.2 * np.sin(2 * np.pi * 2 * f * t)) * adsr(n, 0.01, 0.1, 0.7, 0.15)


def kick():
    n = int(0.35 * SR)
    t = np.arange(n) / SR
    f = 48 + 90 * np.exp(-t * 28)
    return np.sin(2 * np.pi * np.cumsum(f) / SR) * np.exp(-t * 11)


def hat(open_=False):
    n = int((0.25 if open_ else 0.06) * SR)
    t = np.arange(n) / SR
    x = rng.standard_normal(n)
    x = np.diff(x, prepend=0)  # pasa-altos burdo
    return x * np.exp(-t * (18 if open_ else 70))


def clap():
    n = int(0.22 * SR)
    t = np.arange(n) / SR
    x = rng.standard_normal(n)
    x = np.convolve(x, np.ones(6) / 6, mode="same")
    return x * np.exp(-t * 22)


# Progresión: Cmaj7 - Am7 - Fmaj7 - G6 (re-armonizada, luminosa)
chords = [
    ([48, 55, 59, 64], 36, [64, 67, 71, 72]),   # Cmaj7
    ([45, 52, 55, 60], 33, [60, 64, 67, 69]),   # Am7
    ([41, 48, 52, 57], 29, [60, 64, 65, 69]),   # Fmaj7
    ([43, 50, 55, 59], 31, [59, 62, 67, 69]),   # G6
]
arp = [0, 1, 2, 3, 2, 1, 2, 3]  # patrón de corcheas

for bar in range(BARS):
    pad_notes, bass_note, arp_notes = chords[bar % 4]
    t0 = bar * 4 * BEAT
    add(pad(pad_notes, t0, 4 * BEAT + 0.8), t0, gain=0.20)
    add(bass(bass_note + 12, 1.6 * BEAT), t0, gain=0.30)
    add(bass(bass_note + 12, 1.2 * BEAT), t0 + 2.5 * BEAT, gain=0.22)
    if bar >= 1:
        for i, a in enumerate(arp):
            tt = t0 + i * BEAT / 2
            vel = 0.17 if i % 2 == 0 else 0.11
            add(epiano(arp_notes[a] + 12 * (i // 4 % 2 == 1 and 0)), tt, pan=(-0.4 if i % 2 else 0.4), gain=vel)
    if bar >= 2:
        for b in range(4):
            add(kick(), t0 + b * BEAT, gain=0.42 if b % 2 == 0 else 0.0)
        for i in range(8):
            add(hat(i == 7), t0 + i * BEAT / 2 + 0.0, pan=0.3, gain=0.07 if i % 2 else 0.04)
        add(clap(), t0 + 1 * BEAT, gain=0.10)
        add(clap(), t0 + 3 * BEAT, gain=0.10)

# Reverb simple: eco difuso con varios retardos
wet = np.zeros_like(mix)
for d, g in ((0.043, 0.35), (0.071, 0.28), (0.113, 0.22), (0.167, 0.16), (0.241, 0.1)):
    k = int(d * SR)
    wet[0, k:] += mix[1, :-k] * g
    wet[1, k:] += mix[0, :-k] * g
mix = mix + wet * 0.7

# Fundido de entrada/salida y normalización
fi = int(1.2 * SR)
fo = int(3.0 * SR)
mix[:, :fi] *= np.linspace(0, 1, fi)
mix[:, -fo:] *= np.linspace(1, 0, fo) ** 1.5
mix /= np.max(np.abs(mix)) / 0.89

pcm = (mix.T * 32767).astype("<i2")
with wave.open(sys.argv[1], "wb") as w:
    w.setnchannels(2)
    w.setsampwidth(2)
    w.setframerate(SR)
    w.writeframes(pcm.tobytes())
print(f"{DUR:.1f}s")
