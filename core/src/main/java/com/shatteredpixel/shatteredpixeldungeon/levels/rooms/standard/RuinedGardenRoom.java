package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

public class RuinedGardenRoom extends StandardRoom {

    @Override
    public void paint(Level level) {
        Painter.fill(level, this, Terrain.WALL);
        Painter.fill(level, this, 1, Terrain.GRASS);

        if (width() > 4 && height() > 4) {
            int patchCount = Random.IntRange(3, 6);
            for (int i = 0; i < patchCount; i++) {
                int x = left + 2 + Random.Int(Math.max(1, width() - 4));
                int y = top + 2 + Random.Int(Math.max(1, height() - 4));
                int size = Random.Int(2) + 1;
                Painter.fill(level, x, y, size, size, Terrain.REGION_DECO);
            }

            int tuftCount = Random.IntRange(2, 4);
            for (int i = 0; i < tuftCount; i++) {
                Point p = random();
                Painter.set(level, p, Terrain.HIGH_GRASS);
            }
        }

        for (Door door : connected.values()) {
            door.set(Door.Type.REGULAR);
        }
    }
}
