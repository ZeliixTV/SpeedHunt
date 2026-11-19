package com.speedhunt.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.Random;

/** The animated hide-and-seek map. Everything is drawn in a 340-unit wide coordinate space. */
final class MapView extends View {
    interface Listener { void onSpot(int index); }

    static final float MW = 340f;
    static final float ASPECT = 0.78f;
    static final float MH = MW / ASPECT;
    static final float[][] SPOT = {{.20f, .25f}, {.78f, .22f}, {.22f, .68f}, {.78f, .68f}, {.50f, .47f}};
    static final String[] LABEL = {"NORTH HOUSE", "EAST CAFE", "WEST GARDEN", "SOUTH SHOP", "OLD SHRINE"};
    static final int[] HOUSE = {0xFFE9A46A, 0xFF8FB7D5, 0xFFE6C66D, 0xFFB89AD8, 0xFFD88F9A};
    private static final float[][] TREES = {{.07f, .10f}, {.36f, .085f}, {.64f, .085f}, {.93f, .10f},
            {.05f, .47f}, {.95f, .47f}, {.06f, .87f}, {.94f, .87f}};
    private static final float PAD_PX = .34f, PAD_HX = .66f, PAD_Y = .91f;
    private static final int[] CONFETTI = {Pal.MINT, Pal.CORAL, Pal.GOLD, Pal.PURPLE, Pal.CREAM};

    private static final class Move {
        final float x0, y0, x1, y1;
        final int dur;
        final long t0;
        final Runnable done;
        Move(float x0, float y0, float x1, float y1, int dur, Runnable done) {
            this.x0 = x0; this.y0 = y0; this.x1 = x1; this.y1 = y1;
            this.dur = dur; this.done = done; this.t0 = SystemClock.uptimeMillis();
        }
    }

    private static final class Pt {
        float x, y, vx, vy, life, max, size, rot, vr;
        int color;
        boolean confetti;
    }

    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tp = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF r = new RectF();
    private final Path path = new Path();
    private final Path clip = new Path();
    private final DashPathEffect dash = new DashPathEffect(new float[]{6f, 8f}, 0f);
    private final Random rnd = new Random();
    private final ArrayList<Pt> pts = new ArrayList<>();

    private final boolean[] avail = {true, true, true, true, true};
    private final long[] deadAt = new long[5];
    private String skinEmoji = "🦆";
    private Listener listener;
    private boolean interactive, running;
    private float ga = 1f;

    private float left, top, scale = 1f;
    private float px = PAD_PX * MW, py = PAD_Y * MH, hx = PAD_HX * MW, hy = PAD_Y * MH;
    private float hopP, hopH;
    private Move pm, hm;
    private long last, lastPuffP, lastPuffH, shakeEnd, flashAt;
    private static final int SHAKE_MS = 480;

    MapView(Context c) {
        super(c);
        tp.setTextAlign(Paint.Align.CENTER);
        tp.setTypeface(Ui.HEAVY);
        bgPaint.setShader(new LinearGradient(0, 0, MW, MH,
                new int[]{0xFF344E56, 0xFF253E49, 0xFF344B4B}, new float[]{0f, .5f, 1f}, Shader.TileMode.CLAMP));
        clip.addRoundRect(new RectF(0, 0, MW, MH), 22, 22, Path.Direction.CW);
    }

    // ---------------------------------------------------------------- public API
    void setListener(Listener l) { listener = l; }
    void setInteractive(boolean b) { interactive = b; }
    void setSkin(String emoji) { skinEmoji = emoji; }

    void movePlayer(int spot, int dur, Runnable done) {
        pm = new Move(px, py, SPOT[spot][0] * MW - 9f, SPOT[spot][1] * MH + 11f, dur, done);
    }

    void moveHunter(int spot, int dur, Runnable done) {
        hm = new Move(hx, hy, SPOT[spot][0] * MW + 10f, SPOT[spot][1] * MH + 11f, dur, done);
    }

    void killSpot(int spot) {
        avail[spot] = false;
        deadAt[spot] = SystemClock.uptimeMillis();
    }

    void caught() {
        long now = SystemClock.uptimeMillis();
        shakeEnd = now + SHAKE_MS;
        flashAt = now;
        burst(hx, hy, Pal.CORAL, 18);
        burst(px, py, Pal.CREAM, 10);
    }

