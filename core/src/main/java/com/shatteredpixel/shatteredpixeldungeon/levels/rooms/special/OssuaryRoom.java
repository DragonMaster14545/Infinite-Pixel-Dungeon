package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special;

import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.watabou.utils.Random;

public class OssuaryRoom extends SpecialRoom {

    @Override
    public int minWidth() { return 7; }

    @Override
    public int minHeight() { return 7; }

    @Override
    public void paint(Level level) {
        Painter.fill(level, this, Terrain.WALL);
        Painter.fill(level, this, 1, Terrain.EMPTY);

        Door d = entrance();
        int doorInside = -1;
        {
            int ix = d.x;
            int iy = d.y;
            if (d.x == left) ix++;
            else if (d.x == right) ix--;
            else if (d.y == top) iy++;
            else iy--;
            doorInside = level.pointToCell(new com.watabou.utils.Point(ix, iy));
        }

        int n = Random.IntRange(3, 4);
        for (int i = 0; i < n; i++) {
            int pos = -1;
            for (int tries = 0; tries < 40; tries++) {
                int p = RoomLoot.freeCell(level, this, 1, Terrain.EMPTY);
                if (p != -1 && !level.adjacent(p, doorInside) && p != doorInside) {
                    pos = p;
                    break;
                }
            }
            if (pos == -1) continue;
            level.drop(RoomLoot.roll(Generator.Category.WEAPON, Generator.Category.ARMOR, Generator.Category.RING), pos).type = Heap.Type.TOMB;
        }

        d.set(Door.Type.REGULAR);
    }
}
