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

package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;



import com.shatteredpixel.shatteredpixeldungeon.utils.Big;
import java.math.BigInteger;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLivingEarth;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Earthroot;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class ChaliceOfBlood extends Artifact {

	{
		image = ItemSpriteSheet.ARTIFACT_CHALICE1;

		levelCap = 10;
	}

	public static final String AC_PRICK = "PRICK";

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if (isEquipped( hero )
				&& (level().compareTo(BigInteger.valueOf(levelCap)) < 0)
				&& !cursed
				&& !hero.isInvulnerable(getClass())
				&& hero.buff(MagicImmune.class) == null)
			actions.add(AC_PRICK);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action ) {
		super.execute(hero, action);

		if (action.equals(AC_PRICK)){

			BigInteger damage = BigInteger.valueOf(Big.toInt((BigInteger.valueOf(5).add(BigInteger.valueOf(3).multiply((level().multiply(level())))))));

			if ((Big.dec(damage).compareTo(Big.dec(hero.HP).multiply(Big.dec(0.75))) > 0)) {

				GameScene.show(
					new WndOptions(new ItemSprite(this),
							Messages.titleCase(name()),
							Messages.get(this, "prick_warn"),
							Messages.get(this, "yes"),
							Messages.get(this, "no")) {
						@Override
						protected void onSelect(int index) {
							if (index == 0)
								prick(Dungeon.hero);
						}
					}
				);

			} else {
				prick(hero);
			}
		}
	}

	private void prick(Hero hero){
		BigInteger damage = BigInteger.valueOf(Big.toInt((BigInteger.valueOf(5).add(BigInteger.valueOf(3).multiply((level().multiply(level())))))));

		Earthroot.Armor armor = hero.buff(Earthroot.Armor.class);
		if (armor != null) {
			damage = armor.absorb(damage);
		}

		WandOfLivingEarth.RockArmor rockArmor = hero.buff(WandOfLivingEarth.RockArmor.class);
		if (rockArmor != null) {
			damage = rockArmor.absorb(damage);
		}

		damage = damage.subtract(hero.drRoll());

		hero.sprite.operate( hero.pos );
		hero.busy();
		hero.spend(3f);
		GLog.w( Messages.get(this, "onprick") );
		if ((damage.compareTo(BigInteger.ZERO) <= 0)){
			damage = BigInteger.ONE;
		} else {
			Sample.INSTANCE.play(Assets.Sounds.CURSED);
			hero.sprite.emitter().burst( ShadowParticle.CURSE, Big.particles(BigInteger.valueOf(4).add(damage.divide(BigInteger.TEN))) );
		}

		hero.damage(damage, this);

		if (!hero.isAlive()) {
			Badges.validateDeathFromFriendlyMagic();
			Dungeon.fail( this );
			GLog.n( Messages.get(this, "ondeath") );
		} else {
			upgrade();
			Catalog.countUse(getClass());
		}
	}

	@Override
	public Item upgrade() {
		if ((level().compareTo(BigInteger.valueOf(6)) >= 0))
			image = ItemSpriteSheet.ARTIFACT_CHALICE3;
		else if ((level().compareTo(BigInteger.valueOf(2)) >= 0))
			image = ItemSpriteSheet.ARTIFACT_CHALICE2;
		return super.upgrade();
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		if ((level().compareTo(BigInteger.valueOf(7)) >= 0)) image = ItemSpriteSheet.ARTIFACT_CHALICE3;
		else if ((level().compareTo(BigInteger.valueOf(3)) >= 0)) image = ItemSpriteSheet.ARTIFACT_CHALICE2;
	}

	@Override
	protected ArtifactBuff passiveBuff() {
		return new chaliceRegen();
	}
	
	@Override
	public void charge(Hero target, float amount) {
		if (cursed || target.buff(MagicImmune.class) != null) return;

		//grants 5 turns of healing up-front, if hero isn't starving
		if (target.isStarving()) return;

		double healDelay = Big.dec(1d).max(Big.dec(10d).subtract((Big.dec(1.33d).add(Big.dec(level()).multiply(Big.dec(0.667d)))))).doubleValue();
		healDelay /= amount;
		double heal = 5d/healDelay;
		//effectively 0.5/1/1.5/2/2.5 HP per turn at +0/+6/+8/+9/+10
		//apply rarity first, so the fractional part is rounded probabilistically
		//instead of being truncated away afterward
		//(which made regen actually gives a little against what the item's desc says)
		heal *= getRarityMultiplier().doubleValue();
		BigInteger healAmount = Big.of(heal);
		if ((Big.dec(Random.Double()).compareTo(Big.dec(heal).subtract(Big.dec(healAmount))) < 0)){
			healAmount = healAmount.add(BigInteger.ONE);
		}
		if ((healAmount.compareTo(BigInteger.ONE) >= 0) && (target.HP.compareTo(target.HT) < 0)) {
			target.HP = target.HT.min(target.HP.add(healAmount));
			target.sprite.showStatusWithIcon(CharSprite.POSITIVE, healAmount.toString(), FloatingText.HEALING);

			if ((target.HP.compareTo(target.HT) == 0) && target instanceof Hero) {
				((Hero) target).resting = false;
			}
		}
	}
	
	@Override
	public String desc() {
		String desc = super.desc();

		if (isEquipped (Dungeon.hero)){
			desc += "\n\n";
			if (cursed)
				desc += Messages.get(this, "desc_cursed");
			else if ((level().compareTo(BigInteger.ZERO) == 0))
				desc += Messages.get(this, "desc_1");
			else if ((level().compareTo(BigInteger.valueOf(levelCap)) < 0))
				desc += Messages.get(this, "desc_2");
			else
				desc += Messages.get(this, "desc_3");
		}

		desc += Messages.get(this, "hp", ((level().compareTo(BigInteger.TEN) < 0) ? Big.div(Big.dec(1f), (Big.dec(10f).subtract((Big.dec(level()).multiply(Big.dec(0.9f)))))) : Big.dec(((3 * Math.pow(level().subtract(BigInteger.ONE).doubleValue(), 2) / 243)))).multiply(Big.dec(getRarityMultiplier())));

		return desc;
	}

	public class chaliceRegen extends ArtifactBuff {
		//see Regeneration.class for effect
	}

}
