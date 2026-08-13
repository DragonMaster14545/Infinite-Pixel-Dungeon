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

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import java.util.ArrayList;
public abstract class BlueprintRecipe extends Item {

    public static final String AC_USE = "USE";

    {
        stackable = true;
        defaultAction = AC_USE;
    }

    protected static final float TIME_TO_CRAFT = 1f;

    protected abstract Item produce();

    @Override
    public boolean isIdentified() {
        return true;
    }

    @Override
    public String name() {
        Item result = produce();
        if (result != null) {
            return Messages.get( this, "name_recipe", result.name() );
        } else {
            return super.name();
        }
    }

    @Override
    public String desc() {
        Item result = produce();
        if (result != null) {
            return Messages.get( this, "desc_recipe", result.name() );
        } else {
            return super.desc();
        }
    }

    @Override
    public ArrayList<String> actions( Hero hero ) {
        ArrayList<String> actions = super.actions( hero );
        actions.add( AC_USE );
        return actions;
    }

    @Override
    public void execute( Hero hero, String action ) {
        super.execute( hero, action );

        if (action.equals( AC_USE )) {

            Item result = produce();
            if (result == null) {
                GLog.w( Messages.get( this, "no_recipe" ) );
                return;
            }

            detach( hero.belongings.backpack );

            if (!result.collect( hero.belongings.backpack )) {
                Heap heap = Dungeon.level.drop( result, hero.pos );
                if (!heap.isEmpty()) {
                    heap.sprite.drop( hero.pos );
                }
            }

            GLog.p( Messages.get( this, "crafted" ), result.title() );
            hero.spendAndNext( TIME_TO_CRAFT );
        }
    }
}