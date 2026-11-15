package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.Spell;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.Runestone;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;

import java.math.BigInteger;

public class ShulkerFilter implements Bundlable {

	public static final int ANY = 0;
	public static final int YES = 1;
	public static final int NO = 2;

	public static final String[] CATEGORY_KEYS = {
			"weapon", "missile", "armor", "wand", "ring", "artifact", "potion", "scroll",
			"seed", "stone", "food", "bomb", "spell", "other"
	};

	public int categories = 0;
	public int enchanted = ANY;
	public int identified = ANY;
	public int cursed = ANY;
	public int upgradable = ANY;
	public int stackable = ANY;
	public BigInteger levelMin = null;
	public BigInteger levelMax = null;
	public BigInteger quantityMin = null;
	public BigInteger quantityMax = null;
	public Item.Rarity rarityMin = null;
	public int tierMin = 0;
	public String name = "";
	public boolean autoStore = false;

	public static int categoryOf(Item item) {
		if (item instanceof MissileWeapon) return 1;
		if (item instanceof Weapon) return 0;
		if (item instanceof Armor) return 2;
		if (item instanceof Wand) return 3;
		if (item instanceof Ring) return 4;
		if (item instanceof Artifact) return 5;
		if (item instanceof Potion) return 6;
		if (item instanceof Scroll) return 7;
		if (item instanceof Plant.Seed) return 8;
		if (item instanceof Runestone) return 9;
		if (item instanceof Food) return 10;
		if (item instanceof Bomb) return 11;
		if (item instanceof Spell) return 12;
		return 13;
	}

	public boolean isActive() {
		return categories != 0 || enchanted != ANY || identified != ANY || cursed != ANY
				|| upgradable != ANY || stackable != ANY
				|| levelMin != null || levelMax != null
				|| quantityMin != null || quantityMax != null
				|| rarityMin != null || tierMin > 0
				|| (name != null && !name.trim().isEmpty());
	}

	public void clear() {
		categories = 0;
		enchanted = identified = cursed = upgradable = stackable = ANY;
		levelMin = levelMax = quantityMin = quantityMax = null;
		rarityMin = null;
		tierMin = 0;
		name = "";
		autoStore = false;
	}

	public boolean hasCategory(int index) {
		return (categories & (1 << index)) != 0;
	}

	public void toggleCategory(int index) {
		categories ^= (1 << index);
	}

	private static int rank(Item.Rarity r) {
		if (r == null || r == Item.Rarity.NONE) return 0;
		return r.multiplier.intValue() + 1;
	}

	private static boolean tri(int mode, boolean value) {
		return mode == ANY || (mode == YES) == value;
	}

	public boolean matches(Item item) {
		if (item == null) return false;

		if (categories != 0 && !hasCategory(categoryOf(item))) return false;

		if (identified != ANY && !tri(identified, item.isIdentified())) return false;

		if (cursed != ANY) {
			if (!item.cursedKnown && !item.isIdentified()) return false;
			if (!tri(cursed, item.cursed)) return false;
		}

		if (enchanted != ANY) {
			boolean has;
			if (item instanceof Weapon) {
				has = ((Weapon) item).enchantment != null;
			} else if (item instanceof Armor) {
				has = ((Armor) item).glyph != null;
			} else {
				return false;
			}
			if (!item.isIdentified() && !item.cursedKnown) return false;
			if (!tri(enchanted, has)) return false;
		}

		if (upgradable != ANY && !tri(upgradable, item.isUpgradable())) return false;
		if (stackable != ANY && !tri(stackable, item.stackable)) return false;

		if (levelMin != null || levelMax != null) {
			if (!item.levelKnown) return false;
			BigInteger lvl = item.level();
			if (levelMin != null && lvl.compareTo(levelMin) < 0) return false;
			if (levelMax != null && lvl.compareTo(levelMax) > 0) return false;
		}

		if (quantityMin != null || quantityMax != null) {
			BigInteger q = item.quantity();
			if (quantityMin != null && q.compareTo(quantityMin) < 0) return false;
			if (quantityMax != null && q.compareTo(quantityMax) > 0) return false;
		}

		if (rarityMin != null && rank(item.rarity) < rank(rarityMin)) return false;

		if (tierMin > 0) {
			int tier;
			if (item instanceof Weapon) tier = ((Weapon) item).tier;
			else if (item instanceof Armor) tier = ((Armor) item).tier;
			else return false;
			if (tier < tierMin) return false;
		}

		if (name != null && !name.trim().isEmpty()) {
			String n = item.name();
			if (n == null || !n.toLowerCase().contains(name.trim().toLowerCase())) return false;
		}

		return true;
	}

