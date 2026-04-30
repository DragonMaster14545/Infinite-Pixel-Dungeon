package com.shatteredpixel.shatteredpixeldungeon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.SacrificialFire;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.WaterOfAwareness;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.WaterOfHealth;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.ArmoredStatue;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.CrystalMimic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GoldenMimic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Blacksmith;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Ghost;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Imp;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Wandmaker;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Statue;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.Dewdrop;
import com.shatteredpixel.shatteredpixeldungeon.items.EnergyCrystal;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap.Type;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.journal.Guidebook;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.CrystalKey;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.GoldenKey;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.IronKey;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.CeremonialCandle;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.CorpseDust;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.Embers;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.Pickaxe;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Trinket;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.TrinketCatalyst;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.connection.ConnectionRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret.SecretRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret.SecretWellRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.MagicWellRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.SacrificeRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.SpecialRoom;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndMessage;
import com.watabou.noosa.Game;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

public class SeedFinder {
    public static final boolean CONDITION_ANY = true;
    public static final boolean CONDITION_ALL = false;

    public static class Options {
        public static int floors;
        public static boolean condition;
        public static long seed;

        public static boolean searchForDaily;
        public static boolean searchForWeekly;
        public static int DailyOffset;

        public static boolean ignoreBlacklist;
        public static boolean useChallenges;
        public static boolean[] challenges;

        public static boolean useRooms;
        public static boolean logPotions;
        public static boolean logScrolls;
        public static boolean logTrinkets;
        public static boolean logEquipment;
        public static boolean logRings;
        public static boolean logWands;
        public static boolean logArtifacts;
        public static boolean logOther;
        public static boolean checkShops;

        public static boolean trueRandom;
        public static boolean sequentialMode;
        public static long startingSeed;
        public static int infoSpacing;
        public static String spacingChar;
    }

    static final List<Class<? extends Item>> blacklist = Arrays.asList(
            Gold.class, Dewdrop.class, IronKey.class, GoldenKey.class,
            CrystalKey.class, EnergyCrystal.class, CorpseDust.class, Embers.class,
            CeremonialCandle.class, Pickaxe.class, Guidebook.class
    );

    public static List<Room> roomList;

    public static void loadConfig() {
        // pull options from SPDSettings
        Options.floors = SPDSettings.seedfinderFloors();
        Options.condition = SPDSettings.seedfinderConditionANY();

        Options.searchForDaily = false;
        Options.searchForWeekly = false;

        Options.useRooms = SPDSettings.useRooms();

        Options.logTrinkets = SPDSettings.logTrinkets();
        Options.logEquipment = SPDSettings.logEquipment();
        Options.logScrolls = SPDSettings.logScrolls();
        Options.logPotions = SPDSettings.logPotions();
        Options.logRings = SPDSettings.logRings();
        Options.logWands = SPDSettings.logWands();
        Options.logArtifacts = SPDSettings.logArtifacts();
        Options.logOther = SPDSettings.logMisc();

        Options.checkShops = SPDSettings.checkShops();

        Options.ignoreBlacklist = SPDSettings.ignoreBlacklist();
        Options.challenges = SPDSettings.challenges();

        // defaults, only adjustable in CLI seedfinder
        Options.useChallenges = true;
        Options.trueRandom = false;
        Options.sequentialMode = false;
        Options.startingSeed = 0;
        Options.infoSpacing = 1;
        Options.spacingChar = " ";
    }

    private static ArrayList<Heap> getMobDrops(Level l) {
        ArrayList<Heap> heaps = new ArrayList<>();

        for (Mob m : l.mobs) {
            if (m instanceof Statue && Options.logEquipment) {
                Heap h = new Heap();
                h.items = new LinkedList<>();
                h.items.add(((Statue) m).weapon.identify(false));
                if (m instanceof ArmoredStatue) {
                    h.items.add(((ArmoredStatue) m).armor().identify(false));
                }
                h.type = Type.STATUE;
                heaps.add(h);
            } else if (m instanceof Mimic) {
                Heap h = new Heap();
                h.items = new LinkedList<>();

                for (Item item : ((Mimic) m).items) {
                    h.items.add(item.identify(false));
                }

                if (m instanceof GoldenMimic) {
                    h.type = Type.GOLDEN_MIMIC;
                } else if (m instanceof CrystalMimic) {
                    h.type = Type.CRYSTAL_MIMIC;
                } else {
                    h.type = Type.MIMIC;
                }
                heaps.add(h);
            }
        }

        return heaps;
    }

