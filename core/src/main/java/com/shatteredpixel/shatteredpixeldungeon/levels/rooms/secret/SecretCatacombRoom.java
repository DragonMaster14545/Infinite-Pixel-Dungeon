package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret;

import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.RoomLoot;
import com.watabou.utils.Random;

public class SecretCatacombRoom extends SecretRoom {

    @Override
    public int minWidth() { return 6; }

    @Override
    public int minHeight() { return 6; }

    @Override
    public void paint(Level level) {
        Painter.fill(level, this, Terrain.WALL);
        Painter.fill(level, this, 1, Terrain.EMPTY);

        int n = Random.IntRange(3, 4);
        for (int i = 0; i < n; i++) {
            int pos = RoomLoot.freeCell(level, this, 1, Terrain.EMPTY);
            if (pos == -1) continue;
            level.drop(Generator.random(), pos).setHauntedIfCursed().type = Heap.Type.SKELETON;
        }

        entrance().set(Door.Type.HIDDEN);
    }
}
