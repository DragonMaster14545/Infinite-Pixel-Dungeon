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

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfTerraforming;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;

public abstract class WndTerrainSelect extends WndTitledMessage {

    private static final int COLS  = 3;
    private static final int BTN_W = 60;
    private static final int BTN_H = 16;
    private static final int GAP   = 2;

    private static final Object[][] OPTIONS = {
            { Terrain.EMPTY,            "empty" },
            { Terrain.WALL,             "wall" },
            { Terrain.WALL_DECO,        "wall_deco" },
            { Terrain.BARRICADE,        "barricade" },
            { Terrain.PEDESTAL,         "pedestal" },
            { Terrain.WATER,            "water" },
            { Terrain.EMPTY_WELL,       "empty_well" },
            { Terrain.WELL,             "well" },
            { Terrain.GRASS,            "grass" },
            { Terrain.HIGH_GRASS,       "high_grass" },
            { Terrain.FURROWED_GRASS,   "furrowed_grass" },
            { Terrain.CHASM,            "chasm" },
            { Terrain.STATUE,           "statue" },
            { Terrain.BOOKSHELF,        "bookshelf" },
            { Terrain.EMPTY_DECO,       "empty_deco" },
            { Terrain.EMBERS,           "embers" },
            { Terrain.REGION_DECO,      "region_deco" },
            { Terrain.REGION_DECO_ALT,  "region_deco_alt" }
    };

    public WndTerrainSelect( WandOfTerraforming wand ){
        super( Icons.get( Icons.INFO ),
                Messages.get( WndTerrainSelect.class, "title" ),
                Messages.get( WndTerrainSelect.class, "prompt" ) );

        int top = height + 2;
        int left = 0;

        for (Object[] option : OPTIONS){
            final int terrain = (Integer) option[0];
            String key = (String) option[1];

            RedButton btn = new RedButton( Messages.get( WndTerrainSelect.class, key ) ){
                @Override
                public void onClick(){
                    onSelect( terrain );
                    hide();
                }
            };
            btn.setRect( left, top, BTN_W, BTN_H );
            add( btn );

            left += BTN_W + GAP;
            if (left + BTN_W > COLS * (BTN_W + GAP)){
                left = 0;
                top += BTN_H + GAP;
            }
        }

        if (left > 0) top += BTN_H + GAP;

        resize( COLS * (BTN_W + GAP) - GAP, top );
    }

    public abstract void onSelect( int terrain );
}