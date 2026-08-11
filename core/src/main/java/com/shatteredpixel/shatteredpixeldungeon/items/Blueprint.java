/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTransmutation;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.Alchemize;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.Recycle;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.UnstableSpell;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class Blueprint extends Item {

	{
		image = ItemSpriteSheet.BLUEPRINT;
		defaultAction = AC_STORE;
		bones = true;
		stackable = false;
	}

	private static final String AC_STORE = "STORE";
	private static final String AC_BUILD = "BUILD";
	private static final String AC_RELEASE = "RELEASE";

	private static final String STORED = "stored";

	private final ArrayList<Item> storedItems = new ArrayList<>();

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_STORE);
		if (!storedItems.isEmpty()) {
			actions.add(AC_RELEASE);
		}
		if (storedItems.size() == 3) {
			actions.add(AC_BUILD);
		}
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);

		if (action.equals(AC_STORE)) {
			GameScene.selectItem(itemSelector);
		} else if (action.equals(AC_BUILD)) {
			build();
			hero.spendAndNext(Actor.TICK);
		} else if (action.equals(AC_RELEASE)) {
			release();
			hero.spendAndNext(Actor.TICK);
		}
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public String status() {
		return storedItems.isEmpty() ? null : Messages.format("%d/3", storedItems.size());
	}

	@Override
	public String name() {
		if (!storedItems.isEmpty()) {
			return Messages.get(this, "name_stored", storedItems.get(0).name());
		} else {
			return super.name();
		}
	}

	@Override
	public String info() {
		String info = super.info();
		if (!storedItems.isEmpty()) {
			info += "\n\n" + Messages.get(this, "stored_items", storedItems.size(), 3);
			for (Item item : storedItems) {
				info += "\n" + item.title();
			}
		}
		return info;
	}

	@Override
	public long value() {
		return 1500;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(STORED, storedItems);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		storedItems.clear();
		for (Bundlable item : bundle.getCollection(STORED)) {
			if (item != null) {
				storedItems.add((Item)item);
			}
		}
	}

	private final WndBag.ItemSelector itemSelector = new WndBag.ItemSelector() {

		@Override
		public String textPrompt() {
			return Messages.get(Blueprint.class, "prompt");
		}

		@Override
		public boolean itemSelectable(Item item) {
			if (item == null || item == Blueprint.this || item.isEquipped(Dungeon.hero)) {
				return false;
			}
			if (!item.isIdentified() || item.cursed || !item.isUpgradable()) {
				return false;
			}
			if (storedItems.isEmpty()) {
				return true;
			}
			return item.isSimilar(storedItems.get(0));
		}

		@Override
		public void onSelect(Item item) {
			if (item == null) {
				return;
			}

			if (storedItems.size() >= 3) {
				GLog.w(Messages.get(Blueprint.class, "full"));
				return;
			}

			if (!storedItems.isEmpty() && !item.isSimilar(storedItems.get(0))) {
				GLog.w(Messages.get(Blueprint.class, "unmatched"));
				return;
			}

			Item detached = item.detach(Dungeon.hero.belongings.backpack);
			if (detached == null) {
				GLog.w(Messages.get(Blueprint.class, "cant_store"));
				return;
			}

			storedItems.add(detached);
			GLog.i(Messages.get(Blueprint.class, "store", detached.name(), storedItems.size()));
			Item.updateQuickslot();
		}
	};

	private void build() {
		if (storedItems.size() < 3) {
			GLog.w(Messages.get(Blueprint.class, "not_enough"));
			return;
		}

		Item base = storedItems.get(0).duplicate();
		if (base == null) {
			GLog.w(Messages.get(Blueprint.class, "cant_build"));
			return;
		}

		long totalLevel = 0;
		for (Item item : storedItems) {
			totalLevel += item.trueLevel();
			item.quantity(0);
		}

		base.level(totalLevel);
		storedItems.clear();

		if (!base.collect()) {
			Dungeon.level.drop(base, Dungeon.hero.pos).sprite.drop();
		}

		GLog.i(Messages.get(this, "built", base.name()));
		detach(Dungeon.hero.belongings.backpack);
		Item.updateQuickslot();
	}

	private void release() {
		if (storedItems.isEmpty()) {
			return;
		}

		for (Item item : storedItems) {
			if (!item.collect()) {
				Dungeon.level.drop(item, Dungeon.hero.pos).sprite.drop();
			}
		}
		storedItems.clear();
		GLog.i(Messages.get(this, "released"));
		Item.updateQuickslot();
	}

	public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe.SimpleRecipe {

		private static final int OUT_QUANTITY = 1;

		{
			inputs =  new Class[]{Alchemize.class, Recycle.class, UnstableSpell.class};
			inQuantity = new int[]{1, 1, 1};

			cost = 15;

			output = Blueprint.class;
			outQuantity = OUT_QUANTITY;
		}

	}
}
