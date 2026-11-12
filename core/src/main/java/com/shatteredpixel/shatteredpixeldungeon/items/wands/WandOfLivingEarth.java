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

package com.shatteredpixel.shatteredpixeldungeon.items.wands;



import com.shatteredpixel.shatteredpixeldungeon.utils.Big;
import java.math.BigInteger;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.NPC;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EarthGuardianSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.ColorMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class WandOfLivingEarth extends DamageWand {
	
	{
		image = ItemSpriteSheet.WAND_LIVING_EARTH;
	}
	
	@Override
	public BigInteger min(BigInteger lvl) {
		return (BigInteger.valueOf(4).multiply(getRarityMultiplier()));
	}
	
	@Override
	public BigInteger max(BigInteger lvl) {
		return ((BigInteger.valueOf(6).add(BigInteger.TWO.multiply(lvl))).multiply(getRarityMultiplier()));
	}
	
	@Override
    public void onZap(Ballistica bolt) {
		Char ch = Actor.findChar(bolt.collisionPos);
		BigInteger damage = damageRoll();
		BigInteger armorToAdd = damage;

		EarthGuardian guardian = null;
		for (Mob m : Dungeon.level.mobs){
			if (m instanceof EarthGuardian){
				guardian = (EarthGuardian) m;
				break;
			}
		}

		RockArmor buff = curUser.buff(RockArmor.class);
		//only grant armor if we are shooting at an enemy, a hiding mimic, or the guardian
		if ((guardian == null || ch != guardian) && (ch == null
				|| ch.alignment == Char.Alignment.ALLY
				|| ch.alignment == Char.Alignment.NEUTRAL && !(ch instanceof Mimic))){
			armorToAdd = BigInteger.ZERO;
		} else {
			if (buff == null && guardian == null) {
				buff = Buff.affect(curUser, RockArmor.class);
			}
			if (buff != null) {
				buff.addArmor(buffedLvl(), armorToAdd);
			}
		}

		//shooting at the guardian
		if (guardian != null && guardian == ch){
			guardian.sprite.centerEmitter().burst(MagicMissile.EarthParticle.ATTRACT, (int) Math.min(Math.sqrt(Big.dec(8).add(Big.div(Big.dec(buffedLvl()), Big.dec(2f))).doubleValue()), 1000));
			guardian.setInfo(curUser, buffedLvl(), armorToAdd);
			wandProc(guardian, chargesPerCast());
			Sample.INSTANCE.play( Assets.Sounds.HIT_MAGIC, 1, 0.9f * Random.Float(0.87f, 1.15f) );

		//shooting the guardian at a location
		} else if (guardian == null && buff != null && (buff.armor.compareTo(buff.armorToGuardian()) >= 0)){

			//create a new guardian
			guardian = new EarthGuardian();
			guardian.setInfo(curUser, buffedLvl(), buff.armor);

			//if the collision pos is occupied (likely will be), then spawn the guardian in the
			//adjacent cell which is closes to the user of the wand.
			if (ch != null){

				ch.sprite.centerEmitter().burst(MagicMissile.EarthParticle.BURST,  (int) Math.min(Math.sqrt(Big.dec(5).add(Big.div(Big.dec(buffedLvl()), Big.dec(2f))).doubleValue()), 1000));

				wandProc(ch, chargesPerCast());
				ch.damage(damage, this);

				int closest = -1;
				boolean[] passable = Dungeon.level.passable;

				for (int n : PathFinder.NEIGHBOURS9) {
					int c = bolt.collisionPos + n;
					if (passable[c] && Actor.findChar( c ) == null
						&& (closest == -1 || (Dungeon.level.trueDistance(c, curUser.pos) < (Dungeon.level.trueDistance(closest, curUser.pos))))) {
						closest = c;
					}
				}

				if (closest == -1){
					if ((armorToAdd.compareTo(BigInteger.ZERO) > 0)) {curUser.sprite.centerEmitter().burst(MagicMissile.EarthParticle.ATTRACT,  (int) Math.min(Math.sqrt(Big.dec(8).add(Big.div(Big.dec(buffedLvl()), Big.dec(2f))).doubleValue()), 1000));
					}
					return; //do not spawn guardian or detach buff
				} else {
					guardian.pos = closest;
					GameScene.add(guardian, 1);
					Dungeon.level.occupyCell(guardian);
				}

				if (ch.alignment == Char.Alignment.ENEMY || ch.buff(Amok.class) != null) {
					guardian.aggro(ch);
				}

			} else {
				guardian.pos = bolt.collisionPos;
				GameScene.add(guardian, 1);
				Dungeon.level.occupyCell(guardian);
			}

			guardian.sprite.centerEmitter().burst(MagicMissile.EarthParticle.ATTRACT,  (int) Math.min(Math.sqrt(Big.dec(8).add(Big.div(Big.dec(buffedLvl()), Big.dec(2f))).doubleValue()), 1000));
			buff.detach();
			Sample.INSTANCE.play( Assets.Sounds.HIT_MAGIC, 1, 0.9f * Random.Float(0.87f, 1.15f) );

		//shooting at a location/enemy with no guardian being shot
		} else {

			if (ch != null) {

				ch.sprite.centerEmitter().burst(MagicMissile.EarthParticle.BURST,  (int) Math.min(Math.sqrt(Big.dec(5).add(Big.div(Big.dec(buffedLvl()), Big.dec(2f))).doubleValue()), 1000));

				wandProc(ch, chargesPerCast());
				ch.damage(damage, this);
				Sample.INSTANCE.play( Assets.Sounds.HIT_MAGIC, 1, 0.8f * Random.Float(0.87f, 1.15f) );
				
				if (guardian == null) {
					if ((armorToAdd.compareTo(BigInteger.ZERO) > 0)) {
						curUser.sprite.centerEmitter().burst(MagicMissile.EarthParticle.ATTRACT,  (int) Math.min(Math.sqrt(Big.dec(5).add(Big.div(Big.dec(buffedLvl()), Big.dec(2f))).doubleValue()), 1000));
					}
				} else {
					guardian.sprite.centerEmitter().burst(MagicMissile.EarthParticle.ATTRACT,  (int) Math.min(Math.sqrt(Big.dec(5).add(Big.div(Big.dec(buffedLvl()), Big.dec(2f))).doubleValue()), 1000));
					guardian.setInfo(curUser, buffedLvl(), armorToAdd);
					if (ch.alignment == Char.Alignment.ENEMY || ch.buff(Amok.class) != null) {
						guardian.aggro(ch);
					}
				}

			} else {
				Dungeon.level.pressCell(bolt.collisionPos);
			}
		}

	}

	@Override
	public String upgradeStat2(BigInteger level) {
		return BigInteger.valueOf(16).add(BigInteger.valueOf(8).multiply(level)).toString();
	}

	@Override
	public String upgradeStat3(BigInteger level) {
		if (Dungeon.isChallenged(Challenges.NO_ARMOR)){
			return level + "-" + (BigInteger.TWO.add(level));
		} else {
			return level + "-" + (BigInteger.valueOf(3).add((BigInteger.valueOf(3).multiply(level))));
		}
	}

	@Override
	public void fx(Ballistica bolt, Callback callback) {
		MagicMissile.boltFromChar(curUser.sprite.parent,
				MagicMissile.EARTH,
				curUser.sprite,
				bolt.collisionPos,
				callback);
		Sample.INSTANCE.play(Assets.Sounds.ZAP);
	}
	
	@Override
	public void onHit(MagesStaff staff, Char attacker, Char defender, BigInteger damage) {
		EarthGuardian guardian = null;
		for (Mob m : Dungeon.level.mobs){
			if (m instanceof EarthGuardian){
				guardian = (EarthGuardian) m;
				break;
			}
		}
		
		int armor = Big.toInt(Big.round(Big.dec(damage).multiply(Big.dec(0.33f)).multiply(Big.dec(procChanceMultiplier(attacker)))));

		if (guardian != null){
			guardian.sprite.centerEmitter().burst(MagicMissile.EarthParticle.ATTRACT,  (int) Math.min(Math.sqrt(Big.dec(5).add(Big.div(Big.dec(buffedLvl()), Big.dec(2f))).doubleValue()), 1000));
			guardian.setInfo(Dungeon.hero, buffedLvl(), BigInteger.valueOf(armor));
		} else {
			attacker.sprite.centerEmitter().burst(MagicMissile.EarthParticle.ATTRACT,  (int) Math.min(Math.sqrt(Big.dec(5).add(Big.div(Big.dec(buffedLvl()), Big.dec(2f))).doubleValue()), 1000));
			Buff.affect(attacker, RockArmor.class).addArmor(buffedLvl(), BigInteger.valueOf(armor));
		}
	}
	
	@Override
	public void staffFx(MagesStaff.StaffParticle particle) {
		if (Dungeon.Int(10) == 0){
			particle.color(ColorMath.random(0xFFF568, 0x80791A));
		} else {
			particle.color(ColorMath.random(0x805500, 0x332500));
		}
		particle.am = 1f;
		particle.setLifespan(2f);
		particle.setSize( 1f, 2f);
		particle.shuffleXY(0.5f);
		float dst = Random.Float(11f);
		particle.x -= dst;
		particle.y += dst;
	}

	public static class RockArmor extends Buff {

		{
			type = buffType.POSITIVE;
		}

		private BigInteger wandLevel = BigInteger.ZERO;
		private BigInteger armor = BigInteger.ZERO;

		private void addArmor( BigInteger wandLevel, BigInteger toAdd ){
			this.wandLevel = this.wandLevel.max(wandLevel);
			armor = armor.add(toAdd);
			armor = armor.min(BigInteger.TWO.multiply(armorToGuardian()));
		}

		private BigInteger armorToGuardian(){
			return BigInteger.valueOf(8).add(wandLevel.multiply(BigInteger.valueOf(4)));
		}

		public BigInteger absorb( BigInteger damage ) {
			BigInteger block = damage.subtract(damage.divide(BigInteger.TWO));
			if ((armor.compareTo(block) <= 0)) {
				detach();
				return damage.subtract(armor);
			} else {
				armor = armor.subtract(block);
				return damage.subtract(block);
			}
		}

		@Override
		public int icon() {
			return BuffIndicator.ARMOR;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.brightness(0.6f);
		}

		@Override
		public float iconFadePercent() {
			return Big.dec(0).max(Big.div(Big.dec((armorToGuardian().subtract(armor))), Big.dec(armorToGuardian().floatValue()))).floatValue();
		}

		@Override
		public String iconTextDisplay() {
			return armor.toString();
		}

		@Override
		public String desc() {
			return Messages.get( this, "desc", armor, armorToGuardian());
		}

		private static final String WAND_LEVEL = "wand_level";
		private static final String ARMOR = "armor";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			Big.put(bundle, WAND_LEVEL, wandLevel);
			Big.put(bundle, ARMOR, armor);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			wandLevel = BigInteger.valueOf(bundle.getInt(WAND_LEVEL));
			armor = BigInteger.valueOf(bundle.getInt(ARMOR));
		}
	}

	public static class EarthGuardian extends NPC {

		{
			spriteClass = EarthGuardianSprite.class;

			alignment = Alignment.ALLY;
			state = HUNTING;
			intelligentAlly = true;

			properties.add(Property.INORGANIC);

			WANDERING = new Wandering();

			//before other mobs
			actPriority = MOB_PRIO + 1;

			HP = HT = BigInteger.ZERO;
		}

		private BigInteger wandLevel = BigInteger.valueOf(-1);

		public void setInfo(Hero hero, BigInteger wandLevel, BigInteger healthToAdd){
			if ((wandLevel.compareTo(this.wandLevel) > 0)) {
				this.wandLevel = wandLevel;
				HT = (BigInteger.valueOf(16).add(BigInteger.valueOf(8).multiply(wandLevel)));
			}
			if ((HP.compareTo(BigInteger.ZERO) != 0)){
				sprite.showStatusWithIcon(CharSprite.POSITIVE, healthToAdd.toString(), FloatingText.HEALING);
			}
			HP = HT.min(HP.add(healthToAdd));
			//half of hero's evasion
			defenseSkill = BigInteger.valueOf((hero.lvl + 4)/2);
		}

		@Override
		public BigInteger attackSkill(Char target) {
			//same as the hero
			return BigInteger.TWO.multiply(defenseSkill).add(BigInteger.valueOf(5));
		}

		@Override
		public BigInteger attackProc(Char enemy, BigInteger damage) {
			if (enemy instanceof Mob) ((Mob)enemy).aggro(this);
			return super.attackProc(enemy, damage);
		}

		@Override
		public BigInteger damageRoll() {
			return Big.normalRange(BigInteger.TWO, BigInteger.valueOf(4).add(Dungeon.scalingDepth().divide(BigInteger.TWO)));
		}

		@Override
		public BigInteger drRoll() {
			BigInteger dr = super.drRoll();
			if (Dungeon.isChallenged(Challenges.NO_ARMOR)){
				return (dr.add(Big.normalRange(wandLevel, BigInteger.TWO.add(wandLevel))));
			} else {
				return (dr.add(Big.normalRange(wandLevel, BigInteger.valueOf(3).add(BigInteger.valueOf(3).multiply(wandLevel)))));
			}
		}

		@Override
		public String description() {
			String desc = Messages.get(this, "desc");

			if (Actor.chars().contains(this)) {
				if (Dungeon.isChallenged(Challenges.NO_ARMOR)) {
					desc += "\n\n" + Messages.get(this, "wand_info", wandLevel, BigInteger.TWO.add(wandLevel));
				} else {
					desc += "\n\n" + Messages.get(this, "wand_info", wandLevel, BigInteger.valueOf(3).add(BigInteger.valueOf(3).multiply(wandLevel)));
				}
			}

			return desc;

		}
		
		{
			immunities.add( AllyBuff.class );
		}

		private static final String DEFENSE = "defense";
		private static final String WAND_LEVEL = "wand_level";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			Big.put(bundle, DEFENSE, defenseSkill);
			Big.put(bundle, WAND_LEVEL, wandLevel);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			defenseSkill = BigInteger.valueOf(bundle.getInt(DEFENSE));
			wandLevel = Big.getBig(bundle, WAND_LEVEL);
		}

		private class Wandering extends Mob.Wandering{

			@Override
			public boolean act(boolean enemyInFOV, boolean justAlerted) {
				if (!enemyInFOV){
					Buff.affect(Dungeon.hero, RockArmor.class).addArmor(wandLevel, HP);
					Dungeon.hero.sprite.centerEmitter().burst(MagicMissile.EarthParticle.ATTRACT, Big.toInt((Big.dec(8).add(Big.div(Big.dec(wandLevel), Big.dec(2f))).min(Big.dec(1000))).toBigInteger()));
					destroy();
					sprite.die();
					return true;
				} else {
					return super.act(enemyInFOV, justAlerted);
				}
			}

		}

	}
}
