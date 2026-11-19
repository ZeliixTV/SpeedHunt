package com.speedhunt.game;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Random;

/** One round of SPEED HUNT. */
final class GameScreen extends FrameLayout implements BackAware {
    private final MainActivity act;
    private final Store st;
    private final Skin skin;
    private final GameLogic logic = new GameLogic(new Random());
    private final Handler h = new Handler(Looper.getMainLooper());
    private MapView map;
    private TextView pingTxt, spotsTxt, status, hint;
    private final View[] segs = new View[GameLogic.PINGS];
    private boolean busy, done, gone;

    GameScreen(MainActivity a) {
        super(a);
        act = a;
        st = a.store;
        skin = Skin.ALL[st.skin];
        build();
        updateHud();
    }

    private void build() {
        Context c = getContext();
        addView(new BgView(c, 0xFF1F1C3B), new LayoutParams(Ui.MATCH, Ui.MATCH));
        LinearLayout col = new LinearLayout(c);
        col.setOrientation(LinearLayout.VERTICAL);
        addView(col, new LayoutParams(Ui.MATCH, Ui.MATCH));

        LinearLayout top = new LinearLayout(c);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(Ui.dp(16), Ui.dp(12), Ui.dp(16), Ui.dp(6));
        top.addView(Ui.roundIcon(c, IconView.CLOSE, new Runnable() {
            @Override public void run() { onBack(); }
        }), Ui.lin(Ui.dp(44), Ui.dp(44)));
        TextView t = Ui.text(c, "HIDE & SEEK", 18, Pal.CREAM, true);
        t.setGravity(Gravity.CENTER);
        top.addView(t, Ui.lin(0, Ui.WRAP, 1f));
        pingTxt = Ui.text(c, "", 14, Pal.MINT, true);
        pingTxt.setLetterSpacing(.08f);
        pingTxt.setMinWidth(Ui.dp(70));
        pingTxt.setGravity(Gravity.END);
        top.addView(pingTxt, Ui.lin(Ui.WRAP, Ui.WRAP));
        col.addView(top, Ui.lin(Ui.MATCH, Ui.WRAP));

        LinearLayout legend = new LinearLayout(c);
        legend.setGravity(Gravity.CENTER_VERTICAL);
        legend.setPadding(Ui.dp(20), Ui.dp(4), Ui.dp(20), Ui.dp(10));
        legend.addView(dot(c, Pal.MINT), Ui.lin(Ui.dp(9), Ui.dp(9), 0, 0, 5, 0));
        legend.addView(Ui.text(c, "YOU", 10, Pal.WHITE70, true), Ui.lin(Ui.WRAP, Ui.WRAP, 0, 0, 16, 0));
        legend.addView(dot(c, Pal.CORAL), Ui.lin(Ui.dp(9), Ui.dp(9), 0, 0, 5, 0));
        legend.addView(Ui.text(c, "HUNTER", 10, Pal.WHITE70, true), Ui.lin(Ui.WRAP, Ui.WRAP));
        legend.addView(Ui.spacer(c, 1f));
        spotsTxt = Ui.text(c, "", 10, Pal.WHITE54, true);
        legend.addView(spotsTxt, Ui.lin(Ui.WRAP, Ui.WRAP));
        col.addView(legend, Ui.lin(Ui.MATCH, Ui.WRAP));

        map = new MapView(c);
        map.setSkin(skin.emoji);
        map.setInteractive(true);
        map.setListener(new MapView.Listener() {
            @Override public void onSpot(int i) { pick(i); }
        });
        col.addView(map, Ui.lin(Ui.MATCH, 0, 1f, 14, 0, 14, 0));

        LinearLayout bottom = new LinearLayout(c);
        bottom.setOrientation(LinearLayout.VERTICAL);
        bottom.setGravity(Gravity.CENTER_HORIZONTAL);
        bottom.setPadding(Ui.dp(18), Ui.dp(12), Ui.dp(18), Ui.dp(18));
        status = Ui.text(c, "", 17, Pal.CREAM, true);
        status.setGravity(Gravity.CENTER);
        bottom.addView(status, Ui.lin(Ui.MATCH, Ui.WRAP));
        hint = Ui.text(c, "", 12, Pal.WHITE70, false);
        hint.setGravity(Gravity.CENTER);
        hint.setMinLines(2);
        bottom.addView(hint, Ui.lin(Ui.MATCH, Ui.WRAP, 0, 6, 0, 12));
        LinearLayout bars = new LinearLayout(c);
        for (int i = 0; i < GameLogic.PINGS; i++) {
            segs[i] = new View(c);
            bars.addView(segs[i], Ui.lin(0, Ui.dp(5), 1f, 3, 0, 3, 0));
        }
        bottom.addView(bars, Ui.lin(Ui.MATCH, Ui.WRAP));
        col.addView(bottom, Ui.lin(Ui.MATCH, Ui.WRAP));

        setStatus("CHOOSE A HIDEOUT", Pal.CREAM,
                "Tap one of the glowing locations. Your character runs there automatically.");
    }

