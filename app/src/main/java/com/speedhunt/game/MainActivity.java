package com.speedhunt.game;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {
    Store store;
    private FrameLayout content;
    private View current;
    private FrameLayout sheet;
    private MenuScreen menu;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        Ui.init(this);
        store = new Store(this);
        Sfx.sound = store.sound;
        Sfx.haptics = store.haptics;
        Sfx.init();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Pal.INK);
        content = new MaxWidthLayout(this);
        root.addView(content, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH, Gravity.CENTER_HORIZONTAL));
        setContentView(root);
        showMenu();
    }

    void setBars(int status, int nav) {
        getWindow().setStatusBarColor(status);
        getWindow().setNavigationBarColor(nav);
    }

    // ------------------------------------------------------------------ navigation
    private void swap(final View next) {
        final View old = current;
        current = next;
        content.addView(next, 0, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
        next.setAlpha(0f);
        next.animate().alpha(1f).setDuration(180).start();
        if (old != null) {
            old.animate().alpha(0f).setDuration(180).withEndAction(new Runnable() {
                @Override public void run() { content.removeView(old); }
            }).start();
        }
    }

    void showMenu() {
        closeSheet(false);
        setBars(Pal.BG[store.bg], Pal.INK);
        menu = new MenuScreen(this);
        swap(menu);
    }

    void showShop() {
        closeSheet(false);
        setBars(0xFF1F1C3B, Pal.INK);
        swap(new ShopScreen(this));
    }

    /** Shows the tutorial the first time, then starts the round. */
    void startGame() {
        if (!store.helpSeen) {
            openHelp(new Runnable() {
                @Override public void run() { startGameNow(); }
            });
        } else {
            startGameNow();
        }
    }

    void startGameNow() {
        closeSheet(false);
        store.helpSeen = true;
        store.save();
        setBars(0xFF1F1C3B, Pal.INK);
        swap(new GameScreen(this));
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        if (sheet != null) {
            closeSheet(true);
            return;
        }
        if (current instanceof BackAware && ((BackAware) current).onBack()) return;
        super.onBackPressed();
    }

    // ------------------------------------------------------------------ sheets
    void openSheet(View panel, boolean bottom) {
        closeSheet(false);
        final FrameLayout s = new FrameLayout(this);
        s.setBackgroundColor(0xB3000000);
        s.setClickable(true);
        s.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { closeSheet(true); }
        });
        panel.setClickable(true);
        ScrollView sv = new ScrollView(this);
        sv.setVerticalScrollBarEnabled(false);
        sv.addView(panel, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP,
                bottom ? Gravity.BOTTOM : Gravity.CENTER);
        lp.setMargins(Ui.dp(14), Ui.dp(14), Ui.dp(14), Ui.dp(14));
        s.addView(sv, lp);
        content.addView(s, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
        sheet = s;
        s.setAlpha(0f);
        s.animate().alpha(1f).setDuration(160).start();
        sv.setTranslationY(Ui.dp(40));
        sv.animate().translationY(0).setDuration(240).setInterpolator(new DecelerateInterpolator()).start();
    }

    void closeSheet(boolean animate) {
        if (sheet == null) return;
        final View s = sheet;
        sheet = null;
        if (animate) {
            s.animate().alpha(0f).setDuration(120).withEndAction(new Runnable() {
                @Override public void run() { content.removeView(s); }
            }).start();
        } else {
            content.removeView(s);
        }
    }

    void confirm(String title, String msg, String yes, int yesColor, String no, final Runnable onYes) {
        LinearLayout p = Ui.card(this);
        p.addView(centered(Ui.text(this, title, 20, Pal.CREAM, true)), Ui.lin(Ui.MATCH, Ui.WRAP));
        TextView m = Ui.text(this, msg, 14, Pal.WHITE70, false);
        m.setGravity(Gravity.CENTER);
        p.addView(m, Ui.lin(Ui.MATCH, Ui.WRAP, 0, 10, 0, 20));
        LinearLayout row = new LinearLayout(this);
        LinearLayout n = Ui.btn(this, -1, no, 0x00000000, Pal.CREAM, 13, 14, Pal.WHITE24, 1);
        LinearLayout y = Ui.btn(this, -1, yes, yesColor, 0xFFFFFFFF, 13, 14, 0, 0);
        Ui.onClick(n, new Runnable() { @Override public void run() { closeSheet(true); } });
        Ui.onClick(y, new Runnable() {
            @Override public void run() {
                closeSheet(false);
                onYes.run();
            }
        });
        row.addView(n, Ui.lin(0, Ui.dp(48), 1f));
        row.addView(y, Ui.lin(0, Ui.dp(48), 1f, 10, 0, 0, 0));
        p.addView(row, Ui.lin(Ui.MATCH, Ui.WRAP));
        openSheet(p, false);
    }

    private TextView centered(TextView t) {
        t.setGravity(Gravity.CENTER);
        return t;
    }

    // ------------------------------------------------------------------ settings
    void openSettings() {
        LinearLayout p = Ui.card(this);
        p.addView(centered(Ui.text(this, "SETTINGS", 20, Pal.CREAM, true)), Ui.lin(Ui.MATCH, Ui.WRAP));

        p.addView(Ui.text(this, "MENU BACKGROUND", 11, Pal.WHITE54, true), Ui.lin(Ui.WRAP, Ui.WRAP, 0, 20, 0, 10));
        final LinearLayout swatches = new LinearLayout(this);
        p.addView(swatches, Ui.lin(Ui.MATCH, Ui.WRAP));
        fillSwatches(swatches);

        p.addView(toggleRow("SOUND", store.sound, new java.util.function.Consumer<Boolean>() {
            @Override public void accept(Boolean on) {
                store.sound = on; Sfx.sound = on; store.save();
                if (on) Sfx.tap();
            }
        }), Ui.lin(Ui.MATCH, Ui.WRAP, 0, 18, 0, 0));
        p.addView(toggleRow("VIBRATION", store.haptics, new java.util.function.Consumer<Boolean>() {
            @Override public void accept(Boolean on) { store.haptics = on; Sfx.haptics = on; store.save(); }
        }), Ui.lin(Ui.MATCH, Ui.WRAP, 0, 8, 0, 0));

        LinearLayout help = Ui.btn(this, -1, "HOW TO PLAY", 0x00000000, Pal.CREAM, 13, 14, Pal.WHITE24, 1);
        Ui.onClick(help, new Runnable() {
            @Override public void run() { openHelp(null); }
        });
        p.addView(help, Ui.lin(Ui.MATCH, Ui.dp(48), 0, 18, 0, 0));
        LinearLayout reset = Ui.btn(this, -1, "RESET PROGRESS", 0x00000000, Pal.CORAL, 13, 14, Pal.a(Pal.CORAL, .5f), 1);
        Ui.onClick(reset, new Runnable() {
            @Override public void run() {
                confirm("RESET PROGRESS?", "Coins, skins and records will be deleted from this device.",
                        "RESET", Pal.CORAL, "CANCEL", new Runnable() {
                            @Override public void run() {
                                store.reset();
                                showMenu();
                            }
                        });
            }
        });
        p.addView(reset, Ui.lin(Ui.MATCH, Ui.dp(48), 0, 10, 0, 0));

        TextView foot = Ui.text(this, "SPEED HUNT 1.0 - works offline. Progress is saved on this device.", 11, Pal.WHITE54, false);
        foot.setGravity(Gravity.CENTER);
        p.addView(foot, Ui.lin(Ui.MATCH, Ui.WRAP, 0, 16, 0, 0));
        openSheet(p, true);
    }

    private void fillSwatches(final LinearLayout host) {
        host.removeAllViews();
        for (int i = 0; i < Pal.BG.length; i++) {
            final int idx = i;
            boolean on = store.bg == i;
            LinearLayout cell = new LinearLayout(this);
            cell.setOrientation(LinearLayout.VERTICAL);
            cell.setGravity(Gravity.CENTER_HORIZONTAL);
            View sw = new View(this);
            sw.setBackground(Ui.box(Pal.BG[i], 24, on ? Pal.MINT : Pal.WHITE24, on ? 3 : 1));
            cell.addView(sw, Ui.lin(Ui.dp(48), Ui.dp(48)));
            cell.addView(Ui.text(this, Pal.BG_NAMES[i], 9, on ? Pal.MINT : Pal.WHITE54, true), Ui.lin(Ui.WRAP, Ui.WRAP, 0, 6, 0, 0));
            Ui.press(cell);
            Ui.onClick(cell, new Runnable() {
                @Override public void run() {
                    store.bg = idx;
                    store.save();
                    if (menu != null) menu.applyBg(idx);
                    fillSwatches(host);
                }
            });
            host.addView(cell, Ui.lin(Ui.WRAP, Ui.WRAP, 0, 0, 16, 0));
        }
    }

    private View toggleRow(String label, boolean initial, final java.util.function.Consumer<Boolean> cb) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(Ui.dp(16), 0, Ui.dp(10), 0);
        row.setBackground(Ui.box(Pal.a(0xFF000000, .2f), 14));
        row.addView(Ui.text(this, label, 13, Pal.CREAM, true), Ui.lin(0, Ui.WRAP, 1f));
        final TextView pill = Ui.text(this, "", 12, Pal.INK, true);
        pill.setGravity(Gravity.CENTER);
        final boolean[] state = {initial};
        applyToggle(pill, state[0]);
        row.addView(pill, Ui.lin(Ui.dp(64), Ui.dp(34), 0, 8, 0, 8));
        Ui.press(row);
        Ui.onClick(row, new Runnable() {
            @Override public void run() {
                state[0] = !state[0];
                applyToggle(pill, state[0]);
                cb.accept(state[0]);
            }
        });
        return row;
    }

    private void applyToggle(TextView pill, boolean on) {
        pill.setText(on ? "ON" : "OFF");
        pill.setTextColor(on ? Pal.INK : Pal.WHITE70);
        pill.setBackground(Ui.box(on ? Pal.MINT : Pal.WHITE10, 17));
    }

    // ------------------------------------------------------------------ help
    void openHelp(final Runnable after) {
        LinearLayout p = Ui.card(this);
        p.addView(centered(Ui.text(this, "HOW TO PLAY", 20, Pal.CREAM, true)), Ui.lin(Ui.MATCH, Ui.WRAP, 0, 0, 0, 14));
        String[][] steps = {
            {"TAP A HIDEOUT", "Pick one of the glowing buildings. Your character runs there and hides."},
            {"THE HUNTER PICKS", "The hunter checks a random hideout. If it is yours, you are caught."},
            {"SURVIVE 4 PINGS", "Every empty spot the hunter checks disappears. Survive all four pings to escape."},
            {"EARN COINS", "1 ping = 2, 2 pings = 25, 3 pings = 50, full escape = 100 coins. Spend them on skins."},
        };
        for (int i = 0; i < steps.length; i++) {
            LinearLayout row = new LinearLayout(this);
            TextView num = Ui.text(this, String.valueOf(i + 1), 14, Pal.INK, true);
            num.setGravity(Gravity.CENTER);
            num.setBackground(Ui.box(Pal.MINT, 15));
            row.addView(num, Ui.lin(Ui.dp(30), Ui.dp(30), 0, 2, 12, 0));
            LinearLayout txt = new LinearLayout(this);
            txt.setOrientation(LinearLayout.VERTICAL);
            txt.addView(Ui.text(this, steps[i][0], 13, Pal.CREAM, true), Ui.lin(Ui.WRAP, Ui.WRAP));
            txt.addView(Ui.text(this, steps[i][1], 13, Pal.WHITE70, false), Ui.lin(Ui.WRAP, Ui.WRAP, 0, 3, 0, 0));
            row.addView(txt, Ui.lin(0, Ui.WRAP, 1f));
            p.addView(row, Ui.lin(Ui.MATCH, Ui.WRAP, 0, 0, 0, 14));
        }
        LinearLayout ok = Ui.btn(this, -1, after != null ? "GOT IT - START" : "GOT IT", Pal.PURPLE, 0xFFFFFFFF, 15, 16, 0, 0);
        Ui.onClick(ok, new Runnable() {
            @Override public void run() {
                closeSheet(false);
                if (after != null) after.run();
            }
        });
        p.addView(ok, Ui.lin(Ui.MATCH, Ui.dp(54), 0, 4, 0, 0));
        openSheet(p, true);
    }
}
