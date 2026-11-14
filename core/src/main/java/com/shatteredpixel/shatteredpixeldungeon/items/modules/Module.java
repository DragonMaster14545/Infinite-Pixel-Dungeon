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


import java.math.BigInteger;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Module extends Item {

    {
        stackable = true;
        image = ItemSpriteSheet.SOMETHING;

        defaultAction = AC_USE;
    }

    public Module() {

    }

    public Module( BigInteger value ) {
        this.quantity = value;
    }

    public static int level = 0;

    public static final String AC_USE = "USE";

    @Override
    public ArrayList<String> actions(Hero hero ) {
        ArrayList<String> actions = super.actions( hero );
        actions.add( AC_USE );
        return actions;
    }

    @Override
    public void execute( Hero hero, String action ) {
        super.execute(hero, action);
    }

    @Override
    public BigInteger level(){
        return BigInteger.valueOf(level);
    }


    @Override
    public boolean isUpgradable() {
        return false;
    }

    @Override
    public boolean isIdentified() {
        return true;
    }

    @Override
    public BigInteger value() {
        return BigInteger.valueOf(75000).multiply(quantity);
    }

    @Override
    public Item random() {
        quantity = (Dungeon.LongRange( BigInteger.valueOf(2), BigInteger.valueOf(4) ));
        return this;
    }

    public Item random(long x){
        switch (Random.Int(1, 11)) {
            case 0:
                return new ChampionModule().quantity(BigInteger.valueOf(x));
            case 1:
                return new DimensionalRiftModule().quantity(BigInteger.valueOf(x));
            case 2:
                return new GoldModule().quantity(BigInteger.valueOf(x));
            case 3:
                return new HasteModule().quantity(BigInteger.valueOf(x));
            case 4:
                return new ItemModule().quantity(BigInteger.valueOf(x));
            case 5:
                return new MindVisionModule().quantity(BigInteger.valueOf(x));
            case 6:
                return new PotionModule().quantity(BigInteger.valueOf(x));
            case 7:
                return new ScrollModule().quantity(BigInteger.valueOf(x));
            case 8:
                return new PurityModule().quantity(BigInteger.valueOf(x));
            case 9:
                return new TimeReverserModule().quantity(BigInteger.valueOf(x));
            case 10:
                return new HealthModule().quantity(BigInteger.valueOf(x));
            case 11: default:
                return new LuckModule().quantity(BigInteger.valueOf(x));
        }
    }

}
