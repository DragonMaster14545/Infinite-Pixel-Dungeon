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

package com.shatteredpixel.shatteredpixeldungeon.items.rings;


import java.math.BigInteger;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class RingOfSharpshooting extends Ring {

	{
		icon = ItemSpriteSheet.Icons.RING_SHARPSHOOT;
	}

	public String statsInfo() {
		if (isIdentified()){
			String info = Messages.get(this, "stats",
					soloBuffedBonus(), Messages.decimalFormat("#.##", 100f * (Math.pow(1.15, soloBonus().doubleValue()) - 1f)));
			if (isEquipped(Dungeon.hero) && (soloBuffedBonus().compareTo(combinedBuffedBonus(Dungeon.hero)) != 0)){
				info += "\n\n" + Messages.get(this, "combined_stats",
						combinedBuffedBonus(Dungeon.hero), Messages.decimalFormat("#.##", 100f * (Math.pow(1.15, combinedBonus(Dungeon.hero).doubleValue()) - 1f)));
			}
			return info;
		} else {
			return Messages.get(this, "typical_stats", 1, Messages.decimalFormat("#.##", 15f));
		}
	}

	@Override
	public String upgradeStat1(BigInteger level) {
		if (cursed && cursedKnown) level = BigInteger.valueOf(-1).min(level.subtract(BigInteger.valueOf(3)));
		return level.add(BigInteger.ONE).toString();
	}

	@Override
	public String upgradeStat2(BigInteger level) {
		if (cursed && cursedKnown) level = BigInteger.valueOf(-1).min(level.subtract(BigInteger.valueOf(3)));
		return Messages.decimalFormat("#.##", 100f * (Math.pow(1.15, level.add(BigInteger.ONE).doubleValue())-1f)) + "%";
	}
	
	@Override
	protected RingBuff buff( ) {
		return new Aim();
	}
	
	public static BigInteger levelDamageBonus( Char target ){
        return getBuffedBonus(target, RingOfSharpshooting.Aim.class).add(BigInteger.ONE).min(BigInteger.valueOf(10L * (1 + Dungeon.cycle + Dungeon.hero.upgrades.ringExpansion())));
		//return getBuffedBonus(target, RingOfSharpshooting.Aim.class) + 1;
	}
	
	public static float durabilityMultiplier( Char target ){
        return Math.min((float)(Math.pow(1.15, getBonus(target, Aim.class))), 10f * (1 + Dungeon.cycle + Dungeon.hero.upgrades.ringExpansion()));
		//return (float)(Math.pow(1.2, getBonus(target, Aim.class)));
	}

	public class Aim extends RingBuff {
	}
}
