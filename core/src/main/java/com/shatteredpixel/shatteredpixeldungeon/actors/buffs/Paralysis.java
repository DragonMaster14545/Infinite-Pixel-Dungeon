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

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;



import com.shatteredpixel.shatteredpixeldungeon.utils.Big;
import java.math.BigInteger;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class Paralysis extends FlavourBuff {

	public static final float DURATION	= 10f;

	{
		type = buffType.NEGATIVE;
		announced = true;
	}
	
	@Override
	public boolean attachTo( Char target ) {
		if (super.attachTo( target )) {
			target.paralysed++;
			return true;
		} else {
			return false;
		}
	}
	
	public void processDamage( BigInteger damage ){
		if (target == null) return;
		ParalysisResist resist = target.buff(ParalysisResist.class);
		if (resist == null){
			resist = Buff.affect(target, ParalysisResist.class);
		}
		resist.damage = resist.damage.add(damage);
		if ((Big.normalRange(BigInteger.ZERO, resist.damage).compareTo(Big.normalRange(BigInteger.ZERO, target.HP)) >= 0)){
			if (Dungeon.level.heroFOV[target.pos]) {
				target.sprite.showStatus(CharSprite.NEUTRAL, Messages.get(this, "out"));
			}
			detach();
		}
	}
	
	@Override
	public void detach() {
		super.detach();
		if (target.paralysed > 0)
			target.paralysed--;
	}
	
	@Override
	public int icon() {
		return BuffIndicator.PARALYSIS;
	}

	@Override
	public float iconFadePercent() {
		return Math.max(0, (DURATION - visualcooldown()) / DURATION);
	}

	@Override
	public void fx(boolean on) {
		if (on)                         target.sprite.add(CharSprite.State.PARALYSED);
		else if (target.paralysed <= 1) target.sprite.remove(CharSprite.State.PARALYSED);
	}

	public static class ParalysisResist extends Buff {
		
		{
			type = buffType.POSITIVE;
		}
		
		private BigInteger damage = BigInteger.ZERO;
		
		@Override
		public boolean act() {
			if (target.buff(Paralysis.class) == null) {
				damage = Big.trunc(Big.dec(damage).subtract(Big.ceil(Big.div(Big.dec(damage), Big.dec(10f)))));
				if ((damage.compareTo(BigInteger.ZERO) >= 0)) detach();
			}
			spend(TICK);
			return true;
		}
		
		private static final String DAMAGE = "damage";
		
		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			damage = Big.getBig(bundle, DAMAGE);
		}
		
		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			Big.put(bundle, DAMAGE, damage);
		}
	}
}
