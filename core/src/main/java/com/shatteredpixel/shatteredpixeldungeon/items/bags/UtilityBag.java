/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2023 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.items.bags;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Amulet;
import com.shatteredpixel.shatteredpixeldungeon.items.Ankh;
import com.shatteredpixel.shatteredpixeldungeon.items.Honeypot;
import com.shatteredpixel.shatteredpixeldungeon.items.InfoPage;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KingsCrown;
import com.shatteredpixel.shatteredpixeldungeon.items.PsycheChest;
import com.shatteredpixel.shatteredpixeldungeon.items.TengusMask;
import com.shatteredpixel.shatteredpixeldungeon.items.TicketToArena;
import com.shatteredpixel.shatteredpixeldungeon.items.TicketToPortableShop;
import com.shatteredpixel.shatteredpixeldungeon.items.Torch;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.fishingrods.FishingRod;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.KeyToTruth;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.CeremonialCandle;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.CorpseDust;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.DarkGold;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.DwarfToken;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.Embers;
import com.shatteredpixel.shatteredpixeldungeon.items.remains.RemainsItem;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.treasurebags.IdealBag;
import com.shatteredpixel.shatteredpixeldungeon.items.treasurebags.TreasureBag;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Trinket;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.TrinketCatalyst;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class UtilityBag extends Bag {

	{
		image = ItemSpriteSheet.BACKPACK;
	}

	@Override
	public boolean canHold( Item item ) {
		if ( item instanceof Trinket || item instanceof TrinketCatalyst || item instanceof Food || item instanceof Ankh ||
            item instanceof Torch || item instanceof Honeypot || item instanceof Amulet || item instanceof TreasureBag ||
            item instanceof RemainsItem || item instanceof IdealBag.Plutonium || item instanceof TicketToArena ||
            item instanceof DwarfToken || item instanceof Embers || item instanceof CorpseDust || item instanceof CeremonialCandle ||
            item instanceof DarkGold || item instanceof TengusMask || item instanceof KingsCrown || item instanceof FishingRod ||
            item instanceof PsycheChest || item instanceof KeyToTruth || item instanceof TicketToPortableShop ||
            item instanceof InfoPage) {
			return super.canHold(item);
		} else {
			return false;
		}
	}

	@Override
	public boolean collect(Bag container) {
		if (super.collect( container )) {

			if (!Dungeon.LimitedDrops.UTILITY_BAG.dropped()){
				Dungeon.LimitedDrops.UTILITY_BAG.drop();
			}

			return true;
		} else {
			return false;
		}
	}

	public int capacity(){
		return 60;
	}

	@Override
	public long value() {
		return 650 * (Dungeon.cycle + 1);
	}

}
