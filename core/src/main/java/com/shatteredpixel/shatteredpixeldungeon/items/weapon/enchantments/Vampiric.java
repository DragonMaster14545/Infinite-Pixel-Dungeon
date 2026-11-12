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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;



import com.shatteredpixel.shatteredpixeldungeon.utils.Big;
import java.math.BigInteger;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite.Glowing;
import com.watabou.utils.Random;

public class Vampiric extends Weapon.Enchantment {

	private static ItemSprite.Glowing RED = new ItemSprite.Glowing( 0x660022 );
	
	@Override
	public BigInteger proc( Weapon weapon, Char attacker, Char defender, BigInteger damage ) {
		
		//chance to heal scales from 10%-60% based on missing HP
		double missingPercent = Big.div(Big.dec((attacker.HT.subtract(attacker.HP))), Big.dec(attacker.HT.doubleValue())).doubleValue();
		double healChance = 0.1f + .5f*missingPercent;

		healChance *= procChanceMultiplier(attacker);
		
		if (Random.Double() < healChance){

			double powerMulti = Math.max(1f, healChance);
			
			//heals for 200% of damage dealt
			BigInteger healAmt = Big.round(Big.dec(damage).multiply(Big.dec(2f)).multiply(Big.dec(powerMulti)));
			healAmt = healAmt.min(attacker.HT.subtract(attacker.HP));
			
			if ((healAmt.compareTo(BigInteger.ZERO) > 0) && attacker.isAlive()) {
				
				attacker.HP = attacker.HP.add(healAmt);
				attacker.sprite.showStatusWithIcon( CharSprite.POSITIVE, healAmt.toString(), FloatingText.HEALING );
				
			}
		}

		return damage;
	}
	
	@Override
	public Glowing glowing() {
		return RED;
	}
}
