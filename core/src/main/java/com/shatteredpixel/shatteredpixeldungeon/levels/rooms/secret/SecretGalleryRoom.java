package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret;

import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.RoomLoot;
import com.watabou.utils.Point;

public class SecretGalleryRoom extends SecretRoom {

    @Override
    public int minWidth() { return 5; }

    @Override
    public int minHeight() { return 5; }

    @Override
    public void paint(Level level) {
        Painter.fill(level, this, Terrain.WALL);
        Painter.fill(level, this, 1, Terrain.EMPTY_SP);

        Point c = center();
        int a = level.pointToCell(new Point(c.x - 1, c.y));
        int b = level.pointToCell(new Point(c.x + 1, c.y));
        Painter.set(level, a, Terrain.PEDESTAL);
        Painter.set(level, b, Terrain.PEDESTAL);
        level.drop(RoomLoot.roll(Generator.Category.WAND, Generator.Category.RING, Generator.Category.ARTIFACT), a);
        level.drop(RoomLoot.roll(Generator.Category.WEAPON, Generator.Category.ARMOR), b);

        entrance().set(Door.Type.HIDDEN);
    }
}
