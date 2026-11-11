package com.shatteredpixel.shatteredpixeldungeon.items;

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
		public long gold;
		public long energy;

		@Override
		public void storeInBundle( Bundle bundle ) {
			bundle.put( "item", item );
			bundle.put( "sold", sold );
			bundle.put( "gold", gold );
			bundle.put( "energy", energy );
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
			item = (Item) bundle.get( "item" );
			sold = bundle.getBoolean( "sold" );
			gold = bundle.getLong( "gold" );
			energy = bundle.getLong( "energy" );
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

		long elapsed = now() - stockedAt;
		if ( offers.isEmpty() || elapsed >= RESTOCK_MS || elapsed < 0 ) {
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
				if ( b.contains( SEED ) && b.getLong( SEED ) == cachedSeed ) {
					Collection<Bundlable> loaded = b.getCollection( OFFERS_KEY );
					for (Bundlable o : loaded) {
						if ( o instanceof Offer && ( (Offer) o ).item != null ) offers.add( (Offer) o );
					}
					stockedAt = b.getLong( STOCKED_AT );
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
				long value = Math.max( 1, offer.item.value() );
				offer.gold = Math.max( 35, value * 5 );
				offer.energy = Math.max( 5, offer.gold / 30 );
				offers.add( offer );
			}
		} finally {
			Random.popGenerator();
		}
		save();
	}

	private static Item rollItem() {
		// we sold clean items here of course, we are a trader....
		Item item = null;
		for (int attempt = 0; attempt < 8; attempt++) {
			Generator.Category cat = CATEGORIES[Random.chances( WEIGHTS )];
			item = Generator.randomUsingDefaults( cat );
			if ( item == null || item instanceof Gold ) continue;
			if ( item.cursed || item.level() < 0 || item.value() <= 0 ) continue;
			break;
		}
		if ( item == null || item instanceof Gold ) item = new Food();
		if ( item.isUpgradable() ) item.identify();
		return item;
	}

	public static String buy( Hero hero, int index, boolean withEnergy ) {
		if ( hero == null || !hero.isAlive() ) return "failed";

		ArrayList<Offer> list = offers();
		if ( index < 0 || index >= list.size() ) return "failed";
		Offer offer = list.get( index );
		if ( offer.sold ) return "sold_out";

		long cost = withEnergy ? offer.energy : offer.gold;
		if ( ( withEnergy ? Dungeon.energy : Dungeon.gold ) < cost ) {
			return withEnergy ? "no_energy" : "no_gold";
		}

		String name = offer.item.name();
		if ( !offer.item.collect( hero.belongings.backpack ) ) return "no_room";

		if ( withEnergy ) {
			Dungeon.energy -= cost;
		} else {
			Dungeon.gold -= cost;
		}
		offer.sold = true;
		save();

		GLog.p( Messages.get( TraderStock.class, "bought", name ) );
		return null;
	}
}
