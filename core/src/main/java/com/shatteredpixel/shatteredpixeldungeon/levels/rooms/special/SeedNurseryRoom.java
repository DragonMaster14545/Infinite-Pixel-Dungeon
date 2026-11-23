package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special;

import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.watabou.utils.Random;

public class SeedNurseryRoom extends SpecialRoom {

    @Override
    public int minWidth() { return 6; }

    @Override
    public int minHeight() { return 6; }

    @Override
    public void paint(Level level) {
        Painter.fill(level, this, Terrain.WALL);
        Painter.fill(level, this, 1, Terrain.GRASS);

        for (int i = 0; i < width() * height() / 6; i++) {
            int pos = RoomLoot.freeCell(level, this, 1, Terrain.GRASS);
            if (pos != -1) Painter.set(level, pos, Terrain.HIGH_GRASS);
        }

        int plants = Random.IntRange(3, 4);
        for (int i = 0; i < plants; i++) {
            Item seed = null;
            try {
                seed = Generator.random(Generator.Category.SEED);
            } catch (Throwable ignored) {
            }
            if (!(seed instanceof Plant.Seed)) continue;
            int pos = RoomLoot.freeCell(level, this, 1, Terrain.GRASS);
            if (pos == -1) pos = RoomLoot.freeCell(level, this, 1, Terrain.HIGH_GRASS);
            if (pos != -1) level.plant((Plant.Seed) seed, pos);
        }

        int loose = Random.IntRange(2, 3);
        for (int i = 0; i < loose; i++) {
            Item seed = null;
            try {
                seed = Generator.random(Generator.Category.SEED);
            } catch (Throwable ignored) {
            }
            if (seed == null) continue;
            int pos = RoomLoot.freeCell(level, this, 1, Terrain.GRASS);
            if (pos != -1) level.drop(seed, pos);
        }

        entrance().set(Door.Type.REGULAR);
    }
}
