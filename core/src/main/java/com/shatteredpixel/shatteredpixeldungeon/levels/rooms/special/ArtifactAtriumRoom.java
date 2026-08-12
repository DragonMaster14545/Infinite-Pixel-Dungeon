package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

public class ArtifactAtriumRoom extends SpecialRoom {

    @Override
    public void paint(Level level) {
        Painter.fill(level, this, Terrain.WALL);
        Painter.fill(level, this, 1, Terrain.EMPTY_SP);

        int innerLeft = left + 1;
        int innerTop = top + 1;
        int innerRight = right - 1;
        int innerBottom = bottom - 1;
        for (int x = innerLeft; x <= innerRight; x++) {
            Painter.set(level, new Point(x, innerTop), Random.Int(3) == 0 ? Terrain.REGION_DECO : Terrain.CHASM);
            Painter.set(level, new Point(x, innerBottom), Random.Int(3) == 0 ? Terrain.REGION_DECO : Terrain.CHASM);
        }
        for (int y = innerTop; y <= innerBottom; y++) {
            Painter.set(level, new Point(innerLeft, y), Random.Int(3) == 0 ? Terrain.REGION_DECO : Terrain.CHASM);
            Painter.set(level, new Point(innerRight, y), Random.Int(3) == 0 ? Terrain.REGION_DECO : Terrain.CHASM);
        }

        if (innerRight - innerLeft > 2 && innerBottom - innerTop > 2) {
            Painter.fill(level, innerLeft + 1, innerTop + 1,
                    innerRight - innerLeft - 1, innerBottom - innerTop - 1, Terrain.EMPTY_SP);
        }

        int count = Random.IntRange(1, 3);
        for (int i = 0; i < count; i++) {
            int pos;
            int attempts = 0;
            do {
                pos = level.pointToCell(random());
                attempts++;
            } while (attempts < 20 && (level.map[pos] != Terrain.EMPTY_SP || level.heaps.get(pos) != null));

            Item artifact = Generator.random(Generator.Category.ARTIFACT);
            if (artifact != null) {
                level.drop(artifact, pos);
            }
        }

        entrance().set(Door.Type.LOCKED);
    }
}
