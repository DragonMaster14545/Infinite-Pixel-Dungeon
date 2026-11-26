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


import java.math.BigInteger;
import com.shatteredpixel.shatteredpixeldungeon.items.ArcaneResin;
import com.shatteredpixel.shatteredpixeldungeon.items.Blueprint;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.fishingrods.GoldenFishingRod;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfExperience;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfEnchantment;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.Raritize;
import com.shatteredpixel.shatteredpixeldungeon.items.test_tubes.Tubes;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Trinket;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.WondrousResin;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLifesteal;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.SearingSlasher;

import java.util.ArrayList;
import java.util.HashMap;
public class BattlePassTiers {

    public static final BigInteger REPEATABLE_TIER_GOLD = BigInteger.valueOf(300L);

    private static class CustomReward {
        final Class<? extends Item> itemClass;
        final int quantity;
        CustomReward( Class<? extends Item> itemClass, int quantity ){
            this.itemClass = itemClass;
            this.quantity = quantity;
        }
    }

    private static final HashMap<Integer, CustomReward> customItems = new HashMap<>();

    public static void setCustomItem( int tier, Class<? extends Item> itemClass ){
        setCustomItem( tier, itemClass, 1 );
    }

    public static void setCustomItem( int tier, Class<? extends Item> itemClass, int quantity ){
        customItems.put( tier, new CustomReward( itemClass, Math.max( 1, quantity ) ) );
    }

    public static void clearCustomItem( int tier ){
        customItems.remove( tier );
    }
    public static BigInteger goldFor( int tier ){
        if (tier == BattlePass.REPEATABLE_TIER) return REPEATABLE_TIER_GOLD;
        return BigInteger.valueOf(50L + tier * 10L);
    }

    private static Item deterministicRandom( Generator.Category[] categories, int tier ){
        String monthKey = BattlePass.currentMonthKey();
        long hash = 1125899906842597L;
        String seed = monthKey + ":" + tier;
        for (int i = 0; i < seed.length(); i++){
            hash = 31*hash + seed.charAt(i);
        }
        int catIndex = (int) Math.floorMod( hash, categories.length );
        Item item;
        do {
            item = Generator.random( categories[catIndex] );
        } while (item instanceof Tubes);
        return item;
    }

    public static boolean isItemTier( int tier ){
        if (customItems.containsKey( tier )) return true;
        return tier != BattlePass.REPEATABLE_TIER && tier % 5 == 0;
    }
    private static final HashMap<Integer, Item> rewardCache = new HashMap<>();
    public static void resetRewards() {
        rewardCache.clear();
        premiumRewardCache.clear();
        rewardExtraCache.clear();
        premiumRewardExtraCache.clear();
        repeatableClasses.clear();
        premiumRepeatableClasses.clear();
        repeatableClassesMonth = null;
    }

    public static HashMap<Integer, Item> rewardSnapshot() {
        return new HashMap<>( rewardCache );
    }

    static {
        BattlePassTiers.setCustomItem( 15, Raritize.class, 4 );
        BattlePassTiers.setCustomItem( 25, Blueprint.class );
        BattlePassTiers.setCustomItem( 50, Blueprint.class );
        BattlePassTiers.setCustomItem( 75, Blueprint.class );
        BattlePassTiers.setCustomItem( 80, WandOfLifesteal.class );
        BattlePassTiers.setCustomItem( 100, WandOfTeleportation.class );
    }
    public static Item rewardFor( int tier ){
        if (!isItemTier( tier )) return null;

        if (!rewardCache.containsKey( tier )){
            CustomReward custom = customItems.get( tier );
            if (custom != null){
                try {
                    Item item = custom.itemClass.newInstance();
                    item.quantity( BigInteger.valueOf(custom.quantity) );
                    rewardCache.put( tier, item );
                } catch (Exception e){
                    rewardCache.put( tier, null );
                }
            } else {
                rewardCache.put( tier, deterministicRandom( Generator.Category.values(), tier ) );
            }
        }
        return rewardCache.get( tier );
    }

    private static final HashMap<Integer, CustomReward> premiumItems = new HashMap<>();
    private static final HashMap<Integer, Item> premiumRewardCache = new HashMap<>();

    public static void setPremiumItem( int tier, Class<? extends Item> itemClass ){
        setPremiumItem( tier, itemClass, 1 );
    }

