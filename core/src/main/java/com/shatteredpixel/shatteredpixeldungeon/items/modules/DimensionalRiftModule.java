/*
 *
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * Experienced Pixel Dungeon
 * Copyright (C) 2019-2024 Trashbox Bobylev
 *
 * Extended Experienced Pixel Dungeon
 * Copyright (C) 2023-2024 John Nollas
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
 *
 */

package com.shatteredpixel.shatteredpixeldungeon.items.modules;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.branch;
import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.depth;
import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.levels.ArenaLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.DimensionalLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.Objects;

public class DimensionalRiftModule extends Module {

    private static final String AC_RESET = "RESET";

    {
        stackable = true;
        image = ItemSpriteSheet.DIMENSIONAL_SUFFERING;

        defaultAction = AC_USE;
    }


    public int depth;
    public int branch;
    public int pos;

    @Override
    public ArrayList<String> actions(Hero hero ) {
        ArrayList<String> actions = super.actions( hero );
        if (Dungeon.branch == 6) actions.add( AC_RESET );
        return actions;
    }

    @Override
    public void execute( Hero hero, String action ) {
        super.execute(hero, action);
        if (action.equals(AC_USE)) {
            if (Dungeon.branch != 6){
                depth = Dungeon.depth;
                branch = Dungeon.branch;
                pos = hero.pos;
                try {
                    Dungeon.saveLevel(GamesInProgress.curSlot);
                } catch (Exception e){
                    Game.reportException(e);
                }
                InterlevelScene.curTransition = new LevelTransition(Dungeon.level, -1, LevelTransition.Type.BRANCH_EXIT, 30, 6, LevelTransition.Type.BRANCH_ENTRANCE);
                InterlevelScene.mode = InterlevelScene.Mode.DESCEND;
                Game.switchScene( InterlevelScene.class );
                Buff.affect(hero, DimensionalLevel.DimensionalCounter.class);
                hero.grinding = false;
            } else {
                //InterlevelScene.curTransition = new LevelTransition(Dungeon.level, -1, LevelTransition.Type.BRANCH_ENTRANCE, depth, branch, LevelTransition.Type.BRANCH_EXIT);
                InterlevelScene.mode = InterlevelScene.Mode.RETURN;
                InterlevelScene.returnDepth = depth;
                InterlevelScene.returnBranch = branch;
                InterlevelScene.returnPos = pos;
                Game.switchScene( InterlevelScene.class );
                detach(hero.belongings.backpack);
                Buff.detach(hero, DimensionalLevel.DimensionalCounter.class);
                hero.grinding = true;
            }
        } else if (action.equals(AC_RESET)) {
            InterlevelScene.mode = InterlevelScene.Mode.RESET;
            Game.switchScene(InterlevelScene.class);
            Dungeon.level.reset();
        }
    }

    private static final String DEPTH	= "depth";
    private static final String BRANCH	= "branch";
    private static final String POS		= "pos";

    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle( bundle );
        bundle.put( DEPTH, depth );
        bundle.put( BRANCH, branch );
        if (depth != -1) {
            bundle.put( POS, pos);
        }
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle(bundle);
        depth	= bundle.getInt( DEPTH );
        if (bundle.contains(BRANCH))
            branch	= bundle.getInt( BRANCH );
        else
            branch = Dungeon.BRANCH_NORMAL;
        pos	= bundle.getInt( POS );
    }

    @Override
    public long value() {
        return 750 * quantity * Dungeon.depth * hero.lvl;
    }
}
