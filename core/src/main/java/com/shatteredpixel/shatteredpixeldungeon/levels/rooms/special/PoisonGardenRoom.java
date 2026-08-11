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

        int patchCount = Random.IntRange(2, 4);
        for (int i = 0; i < patchCount; i++) {
            Point p = random();
            int patchW = Random.IntRange(1, 2);
            int patchH = Random.IntRange(1, 2);
            Painter.fill(level, p.x - patchW/2, p.y - patchH/2, patchW + 1, patchH + 1, Terrain.HIGH_GRASS);
        }

        int clusters = Random.IntRange(1, 2);
        for (int c = 0; c < clusters; c++) {
            Point clusterCenter = random();
            int trapsInCluster = Random.IntRange(2, 3);
            for (int i = 0; i < trapsInCluster; i++) {
                int ox = clusterCenter.x + Random.IntRange(-1, 1);
                int oy = clusterCenter.y + Random.IntRange(-1, 1);
                if (ox > left && ox < right && oy > top && oy < bottom) {
                    Painter.set(level, new Point(ox, oy), Terrain.SECRET_TRAP);
                }
            }
        }

        entrance().set(Door.Type.REGULAR);
    }
}
