package com.shatteredpixel.shatteredpixeldungeon;


import java.math.BigInteger;
import com.shatteredpixel.shatteredpixeldungeon.utils.Big;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Collections;

public class Tasks {

    public enum Type {
        REACH_DEPTH, SLAY_ENEMIES, DRINK_POTION, READ_SCROLL, USE_WAND,
        PICK_UP_GOLD, OPEN_CHEST, EQUIP_ARMOR, REST_TO_FULL, IDENTIFY_ITEM
    }

    public static class Task {
        public final Type type;
        public final BigInteger target;
        public BigInteger progress = BigInteger.ZERO;
        public boolean completed;

        Task( Type type, BigInteger target ){
            this.type = type;
            this.target = target;
        }

        public String description(){
            switch (type){
                case REACH_DEPTH:   return Messages.get( Tasks.class, "task_reach_depth", target );
                case SLAY_ENEMIES:  return Messages.get( Tasks.class, "task_slay_enemies", target );
                case DRINK_POTION:  return Messages.get( Tasks.class, "task_drink_potion" );
                case READ_SCROLL:   return Messages.get( Tasks.class, "task_read_scroll" );
                case USE_WAND:      return Messages.get( Tasks.class, "task_use_wand" );
                case PICK_UP_GOLD:  return Messages.get( Tasks.class, "task_pick_up_gold", target );
                case OPEN_CHEST:    return Messages.get( Tasks.class, "task_open_chest" );
                case EQUIP_ARMOR:   return Messages.get( Tasks.class, "task_equip_armor" );
                case REST_TO_FULL:  return Messages.get( Tasks.class, "task_rest_to_full" );
                case IDENTIFY_ITEM: return Messages.get( Tasks.class, "task_identify_item" );
                default: return "";
            }
        }
    }

    private static final int TASK_COUNT = 6;
    private static final int GOLD_REWARD = 500;
    private static final int XP_REWARD = 1000;

    public static ArrayList<Task> tasks = new ArrayList<>();
    private static boolean allClaimed = false;

    public static void onRunStart(){
        tasks = rollTasks();
        allClaimed = false;
    }

    private static ArrayList<Task> rollTasks(){
        ArrayList<Type> pool = new ArrayList<>();
        Collections.addAll( pool, Type.values() );
        Collections.shuffle( pool );

        ArrayList<Task> result = new ArrayList<>();
        for (int i = 0; i < Math.min( TASK_COUNT, pool.size() ); i++){
            Type type = pool.get(i);
            int target;
            switch (type){
                case DRINK_POTION:      target = 1 + (int)(Math.random()*6); break;
                case READ_SCROLL:       target = 1 + (int)(Math.random()*8); break;
                case USE_WAND:          target = 10 + (int)(Math.random()*2); break;
                case OPEN_CHEST:        target = 2 + (int)(Math.random()*2); break;
                case IDENTIFY_ITEM:     target = 4 + (int)(Math.random()*2); break;
                case REACH_DEPTH:       target = Math.min(25, Random.Int(5, 24)); break;
                case SLAY_ENEMIES:      target = 5 + (int)(Math.random()*6); break;
                case PICK_UP_GOLD:      target = 50 + (int)(Math.random()*100); break;
                default: target = 1; break;
            }
            result.add( new Task( type, BigInteger.valueOf(target) ) );
        }
        return result;
    }

    private static Task find( Type type ){
        for (Task t : tasks) if (t.type == type) return t;
        return null;
    }

    private static void progress( Type type, BigInteger amount ){
        Task t = find( type );
        if (t == null || t.completed) return;
        t.progress = t.target.min(t.progress.add(amount));
        if ((t.progress.compareTo(t.target) >= 0)){
            t.completed = true;
            GLog.p( Messages.get( Tasks.class, "task_done", t.description() ) );
            checkAllComplete();
        }
    }

    private static void checkAllComplete(){
        if (allClaimed || tasks.isEmpty()) return;
        for (Task t : tasks) if (!t.completed) return;

        allClaimed = true;
        Dungeon.gold = Dungeon.gold.add(BigInteger.valueOf(GOLD_REWARD));
        BattlePass.totalXP += XP_REWARD;
        BattlePass.saveGlobal();
        GLog.p( Messages.get( Tasks.class, "all_done", GOLD_REWARD, XP_REWARD ) );
    }

    public static void onDepthReached( int depth ){
        Task t = find( Type.REACH_DEPTH );
        if (t != null && !t.completed && (BigInteger.valueOf(depth).compareTo(t.target) >= 0)){
            t.progress = t.target;
            t.completed = true;
            GLog.p( Messages.get( Tasks.class, "task_done", t.description() ) );
            checkAllComplete();
        }
    }

    public static void onEnemySlain(){ progress( Type.SLAY_ENEMIES, BigInteger.ONE ); }
    public static void onPotionDrunk(){ progress( Type.DRINK_POTION, BigInteger.ONE ); }
    public static void onScrollRead(){ progress( Type.READ_SCROLL, BigInteger.ONE ); }
    public static void onWandUsed(){ progress( Type.USE_WAND, BigInteger.ONE ); }
    public static void onGoldCollected( BigInteger amount ){ progress( Type.PICK_UP_GOLD, amount ); }
    public static void onChestOpened(){ progress( Type.OPEN_CHEST, BigInteger.ONE ); }
    public static void onArmorEquipped(){ progress( Type.EQUIP_ARMOR, BigInteger.ONE ); }
    public static void onRestedToFull(){ progress( Type.REST_TO_FULL, BigInteger.ONE ); }
    public static void onItemIdentified(){ progress( Type.IDENTIFY_ITEM, BigInteger.ONE ); }

    private static final String TASK_TYPES      = "task_types";
    private static final String TASK_TARGETS    = "task_targets";
    private static final String TASK_PROGRESS   = "task_progress";
    private static final String TASK_COMPLETED  = "task_completed";
    private static final String ALL_CLAIMED     = "all_claimed";

    public static void storeInBundle( Bundle bundle ){
        String[] types = new String[tasks.size()];
        BigInteger[] targets = Big.newArray(tasks.size());
        BigInteger[] progress = Big.newArray(tasks.size());
        boolean[] completed = new boolean[tasks.size()];

        for (int i = 0; i < tasks.size(); i++){
            Task t = tasks.get(i);
            types[i] = t.type.name();
            targets[i] = t.target;
            progress[i] = t.progress;
            completed[i] = t.completed;
        }

        bundle.put( TASK_TYPES, types );
        Big.putArray(bundle, TASK_TARGETS, targets);
        Big.putArray(bundle, TASK_PROGRESS, progress);
        bundle.put( TASK_COMPLETED, completed );
        bundle.put( ALL_CLAIMED, allClaimed );
    }

    public static void restoreFromBundle( Bundle bundle ){
        tasks = new ArrayList<>();

        if (bundle.contains( TASK_TYPES )){
            String[] types = bundle.getStringArray( TASK_TYPES );
            BigInteger[] targets = Big.getBigArray(bundle, TASK_TARGETS);
            BigInteger[] progress = Big.getBigArray(bundle, TASK_PROGRESS);
            boolean[] completed = bundle.getBooleanArray( TASK_COMPLETED );

            for (int i = 0; i < types.length; i++){
                Task t = new Task( Type.valueOf( types[i] ), targets[i] );
                t.progress = progress[i];
                t.completed = completed[i];
                tasks.add( t );
            }
        }

        allClaimed = bundle.contains( ALL_CLAIMED ) && bundle.getBoolean( ALL_CLAIMED );
    }
}