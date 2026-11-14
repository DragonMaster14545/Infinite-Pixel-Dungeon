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
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Random;

public class Scorching extends Weapon.Enchantment {

	private static ItemSprite.Glowing COLOR = new ItemSprite.Glowing( 0xf58442 );

	@Override
	public BigInteger proc(Weapon weapon, Char attacker, Char defender, BigInteger damage) {
		BigInteger level = BigInteger.ZERO.max(weapon.buffedLvl());

		float procChance = Big.div((Big.dec(level).add(Big.dec(1f))), (Big.dec(level).add(Big.dec(3f)))).multiply(Big.dec(procChanceMultiplier(attacker))).floatValue();
		float powerMulti = Math.min(2f, procChance);

		if ((Big.dec(damage).compareTo(Big.dec(defender.HT).multiply(Big.dec(0.07d))) > 0) && !((damage.compareTo(defender.HT) > 0))) {
			defender.damage(Big.trunc((Big.dec(damage).multiply(Big.dec(powerMulti)))), this);
			defender.sprite.emitter().burst( FlameParticle.FACTORY, Big.particles(Math.sqrt(level.add(BigInteger.ONE).doubleValue())));
		}

		return damage;
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return COLOR;
	}

}
