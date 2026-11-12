/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.items.trinkets;



import com.shatteredpixel.shatteredpixeldungeon.utils.Big;
import java.math.BigInteger;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

public class ThirteenLeafClover extends Trinket {

	{
		image = ItemSpriteSheet.CLOVER;
	}

	@Override
	protected BigInteger upgradeEnergyCost() {
		//6 -> 5(11) -> 7(18) -> 8(26)
		return Big.round(Big.dec(5).add(Big.dec(1.67f).multiply(Big.dec(level()))));
	}

	@Override
	public String statsDesc() {
		if (isIdentified()){
			return Messages.get(this, "stats_desc", (int)(100*combatDistributionInverseChance(buffedLvl())));
		} else {
			return Messages.get(this, "typical_stats_desc", (int)(100*combatDistributionInverseChance(BigInteger.ZERO)));
		}
	}

	public static float combatDistributionInverseChance(){
		return combatDistributionInverseChance(BigInteger.valueOf(trinketLevel(ThirteenLeafClover.class)));
	}

	public static float combatDistributionInverseChance( BigInteger level ){
		if ((level.compareTo(BigInteger.valueOf(-1)) <= 0)){
			return 0;
		} else {
			return Big.dec(0.25f).add(Big.dec(0.25f).multiply(Big.dec(level))).floatValue();
		}
	}

	public static BigInteger invCombatRoll( BigInteger min, BigInteger max){
		return Big.invNormalRange(min, max);
	}

}
