package com.speedhunt.game;

import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Rounded coin counter used in the headers. */
final class CoinPill extends LinearLayout {
    private final TextView t;

    CoinPill(Context c, int amount) {
        super(c);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setPadding(Ui.dp(12), Ui.dp(8), Ui.dp(14), Ui.dp(8));
        setBackground(Ui.box(Pal.PANEL, 30, Pal.WHITE10, 1));
        LayoutParams lp = new LayoutParams(Ui.dp(16), Ui.dp(16));
        lp.rightMargin = Ui.dp(7);
        addView(Ui.coin(c, 16), lp);
        t = Ui.text(c, String.valueOf(amount), 15, Pal.CREAM, true);
        addView(t, new LayoutParams(Ui.WRAP, Ui.WRAP));
    }

    void set(int amount) { t.setText(String.valueOf(amount)); }
}
