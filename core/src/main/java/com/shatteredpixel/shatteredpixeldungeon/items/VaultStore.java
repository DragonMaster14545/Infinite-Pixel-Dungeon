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
		if (Dungeon.weekly) return Mode.WEEKLY;
		if (Dungeon.daily) return Mode.DAILY;
		if (Dungeon.customSeedText != null && !Dungeon.customSeedText.isEmpty()) return Mode.SEED;
		if (IntStream.range(0, Dungeon.challenges.length).anyMatch(i -> Dungeon.challenges[i])) return Mode.CHALLENGE;
		return Mode.NORMAL;
	}

	private static String fileName(Mode mode) {
		if (mode == Mode.NORMAL) return "vaultBoxes.dat";
		return "vaultBoxes_" + mode.name().toLowerCase() + ".dat";
	}

	private static final String BOXES = "boxes";

	private static final EnumMap<Mode, ArrayList<ShulkerBox>> cache = new EnumMap<>(Mode.class);

	public static ArrayList<ShulkerBox> boxes() {
		Mode mode = mode();

		ArrayList<ShulkerBox> list = cache.get(mode);
		if (list == null) {
			list = new ArrayList<>();
			String file = fileName(mode);
			if (FileUtils.fileExists(file)) {
				try {
					Bundle b = FileUtils.bundleFromFile(file);
					Collection<Bundlable> loaded = b.getCollection(BOXES);
					for (Bundlable o : loaded) {
						if (o instanceof ShulkerBox) list.add((ShulkerBox) o);
					}
				} catch (IOException ignored) {
				}
			}
			cache.put(mode, list);
		}
		return list;
	}

	public static boolean save() {
		try {
			Bundle b = new Bundle();
			b.put(BOXES, boxes());
			FileUtils.bundleToFile(fileName(mode()), b);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public static void sync() {
		try {
			Dungeon.saveAll();
		} catch (Exception ignored) {
		}
	}
}
