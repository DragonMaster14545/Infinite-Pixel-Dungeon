package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret;

import com.shatteredpixel.shatteredpixeldungeon.items.Dewdrop;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.RoomLoot;
import com.shatteredpixel.shatteredpixeldungeon.plants.Earthroot;
import com.shatteredpixel.shatteredpixeldungeon.plants.Sungrass;

public class SecretGladeRoom extends SecretRoom {

    @Override
    public int minWidth() { return 6; }

    @Override
    public int minHeight() { return 6; }

    @Override
    public void paint(Level level) {
        Painter.fill(level, this, Terrain.WALL);
        Painter.fill(level, this, 1, Terrain.GRASS);

        for (int i = 0; i < width() * height() / 5; i++) {
            int pos = RoomLoot.freeCell(level, this, 1, Terrain.GRASS);
            if (pos != -1) Painter.set(level, pos, Terrain.HIGH_GRASS);
        }

        for (int i = 0; i < 2; i++) {
            int pos = RoomLoot.freeCell(level, this, 1, Terrain.GRASS);
            if (pos != -1) level.plant(new Sungrass.Seed(), pos);
        }
        int e = RoomLoot.freeCell(level, this, 1, Terrain.GRASS);
        if (e != -1) level.plant(new Earthroot.Seed(), e);

        for (int i = 0; i < 3; i++) {
            int pos = RoomLoot.freeCell(level, this, 1, Terrain.GRASS);
            if (pos != -1) level.drop(new Dewdrop(), pos);
        }

        entrance().set(Door.Type.HIDDEN);
    }
}
