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



import com.shatteredpixel.shatteredpixeldungeon.utils.Big;
import java.math.BigInteger;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class HandAxe extends MeleeWeapon {

	{
		image = ItemSpriteSheet.HAND_AXE;
		hitSound = Assets.Sounds.HIT_SLASH;
		hitSoundPitch = 1f;

		internalTier = tier = 2;
		ACC = 1.32f; //32% boost to accuracy
	}

	@Override
	public BigInteger max(BigInteger lvl) {
		return  BigInteger.valueOf(5L*(tier()+1)).add(lvl.multiply(BigInteger.valueOf((tier()+2))));   //+4
	}

	@Override
	public String targetingPrompt() {
		return Messages.get(this, "prompt");
	}

	@Override
	protected void duelistAbility(Hero hero, Integer target) {
		//+(4+1.5*lvl) damage, roughly +55% base dmg, +75% scaling
		BigInteger dmgBoost = augment.damageFactor(BigInteger.valueOf(4).add(Big.round(Big.dec(1.5d).multiply(Big.dec(buffedLvl())))));
		Mace.heavyBlowAbility(hero, target, 1, dmgBoost, this);
	}

	@Override
	public String abilityInfo() {
		BigInteger dmgBoost = levelKnown ? BigInteger.valueOf(4).add(Big.round(Big.dec(1.5d).multiply(Big.dec(buffedLvl())))) : BigInteger.valueOf(4);
		if (levelKnown){
			return Messages.get(this, "ability_desc", augment.damageFactor(min().add(dmgBoost)), augment.damageFactor(max().add(dmgBoost)));
		} else {
			return Messages.get(this, "typical_ability_desc", min(BigInteger.ZERO).add(dmgBoost), max(BigInteger.ZERO).add(dmgBoost));
		}
	}

	public String upgradeAbilityStat(BigInteger level){
		int dmgBoost = Big.toInt(BigInteger.valueOf(4).add(Big.round(Big.dec(1.5f).multiply(Big.dec(level)))));
		return augment.damageFactor(min(level).add(BigInteger.valueOf(dmgBoost))) + "-" + augment.damageFactor(max(level).add(BigInteger.valueOf(dmgBoost)));
	}

}
