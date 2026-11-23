/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2019 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.levels;


import java.math.BigInteger;
import com.shatteredpixel.shatteredpixeldungeon.utils.Big;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Bones;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CounterBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Overload;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.RageShield;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Stamina;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.MobSpawner;
import com.shatteredpixel.shatteredpixeldungeon.items.Ankh;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.emblem.LaserisedEmblem;
import com.shatteredpixel.shatteredpixeldungeon.items.emblem.SummonerEmblem;
import com.shatteredpixel.shatteredpixeldungeon.items.emblem.TrihitEmblem;
import com.shatteredpixel.shatteredpixeldungeon.items.food.SmallRation;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfAquaticRejuvenation;
import com.shatteredpixel.shatteredpixeldungeon.items.treasurebags.BiggerGambleBag;
import com.shatteredpixel.shatteredpixeldungeon.items.treasurebags.GambleBag;
import com.shatteredpixel.shatteredpixeldungeon.items.treasurebags.QualityBag;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.ArenaShopLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.SummoningTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTileSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Group;
import com.watabou.noosa.audio.Music;
import com.watabou.utils.Bundle;
import com.watabou.utils.Point;
import com.watabou.utils.Random;
import com.watabou.utils.Rect;

import java.util.ArrayList;

public class WaveArenaLevel extends Level {

    {
        color1 = 0x534f3e;
        color2 = 0xb9d661;
    }

    @Override
    public void playLevelMusic() {
        Music.INSTANCE.play(Assets.Music.ARENA, true);
    }

    private static final int WIDTH = 32;
    private static final int HEIGHT = 32;

    private static final int ROOM_LEFT		= WIDTH / 2 - 3;
    private static final int ROOM_RIGHT		= WIDTH / 2 + 1;
    private static final int ROOM_TOP		= HEIGHT / 2 - 2;
    private static final int ROOM_BOTTOM	= HEIGHT / 2 + 2;

    private int arenaDoor;
    private boolean enteredArena = false;
    private boolean keyDropped = false;
    private ArenaShopLevel shop;
    private int waveNumber = 0;
    private boolean waveActive = false;
    private int waveTimer = 0;
    private int announcedRemaining = -1;

    @Override
    public String tilesTex() {
        return Assets.Environment.TILES_ARENA;
    }

    @Override
    public String waterTex() {
        return Assets.Environment.WATER_ARENA;
    }

