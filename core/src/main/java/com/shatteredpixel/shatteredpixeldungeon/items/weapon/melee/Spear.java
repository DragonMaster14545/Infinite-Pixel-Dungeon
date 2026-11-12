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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.AttackIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

public class Spear extends MeleeWeapon {

	{
		image = ItemSpriteSheet.SPEAR;
		hitSound = Assets.Sounds.HIT_STAB;
		hitSoundPitch = 0.9f;

		internalTier = tier = 2;
		DLY = 1.5f; //0.67x speed
		RCH = 2;    //extra reach
	}

	@Override
	public BigInteger max(BigInteger lvl) {
		return  BigInteger.valueOf(Math.round(8d*(tier()+1))).add(lvl.multiply(BigInteger.valueOf(Math.round(1.66d*(tier()+2))))); //+6.64 per level, up from +4
	}

	@Override
	public String targetingPrompt() {
		return Messages.get(this, "prompt");
	}

	@Override
	protected void duelistAbility(Hero hero, Integer target) {
		//+(9+2*lvl) damage, roughly +83% base damage, +80% scaling
		BigInteger dmgBoost = augment.damageFactor(BigInteger.valueOf(9).add(Big.round(Big.dec(2f).multiply(Big.dec(buffedLvl())))));
		Spear.spikeAbility(hero, target, 1, dmgBoost, this);
	}

	@Override
	public String abilityInfo() {
		BigInteger dmgBoost = levelKnown ? BigInteger.valueOf(9).add(Big.round(Big.dec(2f).multiply(Big.dec(buffedLvl())))) : BigInteger.valueOf(9);
		if (levelKnown){
			return Messages.get(this, "ability_desc", augment.damageFactor(min().add(dmgBoost)), augment.damageFactor(max().add(dmgBoost)));
		} else {
			return Messages.get(this, "typical_ability_desc", min(BigInteger.ZERO).add(dmgBoost), max(BigInteger.ZERO).add(dmgBoost));
		}
	}

	public String upgradeAbilityStat(BigInteger level){
		int dmgBoost = Big.toInt(BigInteger.valueOf(9).add(Big.round(Big.dec(2f).multiply(Big.dec(level)))));
		return augment.damageFactor(min(level).add(BigInteger.valueOf(dmgBoost))) + "-" + augment.damageFactor(max(level).add(BigInteger.valueOf(dmgBoost)));
	}

	public static void spikeAbility(Hero hero, Integer target, float dmgMulti, BigInteger dmgBoost, MeleeWeapon wep){
		if (target == null) {
			return;
		}

		Char enemy = Actor.findChar(target);
		if (enemy == null || enemy == hero || hero.isCharmedBy(enemy) || !Dungeon.level.heroFOV[target]) {
			GLog.w(Messages.get(wep, "ability_no_target"));
			return;
		}

		hero.belongings.abilityWeapon = wep;
		if (!hero.canAttack(enemy) || Dungeon.level.adjacent(hero.pos, enemy.pos)){
			GLog.w(Messages.get(wep, "ability_target_range"));
			hero.belongings.abilityWeapon = null;
			return;
		}
		hero.belongings.abilityWeapon = null;

		hero.sprite.attack(enemy.pos, new Callback() {
			@Override
			public void call() {
				wep.beforeAbilityUsed(hero, enemy);
				AttackIndicator.target(enemy);
				int oldPos = enemy.pos;
				//do not push if enemy has moved, or another push is active (e.g. elastic)
				if (hero.attack(enemy, dmgMulti, dmgBoost.floatValue(), Char.INFINITE_ACCURACY)) {
					if (enemy.isAlive() && enemy.pos == oldPos && !Pushing.pushingExistsForChar(enemy)){
						//trace a ballistica to our target (which will also extend past them
						Ballistica trajectory = new Ballistica(hero.pos, enemy.pos, Ballistica.STOP_TARGET);
						//trim it to just be the part that goes past them
						trajectory = new Ballistica(trajectory.collisionPos, trajectory.path.get(trajectory.path.size() - 1), Ballistica.PROJECTILE);
						//knock them back along that ballistica
						WandOfBlastWave.throwChar(enemy, trajectory, BigInteger.ONE, true, false, hero);
					} else if (!enemy.isAlive()) {
						wep.onAbilityKill(hero, enemy);
					}
					Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG);
				}
				Invisibility.dispel();
				hero.spendAndNext(hero.attackDelay());
				wep.afterAbilityUsed(hero);
			}
		});
	}

}
