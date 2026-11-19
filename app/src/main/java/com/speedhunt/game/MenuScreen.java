package com.speedhunt.game;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** Main menu. */
final class MenuScreen extends FrameLayout {
    private final MainActivity act;
    private final Store st;
    private BgView bg;
    private ObjectAnimator bob, glow;

    MenuScreen(MainActivity a) {
        super(a);
        act = a;
        st = a.store;
        build();
    }

    void applyBg(int idx) {
        bg.setTop(Pal.BG[idx]);
        act.setBars(Pal.BG[idx], Pal.INK);
    }

    private void build() {
        Context c = getContext();
        bg = new BgView(c, Pal.BG[st.bg]);
        addView(bg, new LayoutParams(Ui.MATCH, Ui.MATCH));

        ScrollView sv = new ScrollView(c);
        sv.setFillViewport(true);
        sv.setVerticalScrollBarEnabled(false);
        sv.setOverScrollMode(OVER_SCROLL_NEVER);
        addView(sv, new LayoutParams(Ui.MATCH, Ui.MATCH));

        LinearLayout col = new LinearLayout(c);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(Ui.dp(20), Ui.dp(14), Ui.dp(20), Ui.dp(16));
        sv.addView(col, new LayoutParams(Ui.MATCH, Ui.WRAP));

        // top bar
        LinearLayout top = new LinearLayout(c);
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = Ui.text(c, "SPEED HUNT", 22, Pal.CREAM, true);
        title.setLetterSpacing(0.1f);
        top.addView(title, Ui.lin(0, Ui.WRAP, 1f));
        top.addView(new CoinPill(c, st.coins), Ui.lin(Ui.WRAP, Ui.WRAP));
        col.addView(top, Ui.lin(Ui.MATCH, Ui.WRAP));

        col.addView(Ui.spacer(c, 1f));

        // hero card
        LinearLayout card = new LinearLayout(c);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        card.setPadding(Ui.dp(22), Ui.dp(22), Ui.dp(22), Ui.dp(20));
        card.setBackground(Ui.box(0x2E000000, 28, Pal.a(0xFFFFFFFF, .08f), 1));
        col.addView(card, Ui.lin(Ui.MATCH, Ui.WRAP));

        TextView hero = Ui.text(c, "HIDE FAST.\nSTAY UNSEEN.", 32, Pal.CREAM, true);
        hero.setGravity(Gravity.CENTER);
        hero.setLineSpacing(0, 1.02f);
        card.addView(hero, Ui.lin(Ui.WRAP, Ui.WRAP, 0, 0, 0, 10));

        Skin skin = Skin.ALL[st.skin];
        FrameLayout wrap = new FrameLayout(c);
        card.addView(wrap, Ui.lin(Ui.dp(190), Ui.dp(180)));

        View halo = new View(c);
        GradientDrawable hg = new GradientDrawable();
        hg.setShape(GradientDrawable.OVAL);
        hg.setColor(Pal.a(Pal.PURPLE, .20f));
        halo.setBackground(hg);
        wrap.addView(halo, new LayoutParams(Ui.dp(170), Ui.dp(170), Gravity.CENTER));

        final FrameLayout tile = new FrameLayout(c);
        tile.setBackground(Ui.box(Pal.PANEL, 28, Pal.PURPLE, 2));
        TextView emoji = new TextView(c);
        emoji.setText(skin.emoji);
        emoji.setTextSize(68);
        emoji.setIncludeFontPadding(false);
        emoji.setGravity(Gravity.CENTER);
        tile.addView(emoji, new LayoutParams(Ui.MATCH, Ui.MATCH, Gravity.CENTER));
        wrap.addView(tile, new LayoutParams(Ui.dp(130), Ui.dp(130), Gravity.CENTER));
        Ui.press(tile);
        Ui.onClick(tile, new Runnable() { @Override public void run() { act.showShop(); } });

        bob = ObjectAnimator.ofFloat(tile, "translationY", 0f, -Ui.dp(7));
        bob.setDuration(1300);
        bob.setRepeatCount(ObjectAnimator.INFINITE);
        bob.setRepeatMode(ObjectAnimator.REVERSE);
        glow = ObjectAnimator.ofFloat(halo, "alpha", .55f, 1f);
        glow.setDuration(1300);
        glow.setRepeatCount(ObjectAnimator.INFINITE);
        glow.setRepeatMode(ObjectAnimator.REVERSE);

        card.addView(Ui.text(c, skin.name, 16, Pal.MINT, true), Ui.lin(Ui.WRAP, Ui.WRAP, 0, 2, 0, 6));
        TextView chip = Ui.text(c, skin.rarity.toUpperCase(), 10, skin.color(), true);
        chip.setLetterSpacing(.12f);
        chip.setPadding(Ui.dp(10), Ui.dp(4), Ui.dp(10), Ui.dp(4));
        chip.setBackground(Ui.box(Pal.a(skin.color(), .14f), 12));
        card.addView(chip, Ui.lin(Ui.WRAP, Ui.WRAP, 0, 0, 0, 10));
        TextView tag = Ui.text(c, "A tiny hide-and-seek survival game", 13, Pal.WHITE70, false);
        tag.setGravity(Gravity.CENTER);
        card.addView(tag, Ui.lin(Ui.WRAP, Ui.WRAP));

        col.addView(Ui.spacer(c, 1f));

        // buttons
        LinearLayout play = Ui.btn(c, IconView.PLAY, "PLAY", Pal.PURPLE, 0xFFFFFFFF, 18, 18, 0, 0);
        Ui.onClick(play, new Runnable() { @Override public void run() { act.startGame(); } });
        col.addView(play, Ui.lin(Ui.MATCH, Ui.dp(60)));

        LinearLayout row = new LinearLayout(c);
        LinearLayout shop = Ui.btn(c, IconView.BAG, "SKINS & SHOP", 0x00000000, Pal.CREAM, 12, 16, Pal.WHITE24, 1);
        LinearLayout set = Ui.btn(c, IconView.GEAR, "SETTINGS", 0x00000000, Pal.CREAM, 12, 16, Pal.WHITE24, 1);
        Ui.onClick(shop, new Runnable() { @Override public void run() { act.showShop(); } });
        Ui.onClick(set, new Runnable() { @Override public void run() { act.openSettings(); } });
        row.addView(shop, Ui.lin(0, Ui.dp(52), 1f));
        LinearLayout.LayoutParams lp = Ui.lin(0, Ui.dp(52), 1f);
        lp.leftMargin = Ui.dp(10);
        row.addView(set, lp);
        col.addView(row, Ui.lin(Ui.MATCH, Ui.WRAP, 0, 12, 0, 0));

        TextView best = Ui.text(c, "BEST SURVIVAL  •  " + st.best + " / 4 PINGS", 11, Pal.WHITE54, true);
        best.setLetterSpacing(.1f);
        col.addView(best, Ui.lin(Ui.WRAP, Ui.WRAP, 0, 18, 0, 0));
        ((LinearLayout.LayoutParams) best.getLayoutParams()).gravity = Gravity.CENTER_HORIZONTAL;
        TextView stats = Ui.text(c, st.games + " ROUNDS  •  " + st.wins + " ESCAPES", 10, Pal.a(0xFFFFFFFF, .38f), true);
        stats.setLetterSpacing(.1f);
        col.addView(stats, Ui.lin(Ui.WRAP, Ui.WRAP, 0, 6, 0, 0));
        ((LinearLayout.LayoutParams) stats.getLayoutParams()).gravity = Gravity.CENTER_HORIZONTAL;
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (bob != null) bob.start();
        if (glow != null) glow.start();
    }

    @Override protected void onDetachedFromWindow() {
        if (bob != null) bob.cancel();
        if (glow != null) glow.cancel();
        super.onDetachedFromWindow();
    }
}
