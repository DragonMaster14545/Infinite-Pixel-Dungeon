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
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class SackOfHolding extends Bag {

	{
		image = ItemSpriteSheet.CHEST;
	}

	@Override
	public boolean canHold( Item item ) {
		return true;
	}

	@Override
	public boolean collect(Bag container) {
        return super.collect(container);
	}

	public int capacity(){
		return 57;
	}

	@Override
	public long value() {
		return 650 * (Dungeon.cycle + 1);
	}

	@Override
	public String name() {
		return "Sack of Holding";
	}

	@Override
	public String desc() {
		return "A bag that can hold any item through some weird magic";
	}
}
