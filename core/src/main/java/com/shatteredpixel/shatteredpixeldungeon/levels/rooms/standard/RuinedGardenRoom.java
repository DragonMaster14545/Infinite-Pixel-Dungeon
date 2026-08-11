package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;

public class RuinedGardenRoom extends StandardRoom {

    @Override
    public void paint(Level level) {
        Painter.fill(level, this, Terrain.WALL);
        Painter.fill(level, this, 1, Terrain.GRASS);

        if (width() > 6 && height() > 6) {
            Painter.fill(level, left + 2, top + 2, width() - 4, height() - 4, Terrain.REGION_DECO);
        }

        for (Door door : connected.values()) {
            door.set(Door.Type.REGULAR);
        }
    }
}
