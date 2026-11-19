package com.speedhunt.game;

import android.content.Context;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;

/** Skin shop. */
final class ShopScreen extends FrameLayout implements BackAware {
    private final MainActivity act;
    private final Store st;
    private CoinPill pill;
    private TextView sub;
    private LinearLayout list, chips;
    private int filter = -1; // -1 = all, otherwise index into Skin.RARITIES
    private final SparseArray<View> cards = new SparseArray<>();

    ShopScreen(MainActivity a) {
        super(a);
        act = a;
        st = a.store;
        build();
        refresh();
    }

    @Override public boolean onBack() {
        act.showMenu();
        return true;
    }

    private void build() {
        Context c = getContext();
        addView(new BgView(c, 0xFF1F1C3B), new LayoutParams(Ui.MATCH, Ui.MATCH));
        LinearLayout col = new LinearLayout(c);
        col.setOrientation(LinearLayout.VERTICAL);
        addView(col, new LayoutParams(Ui.MATCH, Ui.MATCH));

        LinearLayout top = new LinearLayout(c);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(Ui.dp(16), Ui.dp(12), Ui.dp(16), Ui.dp(8));
        top.addView(Ui.roundIcon(c, IconView.BACK, new Runnable() {
            @Override public void run() { act.showMenu(); }
        }), Ui.lin(Ui.dp(44), Ui.dp(44)));
        LinearLayout titles = new LinearLayout(c);
        titles.setOrientation(LinearLayout.VERTICAL);
        titles.addView(Ui.text(c, "SKINS & SHOP", 19, Pal.CREAM, true), Ui.lin(Ui.WRAP, Ui.WRAP));
        sub = Ui.text(c, "", 11, Pal.WHITE54, true);
        titles.addView(sub, Ui.lin(Ui.WRAP, Ui.WRAP, 0, 3, 0, 0));
        top.addView(titles, Ui.lin(0, Ui.WRAP, 1f, 14, 0, 0, 0));
        pill = new CoinPill(c, st.coins);
        top.addView(pill, Ui.lin(Ui.WRAP, Ui.WRAP));
        col.addView(top, Ui.lin(Ui.MATCH, Ui.WRAP));

        HorizontalScrollView hs = new HorizontalScrollView(c);
        hs.setHorizontalScrollBarEnabled(false);
        chips = new LinearLayout(c);
        chips.setPadding(Ui.dp(16), Ui.dp(4), Ui.dp(16), Ui.dp(8));
        hs.addView(chips, new FrameLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
        col.addView(hs, Ui.lin(Ui.MATCH, Ui.WRAP));

        ScrollView sv = new ScrollView(c);
        sv.setVerticalScrollBarEnabled(false);
        sv.setClipToPadding(false);
        sv.setPadding(Ui.dp(10), Ui.dp(4), Ui.dp(10), Ui.dp(24));
        list = new LinearLayout(c);
        list.setOrientation(LinearLayout.VERTICAL);
        sv.addView(list, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        col.addView(sv, Ui.lin(Ui.MATCH, 0, 1f));
    }

    private void buildChips() {
        Context c = getContext();
        chips.removeAllViews();
        String[] names = {"ALL", "COMMON", "RARE", "EPIC", "LEGENDARY"};
        for (int i = 0; i < names.length; i++) {
            final int f = i - 1;
            boolean on = f == filter;
            TextView t = Ui.text(c, names[i], 11, on ? Pal.INK : Pal.CREAM, true);
            t.setLetterSpacing(.1f);
            t.setGravity(Gravity.CENTER);
            t.setPadding(Ui.dp(14), Ui.dp(9), Ui.dp(14), Ui.dp(9));
            t.setBackground(on ? Ui.box(Pal.CREAM, 20) : Ui.box(Pal.PANEL, 20, Pal.WHITE10, 1));
            Ui.press(t);
            Ui.onClick(t, new Runnable() {
                @Override public void run() { filter = f; refresh(); }
            });
            chips.addView(t, Ui.lin(Ui.WRAP, Ui.WRAP, 0, 0, 8, 0));
        }
    }

    private void refresh() {
        Context c = getContext();
        buildChips();
        pill.set(st.coins);
        sub.setText(st.owned.size() + " / " + Skin.ALL.length + " UNLOCKED");
        list.removeAllViews();
        cards.clear();
        ArrayList<Integer> idx = new ArrayList<>();
        for (int i = 0; i < Skin.ALL.length; i++) {
            if (filter < 0 || Skin.ALL[i].rarity.equals(Skin.RARITIES[filter])) idx.add(i);
        }
        for (int k = 0; k < idx.size(); k += 2) {
            LinearLayout row = new LinearLayout(c);
            row.addView(card(idx.get(k)), Ui.lin(0, Ui.WRAP, 1f, 6, 6, 6, 6));
            if (k + 1 < idx.size()) row.addView(card(idx.get(k + 1)), Ui.lin(0, Ui.WRAP, 1f, 6, 6, 6, 6));
            else row.addView(new View(c), Ui.lin(0, 1, 1f, 6, 6, 6, 6));
            list.addView(row, Ui.lin(Ui.MATCH, Ui.WRAP));
        }
    }

    private View card(final int idx) {
        Context c = getContext();
        final Skin s = Skin.ALL[idx];
        boolean has = st.owned.contains(idx), active = st.skin == idx;
        LinearLayout cd = new LinearLayout(c);
        cd.setOrientation(LinearLayout.VERTICAL);
        cd.setGravity(Gravity.CENTER_HORIZONTAL);
        cd.setPadding(Ui.dp(10), Ui.dp(14), Ui.dp(10), Ui.dp(10));
        cd.setBackground(Ui.box(Pal.PANEL, 20, active ? Pal.MINT : Pal.WHITE10, active ? 2 : 1));
        cards.put(idx, cd);

        FrameLayout tile = new FrameLayout(c);
        tile.setBackground(Ui.box(Pal.a(s.color(), .13f), 20));
        TextView em = new TextView(c);
        em.setText(s.emoji);
        em.setTextSize(42);
        em.setIncludeFontPadding(false);
        em.setGravity(Gravity.CENTER);
        tile.addView(em, new LayoutParams(Ui.MATCH, Ui.MATCH, Gravity.CENTER));
        cd.addView(tile, Ui.lin(Ui.dp(78), Ui.dp(78)));

        TextView name = Ui.text(c, s.name, 13, Pal.CREAM, true);
        name.setSingleLine(true);
        cd.addView(name, Ui.lin(Ui.WRAP, Ui.WRAP, 0, 10, 0, 0));
        TextView rar = Ui.text(c, s.rarity.toUpperCase(), 10, s.color(), true);
        rar.setLetterSpacing(.12f);
        cd.addView(rar, Ui.lin(Ui.WRAP, Ui.WRAP, 0, 3, 0, 10));

        final LinearLayout b;
        if (active) {
            b = Ui.btn(c, IconView.CHECK, "EQUIPPED", Pal.a(Pal.MINT, .16f), Pal.MINT, 11, 12, 0, 0);
            b.setClickable(false);
        } else if (has) {
            b = Ui.btn(c, -1, "EQUIP", Pal.PURPLE, 0xFFFFFFFF, 11, 12, 0, 0);
        } else {
            b = Ui.btn(c, -1, "", Pal.MINT, Pal.INK, 11, 12, 0, 0);
            b.removeAllViews();
            b.addView(Ui.coin(c, 13), Ui.lin(Ui.dp(13), Ui.dp(13), 0, 0, 6, 0));
            b.addView(Ui.text(c, String.valueOf(s.price), 12, Pal.INK, true), Ui.lin(Ui.WRAP, Ui.WRAP));
            if (st.coins < s.price) b.setAlpha(.6f);
        }
        if (!active) {
            Ui.onClick(b, new Runnable() {
                @Override public void run() { choose(idx, b); }
            });
        }
        cd.addView(b, Ui.lin(Ui.MATCH, Ui.dp(38)));
        return cd;
    }

    private void choose(int idx, View btn) {
        Skin s = Skin.ALL[idx];
        if (!st.owned.contains(idx)) {
            if (st.coins < s.price) {
                Sfx.error();
                Ui.shake(btn);
                Ui.banner(this, "Not enough coins yet - win more rounds!", Pal.GOLD);
                return;
            }
            st.coins -= s.price;
            st.owned.add(idx);
            Sfx.buy();
        }
        st.skin = idx;
        st.save();
        refresh();
        View v = cards.get(idx);
        if (v != null) Ui.pop(v);
    }
}
