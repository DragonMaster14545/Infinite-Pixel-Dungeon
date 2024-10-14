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
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Item implements Bundlable {

	public enum Rarity {
//		ULTRA_RARE(0x8A2BE2, "UR", Math.pow(0.1,95), Math.pow(2,94)),
//		SUPREME(0x7FFF00, "S", Math.pow(0.1,94), Math.pow(2,93)),
//		FABLED(0xFFDAB9, "F", Math.pow(0.1,93), Math.pow(2,92)),
//		EXOTIC(0xFF8C00, "Ex", Math.pow(0.1,92), Math.pow(2,91)),
//		UNIQUE(0xFF69B4, "Un", Math.pow(0.1,91), Math.pow(2,90)),
//		PRESTIGIOUS(0x4682B4, "P", Math.pow(0.1,90), Math.pow(2,89)),
//		ELITE(0xADFF2F, "El", Math.pow(0.1,89), Math.pow(2,88)),
//		PHENOMENAL(0xFF6347, "Ph", Math.pow(0.1,88), Math.pow(2,87)),
//		ASTOUNDING(0xFF4500, "As", Math.pow(0.1,87), Math.pow(2,86)),
//		UNPARALLELED(0x32CD32, "Up", Math.pow(0.1,86), Math.pow(2,85)),
//		TRANSCENDENTAL(0xB22222, "Tr", Math.pow(0.1,85), Math.pow(2,84)),
//		SPECTACULAR(0xFFD700, "Sp", Math.pow(0.1,84), Math.pow(2,83)),
//		IMPERIAL(0xB8860B, "Im", Math.pow(0.1,83), Math.pow(2,82)),
//		REVERED(0xCD5C5C, "Re", Math.pow(0.1,82), Math.pow(2,81)),
//		NOBLE(0xDDA0DD, "N", Math.pow(0.1,81), Math.pow(2,80)),
//		DIVINITY(0xFFE4E1, "Di", Math.pow(0.1,80), Math.pow(2,79)),
//		EXTRAORDINARY(0xFF4500, "Ex", Math.pow(0.1,79), Math.pow(2,78)),
//		RENOWNED(0x7B68EE, "Ren", Math.pow(0.1,78), Math.pow(2,77)),
//		UNMATCHED(0xB0E0E6, "Um", Math.pow(0.1,77), Math.pow(2,76)),
//		SUPERIOR(0xFFB6C1, "Su", Math.pow(0.1,76), Math.pow(2,75)),
//		ILLUMINATED(0xB0C4DE, "Ill", Math.pow(0.1,75), Math.pow(2,74)),
//		EXCEPTIONAL(0xF08080, "Ex", Math.pow(0.1,74), Math.pow(2,73)),
//		UNSURPASSED(0xFFA07A, "U", Math.pow(0.1,73), Math.pow(2,72)),
//		PREEMINENT(0xDDA0DD, "Pre", Math.pow(0.1,72), Math.pow(2,71)),
//		VENERABLE(0xB22222, "Ve", Math.pow(0.1,71), Math.pow(2,70)),
//		WONDROUS(0xFFD700, "W", Math.pow(0.1,70), Math.pow(2,69)),
//		UNBELIEVABLE(0xB0E0E6, "U", Math.pow(0.1,69), Math.pow(2,68)),
//		BREATHTAKING(0xFF7F50, "Br", Math.pow(0.1,68), Math.pow(2,67)),
//		ILLUSORY(0xADD8E6, "Ill", Math.pow(0.1,67), Math.pow(2,66)),
//		SPECTRAL(0xFFDAB9, "Sp", Math.pow(0.1,66), Math.pow(2,65)),
//		MAGICAL(0xFF1493, "Ma", Math.pow(0.1,65), Math.pow(2,64)),
//		ASTROLOGICAL(0x800080, "Ast", Math.pow(0.1,64), Math.pow(2,63)),
//		COVENANTED(0xFF69B4, "Co", Math.pow(0.1,63), Math.pow(2,62)),
//		CELEBRATED(0x00FA9A, "Ce", Math.pow(0.1,62), Math.pow(2,61)),
//		TREASURED(0x32CD32, "T", Math.pow(0.1,61), Math.pow(2,60)),
		RADIANT(0xF0E68C, "R", Math.pow(0.1,60), Math.pow(2,59)),
		STELLAR(0x87CEFA, "St", Math.pow(0.1,59), Math.pow(2,58)),
		MAGNIFICENT(0xFFD700, "Mg", Math.pow(0.1,58), Math.pow(2,57)),
		EXALTED(0xDAA520, "Ex", Math.pow(0.1,57), Math.pow(2,56)),
		VALIANT(0xB0C4DE, "V", Math.pow(0.1,56), Math.pow(2,55)),
		ETHEREAL(0x98FB98, "Et", Math.pow(0.1,55), Math.pow(2,54)),
		BRILLIANT(0xFF8C00, "Br", Math.pow(0.1,54), Math.pow(2,53)),
		IMPRESSIVE(0xF0E68C, "Im", Math.pow(0.1,53), Math.pow(2,52)),
		INCREDIBLE(0xFFD700, "In", Math.pow(0.1,52), Math.pow(2,51)),
		CHERISHED(0xFF1493, "Ch", Math.pow(0.1,51), Math.pow(2,50)),
		SPLENDID(0x7FFF00, "Sp", Math.pow(0.1,50), Math.pow(2,49)),
		GLORIOUS(0xFF6347, "G", Math.pow(0.1,49), Math.pow(2,48)),
		UNFORGETTABLE(0x6A5ACD, "U", Math.pow(0.1,48), Math.pow(2,47)),
		MAGNANIMOUS(0xD2691E, "M", Math.pow(0.1,47), Math.pow(2,46)),
		NOBILITY(0xCD5C5C, "N", Math.pow(0.1,46), Math.pow(2,45)),
		VISIONARY(0xFFB6C1, "V", Math.pow(0.1,45), Math.pow(2,44)),
		RESPLENDENT(0xFFD700, "Re", Math.pow(0.1,44), Math.pow(2,43)),
		MAGNETIC(0x8B008B, "M", Math.pow(0.1,43), Math.pow(2,42)),
		FORTUITOUS(0xFF8C00, "F", Math.pow(0.1,42), Math.pow(2,41)),
		SUPERNATURAL(0x7B68EE, "S", Math.pow(0.1,41), Math.pow(2,40)),
		GRANDIOSE(0xFFA07A, "Gr", Math.pow(0.1,40), Math.pow(2,39)),
		VALOROUS(0xB22222, "V", Math.pow(0.1,39), Math.pow(2,38)),
		HEROIC(0xB8860B, "H", Math.pow(0.1,38), Math.pow(2,37)),
		RESILIENT(0x00FA9A, "R", Math.pow(0.1,37), Math.pow(2,36)),
		ILLUSIVE(0x20B2AA, "I", Math.pow(0.1,36), Math.pow(2,35)),
		CONQUEROR(0xFF6347, "C", Math.pow(0.1,35), Math.pow(2,34)),
		ROGUE(0xCD853F, "R", Math.pow(0.1,34), Math.pow(2,33)),
		SOVEREIGN(0x8A2BE2, "S", Math.pow(0.1,33), Math.pow(2,32)),
		ASCENDED(0xFFE4E1, "A", Math.pow(0.1,32), Math.pow(2,31)),
		TRANSLUCENT(0xB0E0E6, "T", Math.pow(0.1,31), Math.pow(2,30)),
		MAGNIFIC(0xFF4500, "Mg", Math.pow(0.1,30), Math.pow(2,29)),
		UNSTOPPABLE(0xF08080, "U", Math.pow(0.1,29), Math.pow(2,28)),
		FORTIFIED(0xE6E6FA, "F", Math.pow(0.1,28), Math.pow(2,27)),
		HEAVENLY(0xF0E68C, "H", Math.pow(0.1,27), Math.pow(2,26)),
		LUMINOUS(0xB22222, "L", Math.pow(0.1,26), Math.pow(2,25)),
		MYSTICAL(0x7FFF00, "My", Math.pow(0.1,25), Math.pow(2,24)),
		EXQUISITE(0xFFD700, "Ex", Math.pow(0.1,24), Math.pow(2,23)),
		BLAZING(0xFF4500, "Bl", Math.pow(0.1,23), Math.pow(2,22)),
		COSMIC(0x8A2BE2, "C", Math.pow(0.1,22), Math.pow(2,21)),
		TRANQUIL(0x87CEFA, "T", Math.pow(0.1,21), Math.pow(2,20)),
		PRISTINE(0x98FB98, "Pr", Math.pow(0.1,20), Math.pow(2,19)),
		ULTIMATE(0xF5FFFA, "U", Math.pow(0.1,19), Math.pow(2,18)),
		WHIMSICAL(0xFF00FF, "Wh", Math.pow(0.1,18), Math.pow(2,17)),
		HARMONIOUS(0x8FBC8F, "H", Math.pow(0.1,17), Math.pow(2,16)),
		OMNIPOTENT(0x9932CC, "O", Math.pow(0.1,16), Math.pow(2,15)),
		ETERNAL(0xDC143C, "Et", Math.pow(0.1,15), Math.pow(2,14)),
		INFINITE(0x4682B4, "In", Math.pow(0.1,14), Math.pow(2,13)),
		CELESTIAL(0x98FB98, "Ce", Math.pow(0.1,13), Math.pow(2,12)),
		GALACTIC(0x20B2AA, "G", Math.pow(0.1,12), Math.pow(2,11)),
		IMMORTAL(0x4B0082, "I", Math.pow(0.1,11), Math.pow(2,10)),
		TRANSCENDENT(0x8B4513, "T", Math.pow(0.1,10), Math.pow(2,9)),
		DIVINE(0x87CEEB, "D", Math.pow(0.1,9), Math.pow(2,8)),
		ANCIENT(0xFF1493, "A", Math.pow(0.1,8), Math.pow(2,7)),
		MYTHICAL(0x800080, "M", Math.pow(0.1,7), Math.pow(2,6)),
		LEGENDARY(0xFF4500, "L", Math.pow(0.1,6), Math.pow(2,5)),
		EPIC(0xFF00FF, "E", Math.pow(0.1,5), Math.pow(2,4)),
		RARE(0x0000FF, "R", Math.pow(0.1,4), Math.pow(2,3)),
		UNCOMMON(0xFFFF00, "U", Math.pow(0.1,3), Math.pow(2,2)),
		COMMON(0x00FF00, "C", 0.05f, Math.pow(2,1)),
		NONE(0xFFFFFF, " ", 0, 1);

		Rarity(int color, String name, double chance, double multiplier) {
			this.color = color;
			this.name = name;
			this.chance = chance;
			this.multiplier = (long) multiplier;
		}

		public final int color;
		public final String name;
		public final double chance;
		public final long multiplier;
	}

	protected static final String TXT_TO_STRING_LVL		= "%s %+d";
	protected static final String TXT_TO_STRING_X		= "%s x%d";
	
	protected static final float TIME_TO_THROW		= 1.0f;
	protected static final float TIME_TO_PICK_UP	= 1.0f;
	protected static final float TIME_TO_DROP		= 1.0f;
	
	public static final String AC_DROP		= "DROP";
	public static final String AC_THROW		= "THROW";
	
	public String defaultAction;
	public boolean usesTargeting;

	//TODO should these be private and accessed through methods?
	public int image = 0;
	public int icon = -1; //used as an identifier for items with randomized images
	
	public boolean stackable = false;
	protected long quantity = 1;
	public boolean dropsDownHeap = false;
	
	private long level = 0;
	public Rarity rarity = Rarity.NONE;

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
		float random = Dungeon.Float(1, Dungeon.LuckDirection.DOWN);
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
			
		}
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
	
	public static void evoke( Hero hero ) {
		hero.sprite.emitter().burst( Speck.factory( Speck.EVOKE ), 5 );
	}

	public String title() {

		String name = name();

		if (visiblyUpgraded() != 0)
			name = Messages.format( TXT_TO_STRING_LVL, name, visiblyUpgraded()  );

		if (quantity > 1)
			name = Messages.format( TXT_TO_STRING_X, name, quantity );

		return name;

	}
	
	public String name() {
		return trueName();
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
