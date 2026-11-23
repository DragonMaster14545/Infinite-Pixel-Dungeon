package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special;

import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.watabou.utils.Random;

public class CrystalGalleryRoom extends OrientedCrystalRoom {

    @Override
    public void paint(Level level) {
        Painter.fill(level, this, Terrain.WALL);
        orient();
        entrance().set(Door.Type.REGULAR);

        fillLocal(level, 0, 0, 1, span - 1, Terrain.EMPTY);
        fillLocal(level, 2, 0, depth - 1, span - 1, Terrain.WALL);

        int[] alcoves = {0, span / 2, span - 1};
        Item[] prizes = {
                RoomLoot.roll(Random.oneOf(Generator.Category.POTION, Generator.Category.SCROLL)),
                RoomLoot.roll(Random.oneOf(Generator.Category.WEAPON, Generator.Category.ARMOR)),
                RoomLoot.roll(Random.oneOf(Generator.Category.WAND, Generator.Category.RING, Generator.Category.ARTIFACT))
        };
        for (int i = 0; i < 3; i++) {
            int l = alcoves[i];
            set(level, 2, l, Terrain.CRYSTAL_DOOR);
            fillLocal(level, 3, l, depth - 1, l, Terrain.EMPTY_SP);
            set(level, depth - 1, l, Terrain.PEDESTAL);
            drop(level, prizes[i], depth - 1, l);
        }

        addKeys(level, 2);
    }
}