    public static class SeedfinderLogResult {
        public String[] main;
        public String[] rooms;
    }

    public static class SeedLog {
        public String seed;

        public int maxDepth;

        public LinkedList<Item> rolledTrinkets;

        public List<HashSet<ItemLog>> items;
        public List<List<Item>> forSale;
        public List<HashMap<Room, String>> roomList;
        public List<Level.Feeling> feelings;

        public int ghostDepth = -1;
        public Weapon ghostWeapon;
        public Armor ghostArmor;

        public int wandmakerDepth = -1;
        public int wandmakerType = -1;
        public Wand wandmakerWand1;
        public Wand wandmakerWand2;

        public int blacksmithDepth = -1;
        public int blacksmithType = -1;
        public ArrayList<Item> blacksmithSmithRewards;

        public int impDepth = -1;
        public Boolean impType;
        public Ring impReward;


        public SeedLog(String seed, int maxDepth) {
            this.seed = seed;
            this.maxDepth = maxDepth;

            this.items = new ArrayList<>();
            this.forSale = new ArrayList<>();
            this.roomList = new ArrayList<>();
            for (int i = 0; i < maxDepth; i++) {
                this.items.add(new HashSet<>());
                this.roomList.add(new HashMap<>());
            }
            this.feelings = new ArrayList<>();
        }

        public void addEntry(int depth, Object src, List<Item> content) {
            this.items.get(depth-1).add(new ItemLog(src, content));
        }

        public void addForSale(List<Item> content) {
            this.forSale.add(content);
        }

        public void addRoom(int depth, Room room, String caption) {
            this.roomList.get(depth-1).put(room, caption);
        }

        public SeedfinderLogResult toLogResult() {
            String[] main = new String[maxDepth + 1];
            Arrays.fill(main, "");
            String[] rooms = new String[maxDepth + 1];
            Arrays.fill(rooms, "");

            //seed text in 0th entry
            main[0] += Messages.get(this, "custom_seed", seed) + "\n\n";

            //trinkets in 0th entry
            if (Options.logTrinkets && rolledTrinkets != null) {
                LinkedList<String> trinketStrings = new LinkedList<>();
                for (Item trinket : rolledTrinkets) {
                    trinketStrings.add(checkTarget(trinket.title()));
                }

                main[0] += Messages.get(this, "trinkets", trinketStrings.toArray());
            }

            for (int depth = 1; depth <= maxDepth; depth++) {
                //depth and floor feeling
                String depthFeeling;
                if (depth % 5 == 0) {
                    depthFeeling = Messages.get(this, "boss_floor");
                } else {
                    Level.Feeling feeling = feelings.get(depth-1);
                    depthFeeling = (feeling == Level.Feeling.NONE) ?
                            Messages.get(this, "none_floor") : feeling.title();
                }
                main[depth] += Messages.get(this, "items_title", depth, depthFeeling);

                //add shop items
                if (Options.checkShops) {
                    switch (depth) {
                        case 6: case 11: case 16: case 20:
                        int shopIndex = depth / 5 - 1;
                        if (shopIndex < forSale.size()) {
                            main[depth] += "\n\n";
                            main[depth] += itemsToString("Shop", forSale.get(shopIndex));
                        }
                    }
                }

                //add floor items
                main[depth] += "\n\n";
                for (ItemLog entry : items.get(depth-1)) {
                    main[depth] += entry.toString();
                    main[depth] += "\n";
                }
                main[depth] += "\n";

                //handle quests
                if (depth == ghostDepth) {
                    String questType = Messages.get(this, "ghost_type_"+depth);
                    main[depth] += Messages.get(this, "ghost", questType,
                            checkTarget(ghostWeapon.title()),
                            checkTarget(ghostArmor.title()));
                } else if (depth == wandmakerDepth) {
                    String questType = Messages.get(this, "wandmaker_type_"+wandmakerType);
                    main[depth] += Messages.get(this, "wandmaker", questType,
                            checkTarget(wandmakerWand1.title()),
                            checkTarget(wandmakerWand2.title()));
                } else if (depth == blacksmithDepth) {
                    String questType = Messages.get(this, "blacksmith_type_"+blacksmithType);
                    main[depth] += itemsToString(Messages.get(this, "blacksmith", questType),
                            blacksmithSmithRewards);
                } else if (depth == impDepth) {
                    String questType = impType ? "monks" : "golems";
                    main[depth] += Messages.get(this, "imp", questType,
                            checkTarget(impReward.title()));
                }

                //add rooms
                rooms[depth] += Messages.get(this, "rooms_title", depth, depthFeeling);
                rooms[depth] += "\n\n";

                for (Room room : roomList.get(depth-1).keySet()) {
                    String roomName = room.getClass().getSimpleName()
                            .replaceAll("([a-z])([A-Z])", "$1 $2").toLowerCase();
                    if (Options.useRooms) {
                        roomName = checkTarget(roomName);
                    }

                    if (!roomList.get(depth-1).get(room).isEmpty()) {
                        rooms[depth] += Messages.format("_-_ %s (%s)\n", roomName, roomList.get(depth-1).get(room));
                    } else {
                        rooms[depth] += Messages.format("_-_ %s\n", roomName);
                    }
                }
            }

            SeedfinderLogResult result = new SeedfinderLogResult();
            result.main = main;
            result.rooms = rooms;
            return result;
        }
    }

