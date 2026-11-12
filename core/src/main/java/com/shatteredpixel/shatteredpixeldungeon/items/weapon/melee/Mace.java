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
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Daze;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfDisintegration;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.AttackIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

public class Mace extends MeleeWeapon {

	{
		image = ItemSpriteSheet.MACE;
		hitSound = Assets.Sounds.HIT_CRUSH;
		hitSoundPitch = 1f;

		internalTier = tier = 3;
		ACC = 1.28f; //28% boost to accuracy
	}

	@Override
	public BigInteger max(BigInteger lvl) {
		return  BigInteger.valueOf((5L*tier())).add(lvl.multiply(BigInteger.valueOf((tier()+2))));   //+5
	}

    @Override
    public BigInteger proc(Char attacker, Char defender, BigInteger damage) {
	    if (attacker == Dungeon.hero && defender != null) {
			WandOfDisintegration wand = ((WandOfDisintegration)(new WandOfDisintegration().upgrade((Big.round(Big.dec(level()).multiply(Big.dec(Math.pow(3, Dungeon.cycle))))))));
			curUser = Dungeon.hero;
	    	wand.fx(
	    		new Ballistica(attacker.pos, defender.pos, Ballistica.STOP_SOLID),
				new Callback() {
					public void call() {
						wand.onZap(new Ballistica(attacker.pos, defender.pos, Ballistica.STOP_SOLID));
						wand.wandUsed();
					}
				}

		);
	    }
        return super.proc(attacker, defender, damage);
    }
	@Override
	public String targetingPrompt() {
		return Messages.get(this, "prompt");
	}

	@Override
	protected void duelistAbility(Hero hero, Integer target) {
		//+(4+1.5*lvl) damage, roughly +55% base dmg, +60% scaling
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

	public static void heavyBlowAbility(Hero hero, Integer target, float dmgMulti, BigInteger dmgBoost, MeleeWeapon wep){
		if (target == null) {
			return;
		}

		Char enemy = Actor.findChar(target);
		if (enemy == null || enemy == hero || hero.isCharmedBy(enemy) || !Dungeon.level.heroFOV[target]) {
			GLog.w(Messages.get(wep, "ability_no_target"));
			return;
		}

		hero.belongings.abilityWeapon = wep;
		if (!hero.canAttack(enemy)){
			GLog.w(Messages.get(wep, "ability_target_range"));
			hero.belongings.abilityWeapon = null;
			return;
		}
		hero.belongings.abilityWeapon = null;

		//no bonus damage if attack isn't a surprise
		if (enemy instanceof Mob && !((Mob) enemy).surprisedBy(hero)){
			dmgMulti = Math.min(1, dmgMulti);
			dmgBoost = BigInteger.ZERO;
		}

		float finalDmgMulti = dmgMulti;
		BigInteger finalDmgBoost = dmgBoost;
		hero.sprite.attack(enemy.pos, new Callback() {
			@Override
			public void call() {
				wep.beforeAbilityUsed(hero, enemy);
				AttackIndicator.target(enemy);
				if (hero.attack(enemy, finalDmgMulti, finalDmgBoost.floatValue(), Char.INFINITE_ACCURACY)) {
					Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG);
					if (enemy.isAlive()){
						Buff.affect(enemy, Daze.class, Daze.DURATION);
					} else {
						wep.onAbilityKill(hero, enemy);
					}
				}
				Invisibility.dispel();
				hero.spendAndNext(hero.attackDelay());
				wep.afterAbilityUsed(hero);
			}
		});
	}

}
