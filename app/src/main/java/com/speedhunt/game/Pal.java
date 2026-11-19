package com.speedhunt.game;

/** Colour palette (same look as the original prototype). */
final class Pal {
    static final int INK = 0xFF17152D;
    static final int PANEL = 0xFF24213F;
    static final int PURPLE = 0xFF9A78FF;
    static final int MINT = 0xFF64E6A5;
    static final int CORAL = 0xFFFF626F;
    static final int CREAM = 0xFFFFF4D8;
    static final int GOLD = 0xFFFFC857;
    static final int WHITE70 = 0xB3FFFFFF;
    static final int WHITE54 = 0x8AFFFFFF;
    static final int WHITE24 = 0x3DFFFFFF;
    static final int WHITE10 = 0x1AFFFFFF;

    static final int[] BG = {0xFF252344, 0xFF17384A, 0xFF3A2545};
    static final String[] BG_NAMES = {"TWILIGHT", "OCEAN", "SAKURA"};

    private Pal() {}

    static int a(int color, float alpha) {
        int al = Math.max(0, Math.min(255, Math.round(255f * alpha)));
        return (color & 0x00FFFFFF) | (al << 24);
    }
}