    public static class ItemLog {
        Object src;
        List<Item> content;

        public ItemLog(Object src, List<Item> content) {
            this.src = src;
            this.content = content;
        }

        @Override
        public String toString() {
            String caption;
            if (src instanceof Heap.Type) {
                caption = ((Type) src).name().replaceAll("_", " ").toLowerCase();
            } else {
                caption = src.getClass().getSimpleName()
                        .replaceAll("([a-z])([A-Z])", "$1 $2").toLowerCase();
            }

            return itemsToString(caption, content);
        }
    }

    public static HashMap<String, Boolean> targets = new HashMap<>();
    public static final HashMap<String, Boolean> DEFAULT_TARGETS = new HashMap<>();
    static {
        DEFAULT_TARGETS.put("upgrade", false);
        DEFAULT_TARGETS.put("strength", false);
        DEFAULT_TARGETS.put("entrance", false);
        DEFAULT_TARGETS.put("exit", false);
    }

    public static SeedLog findSeed() {
        HashMap<String, Boolean> inputTargets = new HashMap<>();
        for (String target : SPDSettings.seedfinderPrompt().toLowerCase().split(System.lineSeparator())) {
            inputTargets.put(target, false);
        }

        SeedFinder.loadConfig();

        long start = Random.Long(DungeonSeed.TOTAL_SEEDS);
        for (long i = start; i < DungeonSeed.TOTAL_SEEDS; i++) {
            if (Thread.currentThread().isInterrupted()) {
                Gdx.app.postRunnable(() -> ShatteredPixelDungeon.scene()
                        .addToFront(new WndMessage("Searched interrupted.")));
                return null;
            }

            targets = new HashMap<>(inputTargets);
            SPDSettings.customSeed(DungeonSeed.convertToCode(i));
            SeedLog log = scoutDungeon();
            log.toLogResult();

            if ((Options.condition == CONDITION_ALL && !targets.containsValue(false)) ||
                    (Options.condition == CONDITION_ANY && targets.containsValue(true))) {
                long count = i - start + 1;
                Gdx.app.postRunnable(() -> ShatteredPixelDungeon.scene()
                        .addToFront(new WndMessage("Searched through _" + count + "_ seeds.")));
                return log;
            }
        }

        return null;
    }

    public static String checkTarget(String title) {
        boolean match = false;
        for (String target : targets.keySet()) {
            if (title.contains(target)) {
                targets.put(target, true);
                match = true;
            }
        }

        if (match) return Messages.format("_%s_", title);
        else	   return title;
    }

    private static final long SECOND = 1000;
    private static final long MINUTE = 60 * SECOND;
    private static final long HOUR = 60 * MINUTE;
    private static final long DAY = 24 * HOUR;
    private static final long WEEK = 7 * DAY;

