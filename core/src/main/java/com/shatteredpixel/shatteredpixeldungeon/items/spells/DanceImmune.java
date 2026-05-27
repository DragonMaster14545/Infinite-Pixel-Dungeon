/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2019-2024 Evan Debenham
 *
 * Experienced Pixel Dungeon
 * Copyright (C) 2019-2024 Trashbox Bobylev
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.items.spells;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DanceImmuneBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

public class DanceImmune extends Spell {
    {
        image = ItemSpriteSheet.TELE_GRAB;

        talentChance = 1/(float) Recipe.OUT_QUANTITY;
    }

    @Override
    protected void onCast(Hero hero) {
        if (hero.buff(DanceImmuneBuff.class) == null) {
            Buff.affect(hero, DanceImmuneBuff.class, 10f);
            GLog.p(Messages.get(this, "applied"));
            Catalog.countUse(getClass());
            detach( curUser.belongings.backpack );
        } else {
            GLog.w(Messages.get(this, "ongoing"));
        }
    }

    @Override
    public long value() {
        return (long)(60 * (quantity/(float) Recipe.OUT_QUANTITY));
    }

    @Override
    public long energyVal() {
        return (long)(12 * (quantity/(float) Recipe.OUT_QUANTITY));
    }

    public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe.SimpleRecipe {

        private static final int OUT_QUANTITY = 1;

        {
            inputs =  new Class[]{Alchemize.class, ScrollOfRemoveCurse.class};
            inQuantity = new int[]{1, 1};

            cost = 15;

            output = DanceImmune.class;
            outQuantity = OUT_QUANTITY;
        }

    }
}
