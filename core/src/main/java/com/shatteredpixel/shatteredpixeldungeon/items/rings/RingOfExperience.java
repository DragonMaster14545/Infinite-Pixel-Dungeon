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



import com.shatteredpixel.shatteredpixeldungeon.utils.Big;
import java.math.BigInteger;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

import java.text.DecimalFormat;

public class RingOfExperience extends Ring {

	{
		icon = ItemSpriteSheet.Icons.RING_EXPERIENCE;
	}
	
	public String statsInfo() {
		if (isIdentified()){
			String info = Messages.get(this, "stats",
					new DecimalFormat("#.###").format(Big.dec(100f).multiply((Big.dec(1.02f).add(Big.dec(soloVisualBonus()).multiply(Big.dec(0.02))).subtract(Big.dec(1f))))));
			if (isEquipped(Dungeon.hero) && (soloBuffedBonus().compareTo(combinedBuffedBonus(Dungeon.hero)) != 0)){
				info += "\n\n" + Messages.get(this, "combined_stats",
						Messages.decimalFormat("#.##", Big.dec(100f).multiply((Big.dec(1.02f).add(Big.dec(combinedBuffedBonus(Dungeon.hero)).multiply(Big.dec(0.02))).subtract(Big.dec(1f)))).doubleValue()));
			}
			return info;
		} else {
			return Messages.get(this, "typical_stats", new DecimalFormat("#.###").format(20f));
		}
	}

	public String upgradeStat1(BigInteger level){
		if (cursed && cursedKnown) level = BigInteger.valueOf(-1).min(level.subtract(BigInteger.valueOf(3)));

		return Messages.decimalFormat("#.##", Big.dec(100f).multiply((Big.dec(1.02f).add(Big.dec(level).multiply(Big.dec(0.02))).subtract(Big.dec(1f)))).doubleValue()) + "%";
	}
	
	@Override
	protected RingBuff buff( ) {
		return new Experience();
	}
	
	public static float experienceMultiplier( Char target ){
        float multiplier = 1f;
        if ((getBuffedBonus(target, Experience.class).compareTo(BigInteger.ZERO) > 0)) multiplier = 1.02f;
        if ((getBuffedBonus(target, Experience.class).compareTo(BigInteger.ONE) > 0)) multiplier += Big.dec(getBuffedBonus(target, Experience.class)).multiply(Big.dec(0.02)).floatValue();

		return Math.min(multiplier, 10f * (1 + Dungeon.cycle + Dungeon.hero.upgrades.ringExpansion()));
	}
	
	public class Experience extends RingBuff {
	}
}
