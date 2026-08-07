package com.shatteredpixel.shatteredpixeldungeon;

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
        public final long target;
        public long progress;
        public boolean completed;

        Task( Type type, long target ){
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
            result.add( new Task( type, target ) );
        }
        return result;
    }

    private static Task find( Type type ){
        for (Task t : tasks) if (t.type == type) return t;
        return null;
    }

    private static void progress( Type type, long amount ){
        Task t = find( type );
        if (t == null || t.completed) return;
        t.progress = Math.min( t.target, t.progress + amount );
        if (t.progress >= t.target){
            t.completed = true;
            GLog.p( Messages.get( Tasks.class, "task_done", t.description() ) );
            checkAllComplete();
        }
    }

    private static void checkAllComplete(){
        if (allClaimed || tasks.isEmpty()) return;
        for (Task t : tasks) if (!t.completed) return;

        allClaimed = true;
        Dungeon.gold += GOLD_REWARD;
        BattlePass.totalXP += XP_REWARD;
        BattlePass.saveGlobal();
        GLog.p( Messages.get( Tasks.class, "all_done", GOLD_REWARD, XP_REWARD ) );
    }

    public static void onDepthReached( int depth ){
        Task t = find( Type.REACH_DEPTH );
        if (t != null && !t.completed && depth >= t.target){
            t.progress = t.target;
            t.completed = true;
            GLog.p( Messages.get( Tasks.class, "task_done", t.description() ) );
            checkAllComplete();
        }
    }

    public static void onEnemySlain(){ progress( Type.SLAY_ENEMIES, 1 ); }
    public static void onPotionDrunk(){ progress( Type.DRINK_POTION, 1 ); }
    public static void onScrollRead(){ progress( Type.READ_SCROLL, 1 ); }
    public static void onWandUsed(){ progress( Type.USE_WAND, 1 ); }
    public static void onGoldCollected( long amount ){ progress( Type.PICK_UP_GOLD, amount ); }
    public static void onChestOpened(){ progress( Type.OPEN_CHEST, 1 ); }
    public static void onArmorEquipped(){ progress( Type.EQUIP_ARMOR, 1 ); }
    public static void onRestedToFull(){ progress( Type.REST_TO_FULL, 1 ); }
    public static void onItemIdentified(){ progress( Type.IDENTIFY_ITEM, 1 ); }

    private static final String TASK_TYPES      = "task_types";
    private static final String TASK_TARGETS    = "task_targets";
    private static final String TASK_PROGRESS   = "task_progress";
    private static final String TASK_COMPLETED  = "task_completed";
    private static final String ALL_CLAIMED     = "all_claimed";

    public static void storeInBundle( Bundle bundle ){
        String[] types = new String[tasks.size()];
        long[] targets = new long[tasks.size()];
        long[] progress = new long[tasks.size()];
        boolean[] completed = new boolean[tasks.size()];

        for (int i = 0; i < tasks.size(); i++){
            Task t = tasks.get(i);
            types[i] = t.type.name();
            targets[i] = t.target;
            progress[i] = t.progress;
            completed[i] = t.completed;
        }

        bundle.put( TASK_TYPES, types );
        bundle.put( TASK_TARGETS, targets );
        bundle.put( TASK_PROGRESS, progress );
        bundle.put( TASK_COMPLETED, completed );
        bundle.put( ALL_CLAIMED, allClaimed );
    }

    public static void restoreFromBundle( Bundle bundle ){
        tasks = new ArrayList<>();

        if (bundle.contains( TASK_TYPES )){
            String[] types = bundle.getStringArray( TASK_TYPES );
            long[] targets = bundle.getLongArray( TASK_TARGETS );
            long[] progress = bundle.getLongArray( TASK_PROGRESS );
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