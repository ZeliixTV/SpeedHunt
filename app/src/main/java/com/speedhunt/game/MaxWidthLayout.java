package com.speedhunt.game;

import android.content.Context;
import android.widget.FrameLayout;

/** Keeps the UI phone-sized on tablets / foldables. */
final class MaxWidthLayout extends FrameLayout {
    MaxWidthLayout(Context c) { super(c); }

    @Override
    protected void onMeasure(int w, int h) {
        int size = MeasureSpec.getSize(w);
        int max = Ui.dp(560);
        if (size > max) w = MeasureSpec.makeMeasureSpec(max, MeasureSpec.EXACTLY);
        super.onMeasure(w, h);
    }
}
