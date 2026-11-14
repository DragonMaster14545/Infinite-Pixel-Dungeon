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
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.mage.WildMagic;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blazing;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ConeAOE;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.GameMath;
import com.watabou.utils.PathFinder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WandOfFireblast extends DamageWand {

	{
		image = ItemSpriteSheet.WAND_FIREBOLT;

		//only used for targeting, actual projectile logic is Ballistica.STOP_SOLID | Ballistica.IGNORE_SOFT_SOLID
		collisionProperties = Ballistica.WONT_STOP;
	}

	//1/2/3 base damage with 1/2/3 scaling based on charges used
	public BigInteger min(BigInteger lvl){
		return ((BigInteger.ONE.add(lvl)).multiply(chargesPerCast()).multiply(getRarityMultiplier()));
	}

	//2/8/18 base damage with 2/4/6 scaling based on charges used
	public BigInteger max(BigInteger lvl){
		switch (Big.toInt(chargesPerCast())){
			case 1: default:
				return ((BigInteger.valueOf(2).add(BigInteger.valueOf(2).multiply(lvl))).multiply(getRarityMultiplier()));
			case 2:
				return ((BigInteger.valueOf(2).multiply((BigInteger.valueOf(4).add(BigInteger.valueOf(2).multiply(lvl))))).multiply(getRarityMultiplier()));
			case 3:
				return ((BigInteger.valueOf(3).multiply((BigInteger.valueOf(6).add(BigInteger.valueOf(2).multiply(lvl))))).multiply(getRarityMultiplier()));
		}
	}

	ConeAOE cone;

	@Override
	public void onZap(Ballistica bolt) {

		ArrayList<Char> affectedChars = new ArrayList<>();
		ArrayList<Integer> adjacentCells = new ArrayList<>();
		for( int cell : cone.cells ){

			//ignore caster cell
			if (cell == bolt.sourcePos){
				continue;
			}

			//knock doors open
			if (Dungeon.level.map[cell] == Terrain.DOOR){
				Level.set(cell, Terrain.OPEN_DOOR);
				GameScene.updateMap(cell);
			}

			//only ignite cells directly near caster if they are flammable or solid
			if (Dungeon.level.adjacent(bolt.sourcePos, cell)
					&& !(Dungeon.level.flamable[cell] || Dungeon.level.solid[cell])){
				adjacentCells.add(cell);
				//do burn any heaps located here though
				if (Dungeon.level.heaps.get(cell) != null){
					Dungeon.level.heaps.get(cell).burn();
				}
			} else {
				GameScene.add( Blob.seed( cell, BigInteger.valueOf(Big.toInt(BigInteger.valueOf(Integer.MAX_VALUE-1).min(BigInteger.ONE.add(chargesPerCast())))), Fire.class ) );
			}

			Char ch = Actor.findChar( cell );
			if (ch != null) {
				affectedChars.add(ch);
			}
		}

		//if wand was shot right at a wall
		if (cone.cells.isEmpty()){
			adjacentCells.add(bolt.sourcePos);
		}

		//ignite cells that share a side with an adjacent cell, are flammable, and are closer to the collision pos
		//This prevents short-range casts not igniting barricades or bookshelves
		for (int cell : adjacentCells){
			for (int i : PathFinder.NEIGHBOURS8){
				if (Dungeon.level.trueDistance(cell+i, bolt.collisionPos) < Dungeon.level.trueDistance(cell, bolt.collisionPos)
						&& Dungeon.level.flamable[cell+i]
						&& (Fire.volumeAt(cell+i, Fire.class).compareTo(BigInteger.ZERO) == 0)){
					GameScene.add( Blob.seed( cell+i, BigInteger.valueOf(Big.toInt(BigInteger.valueOf(Integer.MAX_VALUE-1).min(BigInteger.ONE.add(chargesPerCast())))), Fire.class ) );
				}
			}
		}

		for ( Char ch : affectedChars ){
			wandProc(ch, chargesPerCast());
			ch.damage(Big.round(Big.dec(damageRoll()).multiply(Big.dec(Dungeon.fireDamage))), this);
			if (ch.isAlive()) {
				Buff.affect(ch, Burning.class).reignite(ch);
				switch (Big.toInt(chargesPerCast())) {
					case 1:
						break; //no effects
					case 2:
						Buff.affect(ch, Cripple.class, 4f);
						break;
					case 3:
						Buff.affect(ch, Paralysis.class, 4f);
						break;
				}
			}
		}
	}

	@Override
	public void onHit(MagesStaff staff, Char attacker, Char defender, BigInteger damage) {
		//acts like blazing enchantment
		new FireBlastOnHit().proc( staff, attacker, defender, damage);
	}

	public static class FireBlastOnHit extends Blazing {
		@Override
		protected float procChanceMultiplier(Char attacker) {
			return Wand.procChanceMultiplier(attacker);
		}
	}

    @Override
    public List<Integer> aimTiles(int target) {
        if (cursed && cursedKnown) {
            return super.aimTiles(target);
        }
        Ballistica b = new Ballistica(Dungeon.hero.pos, target, Ballistica.WONT_STOP);
        ConeAOE tempCone = new ConeAOE( b,
                BigInteger.valueOf(3).add(BigInteger.valueOf(2).multiply(chargesPerCast())).floatValue(),
                BigInteger.valueOf(30).add(BigInteger.valueOf(20).multiply(chargesPerCast())).floatValue(),
                Ballistica.STOP_TARGET | Ballistica.STOP_SOLID | Ballistica.IGNORE_SOFT_SOLID);
        return Arrays.asList(tempCone.cells.toArray(new Integer[0]));
    }

	@Override
	public void fx(Ballistica bolt, Callback callback) {
		//need to perform flame spread logic here so we can determine what cells to put flames in.

		// 5/7/9 distance
		int maxDist = Big.toInt((BigInteger.valueOf(3).add(BigInteger.valueOf(2).multiply(chargesPerCast()))));

		cone = new ConeAOE( bolt,
				maxDist,
				BigInteger.valueOf(30).add(BigInteger.valueOf(20).multiply(chargesPerCast())).floatValue(),
				Ballistica.STOP_TARGET | Ballistica.STOP_SOLID | Ballistica.IGNORE_SOFT_SOLID);

		//cast to cells at the tip, rather than all cells, better performance.
		Ballistica longestRay = null;
		for (Ballistica ray : cone.outerRays){
			if (longestRay == null || ray.dist > longestRay.dist){
				longestRay = ray;
			}
			((MagicMissile)curUser.sprite.parent.recycle( MagicMissile.class )).reset(
					MagicMissile.FIRE_CONE,
					curUser.sprite,
					ray.path.get(ray.dist),
					null
			);
		}

		//final zap at half distance of the longest ray, for timing of the actual wand effect
		MagicMissile.boltFromChar( curUser.sprite.parent,
				MagicMissile.FIRE_CONE,
				curUser.sprite,
				longestRay.path.get(longestRay.dist/2),
				callback );
		Sample.INSTANCE.play( Assets.Sounds.ZAP );
		Sample.INSTANCE.play( Assets.Sounds.BURNING );
	}

	@Override
	protected BigInteger chargesPerCast() {
		if (cursed ||
				(charger != null && charger.target == null && charger.target.buff(WildMagic.WildMagicTracker.class) != null)){
			return BigInteger.ONE;
		}
		//consumes 30% of current charges, rounded up, with a min of 1 and a max of 3.
		return Big.of(GameMath.gate(1, Big.trunc(Big.ceil(Big.dec(curCharges).multiply(Big.dec(0.3d)))).floatValue(), 3));
	}

	@Override
	public String statsDesc() {
		if (levelKnown)
			return Messages.get(this, "stats_desc", chargesPerCast(), Big.round(Big.dec(min()).multiply(Big.dec(Dungeon.fireDamage))), Big.round(Big.dec(max()).multiply(Big.dec(Dungeon.fireDamage)))) + "\n\n" + Messages.get(Wand.class, "charges", curCharges, getMaxCharges());
		else
			return Messages.get(this, "stats_desc", chargesPerCast(), min(BigInteger.ZERO), max(BigInteger.ZERO));
	}

	@Override
	public String upgradeStat1(BigInteger level) {
		return (BigInteger.ONE.add(level)) + "-" + (BigInteger.valueOf(2).add(BigInteger.valueOf(2).multiply(level)));
	}

	@Override
	public String upgradeStat2(BigInteger level) {
		return (BigInteger.valueOf(2).add(BigInteger.valueOf(2).multiply(level))) + "-" + BigInteger.valueOf(2).multiply((BigInteger.valueOf(4).add(BigInteger.valueOf(2).multiply(level))));
	}

	@Override
	public String upgradeStat3(BigInteger level) {
		return (BigInteger.valueOf(3).add(BigInteger.valueOf(3).multiply(level))) + "-" + BigInteger.valueOf(3).multiply((BigInteger.valueOf(6).add(BigInteger.valueOf(2).multiply(level))));
	}

	@Override
	public void staffFx(MagesStaff.StaffParticle particle) {
		particle.color( 0xEE7722 );
		particle.am = 0.5f;
		particle.setLifespan(0.6f);
		particle.acc.set(0, -40);
		particle.setSize( 0f, 3f);
		particle.shuffleXY( 1.5f );
	}

}