    public static void setPremiumItem( int tier, Class<? extends Item> itemClass, int quantity ){
        premiumItems.put( tier, new CustomReward( itemClass, Math.max( 1, quantity ) ) );
    }

    public static boolean hasPremiumReward( int tier ){
        return tier >= 1 && tier <= BattlePass.TIER_XP.length + 1;
    }

    public static Item premiumRewardFor( int tier ){
        if (!hasPremiumReward( tier )) return null;

        if (!premiumRewardCache.containsKey( tier )){
            CustomReward custom = premiumItems.get( tier );
            if (custom != null){
                try {
                    Item item = custom.itemClass.newInstance();
                    item.quantity( BigInteger.valueOf(custom.quantity) );
                    premiumRewardCache.put( tier, item );
                } catch (Exception e){
                    premiumRewardCache.put( tier, null );
                }
            } else {
                premiumRewardCache.put( tier, deterministicRandom( Generator.Category.values(), tier ) );
            }
        }
        return premiumRewardCache.get( tier );
    }

    static {
        setPremiumItem( 10, ScrollOfUpgrade.class, 10 );
        setPremiumItem( 75, WandOfLifesteal.class, 1 );
        setPremiumItem( 100, GoldenFishingRod.class, 1 );
    }

    public static boolean isFeaturedPremiumReward( int tier ){
        return premiumItems.containsKey( tier );
    }

    public static void restoreRewards( HashMap<Integer, Item> snapshot ){
        rewardCache.clear();
        rewardCache.putAll( snapshot );
    }

    public static HashMap<Integer, Item> premiumRewardSnapshot() {
        return new HashMap<>( premiumRewardCache );
    }

    public static void restorePremiumRewards( HashMap<Integer, Item> snapshot ){
        premiumRewardCache.clear();
        premiumRewardCache.putAll( snapshot );
    }

    private static final HashMap<Integer, Class<? extends Item>> repeatableClasses = new HashMap<>();
    private static final HashMap<Integer, Class<? extends Item>> premiumRepeatableClasses = new HashMap<>();
    private static String repeatableClassesMonth;

    private static Class<? extends Item> pickRepeatable( String tag, int index, HashMap<Integer, Class<? extends Item>> cache ){
        String monthKey = BattlePass.currentMonthKey();
        if (!monthKey.equals( repeatableClassesMonth )){
            repeatableClasses.clear();
            premiumRepeatableClasses.clear();
            repeatableClassesMonth = monthKey;
        }
        if (cache.containsKey( index )) return cache.get( index );
        Generator.Category[] categories = Generator.Category.values();
        long hash = 1125899906842597L;
        String seed = monthKey + ":" + tag + ":" + index;
        for (int i = 0; i < seed.length(); i++){
            hash = 31*hash + seed.charAt(i);
        }
        int catIndex = (int) Math.floorMod( hash, categories.length );
        Class<? extends Item> result = null;
        com.watabou.utils.Random.pushGenerator( hash );
        try {
            Item sample = Generator.random( categories[catIndex] );
            int guard = 0;
            while (sample instanceof Tubes && guard++ < 50) {
                sample = Generator.random( categories[catIndex] );
            }
            if (sample != null && !(sample instanceof Tubes)) result = sample.getClass();
        } finally {
            com.watabou.utils.Random.popGenerator();
        }
        cache.put( index, result );
        return result;
    }

    private static Item instance( Class<? extends Item> cls ){
        if (cls == null) return null;
        try {
            return cls.newInstance();
        } catch (Exception e){
            return null;
        }
    }

    public static Item repeatableRewardAt( int index ){
        return instance( pickRepeatable( "repeat", index, repeatableClasses ) );
    }

    public static Item premiumRepeatableRewardAt( int index ){
        return instance( pickRepeatable( "premiumRepeat", index, premiumRepeatableClasses ) );
    }

    public static Item repeatableRewardFor(){
        return repeatableRewardAt( BattlePass.repeatableTiersClaimed );
    }

    public static Item premiumRepeatableRewardFor(){
        return premiumRepeatableRewardAt( BattlePass.premiumRepeatableTiersClaimed );
    }

    private static final double BONUS_ITEM_CHANCE = 0.2;

