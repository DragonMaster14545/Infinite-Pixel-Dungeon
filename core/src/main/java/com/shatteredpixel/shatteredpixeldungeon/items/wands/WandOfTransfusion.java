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
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.Beam;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BloodParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

public class WandOfTransfusion extends DamageWand {

	{
		image = ItemSpriteSheet.WAND_TRANSFUSION;

		collisionProperties = Ballistica.PROJECTILE;
	}

	@Override
	public BigInteger min(BigInteger level) {
		return ((BigInteger.valueOf(3).add(level)).multiply(getRarityMultiplier()));
	}

	@Override
	public BigInteger max(BigInteger level) {
		return ((BigInteger.valueOf(6).add(BigInteger.TWO.multiply(level))).multiply(getRarityMultiplier()));
	}

	private boolean freeCharge = false;

	@Override
    public void onZap(Ballistica beam) {

		for (int c : beam.subPath(0, beam.dist))
			CellEmitter.center(c).burst( BloodParticle.BURST, 1 );

		int cell = beam.collisionPos;

		Char ch = Actor.findChar(cell);

		if (ch instanceof Mob){
			
			wandProc(ch, chargesPerCast());
			
			//this wand does different things depending on the target.
			
			//heals/shields an ally or a charmed enemy while damaging self
			if (ch.alignment == Char.Alignment.ALLY || ch.buff(Charm.class) != null){
				
				// 5% of max hp
				BigInteger selfDmg = Big.round(Big.dec(curUser.HT).multiply(Big.dec(0.05f)));
				
				BigInteger healing = (selfDmg.add(BigInteger.valueOf(3).multiply(buffedLvl())));
				BigInteger shielding = (ch.HP.add(healing)).subtract(ch.HT);
				if ((shielding.compareTo(BigInteger.ZERO) > 0)){
					healing = healing.subtract(shielding);
					Buff.affect(ch, Barrier.class).setShield(shielding);
				} else {
					shielding = BigInteger.ZERO;
				}
				
				ch.HP = ch.HP.add(healing);
				
				ch.sprite.emitter().burst(Speck.factory(Speck.HEALING),  (int) Math.min(Math.sqrt(Big.dec(2).add(Big.div(Big.dec(buffedLvl()), Big.dec(2f))).doubleValue()), 1000));
				if ((healing.compareTo(BigInteger.ZERO) > 0)) {
					ch.sprite.showStatusWithIcon(CharSprite.POSITIVE, healing.toString(), FloatingText.HEALING);
				}
				if ((shielding.compareTo(BigInteger.ZERO) > 0)){
					ch.sprite.showStatusWithIcon(CharSprite.POSITIVE, shielding.toString(), FloatingText.SHIELDING);
				}
				
				if (!freeCharge) {
					damageHero(selfDmg);
				} else {
					freeCharge = false;
				}

			//for enemies...
			//(or for mimics which are hiding, special case)
			} else if (ch.alignment == Char.Alignment.ENEMY || ch instanceof Mimic) {

				//grant a self-shield, and...
				Buff.affect(curUser, Barrier.class).setShield((BigInteger.valueOf(7).add(buffedLvl())));
curUser.sprite.showStatusWithIcon(CharSprite.POSITIVE, BigInteger.valueOf(7).add(buffedLvl()).toString(), FloatingText.SHIELDING);

				//charms living enemies
				if (!ch.properties().contains(Char.Property.UNDEAD)) {
					Charm charm = Buff.affect(ch, Charm.class, Charm.DURATION/2f);
					charm.object = curUser.id();
					charm.ignoreHeroAllies = true;
					ch.sprite.centerEmitter().start( Speck.factory( Speck.HEART ), 0.2f,  (int) Math.sqrt(3 ) );
				
				//harms the undead
				} else {
					ch.damage(Big.trunc((Big.dec(Big.normalRange(BigInteger.valueOf(3).add(buffedLvl()), BigInteger.valueOf(6).add(BigInteger.TWO.multiply(buffedLvl())))).multiply(Big.dec((1+ Dungeon.hero.lvl/150f))))), this);
					ch.sprite.emitter().start(ShadowParticle.UP, 0.05f,  (int) Math.min(Math.sqrt(BigInteger.TEN.add(buffedLvl()).doubleValue()), 1000));
					Sample.INSTANCE.play(Assets.Sounds.BURNING);
				}

			}
			
		}
		
	}

	//this wand costs health too
	private void damageHero(BigInteger damage){
		
		curUser.damage(damage, this);

		if (!curUser.isAlive()){
			Badges.validateDeathFromFriendlyMagic();
			Dungeon.fail( this );
			GLog.n( Messages.get(this, "ondeath") );
		}
	}

	@Override
	public void onHit(MagesStaff staff, Char attacker, Char defender, BigInteger damage) {
		if (defender.buff(Charm.class) != null && defender.buff(Charm.class).object == attacker.id()){
			//grants a free use of the staff and shields self
			freeCharge = true;
			int shieldToGive = Big.toInt(Big.round(Big.dec((BigInteger.TWO.multiply((BigInteger.valueOf(7).add(buffedLvl()))))).multiply(Big.dec(procChanceMultiplier(attacker)))));
			Buff.affect(attacker, Barrier.class).setShield(BigInteger.valueOf(shieldToGive));
			attacker.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(shieldToGive), FloatingText.SHIELDING);
			GLog.p( Messages.get(this, "charged") );
			attacker.sprite.emitter().burst(BloodParticle.BURST, 20);
		}
	}

	@Override
	public void fx(Ballistica beam, Callback callback) {
		curUser.sprite.parent.add(
				new Beam.HealthRay(curUser.sprite.center(), DungeonTilemap.raisedTileCenterToWorld(beam.collisionPos)));
		callback.call();
	}

	@Override
	public void staffFx(MagesStaff.StaffParticle particle) {
		particle.color( 0xCC0000 );
		particle.am = 0.6f;
		particle.setLifespan(1f);
		particle.speed.polar( Random.Float(PointF.PI2), 2f );
		particle.setSize( 1f, 2f);
		particle.radiateXY(0.5f);
	}

	@Override
	public String statsDesc() {
		BigInteger selfDMG = Dungeon.hero != null ? Big.round(Big.dec(Dungeon.hero.HT).multiply(Big.dec(0.05d))): BigInteger.ONE;
		if (levelKnown)
			return Messages.get(this, "stats_desc", selfDMG, selfDMG.add(BigInteger.valueOf(3).multiply(buffedLvl())), BigInteger.valueOf(7).add(buffedLvl()), min(), max()) + "\n\n" + Messages.get(Wand.class, "charges", curCharges, getMaxCharges());
		else
			return Messages.get(this, "stats_desc", selfDMG, selfDMG, 7, min(BigInteger.ZERO), max(BigInteger.ZERO));
	}

	@Override
	public String upgradeStat1(BigInteger level) {
		BigInteger selfDMG = Dungeon.hero != null ? Big.round(Big.dec(Dungeon.hero.HT).multiply(Big.dec(0.05f))): BigInteger.ONE;
		return selfDMG.add(BigInteger.valueOf(3).multiply(level)).toString();
	}

	@Override
	public String upgradeStat2(BigInteger level) {
		return BigInteger.valueOf(7).add(level).toString();
	}

	@Override
	public String upgradeStat3(BigInteger level) {
		return super.upgradeStat1(level); //damage
	}

	private static final String FREECHARGE = "freecharge";

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		freeCharge = bundle.getBoolean( FREECHARGE );
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put( FREECHARGE, freeCharge );
	}

}
