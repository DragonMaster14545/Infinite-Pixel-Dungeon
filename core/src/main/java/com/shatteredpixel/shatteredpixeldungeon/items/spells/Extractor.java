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

package com.shatteredpixel.shatteredpixeldungeon.items.spells;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.fishingrods.FishingRod;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTransmutation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

public class Extractor extends InventorySpell {
	
	{
		image = ItemSpriteSheet.EXTRACTOR;

		talentFactor = 2;
		talentChance = 1/(float) Recipe.OUT_QUANTITY;
	}

	@Override
	protected boolean usableOnItem(Item item) {
		return item.isUpgradable() && !(item.level() <= 0);
	}

	@Override
	protected void onItemSelected(Item item) {

		Dungeon.level.drop(new ScrollOfUpgrade().quantity(item.level()), curUser.pos).sprite.drop();
		item.level(0);

		GLog.p(Messages.get(this, "extracted"));
		curUser.sprite.emitter().start(Speck.factory(Speck.UP), 0.2f, 10);
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

		private static final int OUT_QUANTITY = 2;

		{
			inputs =  new Class[]{ScrollOfUpgrade.class, Recycle.class};
			inQuantity = new int[]{1, 1};
			
			cost = 50;
			
			output = Extractor.class;
			outQuantity = OUT_QUANTITY;
		}
		
	}
}
