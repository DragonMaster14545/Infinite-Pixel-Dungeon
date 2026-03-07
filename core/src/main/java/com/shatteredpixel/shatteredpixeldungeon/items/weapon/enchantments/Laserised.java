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
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfDisintegration;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Longsword;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;

public class Laserised extends Weapon.Enchantment {

	private static ItemSprite.Glowing COLOR = new ItemSprite.Glowing( 0xfc3503 );

	@Override
	public long proc( Weapon weapon, Char attacker, Char defender, long damage ) {
        if (attacker == Dungeon.hero && defender != null) {
            WandOfDisintegration wand = ((WandOfDisintegration)(new WandOfDisintegration().upgrade(Math.round(weapon.level()))));
            wand.fx(
                    new Ballistica(attacker.pos, defender.pos, Ballistica.STOP_SOLID),
                    new Callback() {
                        public void call() {
                            wand.onZap(new Ballistica(attacker.pos, defender.pos, Ballistica.STOP_SOLID));
                            wand.wandUsed();
                        }
                    }

            );
        }
		
		return damage;
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return COLOR;
	}
}
