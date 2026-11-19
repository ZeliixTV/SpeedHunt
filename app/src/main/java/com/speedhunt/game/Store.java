package com.speedhunt.game;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;

/** Local save data (works fully offline, stored on the device). */
final class Store {
    private final SharedPreferences p;
    int coins, skin, best, bg, games, wins;
    boolean sound, haptics, helpSeen;
    final Set<Integer> owned = new HashSet<>();

    Store(Context c) {
        p = c.getSharedPreferences("speed_hunt", Context.MODE_PRIVATE);
        load();
    }

    void load() {
        coins = Math.max(0, p.getInt("coins", 0));
        skin = p.getInt("skin", 0);
        best = Math.max(0, Math.min(GameLogic.PINGS, p.getInt("best", 0)));
        bg = p.getInt("bg", 0);
        games = p.getInt("games", 0);
        wins = p.getInt("wins", 0);
        sound = p.getBoolean("sound", true);
        haptics = p.getBoolean("haptics", true);
        helpSeen = p.getBoolean("help", false);
        owned.clear();
        String s = p.getString("owned", "0,1");
        for (String part : s.split(",")) {
            try {
                int i = Integer.parseInt(part.trim());
                if (i >= 0 && i < Skin.ALL.length) owned.add(i);
            } catch (NumberFormatException ignored) { }
        }
        owned.add(0);
        owned.add(1);
        if (skin < 0 || skin >= Skin.ALL.length || !owned.contains(skin)) skin = 0;
        bg = ((bg % 3) + 3) % 3;
    }

    void save() {
        StringBuilder sb = new StringBuilder();
        for (int i : owned) {
            if (sb.length() > 0) sb.append(',');
            sb.append(i);
        }
        p.edit().putInt("coins", coins).putInt("skin", skin).putInt("best", best)
                .putInt("bg", bg).putInt("games", games).putInt("wins", wins)
                .putBoolean("sound", sound).putBoolean("haptics", haptics)
                .putBoolean("help", helpSeen).putString("owned", sb.toString()).apply();
    }

    void finishRound(int reward, int survived, boolean won) {
        coins += reward;
        games++;
        if (won) wins++;
        if (survived > best) best = survived;
        save();
    }

    void reset() {
        coins = 0; skin = 0; best = 0; bg = 0; games = 0; wins = 0; helpSeen = false;
        owned.clear();
        owned.add(0);
        owned.add(1);
        save();
    }
}
