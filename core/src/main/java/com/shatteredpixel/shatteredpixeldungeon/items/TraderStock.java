package com.shatteredpixel.shatteredpixeldungeon.items;



import com.shatteredpixel.shatteredpixeldungeon.items.RubyBank;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.utils.Big;
import java.math.BigInteger;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.FileUtils;
import com.watabou.utils.Random;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class TraderStock {

	public static final long RESTOCK_MS = 10 * 60 * 1000L;
	public static final int OFFERS = 24;
	public static class Offer implements Bundlable {
		public Item item;
		public boolean sold;
		public BigInteger gold = BigInteger.ZERO;
		public BigInteger energy = BigInteger.ZERO;
		public BigInteger ruby = BigInteger.ZERO;

		@Override
		public void storeInBundle( Bundle bundle ) {
			bundle.put( "item", item );
			bundle.put( "sold", sold );
			Big.put(bundle, "gold", gold);
			Big.put(bundle, "energy", energy);
			Big.put(bundle, "ruby", ruby);
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
			item = (Item) bundle.get( "item" );
			sold = bundle.getBoolean( "sold" );
			gold = Big.getBig(bundle, "gold");
			energy = Big.getBig(bundle, "energy");
			ruby = bundle.contains("ruby") ? Big.getBig(bundle, "ruby") : BigInteger.ZERO;
		}
	}

	private static final String OFFERS_KEY = "offers";
	private static final String STOCKED_AT = "stocked_at";
	private static final String SEED = "seed";

	private static ArrayList<Offer> offers = null;
	private static long stockedAt = 0;
	private static int cachedSlot = -1;
	private static long cachedSeed = 0;

	private static String fileName( int slot ) {
		return "trader_" + slot + ".dat";
	}

	private static long now() {
		return System.currentTimeMillis();
	}

	public static boolean ensureCurrent() {
		if ( offers == null || cachedSlot != GamesInProgress.curSlot || cachedSeed != Dungeon.seed ) {
			load();
		}

		boolean noDeals = true;
		for (Offer o : offers) if ( o.ruby.signum() > 0 ) noDeals = false;
		long elapsed = now() - stockedAt;
		if ( offers.isEmpty() || noDeals || elapsed >= RESTOCK_MS || elapsed < 0 ) {
			restock();
			return true;
		}
		return false;
	}

	public static long msLeft() {
		return Math.max( 0, Math.min( RESTOCK_MS, stockedAt + RESTOCK_MS - now() ) );
	}

	public static ArrayList<Offer> offers() {
		ensureCurrent();
		return offers;
	}

	private static void load() {
		cachedSlot = GamesInProgress.curSlot;
		cachedSeed = Dungeon.seed;
		offers = new ArrayList<>();
		stockedAt = 0;

		String file = fileName( cachedSlot );
		if ( FileUtils.fileExists( file ) ) {
			try {
				Bundle b = FileUtils.bundleFromFile( file );
				if (b.contains( SEED ) && (Big.getBig(b, SEED).compareTo(BigInteger.valueOf(cachedSeed)) == 0)) {
					Collection<Bundlable> loaded = b.getCollection( OFFERS_KEY );
					for (Bundlable o : loaded) {
						if ( o instanceof Offer && ( (Offer) o ).item != null ) offers.add( (Offer) o );
					}
					stockedAt = Big.toLong(Big.getBig(b, STOCKED_AT));
				}
			} catch (IOException ignored) {
			}
		}
	}

	public static boolean save() {
		if ( offers == null ) return true;
		try {
			Bundle b = new Bundle();
			b.put( OFFERS_KEY, offers );
			b.put( STOCKED_AT, stockedAt );
			b.put( SEED, cachedSeed );
			FileUtils.bundleToFile( fileName( cachedSlot ), b );
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	private static final Generator.Category[] CATEGORIES = {
			Generator.Category.WEAPON, Generator.Category.ARMOR, Generator.Category.MISSILE,
			Generator.Category.WAND, Generator.Category.RING,
			Generator.Category.FOOD, Generator.Category.POTION, Generator.Category.SCROLL,
			Generator.Category.SEED, Generator.Category.STONE,
			Generator.Category.TUBES, Generator.Category.MODULE
	};
	private static final float[] WEIGHTS = { 3, 2, 2, 1, 1, 3, 5, 5, 2, 2, 1, 1 };

	private static void restock() {
		stockedAt = now();
		offers = new ArrayList<>();

		Random.pushGenerator( cachedSeed ^ ( stockedAt * 0x9E3779B97F4A7C15L ) );
		try {
			for (int i = 0; i < OFFERS; i++) {
				Offer offer = new Offer();
				offer.item = rollItem();
				BigInteger value = BigInteger.ONE.max(offer.item.value());
				offer.gold = BigInteger.valueOf(35).max(value.multiply(BigInteger.valueOf(5)));
				offer.energy = BigInteger.valueOf(5).max(offer.gold.divide(BigInteger.valueOf(30)));
				offers.add( offer );
				}
				offers.addAll( 0, rubyDeals() );
		} finally {
			Random.popGenerator();
		}
		save();
	}

		private static ArrayList<Offer> rubyDeals() {
			ArrayList<Offer> deals = new ArrayList<>();
			Generator.Category[][] kinds = {
					{ Generator.Category.POTION },
					{ Generator.Category.SCROLL },
					{ Generator.Category.WEAPON, Generator.Category.ARMOR, Generator.Category.WAND, Generator.Category.RING },
					{ Generator.Category.FOOD, Generator.Category.SEED, Generator.Category.STONE },
					{ Generator.Category.ARTIFACT, Generator.Category.TUBES }
			};
			int[] sizes = { 10, 10, 5, 8, 10 };
			int[] prices = { 25, 25, 40, 10, 30 };
			for (int k = 0; k < kinds.length; k++) {
				ShulkerBox box = new ShulkerBox();
				int level = Math.min( 5, Dungeon.depth / 5 );
				if ( level > 0 ) box.upgrade( BigInteger.valueOf( level ) );
				for (int n = 0; n < sizes[k]; n++) {
					Item item = null;
					for (int attempt = 0; attempt < 8; attempt++) {
						Generator.Category cat = kinds[k][Random.Int( kinds[k].length )];
						Item roll = Generator.randomUsingDefaults( cat );
						if ( roll == null || roll instanceof Gold ) continue;
						if ( roll.cursed || roll.level().signum() < 0 || roll.value().signum() <= 0 ) continue;
						item = roll;
						break;
					}
					if ( item == null ) continue;
					if ( item.isUpgradable() || item instanceof Artifact ) item.identify();
					box.contents.add( item );
				}
				Offer offer = new Offer();
				offer.item = box;
				offer.ruby = BigInteger.valueOf( prices[k] + 2L * level );
				deals.add( offer );
			}
			return deals;
		}
	
		private static Item rollItem() {
		// we sold clean items here of course, we are a trader....
		Item item = null;
		boolean clean = false;
		for (int attempt = 0; attempt < 8 && !clean; attempt++) {
			Generator.Category cat = CATEGORIES[Random.chances( WEIGHTS )];
			item = Generator.randomUsingDefaults( cat );
			if ( item == null || item instanceof Gold ) continue;
			if (item.cursed || (item.level().compareTo(BigInteger.ZERO) < 0) || (item.value().compareTo(BigInteger.ZERO) <= 0)) continue;
			clean = true;
		}
		if ( !clean ) item = new Food();
		if ( item.isUpgradable() ) item.identify();
		return item;
	}

	public static String buy( Hero hero, Offer offer, boolean withEnergy ) {
		if ( hero == null || !hero.isAlive() ) return "failed";

		ArrayList<Offer> list = offers();
		if ( offer == null || !list.contains( offer ) ) return "failed";
		if ( offer.sold ) return "sold_out";
		if ( offer.ruby.signum() > 0 ) {
		if ( RubyBank.get().compareTo( offer.ruby ) < 0 ) return "no_ruby";
		String boxName = offer.item.name();
		if ( !offer.item.collect( hero.belongings.backpack ) ) return "no_room";
		RubyBank.spend( offer.ruby );
		offer.sold = true;
		save();
		GLog.p( Messages.get( TraderStock.class, "bought", boxName ) );
		return null;
		}

		BigInteger cost = withEnergy ? offer.energy : offer.gold;
		if ((( withEnergy ? Dungeon.energy : Dungeon.gold ).compareTo(cost) < 0)) {
			return withEnergy ? "no_energy" : "no_gold";
		}

		String name = offer.item.name();
		if ( !offer.item.collect( hero.belongings.backpack ) ) return "no_room";

		if ( withEnergy ) {
			Dungeon.energy = Dungeon.energy.subtract(cost);
		} else {
			Dungeon.gold = Dungeon.gold.subtract(cost);
		}
		offer.sold = true;
		save();

		GLog.p( Messages.get( TraderStock.class, "bought", name ) );
		return null;
	}
}
