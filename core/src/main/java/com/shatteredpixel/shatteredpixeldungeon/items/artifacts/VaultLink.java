package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;



import com.shatteredpixel.shatteredpixeldungeon.utils.Big;
import java.math.BigInteger;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Amulet;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.ShulkerBox;
import com.shatteredpixel.shatteredpixeldungeon.items.VaultStore;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Cheese;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.Key;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfMight;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfEarthblast;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFireblast;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLivingEarth;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndVaultLink;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
public class VaultLink extends Artifact {

	public static final String AC_OPEN        = "OPEN";
	public static final String AC_DEPOSIT_ALL = "DEPOSIT_ALL";

	private static final int BASE_SLOTS = 6;
	private static final int SLOTS_PER_LEVEL = 2;

	{
		image = ItemSpriteSheet.VAULT_LINK;

		levelCap = 10;
		exp = BigInteger.ZERO;
		charge = BigInteger.ZERO;
		chargeCap = BigInteger.ZERO;

		defaultAction = AC_OPEN;
		unique = true;
	}

	public static final BigInteger[] SHOP_LEVELS = {BigInteger.ZERO, BigInteger.valueOf(5), BigInteger.TEN, BigInteger.valueOf(20), BigInteger.valueOf(35), BigInteger.valueOf(50), BigInteger.valueOf(75), BigInteger.valueOf(100)};

	public static final BigInteger[] SLOT_PACKS = {BigInteger.ONE, BigInteger.valueOf(5), BigInteger.TEN, BigInteger.valueOf(25), BigInteger.valueOf(50), BigInteger.valueOf(100), BigInteger.valueOf(150), BigInteger.valueOf(200)};

	public BigInteger extraSlots() {
		return VaultStore.extraSlots();
	}

	public BigInteger packSize(BigInteger pack) {
		return BigInteger.ZERO.max(pack.min(BigInteger.valueOf(10000).subtract(VaultStore.extraSlots())));
	}

	public BigInteger slotGoldPrice(BigInteger pack) {
		long n = Big.toLong(VaultStore.extraSlots());
		long c = Big.toLong(packSize(pack));
		return BigInteger.valueOf(c * 2500 + 250 * (n * c + c * (c - 1) / 2));
	}

	public BigInteger slotEnergyPrice(BigInteger pack) {
		return BigInteger.valueOf(60).max(slotGoldPrice(pack).divide(BigInteger.valueOf(5)));
	}

	public String buySlots(Hero hero, BigInteger pack, boolean withEnergy) {
		if (cursed) return "cursed";
		if (hero == null || !hero.isAlive()) return "shop_failed";

		BigInteger count = packSize(pack);
		if ((count.compareTo(BigInteger.ZERO) <= 0)) return "shop_slots_max";

		BigInteger cost = withEnergy ? slotEnergyPrice(pack) : slotGoldPrice(pack);
		if (((withEnergy ? Dungeon.energy : Dungeon.gold).compareTo(cost) < 0)) {
			return withEnergy ? "shop_no_energy" : "shop_no_gold";
		}

		BigInteger before = VaultStore.extraSlots();
		VaultStore.setExtraSlots(before.add(count));
		if (!VaultStore.save()) {
			VaultStore.setExtraSlots(before);
			return "write_failed";
		}

		pay(withEnergy, cost);
		if (!VaultStore.sync()) {
			VaultStore.setExtraSlots(before);
			VaultStore.save();
			refund(withEnergy, cost);
			return "write_failed";
		}

		GLog.i(Messages.get(this, "shop_slots_bought", count, slots()));
		updateQuickslot();
		return null;
	}

	private int bought = 0;

	public int boughtCount() {
		return bought;
	}

	public BigInteger inflationPercent() {
		return BigInteger.valueOf(Math.min(1000, 5 * bought));
	}

	private BigInteger basePrice(BigInteger level) {
		return Big.trunc((Big.dec(BigInteger.valueOf(2500).add(BigInteger.valueOf(500).multiply(level))).add(Big.dec(Math.pow(25, 2)))));
	}

	private BigInteger inflate(BigInteger price) {
		return BigInteger.ONE.max(price.multiply((BigInteger.valueOf(100).add(inflationPercent()))).divide(BigInteger.valueOf(100)));
	}

	public BigInteger goldPrice(BigInteger level) {
		return inflate(basePrice(level));
	}

	public BigInteger energyPrice(BigInteger level) {
		return inflate(BigInteger.valueOf(60).max(basePrice(level).divide(BigInteger.valueOf(5))));
	}

	private void pay(boolean withEnergy, BigInteger cost) {
		if (withEnergy) Dungeon.energy = Dungeon.energy.subtract(cost);
		else Dungeon.gold = Dungeon.gold.subtract(cost);
	}

	private void refund(boolean withEnergy, BigInteger cost) {
		if (withEnergy) Dungeon.energy = Dungeon.energy.add(cost);
		else Dungeon.gold = Dungeon.gold.add(cost);
	}

