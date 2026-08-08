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

import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfEnchantment;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.GalacticInfusion;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.Raritize;
import com.shatteredpixel.shatteredpixeldungeon.items.treasurebags.BiggerGambleBag;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.BattlePassScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndMessage;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.utils.Bundle;
import com.watabou.utils.FileUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class BattlePass {

    public static final int XP_PER_DEPTH = 15;

    //XP granted per enemy slain
    public static final int XP_PER_KILL = 2;

    //1 XP per this many gold collected (rounded down)
    public static final int GOLD_PER_XP = 1000;

    public static final int[] TIER_XP = buildTierCosts();
    private static int[] buildTierCosts(){
        int[] costs = new int[100];
        for (int i = 0; i < costs.length; i++){
            costs[i] = 100 + i*100;
        }
        return costs;
    }

    public static final int REPEATABLE_TIER    = TIER_XP.length + 1; //101
    public static final int REPEATABLE_TIER_XP = 100 + TIER_XP.length*100; //3100, continues the same cost curve
    public static final int RESET_ENERGY_COST = 600000;
    public static final int HISTORY_CLAIM_ENERGY_COST = 100;

    public static int totalXP;
    public static ArrayList<Integer> claimedTiers = new ArrayList<>();
    public static ArrayList<Integer> premiumClaimedTiers = new ArrayList<>();
    //how many times the repeatable tier has been claimed this month
    public static int repeatableTiersClaimed;
    public static int premiumRepeatableTiersClaimed;

    private static String monthKey;
    private static ArrayList<MonthRecord> history = new ArrayList<>();
    private static final int MAX_HISTORY_MONTHS = 12;

    private static final SimpleDateFormat MONTH_KEY_FORMAT =
            new SimpleDateFormat( "yyyy-MM", Locale.US );
    private static final SimpleDateFormat MONTH_LABEL_FORMAT =
            new SimpleDateFormat( "MMMM yyyy", Locale.US );

    private static boolean loaded = false;

    private static void ensureLoaded(){
        if (!loaded){
            loadGlobal();
            loaded = true;
        }
        checkRollover();
    }

    private static void checkRollover(){
        String now = MONTH_KEY_FORMAT.format( new Date() );
        if (monthKey == null){
            monthKey = now;
            return;
        }
        if (monthKey.equals( now ) || monthKey.startsWith( now + "-r" )) return;

        history.add( 0, new MonthRecord( monthKey, totalXP, new ArrayList<>( claimedTiers ), repeatableTiersClaimed,
                BattlePassTiers.rewardSnapshot(), seasonName( monthKey ), premium,
                new ArrayList<>( premiumClaimedTiers ), BattlePassTiers.premiumRewardSnapshot(),
                premiumRepeatableTiersClaimed, BattlePassTiers.rewardExtraSnapshot(),
                BattlePassTiers.premiumRewardExtraSnapshot() ) );
        while (history.size() > MAX_HISTORY_MONTHS){
            history.remove( history.size()-1 );
        }

        monthKey = now;
        totalXP = 0;
        claimedTiers = new ArrayList<>();
        repeatableTiersClaimed = 0;
        premiumRepeatableTiersClaimed = 0;
        premiumClaimedTiers = new ArrayList<>();
        premium = false;
        BattlePassTiers.resetRewards();
        saveGlobal();
    }

    public static boolean canAffordReset(){
        ensureLoaded();
        return Dungeon.energy >= RESET_ENERGY_COST && isBattlePassFinished();
    }

    public static boolean isBattlePassFinished(){
        ensureLoaded();
        return tiersReached() >= TIER_XP.length;
    }

    public static boolean resetImmediately(){
        ensureLoaded();
        if (!canAffordReset()) return false;

        Dungeon.energy -= RESET_ENERGY_COST;

        history.add( 0, new MonthRecord( monthKey, totalXP, new ArrayList<>( claimedTiers ), repeatableTiersClaimed,
            BattlePassTiers.rewardSnapshot(), seasonName( monthKey ), premium,
            new ArrayList<>( premiumClaimedTiers ), BattlePassTiers.premiumRewardSnapshot(),
            premiumRepeatableTiersClaimed, BattlePassTiers.rewardExtraSnapshot(),
            BattlePassTiers.premiumRewardExtraSnapshot() ) );
        while (history.size() > MAX_HISTORY_MONTHS){
            history.remove( history.size()-1 );
        }

        monthKey = monthKey + "-r" + System.currentTimeMillis();
        totalXP = 0;
        claimedTiers = new ArrayList<>();
        repeatableTiersClaimed = 0;
        premiumClaimedTiers = new ArrayList<>();
        premiumRepeatableTiersClaimed = 0;
        premium = false;
        BattlePassTiers.resetRewards();

        saveGlobal();
        return true;
    }

    public static boolean canAffordHistoryClaim(){
        return Dungeon.energy >= HISTORY_CLAIM_ENERGY_COST;
    }

    public static Item buyHistoryTier( String monthKey, int tier, boolean premiumTrack ){
        ensureLoaded();
        MonthRecord record = historyRecord( monthKey );
        if (record == null || !canAffordHistoryClaim()) return null;

        boolean tierWasUnlocked = tier == REPEATABLE_TIER
                ? record.repeatableTiersReached() > 0
                : tier <= record.tiersReached();
        if (!tierWasUnlocked) return null;

        Item reward;
        if (premiumTrack) {
            if (!record.premium) return null;
            if (tier == REPEATABLE_TIER) {
                if (record.premiumRepeatableTiersClaimed >= record.repeatableTiersReached()) return null;
                record.premiumRepeatableTiersClaimed++;
                reward = BattlePassTiers.premiumRepeatableRewardFor();
            } else {
                if (record.premiumClaimedTiers.contains( tier )) return null;
                record.premiumClaimedTiers.add( tier );
                reward = record.premiumRewardSnapshot.get( tier );
            }
        } else {
            if (tier == REPEATABLE_TIER) {
                if (record.repeatableTiersClaimed >= record.repeatableTiersReached()) return null;
                record.repeatableTiersClaimed++;
                reward = BattlePassTiers.repeatableRewardFor();
            } else {
                if (record.claimedTiers.contains( tier )) return null;
                record.claimedTiers.add( tier );
                reward = record.rewardSnapshot.get( tier );
            }
        }

        Dungeon.energy -= HISTORY_CLAIM_ENERGY_COST;

        if (reward != null && Dungeon.hero != null){
            reward.collect();
        } else if (reward == null && !premiumTrack) {
            Dungeon.gold += BattlePassTiers.goldFor( tier );
        }

        saveGlobal();
        return reward;
    }

    private static final String[] SEASON_ADJ = {
            "Ember", "Frost", "Blood", "Storm", "Shadow", "Verdant",
            "Golden", "Crimson", "Silent", "Wild", "Ashen", "Hollow",
            "Iron", "Obsidian", "Radiant", "Withered", "Feral", "Gilded",
            "Molten", "Glacial", "Spectral", "Savage", "Sable", "Burning",
            "Twilight", "Forsaken", "Emerald", "Bleak", "Thundering", "Sunken",
            "Venomous", "Astral", "Charred", "Cursed", "Divine", "Frozen",
            "Grim", "Hallowed", "Infernal", "Jagged", "Lunar", "Murky",
            "Nocturnal", "Onyx", "Primal", "Rotten", "Solar", "Tempest",
            "Umbral", "Vile", "Whispering", "Abyssal", "Broken", "Celestial",
            "Draconic", "Eternal", "Fabled", "Ghastly", "Haunted", "Immortal"
    };
    private static final String[] SEASON_NOUN = {
            "Moon", "Reckoning", "Bloom", "Vigil", "Ascent", "Tide",
            "Dawn", "Rising", "Echo", "Requiem", "Gauntlet", "Descent",
            "Eclipse", "Hunt", "Covenant", "Uprising", "Pact", "Harvest",
            "Wake", "Trial", "Siege", "Passage", "Awakening", "Reign",
            "Crossing", "Omen", "Vow", "Blight", "Convergence", "Nightfall",
            "Abyss", "Chorus", "Deluge", "Ember", "Fracture", "Genesis",
            "Horizon", "Ingress", "Judgment", "Kinship", "Labyrinth", "Maelstrom",
            "Nemesis", "Onslaught", "Prophecy", "Quietus", "Rampart", "Sanctum",
            "Threshold", "Undertow", "Verdict", "Wraith", "Zenith", "Ashfall",
            "Bastion", "Cataclysm", "Dominion", "Exodus", "Fable", "Gloaming"
    };

    private static final HashMap<String, String> seasonNameOverrides = new HashMap<>();

    public static void setSeasonName( String monthKey, String name ){
        seasonNameOverrides.put( monthKey, name );
    }

    private static String cachedSeasonName;
    private static String cachedSeasonNameKey;

    public static String seasonName( String monthKey ){
        String custom = seasonNameOverrides.get( monthKey );
        if (custom != null) return custom;

        if (!monthKey.equals( cachedSeasonNameKey )){
            long hash = 1125899906842597L; //arbitrary odd seed
            for (int i = 0; i < monthKey.length(); i++){
                hash = 31*hash + monthKey.charAt(i);
            }

            int adjIndex  = (int) Math.floorMod( hash,     SEASON_ADJ.length );
            int nounIndex = (int) Math.floorMod( hash / 31, SEASON_NOUN.length );

            cachedSeasonName = SEASON_ADJ[adjIndex] + " " + SEASON_NOUN[nounIndex];
            cachedSeasonNameKey = monthKey;
        }
        return cachedSeasonName;
    }

    public static String currentSeasonName(){
        ensureLoaded();
        return seasonName( monthKey );
    }

    public static String previousSeasonName(){
        ensureLoaded();
        if (history.isEmpty()) return null;
        return history.get( 0 ).seasonName; //history is stored most-recent-first
    }

    public static final int PREMIUM_COST_GOLD = 500;
    public static Class<? extends Item> premiumCostItem = null;

    public static boolean premium;
    public static int premiumCostItemQuantity = 300;
    private static final Class<? extends Item>[] PREMIUM_COST_POOL = new Class[] {
            Raritize.class, ScrollOfUpgrade.class, GalacticInfusion.class,
            BiggerGambleBag.class, PotionOfHealing.class, ScrollOfEnchantment.class
    };
    public static boolean canAffordPremium(){
        ensureLoaded();
        Class<? extends Item> costItem = premiumCostItem();
        return Dungeon.hero != null
                && Dungeon.hero.belongings.count( costItem ) >= premiumCostItemQuantity;
    }
    public static boolean isPremium(){
        ensureLoaded();
        return premium;
    }

    public static boolean isPremiumUnlocked(){
        ensureLoaded();
        return premium;
    }

    public static void unlockPremium(){
        if (BattlePass.isPremium()) {
            ShatteredPixelDungeon.scene().addToFront( new WndMessage( Messages.get( BattlePassScene.class, "premium_already_unlocked" ) ) );
            return;
        }

        Class<? extends Item> costItem = BattlePass.premiumCostItem();
        int qty = BattlePass.premiumCostItemQuantity;

        if (!BattlePass.canAffordPremium()) {
            ShatteredPixelDungeon.scene().addToFront( new WndMessage(
                    Messages.get( BattlePassScene.class, "premium_cant_afford", qty, Messages.get( costItem, "name" ) ) ) );
            return;
        }

        Item costIcon;
        try {
            costIcon = BattlePass.premiumCostItem().newInstance();
        } catch (Exception e) {
            costIcon = new Gold( BattlePass.premiumCostItemQuantity );
        }

        ItemSprite icon = new ItemSprite();
        icon.view( costIcon );

        ShatteredPixelDungeon.scene().addToFront( new WndOptions(
                icon,
                Messages.get( BattlePassScene.class, "premium_confirm_title" ),
                Messages.get( BattlePassScene.class, "premium_confirm_body", qty, Messages.get( costItem, "name" ) ),
                Messages.get( BattlePassScene.class, "premium_confirm_yes" ),
                Messages.get( BattlePassScene.class, "premium_confirm_no" ) ){
            @Override
            protected void onSelect( int index ){
                if (index == 0 && BattlePass.purchasePremium()) {
                    GLog.p( Messages.get( BattlePassScene.class, "premium_unlocked" ) );
                    BattlePassScene.seeCurrentMonth();
                }
            }
        } );
    }

    private static Class<? extends Item> cachedPremiumCostItem;
    private static String cachedPremiumCostKey;

    public static Class<? extends Item> premiumCostItem(){
        ensureLoaded();
        if (!monthKey.equals( cachedPremiumCostKey )){
            long hash = 1125899906842597L;
            for (int i = 0; i < monthKey.length(); i++){
                hash = 31*hash + monthKey.charAt(i);
            }
            int index = (int) Math.floorMod( hash, PREMIUM_COST_POOL.length );
            cachedPremiumCostItem = PREMIUM_COST_POOL[index];
            cachedPremiumCostKey = monthKey;
        }
        return cachedPremiumCostItem;
    }

    public static boolean purchasePremium(){
        ensureLoaded();
        if (premium) return true;
        if (!canAffordPremium()) return false;

        Dungeon.hero.belongings.removeItem( premiumCostItem(), premiumCostItemQuantity );

        premium = true;
        saveGlobal();
        return true;
    }

    public static class MonthRecord {

        public final String monthKey;
        public final int finalXP;
        public final ArrayList<Integer> claimedTiers;
        public final HashMap<Integer, Item> rewardSnapshot;
        public final HashMap<Integer, ArrayList<Item>> rewardExtraSnapshot;
        public int repeatableTiersClaimed;
        public final String seasonName;
        public final boolean premium;
        public final ArrayList<Integer> premiumClaimedTiers;
        public final HashMap<Integer, Item> premiumRewardSnapshot;
        public final HashMap<Integer, ArrayList<Item>> premiumRewardExtraSnapshot;
        public int premiumRepeatableTiersClaimed;

        MonthRecord( String monthKey, int finalXP, ArrayList<Integer> claimedTiers,
                     int repeatableTiersClaimed, HashMap<Integer, Item> rewardSnapshot, String seasonName,
                     boolean premium, ArrayList<Integer> premiumClaimedTiers,
                     HashMap<Integer, Item> premiumRewardSnapshot, int premiumRepeatableTiersClaimed,
                     HashMap<Integer, ArrayList<Item>> rewardExtraSnapshot,
                     HashMap<Integer, ArrayList<Item>> premiumRewardExtraSnapshot ){
            this.monthKey = monthKey;
            this.finalXP = finalXP;
            this.claimedTiers = claimedTiers;
            this.repeatableTiersClaimed = repeatableTiersClaimed;
            this.rewardSnapshot = rewardSnapshot;
            this.seasonName = seasonName;
            this.premium = premium;
            this.premiumClaimedTiers = premiumClaimedTiers;
            this.premiumRewardSnapshot = premiumRewardSnapshot;
            this.premiumRepeatableTiersClaimed = premiumRepeatableTiersClaimed;
            this.rewardExtraSnapshot = rewardExtraSnapshot;
            this.premiumRewardExtraSnapshot = premiumRewardExtraSnapshot;
        }

        public int tiersReached(){
            return tiersReachedForXP( finalXP );
        }

        public int repeatableTiersReached(){
            return repeatableTiersUnlockedForXP( finalXP );
        }

        public String label(){
            return BattlePass.label( monthKey );
        }

        private static final String M_KEY       = "month";
        private static final String M_XP        = "xp";
        private static final String M_CLAIMED   = "claimed";
        private static final String M_REPEATED  = "repeated";
        private static final String M_REWARDTIERS  = "reward_tiers";
        private static final String M_REWARDPREFIX  = "reward_";
        private static final String M_SEASON_NAME = "season_name";
        private static final String M_PREMIUM = "premium";
        private static final String M_PREMIUM_CLAIMED = "premium_claimed";
        private static final String M_PREMIUM_REWARDTIERS = "premium_reward_tiers";
        private static final String M_PREMIUM_REWARDPREFIX = "premium_reward_";
        private static final String M_PREMIUM_REPEATED = "premium_repeated";
        private static final String M_EXTRA_TIERS = "extra_tiers";
        private static final String M_EXTRA_COUNT_PREFIX = "extra_count_";
        private static final String M_EXTRA_ITEM_PREFIX = "extra_item_";
        private static final String M_PREMIUM_EXTRA_TIERS = "premium_extra_tiers";
        private static final String M_PREMIUM_EXTRA_COUNT_PREFIX = "premium_extra_count_";
        private static final String M_PREMIUM_EXTRA_ITEM_PREFIX = "premium_extra_item_";

        private static void storeExtras( Bundle b, HashMap<Integer, ArrayList<Item>> extras,
                                         String tiersKey, String countPrefix, String itemPrefix ){
            int[] tiers = new int[extras.size()];
            int i = 0;
            for (int t : extras.keySet()) tiers[i++] = t;
            b.put( tiersKey, tiers );
            for (int t : tiers) {
                ArrayList<Item> list = extras.get( t );
                b.put( countPrefix + t, list.size() );
                for (int j = 0; j < list.size(); j++) {
                    b.put( itemPrefix + t + "_" + j, list.get(j) );
                }
            }
        }

        private static HashMap<Integer, ArrayList<Item>> restoreExtras( Bundle b,
                                                                        String tiersKey, String countPrefix, String itemPrefix ){
            HashMap<Integer, ArrayList<Item>> extras = new HashMap<>();
            if (b.contains( tiersKey )) {
                for (int t : b.getIntArray( tiersKey )) {
                    int count = b.contains( countPrefix + t ) ? b.getInt( countPrefix + t ) : 0;
                    ArrayList<Item> list = new ArrayList<>();
                    for (int j = 0; j < count; j++) {
                        Object item = b.get( itemPrefix + t + "_" + j );
                        if (item != null) list.add( (Item) item );
                    }
                    extras.put( t, list );
                }
            }
            return extras;
        }

        static MonthRecord restore( Bundle b ){
            String key = b.getString( M_KEY );
            int xp = b.getInt( M_XP );
            ArrayList<Integer> claimed = new ArrayList<>();
            for (int t : b.getIntArray( M_CLAIMED )) claimed.add( t );
            int repeated = b.contains( M_REPEATED ) ? b.getInt( M_REPEATED ) : 0;

            HashMap<Integer, Item> rewardSnapshot = new HashMap<>();
            if (b.contains( M_REWARDTIERS )) {
                for (int t : b.getIntArray( M_REWARDTIERS )) {
                    rewardSnapshot.put(t, (Item) b.get(M_REWARDPREFIX + t));
                }
            }
            String seasonName = b.contains( M_SEASON_NAME ) ? b.getString( M_SEASON_NAME ) : BattlePass.label( key );
            boolean premium = b.contains( M_PREMIUM ) && b.getBoolean( M_PREMIUM );

            ArrayList<Integer> premiumClaimed = new ArrayList<>();
            if (b.contains( M_PREMIUM_CLAIMED )){
                for (int t : b.getIntArray( M_PREMIUM_CLAIMED )) premiumClaimed.add( t );
            }
            HashMap<Integer, Item> premiumRewardSnapshot = new HashMap<>();
            if (b.contains( M_PREMIUM_REWARDTIERS )) {
                for (int t : b.getIntArray( M_PREMIUM_REWARDTIERS )) {
                    premiumRewardSnapshot.put(t, (Item) b.get(M_PREMIUM_REWARDPREFIX + t));
                }
            }
            int premiumRepeated = b.contains( M_PREMIUM_REPEATED ) ? b.getInt( M_PREMIUM_REPEATED ) : 0;

            HashMap<Integer, ArrayList<Item>> rewardExtras =
                    restoreExtras( b, M_EXTRA_TIERS, M_EXTRA_COUNT_PREFIX, M_EXTRA_ITEM_PREFIX );
            HashMap<Integer, ArrayList<Item>> premiumRewardExtras =
                    restoreExtras( b, M_PREMIUM_EXTRA_TIERS, M_PREMIUM_EXTRA_COUNT_PREFIX, M_PREMIUM_EXTRA_ITEM_PREFIX );

            return new MonthRecord( key, xp, claimed, repeated, rewardSnapshot, seasonName, premium,
                    premiumClaimed, premiumRewardSnapshot, premiumRepeated, rewardExtras, premiumRewardExtras );
        }

        Bundle store(){
            Bundle b = new Bundle();
            b.put( M_KEY, monthKey );
            b.put( M_XP, finalXP );
            int[] arr = new int[claimedTiers.size()];
            for (int i = 0; i < arr.length; i++) arr[i] = claimedTiers.get(i);
            b.put( M_CLAIMED, arr );
            b.put( M_REPEATED, repeatableTiersClaimed );

            int[] rewardTiers = new int[rewardSnapshot.size()];
            int i = 0;
            for (int t: rewardSnapshot.keySet()) rewardTiers[i++] = t;
            b.put(M_REWARDTIERS, rewardTiers);
            for (int t : rewardTiers) b.put(M_REWARDPREFIX + t, rewardSnapshot.get(t));
            b.put( M_SEASON_NAME, seasonName );
            b.put( M_PREMIUM, premium );

            int[] premClaimedArr = new int[premiumClaimedTiers.size()];
            for (int j = 0; j < premClaimedArr.length; j++) premClaimedArr[j] = premiumClaimedTiers.get(j);
            b.put( M_PREMIUM_CLAIMED, premClaimedArr );

            int[] premRewardTiers = new int[premiumRewardSnapshot.size()];
            int k = 0;
            for (int t : premiumRewardSnapshot.keySet()) premRewardTiers[k++] = t;
            b.put( M_PREMIUM_REWARDTIERS, premRewardTiers );
            for (int t : premRewardTiers) b.put( M_PREMIUM_REWARDPREFIX + t, premiumRewardSnapshot.get(t) );
            b.put( M_PREMIUM_REPEATED, premiumRepeatableTiersClaimed );

            storeExtras( b, rewardExtraSnapshot, M_EXTRA_TIERS, M_EXTRA_COUNT_PREFIX, M_EXTRA_ITEM_PREFIX );
            storeExtras( b, premiumRewardExtraSnapshot, M_PREMIUM_EXTRA_TIERS, M_PREMIUM_EXTRA_COUNT_PREFIX, M_PREMIUM_EXTRA_ITEM_PREFIX );

            return b;
        }
    }

    private static String label( String monthKey ){
        try {
            return MONTH_LABEL_FORMAT.format( MONTH_KEY_FORMAT.parse( monthKey ) );
        } catch (Exception e) {
            return monthKey;
        }
    }

    private static int lastDeepestFloor;
    private static int lastEnemiesSlain;
    private static long lastGoldCollected;

    public static void onRunStart(){
        lastDeepestFloor = 0;
        lastEnemiesSlain = 0;
        lastGoldCollected = 0;
        Tasks.onRunStart();
    }

    public static void onRunEnd(){
        ensureLoaded();

        int deltaDepth = Statistics.deepestFloor - lastDeepestFloor;
        if (deltaDepth > 0){
            totalXP += deltaDepth * XP_PER_DEPTH;
            lastDeepestFloor = Statistics.deepestFloor;
        }

        int deltaKills = Statistics.enemiesSlain - lastEnemiesSlain;
        if (deltaKills > 0){
            totalXP += deltaKills * XP_PER_KILL;
            lastEnemiesSlain = Statistics.enemiesSlain;
        }

        long deltaGold = Statistics.goldCollected - lastGoldCollected;
        if (deltaGold > 0){
            totalXP += (int)(deltaGold / GOLD_PER_XP);
            lastGoldCollected = Statistics.goldCollected;
        }

        boolean deltaIsAscended = Statistics.ascended;
        if (deltaIsAscended) {
            totalXP *= 2; // hehe
        }

        saveGlobal();
    }

    public static int tiersReached(){
        ensureLoaded();
        return tiersReachedForXP( totalXP );
    }

    private static int tiersReachedForXP( int xp ){
        int xpLeft = xp;
        int tier = 0;
        for (int cost : TIER_XP){
            if (xpLeft < cost) break;
            xpLeft -= cost;
            tier++;
        }
        return tier;
    }

    public static int repeatableTiersUnlocked(){
        ensureLoaded();
        return repeatableTiersUnlockedForXP( totalXP );
    }

    private static int repeatableTiersUnlockedForXP( int xp ){
        int xpLeft = xp;
        for (int cost : TIER_XP){
            if (xpLeft < cost) return 0;
            xpLeft -= cost;
        }
        return xpLeft / REPEATABLE_TIER_XP;
    }

    public static int repeatableTiersAvailable(){
        ensureLoaded();
        return Math.max( 0, repeatableTiersUnlocked() - repeatableTiersClaimed );
    }

    public static int xpIntoCurrentTier(){
        ensureLoaded();
        int xpLeft = totalXP;
        for (int cost : TIER_XP){
            if (xpLeft < cost) return xpLeft;
            xpLeft -= cost;
        }
        return xpLeft % REPEATABLE_TIER_XP;
    }

    public static int xpForCurrentTier(){
        int tier = tiersReached();
        if (tier >= TIER_XP.length) return REPEATABLE_TIER_XP;
        return TIER_XP[tier];
    }

    public static boolean isUnlocked( int tier ){
        if (tier == REPEATABLE_TIER) return repeatableTiersUnlocked() > 0;
        return tier >= 1 && tier <= tiersReached();
    }

    public static boolean isClaimed( int tier ){
        ensureLoaded();
        if (tier == REPEATABLE_TIER) return repeatableTiersClaimed >= repeatableTiersUnlocked();
        return claimedTiers.contains( tier );
    }

    public static boolean isClaimable( int tier ){
        return isUnlocked( tier ) && !isClaimed( tier );
    }

    public static boolean isPremiumClaimed( int tier ){
        ensureLoaded();
        if (tier == REPEATABLE_TIER) {
            return premiumRepeatableTiersClaimed >= repeatableTiersUnlocked();
        }
        return premiumClaimedTiers.contains( tier );
    }

    public static boolean isPremiumClaimable( int tier ){
        if (tier == REPEATABLE_TIER) {
            return premium && repeatableTiersUnlocked() > premiumRepeatableTiersClaimed;
        }
        return premium && isUnlocked( tier ) && BattlePassTiers.hasPremiumReward( tier ) && !isPremiumClaimed( tier );
    }

    public static Item claimPremium( int tier ){
        ensureLoaded();
        if (!isPremiumClaimable( tier )) return null;

        Item bonus;
        ArrayList<Item> extras = BattlePassTiers.premiumRewardExtrasFor( tier );

        if (tier == REPEATABLE_TIER) {
            premiumRepeatableTiersClaimed++;
            bonus = BattlePassTiers.premiumRepeatableRewardFor();
        } else {
            premiumClaimedTiers.add( tier );
            bonus = BattlePassTiers.premiumRewardFor( tier );
        }

        if (Dungeon.hero != null){
            if (bonus != null) bonus.collect();
            for (Item extra : extras) extra.collect();
        }

        saveGlobal();
        return bonus;
    }

    public static Item claim( int tier ){
        ensureLoaded();
        if (!isClaimable( tier )) return null;

        Item result = null;
        ArrayList<Item> extras = BattlePassTiers.rewardExtrasFor( tier );

        if (tier == REPEATABLE_TIER){
            repeatableTiersClaimed++;
            Item reward = BattlePassTiers.repeatableRewardFor();
            if (reward != null && Dungeon.hero != null){
                reward.collect();
                result = reward;
            } else {
                Dungeon.gold += BattlePassTiers.goldFor( tier );
            }
        } else {
            claimedTiers.add( tier );
            if (BattlePassTiers.isItemTier( tier )){
                Item reward = BattlePassTiers.rewardFor( tier );
                if (reward != null && Dungeon.hero != null){
                    reward.collect();
                    result = reward;
                } else {
                    Dungeon.gold += BattlePassTiers.goldFor( tier );
                }
            } else {
                Dungeon.gold += BattlePassTiers.goldFor( tier );
            }
        }

        if (Dungeon.hero != null){
            for (Item extra : extras) extra.collect();
        }

        saveGlobal();
        return result;
    }


    public static String currentMonthKey(){
        ensureLoaded();
        return monthKey;
    }

    public static String currentMonthLabel(){
        ensureLoaded();
        return label( monthKey );
    }

    public static int daysRemainingInMonth(){
        ensureLoaded();
        Calendar cal = Calendar.getInstance();
        int today = cal.get( Calendar.DAY_OF_MONTH );
        int lastDay = cal.getActualMaximum( Calendar.DAY_OF_MONTH );
        return lastDay - today + 1;
    }

    public static String timeRemainingInMonth() {
        ensureLoaded();
        Calendar end = Calendar.getInstance();
        end.set( Calendar.DAY_OF_MONTH, end.getActualMaximum( Calendar.DAY_OF_MONTH ));
        end.set( Calendar.HOUR_OF_DAY, 23 );
        end.set( Calendar.MINUTE, 59 );
        end.set( Calendar.SECOND, 59 );
        end.set( Calendar.MILLISECOND, 999 );

        long millisecondsLeft = Math.max(0,end.getTimeInMillis() - System.currentTimeMillis());
        long days = millisecondsLeft / (24*60*60*1000L);
        long hours = (millisecondsLeft / (60*60*1000L)) % 24;
        long mins = (millisecondsLeft / (60*1000L)) % 60;

        return String.format(Locale.US, "%dd %02dh %02dm", days, hours, mins);
    }

    public static ArrayList<MonthRecord> history(){
        ensureLoaded();
        return new ArrayList<>( history );
    }

    public static MonthRecord historyRecord( String monthKey ){
        ensureLoaded();
        for (MonthRecord r : history){
            if (r.monthKey.equals( monthKey )) return r;
        }
        return null;
    }



    private static final String FILE       = "battlepass_new.dat";
    private static final String TOTAL_XP   = "battlepass_xp";
    private static final String CLAIMED    = "battlepass_claimed";
    private static final String REPEATED   = "battlepass_repeated";
    private static final String MONTH_KEY  = "battlepass_month";
    private static final String HISTORY_N  = "battlepass_history_count";
    private static final String HISTORY_I  = "battlepass_history_";
    private static final String PREMIUM = "battlepass_premium";
    private static final String PREMIUM_CLAIMED = "battlepass_premium_claimed";
    private static final String REWARD_TIERS         = "battlepass_reward_tiers";
    private static final String REWARD_PREFIX        = "battlepass_reward_";
    private static final String PREMIUM_REWARD_TIERS  = "battlepass_premium_reward_tiers";
    private static final String PREMIUM_REWARD_PREFIX = "battlepass_premium_reward_";

    public static void saveGlobal(){
        try {
            Bundle bundle = new Bundle();
            bundle.put( TOTAL_XP, totalXP );
            int[] claimedArr = new int[claimedTiers.size()];
            for (int i = 0; i < claimedArr.length; i++){
                claimedArr[i] = claimedTiers.get(i);
            }
            bundle.put( CLAIMED, claimedArr );
            bundle.put( REPEATED, repeatableTiersClaimed );
            bundle.put( MONTH_KEY, monthKey );
            bundle.put( HISTORY_N, history.size() );
            bundle.put( PREMIUM, premium );
            for (int i = 0; i < history.size(); i++){
                bundle.put( HISTORY_I + i, history.get(i).store() );
            }
            int[] premiumClaimedArr = new int[premiumClaimedTiers.size()];
            for (int i = 0; i < premiumClaimedArr.length; i++){
                premiumClaimedArr[i] = premiumClaimedTiers.get(i);
            }
            bundle.put( PREMIUM_CLAIMED, premiumClaimedArr );
            HashMap<Integer, Item> rewards = BattlePassTiers.rewardSnapshot();
            int[] rewardTiers = new int[rewards.size()];
            int ri = 0;
            for (int t : rewards.keySet()) rewardTiers[ri++] = t;
            bundle.put( REWARD_TIERS, rewardTiers );
            for (int t : rewardTiers) bundle.put( REWARD_PREFIX + t, rewards.get(t) );

            HashMap<Integer, Item> premiumRewards = BattlePassTiers.premiumRewardSnapshot();
            int[] premiumRewardTiers = new int[premiumRewards.size()];
            int pi = 0;
            for (int t : premiumRewards.keySet()) premiumRewardTiers[pi++] = t;
            bundle.put( PREMIUM_REWARD_TIERS, premiumRewardTiers );
            for (int t : premiumRewardTiers) bundle.put( PREMIUM_REWARD_PREFIX + t, premiumRewards.get(t) );
            FileUtils.bundleToFile( FILE, bundle );
        } catch (IOException e) {
            ShatteredPixelDungeon.reportException( e );
        }
    }

    public static void loadGlobal(){
        try {
            Bundle bundle = FileUtils.bundleFromFile( FILE );
            totalXP = bundle.contains( TOTAL_XP ) ? bundle.getInt( TOTAL_XP ) : 0;
            claimedTiers = new ArrayList<>();
            if (bundle.contains( CLAIMED )){
                for (int t : bundle.getIntArray( CLAIMED )){
                    claimedTiers.add( t );
                }
            }
            repeatableTiersClaimed = bundle.contains( REPEATED ) ? bundle.getInt( REPEATED ) : 0;

            monthKey = bundle.contains( MONTH_KEY ) ? bundle.getString( MONTH_KEY ) : null;

            history = new ArrayList<>();
            if (bundle.contains( HISTORY_N )){
                int n = bundle.getInt( HISTORY_N );
                for (int i = 0; i < n; i++){
                    history.add( MonthRecord.restore( bundle.getBundle( HISTORY_I + i ) ) );
                }
            }
            premium = bundle.contains( PREMIUM ) && bundle.getBoolean( PREMIUM );
            premiumClaimedTiers = new ArrayList<>();
            if (bundle.contains( PREMIUM_CLAIMED )){
                for (int t : bundle.getIntArray( PREMIUM_CLAIMED )){
                    premiumClaimedTiers.add( t );
                }
            }
            if (bundle.contains( REWARD_TIERS )){
                HashMap<Integer, Item> rewards = new HashMap<>();
                for (int t : bundle.getIntArray( REWARD_TIERS )){
                    rewards.put( t, (Item) bundle.get( REWARD_PREFIX + t ) );
                }
                BattlePassTiers.restoreRewards( rewards );
            }
            if (bundle.contains( PREMIUM_REWARD_TIERS )){
                HashMap<Integer, Item> premiumRewards = new HashMap<>();
                for (int t : bundle.getIntArray( PREMIUM_REWARD_TIERS )){
                    premiumRewards.put( t, (Item) bundle.get( PREMIUM_REWARD_PREFIX + t ) );
                }
                BattlePassTiers.restorePremiumRewards( premiumRewards );
            }
        } catch (IOException e) {
            totalXP = 0;
            claimedTiers = new ArrayList<>();
            repeatableTiersClaimed = 0;
            monthKey = null;
            history = new ArrayList<>();
            premium = false;
            premiumClaimedTiers = new ArrayList<>();
        }
    }
}