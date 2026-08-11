package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.watabou.utils.Point;

public class SpiralRoom extends StandardRoom {

    @Override
    public void paint(Level level) {
        Painter.fill(level, this, Terrain.WALL);
        Painter.fill(level, this, 1, Terrain.EMPTY_SP);

        int inset = 1;
        while (left + inset < right - inset && top + inset < bottom - inset) {
            Painter.fill(level, left + inset, top + inset, right - left - inset * 2 + 1, 1, Terrain.REGION_DECO);
            Painter.fill(level, left + inset, top + inset, 1, bottom - top - inset * 2 + 1, Terrain.REGION_DECO);
            Painter.fill(level, left + inset, bottom - inset, right - left - inset * 2 + 1, 1, Terrain.REGION_DECO);
            Painter.fill(level, right - inset, top + inset, 1, bottom - top - inset * 2 + 1, Terrain.REGION_DECO);
            inset += 2;
        }

        for (Door door : connected.values()) {
            door.set(Door.Type.REGULAR);
        }
    }
}
