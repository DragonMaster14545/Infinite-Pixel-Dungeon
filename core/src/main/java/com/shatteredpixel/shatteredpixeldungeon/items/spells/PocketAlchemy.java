/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.items.spells;

import static com.shatteredpixel.shatteredpixeldungeon.actors.Actor.TICK;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;

public class PocketAlchemy extends TargetedSpell {

	{
		image = ItemSpriteSheet.PORTABLE_ALCHEMY;

		talentChance = 1/(float) Recipe.OUT_QUANTITY;
	}

    @Override
    public void execute( final Hero hero, String action ) {

        if (action.equals( AC_CAST )) {

            if (hero.buff(AlchemyCooldown.class) != null) {
                GLog.w( Messages.get(this, "alchemy_oncooldown") );
                return;
            }

            super.execute(hero, action);

        }

    }

	@Override
	protected void fx(Ballistica bolt, Callback callback) {
		MagicMissile.boltFromChar( curUser.sprite.parent,
				MagicMissile.BEACON,
				curUser.sprite,
				bolt.collisionPos,
				callback);
		Sample.INSTANCE.play( Assets.Sounds.ZAP );
	}

	@Override
	protected void affectTarget(Ballistica bolt, Hero hero) {
        int cell = bolt.collisionPos;

        Char ch = Actor.findChar(cell);
        if ((Dungeon.level.map[cell] == Terrain.EMPTY
                || Dungeon.level.map[cell] == Terrain.WATER
                || Dungeon.level.map[cell] == Terrain.GRASS
                || Dungeon.level.map[cell] == Terrain.HIGH_GRASS
                || Dungeon.level.map[cell] == Terrain.EMBERS) && ch == null) {
            Level.set(cell, Terrain.ALCHEMY);
            GameScene.updateMap(cell);
            Buff.affect(hero, AlchemyCooldown.class).set(750);
        }

	}

	@Override
	public long value() {
		return (long)(60 * (quantity/(float) Recipe.OUT_QUANTITY));
	}

	@Override
	public long energyVal() {
		return (long)(12 * (quantity/(float) Recipe.OUT_QUANTITY));
	}

    public static class AlchemyCooldown extends Buff {

        int duration = 0;
        int maxDuration = 0;

        {
            type = buffType.NEUTRAL;
            announced = false;
        }

        public void set(int time) {
            maxDuration = time;
            duration = maxDuration;
        }

        public void hit(int time) {
            duration -= time;
            if (duration <= 0) detach();
        }

        @Override
        public boolean act() {
            duration--;
            if (duration <= 0) {
                detach();
            }
            spend(TICK);
            return true;
        }

        @Override
        public int icon() {
            return BuffIndicator.TIME;
        }

        @Override
        public float iconFadePercent() {
            return Math.max(0, (maxDuration - duration) / maxDuration);
        }

        @Override
        public String iconTextDisplay() {
            return Integer.toString(duration);
        }

        @Override
        public String toString() {
            return Messages.get(this, "name");
        }

        @Override
        public String desc() {
            return Messages.get(this, "desc", duration);
        }

        private static final String MAX_DURATION = "maxDuration";
        private static final String DURATION = "duration";
        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put( MAX_DURATION, maxDuration );
            bundle.put( DURATION, duration );
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            maxDuration = bundle.getInt( MAX_DURATION );
            duration = bundle.getInt( DURATION );
        }
    }

	public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe.SimpleRecipe {

		private static final int OUT_QUANTITY = 3;

		{
			inputs =  new Class[]{ScrollOfMagicMapping.class, Alchemize.class};
			inQuantity = new int[]{1, 1};

			cost = 25;

			output = PocketAlchemy.class;
			outQuantity = OUT_QUANTITY;
		}

	}

}
