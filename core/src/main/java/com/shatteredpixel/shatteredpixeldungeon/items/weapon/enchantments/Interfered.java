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

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Electricity;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.effects.Lightning;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Interfered extends Weapon.Enchantment {

	private static ItemSprite.Glowing WHITE = new ItemSprite.Glowing( 0xFFFFFF, 0.2f );

	@Override
	public long proc( Weapon weapon, Char attacker, Char defender, long damage ) {
		long level = Math.max( 0, weapon.buffedLvl() );

		// lvl 0 - 12.5%
		// lvl 1 - 20%
		// lvl 2 - 27.27%
		float procChance = (level+1f)/(level+8f) * procChanceMultiplier(attacker);
		if (Random.Float() < procChance) {

			double powerMulti = Math.max(0.9d, procChance);
			Buff.affect(defender, InterferedApplier.class).applyMulti(powerMulti);

		}

		return damage;

	}

	@Override
	public ItemSprite.Glowing glowing() {
		return WHITE;
	}

	public static class InterferedApplier extends Buff {

		public double multi = 0d;
		public void applyMulti(double mult) {
			multi = mult;
		}

		@Override
		public boolean act() {

			target.damage(Math.round(Dungeon.hero.damageRoll() * (0.1d + multi)), new Burning());
			target.damage(Math.round(Dungeon.hero.damageRoll() * (0.1d + multi)), new Chill());
			target.damage(Math.round(Dungeon.hero.damageRoll() * (0.1d + multi)), new Electricity());

			detach();
			return true;
		}
	}
}
