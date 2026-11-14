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
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class VialOfBlood extends Trinket {

	{
		image = ItemSpriteSheet.BLOOD_VIAL;
	}

	@Override
	protected BigInteger upgradeEnergyCost() {
		//6 -> 8(14) -> 10(24) -> 12(36)
		return BigInteger.valueOf(6).add(BigInteger.valueOf(2).multiply(level()));
	}

	@Override
	public String statsDesc() {
		if (isIdentified()){
			return Messages.get(this,
					"stats_desc",
					Messages.decimalFormat("#.##", 100*(totalHealMultiplier(Big.toInt(buffedLvl()))-1f)),
					maxHealPerTurn(Big.toInt(buffedLvl())).toString());
		} else {
			return Messages.get(this,
					"typical_stats_desc",
					Messages.decimalFormat("#.##", 100*(totalHealMultiplier(Big.toInt(buffedLvl()))-1f)),
					maxHealPerTurn(Big.toInt(buffedLvl())).toString());
		}
	}

	public static boolean delayBurstHealing(){
		return trinketLevel(VialOfBlood.class) != -1;
	}

	public static float totalHealMultiplier(){
		return totalHealMultiplier(trinketLevel(VialOfBlood.class));
	}

	public static float totalHealMultiplier(int level){
		if (level == -1){
			return 1;
		} else {
			return 1f + 0.125f*(level+1);
		}
	}

	public static BigInteger maxHealPerTurn(){
		return maxHealPerTurn(trinketLevel(VialOfBlood.class));
	}

	public static BigInteger maxHealPerTurn(int level){
		BigInteger maxHP = Dungeon.hero == null ? BigInteger.valueOf(20) : Dungeon.hero.HT;
		if (level == -1){
			return maxHP;
		} else {
			switch (level){
				case 0: default:
					return BigInteger.valueOf(4).add(Big.round(Big.dec(0.15f).multiply(Big.dec(maxHP))));
				case 1:
					return BigInteger.valueOf(3).add(Big.round(Big.dec(0.10f).multiply(Big.dec(maxHP))));
				case 2:
					return BigInteger.valueOf(2).add(Big.round(Big.dec(0.07f).multiply(Big.dec(maxHP))));
				case 3:
					return BigInteger.ONE.add(Big.round(Big.dec(0.05f).multiply(Big.dec(maxHP))));
			}
		}
	}

}
