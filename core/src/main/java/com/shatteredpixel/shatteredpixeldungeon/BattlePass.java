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
import java.util.ArrayList;
public class BattlePass {

    public static final int XP_PER_DEPTH = 50;

    //XP granted per enemy slain
    public static final int XP_PER_KILL = 2;

    //1 XP per this many gold collected (rounded down)
    public static final int GOLD_PER_XP = 10;

    public static final int[] TIER_XP = buildTierCosts();
    private static int[] buildTierCosts(){
        int[] costs = new int[50];
        for (int i = 0; i < costs.length; i++){
            costs[i] = 100 + i*30;
        }
        return costs;
    }

    public static int totalXP;
    public static ArrayList<Integer> claimedTiers = new ArrayList<>();

    private static boolean loaded = false;

    private static void ensureLoaded(){
        if (!loaded){
            loadGlobal();
            loaded = true;
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
        int xpLeft = totalXP;
        int tier = 0;
        for (int cost : TIER_XP){
            if (xpLeft < cost) break;
            xpLeft -= cost;
            tier++;
        }
        return tier;
    }

    public static int xpIntoCurrentTier(){
        ensureLoaded();
        int xpLeft = totalXP;
        for (int cost : TIER_XP){
            if (xpLeft < cost) return xpLeft;
            xpLeft -= cost;
        }
        return 0; //every tier cleared
    }

    public static int xpForCurrentTier(){
        int tier = tiersReached();
        if (tier >= TIER_XP.length) return 0;
        return TIER_XP[tier];
    }

    public static boolean isUnlocked( int tier ){
        return tier >= 1 && tier <= tiersReached();
    }

    public static boolean isClaimed( int tier ){
        ensureLoaded();
        return claimedTiers.contains( tier );
    }

    public static boolean isClaimable( int tier ){
        return isUnlocked( tier ) && !isClaimed( tier );
    }

    public static Item claim( int tier ){
        ensureLoaded();
        if (!isClaimable( tier )) return null;

        claimedTiers.add( tier );

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


    private static final String FILE     = "battlepass.dat";
    private static final String TOTAL_XP = "battlepass_xp";
    private static final String CLAIMED  = "battlepass_claimed";

    public static void saveGlobal(){
        try {
            Bundle bundle = new Bundle();
            bundle.put( TOTAL_XP, totalXP );
            int[] claimedArr = new int[claimedTiers.size()];
            for (int i = 0; i < claimedArr.length; i++){
                claimedArr[i] = claimedTiers.get(i);
            }
            bundle.put( CLAIMED, claimedArr );
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
        } catch (IOException e) {
            totalXP = 0;
            claimedTiers = new ArrayList<>();
        }
    }
}
