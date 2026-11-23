package com.shatteredpixel.shatteredpixeldungeon.levels;


import java.math.BigInteger;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
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
import com.shatteredpixel.shatteredpixeldungeon.levels.WaveArenaLevel;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;
import com.watabou.utils.FileUtils;

import java.io.IOException;
import java.util.Iterator;

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

    private static String file() {
        return "arena_inventory_" + GamesInProgress.curSlot + ".dat";
    }

    private static void persist() {
        try {
            Bundle b = new Bundle();
            b.put( "seed", Dungeon.seed );
            b.put( "depth", depth );
            b.put( "branch", branch );
            b.put( "pos", pos );
            b.put( "belongings", stashedBelongings );
            b.put( "quickslot", savedQuickslot );
            FileUtils.bundleToFile( file(), b );
        } catch (IOException e) {
            Game.reportException( e );
        }
    }

    private static void clearFile() {
        try {
            if (FileUtils.fileExists( file() )) FileUtils.deleteFile( file() );
        } catch (Exception ignored) {
        }
    }

    public static void ensureLoaded() {
        if (active && stashedBelongings != null) return;
        try {
            if (!FileUtils.fileExists( file() )) return;
            Bundle b = FileUtils.bundleFromFile( file() );
            if (b.getLong( "seed" ) != Dungeon.seed) {
                clearFile();
                return;
            }
            stashedBelongings = b.getBundle( "belongings" );
            savedQuickslot = b.getBundle( "quickslot" );
            depth = b.getInt( "depth" );
            branch = b.getInt( "branch" );
            pos = b.getInt( "pos" );
            stashOwner = Dungeon.hero;
            active = true;
        } catch (Exception e) {
            Game.reportException( e );
        }
    }

    public static void stashAndStart( Hero hero ){
        ensureLoaded();
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
        persist();

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
        new Barricade().quantity(BigInteger.valueOf(2)).collect();
        new SmallRation().collect();
        new TicketToWaveArena().collect();

        // separate chains lol, I every ethereal chains to be removed from inventory after going back to normal depth.
        EtherealChains.WaveEternalChains chains = new EtherealChains.WaveEternalChains();
        chains.collect();
        chains.identify();
        Dungeon.quickslot.setSlot(0, chains);
    }

    public static void restoreAndMerge( Hero hero ){
        ensureLoaded();
        if (!active || stashedBelongings == null) return;

        if (stashOwner != hero) {
            GLog.n( Messages.get( ArenaInventory.class, "stash_mismatch" ) );
            stashedBelongings = null;
            savedQuickslot = null;
            stashOwner = null;
            active = false;
            clearFile();
            return;
        }

        EtherealChains.WaveEternalChains chains = hero.belongings.getItem(EtherealChains.WaveEternalChains.class);
        if (chains != null) chains.detach(hero.belongings.backpack);
        Buff.detach( hero, WaveArenaLevel.WaveCountdown.class );
        forceUnequipAll( hero );

        ArrayList<Item> earned = new ArrayList<>( hero.belongings.backpack.items );
        if (hero.belongings.weapon != null)    earned.add( hero.belongings.weapon );
        if (hero.belongings.armor != null)     earned.add( hero.belongings.armor );
        if (hero.belongings.secondWep != null) earned.add( hero.belongings.secondWep );
        earned.addAll( hero.belongings.rings );
        earned.addAll( hero.belongings.artifacts );
        earned.addAll( hero.belongings.miscs );

        removeKit( earned, Dagger.class, 1 );
        removeKit( earned, ClothArmor.class, 1 );
        removeKit( earned, Barricade.class, 2 );
        removeKit( earned, SmallRation.class, 1 );

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
        clearFile();

        GLog.p( Messages.get( ArenaInventory.class, "restored" ) );
    }

    private static void removeKit( ArrayList<Item> earned, Class<?> cls, int count ){
        for (Iterator<Item> it = earned.iterator(); it.hasNext() && count > 0; ) {
            Item item = it.next();
            if (item == null || item.getClass() != cls) continue;
            BigInteger have = item.quantity();
            BigInteger take = BigInteger.valueOf( count );
            if (have.compareTo( take ) > 0) {
                item.quantity( have.subtract( take ) );
                count = 0;
            } else {
                count -= have.intValue();
                it.remove();
            }
        }
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
        clearFile();
    }
}