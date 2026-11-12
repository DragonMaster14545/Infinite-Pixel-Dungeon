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
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite.Glowing;
import com.watabou.utils.Random;

public class Blazing extends Weapon.Enchantment {

	private static ItemSprite.Glowing ORANGE = new ItemSprite.Glowing( 0xFF4400 );
	
	@Override
	public BigInteger proc( Weapon weapon, Char attacker, Char defender, BigInteger damage ) {
		BigInteger level = BigInteger.ZERO.max(weapon.buffedLvl());

		// lvl 0 - 33%
		// lvl 1 - 50%
		// lvl 2 - 60%
		float procChance = Big.div((Big.dec(level).add(Big.dec(1f))), (Big.dec(level).add(Big.dec(3f)))).multiply(Big.dec(procChanceMultiplier(attacker))).floatValue();
		if (Random.Float() < procChance) {

			float powerMulti = Math.max(1f, procChance);

			if (defender.buff(Burning.class) == null){
				Buff.affect(defender, Burning.class).reignite(defender, 8f);
				powerMulti -= 1;
			}

			if (powerMulti > 0){
				BigInteger burnDamage = Dungeon.NormalLongRange( BigInteger.ONE, BigInteger.valueOf(3).add(Dungeon.scalingDepth().divide(BigInteger.valueOf(4))) );
				burnDamage = Big.round(Big.dec(burnDamage).multiply(Big.dec(0.67f)).multiply(Big.dec(powerMulti)));
				if ((burnDamage.compareTo(BigInteger.ZERO) > 0)) {
					defender.damage(burnDamage, this);
				}
			}
			
			defender.sprite.emitter().burst( FlameParticle.FACTORY, (int) Math.min(50, Math.sqrt(level.add(BigInteger.ONE).doubleValue())));
			
		}

		return damage;

	}
	
	@Override
	public Glowing glowing() {
		return ORANGE;
	}
}
