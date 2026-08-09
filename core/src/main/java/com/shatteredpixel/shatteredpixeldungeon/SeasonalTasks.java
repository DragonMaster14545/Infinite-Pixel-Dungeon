package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.Collections;

public class SeasonalTasks {

    public enum Type {
        REACH_DEPTH, SLAY_ENEMIES, COMPLETE_RUNS, DRINK_POTIONS, READ_SCROLLS,
        COLLECT_GOLD, DIE_TIMES, IDENTIFY_ITEMS
    }

    public static class Task {
        public final Type type;
        public final long target;
        public long progress;
        public boolean completed;
        public boolean claimed;

        Task( Type type, long target ){
            this.type = type;
            this.target = target;
        }

        public String description(){
            switch (type){
                case REACH_DEPTH:    return Messages.get( SeasonalTasks.class, "task_reach_depth", target );
                case SLAY_ENEMIES:   return Messages.get( SeasonalTasks.class, "task_slay_enemies", target );
                case COMPLETE_RUNS:  return Messages.get( SeasonalTasks.class, "task_complete_runs", target );
                case DRINK_POTIONS:  return Messages.get( SeasonalTasks.class, "task_drink_potions", target );
                case READ_SCROLLS:   return Messages.get( SeasonalTasks.class, "task_read_scrolls", target );
                case COLLECT_GOLD:   return Messages.get( SeasonalTasks.class, "task_collect_gold", target );
                case DIE_TIMES:      return Messages.get( SeasonalTasks.class, "task_die_times", target );
                case IDENTIFY_ITEMS: return Messages.get( SeasonalTasks.class, "task_identify_items", target );
                default: return "";
            }
        }
    }

    private static final int TASK_COUNT = 5;
    private static final int XP_PER_TASK = 500;
    private static final int GOLD_PER_TASK = 250;
    private static final int ALL_DONE_XP_BONUS = 5000;
    private static final int ALL_DONE_GOLD_BONUS = 2500;

    public static ArrayList<Task> tasks = new ArrayList<>();
    private static boolean allBonusClaimed = false;

    public static void rollForNewSeason(){
        tasks = new ArrayList<>();
        allBonusClaimed = false;

        ArrayList<Type> pool = new ArrayList<>();
        Collections.addAll( pool, Type.values() );
        Collections.shuffle( pool );

        for (int i = 0; i < Math.min( TASK_COUNT, pool.size() ); i++){
            Type type = pool.get(i);
            long target;
            switch (type){
                case REACH_DEPTH:    target = 25; break;
                case SLAY_ENEMIES:   target = 100 + (long)(Math.random()*500); break;
                case COMPLETE_RUNS:  target = 3 + (long)(Math.random()*10); break;
                case DRINK_POTIONS:  target = 15 + (long)(Math.random()*50); break;
                case READ_SCROLLS:   target = 15 + (long)(Math.random()*100); break;
                case COLLECT_GOLD:   target = 200000 + (long)(Math.random()*300000); break;
                case DIE_TIMES:      target = 2 + (long)(Math.random()*3); break;
                case IDENTIFY_ITEMS: target = 30 + (long)(Math.random()*70); break;
                default: target = 1; break;
            }
            tasks.add( new Task( type, target ) );
        }
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
            GLog.p( Messages.get( SeasonalTasks.class, "task_ready", t.description() ) );
        }
    }

    public static boolean claim( Task t ){
        if (t == null || !t.completed || t.claimed) return false;
        t.claimed = true;
        BattlePass.totalXP += XP_PER_TASK;
        Dungeon.gold += GOLD_PER_TASK;
        checkAllClaimed();
        BattlePass.saveGlobal();
        return true;
    }

    private static void checkAllClaimed(){
        if (allBonusClaimed || tasks.isEmpty()) return;
        for (Task t : tasks) if (!t.claimed) return;

        allBonusClaimed = true;
        BattlePass.totalXP += ALL_DONE_XP_BONUS;
        Dungeon.gold += ALL_DONE_GOLD_BONUS;
        GLog.p( Messages.get( SeasonalTasks.class, "all_done", ALL_DONE_XP_BONUS, ALL_DONE_GOLD_BONUS ) );
    }


    public static void onDepthReached( int depth ){
        Task t = find( Type.REACH_DEPTH );
        if (t != null && !t.completed && depth >= t.target){
            t.progress = t.target;
            t.completed = true;
            GLog.p( Messages.get( SeasonalTasks.class, "task_ready", t.description() ) );
        }
    }

    public static void onRunCompleted(){ progress( Type.COMPLETE_RUNS, 1 ); }
    public static void onEnemySlain(){ progress( Type.SLAY_ENEMIES, 1 ); }
    public static void onPotionDrunk(){ progress( Type.DRINK_POTIONS, 1 ); }
    public static void onScrollRead(){ progress( Type.READ_SCROLLS, 1 ); }
    public static void onGoldCollected( long amount ){ progress( Type.COLLECT_GOLD, amount ); }
    public static void onItemIdentified(){ progress( Type.IDENTIFY_ITEMS, 1 ); }

    public static void onHeroDied(){
        progress( Type.DIE_TIMES, 1 );
    }


    private static final String TASK_TYPES     = "seasontask_types";
    private static final String TASK_TARGETS   = "seasontask_targets";
    private static final String TASK_PROGRESS  = "seasontask_progress";
    private static final String TASK_COMPLETED = "seasontask_completed";
    private static final String TASK_CLAIMED   = "seasontask_claimed";
    private static final String ALL_CLAIMED    = "seasontask_all_claimed";

    public static void storeInBundle( Bundle bundle ){
        String[] types = new String[tasks.size()];
        long[] targets = new long[tasks.size()];
        long[] progress = new long[tasks.size()];
        boolean[] completed = new boolean[tasks.size()];
        boolean[] claimed = new boolean[tasks.size()];

        for (int i = 0; i < tasks.size(); i++){
            Task t = tasks.get(i);
            types[i] = t.type.name();
            targets[i] = t.target;
            progress[i] = t.progress;
            completed[i] = t.completed;
            claimed[i] = t.claimed;
        }

        bundle.put( TASK_TYPES, types );
        bundle.put( TASK_TARGETS, targets );
        bundle.put( TASK_PROGRESS, progress );
        bundle.put( TASK_COMPLETED, completed );
        bundle.put( TASK_CLAIMED, claimed );
        bundle.put( ALL_CLAIMED, allBonusClaimed );
    }

    public static void restoreFromBundle( Bundle bundle ){
        tasks = new ArrayList<>();
        if (bundle.contains( TASK_TYPES )){
            String[] types = bundle.getStringArray( TASK_TYPES );
            long[] targets = bundle.getLongArray( TASK_TARGETS );
            long[] progress = bundle.getLongArray( TASK_PROGRESS );
            boolean[] completed = bundle.getBooleanArray( TASK_COMPLETED );
            boolean[] claimed = bundle.contains( TASK_CLAIMED )
                    ? bundle.getBooleanArray( TASK_CLAIMED ) : new boolean[types.length];

            for (int i = 0; i < types.length; i++){
                Task t = new Task( Type.valueOf( types[i] ), targets[i] );
                t.progress = progress[i];
                t.completed = completed[i];
                t.claimed = claimed[i];
                tasks.add( t );
            }
        }
        allBonusClaimed = bundle.contains( ALL_CLAIMED ) && bundle.getBoolean( ALL_CLAIMED );
    }
}