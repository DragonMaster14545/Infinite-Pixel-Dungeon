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

        int count = Random.IntRange(1, 4);
        for (int i = 0; i < count; i++) {
            Point pos = random();
            Painter.set(level, pos, Terrain.ALCHEMY);
        }

        entrance().set(Door.Type.REGULAR);
    }
}
