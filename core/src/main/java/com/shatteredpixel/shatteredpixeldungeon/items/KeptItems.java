package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.Key;
import com.watabou.utils.Bundle;
import com.watabou.utils.FileUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import com.watabou.utils.Bundlable;

public class KeptItems {

    public static final int MAX_STORED = 15;
    public static final int MAX_PER_ENDING = 3;

    private static final String FILE = "kept_items.dat";
    private static final String KEY = "items";

    public static boolean pending = false;

    public static ArrayList<Item> stored() {
        ArrayList<Item> list = new ArrayList<>();
        try {
            if (FileUtils.fileExists(FILE)) {
                Bundle b = FileUtils.bundleFromFile(FILE);
                Collection<Bundlable> loaded = b.getCollection(KEY);
                for (Bundlable o : loaded) {
                    if (o instanceof Item) list.add((Item) o);
                }
            }
        } catch (Exception ignored) {
        }
        return list;
    }

    private static boolean write(ArrayList<Item> list) {
        try {
            Bundle b = new Bundle();
            b.put(KEY, list);
            FileUtils.bundleToFile(FILE, b);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean add(Item item) {
        ArrayList<Item> list = stored();
        if (list.size() >= MAX_STORED) return false;
        list.add(item);
        return write(list);
    }

    public static Item take(int index) {
        ArrayList<Item> list = stored();
        if (index < 0 || index >= list.size()) return null;
        Item item = list.remove(index);
        write(list);
        return item;
    }

    public static void markPending() {
        pending = !stored().isEmpty();
    }

    public static boolean consumePending() {
        boolean p = pending;
        pending = false;
        return p && !stored().isEmpty();
    }

    public static boolean eligible(Item item) {
        if (item == null) return false;
        if (item instanceof Gold || item instanceof Amulet || item instanceof Key || item instanceof Bag) return false;
        if (item instanceof ShulkerBox || item instanceof Ruby) return false;
        if (item.unique) return false;
        return true;
    }

    public static ArrayList<Item> candidates(Hero hero) {
        ArrayList<Item> list = new ArrayList<>();
        if (hero == null) return list;
        for (Item i : hero.belongings) {
            if (eligible(i) && !list.contains(i)) list.add(i);
        }
        return list;
    }

    public static BigInteger cost(Item item) {
        int tiers = Math.max(0, Item.Rarity.COMMON.ordinal() - item.rarity.ordinal());
        double lvl = Math.min(1000000d, Math.max(0d, item.level().doubleValue()));
        return BigInteger.valueOf(4 + 3L * tiers + 2L * (long) Math.sqrt(lvl));
    }

    public static Item copyOf(Item item) {
        Bundle b = new Bundle();
        b.put("i", item);
        Item copy = (Item) b.get("i");
        if (copy.stackable) copy.quantity(BigInteger.ONE);
        return copy;
    }
}
