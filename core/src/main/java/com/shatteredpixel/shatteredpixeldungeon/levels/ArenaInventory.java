package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Dagger;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClothArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.food.SmallRation;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class ArenaInventory {

    private static Bundle stashedBelongings = null;
    private static boolean active = false;

    public static boolean isActive(){
        return active;
    }

    public static void stashAndStart( Hero hero ){
        if (active) return;

        Bundle b = new Bundle();
        hero.belongings.storeInBundle( b );
        stashedBelongings = b;
        active = true;

        ArrayList<Item> toRemove = new ArrayList<>( hero.belongings.backpack.items );
        for (Item item : toRemove) {
            item.detach( hero.belongings.backpack );
        }
        if (hero.belongings.weapon != null) {
            hero.belongings.weapon.detach( hero.belongings.backpack );
        }
        if (hero.belongings.armor != null) {
            hero.belongings.armor.detach( hero.belongings.backpack );
        }
        if (hero.belongings.secondWep != null){
            hero.belongings.secondWep.detach( hero.belongings.backpack );
        }
        for (Item ring : new ArrayList<Item>( hero.belongings.rings )) {       ring.detach( hero.belongings.backpack );}
        for (Item art : new ArrayList<Item>( hero.belongings.artifacts ))    art.detach( hero.belongings.backpack );
        for (Item misc : new ArrayList<Item>( hero.belongings.miscs ))       misc.detach( hero.belongings.backpack );

        giveStarterKit( hero );

        GLog.w( Messages.get( ArenaInventory.class, "stashed" ) );
    }

    private static void giveStarterKit( Hero hero ){
        new Dagger().identify().collect();
        new ClothArmor().identify().collect();
        new SmallRation().collect();
    }

    public static void restoreAndMerge( Hero hero ){
        if (!active || stashedBelongings == null) return;

        ArrayList<Item> earned = new ArrayList<>( hero.belongings.backpack.items );
        if (hero.belongings.weapon != null)    earned.add( hero.belongings.weapon );
        if (hero.belongings.armor != null)     earned.add( hero.belongings.armor );
        if (hero.belongings.secondWep != null) earned.add( hero.belongings.secondWep );
        earned.addAll( hero.belongings.rings );
        earned.addAll( hero.belongings.artifacts );
        earned.addAll( hero.belongings.miscs );

        ArrayList<Item> toRemove = new ArrayList<>( hero.belongings.backpack.items );
        for (Item item : toRemove) item.detach( hero.belongings.backpack );
        if (hero.belongings.weapon != null)      hero.belongings.weapon.detach( hero.belongings.backpack );
        if (hero.belongings.armor != null)       hero.belongings.armor.detach( hero.belongings.backpack );
        if (hero.belongings.secondWep != null)   hero.belongings.secondWep.detach( hero.belongings.backpack );
        for (Item ring : new ArrayList<Item>( hero.belongings.rings ))       ring.detach( hero.belongings.backpack );
        for (Item art : new ArrayList<Item>( hero.belongings.artifacts ))    art.detach( hero.belongings.backpack );
        for (Item misc : new ArrayList<Item>( hero.belongings.miscs ))       misc.detach( hero.belongings.backpack );

        hero.belongings.restoreFromBundle( stashedBelongings );

        for (Item item : earned) {
            if (item == null) continue;
            if (!item.collect( hero.belongings.backpack )) {
                Dungeon.level.drop( item, hero.pos ).sprite.drop();
            }
        }

        stashedBelongings = null;
        active = false;

        GLog.p( Messages.get( ArenaInventory.class, "restored" ) );
    }
}