	public static String plainRarityName(Item.Rarity r) {
		if (r == null) return "";
		return r.trueName.replaceAll("\\[[^\\]]*\\]", "");
	}

	private static final String CATEGORIES = "categories";
	private static final String ENCHANTED = "enchanted";
	private static final String IDENTIFIED = "identified";
	private static final String CURSED = "cursed";
	private static final String UPGRADABLE = "upgradable";
	private static final String STACKABLE = "stackable";
	private static final String LEVEL_MIN = "level_min";
	private static final String LEVEL_MAX = "level_max";
	private static final String QUANTITY_MIN = "quantity_min";
	private static final String QUANTITY_MAX = "quantity_max";
	private static final String RARITY_MIN = "rarity_min";
	private static final String TIER_MIN = "tier_min";
	private static final String NAME = "name";
	private static final String AUTO_STORE = "auto_store";

	private static void putBig(Bundle bundle, String key, BigInteger v) {
		bundle.put(key, v == null ? "" : v.toString());
	}

	private static BigInteger getBigOrNull(Bundle bundle, String key) {
		if (!bundle.contains(key)) return null;
		String s = bundle.getString(key);
		if (s == null || s.isEmpty()) return null;
		try {
			return new BigInteger(s.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		bundle.put(CATEGORIES, categories);
		bundle.put(ENCHANTED, enchanted);
		bundle.put(IDENTIFIED, identified);
		bundle.put(CURSED, cursed);
		bundle.put(UPGRADABLE, upgradable);
		bundle.put(STACKABLE, stackable);
		putBig(bundle, LEVEL_MIN, levelMin);
		putBig(bundle, LEVEL_MAX, levelMax);
		putBig(bundle, QUANTITY_MIN, quantityMin);
		putBig(bundle, QUANTITY_MAX, quantityMax);
		bundle.put(RARITY_MIN, rarityMin == null ? "" : rarityMin.name());
		bundle.put(TIER_MIN, tierMin);
		bundle.put(NAME, name == null ? "" : name);
		bundle.put(AUTO_STORE, autoStore);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		categories = bundle.getInt(CATEGORIES);
		enchanted = bundle.getInt(ENCHANTED);
		identified = bundle.getInt(IDENTIFIED);
		cursed = bundle.getInt(CURSED);
		upgradable = bundle.getInt(UPGRADABLE);
		stackable = bundle.getInt(STACKABLE);
		levelMin = getBigOrNull(bundle, LEVEL_MIN);
		levelMax = getBigOrNull(bundle, LEVEL_MAX);
		quantityMin = getBigOrNull(bundle, QUANTITY_MIN);
		quantityMax = getBigOrNull(bundle, QUANTITY_MAX);
		rarityMin = null;
		if (bundle.contains(RARITY_MIN)) {
			String r = bundle.getString(RARITY_MIN);
			if (r != null && !r.isEmpty()) {
				try {
					rarityMin = Item.Rarity.valueOf(r);
				} catch (IllegalArgumentException e) {
					rarityMin = null;
				}
			}
		}
		tierMin = bundle.getInt(TIER_MIN);
		name = bundle.contains(NAME) ? bundle.getString(NAME) : "";
		autoStore = bundle.getBoolean(AUTO_STORE);
	}
}
