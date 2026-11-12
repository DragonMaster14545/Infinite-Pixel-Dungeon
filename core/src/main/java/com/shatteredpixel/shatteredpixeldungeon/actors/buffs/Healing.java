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
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.VialOfBlood;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;
import com.watabou.utils.GameMath;

public class Healing extends Buff {

	private BigInteger healingLeft = BigInteger.ZERO;
	
	private float percentHealPerTick;
	private BigInteger flatHealPerTick = BigInteger.ZERO;

	private boolean healingLimited = false;
	
	{
		//unlike other buffs, this one acts after the hero and takes priority against other effects
		//healing is much more useful if you get some of it off before taking damage
		actPriority = HERO_PRIO - 1;
		
		type = buffType.POSITIVE;
	}
	
	@Override
	public boolean act(){

		if ((target.HP.compareTo(target.HT) < 0)) {
			target.HP = target.HT.min(target.HP.add(healingThisTick()));

			if ((target.HP.compareTo(target.HT) == 0) && target instanceof Hero) {
				((Hero) target).resting = false;
			}
		}

		target.sprite.showStatusWithIcon(CharSprite.POSITIVE, healingThisTick().toString(), FloatingText.HEALING);
		healingLeft = healingLeft.subtract(healingThisTick());
		
		if ((healingLeft.compareTo(BigInteger.ZERO) <= 0)){
			if (target instanceof Hero) {
				((Hero) target).resting = false;
			}
			detach();
		}
		
		spend( TICK );
		
		return true;
	}
	
	private BigInteger healingThisTick(){
		BigInteger heal = Big.of(GameMath.gate(1d,
				Big.round(Big.dec(healingLeft).multiply(Big.dec(percentHealPerTick)).multiply(Big.dec(1d))).add(flatHealPerTick).floatValue(),
				healingLeft.floatValue()));
		if (healingLimited && (heal.compareTo(VialOfBlood.maxHealPerTurn()) > 0)){
			heal = VialOfBlood.maxHealPerTurn();
		}
		return heal;
	}

	public void setHeal(BigInteger amount, float percentPerTick, BigInteger flatPerTick){
		//multiple sources of healing do not overlap, but do combine the best of their properties
		healingLeft = healingLeft.max(amount);
		percentHealPerTick = Math.max(percentHealPerTick, percentPerTick);
		flatHealPerTick = flatHealPerTick.max(flatPerTick);
	}

	public void applyVialEffect(){
		healingLimited = VialOfBlood.delayBurstHealing();
		if (healingLimited){
			healingLeft = Big.round(Big.dec(healingLeft).multiply(Big.dec(VialOfBlood.totalHealMultiplier())));
		}
	}

	public void increaseHeal( BigInteger amount ){
		healingLeft = healingLeft.add(amount);
	}
	
	@Override
	public void fx(boolean on) {
		if (on) target.sprite.add( CharSprite.State.HEALING );
		else    target.sprite.remove( CharSprite.State.HEALING );
	}
	
	private static final String LEFT = "left";
	private static final String PERCENT = "percent";
	private static final String FLAT = "flat";

	private static final String HEALING_LIMITED = "healing_limited";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		Big.put(bundle, LEFT, healingLeft);
		bundle.put(PERCENT, percentHealPerTick);
		Big.put(bundle, FLAT, flatHealPerTick);
		bundle.put(HEALING_LIMITED, healingLimited);
	}
	
	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		healingLeft = Big.getBig(bundle, LEFT);
		percentHealPerTick = bundle.getFloat(PERCENT);
		flatHealPerTick = Big.getBig(bundle, FLAT);
		healingLimited = bundle.getBoolean(HEALING_LIMITED);
	}
	
	@Override
	public int icon() {
		return BuffIndicator.HEALING;
	}

	@Override
	public String iconTextDisplay() {
		return healingLeft.toString();
	}
	
	@Override
	public String desc() {
		return Messages.get(this, "desc", healingThisTick(), healingLeft);
	}
}