    public static SeedLog scoutDaily() {
        SeedFinder.loadConfig();
        long lastDaily = SPDSettings.lastDaily();
        long time = Game.realTime - (Game.realTime % DAY);
        time = Math.max(time, 20_148 * DAY);
        SPDSettings.lastDaily(time);
        Dungeon.daily = Options.searchForDaily = true;
        targets = new HashMap<>(DEFAULT_TARGETS);
        SeedLog log = scoutDungeon();
        Dungeon.daily = Options.searchForDaily = false;
        SPDSettings.lastDaily(lastDaily);
        return log;
    }

    public static SeedLog scoutWeekly() {
        SeedFinder.loadConfig();
        long lastWeekly = SPDSettings.lastWeekly();
        long time = Game.realTime - (Game.realTime % WEEK);
        time = Math.max(time, 2_821 * WEEK);
        SPDSettings.lastWeekly(time);
        Dungeon.weekly = Options.searchForWeekly = true;
        targets = new HashMap<>(DEFAULT_TARGETS);
        SeedLog log = scoutDungeon();
        Dungeon.weekly = Options.searchForWeekly = false;
        SPDSettings.lastWeekly(lastWeekly);
        return log;
    }

    public static SeedLog scoutSeed(String seed) {
        SeedFinder.loadConfig();
        targets = new HashMap<>(DEFAULT_TARGETS);
        SPDSettings.customSeed(seed);
        return scoutDungeon();
    }

    public static SeedLog scoutDungeon() {
        Dungeon.initSeed();
        SPDSettings.challenges(Options.challenges);
        Dungeon.init();

        SeedLog log = new SeedLog(Dungeon.customSeedText, Options.floors);

        //check trinkets
        if (Options.logTrinkets) {
            log.rolledTrinkets = rollTrinkets();
        }

        //check each floor
        for ( ;Dungeon.depth <= Options.floors; Dungeon.depth++) {

            Level level = Dungeon.level = Dungeon.newLevel();

            log.feelings.add(level.feeling);

            //check generated items and mob drops (only statues and mimics)
            ArrayList<Heap> heaps = new ArrayList<>(level.heaps.valueList());

            heaps.addAll(getMobDrops(level));

            LinkedList<Item> forSale = new LinkedList<>();
            for (Heap heap : filterHeaps(heaps)) {
                if (heap.type != Type.FOR_SALE) {
                    log.addEntry(Dungeon.depth, heap.type, heap.items);
                } else if (Options.checkShops) {
                    forSale.addAll(heap.items);
                }
            }

            //add shop items separately
            switch (Dungeon.depth) {
                case 6: case 11: case 16: case 20:
                    forSale = filterItems(forSale);
                    if (Options.checkShops && !forSale.isEmpty()) {
                        log.addForSale(forSale);
                    }
            }

            //skip below for boss floors
            if (Dungeon.depth % 5 == 0) {
                continue;
            }

            //check rooms
            for (Room room : roomList) {
                String caption = "";
                if (room instanceof SacrificeRoom && Options.logEquipment) {
                    //special case
                    SacrificialFire fire = (SacrificialFire) level.blobs.get(SacrificialFire.class);
                    if (fire != null) {
                        log.addEntry(Dungeon.depth, fire, List.of(fire.getPrize()));
                    }
                }

                if (room instanceof MagicWellRoom || room instanceof SecretWellRoom) {
                    int wellPos;
                    if (room instanceof MagicWellRoom) {
                        Point c = room.center();
                        wellPos = c.x + level.width() * c.y;
                    } else {
                        Point door = ((SecretWellRoom) room).entrance();
                        Point well;
                        if (door.x == room.left){
                            well = new Point(room.right-2, door.y);
                        } else if (door.x == room.right){
                            well = new Point(room.left+2, door.y);
                        } else if (door.y == room.top){
                            well = new Point(door.x, room.bottom-2);
                        } else {
                            well = new Point(door.x, room.top+2);
                        }
                        wellPos = well.x + level.width() * well.y;
                    }
                    WaterOfHealth health = (WaterOfHealth) level.blobs.get(WaterOfHealth.class);
                    WaterOfAwareness aware = (WaterOfAwareness) level.blobs.get(WaterOfAwareness.class);
                    if (health != null && health.cur[wellPos] != 0) {
                        caption = "health";
                    } else if (aware != null && aware.cur[wellPos] != 0) {
                        caption = "awareness";
                    }
                } else {
                    Package roomType = room.getClass().getPackage();
                    if (roomType == SecretRoom.class.getPackage()) {
                        caption = "secret";
                    } else if (roomType == SpecialRoom.class.getPackage()) {
                        caption = "special";
                    } else if (roomType == ConnectionRoom.class.getPackage()) {
                        continue;
                    }
                }

                log.addRoom(Dungeon.depth, room, caption);
            }

            //check quest NPC presence and grab quest info
            for (Mob mob : level.mobs) {
                if (mob instanceof Ghost && Ghost.Quest.armor != null) {
                    log.ghostDepth = Dungeon.depth;
                    log.ghostWeapon = Ghost.Quest.weapon.enchant(Ghost.Quest.enchant);
                    log.ghostArmor = Ghost.Quest.armor.inscribe(Ghost.Quest.glyph);
                } else if (mob instanceof Wandmaker && Wandmaker.Quest.wand1 != null) {
                    log.wandmakerDepth = Dungeon.depth;
                    log.wandmakerType = Wandmaker.Quest.type;
                    log.wandmakerWand1 = Wandmaker.Quest.wand1;
                    log.wandmakerWand2 = Wandmaker.Quest.wand2;
                } else if (mob instanceof Blacksmith && !Blacksmith.Quest.smithRewards.isEmpty()) {
                    log.blacksmithDepth = Dungeon.depth;
                    log.blacksmithType = Blacksmith.Quest.type;
                    log.blacksmithSmithRewards = new ArrayList<>(Blacksmith.Quest.smithRewards);
                } else if (mob instanceof Imp && Imp.Quest.reward != null) {
                    log.impDepth = Dungeon.depth;
                    log.impType = Imp.Quest.alternative;
                    log.impReward = Imp.Quest.reward;
                }
            }
        }

        return log;
    }

