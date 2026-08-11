package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

public class SpiralRoom extends StandardRoom {

    @Override
    public void paint(Level level) {
        Painter.fill(level, this, Terrain.WALL);
        Painter.fill(level, this, 1, Terrain.EMPTY_SP);

        int inset = 1;
        boolean gapOnLeft = Random.Int(2) == 0;
        while (left + inset < right - inset && top + inset < bottom - inset) {
            int ringLeft = left + inset;
            int ringTop = top + inset;
            int ringRight = right - inset;
            int ringBottom = bottom - inset;
            int ringWidth = ringRight - ringLeft + 1;
            int ringHeight = ringBottom - ringTop + 1;

            Painter.fill(level, ringLeft, ringTop, ringWidth, 1, Terrain.REGION_DECO);
            Painter.fill(level, ringLeft, ringTop, 1, ringHeight, Terrain.REGION_DECO);
            Painter.fill(level, ringLeft, ringBottom, ringWidth, 1, Terrain.REGION_DECO);
            Painter.fill(level, ringRight, ringTop, 1, ringHeight, Terrain.REGION_DECO);

            if (ringHeight > 2) {
                int gapX = gapOnLeft ? ringLeft : ringRight;
                int gapY = ringTop + 1 + Random.Int(ringHeight - 2);
                Painter.set(level, new Point(gapX, gapY), Terrain.EMPTY_SP);
                gapOnLeft = !gapOnLeft;
            }

            inset += 2;
        }

        for (Door door : connected.values()) {
            door.set(Door.Type.REGULAR);
        }
    }
}
