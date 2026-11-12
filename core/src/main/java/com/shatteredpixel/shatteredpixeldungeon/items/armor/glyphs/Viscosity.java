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

package com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs;



import com.shatteredpixel.shatteredpixeldungeon.utils.Big;
import java.math.BigInteger;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor.Glyph;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite.Glowing;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

public class Viscosity extends Glyph {
	
	private static ItemSprite.Glowing PURPLE = new ItemSprite.Glowing( 0x8844CC );
	
	@Override
	public BigInteger proc( Armor armor, Char attacker, Char defender, BigInteger damage ) {

		//we use a tracker so that this glyph can apply after armor
		Buff.affect(defender, ViscosityTracker.class).level = armor.buffedLvl();

		return damage;
		
	}

	@Override
	public Glowing glowing() {
		return PURPLE;
	}

	public static class ViscosityTracker extends Buff {

		{
			actPriority = Actor.VFX_PRIO;
		}

		private BigInteger level = BigInteger.ZERO;

		public BigInteger deferDamage(BigInteger dmg){
			//account for icon stomach (just skip the glyph)
			if (target.buff(Talent.WarriorFoodImmunity.class) != null){
				return dmg;
			}

			BigInteger level = BigInteger.ZERO.max(this.level);

			float percent = Big.div(Big.dec((level.add(BigInteger.ONE))), Big.dec((level.add(BigInteger.valueOf(6))).floatValue())).floatValue();
			percent *= genericProcChanceMultiplier(target);

			BigInteger amount;
			if (percent > 1f){
				dmg = Big.round(Big.div(Big.dec(dmg), Big.dec(percent)));
				amount = dmg;
			} else {
				amount = Big.trunc(Big.ceil(Big.dec(dmg).multiply(Big.dec(percent))));
			}

			if ((amount.compareTo(BigInteger.ZERO) > 0)){
				DeferedDamage deferred = Buff.affect( target, DeferedDamage.class );
				deferred.prolong( amount );

				target.sprite.showStatus( CharSprite.WARNING, Messages.get(Viscosity.class, "deferred", amount) );
			}

			return dmg.subtract(amount);
		}

		@Override
		public boolean act() {
			detach();
			return true;
		}
	};

	public static class DeferedDamage extends Buff {
		
		{
			type = buffType.NEGATIVE;
		}

        public BigInteger damage = BigInteger.ZERO;
		
		private static final String DAMAGE	= "damage";
		
		@Override
		public void storeInBundle( Bundle bundle ) {
			super.storeInBundle( bundle );
			Big.put(bundle, DAMAGE, damage);
			
		}
		
		@Override
		public void restoreFromBundle( Bundle bundle ) {
			super.restoreFromBundle( bundle );
			damage = Big.getBig(bundle, DAMAGE);
		}
		
		public void prolong( BigInteger damage ) {
			if ((this.damage.compareTo(BigInteger.ZERO) == 0)){
				//wait 1 turn before damaging if this is freshly applied
				postpone(TICK);
			}
			this.damage = this.damage.add(damage);
		}
		
		@Override
		public int icon() {
			return BuffIndicator.DEFERRED;
		}

		@Override
		public String iconTextDisplay() {
			return damage.toString();
		}


		@Override
		public boolean act() {
			if (target.isAlive()) {

				BigInteger damageThisTick = BigInteger.ONE.max(damage.divide(BigInteger.TEN));
				target.damage( damageThisTick, this );
				if (target == Dungeon.hero && !target.isAlive()) {

					Badges.validateDeathFromFriendlyMagic();

					Dungeon.fail( this );
					GLog.n( Messages.get(this, "ondeath") );
				}
				spend( TICK );

				damage = damage.subtract(damageThisTick);
				if ((damage.compareTo(BigInteger.ZERO) <= 0)) {
					detach();
				}
				
			} else {
				
				detach();
				
			}
			
			return true;
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", damage);
		}
	}
}
