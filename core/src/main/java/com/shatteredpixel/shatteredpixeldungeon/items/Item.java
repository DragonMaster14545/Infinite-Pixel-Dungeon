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

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Degrade;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.TargetedCell;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.Dart;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.TippedDart;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTextInput;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Item implements Bundlable {

	public enum Rarity {
//		ULTRA_RARE(0x8A2BE2, "UR", chance(95), multi(94)),
//		SUPREME(0x7FFF00, "S", chance(94), multi(93)),
//		FABLED(0xFFDAB9, "F", chance(93), multi(92)),
//		EXOTIC(0xFF8C00, "Ex", chance(92), multi(91)),
//		UNIQUE(0xFF69B4, "Un", chance(91), multi(90)),
//		PRESTIGIOUS(0x4682B4, "P", chance(90), multi(89)),
//		ELITE(0xADFF2F, "El", chance(89), multi(88)),
//		PHENOMENAL(0xFF6347, "Ph", chance(88), multi(87)),
//		ASTOUNDING(0xFF4500, "As", chance(87), multi(86)),
//		UNPARALLELED(0x32CD32, "Up", chance(86), multi(85)),
//		TRANSCENDENTAL(0xB22222, "Tr", chance(85), multi(84)),
//		SPECTACULAR(0xFFD700, "Sp", chance(84), multi(83)),
//		IMPERIAL(0xB8860B, "Im", chance(83), multi(82)),
//		REVERED(0xCD5C5C, "Re", chance(82), multi(81)),
//		NOBLE(0xDDA0DD, "N", chance(81), multi(80)),
//		DIVINITY(0xFFE4E1, "Di", chance(80), multi(79)),
//		EXTRAORDINARY(0xFF4500, "Ex", chance(79), multi(78)),
//		RENOWNED(0x7B68EE, "Ren", chance(78), multi(77)),
//		UNMATCHED(0xB0E0E6, "Um", chance(77), multi(76)),
//		SUPERIOR(0xFFB6C1, "Su", chance(76), multi(75)),
//		ILLUMINATED(0xB0C4DE, "Ill", chance(75), multi(74)),
//		EXCEPTIONAL(0xF08080, "Ex", chance(74), multi(73)),
//		UNSURPASSED(0xFFA07A, "U", chance(73), multi(72)),
//		PREEMINENT(0xDDA0DD, "Pre", chance(72), multi(71)),
//		VENERABLE(0xB22222, "Ve", chance(71), multi(70)),
//		WONDROUS(0xFFD700, "W", chance(70), multi(69)),
//		UNBELIEVABLE(0xB0E0E6, "U", chance(69), multi(68)),
//		BREATHTAKING(0xFF7F50, "Br", chance(68), multi(67)),
//		ILLUSORY(0xADD8E6, "Ill", chance(67), multi(66)),
//		SPECTRAL(0xFFDAB9, "Sp", chance(66), multi(65)),
//		MAGICAL(0xFF1493, "Ma", chance(65), multi(64)),
//		ASTROLOGICAL(0x800080, "Ast", chance(64), multi(63)),
//		COVENANTED(0xFF69B4, "Co", chance(63), multi(62)),
//		CELEBRATED(0x00FA9A, "Ce", chance(62), multi(61)),
//		TREASURED(0x32CD32, "T", chance(61), multi(60)),
		RADIANT(0xF0E68C, "R", chance(116), multi(59), "RADIANT"),
		STELLAR(0x87CEFA, "St", chance(114), multi(58), "STELLAR"),
		MAGNIFICENT(0xFFD700, "Mg", chance(112), multi(57), "MAGNIFICENT"),
		EXALTED(0xDAA520, "Ex", chance(110), multi(56), "EXALTED"),
		VALIANT(0xB0C4DE, "V", chance(108), multi(55), "VALIANT"),
		ETHEREAL(0x98FB98, "Et", chance(106), multi(54), "ETHEREAL"),
		BRILLIANT(0xFF8C00, "Br", chance(104), multi(53), "BRILLIANT"),
		IMPRESSIVE(0xF0E68C, "Im", chance(102), multi(52), "IMPRESSIVE"),
		INCREDIBLE(0xFFD700, "In", chance(100), multi(51), "INCREDIBLE"),
		CHERISHED(0xFF1493, "Ch", chance(98), multi(50), "CHERISHED"),
		SPLENDID(0x7FFF00, "Sp", chance(96), multi(49), "SPLENDID"),
		GLORIOUS(0xFF6347, "G", chance(94), multi(48), "GLORIOUS"),
		UNFORGETTABLE(0x6A5ACD, "U", chance(92), multi(47), "UNFORGETTABLE"),
		MAGNANIMOUS(0xD2691E, "M", chance(90), multi(46), "MAGNANIMOUS"),
		NOBILITY(0xCD5C5C, "N", chance(88), multi(45), "NOBILITY"),
		VISIONARY(0xFFB6C1, "V", chance(86), multi(44), "VISIONARY"),
		RESPLENDENT(0xFFD700, "Re", chance(84), multi(43), "RESPLENDENT"),
		MAGNETIC(0x8B008B, "M", chance(82), multi(42), "MAGNETIC"),
		FORTUITOUS(0xFF8C00, "F", chance(80), multi(41), "FORTUITOUS"),
		SUPERNATURAL(0x7B68EE, "S", chance(78), multi(40), "SUPERNATURAL"),
		GRANDIOSE(0xFFA07A, "Gr", chance(76), multi(39), "GRANDIOSE"),
		VALOROUS(0xB22222, "V", chance(74), multi(38), "VALOROUS"),
		HEROIC(0xB8860B, "H", chance(72), multi(37), "HEROIC"),
		RESILIENT(0x00FA9A, "R", chance(70), multi(36), "RESILIENT"),
		ILLUSIVE(0x20B2AA, "I", chance(68), multi(35), "ILLUSIVE"),
		CONQUEROR(0xFF6347, "C", chance(66), multi(34), "CONQUEROR"),
		ROGUE(0xCD853F, "R", chance(64), multi(33), "ROGUE"),
		SOVEREIGN(0x8A2BE2, "S", chance(62), multi(32), "SOVEREIGN"),
		ASCENDED(0xFFE4E1, "A", chance(60), multi(31), "ASCENDED"),
		TRANSLUCENT(0xB0E0E6, "T", chance(58), multi(30), "TRANSLUCENT"),
		MAGNIFIC(0xFF4500, "Mg", chance(56), multi(29), "MAGNIFIC"),
		UNSTOPPABLE(0xF08080, "U", chance(54), multi(28), "UNSTOPPABLE"),
		FORTIFIED(0xE6E6FA, "F", chance(52), multi(27), "FORTIFIED"),
		HEAVENLY(0xF0E68C, "H", chance(50), multi(26), "HEAVENLY"),
		LUMINOUS(0xB22222, "L", chance(48), multi(25), "LUMINOUS"),
		MYSTICAL(0x7FFF00, "My", chance(46), multi(24), "MYSTICAL"),
		EXQUISITE(0xFFD700, "Ex", chance(44), multi(23), "EXQUISITE"),
		BLAZING(0xFF4500, "Bl", chance(42), multi(22), "BLAZING"),
		COSMIC(0x8A2BE2, "C", chance(40), multi(21), "COSMIC"),
		TRANQUIL(0x87CEFA, "T", chance(38), multi(20), "TRANQUIL"),
		PRISTINE(0x98FB98, "Pr", chance(36), multi(19), "PRISTINE"),
		ULTIMATE(0xF5FFFA, "U", chance(34), multi(18), "ULTIMATE"),
		WHIMSICAL(0xFF00FF, "Wh", chance(32), multi(17), "WHIMSICAL"),
		HARMONIOUS(0x8FBC8F, "H", chance(30), multi(16), "HARMONIOUS"),
		OMNIPOTENT(0x9932CC, "O", chance(28), multi(15), "OMNIPOTENT"),
		ETERNAL(0xDC143C, "Et", chance(26), multi(14), "ETERNAL"),
		INFINITE(0x4682B4, "In", chance(24), multi(13), "INFINITE"),
		CELESTIAL(0x98FB98, "Ce", chance(22), multi(12), "CELESTIAL"),
		GALACTIC(0x20B2AA, "G", chance(20), multi(11), "GALACTIC"),
		IMMORTAL(0x4B0082, "I", chance(18), multi(10), "IMMORTAL"),
		TRANSCENDENT(0x8B4513, "T", chance(16), multi(9), "TRANSCENDENT"),
		DIVINE(0x87CEEB, "D", chance(14), multi(8), "DIVINE"),
		ANCIENT(0xFF1493, "A", chance(12), multi(7), "ANCIENT"),
		MYTHICAL(0x800080, "M", chance(10), multi(6), "MYTHICAL"),
		LEGENDARY(0xFF4500, "L", chance(8), multi(5), "LEGENDARY"),
		EPIC(0xFF00FF, "E", chance(6), multi(4), "EPIC"),
		RARE(0x0000FF, "R", chance(4), multi(3), "RARE"),
		UNCOMMON(0xFFFF00, "U", chance(2), multi(2), "UNCOMMON"),
		COMMON(0x00FF00, "C", chance(0), multi(1), "COMMON"),
		NONE(0xFFFFFF, " ", 0, 1, "");

		Rarity(int color, String name, double chance, double multiplier, String trueName) {
			this.color = color;
			this.name = name;
			this.chance = chance;
			this.multiplier = (long) multiplier;
			this.trueName = Messages.titleCase(trueName);
		}

		public final int color;
		public final String name;
		public final double chance;
		public final long multiplier;
		public final String trueName;
	}

	protected static final String TXT_TO_STRING_LVL		= "%s %+d";
	protected static final String TXT_TO_STRING_X		= "%s x%d";
	protected static final String TXT_TO_STRING_RARITY		= "[%s] %s";
	
	protected static final float TIME_TO_THROW		= 1.0f;
	protected static final float TIME_TO_PICK_UP	= 1.0f;
	protected static final float TIME_TO_DROP		= 1.0f;
	
	public static final String AC_DROP		= "DROP";
	public static final String AC_THROW		= "THROW";
	public static final String AC_RENAME = "RENAME";
	public static final String AC_AIM		= "AIM_MI";
	
	public String defaultAction;
	public boolean usesTargeting;
	public String customName = "";

	//TODO should these be private and accessed through methods?
	public int image = 0;
	public int icon = -1; //used as an identifier for items with randomized images
	
	public boolean stackable = false;
	protected long quantity = 1;
	public boolean dropsDownHeap = false;
	
	private long level = 0;
	public Rarity rarity = Rarity.NONE;
    public int emblemUse = 0;

	public boolean levelKnown = false;
	
	public boolean cursed;
	public boolean cursedKnown;
	
	// Unique items persist through revival
	public boolean unique = false;

	// These items are preserved even if the hero's inventory is lost via unblessed ankh
	// this is largely set by the resurrection window, items can override this to always be kept
	public boolean keptThoughLostInvent = false;

	// whether an item can be included in heroes remains
	public boolean bones = false;

	public boolean wereOofed = false;
	
	public static final Comparator<Item> itemComparator = new Comparator<Item>() {
		@Override
		public int compare( Item lhs, Item rhs ) {
			return Generator.Category.order( lhs ) - Generator.Category.order( rhs );
		}
	};
	
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = new ArrayList<>();
		actions.add( AC_DROP );
		actions.add( AC_THROW );
		return actions;
	}

	public void randomizeRarity() {
		rarity = Rarity.NONE;
		double random = Dungeon.Double(1.1, Dungeon.LuckDirection.DOWN);
		for (Rarity r : Rarity.values()) {
			if (random <= r.chance) {
				rarity = r;
				break;
			}
		}
	}

    public void randomizeCommonRarity() {
        rarity = Rarity.COMMON;
        double random = Dungeon.Double(Rarity.COMMON.chance, Dungeon.LuckDirection.DOWN);
        for (Rarity r : Rarity.values()) {
            if (random <= r.chance) {
                rarity = r;
                break;
            }
        }
    }

	public long getRarityMultiplier() {
		return rarity.multiplier;
	}

	public static float chance(float div) {
		return 0.95f / (div + 1);
	}

	public static double multi(long multi) {
		return 2 * multi;
	}

	public String actionName(String action, Hero hero){
		return Messages.get(this, "ac_" + action);
	}

	public boolean doPickUp(Hero hero) {
		return doPickUp( hero, hero.pos );
	}

	public boolean doPickUp(Hero hero, int pos) {
		return doPickUp( hero, hero.pos, TIME_TO_PICK_UP);
	}

	public boolean doPickUp(Hero hero, int pos, float time) {
		if (collect( hero.belongings.backpack )) {
			
			GameScene.pickUp( this, pos );
			Sample.INSTANCE.play( Assets.Sounds.ITEM );
			if (time > 0f)
				hero.spendAndNext( time );
			return true;
			
		} else {
			return false;
		}
	}
	
	public void doDrop( Hero hero ) {
		hero.spendAndNext(TIME_TO_DROP);
		int pos = hero.pos;
		Dungeon.level.drop(detachAll(hero.belongings.backpack), pos).sprite.drop(pos);
	}

	//resets an item's properties, to ensure consistency between runs
	public void reset(){
		keptThoughLostInvent = false;
	}

	public boolean keptThroughLostInventory(){
		return keptThoughLostInvent;
	}

	public void doThrow( Hero hero ) {
		GameScene.selectCell(thrower);
	}

	public void doAim( Hero hero ) {
		GameScene.selectCell(aimer);
	}
	public void execute( Hero hero, String action ) {

		GameScene.cancel();
		curUser = hero;
		curItem = this;
		
		if (action.equals( AC_DROP )) {
			
			if (hero.belongings.backpack.contains(this) || isEquipped(hero)) {
				doDrop(hero);
			}
			
		} else if (action.equals( AC_THROW )) {
			
			if (hero.belongings.backpack.contains(this) || isEquipped(hero)) {
				doThrow(hero);
			}
			
		} else if (action.equals( AC_RENAME )) {
			if (hero.belongings.backpack.contains(this) || isEquipped(hero)) {
				rename(curItem);
			}
		} else if (action.equals( AC_AIM )) {
			if (hero.belongings.backpack.contains(this) || isEquipped(hero)) {
				doAim(hero);
			}
		}
	}

	private void rename(Item item) {
		GameScene.show( new WndTextInput( "Rename","", item.customName, 100, false, "Rename", "Revert" ) {
			@Override
			public void onSelect( boolean positive, String text ) {
				if (text != null && positive && !text.equals(item.name())) {
					curItem.customName = text;
				} else {
					curItem.customName = "";
				}
			}

			@Override
			public void onBackPressed() {
				GLog.w("You didn't set a name for this.");
			}
		} );
	}

	//can be overridden if default action is variable
	public String defaultAction(){
		return defaultAction;
	}
	
	public void execute( Hero hero ) {
		String action = defaultAction();
		if (action != null) {
			execute(hero, defaultAction());
		}
	}
	
	protected void onThrow( int cell ) {
		Heap heap = Dungeon.level.drop( this, cell );
		if (!heap.isEmpty()) {
			heap.sprite.drop( cell );
		}
	}
	
	//takes two items and merges them (if possible)
	public Item merge( Item other ){
		if (isSimilar( other )){
			quantity += other.quantity;
			other.quantity = 0;
		}
		return this;
	}
	
	public boolean collect( Bag container ) {

		if (quantity <= 0){
			return true;
		}

		ArrayList<Item> items = container.items;

		if (items.contains( this )) {
			return true;
		}

		for (Item item:items) {
			if (item instanceof Bag && ((Bag)item).canHold( this )) {
				if (collect( (Bag)item )){
					return true;
				}
			}
		}

		if (!container.canHold(this)){
			return false;
		}
		
		if (stackable) {
			for (Item item:items) {
				if (isSimilar( item )) {
					item.merge( this );
					item.updateQuickslot();
					if (Dungeon.hero != null && Dungeon.hero.isAlive()) {
						Badges.validateItemLevelAquired( this );
						Talent.onItemCollected(Dungeon.hero, item);
						if (isIdentified()) Catalog.setSeen(getClass());
					}
					if (TippedDart.lostDarts > 0){
						Dart d = new Dart();
						d.quantity(TippedDart.lostDarts);
						TippedDart.lostDarts = 0;
						if (!d.collect()){
							//have to handle this in an actor as we can't manipulate the heap during pickup
							Actor.add(new Actor() {
								{ actPriority = VFX_PRIO; }
								@Override
								protected boolean act() {
									Dungeon.level.drop(d, Dungeon.hero.pos).sprite.drop();
									Actor.remove(this);
									return true;
								}
							});
						}
					}
					return true;
				}
			}
		}

		if (Dungeon.hero != null && Dungeon.hero.isAlive()) {
			Badges.validateItemLevelAquired( this );
			Talent.onItemCollected( Dungeon.hero, this );
			if (isIdentified()) Catalog.setSeen(getClass());
		}

		items.add( this );
		Dungeon.quickslot.replacePlaceholder(this);
		Collections.sort( items, itemComparator );
		updateQuickslot();
		return true;

	}
	
	public final boolean collect() {
		return collect( Dungeon.hero.belongings.backpack );
	}
	
	//returns a new item if the split was sucessful and there are now 2 items, otherwise null
	public Item split( long amount ){
		if (amount <= 0 || amount >= quantity()) {
			return null;
		} else {
			//pssh, who needs copy constructors?
			Item split = Reflection.newInstance(getClass());
			
			if (split == null){
				return null;
			}
			
			Bundle copy = new Bundle();
			this.storeInBundle(copy);
			split.restoreFromBundle(copy);
			split.quantity(amount);
			quantity -= amount;
			
			return split;
		}
	}

	private static final CellSelector.Listener aimer = new CellSelector.Listener() {
		@Override
		public void onSelect(Integer cell) {
			if (cell != null) {
				List<Integer> cells = curItem.aimTiles(cell);
				if (cells.isEmpty()) {
					GLog.w(Messages.get(curItem, "no_aim"));
				}
				for (int i : cells) {
					Dungeon.hero.sprite.parent.add(new TargetedCell(i, 0xFF0000));
				}
			}
		}

		@Override
		public String prompt() {
			return Messages.get(Item.class, "aim_prompt");
		}
	};

	public Item duplicate(){
		Item dupe = Reflection.newInstance(getClass());
		if (dupe == null){
			return null;
		}
		Bundle copy = new Bundle();
		this.storeInBundle(copy);
		dupe.restoreFromBundle(copy);
		return dupe;
	}

	public void remove(long amount){
		if (amount >= quantity()){
			detachAll(Dungeon.hero.belongings.backpack);
		} else {
			quantity(quantity() - amount);
		}
	}
	
	public final Item detach( Bag container ) {
		
		if (quantity <= 0) {
			
			return null;
			
		} else
		if (quantity == 1) {

			if (stackable){
				Dungeon.quickslot.convertToPlaceholder(this);
			}

			return detachAll( container );
			
		} else {
			
			
			Item detached = split(1);
			updateQuickslot();
			if (detached != null) detached.onDetach( );
			return detached;
			
		}
	}
	
	public final Item detachAll( Bag container ) {
		Dungeon.quickslot.clearItem( this );

		for (Item item : container.items) {
			if (item == this) {
				container.items.remove(this);
				item.onDetach();
				container.grabItems(); //try to put more items into the bag as it now has free space
				updateQuickslot();
				return this;
			} else if (item instanceof Bag) {
				Bag bag = (Bag)item;
				if (bag.contains( this )) {
					return detachAll( bag );
				}
			}
		}

		updateQuickslot();
		return this;
	}
	
	public boolean isSimilar( Item item ) {
		return getClass() == item.getClass() && rarity == item.rarity;
	}

	public void onDetach(){}

	//returns the true level of the item, ignoring all modifiers aside from upgrades
	public final long trueLevel(){
		return level;
	}

	//returns the persistant level of the item, only affected by modifiers which are persistent (e.g. curse infusion)
	public long level(){
		return level;
	}
	
	//returns the level of the item, after it may have been modified by temporary boosts/reductions
	//note that not all item properties should care about buffs/debuffs! (e.g. str requirement)
	public long buffedLvl(){
		//only the hero can be affected by Degradation
		if (Dungeon.hero != null && Dungeon.hero.buff( Degrade.class ) != null
			&& (isEquipped( Dungeon.hero ) || Dungeon.hero.belongings.contains( this ))) {
			return Degrade.reduceLevel(level());
		} else {
			return level();
		}
	}

	public void level(long value ){
		level = value;

		updateQuickslot();
	}
	
	public Item upgrade() {
		
		this.level++;

		updateQuickslot();
		
		return this;
	}
	
	final public Item upgrade(long n ) {
		if (n > 20){
			this.level += n - 20;
			for (long i=0; i < 20; i++) {
				upgrade();
			}
			updateQuickslot();
		} else {
			for (long i = 0; i < n; i++) {
				upgrade();
			}
		}
		
		return this;
	}
	
	public Item degrade() {
		
		this.level--;
		
		return this;
	}
	
	final public Item degrade(long n ) {
		if (n > 20){
			this.level -= n - 20;
			for (long i=0; i < 20; i++) {
				degrade();
			}
			updateQuickslot();
		} else {
			for (long i = 0; i < n; i++) {
				degrade();
			}
		}
		
		return this;
	}
	
	public long visiblyUpgraded() {
		return levelKnown ? level() : 0;
	}

	public long buffedVisiblyUpgraded() {
		return levelKnown ? buffedLvl() : 0;
	}
	
	public boolean visiblyCursed() {
		return cursed && cursedKnown;
	}
	
	public boolean isUpgradable() {
		return true;
	}
	
	public boolean isIdentified() {
		return levelKnown && cursedKnown;
	}
	
	public boolean isEquipped( Hero hero ) {
		return false;
	}

	public final Item identify(){
		return identify(true);
	}

	public Item identify( boolean byHero ) {

		if (byHero && Dungeon.hero != null && Dungeon.hero.isAlive()){
			Catalog.setSeen(getClass());
			if (!isIdentified()) Talent.onItemIdentified(Dungeon.hero, this);
		}

		levelKnown = true;
		cursedKnown = true;
		Item.updateQuickslot();
		
		return this;
	}
	
	public void onHeroGainExp( float levelPercent, Hero hero ){
		//do nothing by default
	}

	public boolean needsAim() {
		return false;
	}

	//A list of all tiles the aim button highlights.
	public List<Integer> aimTiles(int target) {
		Ballistica b = new Ballistica(Dungeon.hero.pos, target, Ballistica.WONT_STOP);
		return b.subPath(1, b.dist);
	}

	public static void evoke( Hero hero ) {
		hero.sprite.emitter().burst( Speck.factory( Speck.EVOKE ), 5 );
	}

	public String title() {

		String name = name();

		if (visiblyUpgraded() != 0)
			name = Messages.format( TXT_TO_STRING_LVL, name, visiblyUpgraded()  );

		if (quantity > 1)
			name = Messages.format( TXT_TO_STRING_X, name, quantity );

		if (rarity != Rarity.NONE && isIdentified())
			name = Messages.format( TXT_TO_STRING_RARITY, rarity.trueName, name);

		return name;

	}
	
	public String name() {
		if (this.customName.isEmpty()) {
			return trueName();
		}
		return this.customName;
	}
	
	public final String trueName() {
		return Messages.get(this, "name");
	}
	
	public int image() {
		return image;
	}
	
	public ItemSprite.Glowing glowing() {
		return null;
	}

	public Emitter emitter() { return null; }
	
	public String info() {

		if (Dungeon.hero != null) {
			Notes.CustomRecord note;
			if (this instanceof EquipableItem) {
				note = Notes.findCustomRecord(((EquipableItem) this).customNoteID);
			} else {
				note = Notes.findCustomRecord(getClass());
			}
			if (note != null){
				//we swap underscore(0x5F) with low macron(0x2CD) here to avoid highlighting in the item window
				return Messages.get(this, "custom_note", note.title().replace('_', 'ˍ')) + "\n\n" + desc();
			}
		}

		return desc();
	}
	
	public String desc() {
		return Messages.get(this, "desc");
	}
	
	public long quantity() {
		return quantity;
	}
	
	public Item quantity(long value ) {
		quantity = value;
		return this;
	}

	//item's value in gold coins
	public long value() {
		return 0;
	}

	//item's value in energy crystals
	public long energyVal() {
		return 0;
	}

	public final long sellPrice(){
		return value() * 5 * (Dungeon.depth / 5 + 1);
	}
	
	public Item virtual(){
		Item item = Reflection.newInstance(getClass());
		if (item == null) return null;
		
		item.quantity = 0;
		item.level = level;
		return item;
	}
	
	public Item random() {
		return this;
	}
	
	public String status() {
		return quantity != 1 ? Long.toString( quantity ) : null;
	}

	public static void updateQuickslot() {
		GameScene.updateItemDisplays = true;
	}
	
	private static final String QUANTITY		= "quantity";
	private static final String LEVEL			= "level";
	private static final String LEVEL_KNOWN		= "levelKnown";
	private static final String CURSED			= "cursed";
	private static final String CURSED_KNOWN	= "cursedKnown";
	private static final String QUICKSLOT		= "quickslotpos";
	private static final String KEPT_LOST       = "kept_lost";
	private static final String WERE_OOFED      = "were_oofed";
	private static final String RARITY           = "rarity";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		bundle.put( QUANTITY, quantity );
		bundle.put( LEVEL, level );
		bundle.put( LEVEL_KNOWN, levelKnown );
		bundle.put( CURSED, cursed );
		bundle.put( CURSED_KNOWN, cursedKnown );
		if (Dungeon.quickslot.contains(this)) {
			bundle.put( QUICKSLOT, Dungeon.quickslot.getSlot(this) );
		}
		bundle.put( KEPT_LOST, keptThoughLostInvent );
		bundle.put( WERE_OOFED, wereOofed);
		bundle.put( RARITY, rarity.ordinal() );

		if (!this.customName.equals("")) {
			bundle.put("customName", this.customName);
		}
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		quantity	= bundle.getLong( QUANTITY );
		levelKnown	= bundle.getBoolean( LEVEL_KNOWN );
		cursedKnown	= bundle.getBoolean( CURSED_KNOWN );
		wereOofed = bundle.getBoolean(WERE_OOFED);
		
		long level = bundle.getLong( LEVEL );
		if (level > 0) {
			upgrade( level );
		} else if (level < 0) {
			degrade( -level );
		}
		
		cursed	= bundle.getBoolean( CURSED );

		//only want to populate slot on first load.
		if (Dungeon.hero == null) {
			if (bundle.contains(QUICKSLOT)) {
				Dungeon.quickslot.setSlot(bundle.getInt(QUICKSLOT), this);
			}
		}

		if (bundle.contains("customName")) {
			this.customName = bundle.getString("customName");
		}

		keptThoughLostInvent = bundle.getBoolean( KEPT_LOST );
		rarity = Rarity.values()[bundle.getInt(RARITY)];
	}

	public int targetingPos( Hero user, int dst ){
		return throwPos( user, dst );
	}

	public int throwPos( Char user, int dst){
		return new Ballistica( user.pos, dst, Ballistica.PROJECTILE ).collisionPos;
	}

    public int throwPos( Hero user, int dst){
        return new Ballistica( user.pos, dst, Ballistica.PROJECTILE ).collisionPos;
    }

	public void throwSound(){
		Sample.INSTANCE.play(Assets.Sounds.MISS, 0.6f, 0.6f, 1.5f);
	}
	
	public void cast( final Hero user, final int dst ) {
		
		final int cell = throwPos( user, dst );
		user.sprite.zap( cell );
		user.busy();

		throwSound();

		Char enemy = Actor.findChar( cell );
		QuickSlotButton.target(enemy);
		
		final float delay = castDelay(user, dst);

		if (enemy != null) {
			((MissileSprite) user.sprite.parent.recycle(MissileSprite.class)).
					reset(user.sprite,
							enemy.sprite,
							this,
							new Callback() {
						@Override
						public void call() {
							curUser = user;
							Item i = Item.this.detach(user.belongings.backpack);
							if (i != null) i.onThrow(cell);
							if (curUser.heroClass == HeroClass.WARRIOR
									&& !(Item.this instanceof MissileWeapon)
									&& curUser.buff(Talent.ImprovisedProjectileCooldown.class) == null){
								if (enemy != null && enemy.alignment != curUser.alignment){
									Sample.INSTANCE.play(Assets.Sounds.HIT);
									Buff.affect(enemy, Blindness.class, 3);
									Buff.affect(curUser, Talent.ImprovisedProjectileCooldown.class, 40f);
								}
							}
							if (user.buff(Talent.LethalMomentumTracker.class) != null){
								user.buff(Talent.LethalMomentumTracker.class).detach();
								user.next();
							} else {
								user.spendAndNext(delay);
							}
						}
					});
		} else {
			((MissileSprite) user.sprite.parent.recycle(MissileSprite.class)).
					reset(user.sprite,
							cell,
							this,
							new Callback() {
						@Override
						public void call() {
							curUser = user;
							Item i = Item.this.detach(user.belongings.backpack);
							user.spend(delay);
							if (i != null) i.onThrow(cell);
							user.next();
						}
					});
		}
	}

    public void cast( final Char user, final int dst ) {

        final int cell = throwPos( user, dst );

        throwSound();

        Char enemy = Actor.findChar( cell );
        Item itLink = this;
        if (enemy != null) {

            ((MissileSprite) user.sprite.parent.recycle(MissileSprite.class)).
                    reset(user.sprite,
                            enemy.sprite,
                            this,
                            new Callback() {
                                @Override
                                public void call() {
                                    Dungeon.level.drop(itLink, cell).sprite.drop(cell);
                                    user.next();
                                }
                            });
        } else {
            ((MissileSprite) user.sprite.parent.recycle(MissileSprite.class)).
                    reset(user.sprite,
                            cell,
                            this,
                            new Callback() {
                                @Override
                                public void call() {
                                    Dungeon.level.drop(itLink, cell).sprite.drop(cell);
                                    user.next();
                                }
                            });
        }
    }
	
	public float castDelay( Char user, int dst ){
		return TIME_TO_THROW;
	}
	
	protected static Hero curUser = null;
	protected static Item curItem = null;
	protected static CellSelector.Listener thrower = new CellSelector.Listener() {
		@Override
		public void onSelect( Integer target ) {
			if (target != null) {
				curItem.cast( curUser, target );
			}
		}
		@Override
		public String prompt() {
			return Messages.get(Item.class, "prompt");
		}
	};

	public boolean canBeOofed(){return true;}
}
