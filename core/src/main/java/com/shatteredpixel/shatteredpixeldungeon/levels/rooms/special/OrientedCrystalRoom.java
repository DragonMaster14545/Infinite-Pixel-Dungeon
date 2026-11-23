package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.CrystalKey;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.watabou.utils.Point;

public abstract class OrientedCrystalRoom extends SpecialRoom {

    protected int fx, fy, lx, ly, originX, originY, depth, span;

    @Override
    public int minWidth() { return 7; }

    @Override
    public int minHeight() { return 7; }

    @Override
    public int maxWidth() { return 9; }

    @Override
    public int maxHeight() { return 9; }

    protected void orient() {
        Door e = entrance();
        if (e.x == left) {
            fx = 1; fy = 0; lx = 0; ly = 1;
            originX = left + 1; originY = top + 1;
            depth = width() - 2; span = height() - 2;
        } else if (e.x == right) {
            fx = -1; fy = 0; lx = 0; ly = 1;
            originX = right - 1; originY = top + 1;
            depth = width() - 2; span = height() - 2;
        } else if (e.y == top) {
            fx = 0; fy = 1; lx = 1; ly = 0;
            originX = left + 1; originY = top + 1;
            depth = height() - 2; span = width() - 2;
        } else {
            fx = 0; fy = -1; lx = 1; ly = 0;
            originX = left + 1; originY = bottom - 1;
            depth = height() - 2; span = width() - 2;
        }
    }

    protected int cell(Level level, int d, int l) {
        return level.pointToCell(new Point(originX + fx * d + lx * l, originY + fy * d + ly * l));
    }

    protected void set(Level level, int d, int l, int terrain) {
        Painter.set(level, cell(level, d, l), terrain);
    }

    protected void fillLocal(Level level, int d1, int l1, int d2, int l2, int terrain) {
        for (int d = d1; d <= d2; d++) {
            for (int l = l1; l <= l2; l++) {
                set(level, d, l, terrain);
            }
        }
    }

    protected int doorLateral() {
        Door e = entrance();
        return lx == 1 ? e.x - originX : e.y - originY;
    }

    protected void addKeys(Level level, int n) {
        for (int i = 0; i < n; i++) {
            level.addItemToSpawn(new CrystalKey(Dungeon.depth));
        }
    }

    protected void drop(Level level, Item item, int d, int l) {
        level.drop(item, cell(level, d, l));
    }
}
