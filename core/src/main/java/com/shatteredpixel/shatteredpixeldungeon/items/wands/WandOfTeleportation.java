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
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Levitation;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.Effects;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTransmutation;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.CurseInfusion;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.Vampirism;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Elastic;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Door;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.TenguDartTrap;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

public class WandOfTeleportation extends DamageWand {

	{
		image = ItemSpriteSheet.WAND_TELEPORTATION;

		collisionProperties = Ballistica.STOP_TARGET;
	}

	public long min(long lvl){
		return ((1+lvl)*getRarityMultiplier());
	}

	public long max(long lvl){
		return ((5+3*lvl)*getRarityMultiplier());
	}

	@Override
	public void onZap(Ballistica bolt) {
        //checks if there's an enemy on the cell
        Char ch = Actor.findChar(bolt.collisionPos);
        if (ch != null){
            wandProc(ch, chargesPerCast());
            ch.damage(damageRoll(), this);

            Sample.INSTANCE.play( Assets.Sounds.HIT_MAGIC, 1, Random.Float(0.87f, 1.15f) );
            ch.sprite.burst(0xFFFFFFFF,  (int) Math.min(Math.sqrt(2 + buffedLvl() / 2f), 100));

            for (Wand.Charger wandCharger : curUser.buffs(Wand.Charger.class)){
                if (wandCharger.wand().buffedLvl() < buffedLvl() || curUser.buff(WandOfMagicMissile.MagicCharge.class) != null){
                    Buff.prolong(curUser, WandOfMagicMissile.MagicCharge.class, WandOfMagicMissile.MagicCharge.DURATION).setup(this);
                    break;
                }
            }
        } else {
            ScrollOfTeleportation.teleportToLocation(curUser, bolt.collisionPos);
            Dungeon.level.pressCell(bolt.collisionPos);
        }
	}

    @Override
    protected long chargesPerCast() {
        return 2;
    }

    @Override
    public boolean isUpgradable() {
        return false;
    }

    @Override
	public void onHit(MagesStaff staff, Char attacker, Char defender, long damage) {
        defender.damage(damage, staff);
	}

	@Override
	public String upgradeStat2(long level) {
		return Long.toString(3 + level);
	}

    @Override
	public void staffFx(MagesStaff.StaffParticle particle) {
		particle.color( 0xebebed ); particle.am = 0.6f;
		particle.setLifespan(2f);
		particle.speed.polar(Random.Float(PointF.PI2), 0.3f);
		particle.setSize( 1f, 2f);
		particle.radiateXY(1.5f);
	}

    public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe.SimpleRecipe {

        private static final int OUT_QUANTITY = 1;

        {
            inputs =  new Class[]{ScrollOfTeleportation.class, WandOfMagicMissile.class, ScrollOfTransmutation.class};
            inQuantity = new int[]{1, 1, 1};

            cost = 5000;

            output = WandOfTeleportation.class;
            outQuantity = OUT_QUANTITY;
        }

    }

}
