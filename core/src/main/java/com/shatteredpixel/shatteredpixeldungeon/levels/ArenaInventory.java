package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindofMisc;
import com.shatteredpixel.shatteredpixeldungeon.items.TicketToWaveArena;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.EtherealChains;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.EquipmentBag;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.Barricade;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Dagger;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClothArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.food.SmallRation;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.WornShortsword;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.Collections;

public class ArenaInventory {

    private static Bundle stashedBelongings = null;
    private static Bundle savedQuickslot = null;
    private static boolean active = false;
    private static Hero stashOwner = null;

    public static boolean isActive(){
        return active;
    }

    public static int depth;
    public static int branch;
    public static int pos;

    public static void stashAndStart( Hero hero ){
        if (active && stashOwner == hero) return;

        if (active) {
            stashedBelongings = null;
            savedQuickslot = null;
            active = false;
        }

        Bundle b = new Bundle();
        hero.belongings.storeInBundle( b );

        Bundle bq = new Bundle();
        Dungeon.quickslot.storePlaceholders( bq );
        stashedBelongings = b;
        savedQuickslot = bq;
        stashOwner = hero;
        active = true;

        forceUnequipAll( hero );

        hero.belongings.backpack.clear();
        hero.belongings.weapon = null;
        hero.belongings.armor = null;
        hero.belongings.secondWep = null;
        hero.belongings.rings = new ArrayList<>();
        hero.belongings.artifacts = new ArrayList<>();
        hero.belongings.miscs = new ArrayList<>();

        Dungeon.quickslot.reset();
        QuickSlotButton.reset();
        giveStarterKit( hero );

        GLog.w( Messages.get( ArenaInventory.class, "stashed" ) );
    }

    private static void giveStarterKit( Hero hero ){
        new Dagger().identify().collect();
        new ClothArmor().identify().collect();
        new Barricade().quantity(2).collect();
        new SmallRation().collect();
        new TicketToWaveArena().collect();

        // separate chains lol, I every ethereal chains to be removed from inventory after going back to normal depth.
        EtherealChains.WaveEternalChains chains = new EtherealChains.WaveEternalChains();
        chains.collect();
        chains.identify();
        Dungeon.quickslot.setSlot(0, chains);
    }

    public static void restoreAndMerge( Hero hero ){
        if (!active || stashedBelongings == null) return;

        if (stashOwner != hero) {
            GLog.n( Messages.get( ArenaInventory.class, "stash_mismatch" ) );
            stashedBelongings = null;
            savedQuickslot = null;
            stashOwner = null;
            active = false;
            return;
        }

        Dungeon.hero.belongings.getItem(EtherealChains.WaveEternalChains.class).detach(Dungeon.hero.belongings.backpack);
        forceUnequipAll( hero );

        ArrayList<Item> earned = new ArrayList<>( hero.belongings.backpack.items );
        if (hero.belongings.weapon != null)    earned.add( hero.belongings.weapon );
        if (hero.belongings.armor != null)     earned.add( hero.belongings.armor );
        if (hero.belongings.secondWep != null) earned.add( hero.belongings.secondWep );
        earned.addAll( hero.belongings.rings );
        earned.addAll( hero.belongings.artifacts );
        earned.addAll( hero.belongings.miscs );

        hero.belongings.backpack.clear();
        hero.belongings.weapon = null;
        hero.belongings.armor = null;
        hero.belongings.secondWep = null;
        hero.belongings.rings = new ArrayList<>();
        hero.belongings.artifacts = new ArrayList<>();
        hero.belongings.miscs = new ArrayList<>();

        hero.belongings.restoreFromBundle( stashedBelongings );

        Dungeon.quickslot.reset();
        Dungeon.quickslot.restorePlaceholders( savedQuickslot );
        QuickSlotButton.reset();

        for (Item item : earned) {
            if (item == null) continue;
            if (!item.collect( hero.belongings.backpack )) {
                Dungeon.level.drop( item, hero.pos ).sprite.drop();
            }
        }

        stashedBelongings = null;
        savedQuickslot = null;
        stashOwner = null;
        active = false;

        GLog.p( Messages.get( ArenaInventory.class, "restored" ) );
    }
    private static void forceUnequipAll( Hero hero ){
        boolean addedImmunity = hero.buff( MagicImmune.class ) == null;
        MagicImmune immune = addedImmunity ? Buff.affect( hero, MagicImmune.class, 1f ) : null;

        if (hero.belongings.weapon != null)    hero.belongings.weapon.doUnequip( hero, false, false );
        if (hero.belongings.armor != null)     hero.belongings.armor.doUnequip( hero, false, false );
        if (hero.belongings.secondWep != null) hero.belongings.secondWep.doUnequip( hero, false, false );

        for (Ring ring : new ArrayList<>( hero.belongings.rings )) {
            if (ring != null) ring.doUnequip( hero, false, false );
        }
        for (Artifact art : new ArrayList<>( hero.belongings.artifacts )) {
            if (art != null) art.doUnequip( hero, false, false );
        }
        for (KindofMisc misc : new ArrayList<>( hero.belongings.miscs )) {
            if (misc != null) misc.doUnequip( hero, false, false );
        }

        if (addedImmunity && immune != null) immune.detach();
    }

    public static void reset(){
        stashedBelongings = null;
        savedQuickslot = null;
        stashOwner = null;
        active = false;
    }
}