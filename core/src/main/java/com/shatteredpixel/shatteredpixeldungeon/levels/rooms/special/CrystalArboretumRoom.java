package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special;

import com.shatteredpixel.shatteredpixeldungeon.items.Dewdrop;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.watabou.utils.Random;

public class CrystalArboretumRoom extends OrientedCrystalRoom {

    @Override
    public void paint(Level level) {
        Painter.fill(level, this, Terrain.WALL);
        orient();
        entrance().set(Door.Type.REGULAR);

        int mid = span / 2;
        fillLocal(level, 0, 0, 1, span - 1, Terrain.EMPTY);
        fillLocal(level, 2, 0, 2, span - 1, Terrain.WALL);
        set(level, 2, mid, Terrain.CRYSTAL_DOOR);
        fillLocal(level, 3, 0, depth - 1, span - 1, Terrain.GRASS);

        for (int d = 3; d < depth; d++) {
            for (int l = 0; l < span; l++) {
                if (Random.Int(3) == 0) set(level, d, l, Terrain.HIGH_GRASS);
            }
        }

        int planted = 0;
        for (int d = 3; d < depth && planted < 5; d++) {
            for (int l = 0; l < span && planted < 5; l++) {
                if ((d + l) % 2 != 0) continue;
                Item seed = null;
                try {
                    seed = Generator.random(Generator.Category.SEED);
                } catch (Throwable ignored) {
                }
                if (seed instanceof Plant.Seed) {
                    level.plant((Plant.Seed) seed, cell(level, d, l));
                    planted++;
                }
            }
        }

        for (int i = 0; i < 4; i++) {
            int pos = RoomLoot.freeCell(level, this, 1, Terrain.GRASS);
            if (pos != -1) level.drop(new Dewdrop(), pos);
        }

        addKeys(level, 1);
    }
}
