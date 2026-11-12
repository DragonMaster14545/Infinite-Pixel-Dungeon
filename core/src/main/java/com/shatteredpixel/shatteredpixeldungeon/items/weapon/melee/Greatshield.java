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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;


import java.math.BigInteger;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class Greatshield extends MeleeWeapon {

	{
		image = ItemSpriteSheet.GREATSHIELD;

		internalTier = tier = 5;
	}

	@Override
	public BigInteger max(BigInteger lvl) {
		return  BigInteger.valueOf(Math.round(3d*(tier()+1))).add(lvl.multiply(BigInteger.valueOf((tier()-1))));                   //+4 per level, down from +7
	}

	@Override
	public BigInteger defenseFactor( Char owner ) {
		return DRMax();
	}

	public BigInteger DRMax(){
		return DRMax(buffedLvl());
	}

	//6 extra defence, plus 2 per level
	public BigInteger DRMax(BigInteger lvl){
		return BigInteger.valueOf((tier()+1)).add(BigInteger.valueOf((tier()-2)).multiply(lvl));
	}
	
	public String statsInfo(){
		if (isIdentified()){
			return Messages.get(this, "stats_desc", BigInteger.valueOf((tier()+1)).add(BigInteger.valueOf((tier()-2)).multiply(buffedLvl())));
		} else {
			return Messages.get(this, "typical_stats_desc", (tier()+1));
		}
	}

	@Override
	protected void duelistAbility(Hero hero, Integer target) {
		RoundShield.guardAbility(hero, BigInteger.valueOf(3).add(buffedLvl().divide(BigInteger.valueOf(300))), this);
	}

	@Override
	public String abilityInfo() {
		if (levelKnown){
			return Messages.get(this, "ability_desc", BigInteger.valueOf(3).add(buffedLvl().divide(BigInteger.valueOf(300))));
		} else {
			return Messages.get(this, "typical_ability_desc", 3);
		}
	}

	@Override
	public String upgradeAbilityStat(BigInteger level) {
		return BigInteger.valueOf(3).add(level).toString();
	}
}