    private View dot(Context c, int color) {
        View v = new View(c);
        v.setBackground(Ui.box(color, 5));
        return v;
    }

    private void setStatus(String s, int color, String hintText) {
        status.setText(s);
        status.setTextColor(color);
        hint.setText(hintText);
    }

    private void updateHud() {
        pingTxt.setText("PING " + logic.ping + " / " + GameLogic.PINGS);
        int n = logic.availableCount();
        spotsTxt.setText(n + (n == 1 ? " SPOT LEFT" : " SPOTS LEFT"));
        for (int i = 0; i < segs.length; i++) {
            segs[i].setBackground(Ui.box(i < logic.ping ? Pal.MINT : Pal.WHITE10, 4));
        }
    }

    // ------------------------------------------------------------------ round flow
    private void pick(final int spot) {
        if (busy || done || gone || !logic.isAvailable(spot)) return;
        busy = true;
        map.setInteractive(false);
        Sfx.click(this);
        Sfx.move();
        setStatus("YOU HIDE AT " + MapView.LABEL[spot], Pal.MINT, "Stay quiet...");
        map.movePlayer(spot, 600, new Runnable() {
            @Override public void run() {
                if (gone) return;
                setStatus("THE HUNTER IS MOVING...", Pal.CORAL, "Watch the red hunter travel to a random location.");
                final int target = logic.pickTarget();
                h.postDelayed(new Runnable() {
                    @Override public void run() {
                        if (gone) return;
                        Sfx.move();
                        map.moveHunter(target, 1150, new Runnable() {
                            @Override public void run() { reveal(spot, target); }
                        });
                    }
                }, 300);
            }
        });
    }

    private void reveal(int spot, int target) {
        if (gone) return;
        boolean caught = logic.resolve(spot, target);
        if (caught) {
            map.caught();
            Sfx.caught();
            Sfx.haptic(this, true);
            finish(false);
            return;
        }
        map.killSpot(target);
        Sfx.safe();
        if (logic.finished) {
            finish(true);
            return;
        }
        setStatus("SAFE! NOBODY HOME", Pal.MINT, "The hunter checked an empty spot - it's gone for good.");
        updateHud();
        h.postDelayed(new Runnable() {
            @Override public void run() {
                if (gone) return;
                busy = false;
                map.setInteractive(true);
                setStatus("CHOOSE A HIDEOUT", Pal.CREAM, "Pick your next hiding spot. You can stay where you are.");
            }
        }, 1000);
    }

    private void finish(final boolean won) {
        done = true;
        final int survived = logic.survived();
        final boolean newBest = survived > st.best;
        final int reward = logic.reward;
        st.finishRound(reward, survived, won);
        updateHud();
        if (won) {
            map.celebrate();
            Sfx.win();
            setStatus("YOU ESCAPED!", Pal.MINT, "All four pings survived. Perfect run!");
        } else {
            setStatus("THE HUNTER FOUND YOU", Pal.CORAL, "You survived " + survived + " ping" + (survived == 1 ? "" : "s") + ".");
        }
        h.postDelayed(new Runnable() {
            @Override public void run() { if (!gone) showResult(won, survived, reward, newBest); }
        }, won ? 1500 : 1200);
    }

