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

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.levels.ArenaInventory;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class TicketToWaveArena extends Item{

    {
        image = ItemSpriteSheet.MAGIC_INFUSE;
        stackable = true;
    }

    @Override
    public boolean isIdentified() {
        return true;
    }

    @Override
    public boolean isUpgradable() {
        return false;
    }

    private static final String AC_USE = "USE";

    @Override
    public ArrayList<String> actions(Hero hero ) {
        ArrayList<String> actions = super.actions(hero);
        actions.add(AC_USE);
        return actions;
    }

    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);
        if (action.equals(AC_USE)){
            if (Dungeon.branch != Dungeon.BRANCH_WAVE_ARENA){
                ArenaInventory.depth = Dungeon.depth;
                ArenaInventory.branch = Dungeon.branch;
                ArenaInventory.pos = hero.pos;
                try {
                    Dungeon.saveLevel(GamesInProgress.curSlot);
                } catch (Exception e){
                    Game.reportException(e);
                }
                InterlevelScene.curTransition = new LevelTransition(Dungeon.level, -1, LevelTransition.Type.BRANCH_EXIT, 29, Dungeon.BRANCH_WAVE_ARENA, LevelTransition.Type.BRANCH_ENTRANCE);
                InterlevelScene.mode = InterlevelScene.Mode.DESCEND;
                Game.switchScene( InterlevelScene.class );
                Catalog.countUse(getClass());
                ArenaInventory.stashAndStart( hero );
            } else {
                InterlevelScene.mode = InterlevelScene.Mode.RETURN;
                InterlevelScene.returnDepth = ArenaInventory.depth;
                InterlevelScene.returnBranch = ArenaInventory.branch;
                InterlevelScene.returnPos = ArenaInventory.pos;
                Game.switchScene( InterlevelScene.class );
                detach(hero.belongings.backpack);
                ArenaInventory.restoreAndMerge( hero );
            }
        }
    }

    private static final String DEPTH	= "depth";
    private static final String BRANCH	= "branch";
    private static final String POS		= "pos";

    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle( bundle );
        bundle.put( DEPTH, ArenaInventory.depth );
        bundle.put( BRANCH, ArenaInventory.branch );
        if (ArenaInventory.depth != -1) {
            bundle.put( POS, ArenaInventory.pos);
        }
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle(bundle);
        ArenaInventory.depth	= bundle.getInt( DEPTH );
        if (bundle.contains(BRANCH))
            ArenaInventory.branch	= bundle.getInt( BRANCH );
        else
            ArenaInventory.branch = Dungeon.BRANCH_NORMAL;
        ArenaInventory.pos	= bundle.getInt( POS );
    }

    @Override
    public long value() {
        return 210 * quantity;
    }

    @Override
    public boolean canBeOofed() {
        return false;
    }
}