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

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.watabou.utils.Bundle;
import com.watabou.utils.FileUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class BattlePass {

    public static final int XP_PER_DEPTH = 50;

    //XP granted per enemy slain
    public static final int XP_PER_KILL = 2;

    //1 XP per this many gold collected (rounded down)
    public static final int GOLD_PER_XP = 10;

    public static final int[] TIER_XP = buildTierCosts();
    private static int[] buildTierCosts(){
        int[] costs = new int[100];
        for (int i = 0; i < costs.length; i++){
            costs[i] = 100 + i*30;
        }
        return costs;
    }

    public static final int REPEATABLE_TIER    = TIER_XP.length + 1; //101
    public static final int REPEATABLE_TIER_XP = 100 + TIER_XP.length*30; //3100, continues the same cost curve

    public static int totalXP;
    public static ArrayList<Integer> claimedTiers = new ArrayList<>();

    //how many times the repeatable tier has been claimed this month
    public static int repeatableTiersClaimed;

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
        if (monthKey.equals( now )) return;

        history.add( 0, new MonthRecord( monthKey, totalXP, new ArrayList<>( claimedTiers ), repeatableTiersClaimed ) );
        while (history.size() > MAX_HISTORY_MONTHS){
            history.remove( history.size()-1 );
        }

        monthKey = now;
        totalXP = 0;
        claimedTiers = new ArrayList<>();
        repeatableTiersClaimed = 0;
        saveGlobal();
    }

    public static class MonthRecord {

        public final String monthKey;
        public final int finalXP;
        public final ArrayList<Integer> claimedTiers;
        public final int repeatableTiersClaimed;

        MonthRecord( String monthKey, int finalXP, ArrayList<Integer> claimedTiers, int repeatableTiersClaimed ){
            this.monthKey = monthKey;
            this.finalXP = finalXP;
            this.claimedTiers = claimedTiers;
            this.repeatableTiersClaimed = repeatableTiersClaimed;
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

        static MonthRecord restore( Bundle b ){
            String key = b.getString( M_KEY );
            int xp = b.getInt( M_XP );
            ArrayList<Integer> claimed = new ArrayList<>();
            for (int t : b.getIntArray( M_CLAIMED )) claimed.add( t );
            int repeated = b.contains( M_REPEATED ) ? b.getInt( M_REPEATED ) : 0;
            return new MonthRecord( key, xp, claimed, repeated );
        }

        Bundle store(){
            Bundle b = new Bundle();
            b.put( M_KEY, monthKey );
            b.put( M_XP, finalXP );
            int[] arr = new int[claimedTiers.size()];
            for (int i = 0; i < arr.length; i++) arr[i] = claimedTiers.get(i);
            b.put( M_CLAIMED, arr );
            b.put( M_REPEATED, repeatableTiersClaimed );
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

    public static Item claim( int tier ){
        ensureLoaded();
        if (!isClaimable( tier )) return null;

        if (tier == REPEATABLE_TIER){
            repeatableTiersClaimed++;
        } else {
            claimedTiers.add( tier );
        }

        Item result = null;
        if (BattlePassTiers.isItemTier( tier )){
            Item reward = BattlePassTiers.rewardFor( tier );
            if (reward != null && Dungeon.hero != null){
                reward.collect();
            }
            result = reward;
        } else {
            Dungeon.gold += BattlePassTiers.goldFor( tier );
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



    private static final String FILE       = "battlepass.dat";
    private static final String TOTAL_XP   = "battlepass_xp";
    private static final String CLAIMED    = "battlepass_claimed";
    private static final String REPEATED   = "battlepass_repeated";
    private static final String MONTH_KEY  = "battlepass_month";
    private static final String HISTORY_N  = "battlepass_history_count";
    private static final String HISTORY_I  = "battlepass_history_";

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
            for (int i = 0; i < history.size(); i++){
                bundle.put( HISTORY_I + i, history.get(i).store() );
            }
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
        } catch (IOException e) {
            totalXP = 0;
            claimedTiers = new ArrayList<>();
            repeatableTiersClaimed = 0;
            monthKey = null;
            history = new ArrayList<>();
        }
    }
}