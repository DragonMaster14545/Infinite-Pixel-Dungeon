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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Light;
import com.shatteredpixel.shatteredpixeldungeon.effects.Beam;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.RainbowParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

public class WandOfPrismaticLight extends DamageWand {

	{
		image = ItemSpriteSheet.WAND_PRISMATIC_LIGHT;

		collisionProperties = Ballistica.MAGIC_BOLT;
	}

	public BigInteger min(BigInteger lvl){
		return ((BigInteger.ONE.add(lvl)).multiply(getRarityMultiplier()));
	}

	public BigInteger max(BigInteger lvl){
		return ((BigInteger.valueOf(5).add(BigInteger.valueOf(3).multiply(lvl))).multiply(getRarityMultiplier()));
	}

	@Override
	public void onZap(Ballistica beam) {
		affectMap(beam);
		
		if (Dungeon.level.viewDistance < 6 ){
			if (Dungeon.isChallenged(Challenges.DARKNESS)){
				Buff.prolong( curUser, Light.class, Big.dec(2f).add(Big.dec(buffedLvl())).floatValue());
			} else {
				Buff.prolong( curUser, Light.class, Big.dec(10f).add(Big.dec(buffedLvl().multiply(BigInteger.valueOf(5)))).floatValue());
			}
		}
		
		Char ch = Actor.findChar(beam.collisionPos);
		if (ch != null){
			wandProc(ch, chargesPerCast());
			affectTarget(ch);
		}
	}

	private void affectTarget(Char ch){
		BigInteger dmg = damageRoll();

		//three in (5+lvl) chance of failing
		if ((Dungeon.Long(BigInteger.ZERO, BigInteger.valueOf(5).add(buffedLvl())).compareTo(BigInteger.valueOf(3)) >= 0)) {
			Buff.prolong(ch, Blindness.class, Big.dec(2f).add((Big.dec(buffedLvl()).multiply(Big.dec(0.333f)))).floatValue());
			ch.sprite.emitter().burst(Speck.factory(Speck.LIGHT), 6 );
		}

		if (ch.properties().contains(Char.Property.DEMONIC) || ch.properties().contains(Char.Property.UNDEAD)){
			ch.sprite.emitter().start( ShadowParticle.UP, 0.05f,  Big.particles(Math.sqrt(BigInteger.TEN.add(buffedLvl()).doubleValue())));
			Sample.INSTANCE.play(Assets.Sounds.BURNING);

			ch.damage(Big.round(Big.dec(dmg).multiply(Big.dec(1.333f))), this);
		} else {
			ch.sprite.centerEmitter().burst( RainbowParticle.BURST,  Big.particles(Math.sqrt(BigInteger.TEN.add(buffedLvl()).doubleValue())));

			ch.damage(dmg, this);
		}

	}

	private void affectMap(Ballistica beam){
		boolean noticed = false;
		for (int c : beam.subPath(0, beam.dist)){
			if (!Dungeon.level.insideMap(c)){
				continue;
			}
			for (int n : PathFinder.NEIGHBOURS9){
				int cell = c+n;

				if (Dungeon.level.discoverable[cell])
					Dungeon.level.mapped[cell] = true;

				int terr = Dungeon.level.map[cell];
				if ((Terrain.flags[terr] & Terrain.SECRET) != 0) {

					Dungeon.level.discover( cell );

					GameScene.discoverTile( cell, terr );
					ScrollOfMagicMapping.discover(cell);

					noticed = true;
				}
			}

			CellEmitter.center(c).burst( RainbowParticle.BURST, Random.IntRange( 1, 2 ) );
		}
		if (noticed)
			Sample.INSTANCE.play( Assets.Sounds.SECRET );

		GameScene.updateFog();
	}

	@Override
	public String upgradeStat2(BigInteger level) {
		return Messages.decimalFormat("#", 100*(1-(3/(BigInteger.valueOf(5).add(level)).floatValue()))) + "%";
	}

	@Override
	public String upgradeStat3(BigInteger level) {
		if (Dungeon.isChallenged(Challenges.DARKNESS)){
			return BigInteger.valueOf(2).add(level).toString();
		} else {
			return BigInteger.TEN.add(BigInteger.valueOf(5).multiply(level)).toString();
		}
	}

	@Override
	public void fx(Ballistica beam, Callback callback) {
		curUser.sprite.parent.add(
				new Beam.LightRay(curUser.sprite.center(), DungeonTilemap.raisedTileCenterToWorld(beam.collisionPos)));
		callback.call();
	}

	@Override
	public void onHit(MagesStaff staff, Char attacker, Char defender, BigInteger damage) {
		//cripples enemy
		Buff.prolong( defender, Cripple.class, Big.round(Big.dec((BigInteger.ONE.add(staff.buffedLvl()))).multiply(Big.dec(procChanceMultiplier(attacker)))).floatValue());
	}

	@Override
	public void staffFx(MagesStaff.StaffParticle particle) {
		particle.color( Dungeon.Int( 0x1000000 ) );
		particle.am = 0.5f;
		particle.setLifespan(1f);
		particle.speed.polar(Random.Float(PointF.PI2), 2f);
		particle.setSize( 1f, 2f);
		particle.radiateXY( 0.5f);
	}

}
