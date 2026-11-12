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

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;


import java.math.BigInteger;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.journal.Bestiary;
import com.shatteredpixel.shatteredpixeldungeon.plants.Rotberry;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RotHeartSprite;
import com.watabou.utils.PathFinder;

public class RotHeart extends Mob {

	{
		spriteClass = RotHeartSprite.class;

		HP = HT = Dungeon.getCycleMultiplier(BigInteger.valueOf(80));
		defenseSkill = BigInteger.ZERO;

		EXP = Dungeon.getCycleMultiplier(BigInteger.valueOf(4));

		state = PASSIVE;

		properties.add(Property.IMMOVABLE);
		properties.add(Property.MINIBOSS);
		properties.add(Property.STATIC);
	}

	@Override
	protected boolean act() {
		alerted = false;
		return super.act();
	}

	@Override
	public void damage(BigInteger dmg, Object src) {
		//TODO: when effect properties are done, change this to FIRE
		if (src instanceof Burning) {
			destroy();
			sprite.die();
		} else {
			super.damage(dmg, src);
		}
	}

	@Override
	public BigInteger defenseProc(Char enemy, BigInteger damage) {
		//rot heart spreads less gas in enclosed spaces
		int openNearby = 0;
		for (int i : PathFinder.NEIGHBOURS8){
			if (!Dungeon.level.solid[pos+i]){
				openNearby++;
			}
		}

		GameScene.add(Blob.seed(pos, BigInteger.valueOf(5 + 3*openNearby), ToxicGas.class));

		return super.defenseProc(enemy, damage);
	}

	@Override
	public void beckon(int cell) {
		//do nothing
	}

	@Override
	protected boolean getCloser(int target) {
		return false;
	}

	@Override
	public void destroy() {
		super.destroy();
		Bestiary.skipCountingEncounters = true;
		for (Mob mob : Dungeon.level.mobs.toArray(new Mob[Dungeon.level.mobs.size()])){
			if (mob instanceof RotLasher){
				mob.die(null);
			}
		}
		Bestiary.skipCountingEncounters = false;
	}

	@Override
	public void die(Object cause) {
		super.die(cause);
		Dungeon.level.drop( new Rotberry.Seed(), pos ).sprite.drop();
        //assign score here as player may choose to keep the rotberry seed
        Statistics.questScores[1] += 2000;
	}

	@Override
	public boolean reset() {
		return true;
	}

	@Override
	public BigInteger damageRoll() {
		return BigInteger.ZERO;
	}

	@Override
	public BigInteger attackSkill(Char target ) {
		return BigInteger.ZERO;
	}

	@Override
	public BigInteger cycledDrRoll() {
		return Dungeon.NormalLongRange(Dungeon.getCycleMultiplier(BigInteger.ZERO), Dungeon.getCycleMultiplier(BigInteger.valueOf(5)));
	}
	
	{
		immunities.add( ToxicGas.class );
	}

}
