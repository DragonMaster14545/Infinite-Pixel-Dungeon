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

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ShieldBuff;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.utils.Random;

public class Sharpened extends Weapon.Enchantment {
	
	private static ItemSprite.Glowing COLOR = new ItemSprite.Glowing( 0xd1ff75 );
	
	@Override
	public long proc(Weapon weapon, Char attacker, Char defender, long damage) {
		
		long level = Math.max( 0, weapon.buffedLvl() );

		// lvl 0 - 10%
		// lvl 1 ~ 12%
		// lvl 2 ~ 14%
		float procChance = (level+4f)/(level+40f) * procChanceMultiplier(attacker);
		if (Random.Float() < procChance && defender.buff(Bleeding.class) == null){
			float powerMulti = Math.max(1f, procChance);

			Bleeding b = defender.buff(Bleeding.class);
			if (b == null){
				b = new Bleeding();
			}
			b.set(damage*(0.1f + powerMulti));
			b.attachTo(defender);
		}
		
		return damage;
	}
	
	@Override
	public ItemSprite.Glowing glowing() {
		return COLOR;
	}

}
