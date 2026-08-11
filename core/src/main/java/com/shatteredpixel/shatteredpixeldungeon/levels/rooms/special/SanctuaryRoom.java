package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.watabou.utils.Point;

public class SanctuaryRoom extends SpecialRoom {

    @Override
    public void paint(Level level) {
        Painter.fill(level, this, Terrain.WALL);
        Painter.fill(level, this, 1, Terrain.EMPTY_SP);

        Point center = center();
        Painter.fill(level, center.x - 1, center.y - 1, 3, 3, Terrain.REGION_DECO);
        Painter.set(level, center, Terrain.STATUE_SP);

        int px = 2;
        int py = 1;
        Point[] pillars = {
                new Point(center.x - px, center.y - py),
                new Point(center.x + px, center.y - py),
                new Point(center.x - px, center.y + py),
                new Point(center.x + px, center.y + py),
        };
        for (Point p : pillars) {
            if (p.x > left && p.x < right && p.y > top && p.y < bottom) {
                Painter.set(level, p, Terrain.REGION_DECO_ALT);
            }
        }

        entrance().set(Door.Type.LOCKED);
    }
}
