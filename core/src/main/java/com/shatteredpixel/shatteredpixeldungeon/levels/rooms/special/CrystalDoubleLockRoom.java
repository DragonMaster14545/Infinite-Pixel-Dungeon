package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special;

import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.watabou.utils.Random;

public class CrystalDoubleLockRoom extends OrientedCrystalRoom {

    @Override
    public int minWidth() { return 8; }

    @Override
    public int minHeight() { return 8; }

    @Override
    public int maxWidth() { return 10; }

    @Override
    public int maxHeight() { return 10; }

    @Override
    public void paint(Level level) {
        Painter.fill(level, this, Terrain.WALL);
        orient();
        entrance().set(Door.Type.REGULAR);

        int mid = span / 2;

        fillLocal(level, 0, 0, 1, span - 1, Terrain.EMPTY);
        set(level, 2, mid, Terrain.CRYSTAL_DOOR);

        int wallRow = depth - 3;
        fillLocal(level, 3, 0, wallRow - 1, span - 1, Terrain.EMPTY_SP);
        set(level, wallRow, mid, Terrain.CRYSTAL_DOOR);
        fillLocal(level, wallRow + 1, 0, depth - 1, span - 1, Terrain.EMPTY_SP);

        for (int l = 0; l < span; l++) {
            if (l != mid) {
                set(level, 2, l, Terrain.WALL);
                set(level, wallRow, l, Terrain.WALL);
            }
        }

        for (int l = 0; l < span; l += 2) {
            drop(level, RoomLoot.roll(Generator.Category.POTION, Generator.Category.SCROLL), 3, l);
        }

        int d = depth - 1;
        set(level, d, mid, Terrain.PEDESTAL);
        level.drop(RoomLoot.roll(Generator.Category.WAND, Generator.Category.RING, Generator.Category.ARTIFACT),
                cell(level, d, mid)).type = Heap.Type.CHEST;
        int side = Random.Int(2) == 0 ? 0 : span - 1;
        drop(level, RoomLoot.roll(Generator.Category.WEAPON, Generator.Category.ARMOR), d, side);

        addKeys(level, 2);
    }
}
