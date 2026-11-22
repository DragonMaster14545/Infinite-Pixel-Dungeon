package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.utils.Big;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.FileUtils;
import com.watabou.utils.Random;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;

public class Loadout {

    public static final int STOCK_SIZE = 25;
    public static final int MAX_ITEMS = 10;
    public static final int REROLL_COST = 2;
    public static final long RESTOCK_MS = 12L * 60L * 60L * 1000L;

    private static final String STOCK_FILE = "loadout_stock.dat";
    private static final String PACK_FILE = "loadout_pack.dat";

    public static boolean pending = false;

    public static class Entry implements Bundlable {
        public Item item;
        public BigInteger price = BigInteger.ONE;
        public boolean sold = false;

        @Override
        public void storeInBundle(Bundle bundle) {
            bundle.put("item", item);
            Big.put(bundle, "price", price);
            bundle.put("sold", sold);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            item = (Item) bundle.get("item");
            price = Big.getBig(bundle, "price");
            sold = bundle.getBoolean("sold");
        }
    }

    private static final Generator.Category[] CATEGORIES = {
            Generator.Category.WEAPON, Generator.Category.ARMOR, Generator.Category.MISSILE,
            Generator.Category.WAND, Generator.Category.RING, Generator.Category.ARTIFACT,
            Generator.Category.FOOD, Generator.Category.POTION, Generator.Category.SCROLL,
            Generator.Category.SEED, Generator.Category.STONE,
            Generator.Category.TRINKET, Generator.Category.MODULE
    };
    private static final float[] WEIGHTS = { 4, 3, 2, 2, 2, 1, 2, 4, 4, 1, 1, 1, 1};

    private static ArrayList<Entry> stockCache = null;
    private static long stockedAt = 0L;

    public static ArrayList<Entry> stock() {
        if (stockCache == null) stockCache = loadStock();
        long elapsed = System.currentTimeMillis() - stockedAt;
        if (stockCache.isEmpty() || elapsed >= RESTOCK_MS || elapsed < 0) {
            stockCache = generate();
            stockedAt = System.currentTimeMillis();
            saveStock();
        }
        return stockCache;
    }

    public static long msLeft() {
        if (stockCache == null) stock();
        return Math.max(0L, Math.min(RESTOCK_MS, stockedAt + RESTOCK_MS - System.currentTimeMillis()));
    }

    public static boolean expired() {
        if (stockCache == null) return false;
        long elapsed = System.currentTimeMillis() - stockedAt;
        return elapsed >= RESTOCK_MS || elapsed < 0;
    }

    private static ArrayList<Entry> loadStock() {
        ArrayList<Entry> list = new ArrayList<>();
        stockedAt = 0L;
        try {
            if (FileUtils.fileExists(STOCK_FILE)) {
                Bundle b = FileUtils.bundleFromFile(STOCK_FILE);
                stockedAt = b.getLong("stocked_at");
                Collection<Bundlable> loaded = b.getCollection("entries");
                for (Bundlable o : loaded) {
                    if (o instanceof Entry && ((Entry) o).item != null) list.add((Entry) o);
                }
            }
        } catch (Exception ignored) {
        }
        return list;
    }

    public static void saveStock() {
        try {
            Bundle b = new Bundle();
            b.put("entries", stockCache == null ? new ArrayList<Entry>() : stockCache);
            b.put("stocked_at", stockedAt);
            FileUtils.bundleToFile(STOCK_FILE, b);
        } catch (Exception ignored) {
        }
    }

    private static ArrayList<Entry> generate() {
        ArrayList<Entry> list = new ArrayList<>();
        Random.pushGenerator();
        try {
            int guard = 0;
            while (list.size() < STOCK_SIZE && guard++ < 200) {
                try {
                    int pick = Random.chances(WEIGHTS);
                    if (pick < 0 || pick >= CATEGORIES.length) continue;
                    Item item = Generator.randomUsingDefaults(CATEGORIES[pick]);
                    if (item == null || item instanceof Gold || item instanceof Ruby) continue;
                    if (item.cursed || item.level().signum() < 0) continue;
                    if (item.stackable && item.quantity().compareTo(BigInteger.valueOf(3)) > 0) item.quantity(BigInteger.valueOf(3));
                    if (item.isUpgradable()) item.identify();
                    Entry e = new Entry();
                    e.item = item;
                    e.price = price(item);
                    list.add(e);
                } catch (Throwable ignored) {
                }
            }
        } finally {
            Random.popGenerator();
        }
        return list;
    }

    public static void reroll() {
        stockCache = generate();
        stockedAt = System.currentTimeMillis();
        saveStock();
    }

    public static BigInteger price(Item item) {
        try {
            return rawPrice(item);
        } catch (Throwable t) {
            return BigInteger.valueOf(5);
        }
    }

    private static BigInteger rawPrice(Item item) {
        int tiers = Math.max(0, Item.Rarity.COMMON.ordinal() - item.rarity.ordinal());
        double lvl = Math.min(1000000d, Math.max(0d, item.level().doubleValue()));
        long p = 3 + 2L * tiers + 2L * (long) Math.sqrt(lvl);
        if (item instanceof Artifact) p += 6;
        else if (item instanceof Wand
                || item instanceof Ring) p += 3;
        else if (item instanceof Weapon
                || item instanceof Armor) p += 2;
        if (item.stackable) p += Math.max(0, item.quantity().intValue() - 1);
        return BigInteger.valueOf(p);
    }

    public static ArrayList<Item> pack() {
        ArrayList<Item> list = new ArrayList<>();
        try {
            if (FileUtils.fileExists(PACK_FILE)) {
                Bundle b = FileUtils.bundleFromFile(PACK_FILE);
                Collection<Bundlable> loaded = b.getCollection("items");
                for (Bundlable o : loaded) {
                    if (o instanceof Item) list.add((Item) o);
                }
            }
        } catch (Exception ignored) {
        }
        return list;
    }

    private static boolean writePack(ArrayList<Item> list) {
        try {
            Bundle b = new Bundle();
            b.put("items", list);
            FileUtils.bundleToFile(PACK_FILE, b);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String buy(Entry entry) {
        if (entry == null || entry.sold) return "sold";
        ArrayList<Item> list = pack();
        if (list.size() >= MAX_ITEMS) return "full";
        if (!RubyBank.spend(entry.price)) return "no_ruby";
        list.add(entry.item);
        if (!writePack(list)) {
            RubyBank.add(entry.price);
            return "failed";
        }
        entry.sold = true;
        saveStock();
        return null;
    }

    public static void markPending() {
        pending = !pack().isEmpty();
    }

    public static boolean consumePending() {
        boolean p = pending;
        pending = false;
        return p;
    }

    public static void deliver(Hero hero) {
        ArrayList<Item> list = pack();
        if (list.isEmpty() || hero == null) return;
        LoadoutBox box = new LoadoutBox();
        box.fill(list);
        if (!box.collect(hero.belongings.backpack)) {
            Dungeon.level.drop(box, hero.pos).sprite.drop();
        }
        writePack(new ArrayList<Item>());
    }
}
