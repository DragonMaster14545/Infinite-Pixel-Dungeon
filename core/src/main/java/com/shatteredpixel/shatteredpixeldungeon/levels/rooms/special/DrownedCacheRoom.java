package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special;

import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLevitation;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.watabou.utils.Point;

public class DrownedCacheRoom extends SpecialRoom {

    @Override
    public int minWidth() { return 7; }

    @Override
    public int minHeight() { return 7; }

    @Override
    public void paint(Level level) {
        Painter.fill(level, this, Terrain.WALL);
        Painter.fill(level, this, 1, Terrain.WATER);

        Point c = center();
        Painter.fill(level, c.x - 1, c.y - 1, 3, 3, Terrain.EMPTY_SP);

        Door d = entrance();
        int ix = d.x;
        int iy = d.y;
        if (d.x == left) ix++;
        else if (d.x == right) ix--;
        else if (d.y == top) iy++;
        else iy--;
        Painter.set(level, ix, iy, Terrain.EMPTY_SP);

        int cc = level.pointToCell(c);
        Painter.set(level, cc, Terrain.PEDESTAL);
        level.drop(RoomLoot.roll(Generator.Category.WAND, Generator.Category.RING, Generator.Category.ARTIFACT), cc).type = Heap.Type.CHEST;
        level.drop(RoomLoot.roll(Generator.Category.POTION), level.pointToCell(new Point(c.x - 1, c.y)));
        level.drop(RoomLoot.roll(Generator.Category.SCROLL), level.pointToCell(new Point(c.x + 1, c.y)));

        level.addItemToSpawn(new PotionOfLevitation());
        d.set(Door.Type.REGULAR);
    }
}
