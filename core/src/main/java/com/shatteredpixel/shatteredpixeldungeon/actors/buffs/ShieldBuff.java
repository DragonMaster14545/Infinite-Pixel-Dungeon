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
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.watabou.utils.Bundle;

public abstract class ShieldBuff extends Buff {
	
	private BigInteger shielding = BigInteger.ZERO;
	
	@Override
	public boolean attachTo(Char target) {
		if (super.attachTo(target)) {
			target.needsShieldUpdate = true;
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void detach() {
		target.needsShieldUpdate = true;
		super.detach();
	}
	
	public BigInteger shielding(){
		return shielding;
	}
	
	public void setShield( BigInteger shield ) {
		if ((this.shielding.compareTo(shield) <= 0)) this.shielding = shield;
		if (target != null) target.needsShieldUpdate = true;
	}
	
	public void incShield(){
		incShield(BigInteger.ONE);
	}

	public void incShield( BigInteger amt ){
		shielding = shielding.add(amt);
		if (target != null) target.needsShieldUpdate = true;
	}
	
	public void decShield(){
		decShield(BigInteger.ONE);
	}

	public void decShield( BigInteger amt ){
		shielding = shielding.subtract(amt);
		if (target != null) target.needsShieldUpdate = true;
	}
	
	//returns the amount of damage leftover
	public BigInteger absorbDamage( BigInteger dmg ){
		if ((shielding.compareTo(dmg) >= 0)){
			shielding = shielding.subtract(dmg);
			dmg = BigInteger.ZERO;
		} else {
			dmg = dmg.subtract(shielding);
			shielding = BigInteger.ZERO;
		}
		if ((shielding.compareTo(BigInteger.ZERO) == 0)){
			detach();
		}
		if (target != null) target.needsShieldUpdate = true;
		return dmg;
	}
	
	private static final String SHIELDING = "shielding";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		Big.put(bundle, SHIELDING, shielding);
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		shielding = Big.getBig(bundle, SHIELDING);
	}
	
}
