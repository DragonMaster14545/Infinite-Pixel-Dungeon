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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.watabou.utils.GameMath;

public class Racked extends Weapon.Enchantment {

	private static ItemSprite.Glowing COLOR = new ItemSprite.Glowing( 0x3244a8 );

	@Override
	public long proc(Weapon weapon, Char attacker, Char defender, long damage) {

		if (attacker.buff(StackTracker.class) != null && attacker.buff(StackTracker.class).stacks >= 15) {
			defender.damage((long) (damage * 2d), this);
			attacker.buff(StackTracker.class).resetStacks();
			Buff.affect(attacker, RackedEnchantmentCooldown.class, 5f);
		}

		if (attacker.buff(RackedEnchantmentCooldown.class) == null) {
			Buff.affect(attacker, StackTracker.class).stacks++;
		}

		return damage;
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return COLOR;
	}

	public static class StackTracker extends Buff {

		public int stacks = 0;

		@Override
		public int icon() {
			return BuffIndicator.COMBO;
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", stacks);
		}

		@Override
		public void tintIcon(Image icon) {
			if (stacks >= 10){
				icon.hardlight(1f + (stacks - 10f)*.2f, 0f, 0f);
			} else if (stacks >= 5) {
				icon.hardlight(1f, 1f - (stacks - 5f)*.2f, 0f);
			} else {
				icon.hardlight(1f, 1f, 1f - stacks*.2f);
			}
		}

		public void resetStacks() {
			stacks = 0;
			detach();
		}

		public static String STACKS = "stacks";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(STACKS, stacks);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			stacks = bundle.getInt(STACKS);
		}
	}

	public static class RackedEnchantmentCooldown extends FlavourBuff {
		public int icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(1f, 0.35f, 0.15f); }
		public float iconFadePercent() { return GameMath.gate(0, visualcooldown() / (5), 1); }
	};

}
