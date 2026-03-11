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

package com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ChallengeParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.InsurgenceParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Longsword;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ShadowCaster;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Point;

import java.util.ArrayList;

public class ScrollOfInsurgence extends ExoticScroll {
	
	{
		icon = ItemSpriteSheet.Icons.SCROLL_INSURGENCE;
	}
	
	@Override
	public void doRead() {

		detach(curUser.belongings.backpack);
		identify();

        for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )) {
            mob.beckon( curUser.pos );
        }

        Buff.affect(curUser, ChallengeInsurgence.class).setup(curUser.pos);

        identify();

        curUser.sprite.centerEmitter().start( Speck.factory( Speck.SCREAM ), 0.3f, 3 );
        Sample.INSTANCE.play( Assets.Sounds.CHALLENGE );
		
		readAnimation();
	}

    public static class ChallengeInsurgence extends Buff {

        private ArrayList<Integer> insurgencePositions = new ArrayList<>();
        private ArrayList<Emitter> insurgenceEmitters = new ArrayList<>();

        private static final float DURATION = 100;
        int left = 0;

        {
            type = buffType.POSITIVE;
        }

        @Override
        public int icon() {
            return BuffIndicator.ARMOR;
        }

        @Override
        public void tintIcon(Image icon) {
            icon.hardlight(0f, 0f, 1f);
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
                dist = 1; //smaller boss arenas
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
                if (PathFinder.distance[i] < Integer.MAX_VALUE && !insurgencePositions.contains(i)) {
                    insurgencePositions.add(i);
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

            if (!insurgencePositions.contains(target.pos)){
                detach();
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
                for (int i : insurgencePositions){
                    Emitter e = CellEmitter.get(i);
                    e.pour(InsurgenceParticle.FACTORY, 0.05f);
                    insurgenceEmitters.add(e);
                }
            } else {
                for (Emitter e : insurgenceEmitters){
                    e.on = false;
                }
                insurgenceEmitters.clear();
            }
        }

        private static final String INSURGENCE_POSITIONS = "insurgence_positions";
        private static final String LEFT = "left";

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);

            int[] values = new int[insurgencePositions.size()];
            for (int i = 0; i < values.length; i ++)
                values[i] = insurgencePositions.get(i);
            bundle.put(INSURGENCE_POSITIONS, values);

            bundle.put(LEFT, left);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);

            int[] values = bundle.getIntArray( INSURGENCE_POSITIONS );
            for (int value : values) {
                insurgencePositions.add(value);
            }

            left = bundle.getInt(LEFT);
        }
    }
}
