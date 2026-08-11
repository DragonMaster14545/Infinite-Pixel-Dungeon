package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.watabou.utils.Point;

public class ObservatoryRoom extends StandardRoom {

    @Override
    public void paint(Level level) {
        Painter.fill(level, this, Terrain.WALL);
        Painter.fill(level, this, 1, Terrain.EMPTY_SP);

        Point center = center();
        Painter.fill(level, center.x - 1, center.y - 1, 3, 3, Terrain.REGION_DECO_ALT);
        Painter.set(level, center, Terrain.STATUE_SP);

        for (Door door : connected.values()) {
            door.set(Door.Type.REGULAR);
        }
    }
}