    private void showResult(boolean won, int survived, int reward, boolean newBest) {
        Context c = getContext();
        final FrameLayout scrim = new FrameLayout(c);
        scrim.setBackgroundColor(0xB3000000);
        scrim.setClickable(true);

        LinearLayout card = Ui.card(c);
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        TextView em = new TextView(c);
        em.setText(won ? "🎉" : skin.emoji);
        em.setTextSize(54);
        em.setIncludeFontPadding(false);
        card.addView(em, Ui.lin(Ui.WRAP, Ui.WRAP));
        TextView title = Ui.text(c, won ? "PERFECT ESCAPE!" : "CAUGHT!", 26, won ? Pal.MINT : Pal.CORAL, true);
        card.addView(title, Ui.lin(Ui.WRAP, Ui.WRAP, 0, 10, 0, 6));
        TextView msg = Ui.text(c, won ? "You survived all four pings."
                : "You survived " + survived + " ping" + (survived == 1 ? "" : "s") + ".", 14, Pal.WHITE70, false);
        card.addView(msg, Ui.lin(Ui.WRAP, Ui.WRAP));
        if (newBest) {
            TextView nb = Ui.text(c, "NEW PERSONAL BEST", 10, Pal.INK, true);
            nb.setLetterSpacing(.12f);
            nb.setPadding(Ui.dp(12), Ui.dp(5), Ui.dp(12), Ui.dp(5));
            nb.setBackground(Ui.box(Pal.GOLD, 12));
            card.addView(nb, Ui.lin(Ui.WRAP, Ui.WRAP, 0, 12, 0, 0));
        }

        LinearLayout rw = new LinearLayout(c);
        rw.setGravity(Gravity.CENTER);
        rw.setPadding(Ui.dp(16), Ui.dp(12), Ui.dp(16), Ui.dp(12));
        rw.setBackground(Ui.box(Pal.a(0xFF000000, .22f), 18));
        rw.addView(Ui.coin(c, 24), Ui.lin(Ui.dp(24), Ui.dp(24), 0, 0, 10, 0));
        final TextView amount = Ui.text(c, "+0", 26, Pal.GOLD, true);
        rw.addView(amount, Ui.lin(Ui.WRAP, Ui.WRAP));
        card.addView(rw, Ui.lin(Ui.WRAP, Ui.WRAP, 0, 16, 0, 18));
        if (reward > 0) {
            ValueAnimator va = ValueAnimator.ofInt(0, reward);
            va.setDuration(Math.min(1200, 300 + reward * 12));
            va.setStartDelay(250);
            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override public void onAnimationUpdate(ValueAnimator a) {
                    amount.setText("+" + a.getAnimatedValue());
                }
            });
            va.start();
        }

        LinearLayout again = Ui.btn(c, IconView.PLAY, "PLAY AGAIN", Pal.PURPLE, 0xFFFFFFFF, 15, 16, 0, 0);
        Ui.onClick(again, new Runnable() { @Override public void run() { act.startGameNow(); } });
        card.addView(again, Ui.lin(Ui.MATCH, Ui.dp(54)));
        LinearLayout menu = Ui.btn(c, -1, "MAIN MENU", 0x00000000, Pal.CREAM, 13, 16, Pal.WHITE24, 1);
        Ui.onClick(menu, new Runnable() { @Override public void run() { act.showMenu(); } });
        card.addView(menu, Ui.lin(Ui.MATCH, Ui.dp(48), 0, 10, 0, 0));

        LayoutParams lp = new LayoutParams(Ui.MATCH, Ui.WRAP, Gravity.CENTER);
        lp.setMargins(Ui.dp(22), 0, Ui.dp(22), 0);
        scrim.addView(card, lp);
        addView(scrim, new LayoutParams(Ui.MATCH, Ui.MATCH));
        scrim.setAlpha(0f);
        scrim.animate().alpha(1f).setDuration(220).start();
        Ui.pop(card);
    }

    @Override public boolean onBack() {
        if (done) {
            act.showMenu();
            return true;
        }
        act.confirm("LEAVE ROUND?", "You will lose this round's reward.", "LEAVE", Pal.CORAL, "STAY",
                new Runnable() { @Override public void run() { act.showMenu(); } });
        return true;
    }

    @Override protected void onDetachedFromWindow() {
        gone = true;
        h.removeCallbacksAndMessages(null);
        super.onDetachedFromWindow();
    }
}
