package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

public class SunkenChamberRoom extends StandardRoom {

    @Override
    public void paint(Level level) {
        Painter.fill(level, this, Terrain.WALL);
        Painter.fill(level, this, 1, Terrain.EMPTY_SP);

        if (width() > 4 && height() > 4) {
            Painter.fill(level, left + 1, top + 1, width() - 2, height() - 2, Terrain.WATER);
            Painter.fill(level, left + 2, top + 2, width() - 4, height() - 4, Terrain.EMPTY_SP);

            Point[] corners = {
                    new Point(left + 1, top + 1),
                    new Point(right - 1, top + 1),
                    new Point(left + 1, bottom - 1),
                    new Point(right - 1, bottom - 1),
            };
            for (Point c : corners) {
                if (Random.Int(2) == 0) {
                    Painter.set(level, c, Terrain.WALL_DECO);
                }
            }
        }

        for (Door door : connected.values()) {
            door.set(Door.Type.REGULAR);
        }
    }
}
