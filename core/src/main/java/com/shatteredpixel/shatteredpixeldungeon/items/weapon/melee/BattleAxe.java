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

public class BattleAxe extends MeleeWeapon {

	{
		image = ItemSpriteSheet.BATTLE_AXE;
		hitSound = Assets.Sounds.HIT_SLASH;
		hitSoundPitch = 0.9f;

		internalTier = tier = 4;
		ACC = 1.24f; //24% boost to accuracy
	}

	@Override
	public BigInteger max(BigInteger lvl) {
		return  BigInteger.valueOf(5L*(tier()+1)).add(lvl.multiply(BigInteger.valueOf((tier()+2))));   //+6
	}

	@Override
	public String targetingPrompt() {
		return Messages.get(this, "prompt");
	}

	@Override
	protected void duelistAbility(Hero hero, Integer target) {
		//+(5+1.5*lvl) damage, roughly +40% base dmg, +50% scaling
		BigInteger dmgBoost = augment.damageFactor(BigInteger.valueOf(5).add(Big.round(Big.dec(1.5d).multiply(Big.dec(buffedLvl())))));
		Mace.heavyBlowAbility(hero, target, 1, dmgBoost, this);
	}

	@Override
	public String abilityInfo() {
		int dmgBoost = Big.toInt(levelKnown ? BigInteger.valueOf(5).add(Big.round(Big.dec(1.5f).multiply(Big.dec(buffedLvl())))) : BigInteger.valueOf(5));
		if (levelKnown){
			return Messages.get(this, "ability_desc", augment.damageFactor(min().add(BigInteger.valueOf(dmgBoost))), augment.damageFactor(max().add(BigInteger.valueOf(dmgBoost))));
		} else {
			return Messages.get(this, "typical_ability_desc", min(BigInteger.ZERO).add(BigInteger.valueOf(dmgBoost)), max(BigInteger.ZERO).add(BigInteger.valueOf(dmgBoost)));
		}
	}

	public String upgradeAbilityStat(BigInteger level){
		int dmgBoost = Big.toInt(BigInteger.valueOf(5).add(Big.round(Big.dec(1.5f).multiply(Big.dec(level)))));
		return augment.damageFactor(min(level).add(BigInteger.valueOf(dmgBoost))) + "-" + augment.damageFactor(max(level).add(BigInteger.valueOf(dmgBoost)));
	}

}