    private static ArrayList<Item> rollExtras(int tier, String seedSuffix ){
        ArrayList<Item> extras = new ArrayList<>();
        String monthKey = BattlePass.currentMonthKey();
        long hash = 1125899906842597L;
        String seed = monthKey + ":" + tier + ":" + seedSuffix;
        for (int i = 0; i < seed.length(); i++){
            hash = 31*hash + seed.charAt(i);
        }
        double roll = Math.floorMod( hash, 10000L ) / 10000.0;
        if (roll < BONUS_ITEM_CHANCE) {
            Generator.Category[] categories = Generator.Category.values();
            int catIndex = (int) Math.floorMod( hash / 7, categories.length );
            Item bonus = Generator.random( categories[catIndex] );
            if (bonus != null) extras.add( bonus );
        }
        return extras;
    }

    private static final HashMap<Integer, ArrayList<Item>> rewardExtraCache = new HashMap<>();
    private static final HashMap<Integer, ArrayList<Item>> premiumRewardExtraCache = new HashMap<>();

    public static ArrayList<Item> rewardExtrasFor( int tier ){
        if (tier != BattlePass.REPEATABLE_TIER && !isItemTier( tier )) return new ArrayList<>();
        if (!rewardExtraCache.containsKey( tier )){
            ArrayList<Item> extras = new ArrayList<>();
            ArrayList<CustomReward> custom = customExtraItems.get( tier );
            if (custom != null && !custom.isEmpty()){
                for (CustomReward cr : custom){
                    try {
                        Item item = cr.itemClass.newInstance();
                        item.quantity( BigInteger.valueOf(cr.quantity) );
                        extras.add( item );
                    } catch (Exception e){
                        // hehe no message
                    }
                }
            } else {
                extras.addAll( rollExtras( tier, "extra" ) );
            }
            rewardExtraCache.put( tier, extras );
        }
        return rewardExtraCache.get( tier );
    }

    public static ArrayList<Item> premiumRewardExtrasFor( int tier ){
        if (!hasPremiumReward( tier )) return new ArrayList<>();
        if (!premiumRewardExtraCache.containsKey( tier )){
            ArrayList<Item> extras = new ArrayList<>();
            ArrayList<CustomReward> custom = premiumExtraItems.get( tier );
            if (custom != null && !custom.isEmpty()){
                for (CustomReward cr : custom){
                    try {
                        Item item = cr.itemClass.newInstance();
                        item.quantity( BigInteger.valueOf(cr.quantity) );
                        extras.add( item );
                    } catch (Exception e){
                        // also this one
                    }
                }
            } else {
                extras.addAll( rollExtras( tier, "premiumExtra" ) );
            }
            premiumRewardExtraCache.put( tier, extras );
        }
        return premiumRewardExtraCache.get( tier );
    }

    public static HashMap<Integer, ArrayList<Item>> rewardExtraSnapshot() {
        return new HashMap<>( rewardExtraCache );
    }

    public static HashMap<Integer, ArrayList<Item>> premiumRewardExtraSnapshot() {
        return new HashMap<>( premiumRewardExtraCache );
    }

    private static final HashMap<Integer, ArrayList<CustomReward>> customExtraItems  = new HashMap<>();
    private static final HashMap<Integer, ArrayList<CustomReward>> premiumExtraItems = new HashMap<>();

    public static void addCustomExtraItem( int tier, Class<? extends Item> itemClass ){
        addCustomExtraItem( tier, itemClass, 1 );
    }
    public static void addCustomExtraItem( int tier, Class<? extends Item> itemClass, int quantity ){
        if (!customExtraItems.containsKey( tier )) customExtraItems.put( tier, new ArrayList<>() );
        customExtraItems.get( tier ).add( new CustomReward( itemClass, Math.max( 1, quantity ) ) );
    }

    public static void addPremiumExtraItem( int tier, Class<? extends Item> itemClass ){
        addPremiumExtraItem( tier, itemClass, 1 );
    }
    public static void addPremiumExtraItem( int tier, Class<? extends Item> itemClass, int quantity ){
        if (!premiumExtraItems.containsKey( tier )) premiumExtraItems.put( tier, new ArrayList<>() );
        premiumExtraItems.get( tier ).add( new CustomReward( itemClass, Math.max( 1, quantity ) ) );
    }
}