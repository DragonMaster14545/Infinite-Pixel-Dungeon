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
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTerrainSelect;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;

import java.util.ArrayList;

public class WandOfTerraforming extends Wand {

    {
        image = ItemSpriteSheet.TERRAFORM_WAND;
        collisionProperties = Ballistica.STOP_TARGET;
    }

    public int selectedTerrain = -1;

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        actions.add(AC_CHANGE_TERRAIN);
        return actions;
    }

    private static final String AC_CHANGE_TERRAIN = "CHANGE_TERRAIN";

    @Override
    public void execute( Hero hero, String action ) {
        if (action.equals( AC_ZAP )) {
            if (selectedTerrain == -1) {
                GameScene.show( new WndTerrainSelect( this ) {
                    @Override
                    public void onSelect( int terrain ) {
                        selectedTerrain = terrain;
                        WandOfTerraforming.super.execute( hero, action );
                    }
                } );
            } else {
                super.execute( hero, action );
            }
        } else if (action.equals( AC_CHANGE_TERRAIN )) {
            GameScene.show( new WndTerrainSelect( this ) {
                @Override
                public void onSelect( int terrain ) {
                    selectedTerrain = terrain;
                    GLog.i( Messages.get( WandOfTerraforming.this, "terrain_changed" ) );
                }
            } );
        } else {
            super.execute( hero, action );
        }
    }

    @Override
    public void onZap(Ballistica bolt) {
        if (selectedTerrain == -1) return;

        int cell = bolt.collisionPos;
        Level level = Dungeon.level;

        Char ch = Actor.findChar(bolt.collisionPos);
        int existing = level.map[cell];
        if (!(existing == Terrain.ENTRANCE || existing == Terrain.EXIT
                || existing == Terrain.LOCKED_EXIT || existing == Terrain.UNLOCKED_EXIT) && ch == null) {
            Level.set( cell, selectedTerrain, level );
            GameScene.updateMap( cell );
        } else {
            GLog.w( Messages.get( this, "no_valid_cell" ) );
            return;
        }

        Dungeon.level.pressCell( cell );
        Dungeon.observe();
        GameScene.updateFog();

        Sample.INSTANCE.play( Assets.Sounds.EVOKE );
        GLog.h( Messages.get( this, "reshaped" ) );
    }

    @Override
    public void onHit(MagesStaff staff, Char attacker, Char defender, long damage) {
        //this wand doesn't damage on melee proc, nothing to do
    }

    @Override
    public void fx(Ballistica bolt, Callback callback) {
        MagicMissile.boltFromChar( curUser.sprite.parent,
                MagicMissile.RAINBOW,
                curUser.sprite,
                bolt.collisionPos,
                callback);
        Sample.INSTANCE.play(Assets.Sounds.BEACON);
    }

    @Override
    public void staffFx(MagesStaff.StaffParticle particle) {
        particle.color( 0x228B22 );
        particle.am = 0.6f;
        particle.setLifespan(3f);
        particle.speed.polar((float)(Math.random()*Math.PI*2), 0.3f);
        particle.setSize( 1f, 2f );
        particle.radiateXY(2.5f);
    }

    private static final String SELECTED_TERRAIN = "selected_terrain";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(SELECTED_TERRAIN, selectedTerrain);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        selectedTerrain = bundle.contains(SELECTED_TERRAIN) ? bundle.getInt(SELECTED_TERRAIN) : -1;
    }
}