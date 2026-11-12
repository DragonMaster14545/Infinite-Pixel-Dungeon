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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.items.food.MysteryMeat;
import com.shatteredpixel.shatteredpixeldungeon.sprites.AlbinoSprite;
import com.watabou.utils.Random;

public class Albino extends Rat {

	{
		spriteClass = AlbinoSprite.class;
		
		HP = HT = Dungeon.getCycleMultiplier(BigInteger.valueOf(15));
		EXP = Dungeon.getCycleMultiplier(BigInteger.TWO);
        defenseSkill = Dungeon.getCycleMultiplier(BigInteger.TEN).subtract(BigInteger.TEN);
		
		loot = new MysteryMeat();
		lootChance = 1f;
	}
	
	@Override
	public BigInteger attackProc( Char enemy, BigInteger damage ) {
		damage = super.attackProc( enemy, damage );
		if ((damage.compareTo(BigInteger.ZERO) > 0) && Random.Int( 2 ) == 0) {
			Buff.affect( enemy, Bleeding.class ).set( damage.doubleValue() );
		}
		
		return damage;
	}
}
