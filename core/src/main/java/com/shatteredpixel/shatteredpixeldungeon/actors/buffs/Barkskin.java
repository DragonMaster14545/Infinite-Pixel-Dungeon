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
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

public class Barkskin extends Buff {
	
	{
		type = buffType.POSITIVE;
	}

	private BigInteger level = BigInteger.ZERO;
	private int interval = 1;
	
	@Override
	public boolean act() {
		if (target.isAlive()) {

			spend( interval );
			if (((level = level.subtract(BigInteger.ONE)).compareTo(BigInteger.ZERO) <= 0)) {
				detach();
			}
			
		} else {
			
			detach();
			
		}
		
		return true;
	}
	
	public BigInteger level() {
		return level;
	}
	
	public void set( BigInteger value, int time ) {
		if ((level.compareTo(value) <= 0)) {
			level = value;
			interval = time;
			spend(time - cooldown() - 1);
		}
	}
	
	@Override
	public int icon() {
		return BuffIndicator.BARKSKIN;
	}

	@Override
	public float iconFadePercent() {
		if (target instanceof Hero){
			float max = ((Hero) target).lvl*((Hero) target).pointsInTalent(Talent.BARKSKIN)/2;
			max = Math.max(max, 2+((Hero) target).lvl/3);
			return Big.dec(0).max(Big.div((Big.dec(max).subtract(Big.dec(level))), Big.dec(max))).floatValue();
		}
		return 0;
	}

	@Override
	public String iconTextDisplay() {
		return level.toString();
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", level, dispTurns(visualcooldown()));
	}
	
	private static final String LEVEL	    = "level";
	private static final String INTERVAL    = "interval";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( INTERVAL, interval );
		Big.put(bundle, LEVEL, level);
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		interval = bundle.getInt( INTERVAL );
		level = Big.getBig(bundle, LEVEL);
	}

	//These two methods allow for multiple instances of barkskin to stack in terms of duration
	// but only the stronger bonus is applied

	public static BigInteger currentLevel(Char ch ){
		BigInteger level = BigInteger.ZERO;
		for (Barkskin b : ch.buffs(Barkskin.class)){
			level = level.max(b.level);
		}
		return level;
	}

	//reset if a matching buff exists, otherwise append
	public static void conditionallyAppend(Char ch, BigInteger level, int interval){
		for (Barkskin b : ch.buffs(Barkskin.class)){
			if (b.interval == interval){
				b.set(level, interval);
				return;
			}
		}
		Buff.append(ch, Barkskin.class).set(level, interval);
	}
}
