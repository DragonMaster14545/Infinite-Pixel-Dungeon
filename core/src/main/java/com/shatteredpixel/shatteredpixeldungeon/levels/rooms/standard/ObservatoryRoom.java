package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.watabou.utils.Point;

public class ObservatoryRoom extends StandardRoom {

    @Override
    public void paint(Level level) {
        Painter.fill(level, this, Terrain.WALL);
        Painter.fill(level, this, 1, Terrain.EMPTY_SP);

        Point center = center();

        for (int r = 3; r >= 1; r -= 2) {
            int rl = center.x - r;
            int rt = center.y - r;
            int rr = center.x + r;
            int rb = center.y + r;
            if (rl > left && rt > top && rr < right && rb < bottom) {
                Painter.fill(level, rl, rt, rr - rl + 1, 1, Terrain.REGION_DECO_ALT);
                Painter.fill(level, rl, rt, 1, rb - rt + 1, Terrain.REGION_DECO_ALT);
                Painter.fill(level, rl, rb, rr - rl + 1, 1, Terrain.REGION_DECO_ALT);
                Painter.fill(level, rr, rt, 1, rb - rt + 1, Terrain.REGION_DECO_ALT);
            }
        }

        Painter.fill(level, center.x - 1, center.y - 1, 3, 3, Terrain.REGION_DECO_ALT);
        Painter.set(level, center, Terrain.STATUE_SP);

        for (Door door : connected.values()) {
            door.set(Door.Type.REGULAR);
        }
    }
}
