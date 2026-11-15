package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;

import java.math.BigInteger;
import java.util.ArrayList;

public class CompartmentBox extends ShulkerBox {

	{
		image  = ItemSpriteSheet.COMPARTMENT_BOX;

		defaultAction = AC_OPEN;
		levelKnown = true;
		cursedKnown = true;
	}

	public static final int MAX_CUSTOM = 4;
	public static final int NAME_LIMIT = 500;

	private static final String NAMES = "comp_names";
	private static final String PINS = "comp_pins";
	private static final String ITEMS = "comp_items_";
	private static final String CURRENT = "comp_current";

	private final ArrayList<ArrayList<Item>> lists = new ArrayList<>();
	private final ArrayList<String> names = new ArrayList<>();
	private final ArrayList<Integer> pins = new ArrayList<>();
	private int current = 0;

	{
		lists.add(contents);
		names.add("");
		pins.add(0);
	}

	public int compartmentCount() {
		return lists.size();
	}

	public int currentCompartment() {
		return current;
	}

	public boolean canAddCompartment() {
		return lists.size() < 1 + MAX_CUSTOM;
	}

	public String compartmentName(int index) {
		if (index <= 0 || index >= names.size()) return Messages.get(CompartmentBox.class, "main");
		return names.get(index);
	}

	public int compartmentSize(int index) {
		return index >= 0 && index < lists.size() ? lists.get(index).size() : 0;
	}

	public void selectCompartment(int index) {
		if (index < 0 || index >= lists.size() || index == current) return;
		pins.set(current, pinned);
		current = index;
		contents = lists.get(index);
		pinned = pins.get(index);
	}

	private static String clean(String name) {
		String n = name == null ? "" : name.trim();
		if (n.length() > NAME_LIMIT) n = n.substring(0, NAME_LIMIT).trim();
		return n;
	}

	public int addCompartment(String name) {
		if (!canAddCompartment()) return -1;
		String n = clean(name);
		if (n.isEmpty()) n = Messages.get(CompartmentBox.class, "default_name", lists.size());
		lists.add(new ArrayList<Item>());
		names.add(n);
		pins.add(0);
		return lists.size() - 1;
	}

	public void renameCompartment(int index, String name) {
		if (index <= 0 || index >= names.size()) return;
		String n = clean(name);
		if (n.isEmpty()) return;
		names.set(index, n);
	}

	public boolean deleteCompartment(int index) {
		if (index <= 0 || index >= lists.size()) return false;
		if (!lists.get(index).isEmpty()) {
			GLog.w(Messages.get(CompartmentBox.class, "not_empty"));
			return false;
		}
		if (current == index) selectCompartment(0);
		lists.remove(index);
		names.remove(index);
		pins.remove(index);
		if (current > index) current--;
		return true;
	}

	public boolean moveToCompartment(Item item, int target) {
		int at = indexOf(item);
		if (at < 0 || target < 0 || target >= lists.size() || target == current) return false;

		ArrayList<Item> dest = lists.get(target);
		Item same = null;
		if (item.stackable) {
			for (Item i : dest) {
				if (item.isSimilar(i)) {
					same = i;
					break;
				}
			}
		}

		if (same == null && BigInteger.valueOf(dest.size()).compareTo(capacity()) >= 0) {
			GLog.w(Messages.get(CompartmentBox.class, "compartment_full", compartmentName(target)));
			return false;
		}

		String itemName = item.name();

		if (same != null) {
			BigInteger room = BigInteger.ZERO.max(maxQuantity().subtract(same.quantity()));
			if (room.signum() <= 0) {
				GLog.w(Messages.get(this, "stack_limit", maxQuantity()));
				return false;
			}
			if (item.quantity().compareTo(room) > 0) {
				Item part = item.split(room);
				if (part == null) {
					GLog.w(Messages.get(this, "cannot_split"));
					return false;
				}
				same.merge(part);
				GLog.i(Messages.get(CompartmentBox.class, "moved", itemName, compartmentName(target)));
				return true;
			}
			removeAt(at);
			same.merge(item);
		} else {
			removeAt(at);
			dest.add(item);
		}

		GLog.i(Messages.get(CompartmentBox.class, "moved", itemName, compartmentName(target)));
		return true;
	}

	private void removeAt(int index) {
		int p = pinnedCount();
		contents.remove(index);
		if (index < p) pinned = p - 1;
	}

	@Override
	public ArrayList<Item> allContents() {
		ArrayList<Item> all = new ArrayList<>();
		for (ArrayList<Item> l : lists) all.addAll(l);
		return all;
	}

	@Override
	public boolean isEmpty() {
		for (ArrayList<Item> l : lists) {
			if (!l.isEmpty()) return false;
		}
		return true;
	}

	@Override
	public BigInteger value() {
		BigInteger total = BigInteger.valueOf(420L).add(BigInteger.valueOf(40L).multiply(level()));
		for (ArrayList<Item> l : lists) {
			for (Item i : l) total = total.add(i.value());
		}
		return total;
	}

	@Override
	public String desc() {
		String desc = super.desc();
		if (Dungeon.level != null || Dungeon.hero != null) {
			desc += "\n\n" + Messages.get(this, "desc_compartments", compartmentCount(), compartmentName(current));
		}
		return desc;
	}

	@Override
	public Item duplicate() {
		CompartmentBox dupe = (CompartmentBox) super.duplicate();
		if (dupe != null) {
			for (ArrayList<Item> l : dupe.lists) l.clear();
			for (int i = 0; i < dupe.pins.size(); i++) dupe.pins.set(i, 0);
			dupe.pinned = 0;
		}
		return dupe;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		int cur = current;
		selectCompartment(0);
		super.storeInBundle(bundle);

		int custom = lists.size() - 1;
		String[] n = new String[custom];
		int[] p = new int[custom];
		for (int i = 0; i < custom; i++) {
			n[i] = names.get(i + 1);
			p[i] = Math.max(0, Math.min(pins.get(i + 1), lists.get(i + 1).size()));
			bundle.put(ITEMS + (i + 1), lists.get(i + 1));
		}
		bundle.put(NAMES, n);
		bundle.put(PINS, p);
		bundle.put(CURRENT, cur);

		selectCompartment(cur);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);

		current = 0;
		lists.clear();
		names.clear();
		pins.clear();
		lists.add(contents);
		names.add("");
		pins.add(pinned);

		if (bundle.contains(NAMES)) {
			String[] n = bundle.getStringArray(NAMES);
			int[] p = bundle.contains(PINS) ? bundle.getIntArray(PINS) : new int[0];
			for (int i = 0; i < n.length && i < MAX_CUSTOM; i++) {
				ArrayList<Item> l = new ArrayList<>();
				if (bundle.contains(ITEMS + (i + 1))) {
					for (Bundlable b : bundle.getCollection(ITEMS + (i + 1))) {
						if (b != null) l.add((Item) b);
					}
				}
				lists.add(l);
				names.add(n[i]);
				pins.add(i < p.length ? Math.max(0, Math.min(p[i], l.size())) : 0);
			}
		}

		int cur = bundle.getInt(CURRENT);
		if (cur > 0 && cur < lists.size()) selectCompartment(cur);
	}
}