    private static LinkedList<Item> rollTrinkets() {
        //simulate rolling for trinkets
        TrinketCatalyst cata = new TrinketCatalyst();

        //roll new trinkets if trinkets were not already rolled
        while (cata.rolledTrinkets.size() < TrinketCatalyst.WndTrinket.NUM_TRINKETS) {
            cata.rolledTrinkets.add((Trinket) Generator.random(Generator.Category.TRINKET));
        }

        return new LinkedList<>(cata.rolledTrinkets);
    }

    private static LinkedList<Heap> filterHeaps(ArrayList<Heap> heaps) {
        LinkedList<Heap> filtered = new LinkedList<>();
        for (Heap h : heaps) {
            LinkedList<Item> remaining = filterItems(h.items);
            if (!remaining.isEmpty()) {
                h.items = remaining;
                filtered.add(h);
            }
        }
        return filtered;
    }

    private static LinkedList<Item> filterItems(LinkedList<Item> items) {
        LinkedList<Item> filtered = new LinkedList<>();
        for (Item i : items) {
            if (Options.logArtifacts && i instanceof Artifact) {
                filtered.add(i);
            } else if (Options.logRings && i instanceof Ring) {
                filtered.add(i);
            } else if (Options.logEquipment && (i instanceof Weapon || i instanceof Armor)) {
                filtered.add(i);
            } else if (Options.logWands && i instanceof Wand) {
                filtered.add(i);
            } else if (Options.logPotions && i instanceof Potion) {
                filtered.add(i);
            } else if (Options.logScrolls && i instanceof Scroll) {
                filtered.add(i);
            } else if (Options.logOther && (Options.ignoreBlacklist || !blacklist.contains(i.getClass()))) {
                filtered.add(i);
            }
        }
        return filtered;
    }

    private static String itemsToString(String caption, List<Item> content) {
        LinkedList<String> itemStrings = new LinkedList<>();
        for (Item item: content) {
            item.identify(false);
            itemStrings.add(checkTarget(item.title()));
        }

        return Messages.format("%s: %s", caption, String.join(", ", itemStrings));
    }
}