	public String buy(Hero hero, BigInteger level, boolean withEnergy) {
		if (cursed) return "cursed";
		if (hero == null || !hero.isAlive()) return "shop_failed";

		level = BigInteger.ZERO.max(level.min(BigInteger.valueOf(ShulkerBox.MAX_LEVEL)));
		BigInteger cost = withEnergy ? energyPrice(level) : goldPrice(level);
		if (((withEnergy ? Dungeon.energy : Dungeon.gold).compareTo(cost) < 0)) {
			return withEnergy ? "shop_no_energy" : "shop_no_gold";
		}

		ShulkerBox box = new ShulkerBox();
		box.level(BigInteger.valueOf(Big.toInt(level)));

		if (box.collect(hero.belongings.backpack)) {
			pay(withEnergy, cost);
			bought++;
			GLog.i(Messages.get(this, "shop_bought_pack", box.name()));
			updateQuickslot();
			return null;
		}

		if ((BigInteger.valueOf(VaultStore.boxes().size()).compareTo(slots()) >= 0)) return "shop_no_room";

		VaultStore.boxes().add(box);
		if (!VaultStore.save()) {
			VaultStore.boxes().remove(box);
			return "write_failed";
		}

		pay(withEnergy, cost);
		bought++;
		if (!VaultStore.sync()) {
			VaultStore.boxes().remove(box);
			VaultStore.save();
			refund(withEnergy, cost);
			bought--;
			return "write_failed";
		}

		GLog.i(Messages.get(this, "shop_bought_vault", box.name()));
		updateQuickslot();
		return null;
	}

	private static final String BOUGHT = "bought";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(BOUGHT, bought);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		bought = bundle.getInt(BOUGHT);
	}

	public BigInteger slots() {
		BigInteger normal = BigInteger.valueOf(BASE_SLOTS).add(BigInteger.valueOf(SLOTS_PER_LEVEL).multiply(level()));
		return BigInteger.ONE.max(Big.round(Big.div(Big.dec(normal.multiply(BigInteger.valueOf(VaultStore.capacityPercent()))), Big.dec(100f)))).add(VaultStore.extraSlots());
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
		if ((BigInteger.valueOf(VaultStore.boxes().size()).compareTo(slots()) >= 0)) return "full";
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

		int pinnedBefore = VaultStore.pinnedCount();
		int at = VaultStore.take(box);
		if (!box.collect(hero.belongings.backpack)) {
			VaultStore.putBack(box, at, pinnedBefore);
			GLog.w(Messages.get(this, "pack_full"));
			return;
		}
		if (!VaultStore.save()) {
			VaultStore.putBack(box, at, pinnedBefore);
			box.detachAll(Dungeon.hero.belongings.backpack);
			GLog.w(Messages.get(this, "write_failed"));
			return;
		}
		if (!VaultStore.sync()) {
			VaultStore.putBack(box, at, pinnedBefore);
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
		if (Badges.isUnlocked( Badges.Badge.KIT_UNLOCKED )) desc += "\n\n" + Messages.get(this, "desc_lore");
		if (cursed) return desc + "\n\n" + Messages.get(this, "desc_cursed");

		if (Dungeon.level != null || Dungeon.hero != null) {
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
		}
		return desc;
	}

	@Override
	public int tint() {
		int color;

		if (Dungeon.level == null || Dungeon.hero == null) {
			return -1;
		} else {
			if (VaultStore.mode() == VaultStore.Mode.NORMAL) {
				color = 0xFFFFFF;
			} else if (VaultStore.mode() == VaultStore.Mode.DAILY) {
				color = 0x007bff;
			} else if (VaultStore.mode() == VaultStore.Mode.WEEKLY || VaultStore.mode() == VaultStore.Mode.CHALLENGE) {
				color = 0xffff00;
			} else {
				color = 0x39d600;
			}
		}
		
		int r = (color >> 16) & 0xFF, g = (color >> 8) & 0xFF, b = color & 0xFF;
		r += (255 - r) / 4;
		g += (255 - g) / 4;
		b += (255 - b) / 4;
		return (r << 16) | (g << 8) | b;
	}

	@Override
	protected ArtifactBuff passiveBuff() {
		return new VaultBuff();
	}

	public class VaultBuff extends ArtifactBuff {
	}

	@Override
	public BigInteger value() {
		return BigInteger.ZERO;
	}

	public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe {

		@Override
		public boolean testIngredients(ArrayList<Item> ingredients) {
			boolean kit = false;
			boolean shulker = false;
			boolean scroll = false;
			boolean luck = false;

			if (Badges.isUnlocked(Badges.Badge.KIT_UNLOCKED)) kit = true;

			for (Item ingredient : ingredients){
				if ((ingredient.quantity().compareTo(BigInteger.ZERO) > 0)) {
					if (ingredient instanceof ShulkerBox) {
						shulker = ((ShulkerBox) ingredient).isEmpty();
					} else if (ingredient instanceof ScrollOfUpgrade) {
						scroll = true;
					} else if (ingredient instanceof ElixirOfMight) {
						luck = true;
					}
				}
			}

			return kit && shulker && scroll && luck;
		}

		@Override
		public BigInteger cost(ArrayList<Item> ingredients) {
			return BigInteger.ONE;
		}

		@Override
		public Item brew(ArrayList<Item> ingredients) {
			if (!testIngredients(ingredients)) return null;

			for (Item ingredient : ingredients){
				ingredient.quantity(ingredient.quantity().subtract(BigInteger.ONE));
			}

			return sampleOutput(null);
		}

		@Override
		public Item sampleOutput(ArrayList<Item> ingredients) {
			return new VaultLink().identify();
		}
	}
}
