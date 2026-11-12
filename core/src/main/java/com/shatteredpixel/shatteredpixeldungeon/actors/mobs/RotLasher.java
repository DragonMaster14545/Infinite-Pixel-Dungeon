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
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RotLasherSprite;

public class RotLasher extends Mob {

	{
		spriteClass = RotLasherSprite.class;

		HP = HT = Dungeon.getCycleMultiplier(BigInteger.valueOf(80));
		defenseSkill = BigInteger.ZERO;

		EXP = Dungeon.getCycleMultiplier(BigInteger.ONE);

		loot = Generator.Category.SEED;
		lootChance = 0.75f;

		state = WANDERING = new Waiting();
		viewDistance = 1;

		properties.add(Property.IMMOVABLE);
		properties.add(Property.MINIBOSS);
	}

	@Override
	protected boolean act() {
		if ((HP.compareTo(HT) < 0) && (enemy == null || !Dungeon.level.adjacent(pos, enemy.pos))) {
			sprite.showStatusWithIcon(CharSprite.POSITIVE, BigInteger.valueOf(5).min(HT.subtract(HP)).toString(), FloatingText.HEALING);
			HP = HT.min(HP.add(BigInteger.valueOf(3)).add(BigInteger.valueOf(Dungeon.cycle * 25L)));
		}
		return super.act();
	}

	@Override
	public void damage(BigInteger dmg, Object src) {
		if (src instanceof Burning) {
			destroy();
			sprite.die();
		} else {
			super.damage(dmg, src);
		}
	}

	@Override
	public BigInteger attackProc(Char enemy, BigInteger damage) {
		damage = super.attackProc( enemy, damage );
		Buff.affect( enemy, Cripple.class, 2f );
        if (enemy == Dungeon.hero){
            Statistics.questScores[1] -= 100;
        }
		return super.attackProc(enemy, damage);
	}

	@Override
	public boolean reset() {
		return true;
	}

	@Override
	protected boolean getCloser(int target) {
		return false;
	}

	@Override
	protected boolean getFurther(int target) {
		return false;
	}

	@Override
	public BigInteger damageRoll() {
		return Dungeon.NormalLongRange(Dungeon.getCycleMultiplier(BigInteger.TEN), Dungeon.getCycleMultiplier(BigInteger.valueOf(20)));
	}

	@Override
	public BigInteger attackSkill(Char target ) {
		return Dungeon.getCycleMultiplier(BigInteger.valueOf(25));
	}

	@Override
	public BigInteger cycledDrRoll() {
		return Dungeon.NormalLongRange(Dungeon.getCycleMultiplier(BigInteger.ZERO), Dungeon.getCycleMultiplier(BigInteger.valueOf(8)));
	}
	
	{
		immunities.add( ToxicGas.class );
	}

	private class Waiting extends Mob.Wandering{

		@Override
		protected boolean noticeEnemy() {
			spend(TICK);
			return super.noticeEnemy();
		}
	}
}
