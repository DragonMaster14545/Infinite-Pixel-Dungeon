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
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

public class WeaponBlueprint extends BlueprintRecipe {

    private Class<? extends Weapon> weaponClass;
    private long weaponLevel;

    public WeaponBlueprint(){
        super();
    }

    public WeaponBlueprint ( Class<? extends Weapon> weaponClass ) {
        this(weaponClass, 0);
    }

    public WeaponBlueprint( Class<? extends Weapon> weaponClass, long weaponLevel ){
        this();
        applyWeaponClass( weaponClass );
        this.weaponLevel = weaponLevel;
    }

    @Override
    protected Item produce(){
        if (weaponClass == null) return null;
        Item result = Reflection.newInstance( weaponClass );
        if (result == null) {
            result.level( weaponLevel );
        }
        result.identify();
        return result;
    }

    public static WeaponBlueprint reverseCraft( Hero hero, Weapon weapon ){
        if (weapon == null) return null;
        if (!hero.belongings.backpack.contains( weapon )) return null;
        if (weapon.isEquipped( hero )) return null;

        weapon.detach( hero.belongings.backpack );

        WeaponBlueprint blueprint = new WeaponBlueprint( weapon.getClass() );
        if (!blueprint.collect( hero.belongings.backpack )) {
            Heap heap = Dungeon.level.drop( blueprint, hero.pos );
            if (!heap.isEmpty()) {
                heap.sprite.drop( hero.pos );
            }
        }

        GLog.p( Messages.get( WeaponBlueprint.class, "reverse_crafted" ), weapon.title() );
        return blueprint;
    }

    private void applyWeaponClass( Class<? extends Weapon> weaponClass ) {
        this.weaponClass = weaponClass;
        if (weaponClass != null) {
            Item sample = Reflection.newInstance( weaponClass );
            if (sample != null) {
                image = sample.image;
            }
        }
    }

    private static final String WEAPON_CLASS = "weapon_class";
    private static final String WEAPON_LEVEL = "weapon_level";

    @Override
    public void storeInBundle( Bundle bundle ){
        super.storeInBundle( bundle );
        if (weaponClass != null){
            bundle.put( WEAPON_CLASS, weaponClass.getName() );
        }
        bundle.put(WEAPON_LEVEL, weaponLevel);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void restoreFromBundle( Bundle bundle ){
        super.restoreFromBundle( bundle );
        if (bundle.contains( WEAPON_CLASS )){
            try {
                weaponClass = (Class<? extends Weapon>) Class.forName( bundle.getString( WEAPON_CLASS ) );
            } catch (ClassNotFoundException e){
                weaponClass = null;
            }
        }
        weaponLevel = bundle.getLong( WEAPON_LEVEL );
    }

    public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe {

        private static final long COST = 20;

        private static Weapon weaponIn( ArrayList<Item> ingredients ){
            if (ingredients.size() != 1) return null;
            Item item = ingredients.get(0);
            if (!(item instanceof Weapon)) return null;
            if (!item.isIdentified() || item.cursed) return null;
            if (item.quantity() != 1) return null;
            return (Weapon) item;
        }

        @Override
        public boolean testIngredients( ArrayList<Item> ingredients ) {
            return weaponIn( ingredients ) != null;
        }

        @Override
        public long cost( ArrayList<Item> ingredients ) {
            return COST;
        }

        @Override
        public Item brew( ArrayList<Item> ingredients ) {
            Weapon weapon = weaponIn( ingredients );
            if (weapon == null) return null;

            weapon.quantity( 0 );

            return new WeaponBlueprint( weapon.getClass() );
        }

        @Override
        public Item sampleOutput( ArrayList<Item> ingredients ) {
            Weapon weapon = weaponIn( ingredients );
            if (weapon == null) return null;
            return new WeaponBlueprint( weapon.getClass() );
        }
    }
}