package com.shatteredpixel.shatteredpixeldungeon;


import java.math.BigInteger;
import com.shatteredpixel.shatteredpixeldungeon.utils.Big;
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
        public final BigInteger target;
        public BigInteger progress = BigInteger.ZERO;
        public boolean completed;
        public boolean claimed;

        Task( Type type, BigInteger target ){
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
            BigInteger target;
            switch (type){
                case REACH_DEPTH:    target = BigInteger.valueOf(25); break;
                case SLAY_ENEMIES:   target = BigInteger.valueOf(100).add(Big.of((Math.random()*500))); break;
                case COMPLETE_RUNS:  target = BigInteger.valueOf(3).add(Big.of((Math.random()*10))); break;
                case DRINK_POTIONS:  target = BigInteger.valueOf(15).add(Big.of((Math.random()*50))); break;
                case READ_SCROLLS:   target = BigInteger.valueOf(15).add(Big.of((Math.random()*100))); break;
                case COLLECT_GOLD:   target = BigInteger.valueOf(200000).add(Big.of((Math.random()*300000))); break;
                case DIE_TIMES:      target = BigInteger.valueOf(2).add(Big.of((Math.random()*3))); break;
                case IDENTIFY_ITEMS: target = BigInteger.valueOf(30).add(Big.of((Math.random()*70))); break;
                default: target = BigInteger.ONE; break;
            }
            tasks.add( new Task( type, target ) );
        }
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
            GLog.p( Messages.get( SeasonalTasks.class, "task_ready", t.description() ) );
        }
    }

    public static boolean claim( Task t ){
        if (t == null || !t.completed || t.claimed) return false;
        t.claimed = true;
        BattlePass.totalXP += XP_PER_TASK;
        Dungeon.gold = Dungeon.gold.add(BigInteger.valueOf(GOLD_PER_TASK));
        checkAllClaimed();
        BattlePass.saveGlobal();
        return true;
    }

    private static void checkAllClaimed(){
        if (allBonusClaimed || tasks.isEmpty()) return;
        for (Task t : tasks) if (!t.claimed) return;

        allBonusClaimed = true;
        BattlePass.totalXP += ALL_DONE_XP_BONUS;
        Dungeon.gold = Dungeon.gold.add(BigInteger.valueOf(ALL_DONE_GOLD_BONUS));
        GLog.p( Messages.get( SeasonalTasks.class, "all_done", ALL_DONE_XP_BONUS, ALL_DONE_GOLD_BONUS ) );
    }


    public static void onDepthReached( int depth ){
        Task t = find( Type.REACH_DEPTH );
        if (t != null && !t.completed && (BigInteger.valueOf(depth).compareTo(t.target) >= 0)){
            t.progress = t.target;
            t.completed = true;
            GLog.p( Messages.get( SeasonalTasks.class, "task_ready", t.description() ) );
        }
    }

    public static void onRunCompleted(){ progress( Type.COMPLETE_RUNS, BigInteger.ONE ); }
    public static void onEnemySlain(){ progress( Type.SLAY_ENEMIES, BigInteger.ONE ); }
    public static void onPotionDrunk(){ progress( Type.DRINK_POTIONS, BigInteger.ONE ); }
    public static void onScrollRead(){ progress( Type.READ_SCROLLS, BigInteger.ONE ); }
    public static void onGoldCollected( BigInteger amount ){ progress( Type.COLLECT_GOLD, amount ); }
    public static void onItemIdentified(){ progress( Type.IDENTIFY_ITEMS, BigInteger.ONE ); }

    public static void onHeroDied(){
        progress( Type.DIE_TIMES, BigInteger.ONE );
        onRunCompleted();
    }


    private static final String TASK_TYPES     = "seasontask_types";
    private static final String TASK_TARGETS   = "seasontask_targets";
    private static final String TASK_PROGRESS  = "seasontask_progress";
    private static final String TASK_COMPLETED = "seasontask_completed";
    private static final String TASK_CLAIMED   = "seasontask_claimed";
    private static final String ALL_CLAIMED    = "seasontask_all_claimed";

    public static void storeInBundle( Bundle bundle ){
        String[] types = new String[tasks.size()];
        BigInteger[] targets = Big.newArray(tasks.size());
        BigInteger[] progress = Big.newArray(tasks.size());
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
        Big.putArray(bundle, TASK_TARGETS, targets);
        Big.putArray(bundle, TASK_PROGRESS, progress);
        bundle.put( TASK_COMPLETED, completed );
        bundle.put( TASK_CLAIMED, claimed );
        bundle.put( ALL_CLAIMED, allBonusClaimed );
    }

    public static void restoreFromBundle( Bundle bundle ){
        tasks = new ArrayList<>();
        if (bundle.contains( TASK_TYPES )){
            String[] types = bundle.getStringArray( TASK_TYPES );
            if (types == null) types = new String[0];
            BigInteger[] targets = Big.getBigArray(bundle, TASK_TARGETS);
            BigInteger[] progress = Big.getBigArray(bundle, TASK_PROGRESS);
            boolean[] completed = bundle.getBooleanArray( TASK_COMPLETED );
            boolean[] claimed = bundle.contains( TASK_CLAIMED )
                    ? bundle.getBooleanArray( TASK_CLAIMED ) : new boolean[types.length];

            for (int i = 0; i < types.length; i++){
                Type type;
                try {
                    type = Type.valueOf( types[i] );
                } catch (IllegalArgumentException e){
                    continue;
                }
                Task t = new Task( type, i < targets.length ? targets[i] : BigInteger.ONE );
                t.progress = i < progress.length ? progress[i] : BigInteger.ZERO;
                t.completed = i < completed.length && completed[i];
                t.claimed = i < claimed.length && claimed[i];
                tasks.add( t );
            }
        }
        allBonusClaimed = bundle.contains( ALL_CLAIMED ) && bundle.getBoolean( ALL_CLAIMED );
    }
}