package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special;

import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Arrays;

public class CrystalReliquaryRoom extends OrientedCrystalRoom {

    @Override
    public void paint(Level level) {
        Painter.fill(level, this, Terrain.WALL);
        orient();
        entrance().set(Door.Type.REGULAR);

        fillLocal(level, 0, 0, depth - 1, span - 1, Terrain.EMPTY_SP);

        ArrayList<Generator.Category> cats = new ArrayList<>(Arrays.asList(
                Generator.Category.WEAPON, Generator.Category.ARMOR, Generator.Category.WAND,
                Generator.Category.RING, Generator.Category.ARTIFACT));
        Random.shuffle(cats);

        int[] lateral = {1, span / 2, span - 2};
        for (int i = 0; i < 3; i++) {
            Item prize = RoomLoot.roll(cats.get(i));
            set(level, depth - 1, lateral[i], Terrain.PEDESTAL);
            level.drop(prize, cell(level, depth - 1, lateral[i])).type = Heap.Type.CRYSTAL_CHEST;
        }

        addKeys(level, 2);
    }
}
