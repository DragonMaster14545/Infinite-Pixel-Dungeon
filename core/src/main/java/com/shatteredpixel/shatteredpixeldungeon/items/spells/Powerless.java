/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2019-2024 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.items.spells;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ArtifactRecharge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.Beam;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SacrificialParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HornOfPlenty;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TalismanOfForesight;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfExperience;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRage;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ShadowCaster;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.watabou.utils.GameMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Point;

import java.util.ArrayList;

public class Powerless extends Spell {

	{
		image = ItemSpriteSheet.POWERLESS;

		talentChance = 1/(float) Recipe.OUT_QUANTITY;
	}

	@Override
	protected void onCast(Hero hero) {

		Buff.affect(hero, PowerlessBuff.class).set(5f);

		Catalog.countUse(getClass());
		detach( curUser.belongings.backpack );
	}

	@Override
	public long value() {
		return (long)(60 * (quantity/(float) Recipe.OUT_QUANTITY));
	}

	@Override
	public long energyVal() {
		return (long)(12 * (quantity/(float) Recipe.OUT_QUANTITY));
	}

	public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe.SimpleRecipe {

		private static final int OUT_QUANTITY = 1;

		{
			inputs =  new Class[]{ScrollOfRage.class, Alchemize.class};
			inQuantity = new int[]{1, 1};

			cost = 33;

			output = Powerless.class;
			outQuantity = OUT_QUANTITY;
		}

	}

	public static class PowerlessBuff extends Buff {

		public static final float DURATION = 50f;

		{
			type = buffType.POSITIVE;
		}

		private float left;

		public void set( float duration ) {
			this.left = duration;
		}

		@Override
		public boolean act() {

			if (target.isAlive()) {
				ArrayList<Char> affected = new ArrayList<>();

				for (int i = 0; i < Dungeon.level.length(); i++) {
					Char ch = Actor.findChar(i);
					if (ch != null && (ch.alignment != target.alignment && ch.alignment != Char.Alignment.NEUTRAL)){
						affected.add(ch);
					}
				}

				for (Char ch : affected){
					if ( ch.alignment != target.alignment ) {
						Buff.append(Dungeon.hero, TalismanOfForesight.CharAwareness.class, 1f).charID = target.id();
						Buff.affect(ch, Paralysis.class, 1f);
						((Mob) ch).aggro(target);
					}
				}
      Dungeon.observe();

				if (left <= 0){
					detach();
				} else {
					left--;
					spend( TICK * 5f );
				}

			} else {
				detach();
			}


			return true;
		}

		public float left(){
			return left;
		}

		@Override
		public int icon() {
			return BuffIndicator.VERTIGO;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(1f, 0f, 1f);
		}

		@Override
		public float iconFadePercent() {
			return Math.max(0, (DURATION - left) / DURATION);
		}

		@Override
		public String iconTextDisplay() {
			return Integer.toString((int)left+1);
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", dispTurns(left+1));
		}

		private static final String LEFT = "left";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put( LEFT, left );
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			left = bundle.getFloat(LEFT);
		}
	}
}
