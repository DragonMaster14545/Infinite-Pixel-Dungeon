package com.shatteredpixel.shatteredpixeldungeon.items;



import com.shatteredpixel.shatteredpixeldungeon.utils.Big;
import java.math.BigInteger;
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

	public enum Mode { NORMAL, DAILY, WEEKLY, MONTHLY, SEED, CHALLENGE }

	public static final int MAX_HISTORY = 300;

	public static final String LOG_DEPOSIT = "deposit";
	public static final String LOG_WITHDRAW = "withdraw";
	public static final String LOG_TRANSFER_OUT = "transfer_out";
	public static final String LOG_TRANSFER_IN = "transfer_in";

	public static class Entry {
		public long time;
		public String type;
		public String text;
	}

	private static class Store {
		ArrayList<ShulkerBox> boxes = new ArrayList<>();
		int pinned = 0;
		BigInteger extra = BigInteger.ZERO;
		ArrayList<Entry> history = new ArrayList<>();
	}

	public static int capacityPercent(Mode mode) {
		switch (mode) {
			case DAILY:
			case WEEKLY:
			case MONTHLY: return 50;
			default:        return 100;
		}
	}

	public static int limitPercent(Mode mode) {
		switch (mode) {
			case DAILY:
			case WEEKLY:
            case MONTHLY: return 50;
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
			if (Dungeon.practice) return Dungeon.practiceVault;
			if (Dungeon.weekly) return Mode.WEEKLY;
			if (Dungeon.daily) return Mode.DAILY;
			if (Dungeon.monthly) return Mode.MONTHLY;
			if (Dungeon.customSeedText != null && !Dungeon.customSeedText.isEmpty()) return Mode.SEED;
			if (Dungeon.challenges != null && IntStream.range(0, Dungeon.challenges.length).anyMatch(i -> Dungeon.challenges[i])) return Mode.CHALLENGE;
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
	private static final String HISTORY = "history";

	private static final EnumMap<Mode, Store> stores = new EnumMap<>(Mode.class);

	private static Store store(Mode mode) {
		Store s = stores.get(mode);
		if (s == null) {
			s = new Store();
			String file = fileName(mode);
			if (FileUtils.fileExists(file)) {
				try {
					Bundle b = FileUtils.bundleFromFile(file);
					Collection<Bundlable> loaded = b.getCollection(BOXES);
					for (Bundlable o : loaded) {
						if (o instanceof ShulkerBox) s.boxes.add((ShulkerBox) o);
					}
					if (b.contains(EXTRA_SLOTS)) {
						s.extra = BigInteger.ZERO.max(Big.getBig(b, EXTRA_SLOTS));
					}
					if (b.contains(PINNED)) {
						s.pinned = Math.max(0, Math.min(b.getInt(PINNED), s.boxes.size()));
					}
					if (b.contains(HISTORY)) {
						String[] lines = b.getStringArray(HISTORY);
						if (lines != null) {
							for (String line : lines) {
								String[] p = line.split("\\|", 3);
								if (p.length < 3) continue;
								try {
									Entry e = new Entry();
									e.time = Long.parseLong(p[0]);
									e.type = p[1];
									e.text = p[2];
									s.history.add(e);
								} catch (NumberFormatException ignored) {
								}
							}
						}
					}
				} catch (IOException ignored) {
				}
			}
			stores.put(mode, s);
		}
		return s;
	}

	public static ArrayList<ShulkerBox> boxes() {
		return boxes(mode());
	}

	public static ArrayList<ShulkerBox> boxes(Mode mode) {
		return store(mode).boxes;
	}

	public static BigInteger extraSlots() {
		return extraSlots(mode());
	}

	public static BigInteger extraSlots(Mode mode) {
		return store(mode).extra;
	}

	public static void setExtraSlots(BigInteger n) {
		store(mode()).extra = BigInteger.ZERO.max(n);
	}

	public static int pinnedCount() {
		Store s = store(mode());
		return Math.max(0, Math.min(s.pinned, s.boxes.size()));
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
		store(mode()).pinned = p;
		return true;
	}

	public static int take(ShulkerBox box) {
		int i = indexOf(box);
		if (i < 0) return -1;
		int p = pinnedCount();
		boxes().remove(i);
		if (i < p) store(mode()).pinned = p - 1;
		return i;
	}

	public static void putBack(ShulkerBox box, int index, int pinnedBefore) {
		ArrayList<ShulkerBox> list = boxes();
		list.add(Math.max(0, Math.min(index, list.size())), box);
		store(mode()).pinned = pinnedBefore;
	}

	public static ArrayList<Entry> history() {
		return history(mode());
	}

	public static ArrayList<Entry> history(Mode mode) {
		return store(mode).history;
	}

	public static void log(String type, String text) {
		log(mode(), type, text);
	}

	public static void log(Mode mode, String type, String text) {
		ArrayList<Entry> h = store(mode).history;
		Entry e = new Entry();
		e.time = System.currentTimeMillis();
		e.type = type;
		e.text = text == null ? "" : text.replace('\n', ' ');
		h.add(e);
		while (h.size() > MAX_HISTORY) h.remove(0);
	}

	public static void unlog(Mode mode) {
		ArrayList<Entry> h = store(mode).history;
		if (!h.isEmpty()) h.remove(h.size() - 1);
	}

	public static boolean save() {
		return save(mode());
	}

	public static boolean save(Mode mode) {
		try {
			Store s = store(mode);
			Bundle b = new Bundle();
			b.put(BOXES, s.boxes);
			b.put(PINNED, Math.max(0, Math.min(s.pinned, s.boxes.size())));
			Big.put(b, EXTRA_SLOTS, s.extra);
			String[] lines = new String[s.history.size()];
			for (int i = 0; i < lines.length; i++) {
				Entry e = s.history.get(i);
				lines[i] = e.time + "|" + e.type + "|" + e.text;
			}
			b.put(HISTORY, lines);
			FileUtils.bundleToFile(fileName(mode), b);
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
