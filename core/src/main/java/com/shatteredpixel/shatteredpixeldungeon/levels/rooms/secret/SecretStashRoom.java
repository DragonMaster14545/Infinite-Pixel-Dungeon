package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret;

import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.RoomLoot;
import com.watabou.utils.Random;

public class SecretStashRoom extends SecretRoom {

    @Override
    public int minWidth() { return 5; }

    @Override
    public int minHeight() { return 5; }

    @Override
    public void paint(Level level) {
        Painter.fill(level, this, Terrain.WALL);
        Painter.fill(level, this, 1, Terrain.EMPTY_SP);

        int piles = Random.IntRange(5, 8);
        for (int i = 0; i < piles; i++) {
            int pos = RoomLoot.freeCell(level, this, 1, Terrain.EMPTY_SP);
            if (pos != -1) level.drop(new Gold().random(), pos);
        }

        int chest = RoomLoot.freeCell(level, this, 1, Terrain.EMPTY_SP);
        if (chest != -1) {
            level.drop(RoomLoot.roll(Generator.Category.POTION, Generator.Category.SCROLL), chest).type = Heap.Type.CHEST;
        }

        entrance().set(Door.Type.HIDDEN);
    }
}
