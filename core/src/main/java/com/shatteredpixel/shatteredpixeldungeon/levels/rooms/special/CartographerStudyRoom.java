package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special;

import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.watabou.utils.Point;

public class CartographerStudyRoom extends SpecialRoom {

    @Override
    public int minWidth() { return 6; }

    @Override
    public int minHeight() { return 6; }

    @Override
    public void paint(Level level) {
        Painter.fill(level, this, Terrain.WALL);
        Painter.fill(level, this, 1, Terrain.EMPTY_SP);

        Point c = center();
        int cc = level.pointToCell(c);
        Painter.set(level, cc, Terrain.PEDESTAL);
        level.drop(new ScrollOfMagicMapping(), cc).type = Heap.Type.CHEST;

        for (int i = 0; i < 3; i++) {
            int pos = RoomLoot.freeCell(level, this, 1, Terrain.EMPTY_SP);
            if (pos != -1) level.drop(RoomLoot.roll(Generator.Category.SCROLL, Generator.Category.POTION), pos);
        }

        entrance().set(Door.Type.REGULAR);
    }
}
