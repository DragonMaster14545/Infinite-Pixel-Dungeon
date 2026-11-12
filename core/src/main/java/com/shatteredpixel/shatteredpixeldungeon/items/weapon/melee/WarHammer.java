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
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

public class WarHammer extends MeleeWeapon {

	{
		image = ItemSpriteSheet.WAR_HAMMER;
		hitSound = Assets.Sounds.HIT_CRUSH;
		hitSoundPitch = 0.5f;

		internalTier = tier = 5;
		ACC = 1.20f; //20% boost to accuracy
        DLY = 2f;
	}

	@Override
	public BigInteger max(BigInteger lvl) {
		return  BigInteger.valueOf(20L*(tier()+1)).add(BigInteger.valueOf(5).multiply(lvl).multiply(BigInteger.valueOf((tier()+1))));   //scaling is 5x
	}

	@Override
	public BigInteger min(BigInteger lvl) {
		return  BigInteger.valueOf(tier()*12L).add(lvl.multiply(BigInteger.TEN));    //level scaling
	}

	@Override
	public String targetingPrompt() {
		return Messages.get(this, "prompt");
	}

	@Override
	protected void duelistAbility(Hero hero, Integer target) {
		//+(6+1.5*lvl) damage, roughly +40% base dmg, +45% scaling
		BigInteger dmgBoost = augment.damageFactor(BigInteger.valueOf(tier()*10L).add(buffedLvl().multiply(BigInteger.valueOf(tier())).multiply(BigInteger.valueOf(12))));
		Mace.heavyBlowAbility(hero, target, 1, dmgBoost, this);
	}

	public String upgradeAbilityStat(BigInteger level){
		int dmgBoost = Big.toInt(BigInteger.valueOf(6).add(Big.round(Big.dec(1.5f).multiply(Big.dec(level)))));
		return augment.damageFactor(min(level).add(BigInteger.valueOf(dmgBoost))) + "-" + augment.damageFactor(max(level).add(BigInteger.valueOf(dmgBoost)));
	}

	@Override
	public String abilityInfo() {
		BigInteger dmgBoost = levelKnown ? BigInteger.valueOf(tier()*10L).add(buffedLvl().multiply(BigInteger.valueOf(tier())).multiply(BigInteger.valueOf(12))) : BigInteger.valueOf(tier()*10L);
		if (levelKnown){
			return Messages.get(this, "ability_desc", augment.damageFactor(min().add(dmgBoost)), augment.damageFactor(max().add(dmgBoost)));
		} else {
			return Messages.get(this, "typical_ability_desc", min(BigInteger.ZERO).add(dmgBoost), max(BigInteger.ZERO).add(dmgBoost));
		}
	}

	@Override
    public BigInteger proc(Char attacker, Char defender, BigInteger damage) {
        attacker.sprite.centerEmitter().start( Speck.factory( Speck.STAR ), 0.05f, 10 );
        Buff.affect(attacker, Paralysis.class, Random.Int(1, 3)
				* (Math.round((1f/(1d + (speedMultiplier(attacker) - 1d) * 0.75f))*100))/100f);
        Buff.affect(defender, Paralysis.class, Random.Int(1, 3));
        return super.proc(attacker, defender, damage);
    }
}
