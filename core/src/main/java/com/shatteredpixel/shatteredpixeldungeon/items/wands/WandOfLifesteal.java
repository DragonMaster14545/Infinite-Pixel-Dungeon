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

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Web;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.Beam;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.PurpleParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.RainbowParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTransmutation;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.List;

public class WandOfLifesteal extends DamageWand {

	{
		image = ItemSpriteSheet.LIFESTEAL_WAND;

		collisionProperties = Ballistica.STOP_TARGET;
	}


    public long min(long lvl){
        return ((1+lvl)*getRarityMultiplier());
    }

    public long max(long lvl){
        return ((3+3*lvl)*getRarityMultiplier());
    }

	@Override
	public int targetingPos(Hero user, int dst) {
		if (!cursed || !cursedKnown) {
			return dst;
		} else {
			return super.targetingPos(user, dst);
		}
	}

	@Override
	public void onZap(Ballistica beam) {

        int maxDistance = Math.min(distance(), beam.dist);
        ArrayList<Char> chars = new ArrayList<>();

        int terrainPassed = 2;
        for (int c : beam.subPath(1, maxDistance)) {

            Char ch;
            if ((ch = Actor.findChar( c )) != null) {

                //we don't want to count passed terrain after the last enemy hit. That would be a lot of bonus levels.
                //terrainPassed starts at 2, equivalent of rounding up when /3 for integer arithmetic.
                terrainPassed = terrainPassed%3;
                chars.add(ch);

            }

            if (Dungeon.level.solid[c]) {
                terrainPassed++;
            }

        }

        for (Char ch : chars) {
            wandProc(ch, chargesPerCast());
            affectTarget(ch);
        }

	}

    private void affectTarget(Char ch){

        if ( ch.alignment != curUser.alignment && curUser.HP < curUser.HT ) {
            curUser.sprite.parent.add(new Beam.HealthRay(curUser.sprite.center(), DungeonTilemap.raisedTileCenterToWorld(ch.pos)));
            float drain = damageRoll();
            ch.damage(Math.round(drain), this);
            curUser.HP = Math.min(curUser.HP + Math.round(drain), curUser.HT);
            curUser.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString(Math.round(drain)), FloatingText.HEALING);
        }

    }

	@Override
	public void onHit(MagesStaff staff, Char attacker, Char defender, long damage) {
		//no direct effect, see magesStaff.reachfactor
        Buff.prolong( defender, Amok.class, Math.round((1+staff.buffedLvl())*procChanceMultiplier(attacker)));
	}

	private int distance() {
		return (int) (buffedLvl()*2 + 6);
	}

	@Override
	public String upgradeStat2(long level) {
		return Long.toString(6 + level*2);
	}

	@Override
	public void fx(Ballistica beam, Callback callback) {
		
		int cell = beam.path.get(Math.min(beam.dist, distance()));
		curUser.sprite.parent.add(new Beam.HealthRay(curUser.sprite.center(), DungeonTilemap.raisedTileCenterToWorld( cell )));
		callback.call();
	}

	@Override
	public void staffFx(MagesStaff.StaffParticle particle) {
		particle.color(0x008000);
		particle.am = 0.6f;
		particle.setLifespan(1f);
		particle.acc.set(10, -10);
		particle.setSize( 0.5f, 3f);
		particle.shuffleXY(1f);
	}

    public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe.SimpleRecipe {

        private static final int OUT_QUANTITY = 1;

        {
            inputs =  new Class[]{WandOfDisintegration.class, WandOfTransfusion.class, ScrollOfTransmutation.class};
            inQuantity = new int[]{1, 1, 1};

            cost = 3500;

            output = WandOfLifesteal.class;
            outQuantity = OUT_QUANTITY;
        }

    }

}
