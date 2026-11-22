package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import java.math.BigInteger;

public class RubyRewards {

    public static final int dailyBase = 10;
    public static final int dailyPerChallenge = 5;
    public static final int dailyBaseChallenge = 15;
    public static final int weeklyBase = 40;
    public static final int weeklyChallenge = 7;
    public static final int challenged = 3;
    public static final int monthlyBase = 150;
    public static final int monthlyChallenge = 10;

    public static int lastReward = 0;

    public static int compute() {
        int n = Challenges.activeChallenges();
        if (Dungeon.monthly) {
            if (Dungeon.monthlyReplay) return 0;
            return monthlyBase + (monthlyChallenge * n);
        }
        if (Dungeon.weekly && !Dungeon.weeklyReplay) {
            return weeklyBase + (weeklyChallenge * n);
        }
        if (Dungeon.daily && !Dungeon.dailyReplay) {
            if (n > 0) return dailyBaseChallenge + (dailyPerChallenge * n);
            return dailyBase;
        }
        if (Dungeon.weekly || Dungeon.daily) return 0;
        return challenged * n;
    }

    public static void award() {
        lastReward = 0;
        if (Dungeon.practice) return;
        int amount = compute();
        if (amount <= 0) return;
        RubyBank.add(BigInteger.valueOf(amount));
        lastReward = amount;
        GLog.p(Messages.get(RubyRewards.class, "reward", amount));
    }
}
