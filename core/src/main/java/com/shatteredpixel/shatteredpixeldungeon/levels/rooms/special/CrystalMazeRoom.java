package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;

public class CrystalMazeRoom extends SpecialRoom {

    @Override
    public void paint(Level level) {
        Painter.fill(level, this, Terrain.WALL);
        Painter.fill(level, this, 1, Terrain.EMPTY_SP);

        int innerLeft = left + 2;
        int innerTop = top + 2;
        int innerWidth = width() - 4;
        int innerHeight = height() - 4;

        if (innerWidth > 2 && innerHeight > 2) {
            Painter.fill(level, innerLeft, innerTop, innerWidth, innerHeight, Terrain.REGION_DECO);
            Painter.fill(level, innerLeft + 1, innerTop + 1, innerWidth - 2, innerHeight - 2, Terrain.EMPTY_SP);
        }

        for (Door door : connected.values()) {
            door.set(Door.Type.UNLOCKED);
        }
    }
}
