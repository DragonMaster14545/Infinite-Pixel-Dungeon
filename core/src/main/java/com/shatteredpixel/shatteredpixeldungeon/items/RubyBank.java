package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;

import java.math.BigInteger;

public class RubyBank {

    private static BigInteger cache = null;

    public static BigInteger get() {
        if (cache == null) cache = SPDSettings.ruby();
        return cache;
    }

    public static void set(BigInteger value) {
        cache = value.max(BigInteger.ZERO);
        SPDSettings.ruby(cache);
    }

    public static void add(BigInteger amount) {
        set(get().add(amount));
    }

    public static boolean spend(BigInteger cost) {
        if (get().compareTo(cost) < 0) return false;
        set(get().subtract(cost));
        return true;
    }

    public static void reload() {
        cache = null;
    }
}
