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
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Bundle;

public class Gloves extends MeleeWeapon {

	{
		image = ItemSpriteSheet.GLOVES;
		hitSound = Assets.Sounds.HIT;
		hitSoundPitch = 1.3f;

		internalTier = tier = 1;
		DLY = 0.5f; //2x speed
		
		bones = false;
	}

	@Override
	public BigInteger max(BigInteger lvl) {
		if (Dungeon.hero != null && Dungeon.hero.buff(AttackBuff.class) != null){
			return  Big.of((3d*(tier()+1))).add(lvl.multiply(BigInteger.valueOf(tier()))).add(BigInteger.valueOf(Dungeon.hero.buff(AttackBuff.class).stack));
		}
		return  Big.roundD(2.5d*(tier()+1)).add(lvl.multiply(Big.roundD(0.5d*(tier()+1))));  //+1 per level, down from +2
	}

	@Override
	public BigInteger proc(Char attacker, Char defender, BigInteger damage) {
		if ((damage.compareTo(BigInteger.ZERO) == 0)) Buff.affect(attacker, AttackBuff.class, 2f).stack++;
		return super.proc(attacker, defender, damage);
	}

	public static class AttackBuff extends FlavourBuff {

		public static final float DURATION = 2f;

		public int stack = 0;

		{
			type = buffType.NEGATIVE;
			announced = false;
		}

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put("stack", stack);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			stack = bundle.getInt("stack");
		}
	}

	@Override
	public String targetingPrompt() {
		return Messages.get(this, "prompt");
	}

	@Override
	protected void duelistAbility(Hero hero, Integer target) {
		//+(2+0.5*lvl) damage, roughly +67% base damage, +50% scaling
		BigInteger dmgBoost = augment.damageFactor(BigInteger.TWO.add(Big.round(Big.dec(0.5d).multiply(Big.dec(buffedLvl())))));
		Sai.comboStrikeAbility(hero, target, 0, dmgBoost, this);
	}

	@Override
	public String abilityInfo() {
		int dmgBoost = Big.toInt(levelKnown ? BigInteger.TWO.add(Big.round(Big.dec(0.5f).multiply(Big.dec(buffedLvl())))) : BigInteger.TWO);
		if (levelKnown){
			return Messages.get(this, "ability_desc", augment.damageFactor(BigInteger.valueOf(dmgBoost)));
		} else {
			return Messages.get(this, "typical_ability_desc", augment.damageFactor(BigInteger.valueOf(dmgBoost)));
		}
	}

	public String upgradeAbilityStat(BigInteger level){
		return "+" + augment.damageFactor(BigInteger.TWO.add(Big.round(Big.dec(0.5f).multiply(Big.dec(level)))));
	}

}
