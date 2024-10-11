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

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GnollSprite;

public class Gnoll extends Mob {
	
	{
		spriteClass = GnollSprite.class;
		
		HP = HT = Dungeon.getCycleMultiplier(12);
		defenseSkill = Dungeon.getCycleMultiplier(4);
		
		EXP = Dungeon.getCycleMultiplier(2);
		maxLvl = 8;
		
		loot = Gold.class;
		lootChance = 0.5f;
	}
	
	@Override
	public long damageRoll() {
		return Dungeon.NormalLongRange( Dungeon.getCycleMultiplier(1), Dungeon.getCycleMultiplier(6) );
	}
	
	@Override
	public long attackSkill(Char target ) {
		return Dungeon.getCycleMultiplier(10);
	}
	
	@Override
	public long cycledDrRoll() {
		return Dungeon.NormalLongRange(Dungeon.getCycleMultiplier(0), Dungeon.getCycleMultiplier(2));
	}
}
