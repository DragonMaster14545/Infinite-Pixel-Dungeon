package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Amulet;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.ShulkerBox;
import com.shatteredpixel.shatteredpixeldungeon.items.VaultStore;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.Key;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndVaultLink;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;
public class VaultLink extends Artifact {

	public static final String AC_OPEN        = "OPEN";
	public static final String AC_DEPOSIT_ALL = "DEPOSIT_ALL";

	private static final int BASE_SLOTS = 6;
	private static final int SLOTS_PER_LEVEL = 2;

	{
		image = ItemSpriteSheet.BEACON;

		levelCap = 10;
		exp = 0;
		charge = 0;
		chargeCap = 0;

		defaultAction = AC_OPEN;
		unique = true;
	}

	public long slots() {
		long normal = BASE_SLOTS + SLOTS_PER_LEVEL * level();
		return Math.max(1, Math.round(normal * VaultStore.capacityPercent() / 100f));
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		if (!cursed) {
			actions.add(0, AC_OPEN);
			actions.add(AC_DEPOSIT_ALL);
		}
		return actions;
	}

	@Override
	public boolean isUpgradable() {
		return true;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);

		if (!(action.equals(AC_OPEN) || action.equals(AC_DEPOSIT_ALL))) return;

		if (cursed) {
			GLog.w(Messages.get(this, "cursed"));
		} else if (action.equals(AC_OPEN)) {
			GameScene.show(new WndVaultLink(this));
		} else {
			depositAll(hero);
		}
	}

	private String modeName() {
		return Messages.get(this, "mode_" + VaultStore.mode().name().toLowerCase());
	}

	public ArrayList<ShulkerBox> stored() {
		return new ArrayList<>(VaultStore.boxes());
	}

	public String refusal(Hero hero, Item item) {
		if (!(item instanceof ShulkerBox)) return "not_box";
		if (VaultStore.boxes().size() >= slots()) return "full";
		for (Item inside : ((ShulkerBox) item).contents()) {
			if (inside.unique || inside instanceof Key || inside instanceof Amulet) return "has_unique";
		}
		return null;
	}

	public void depositAll(Hero hero) {
		ArrayList<ShulkerBox> moved = new ArrayList<>();
		for (Item item : new ArrayList<>(hero.belongings.backpack.items)) {
			if (item instanceof ShulkerBox && refusal(hero, item) == null) {
				VaultStore.boxes().add((ShulkerBox) item);
				moved.add((ShulkerBox) item);
			}
		}
		if (moved.isEmpty()) {
			GLog.w(Messages.get(this, "nothing_to_deposit"));
			return;
		}
		if (!VaultStore.save()) {
			VaultStore.boxes().removeAll(moved);
			GLog.w(Messages.get(this, "write_failed"));
			return;
		}
		for (ShulkerBox b : moved) b.detachAll(hero.belongings.backpack);
		if (!VaultStore.sync()) {
			VaultStore.boxes().removeAll(moved);
			VaultStore.save();
			for (ShulkerBox b : moved) b.collect(hero.belongings.backpack);
			GLog.w(Messages.get(this, "write_failed"));
			return;
		}
		finishTurn(hero);
		GLog.i(Messages.get(this, "deposited_all", moved.size()));
	}

	public void startDeposit(final Hero hero, final Runnable after) {
		GameScene.selectItem(new WndBag.ItemSelector() {
			@Override
			public String textPrompt() {
				return Messages.get(VaultLink.this, "deposit_prompt");
			}

			@Override
			public Class<? extends Bag> preferredBag() {
				return Belongings.Backpack.class;
			}

			@Override
			public boolean itemSelectable(Item item) {
				return item instanceof ShulkerBox;
			}

			@Override
			public void onSelect(Item item) {
				if (item instanceof ShulkerBox) deposit(hero, (ShulkerBox) item);
				if (after != null) after.run();
			}
		});
	}

	public void deposit(Hero hero, ShulkerBox box) {
		String why = refusal(hero, box);
		if (why != null) {
			GLog.w(Messages.get(this, why, slots()));
			return;
		}

		ArrayList<ShulkerBox> list = VaultStore.boxes();
		list.add(box);
		if (!VaultStore.save()) {
			list.remove(box);
			GLog.w(Messages.get(this, "write_failed"));
			return;
		}

		box.detachAll(hero.belongings.backpack);
		if (!VaultStore.sync()) {
			list.remove(box);
			VaultStore.save();
			if (!box.collect(hero.belongings.backpack)) {
				Dungeon.level.drop(box, hero.pos);
			}
			GLog.w(Messages.get(this, "write_failed"));
			return;
		}

		finishTurn(hero);
		GLog.i(Messages.get(this, "deposited", box.name()));
	}
	
	public void withdraw(Hero hero, ShulkerBox box) {
		if (!VaultStore.boxes().contains(box)) {
			GLog.w(Messages.get(this, "already_taken"));
			return;
		}

		VaultStore.boxes().remove(box);
		if (!box.collect(hero.belongings.backpack)) {
			VaultStore.boxes().add(box);
			GLog.w(Messages.get(this, "pack_full"));
			return;
		}
		if (!VaultStore.save()) {
			VaultStore.boxes().add(box);
			box.detachAll(Dungeon.hero.belongings.backpack);
			GLog.w(Messages.get(this, "write_failed"));
			return;
		}
		if (!VaultStore.sync()) {
			VaultStore.boxes().add(box);
			box.detachAll(Dungeon.hero.belongings.backpack);
			VaultStore.save();
			GLog.w(Messages.get(this, "write_failed"));
			return;
		}

		finishTurn(hero);
		GLog.i(Messages.get(this, "withdrawn", box.name()));
	}

	private void finishTurn(Hero hero) {
		hero.sprite.operate(hero.pos);
		hero.spend(1f);
		hero.busy();
		Sample.INSTANCE.play(Assets.Sounds.ITEM);
		updateQuickslot();
	}

	@Override
	public String status() {
		return VaultStore.boxes().size() + "/" + slots();
	}

	@Override
	public String desc() {
		String desc = Messages.get(this, "desc", slots());
		if (cursed) return desc + "\n\n" + Messages.get(this, "desc_cursed");

		if (VaultStore.mode() != VaultStore.Mode.NORMAL) {
			desc += "\n\n" + Messages.get(this, "desc_separate", modeName());
			if (VaultStore.capacityPercent() < 100 || VaultStore.limitPercent() < 100) {
				desc += " " + Messages.get(this, "desc_nerf", VaultStore.capacityPercent(), VaultStore.limitPercent());
			}
		}
		ArrayList<ShulkerBox> stored = VaultStore.boxes();
		if (stored.isEmpty()) {
			desc += "\n\n" + Messages.get(this, "desc_empty");
		} else {
			desc += "\n\n" + Messages.get(this, "desc_stored", stored.size(), slots());
		}
		return desc;
	}

	@Override
	protected ArtifactBuff passiveBuff() {
		return new VaultBuff();
	}

	public class VaultBuff extends ArtifactBuff {
	}

	@Override
	public long value() {
		return 0;
	}
}
