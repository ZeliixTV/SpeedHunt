package com.speedhunt.game;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/** UI helpers: everything is built in code, no XML layouts. */
final class Ui {
    static final int MATCH = -1, WRAP = -2;
    static float D = 1f;
    static final Typeface HEAVY = Typeface.create("sans-serif-black", Typeface.NORMAL);
    static final Typeface MED = Typeface.create("sans-serif-medium", Typeface.NORMAL);

    private Ui() {}

    static void init(Context c) { D = c.getResources().getDisplayMetrics().density; }

    static int dp(float v) { return Math.round(v * D); }

    static GradientDrawable box(int fill, float radiusDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(fill);
        g.setCornerRadius(dp(radiusDp));
        return g;
    }

    static GradientDrawable box(int fill, float radiusDp, int stroke, float strokeDp) {
        GradientDrawable g = box(fill, radiusDp);
        g.setStroke(Math.max(1, dp(strokeDp)), stroke);
        return g;
    }

    /** Text size is in dp on purpose, so the layout never breaks with huge system fonts. */
    static TextView text(Context c, String s, float size, int color, boolean heavy) {
        TextView t = new TextView(c);
        t.setText(s);
        t.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
        t.setTextColor(color);
        t.setTypeface(heavy ? HEAVY : MED);
        t.setIncludeFontPadding(false);
        if (heavy) t.setLetterSpacing(0.04f);
        return t;
    }

    static LinearLayout.LayoutParams lin(int w, int h) { return new LinearLayout.LayoutParams(w, h); }

    static LinearLayout.LayoutParams lin(int w, int h, float weight) {
        return new LinearLayout.LayoutParams(w, h, weight);
    }

    static LinearLayout.LayoutParams lin(int w, int h, int l, int t, int r, int b) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(w, h);
        lp.setMargins(dp(l), dp(t), dp(r), dp(b));
        return lp;
    }

    static LinearLayout.LayoutParams lin(int w, int h, float weight, int l, int t, int r, int b) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(w, h, weight);
        lp.setMargins(dp(l), dp(t), dp(r), dp(b));
        return lp;
    }

    static View spacer(Context c, float weight) {
        View v = new View(c);
        v.setLayoutParams(new LinearLayout.LayoutParams(0, 0, weight));
        return v;
    }

    static void press(View v) {
        v.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View view, MotionEvent e) {
                switch (e.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        view.animate().scaleX(.95f).scaleY(.95f).setDuration(80).start();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        view.animate().scaleX(1f).scaleY(1f).setDuration(120).start();
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }

    static void onClick(View v, final Runnable r) {
        v.setClickable(true);
        v.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                Sfx.click(view);
                r.run();
            }
        });
    }

    static void shake(View v) {
        ObjectAnimator a = ObjectAnimator.ofFloat(v, "translationX", 0, -dp(9), dp(9), -dp(7), dp(7), 0);
        a.setDuration(340);
        a.start();
    }

    static void pop(View v) {
        v.setScaleX(.86f);
        v.setScaleY(.86f);
        v.animate().scaleX(1f).scaleY(1f).setDuration(260)
                .setInterpolator(new android.view.animation.OvershootInterpolator(2.2f)).start();
    }

    static View coin(Context c, int sizeDp) {
        View v = new View(c);
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.OVAL);
        g.setColor(Pal.GOLD);
        g.setStroke(Math.max(1, dp(sizeDp / 7f)), 0xFFD9962B);
        v.setBackground(g);
        return v;
    }

    /** Button with optional icon (icon < 0 = none). */
    static LinearLayout btn(Context c, int icon, String label, int fill, int fg, float size,
                            float radiusDp, int stroke, float strokeDp) {
        LinearLayout b = new LinearLayout(c);
        b.setOrientation(LinearLayout.HORIZONTAL);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(10), 0, dp(10), 0);
        b.setBackground(stroke != 0 ? box(fill, radiusDp, stroke, strokeDp) : box(fill, radiusDp));
        if (icon >= 0) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(size + 6), dp(size + 6));
            lp.rightMargin = dp(8);
            b.addView(new IconView(c, icon, fg), lp);
        }
        TextView t = text(c, label, size, fg, true);
        t.setSingleLine(true);
        b.addView(t, new LinearLayout.LayoutParams(WRAP, WRAP));
        b.setClickable(true);
        press(b);
        return b;
    }

    static LinearLayout card(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(dp(22), dp(22), dp(22), dp(20));
        l.setBackground(box(Pal.PANEL, 26, Pal.WHITE10, 1));
        l.setClickable(true);
        return l;
    }

    static View roundIcon(Context c, int icon, final Runnable r) {
        FrameLayout f = new FrameLayout(c);
        f.setBackground(box(Pal.PANEL, 22, Pal.WHITE10, 1));
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(dp(22), dp(22), Gravity.CENTER);
        f.addView(new IconView(c, icon, Pal.CREAM), lp);
        press(f);
        onClick(f, r);
        return f;
    }

    /** Small message bubble at the bottom of a FrameLayout. */
    static void banner(final FrameLayout host, String msg, int color) {
        final TextView t = text(host.getContext(), msg, 13, Pal.INK, true);
        t.setPadding(dp(18), dp(12), dp(18), dp(12));
        t.setBackground(box(color, 22));
        t.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(WRAP, WRAP, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        lp.bottomMargin = dp(28);
        host.addView(t, lp);
        t.setAlpha(0f);
        t.setTranslationY(dp(16));
        t.animate().alpha(1f).translationY(0).setDuration(180).start();
        t.postDelayed(new Runnable() {
            @Override public void run() {
                t.animate().alpha(0f).setDuration(220).withEndAction(new Runnable() {
                    @Override public void run() { host.removeView(t); }
                }).start();
            }
        }, 1800);
    }
}
