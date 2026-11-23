package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

public class RoomLoot {

    public static Item roll(Generator.Category... cats) {
        for (int i = 0; i < 12; i++) {
            Item item = null;
            try {
                item = Generator.random(cats[Random.Int(cats.length)]);
            } catch (Throwable ignored) {
            }
            if (item != null && !item.cursed && !Challenges.isItemBlocked(item)) return item;
        }
        return new Gold().random();
    }

    public static int freeCell(Level level, Room room, int margin, int terrain) {
        int pos = -1;
        for (int i = 0; i < 60; i++) {
            Point p = room.random(margin);
            pos = level.pointToCell(p);
            if (level.map[pos] == terrain && level.heaps.get(pos) == null && level.plants.get(pos) == null && level.findMob(pos) == null) return pos;
        }
        return -1;
    }
}
