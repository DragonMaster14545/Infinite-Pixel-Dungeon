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

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.Transmuting;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.fishingrods.FishingRod;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.Brew;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.Elixir;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.ExoticPotion;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTransmutation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ExoticScroll;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.Runestone;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.TippedDart;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Raritize extends InventorySpell {

	private static final double CHANCE_MULTIPLIER = 1d;

	{
		image = ItemSpriteSheet.RARITIZE;

		talentFactor = 2;
		talentChance = 1/(float) Recipe.OUT_QUANTITY;
	}

	protected double chanceBoost() { return 1d + level(); }

	private static Rarity nextRarity( Rarity current ) {
		if (current == null) return null;
		int up = current.ordinal() - 1;
		return up >= 0 ? Rarity.values()[up] : null;
	}

	public static double ascensionChance( Rarity target ) {
		if (target == null) return 0d;
		if (target.chance <= 0d) return 1d;
		return Math.min( 1d, CHANCE_MULTIPLIER * target.chance );
	}

	private static String rarityName( Rarity rarity ) {
		return rarity == Rarity.NONE ? "None" : rarity.trueName;
	}

	@SuppressWarnings("DefaultLocale")
    private String oddsText(Rarity target ) {
		double p = Math.min( 1d, ascensionChance( target ) * chanceBoost() );
		if (p >= 1d) return "100%";

		String percent;
		if (p >= 0.01d)        percent = String.format( "%.1f", p * 100d );
		else if (p >= 0.0001d) percent = String.format( "%.3f", p * 100d );
		else                   percent = String.format( "%.2e", p * 100d );

		return "1 in " + String.format( "%,d", Math.round( 1d / p ) ) + " (" + percent + "%)";
	}

	@Override
	protected boolean usableOnItem(Item item) {
		return (item instanceof Weapon || item instanceof Armor || item instanceof Ring || item instanceof Artifact
				|| item instanceof Wand || item instanceof FishingRod)
				&& nextRarity( item.rarity ) != null;
	}

	@Override
	protected void onItemSelected(Item item) {

		Rarity next = nextRarity( item.rarity );

		if (next == null) {
			GLog.w(Messages.get(this, "ineligible"));
			return;
		}

		if (Random.Double() < Math.min( 1d, ascensionChance( next ) * chanceBoost())) {
			item.rarity = next;
			GLog.p(Messages.get(this, "raritized", rarityName( next )));
			curUser.sprite.emitter().start(Speck.factory(Speck.UP), 0.2f, 10);
		} else {
			GLog.n(Messages.get(this, "failed", rarityName( next ), oddsText( next )));
		}
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc") + "\n\n" + Messages.get(this, "desc_chance")
				+ (level() > 0 ? "\n\n" + Messages.get(this, "desc_boost", chanceBoost()) : "");
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

		private static final int OUT_QUANTITY = 4;

		{
			inputs =  new Class[]{ScrollOfUpgrade.class, ScrollOfTransmutation.class};
			inQuantity = new int[]{1, 1};
			
			cost = 175;
			
			output = Raritize.class;
			outQuantity = OUT_QUANTITY;
		}
		
	}

	public static final int MAX_LEVEL = Integer.MAX_VALUE;

	@Override
	public String name() {
		return super.name();
	}

	@Override
	public long visiblyUpgraded() { return level(); }

	@Override
	public long buffedVisiblyUpgraded() { return level(); }

	@Override
	public boolean isSimilar( Item item ) {
		return super.isSimilar( item ) && item.level() == level();
	}

	public static class MergeRecipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe {

		@Override
		public boolean testIngredients( ArrayList<Item> ingredients ) {
			if (ingredients == null || ingredients.size() != 2) return false;
			if (!(ingredients.get( 0 ) instanceof Raritize) || !(ingredients.get( 1 ) instanceof Raritize)) return false;
			return ingredients.get( 0 ).level() == ingredients.get( 1 ).level()
					&& ingredients.get( 0 ).level() < MAX_LEVEL;
		}

		@Override
		public long cost( ArrayList<Item> ingredients ) {
			return 5 + ingredients.get( 0 ).level() * 5;
		}

		@Override
		public Item brew( ArrayList<Item> ingredients ) {
			if (!testIngredients( ingredients )) return null;
			long level = ingredients.get( 0 ).level();
			for (Item ingredient : ingredients) {
				ingredient.quantity( ingredient.quantity() - 1 );
			}
			Raritize result = new Raritize();
			result.level( level + 1 );
			result.identify();
			return result;
		}

		@Override
		public Item sampleOutput( ArrayList<Item> ingredients ) {
			if (!testIngredients( ingredients )) return null;
			Raritize result = new Raritize();
			result.level( ingredients.get( 0 ).level() + 1 );
			result.identify();
			return result;
		}
	}
}
