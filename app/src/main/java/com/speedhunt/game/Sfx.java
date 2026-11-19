package com.speedhunt.game;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Looper;
import android.view.HapticFeedbackConstants;
import android.view.View;

/** Tiny synthesised sound effects - no audio files needed. */
final class Sfx {
    static boolean sound = true, haptics = true;
    private static final int SR = 22050;
    private static final Handler H = new Handler(Looper.getMainLooper());
    private static short[] TAP, MOVE, CAUGHT, WIN, BUY, ERROR, SAFE;

    private Sfx() {}

    static void init() {
        TAP = tone(760, 900, 60, 0.35f, 0);
        MOVE = tone(280, 640, 170, 0.30f, 2);
        SAFE = cat(tone(520, 520, 70, 0.35f, 0), tone(780, 780, 140, 0.35f, 0));
        CAUGHT = cat(tone(420, 300, 140, 0.30f, 1), tone(300, 110, 380, 0.30f, 1));
        WIN = cat(tone(523, 523, 100, 0.35f, 2), tone(659, 659, 100, 0.35f, 2),
                tone(784, 784, 100, 0.35f, 2), tone(1046, 1046, 320, 0.35f, 2));
        BUY = cat(tone(880, 880, 70, 0.35f, 0), tone(1320, 1320, 160, 0.35f, 0));
        ERROR = tone(190, 150, 160, 0.30f, 1);
    }

    static void tap() { play(TAP); }
    static void move() { play(MOVE); }
    static void safe() { play(SAFE); }
    static void caught() { play(CAUGHT); }
    static void win() { play(WIN); }
    static void buy() { play(BUY); }
    static void error() { play(ERROR); }

    static void haptic(View v, boolean strong) {
        if (!haptics || v == null) return;
        try {
            v.performHapticFeedback(strong ? HapticFeedbackConstants.LONG_PRESS
                    : HapticFeedbackConstants.VIRTUAL_KEY);
        } catch (Throwable ignored) { }
    }

    static void click(View v) {
        tap();
        haptic(v, false);
    }

    private static short[] tone(float f0, float f1, int ms, float vol, int wave) {
        int n = SR * ms / 1000;
        short[] o = new short[n];
        double ph = 0;
        for (int i = 0; i < n; i++) {
            float t = (float) i / n;
            double f = f0 + (f1 - f0) * t;
            ph += 2 * Math.PI * f / SR;
            double s;
            if (wave == 1) s = Math.sin(ph) >= 0 ? 1 : -1;
            else if (wave == 2) s = 2 / Math.PI * Math.asin(Math.sin(ph));
            else s = Math.sin(ph);
            double env = Math.min(1.0, i / (SR * 0.005)) * Math.pow(1 - t, 1.4);
            o[i] = (short) (s * env * vol * 32767);
        }
        return o;
    }

    private static short[] cat(short[]... parts) {
        int n = 0;
        for (short[] p : parts) n += p.length;
        short[] o = new short[n];
        int k = 0;
        for (short[] p : parts) {
            System.arraycopy(p, 0, o, k, p.length);
            k += p.length;
        }
        return o;
    }

    private static void play(final short[] d) {
        if (!sound || d == null) return;
        try {
            final AudioTrack t = new AudioTrack.Builder()
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
                    .setAudioFormat(new AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SR)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                    .setBufferSizeInBytes(d.length * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC).build();
            t.write(d, 0, d.length);
            t.play();
            H.postDelayed(new Runnable() {
                @Override public void run() {
                    try { t.release(); } catch (Throwable ignored) { }
                }
            }, d.length * 1000L / SR + 300);
        } catch (Throwable ignored) { }
    }
}
