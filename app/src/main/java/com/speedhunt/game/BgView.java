package com.speedhunt.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.SystemClock;
import android.view.View;

/** Gradient background with slowly drifting soft bubbles. */
final class BgView extends View {
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int top;
    private boolean dirty = true, running;
    // x, y (fraction), radius (fraction of width), speed, phase
    private static final float[][] ORBS = {
        {.05f, .08f, .16f, .30f, 0f}, {.95f, .26f, .22f, .22f, 1.7f}, {.20f, .55f, .12f, .35f, 3.1f},
        {.85f, .72f, .17f, .26f, 4.4f}, {.45f, .92f, .20f, .20f, 5.5f}, {.60f, .40f, .07f, .40f, 2.2f}
    };

    BgView(Context c, int topColor) {
        super(c);
        top = topColor;
    }

    void setTop(int c) {
        top = c;
        dirty = true;
        invalidate();
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        running = true;
        postInvalidateOnAnimation();
    }

    @Override protected void onDetachedFromWindow() {
        running = false;
        super.onDetachedFromWindow();
    }

    @Override protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        dirty = true;
    }

    @Override protected void onDraw(Canvas c) {
        int w = getWidth(), h = getHeight();
        if (dirty) {
            p.setShader(new LinearGradient(0, 0, 0, h, top, Pal.INK, Shader.TileMode.CLAMP));
            dirty = false;
        }
        p.setStyle(Paint.Style.FILL);
        c.drawRect(0, 0, w, h, p);
        p.setShader(null);
        float t = SystemClock.uptimeMillis() / 1000f;
        for (float[] o : ORBS) {
            float x = (o[0] + 0.03f * (float) Math.sin(t * o[3] * .6f + o[4])) * w;
            float y = (o[1] + 0.02f * (float) Math.cos(t * o[3] * .5f + o[4])) * h;
            p.setColor(0x0AFFFFFF);
            c.drawCircle(x, y, o[2] * w, p);
        }
        if (running) postInvalidateDelayed(33);
    }
}
