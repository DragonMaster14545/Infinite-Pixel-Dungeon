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

package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;

import java.util.HashMap;
public class BattlePassTiers {

    public static final long REPEATABLE_TIER_GOLD = 300L;

    public static long goldFor( int tier ){
        if (tier == BattlePass.REPEATABLE_TIER) return REPEATABLE_TIER_GOLD;
        return 50L + tier * 10L;
    }

    public static boolean isItemTier( int tier ){
        return tier != BattlePass.REPEATABLE_TIER && tier % 5 == 0;
    }
    private static final HashMap<Integer, Item> rewardCache = new HashMap<>();

    public static Item rewardFor( int tier ){
        if (!isItemTier( tier )) return null;

        if (!rewardCache.containsKey( tier )){
            Generator.Category[] categories = Generator.Category.values();
            int index = Math.min( tier / 5, categories.length - 1 );
            rewardCache.put( tier, Generator.random( categories[index] ) );
        }
        return rewardCache.get( tier );
    }
}