    void celebrate() {
        for (int i = 0; i < 90; i++) {
            Pt q = new Pt();
            q.x = rnd.nextFloat() * MW;
            q.y = -10f - rnd.nextFloat() * 120f;
            q.vx = (rnd.nextFloat() - .5f) * 60f;
            q.vy = 40f + rnd.nextFloat() * 90f;
            q.life = q.max = 3.2f + rnd.nextFloat();
            q.size = 3f + rnd.nextFloat() * 2.5f;
            q.rot = rnd.nextFloat() * 6f;
            q.vr = (rnd.nextFloat() - .5f) * 9f;
            q.color = CONFETTI[rnd.nextInt(CONFETTI.length)];
            q.confetti = true;
            pts.add(q);
        }
        burst(px, py, Pal.MINT, 16);
    }

    // ---------------------------------------------------------------- lifecycle
    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        running = true;
        last = SystemClock.uptimeMillis();
        postInvalidateOnAnimation();
    }

    @Override protected void onDetachedFromWindow() {
        running = false;
        super.onDetachedFromWindow();
    }

    @Override protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        float mw, mh;
        if ((float) w / h > ASPECT) { mh = h; mw = mh * ASPECT; } else { mw = w; mh = mw / ASPECT; }
        left = (w - mw) / 2f;
        top = (h - mh) / 2f;
        scale = mw / MW;
    }

    @Override public boolean onTouchEvent(MotionEvent e) {
        int a = e.getActionMasked();
        if (a == MotionEvent.ACTION_DOWN) return interactive;
        if (a == MotionEvent.ACTION_UP && interactive && listener != null) {
            float ux = (e.getX() - left) / scale, uy = (e.getY() - top) / scale;
            int best = -1;
            float bd = 1e9f;
            for (int i = 0; i < 5; i++) {
                if (!avail[i]) continue;
                float dx = ux - SPOT[i][0] * MW, dy = uy - SPOT[i][1] * MH;
                float d = (float) Math.sqrt(dx * dx + dy * dy);
                if (d < 46f && d < bd) { bd = d; best = i; }
            }
            if (best >= 0) listener.onSpot(best);
        }
        return true;
    }

    // ---------------------------------------------------------------- simulation
    private static float ease(float t) {
        return t < .5f ? 4f * t * t * t : 1f - (float) Math.pow(-2f * t + 2f, 3f) / 2f;
    }

    private void step(long now, float dt) {
        if (pm != null) {
            Move m = pm;
            float t = Math.min(1f, (now - m.t0) / (float) m.dur);
            float e = ease(t);
            px = m.x0 + (m.x1 - m.x0) * e;
            py = m.y0 + (m.y1 - m.y0) * e;
            hopP = (float) Math.sin(Math.PI * t) * 10f;
            if (now - lastPuffP > 45) { lastPuffP = now; puff(px, py + 8f, Pal.MINT); }
            if (t >= 1f) { pm = null; hopP = 0; if (m.done != null) post(m.done); }
        }
        if (hm != null) {
            Move m = hm;
            float t = Math.min(1f, (now - m.t0) / (float) m.dur);
            float e = ease(t);
            hx = m.x0 + (m.x1 - m.x0) * e;
            hy = m.y0 + (m.y1 - m.y0) * e;
            hopH = (float) Math.sin(Math.PI * t * 3f) * 3f;
            if (now - lastPuffH > 40) { lastPuffH = now; puff(hx, hy + 8f, Pal.CORAL); }
            if (t >= 1f) { hm = null; hopH = 0; if (m.done != null) post(m.done); }
        }
        for (int i = pts.size() - 1; i >= 0; i--) {
            Pt q = pts.get(i);
            q.life -= dt;
            if (q.life <= 0f) { pts.remove(i); continue; }
            if (q.confetti) q.vy += 150f * dt;
            q.x += q.vx * dt;
            q.y += q.vy * dt;
            q.rot += q.vr * dt;
        }
    }

    private void puff(float x, float y, int color) {
        Pt q = new Pt();
        q.x = x + (rnd.nextFloat() - .5f) * 6f;
        q.y = y;
        q.vx = (rnd.nextFloat() - .5f) * 12f;
        q.vy = -4f - rnd.nextFloat() * 8f;
        q.life = q.max = .45f;
        q.size = 2.5f + rnd.nextFloat() * 2f;
        q.color = color;
        pts.add(q);
    }

    private void burst(float x, float y, int color, int n) {
        for (int i = 0; i < n; i++) {
            double a = rnd.nextDouble() * Math.PI * 2;
            float sp = 40f + rnd.nextFloat() * 90f;
            Pt q = new Pt();
            q.x = x; q.y = y;
            q.vx = (float) Math.cos(a) * sp;
            q.vy = (float) Math.sin(a) * sp;
            q.life = q.max = .6f + rnd.nextFloat() * .4f;
            q.size = 2.5f + rnd.nextFloat() * 2.5f;
            q.color = color;
            pts.add(q);
        }
    }

    // ---------------------------------------------------------------- drawing helpers
    private int mul(int color) {
        if (ga >= 1f) return color;
        int a = Math.round(Color.alpha(color) * ga);
        return (color & 0x00FFFFFF) | (a << 24);
    }

    private void fill(int color) {
        p.setStyle(Paint.Style.FILL);
        p.setColor(mul(color));
    }

    private void stroke(int color, float w) {
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(w);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeJoin(Paint.Join.ROUND);
        p.setColor(mul(color));
    }

    private static int lighten(int c, float f) {
        return Color.argb(Color.alpha(c), (int) (Color.red(c) + (255 - Color.red(c)) * f),
                (int) (Color.green(c) + (255 - Color.green(c)) * f), (int) (Color.blue(c) + (255 - Color.blue(c)) * f));
    }

    private static int shade(int c, float f) {
        return Color.argb(Color.alpha(c), (int) (Color.red(c) * (1 - f)),
                (int) (Color.green(c) * (1 - f)), (int) (Color.blue(c) * (1 - f)));
    }

    private void rect(Canvas c, float l, float t, float rr, float b, float rad, int color) {
        fill(color);
        r.set(l, t, rr, b);
        if (rad > 0) c.drawRoundRect(r, rad, rad, p); else c.drawRect(r, p);
    }

    private void text(Canvas c, String s, float x, float y, float size, int color, boolean shadow) {
        tp.setTextSize(size);
        float by = y - (tp.ascent() + tp.descent()) / 2f;
        if (shadow) {
            tp.setColor(0xCC000000);
            c.drawText(s, x + .8f, by + .8f, tp);
        }
        tp.setColor(color);
        c.drawText(s, x, by, tp);
    }

    // ---------------------------------------------------------------- drawing
    @Override protected void onDraw(Canvas c) {
        long now = SystemClock.uptimeMillis();
        float dt = Math.min(.05f, Math.max(0f, (now - last) / 1000f));
        last = now;
        step(now, dt);

        c.save();
        c.translate(left, top);
        c.scale(scale, scale);
        if (now < shakeEnd) {
            float k = (shakeEnd - now) / (float) SHAKE_MS;
            c.translate((rnd.nextFloat() - .5f) * 18f * k, (rnd.nextFloat() - .5f) * 18f * k);
        }
        c.save();
        c.clipPath(clip);

        drawGround(c);
        drawRoads(c);
        drawPads(c);
        for (int i = 0; i < 5; i++) drawSpot(c, i, now);

        if (interactive) {
            for (int i = 0; i < 5; i++) {
                if (!avail[i]) continue;
                float rad = 36f + 3f * (float) Math.sin(now / 280.0 + i);
                stroke(Pal.a(Pal.MINT, .55f), 2f);
                c.drawCircle(SPOT[i][0] * MW, SPOT[i][1] * MH, rad, p);
            }
        }

        drawMarker(c, px, py - hopP, Pal.MINT, skinEmoji, 14f, 18f);
        if (hm != null) {
            float ph = (now % 700) / 700f;
            stroke(Pal.a(Pal.CORAL, .5f * (1f - ph)), 2f);
            c.drawCircle(hx, hy - hopH, 14f + ph * 24f, p);
        }
        drawMarker(c, hx, hy - hopH, Pal.CORAL, "!", 13f, 17f);
        drawParticles(c);

        text(c, "SPEED HUNT • ANIME DISTRICT", MW / 2f, 14f, 8f, Pal.a(Pal.CREAM, .75f), true);

        if (flashAt > 0 && now - flashAt < 500) {
            fill(Pal.a(Pal.CORAL, .38f * (1f - (now - flashAt) / 500f)));
            c.drawRect(0, 0, MW, MH, p);
        }
        c.restore();
        stroke(Pal.WHITE10, 2f);
        r.set(0, 0, MW, MH);
        c.drawRoundRect(r, 22, 22, p);
        c.restore();

        if (running) postInvalidateOnAnimation();
    }

    private void drawGround(Canvas c) {
        c.drawRect(0, 0, MW, MH, bgPaint);
        stroke(0x09FFFFFF, 1f);
        for (float x = 0; x < MW; x += 22f) c.drawLine(x, 0, x, MH, p);
        for (float y = 0; y < MH; y += 22f) c.drawLine(0, y, MW, y, p);
        // pond
        fill(0x663E7C94);
        r.set(MW * .5f - 52f, MH * .795f - 12f, MW * .5f + 52f, MH * .795f + 12f);
        c.drawOval(r, p);
        fill(0x33FFFFFF);
        r.set(MW * .5f - 34f, MH * .795f - 7f, MW * .5f - 6f, MH * .795f - 2f);
        c.drawOval(r, p);
        for (float[] t : TREES) drawTree(c, t[0] * MW, t[1] * MH);
    }

    private void drawTree(Canvas c, float x, float y) {
        fill(0x33000000);
        r.set(x - 11f, y + 8f, x + 11f, y + 14f);
        c.drawOval(r, p);
        rect(c, x - 2.5f, y, x + 2.5f, y + 11f, 1f, 0xFF5B4636);
        fill(0xFF2F6B58);
        c.drawCircle(x, y - 4f, 12f, p);
        fill(0xFF3F8A6B);
        c.drawCircle(x - 3.5f, y - 7.5f, 6.5f, p);
    }

    private void drawRoads(Canvas c) {
        float[] ax = new float[5], ay = new float[5];
        for (int i = 0; i < 5; i++) { ax[i] = SPOT[i][0] * MW; ay[i] = SPOT[i][1] * MH; }
        stroke(0xBFB6A88A, 14f);
        path.reset();
        path.moveTo(ax[0], ay[0]);
        path.lineTo(ax[1], ay[1]);
        path.lineTo(ax[3], ay[3]);
        path.lineTo(ax[2], ay[2]);
        path.close();
        c.drawPath(path, p);
        for (int i : new int[]{0, 1, 2, 3}) c.drawLine(ax[4], ay[4], ax[i], ay[i], p);
        stroke(0x44FFF4D8, 1.6f);
        p.setPathEffect(dash);
        c.drawPath(path, p);
        for (int i : new int[]{0, 1, 2, 3}) c.drawLine(ax[4], ay[4], ax[i], ay[i], p);
        p.setPathEffect(null);
    }

    private void drawPads(Canvas c) {
        float[][] pads = {{PAD_PX, 0}, {PAD_HX, 1}};
        for (float[] pd : pads) {
            int col = pd[1] == 0 ? Pal.MINT : Pal.CORAL;
            float x = pd[0] * MW, y = PAD_Y * MH;
            fill(Pal.a(col, .13f));
            c.drawCircle(x, y, 21f, p);
            stroke(Pal.a(col, .5f), 1.5f);
            c.drawCircle(x, y, 21f, p);
            text(c, pd[1] == 0 ? "YOU" : "HUNTER", x, y + 30f, 7.5f, Pal.a(Pal.CREAM, .8f), true);
        }
    }

    private void drawSpot(Canvas c, int i, long now) {
        float x = SPOT[i][0] * MW, y = SPOT[i][1] * MH;
        if (avail[i]) {
            c.save();
            c.translate(x, y);
            building(c, i);
            c.restore();
            text(c, LABEL[i], x, y + 38f, 8f, 0xFFEDE8D8, true);
            return;
        }
        float t = Math.min(1f, (now - deadAt[i]) / 450f);
        if (t < 1f) {
            ga = 1f - t;
            c.save();
            c.translate(x, y);
            float s = 1f - .4f * t;
            c.scale(s, s);
            building(c, i);
            c.restore();
            ga = 1f;
        }
        float xs = Math.min(1f, t * 1.4f);
        stroke(Pal.a(0xFFFFFFFF, .25f * xs), 4f);
        float d = 8f * xs;
        c.drawLine(x - d, y - d, x + d, y + d, p);
        c.drawLine(x + d, y - d, x - d, y + d, p);
    }

    private void building(Canvas c, int i) {
        int col = HOUSE[i];
        fill(Pal.a(col, .13f));
        c.drawCircle(0, 0, 31f, p);
        switch (i) {
            case 0: // house
                rect(c, -21.5f, -17f, 21.5f, 17f, 5f, col);
                fill(lighten(col, .18f));
                path.reset();
                path.moveTo(-26f, -8f); path.lineTo(0f, -30f); path.lineTo(26f, -8f); path.close();
                c.drawPath(path, p);
                rect(c, -4.5f, -1.5f, 4.5f, 15.5f, 0f, 0xFF58445A);
                rect(c, -16.5f, -2.5f, -9.5f, 4.5f, 0f, 0xFFBDE8E9);
                rect(c, 9.5f, -2.5f, 16.5f, 4.5f, 0f, 0xFFBDE8E9);
                break;
            case 1: // cafe
                rect(c, -24f, -14f, 24f, 18f, 4f, col);
                for (int k = 0; k < 6; k++) {
                    int sc = k % 2 == 0 ? Pal.CORAL : Pal.CREAM;
                    rect(c, -27f + k * 9f, -19f, -18f + k * 9f, -7f, 0f, sc);
                    fill(sc);
                    c.drawCircle(-22.5f + k * 9f, -7f, 4.5f, p);
                }
                rect(c, -5f, 1f, 5f, 18f, 0f, 0xFF58445A);
                rect(c, -20f, 2f, -10f, 11f, 1f, 0xFFBDE8E9);
                rect(c, 10f, 2f, 20f, 11f, 1f, 0xFFBDE8E9);
                break;
            case 2: // garden
                rect(c, -26f, 2f, 26f, 19f, 8f, 0xFF4FA36A);
                rect(c, -3f, -8f, 3f, 6f, 1f, 0xFF6B4F3A);
                fill(0xFF5CBF7A);
                c.drawCircle(0f, -15f, 14f, p);
                fill(0xFF7ED99A);
                c.drawCircle(-5f, -19f, 5f, p);
                int[] fl = {0xFFFF8FB5, Pal.GOLD, 0xFFFF8FB5, Pal.CREAM};
                float[] fx = {-17f, -7f, 9f, 18f}, fy = {9f, 13f, 10f, 14f};
                for (int k = 0; k < 4; k++) { fill(fl[k]); c.drawCircle(fx[k], fy[k], 3f, p); }
                break;
            case 3: // shop
                rect(c, -23f, -15f, 23f, 18f, 4f, col);
                rect(c, -26f, -22f, 26f, -12f, 3f, shade(col, .35f));
                rect(c, -15f, -20f, 15f, -14f, 2f, Pal.CREAM);
                rect(c, -17f, -6f, 2f, 9f, 1f, 0xFFBDE8E9);
                rect(c, 8f, -4f, 18f, 18f, 0f, 0xFF58445A);
                fill(Pal.CORAL);
                c.drawCircle(13f, 4f, 1.6f, p);
                break;
            default: // shrine gate
                fill(0x33000000);
                r.set(-24f, 14f, 24f, 22f);
                c.drawOval(r, p);
                rect(c, -17f, -10f, -12f, 19f, 1f, 0xFFE5534B);
                rect(c, 12f, -10f, 17f, 19f, 1f, 0xFFE5534B);
                rect(c, -20f, -7f, 20f, -2.5f, 1f, 0xFFE5534B);
                rect(c, -27f, -19f, 27f, -12f, 2f, 0xFF2B2438);
                rect(c, -29f, -22f, -20f, -17f, 2f, 0xFF2B2438);
                rect(c, 20f, -22f, 29f, -17f, 2f, 0xFF2B2438);
                fill(Pal.GOLD);
                c.drawCircle(0f, 1f, 3f, p);
                break;
        }
    }

    private void drawMarker(Canvas c, float x, float y, int color, String glyph, float rad, float size) {
        fill(Pal.a(color, .18f));
        c.drawCircle(x, y, rad + 7f, p);
        fill(color);
        c.drawCircle(x, y, rad, p);
        stroke(0xFFFFFFFF, 2f);
        c.drawCircle(x, y, rad, p);
        text(c, glyph, x, y, size, 0xFFFFFFFF, false);
    }

    private void drawParticles(Canvas c) {
        for (Pt q : pts) {
            float f = Math.max(0f, q.life / q.max);
            if (q.confetti) {
                fill(Pal.a(q.color, Math.min(1f, f * 2f)));
                c.save();
                c.translate(q.x, q.y);
                c.rotate((float) Math.toDegrees(q.rot));
                c.drawRect(-q.size, -q.size * .55f, q.size, q.size * .55f, p);
                c.restore();
            } else {
                fill(Pal.a(q.color, f * .7f));
                c.drawCircle(q.x, q.y, q.size * (1.2f - f * .4f), p);
            }
        }
    }
}
