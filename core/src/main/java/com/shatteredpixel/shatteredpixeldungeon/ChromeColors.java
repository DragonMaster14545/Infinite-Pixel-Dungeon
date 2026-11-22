package com.shatteredpixel.shatteredpixeldungeon;

import com.watabou.utils.GameSettings;

public class ChromeColors {

    public static final String KEY = "chrome_color";

    public static final int[] COLORS = {
            -1,
            0xFFE066,
            0xD0D8E0,
            0xFF6677,
            0xFFA040,
            0x66E08C,
            0x55DCD4,
            0x6A95FF,
            0xB07BFF,
            0xFF88C8,
            0x808890
    };

    public static int index() {
        return GameSettings.getInt(KEY, 0, 0, COLORS.length - 1);
    }

    public static void index(int value) {
        GameSettings.put(KEY, Math.max(0, Math.min(COLORS.length - 1, value)));
    }

    public static int color() {
        return COLORS[index()];
    }

    public static int labelColor(int i) {
        return COLORS[i] == -1 ? 0xFFFFFF : COLORS[i];
    }
}
