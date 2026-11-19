package com.speedhunt.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Pure game rules (no Android classes, so it can be tested anywhere). */
final class GameLogic {
    static final int SPOTS = 5;
    static final int PINGS = 4;
    static final int WIN_REWARD = 100;

    private final Random rng;
    private final boolean[] available = new boolean[SPOTS];
    int ping = 1;
    boolean finished, won;
    int reward;

    GameLogic(Random rng) {
        this.rng = rng;
        for (int i = 0; i < SPOTS; i++) available[i] = true;
    }

    boolean isAvailable(int i) { return i >= 0 && i < SPOTS && available[i]; }

    int availableCount() {
        int n = 0;
        for (boolean b : available) if (b) n++;
        return n;
    }

    /** Hunter picks one of the remaining spots uniformly at random. */
    int pickTarget() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < SPOTS; i++) if (available[i]) list.add(i);
        return list.get(rng.nextInt(list.size()));
    }

    /** @return true if the player was caught. */
    boolean resolve(int playerPick, int hunterTarget) {
        if (playerPick == hunterTarget) {
            finished = true;
            won = false;
            reward = lossReward(ping - 1);
            return true;
        }
        available[hunterTarget] = false;
        if (ping == PINGS) {
            finished = true;
            won = true;
            reward = WIN_REWARD;
        } else {
            ping++;
        }
        return false;
    }

    int survived() { return won ? PINGS : ping - 1; }

    static int lossReward(int survived) {
        if (survived >= 3) return 50;
        if (survived >= 2) return 25;
        if (survived >= 1) return 2;
        return 0;
    }
}
