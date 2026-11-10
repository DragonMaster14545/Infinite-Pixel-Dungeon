package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.FileUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.stream.IntStream;

public class VaultStore {

	public enum Mode { NORMAL, DAILY, WEEKLY, SEED, CHALLENGE }

	private static int capacityPercent(Mode mode) {
		switch (mode) {
			case DAILY:
			case WEEKLY:    return 50;
			default:        return 100;
		}
	}

	private static int limitPercent(Mode mode) {
		switch (mode) {
			case DAILY:
			case WEEKLY:    return 50;
			default:        return 100;
		}
	}

	public static int capacityPercent() {
		return capacityPercent(mode());
	}

	public static int limitPercent() {
		return limitPercent(mode());
	}

	@SuppressWarnings("NewApi")
    public static Mode mode() {
		if (Dungeon.level != null || Dungeon.hero != null) {
			if (Dungeon.weekly) return Mode.WEEKLY;
			if (Dungeon.daily) return Mode.DAILY;
			if (Dungeon.customSeedText != null && !Dungeon.customSeedText.isEmpty()) return Mode.SEED;
			if (IntStream.range(0, Dungeon.challenges.length).anyMatch(i -> Dungeon.challenges[i])) return Mode.CHALLENGE;
		}
		return Mode.NORMAL;
	}

	private static String fileName(Mode mode) {
		if (mode == Mode.NORMAL) return "vaultBoxes.dat";
		return "vaultBoxes_" + mode.name().toLowerCase() + ".dat";
	}

	private static final String BOXES = "boxes";
	private static final String PINNED = "pinned";
	private static final String EXTRA_SLOTS = "extra_slots";

	private static final EnumMap<Mode, Long> extraCache = new EnumMap<>(Mode.class);

	private static final EnumMap<Mode, Integer> pinnedCache = new EnumMap<>(Mode.class);

	private static final EnumMap<Mode, ArrayList<ShulkerBox>> cache = new EnumMap<>(Mode.class);

	public static ArrayList<ShulkerBox> boxes() {
		Mode mode = mode();

		ArrayList<ShulkerBox> list = cache.get(mode);
		if (list == null) {
			list = new ArrayList<>();
			pinnedCache.put(mode, 0);
			extraCache.put(mode, 0L);
			String file = fileName(mode);
			if (FileUtils.fileExists(file)) {
				try {
					Bundle b = FileUtils.bundleFromFile(file);
					Collection<Bundlable> loaded = b.getCollection(BOXES);
					for (Bundlable o : loaded) {
						if (o instanceof ShulkerBox) list.add((ShulkerBox) o);
					}
					if (b.contains(EXTRA_SLOTS)) {
						extraCache.put(mode, Math.max(0L, b.getLong(EXTRA_SLOTS)));
					}
					if (b.contains(PINNED)) {
						pinnedCache.put(mode, Math.max(0, Math.min(b.getInt(PINNED), list.size())));
					}
				} catch (IOException ignored) {
				}
			}
			cache.put(mode, list);
		}
		return list;
	}

	public static long extraSlots() {
		boxes();
		Long e = extraCache.get(mode());
		return e == null ? 0 : e;
	}

	public static void setExtraSlots(long n) {
		boxes();
		extraCache.put(mode(), Math.max(0L, n));
	}

	public static int pinnedCount() {
		ArrayList<ShulkerBox> list = boxes();
		Integer p = pinnedCache.get(mode());
		return p == null ? 0 : Math.max(0, Math.min(p, list.size()));
	}

	public static int indexOf(ShulkerBox box) {
		ArrayList<ShulkerBox> list = boxes();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i) == box) return i;
		}
		return -1;
	}

	public static boolean isPinned(ShulkerBox box) {
		int i = indexOf(box);
		return i >= 0 && i < pinnedCount();
	}

	public static boolean togglePin(ShulkerBox box) {
		ArrayList<ShulkerBox> list = boxes();
		int i = indexOf(box);
		if (i < 0) return false;
		int p = pinnedCount();

		list.remove(i);
		if (i < p) {
			list.add(p - 1, box);
			p--;
		} else {
			list.add(p, box);
			p++;
		}
		pinnedCache.put(mode(), p);
		return true;
	}

	public static int take(ShulkerBox box) {
		int i = indexOf(box);
		if (i < 0) return -1;
		int p = pinnedCount();
		boxes().remove(i);
		if (i < p) pinnedCache.put(mode(), p - 1);
		return i;
	}

	public static void putBack(ShulkerBox box, int index, int pinnedBefore) {
		ArrayList<ShulkerBox> list = boxes();
		list.add(Math.max(0, Math.min(index, list.size())), box);
		pinnedCache.put(mode(), pinnedBefore);
	}

	public static boolean save() {
		try {
			Bundle b = new Bundle();
			b.put(BOXES, boxes());
			b.put(PINNED, pinnedCount());
			b.put(EXTRA_SLOTS, extraSlots());
			FileUtils.bundleToFile(fileName(mode()), b);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public static boolean sync() {
		try {
			Dungeon.saveAll();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
