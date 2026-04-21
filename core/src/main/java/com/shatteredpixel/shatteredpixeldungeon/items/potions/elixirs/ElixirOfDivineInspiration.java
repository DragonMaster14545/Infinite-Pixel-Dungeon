/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfExperience;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.GooBlob;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.Alchemize;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.watabou.utils.GameMath;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class ElixirOfDivineInspiration extends Elixir {
	
	{
		image = ItemSpriteSheet.ELIXIR_DI;
	}

    @Override
    public boolean isRepeatable() {
        return true;
    }
	
	@Override
	public void apply(Hero hero) {

        Buff.affect(hero, DivineInspiration.class).stacks++;
        GLog.p(Messages.get(this, "divine_applied"));

        new Flare( 6, 32 ).color(0xFFFF00, true).show( curUser.sprite, 4f );
	}

    public static class DivineInspiration extends Buff {

        {
            //laugh all you can
            revivePersists = true;
        }

        public int stacks = 0;

        @Override
        public int icon() {
            return BuffIndicator.BLESS;
        }

        @Override
        public String desc() {
            return Messages.get(this, "desc",
                    Math.round((Math.pow(1.015, stacks) - 1f)*100));
        }

        @Override
        public void tintIcon(Image icon) {
            icon.hardlight(0.5f, 0.5f, 0.5f);
        }

        public static String STACKS = "stacks";

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(STACKS, stacks);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            stacks = bundle.getInt(STACKS);
        }
    }
	
	public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe.SimpleRecipe {
		
		{
			inputs =  new Class[]{PotionOfExperience.class, Alchemize.class};
			inQuantity = new int[]{1, 1};
			
			cost = 70;
			
			output = ElixirOfDivineInspiration.class;
			outQuantity = 3;
		}

	}
	
}
