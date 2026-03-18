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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.InsurgenceParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ShadowCaster;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Warmaster extends Weapon.Enchantment {

	private static ItemSprite.Glowing BLACK = new ItemSprite.Glowing( 0x000000 );

	@Override
	public long proc( Weapon weapon, Char attacker, Char defender, long damage ) {
		float procChance = 1/8f * procChanceMultiplier(attacker);

		if (attacker.buff(WarmasterBuff.class) != null){
			Buff.affect(attacker, WarmasterBuff.class).extend(1);
		} else if (Random.Float() < procChance){
			for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
				mob.beckon(attacker.pos);
			}
			Buff.affect(attacker, WarmasterBuff.class).setup(attacker.pos);
		}

		return damage;
	}

	@Override
	public boolean curse() {
		return true;
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return BLACK;
	}
	public static class WarmasterBuff extends Buff {

		private ArrayList<Integer> warmasterPositions = new ArrayList<>();
		private ArrayList<Emitter> warmasterEmitters = new ArrayList<>();

		private static final float DURATION = 100;
		int left = 0;

		{
			type = buffType.NEGATIVE;
		}

		@Override
		public int icon() {
			return BuffIndicator.ARMOR;
		}

		public void extend( int duration ) {
			this.left += duration;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(0.1f, 0.1f, 0.1f);
		}

		@Override
		public float iconFadePercent() {
			return Math.max(0, (DURATION - left) / DURATION);
		}

		@Override
		public String iconTextDisplay() {
			return Integer.toString(left);
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", left);
		}

		public void setup(int pos){

			int dist;
			if (Dungeon.depth == 5 || Dungeon.depth == 10 || Dungeon.depth == 20){
				dist = 2; //smaller boss arenas
			} else {

				boolean[] visibleCells = new boolean[Dungeon.level.length()];
				Point c = Dungeon.level.cellToPoint(pos);
				ShadowCaster.castShadow(c.x, c.y, Dungeon.level.width(), visibleCells, Dungeon.level.losBlocking, 8);
				int count=0;
				for (boolean b : visibleCells){
					if (b) count++;
				}

				if (count < 30){
					dist = 2;
				} else if (count >= 100) {
					dist = 4;
				} else {
					dist = 3;
				}
			}

			PathFinder.buildDistanceMap( pos, BArray.or( Dungeon.level.passable, Dungeon.level.avoid, null ), dist );
			for (int i = 0; i < PathFinder.distance.length; i++) {
				if (PathFinder.distance[i] < Integer.MAX_VALUE && !warmasterPositions.contains(i)) {
					warmasterPositions.add(i);
				}
			}
			if (target != null) {
				fx(false);
				fx(true);
			}

			left = (int) DURATION;

		}

		@Override
		public boolean act() {

			if (!warmasterPositions.contains(target.pos)){
				detach();
				if (target instanceof Hero) {
					Buff.affect( target, Poison.class).set((double) ((Hero) target).lvl / 3);
				}
			}

			left--;
			BuffIndicator.refreshHero();
			if (left <= 0){
				detach();
			}

			spend(TICK);
			return true;
		}

		@Override
		public void fx(boolean on) {
			if (on){
				for (int i : warmasterPositions){
					Emitter e = CellEmitter.get(i);
					e.pour(InsurgenceParticle.FACTORY, 0.05f);
					warmasterEmitters.add(e);
				}
			} else {
				for (Emitter e : warmasterEmitters){
					e.on = false;
				}
				warmasterEmitters.clear();
			}
		}

		private static final String INSURGENCE_POSITIONS = "insurgence_positions";
		private static final String LEFT = "left";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);

			int[] values = new int[warmasterPositions.size()];
			for (int i = 0; i < values.length; i ++)
				values[i] = warmasterPositions.get(i);
			bundle.put(INSURGENCE_POSITIONS, values);

			bundle.put(LEFT, left);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);

			int[] values = bundle.getIntArray( INSURGENCE_POSITIONS );
			for (int value : values) {
				warmasterPositions.add(value);
			}

			left = bundle.getInt(LEFT);
		}
	}

}
