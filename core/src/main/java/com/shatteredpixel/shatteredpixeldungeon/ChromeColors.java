package com.shatteredpixel.shatteredpixeldungeon;

import com.watabou.noosa.Visual;
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

    public static void tint(Visual v) {
        if (v == null) return;
        int c = color();
        if (c == -1) {
            v.resetColor();
        } else {
            v.hardlight(c);
        }
    }

    public static void shade(Visual v, float b) {
        if (v == null) return;
        int c = color();
        if (c == -1) {
            v.brightness(b);
        } else {
            float r = ((c >> 16) & 0xFF) / 255f;
            float g = ((c >> 8) & 0xFF) / 255f;
            float bl = (c & 0xFF) / 255f;
            v.hardlight(r * b, g * b, bl * b);
        }
    }
}
