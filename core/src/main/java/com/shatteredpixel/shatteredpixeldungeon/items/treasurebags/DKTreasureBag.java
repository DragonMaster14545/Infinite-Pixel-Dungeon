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

package com.shatteredpixel.shatteredpixeldungeon.items.treasurebags;


import java.math.BigInteger;
import com.shatteredpixel.shatteredpixeldungeon.utils.Big;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.*;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.AquaBrew;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.UnstableBrew;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.KingBlade;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.Recycle;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.UnstableSpell;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class DKTreasureBag extends TreasureBag {
    {
        image = ItemSpriteSheet.DK_BAG;
    }

    @Override
    protected ArrayList<Item> items() {
        ArrayList<Item> items = new ArrayList<>();
        if (!Dungeon.LimitedDrops.ARMOR_KIT.dropped()){
            items.add(new KingsCrown());
            Dungeon.LimitedDrops.ARMOR_KIT.drop();
        }
        items.add(new Gold().quantity(BigInteger.valueOf(Random.Long( Big.toLong(BigInteger.valueOf(1500).add(Dungeon.escalatingDepth().multiply(BigInteger.valueOf(175)))), Big.toLong(BigInteger.valueOf(2000).add(Dungeon.escalatingDepth().multiply(BigInteger.valueOf(233)))) ))));
        items.add(Generator.randomUsingDefaults(Generator.Category.ARTIFACT));
        items.add(Generator.randomUsingDefaults(Generator.Category.WEAPON));
        items.add(Generator.randomUsingDefaults(Generator.Category.ARMOR));
        items.add(Generator.randomUsingDefaults(Generator.Category.RING));
        if (Dungeon.cycle > 0){
            if (!Dungeon.LimitedDrops.DK_DROPS.dropped()){
                items.add(new KingBlade());
                Dungeon.LimitedDrops.DK_DROPS.drop();
            }
            for (int i = 0; i < 15; i++) items.add(Generator.random());
            items.add(new Recycle().quantity(BigInteger.valueOf(50)));
            items.add(new AquaBrew().quantity(BigInteger.valueOf(50)));
            items.add(new UnstableSpell().quantity(BigInteger.valueOf(50)));
            items.add(new UnstableBrew().quantity(BigInteger.valueOf(50)));
        }
        if (Dungeon.cycle > 1){
            items.add(new OverloadBeacon().quantity(BigInteger.TWO));
            for (int i = 0; i < 3; i++) items.addAll(new BiggerGambleBag().items());
        }
        return items;
    }
}
