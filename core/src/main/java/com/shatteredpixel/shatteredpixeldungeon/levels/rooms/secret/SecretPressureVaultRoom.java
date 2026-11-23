package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret;

import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.RoomLoot;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.AlarmTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.ChillingTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.ConfusionTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.PoisonDartTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.ToxicTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.watabou.utils.Point;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

public class SecretPressureVaultRoom extends SecretRoom {

    @Override
    public int minWidth() { return 7; }

    @Override
    public int minHeight() { return 7; }

    @Override
    public int maxWidth() { return 9; }

    @Override
    public int maxHeight() { return 9; }

    @Override
    public void paint(Level level) {
        Painter.fill(level, this, Terrain.WALL);
        Painter.fill(level, this, 1, Terrain.EMPTY_SP);

        Door d = entrance();
        int ix = d.x;
        int iy = d.y;
        if (d.x == left) ix++;
        else if (d.x == right) ix--;
        else if (d.y == top) iy++;
        else iy--;
        int doorInside = level.pointToCell(new Point(ix, iy));

        Class<? extends Trap> trapClass = Random.oneOf(
                PoisonDartTrap.class, ConfusionTrap.class, ToxicTrap.class, ChillingTrap.class, AlarmTrap.class);

        Point c = center();
        int cc = level.pointToCell(c);
        for (Point p : getPoints()) {
            int cell = level.pointToCell(p);
            if (level.map[cell] != Terrain.EMPTY_SP) continue;
            if ((p.x + p.y) % 2 != 0) continue;
            if (cell == cc || cell == doorInside || level.adjacent(cell, doorInside)) continue;
            Painter.set(level, cell, Terrain.SECRET_TRAP);
            level.setTrap(Reflection.newInstance(trapClass).hide(), cell);
        }

        Painter.set(level, cc, Terrain.PEDESTAL);
        level.drop(RoomLoot.roll(Generator.Category.WAND, Generator.Category.RING, Generator.Category.ARTIFACT), cc).type = Heap.Type.CHEST;

        d.set(Door.Type.HIDDEN);
    }
}
