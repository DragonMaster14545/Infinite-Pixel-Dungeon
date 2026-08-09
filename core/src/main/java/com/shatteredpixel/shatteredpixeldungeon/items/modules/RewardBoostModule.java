/*
 *
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * Experienced Pixel Dungeon
 * Copyright (C) 2019-2024 Trashbox Bobylev
 *
 * Extended Experienced Pixel Dungeon
 * Copyright (C) 2023-2024 John Nollas
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
 *
 */

package com.shatteredpixel.shatteredpixeldungeon.items.modules;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;

public class RewardBoostModule extends Module {

    {
        stackable = true;
        image = ItemSpriteSheet.REWARD_BOOSTED;

        defaultAction = AC_USE;
    }

    @Override
    public void execute( Hero hero, String action ) {
        super.execute(hero, action);
        if (action.equals(AC_USE)) {

            Buff.prolong(hero, RewardBoost2.class, 30f);

            detach(hero.belongings.backpack);
        }
    }

    @Override
    public long value() {
        return 10000 * quantity * Dungeon.depth;
    }

    public static class RewardBoost2 extends FlavourBuff {
        {
            type = buffType.POSITIVE;
        }

        @Override
        public int icon() {
            return BuffIndicator.UPGRADE;
        }

        @Override
        public void tintIcon(Image icon) {
            icon.hardlight(0x00FF00);
        }

        @Override
        public String name() {
            return "Reward Boost";
        }

        @Override
        public String desc() {
            return "Did you use that module before?";
        }
    }
}
