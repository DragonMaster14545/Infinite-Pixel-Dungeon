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
import com.shatteredpixel.shatteredpixeldungeon.items.fishingrods.GoldenFishingRod;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfExperience;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfEnchantment;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.Raritize;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.SearingSlasher;

import java.util.HashMap;
public class BattlePassTiers {

    public static final long REPEATABLE_TIER_GOLD = 300L;

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
    public static long goldFor( int tier ){
        if (tier == BattlePass.REPEATABLE_TIER) return REPEATABLE_TIER_GOLD;
        return 50L + tier * 10L;
    }

    public static boolean isItemTier( int tier ){
        if (customItems.containsKey( tier )) return true;
        return tier != BattlePass.REPEATABLE_TIER && tier % 5 == 0;
    }
    private static final HashMap<Integer, Item> rewardCache = new HashMap<>();
    public static void resetRewards() {
        rewardCache.clear();
        premiumRewardCache.clear();
    }

    public static HashMap<Integer, Item> rewardSnapshot() {
        return new HashMap<>( rewardCache );
    }

    static {
        BattlePassTiers.setCustomItem( 15, Raritize.class, 4 );
        BattlePassTiers.setCustomItem( 25, ScrollOfEnchantment.class, 2 );
        BattlePassTiers.setCustomItem( 100, GoldenFishingRod.class );
    }
    public static Item rewardFor( int tier ){
        if (!isItemTier( tier )) return null;

        if (!rewardCache.containsKey( tier )){
            CustomReward custom = customItems.get( tier );
            if (custom != null){
                try {
                    Item item = custom.itemClass.newInstance();
                    item.quantity( custom.quantity );
                    rewardCache.put( tier, item );
                } catch (Exception e){
                    rewardCache.put( tier, null );
                }
            } else {
                Generator.Category[] categories = Generator.Category.values();
                int index = Math.min( tier / 5, categories.length - 1 );
                rewardCache.put( tier, Generator.random( categories[index] ) );
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
        return tier >= 1 && tier <= BattlePass.TIER_XP.length;
    }

    public static Item premiumRewardFor( int tier ){
        if (!hasPremiumReward( tier )) return null;

        if (!premiumRewardCache.containsKey( tier )){
            CustomReward custom = premiumItems.get( tier );
            if (custom != null){
                try {
                    Item item = custom.itemClass.newInstance();
                    item.quantity( custom.quantity );
                    premiumRewardCache.put( tier, item );
                } catch (Exception e){
                    premiumRewardCache.put( tier, null );
                }
            } else {
                Generator.Category[] categories = Generator.Category.values();
                int index = Math.min( (tier / 5) + 1, categories.length - 1 );
                premiumRewardCache.put( tier, Generator.random( categories[index] ) );
            }
        }
        return premiumRewardCache.get( tier );
    }

    static {
        setPremiumItem( 10, ScrollOfUpgrade.class, 1 );
        setPremiumItem( 25, PotionOfExperience.class, 1 );
        setPremiumItem( 100, SearingSlasher.class, 1 );
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
}