    private static final String DOOR	= "door";
    private static final String ENTERED	= "entered";
    private static final String DROPPED	= "droppped";
    private static final String SHOP = "shop";
    private static final String WAVE_NUM = "wave_number";
    private static final String WAVE_ACTIVE = "wave_active";
    private static final String WAVE_TIMER = "wave_timer";

    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle( bundle );
        bundle.put( DOOR, arenaDoor );
        bundle.put( ENTERED, enteredArena );
        bundle.put( DROPPED, keyDropped );
        bundle.put( SHOP, shop);
        bundle.put( WAVE_NUM, waveNumber );
        bundle.put( WAVE_ACTIVE, waveActive );
        bundle.put( WAVE_TIMER, waveTimer );
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle( bundle );
        arenaDoor = bundle.getInt( DOOR );
        enteredArena = bundle.getBoolean( ENTERED );
        keyDropped = bundle.getBoolean( DROPPED );
        shop = (ArenaShopLevel) bundle.get(SHOP);
        waveNumber = bundle.contains( WAVE_NUM ) ? bundle.getInt( WAVE_NUM ) : 0;
        waveActive = bundle.contains( WAVE_ACTIVE ) && bundle.getBoolean( WAVE_ACTIVE );
        waveTimer = bundle.contains( WAVE_TIMER ) ? bundle.getInt( WAVE_TIMER ) : 0;
    }

    @Override
    protected boolean build() {

        setSize(WIDTH, HEIGHT);

        Rect space = new Rect();

        space.set(
                Random.IntRange(2, 6),
                Random.IntRange(2, 6),
                Random.IntRange(width-6, width-2),
                Random.IntRange(height-6, height-2)
        );

        Painter.fillEllipse( this, space, Terrain.EMPTY );

        exit = space.left + space.width()/2 + (space.top - 1) * width();

        map[exit] = Terrain.WALL_DECO;

        Painter.fill( this, ROOM_LEFT - 1, ROOM_TOP - 1,
                ROOM_RIGHT - ROOM_LEFT + 3, ROOM_BOTTOM - ROOM_TOP + 3, Terrain.WALL );
        Painter.fill( this, ROOM_LEFT, ROOM_TOP + 1,
                ROOM_RIGHT - ROOM_LEFT + 1, ROOM_BOTTOM - ROOM_TOP, Terrain.EMPTY );

        Painter.fill( this, ROOM_LEFT, ROOM_TOP,
                ROOM_RIGHT - ROOM_LEFT + 1, 1, Terrain.EMPTY_DECO );
        shop = new ArenaShopLevel();
        shop.set(ROOM_LEFT-1, ROOM_TOP-1, ROOM_RIGHT+1, ROOM_BOTTOM+1);

        arenaDoor = Random.Int( ROOM_LEFT, ROOM_RIGHT ) + (ROOM_BOTTOM + 1) * width();
        map[arenaDoor] = Terrain.DOOR;

        int entrance = Random.Int( ROOM_LEFT + 1, ROOM_RIGHT - 1 ) +
                Random.Int( ROOM_TOP + 1, ROOM_BOTTOM - 1 ) * width();
        transitions.add(new LevelTransition(this, entrance, LevelTransition.Type.REGULAR_ENTRANCE));
        map[entrance] = Terrain.PEDESTAL;

        {
            generateItems();
        }

        boolean[] patch = Patch.generate( width, height, 0.30f, 6, true );
        for (int i=0; i < length(); i++) {
            if (map[i] == Terrain.EMPTY && patch[i]) {
                map[i] = Terrain.WATER;
            }
        }

        for (int i=0; i < length(); i++) {
            if (map[i] == Terrain.EMPTY && Random.Int( 6 ) == 0) {
                map[i] = Terrain.INACTIVE_TRAP;
                Trap t = new SummoningTrap().reveal();
                t.active = false;
                setTrap(t, i);
            }
        }

        for (int i=width() + 1; i < length() - width(); i++) {
            if (map[i] == Terrain.EMPTY) {
                int n = 0;
                if (map[i+1] == Terrain.WALL) {
                    n++;
                }
                if (map[i-1] == Terrain.WALL) {
                    n++;
                }
                if (map[i+width()] == Terrain.WALL) {
                    n++;
                }
                if (map[i-width()] == Terrain.WALL) {
                    n++;
                }
                if (Random.Int( 8 ) <= n) {
                    map[i] = Terrain.EMPTY_DECO;
                }
            }
        }

        for (int i=0; i < length() - width(); i++) {
            if (map[i] == Terrain.WALL
                    && DungeonTileSheet.floorTile(map[i + width()])
                    && Random.Int( 3 ) == 0) {
                map[i] = Terrain.WALL_DECO;
            }
        }


        return true;
    }

    private static final int SHOP_STOCK_CAP = 12;

    private boolean insideShop( int cell ) {
        int x = cell % width();
        int y = cell / width();
        return x >= ROOM_LEFT - 1 && x <= ROOM_RIGHT + 1 && y >= ROOM_TOP - 1 && y <= ROOM_BOTTOM + 1;
    }

    private Heap stockedHeap( Item item ) {
        for (int y = ROOM_TOP; y <= ROOM_BOTTOM; y++) {
            for (int x = ROOM_LEFT; x <= ROOM_RIGHT; x++) {
                Heap heap = heaps.get( x + y * width() );
                if (heap != null && heap.type == Heap.Type.FOR_SALE && heap.peek() != null
                        && heap.peek().getClass() == item.getClass()) {
                    return heap;
                }
            }
        }
        return null;
    }

    private int freeShopCell() {
        ArrayList<Integer> free = new ArrayList<>();
        int entrancePos = entrance();
        for (int y = ROOM_TOP; y <= ROOM_BOTTOM; y++) {
            for (int x = ROOM_LEFT; x <= ROOM_RIGHT; x++) {
                int cell = x + y * width();
                if (cell == entrancePos || cell == arenaDoor) continue;
                if (heaps.get( cell ) != null || findMob( cell ) != null) continue;
                free.add( cell );
            }
        }
        return free.isEmpty() ? -1 : Random.element( free );
    }

    public void generateItems() {

        itemsToSpawn = new ArrayList<>();
        itemsToSpawn.add(new SmallRation().quantity(BigInteger.valueOf(Random.IntRange(2, 4))));
        itemsToSpawn.add(new PotionOfHealing().quantity(BigInteger.valueOf(Random.IntRange(2, 4))));

        for (Item item : itemsToSpawn) {

            Heap existing = stockedHeap( item );
            if (existing != null) {
                Item stocked = existing.peek();
                BigInteger cap = BigInteger.valueOf( SHOP_STOCK_CAP );
                if (stocked.quantity().compareTo( cap ) < 0) {
                    stocked.quantity( stocked.quantity().add( item.quantity() ).min( cap ) );
                }
                continue;
            }

            int cell = freeShopCell();
            if (cell == -1) continue;

            drop( item, cell ).type = Heap.Type.FOR_SALE;
        }
    }

    @Override
    protected void createMobs() {
    }

    @Override
    public MobSpawner spawner() {
        return new WaveRespawner();
    }

    //marks a buff-affected char as belonging to the currently-active wave,
    //so the spawner knows when every mob from this wave has died
    public static class WaveMob extends Buff {}

    //counts up seconds spent waiting between waves; visible in the buff bar
    //like ArenaLevel's ArenaCounter, just repurposed for intermission timing
    public static class WaveCountdown extends CounterBuff {}

    @Override
    protected void createItems() {
        Random.pushGenerator(Random.Long());
        ArrayList<Item> bonesItems = Bones.get();
        if (bonesItems != null) {
            int pos;
            do {
                pos = randomRespawnCell(null);
            } while (pos == entrance());
            for (Item i : bonesItems) {
                drop(i, pos).setHauntedIfCursed().type = Heap.Type.REMAINS;
            }
        }
        Random.popGenerator();
    }

    @Override
    public String tileName( int tile ) {
        switch (tile) {
            case Terrain.GRASS:
                return Messages.get(CityLevel.class, "grass_name");
            case Terrain.HIGH_GRASS:
                return Messages.get(CityLevel.class, "high_grass_name");
            case Terrain.WATER:
                return Messages.get(CityLevel.class, "water_name");
            default:
                return super.tileName( tile );
        }
    }

    @Override
    public String tileDesc( int tile ) {
        switch (tile) {
            case Terrain.ENTRANCE:
                return Messages.get(CityLevel.class, "entrance_desc");
            case Terrain.EXIT:
                return Messages.get(CityLevel.class, "exit_desc");
            case Terrain.HIGH_GRASS:
                return Messages.get(CityLevel.class, "high_grass_desc");
            case Terrain.WALL_DECO:
                return Messages.get(CityLevel.class, "wall_deco_desc");
            case Terrain.BOOKSHELF:
                return Messages.get(CityLevel.class, "bookshelf_desc");
            default:
                return super.tileDesc( tile );
        }
    }

    @Override
    public Group addVisuals() {
        super.addVisuals();
        CityLevel.addCityVisuals(this, visuals);
        return visuals;
    }

    //--- wave logic ---

    private static final float WAVE_INTERMISSION = 8f; //seconds of downtime between waves

    private void startNextWave() {
        waveNumber++;
        waveActive = true;
        waveTimer = 0;
        announcedRemaining = -1;

        int waveSize = waveSizeFor( waveNumber );

        int spawned = 0;
        for (int i = 0; i < waveSize; i++) {
            Mob mob = createMobforWaveArena( waveNumber );
            if (mob == null) continue;

            int spawnCell = waveSpawnCell( mob );
            if (spawnCell == -1) continue;

            mob.state = mob.WANDERING;
            mob.pos = spawnCell;

            GameScene.add( mob );
            mob.beckon( Dungeon.hero.pos );
            Buff.affect( mob, WaveMob.class );
            scaleMobForWave( mob, waveNumber );

            spawned++;
        }

        if (spawned > 0) {
            GLog.w( Messages.get( WaveArenaLevel.class, "wave_incoming", waveNumber ) );
            generateItems();
        } else {
            //couldn't spawn anything this wave (no free cells etc) - don't get stuck, try again next tick
            waveActive = false;
            waveNumber--;
        }
    }

    private int waveSpawnCell( Mob mob ) {
        for (int pass = 0; pass < 2; pass++) {
            for (int i = 0; i < 80; i++) {
                int cell = Random.Int( length() );
                if (!passable[cell] || Actor.findChar( cell ) != null || insideShop( cell )) continue;
                if (Char.hasProp( mob, Char.Property.LARGE ) && !openSpace[cell]) continue;
                if (pass == 0 && (heroFOV[cell] || distance( cell, Dungeon.hero.pos ) < 6)) continue;
                return cell;
            }
        }
        return -1;
    }

    private static int waveSizeFor( int wave ) {
        return Math.min( 60, 2 + wave / 3); //grows by 1 every 3 waves
    }

    //the actual "mobs get stronger per wave" scaling
    private static void scaleMobForWave( Mob mob, int wave ) {
        int power = wave * 5;

        mob.HT = mob.HT.multiply( BigInteger.valueOf( 100 + wave * 10L ) ).divide( BigInteger.valueOf( 100 ) );
        mob.HP = mob.HT;

        Buff.affect( mob, Stamina.class, power );
        mob.aggro( Dungeon.hero );

        if (wave >= 5) {
            Buff.affect( mob, ElixirOfAquaticRejuvenation.AquaHealing.class ).set( power * 3 );
        }
        if (wave >= 10) {
            Buff.affect( mob, Overload.class, 20f + wave );
        }
        if (wave >= 15) {
            Buff.affect( mob, RageShield.class ).set( power * 2 );
        }
        if (wave >= 25 && Random.Int( 3 ) == 0) {
            ChampionEnemy.rollForChampion( mob );
        }
    }

    private static final int STRAGGLER_TURNS = 120;
    private static final int TIMEOUT_TURNS = 500;

    public static class WaveRespawner extends MobSpawner {

        {
            actPriority = BUFF_PRIO;
        }

        @Override
        protected boolean act() {
            WaveArenaLevel level = (WaveArenaLevel) Dungeon.level;

            ArrayList<Mob> remaining = new ArrayList<>();
            for (Mob mob : level.mobs.toArray(new Mob[0])) {
                if (mob.alignment == Char.Alignment.ENEMY && mob.isAlive()) {
                    remaining.add( mob );
                }
            }

            if (level.waveActive) {
                level.waveTimer++;

                if (remaining.isEmpty()) {
                    level.waveActive = false;
                    level.waveTimer = 0;
                    level.announcedRemaining = -1;
                    GLog.p( Messages.get( WaveArenaLevel.class, "wave_cleared", level.waveNumber ) );
                    PotionOfHealing.heal( Dungeon.hero );

                    BigInteger reward = new Gold().random().quantity()
                            .multiply( BigInteger.valueOf( 1 + level.waveNumber / 5 ) );
                    Dungeon.gold = Dungeon.gold.add( reward );
                    GLog.p( Messages.get( WaveArenaLevel.class, "wave_reward", Big.fmt( reward ) ) );
                } else {
                    if (remaining.size() <= 3 && remaining.size() != level.announcedRemaining) {
                        level.announcedRemaining = remaining.size();
                        GLog.i( Messages.get( WaveArenaLevel.class, "wave_remaining", remaining.size() ) );
                    }
                    if (level.waveTimer == STRAGGLER_TURNS) {
                        GLog.w( Messages.get( WaveArenaLevel.class, "wave_stragglers" ) );
                        for (Mob mob : remaining) {
                            mob.aggro( Dungeon.hero );
                            mob.beckon( Dungeon.hero.pos );
                        }
                    } else if (level.waveTimer >= TIMEOUT_TURNS) {
                        GLog.w( Messages.get( WaveArenaLevel.class, "wave_timeout" ) );
                        for (Mob mob : remaining) {
                            mob.die( Dungeon.hero );
                        }
                    }
                }
            }

            if (!level.waveActive && Dungeon.hero != null && Dungeon.hero.isAlive()) {
                float needed = WAVE_INTERMISSION + Math.min( 12, level.waveNumber / 2 );

                WaveCountdown countdown = Dungeon.hero.buff( WaveCountdown.class );
                if (countdown == null) {
                    countdown = Buff.affect( Dungeon.hero, WaveCountdown.class );
                    GLog.i( Messages.get( WaveArenaLevel.class, "wave_next", (int) needed ) );
                }

                if (countdown.count() >= needed) {
                    countdown.detach();
                    level.startNextWave();
                } else {
                    countdown.countUp( Actor.TICK );
                }
            }

            spend( Actor.TICK );
            return true;
        }
    }
}
