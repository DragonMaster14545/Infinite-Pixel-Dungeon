package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

public class PoisonGardenRoom extends SpecialRoom {

    @Override
    public void paint(Level level) {
        Painter.fill(level, this, Terrain.WALL);
        Painter.fill(level, this, 1, Terrain.FURROWED_GRASS);

        int poisonCount = Random.Int(3) + 1;
        for (int i = 0; i < poisonCount; i++) {
            Point p = random();
            Painter.set(level, p, Terrain.SECRET_TRAP);
        }

        entrance().set(Door.Type.REGULAR);
    }
}
