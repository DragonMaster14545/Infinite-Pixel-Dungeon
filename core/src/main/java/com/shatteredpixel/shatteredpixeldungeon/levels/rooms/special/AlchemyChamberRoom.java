package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

public class AlchemyChamberRoom extends SpecialRoom {

    @Override
    public void paint(Level level) {
        Painter.fill(level, this, Terrain.WALL);
        Painter.fill(level, this, 1, Terrain.EMPTY_SP);

        int innerLeft = left + 1;
        int innerTop = top + 1;
        int innerRight = right - 1;
        int innerBottom = bottom - 1;

        for (int x = innerLeft; x <= innerRight; x++) {
            if (Random.Int(3) == 0) {
                Painter.set(level, new Point(x, innerTop), Terrain.ALCHEMY);
            }
            if (Random.Int(3) == 0) {
                Painter.set(level, new Point(x, innerBottom), Terrain.ALCHEMY);
            }
        }
        for (int y = innerTop + 1; y <= innerBottom - 1; y++) {
            if (Random.Int(3) == 0) {
                Painter.set(level, new Point(innerLeft, y), Terrain.ALCHEMY);
            }
            if (Random.Int(3) == 0) {
                Painter.set(level, new Point(innerRight, y), Terrain.ALCHEMY);
            }
        }

        int count = Random.IntRange(1, 3);
        for (int i = 0; i < count; i++) {
            Point pos = random();
            Painter.set(level, pos, Terrain.ALCHEMY);
        }

        entrance().set(Door.Type.REGULAR);
    }
}
