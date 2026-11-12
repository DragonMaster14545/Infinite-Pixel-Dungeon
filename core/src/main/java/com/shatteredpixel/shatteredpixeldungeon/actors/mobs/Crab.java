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


import java.math.BigInteger;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.food.MysteryMeat;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CrabSprite;

public class Crab extends Mob {

	{
		spriteClass = CrabSprite.class;
		
		HP = HT = Dungeon.getCycleMultiplier(BigInteger.valueOf(15));
		defenseSkill = Dungeon.getCycleMultiplier(BigInteger.valueOf(5));
		baseSpeed = 2f;
		
		EXP = Dungeon.getCycleMultiplier(BigInteger.valueOf(4));
		maxLvl = 9;
		
		loot = new MysteryMeat();
		lootChance = 0.167f;
	}
	
	@Override
	public BigInteger damageRoll() {
		return Dungeon.NormalLongRange( Dungeon.getCycleMultiplier(BigInteger.ONE), Dungeon.getCycleMultiplier(BigInteger.valueOf(7)) );
	}
	
	@Override
	public BigInteger attackSkill(Char target ) {
		return Dungeon.getCycleMultiplier(BigInteger.valueOf(12));
	}
	
	@Override
	public BigInteger cycledDrRoll() {
		return Dungeon.NormalLongRange(Dungeon.getCycleMultiplier(BigInteger.ZERO), Dungeon.getCycleMultiplier(BigInteger.valueOf(4)));
	}
}
