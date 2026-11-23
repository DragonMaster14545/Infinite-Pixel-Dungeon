package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special;

import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;

public class CrystalArsenalRoom extends OrientedCrystalRoom {

    @Override
    public void paint(Level level) {
        Painter.fill(level, this, Terrain.WALL);
        orient();
        entrance().set(Door.Type.REGULAR);

        int mid = span / 2;
        fillLocal(level, 0, 0, 1, span - 1, Terrain.EMPTY);
        fillLocal(level, 2, 0, 2, span - 1, Terrain.WALL);
        set(level, 2, mid, Terrain.CRYSTAL_DOOR);
        fillLocal(level, 3, 0, depth - 1, span - 1, Terrain.EMPTY_SP);

        Item weapon = RoomLoot.roll(Generator.Category.WEAPON);
        Item armor = RoomLoot.roll(Generator.Category.ARMOR);
        Item missile = null;
        try {
            missile = Generator.randomMissile();
        } catch (Throwable ignored) {
        }
        if (missile == null) missile = RoomLoot.roll(Generator.Category.WEAPON);

        if (!weapon.cursed) weapon.upgrade();
        if (!armor.cursed) armor.upgrade();

        int d = depth - 1;
        int[] slots = {0, mid, span - 1};
        Item[] items = {weapon, armor, missile};
        for (int i = 0; i < 3; i++) {
            set(level, d, slots[i], Terrain.PEDESTAL);
            drop(level, items[i], d, slots[i]);
        }

        addKeys(level, 1);
    }
}
