package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special;

import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfFrost;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Point;

import java.math.BigInteger;

public class ForgeRoom extends SpecialRoom {

    @Override
    public int minWidth() { return 7; }

    @Override
    public int minHeight() { return 7; }

    @Override
    public void paint(Level level) {
        Painter.fill(level, this, Terrain.WALL);
        Painter.fill(level, this, 1, Terrain.EMPTY_SP);

        Point c = center();
        int cc = level.pointToCell(c);

        for (int n : PathFinder.CIRCLE8) {
            Blob.seed(cc + n, BigInteger.ONE, MagicalFireRoom.EternalFire.class, level);
        }

        Painter.set(level, c, Terrain.PEDESTAL);
        Item prize = RoomLoot.roll(Generator.Category.WEAPON, Generator.Category.ARMOR);
        if (!prize.cursed) {
            prize.upgrade();
            prize.upgrade();
        }
        level.drop(prize, cc).type = Heap.Type.CHEST;

        level.addItemToSpawn(new PotionOfFrost());
        entrance().set(Door.Type.REGULAR);
    }
}
