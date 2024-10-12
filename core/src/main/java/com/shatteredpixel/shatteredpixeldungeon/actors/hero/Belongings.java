/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LostInventory;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.KindofMisc;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class Belongings implements Iterable<Item> {

	private Hero owner;

	public static class Backpack extends Bag {
		{
			image = ItemSpriteSheet.BACKPACK;
		}
		public int capacity(){
			int cap = super.capacity();
			for (Item item : items){
				if (item instanceof Bag){
					cap++;
				}
			}
			if (Dungeon.hero != null && Dungeon.hero.belongings.secondWep != null){
				//secondary weapons still occupy an inv. slot
				cap--;
			}
			return cap;
		}
	}

	public Backpack backpack;
	
	public Belongings( Hero owner ) {
		this.owner = owner;
		
		backpack = new Backpack();
		backpack.owner = owner;
	}
	public KindOfWeapon weapon = null;
	public Armor armor = null;
	public List<Artifact> artifacts = new ArrayList<>();
	public int artifactSlots(){
		return 2;
	}
	public List<KindofMisc> miscs = new ArrayList<>();
	public int miscSlots() {
		return 2;
	}
	public List<Ring> rings = new ArrayList<>();
	public int ringSlots(){
		return 2;
	}

	//used when thrown weapons temporary become the current weapon
	public KindOfWeapon thrownWeapon = null;

	//used to ensure that the duelist always uses the weapon she's using the ability of
	public KindOfWeapon abilityWeapon = null;

	//used by the champion subclass
	public KindOfWeapon secondWep = null;

	//*** these accessor methods are so that worn items can be affected by various effects/debuffs
	// we still want to access the raw equipped items in cases where effects should be ignored though,
	// such as when equipping something, showing an interface, or dealing with items from a dead hero

	//normally the primary equipped weapon, but can also be a thrown weapon or an ability's weapon
	public KindOfWeapon attackingWeapon(){
		if (thrownWeapon != null) return thrownWeapon;
		if (abilityWeapon != null) return abilityWeapon;
		return weapon();
	}

	//we cache whether belongings are lost to avoid lots of calls to hero.buff(LostInventory.class)
	private boolean lostInvent;
	public void lostInventory( boolean val ){
		lostInvent = val;
	}

	public boolean lostInventory(){
		return lostInvent;
	}

	public KindOfWeapon weapon(){
		if (!lostInventory() || (weapon != null && weapon.keptThroughLostInventory())){
			return weapon;
		} else {
			return null;
		}
	}

	public Armor armor(){
		if (!lostInventory() || (armor != null && armor.keptThroughLostInventory())){
			return armor;
		} else {
			return null;
		}
	}

	public List<Artifact> artifacts(){
		if (!lostInventory()){
			return artifacts;
		} else {
			List<Artifact> toReturn = new ArrayList<>();
			artifacts.forEach(artifact -> {
				if (artifact != null && !artifact.keptThroughLostInventory()){
					toReturn.add(artifact);
				}
			});
			return toReturn;
		}
	}

	public List<KindofMisc> miscs(){
		if (!lostInventory()){
			return miscs;
		} else {
			List<KindofMisc> toReturn = new ArrayList<>();
			miscs.forEach(misc -> {
				if (misc != null && !misc.keptThroughLostInventory()){
					toReturn.add(misc);
				}
			});
			return toReturn;
		}
	}

	public List<Ring>  rings(){
		if (!lostInventory()){
			return rings;
		} else {
			List<Ring> toReturn = new ArrayList<>();
			rings.forEach(ring -> {
				if (ring != null && !ring.keptThroughLostInventory()){
					toReturn.add(ring);
				}
			});
			return toReturn;
		}
	}

	public KindOfWeapon secondWep(){
		if (!lostInventory() || (secondWep != null && secondWep.keptThroughLostInventory())){
			return secondWep;
		} else {
			return null;
		}
	}

	// ***
	
	private static final String WEAPON		= "weapon";
	private static final String ARMOR		= "armor";
	private static final String ARTIFACT   = "artifact";
	private static final String MISC       = "misc";
	private static final String RING       = "ring";

	private static final String SECOND_WEP = "second_wep";

	public void storeInBundle( Bundle bundle ) {
		
		backpack.storeInBundle( bundle );
		
		bundle.put( WEAPON, weapon );
		bundle.put( ARMOR, armor );
		bundle.put( ARTIFACT, artifacts);
		bundle.put( MISC, miscs);
		bundle.put( RING, rings);
		bundle.put( SECOND_WEP, secondWep );
	}
	
	public void restoreFromBundle( Bundle bundle ) {
		
		backpack.clear();
		backpack.restoreFromBundle( bundle );
		
		weapon = (KindOfWeapon) bundle.get(WEAPON);
		if (weapon() != null)       weapon().activate(owner);
		
		armor = (Armor)bundle.get( ARMOR );
		if (armor() != null)        armor().activate( owner );

		artifacts = new ArrayList<>((Collection<Artifact>) (Collection<?>) bundle.getCollection(ARTIFACT));
		if (artifacts() != null) for (Artifact artifact : artifacts) artifact.activate( owner );
		System.out.println("Artifacts: " + artifacts);

		miscs = new ArrayList<>((Collection<KindofMisc>) (Collection<?>) bundle.getCollection(MISC));
		if (miscs() != null) for (KindofMisc misc : miscs) misc.activate( owner );

		rings = new ArrayList<>((Collection<Ring>) (Collection<?>) bundle.getCollection(RING));
		if (rings() != null) for (Ring ring : rings) ring.activate( owner );

		secondWep = (KindOfWeapon) bundle.get(SECOND_WEP);
		if (secondWep() != null)    secondWep().activate(owner);
	}
	
	public static void preview( GamesInProgress.Info info, Bundle bundle ) {
		if (bundle.contains( ARMOR )){
			Armor armor = ((Armor)bundle.get( ARMOR ));
			if (armor instanceof ClassArmor){
				info.armorTier = 6;
			} else {
				info.armorTier = armor.tier;
			}
		} else {
			info.armorTier = 0;
		}
	}

	//ignores lost inventory debuff
	public ArrayList<Bag> getBags(){
		ArrayList<Bag> result = new ArrayList<>();

		result.add(backpack);

		for (Item i : this){
			if (i instanceof Bag){
				result.add((Bag)i);
			}
		}

		return result;
	}
	
	@SuppressWarnings("unchecked")
	public<T extends Item> T getItem( Class<T> itemClass ) {

		boolean lostInvent = lostInventory();

		for (Item item : this) {
			if (itemClass.isInstance( item )) {
				if (!lostInvent || item.keptThroughLostInventory()) {
					return (T) item;
				}
			}
		}
		
		return null;
	}

	public<T extends Item> ArrayList<T> getAllItems( Class<T> itemClass ) {
		ArrayList<T> result = new ArrayList<>();

		boolean lostInvent = lostInventory();

		for (Item item : this) {
			if (itemClass.isInstance( item )) {
				if (!lostInvent || item.keptThroughLostInventory()) {
					result.add((T) item);
				}
			}
		}

		return result;
	}
	
	public boolean contains( Item contains ){

		boolean lostInvent = lostInventory();
		
		for (Item item : this) {
			if (contains == item) {
				if (!lostInvent || item.keptThroughLostInventory()) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	public Item getSimilar( Item similar ){

		boolean lostInvent = lostInventory();
		
		for (Item item : this) {
			if (similar != item && similar.isSimilar(item)) {
				if (!lostInvent || item.keptThroughLostInventory()) {
					return item;
				}
			}
		}
		
		return null;
	}
	
	public ArrayList<Item> getAllSimilar( Item similar ){
		ArrayList<Item> result = new ArrayList<>();

		boolean lostInvent = lostInventory();
		
		for (Item item : this) {
			if (item != similar && similar.isSimilar(item)) {
				if (!lostInvent || item.keptThroughLostInventory()) {
					result.add(item);
				}
			}
		}
		
		return result;
	}

	//triggers when a run ends, so ignores lost inventory effects
	public void identify() {
		for (Item item : this) {
			item.identify(false);
		}
	}
	
	public void observe() {
		if (weapon() != null) {
			weapon().identify();
			Badges.validateItemLevelAquired(weapon());
		}
		if (armor() != null) {
			armor().identify();
			Badges.validateItemLevelAquired(armor());
		}
		if (artifacts() != null) {
			for (Artifact artifact : artifacts) {
				artifact.identify();
				Badges.validateItemLevelAquired(artifact);
			}
		}
		if (miscs() != null) {
			for (KindofMisc misc : miscs) {
				misc.identify();
				Badges.validateItemLevelAquired(misc);
			}
		}
		if (rings() != null) {
			for (Ring ring : rings) {
				ring.identify();
				Badges.validateItemLevelAquired(ring);
			}
		}
		if (secondWep() != null){
			secondWep().identify();
			Badges.validateItemLevelAquired(secondWep());
		}
		for (Item item : backpack) {
			if (item instanceof EquipableItem || item instanceof Wand) {
				item.cursedKnown = true;
			}
		}
		Item.updateQuickslot();
	}
	
	public void uncurseEquipped() {
		Item[] equippedArray = getEquippedArray();
		ScrollOfRemoveCurse.uncurse( owner, equippedArray);
	}

	public Item[] getEquippedArray() {
		List<Item> equippedList = new ArrayList<>();
		equippedList.add(weapon());
		equippedList.add(armor());
		equippedList.addAll(artifacts());
		equippedList.addAll(miscs());
		equippedList.addAll(rings());
		equippedList.add(secondWep());
        return equippedList.toArray(new Item[0]);
	}

	public KindofMisc[] getMiscsArray() {
		List<KindofMisc> miscsList = new ArrayList<>();
		miscsList.addAll(rings());
		miscsList.addAll(miscs());
		miscsList.addAll(artifacts());
		return miscsList.toArray(new KindofMisc[0]);
	}

	public Item randomUnequipped() {
		if (owner.buff(LostInventory.class) != null) return null;

		Item item;
		do {
			item = Random.element(backpack.items);
		} while (item == null || !item.canBeOofed());
		return item;
	}
	
	public int charge( float charge ) {
		
		int count = 0;
		
		for (Wand.Charger charger : owner.buffs(Wand.Charger.class)){
			charger.gainCharge(charge);
			count++;
		}
		
		return count;
	}

	@Override
	public Iterator<Item> iterator() {
		return new ItemIterator();
	}
	
	private class ItemIterator implements Iterator<Item> {

		private int index = 0;
		
		private Iterator<Item> backpackIterator = backpack.iterator();
		
		private Item[] equipped = getEquippedArray();
		private int backpackIndex = equipped.length;
		
		@Override
		public boolean hasNext() {
			
			for (int i=index; i < backpackIndex; i++) {
				if (equipped[i] != null) {
					return true;
				}
			}
			
			return backpackIterator.hasNext();
		}

		@Override
		public Item next() {
			
			while (index < backpackIndex) {
				Item item = equipped[index++];
				if (item != null) {
					return item;
				}
			}
			
			return backpackIterator.next();
		}

		@Override
		public void remove() {
			if (index == 0){
				equipped[0] = weapon = null;
			} else if (index == 1){
				equipped[1] = armor = null;
			} else if (index >= 2 && index <= 2+artifacts.size()){
				equipped[2+artifacts.size()] = artifacts.toArray(new Artifact[0])[index-2] = null;
			} else if (index > 2+artifacts.size() && index <= 2+artifacts.size()+miscs.size()){
				equipped[2+artifacts.size()+miscs.size()] = miscs.toArray(new KindofMisc[0])[index-2-artifacts.size()] = null;
			} else if (index > 2+artifacts.size()+miscs.size() && index <= 2+artifacts.size()+miscs.size()+rings.size()){
				equipped[2+artifacts.size()+miscs.size()+rings.size()] = rings.toArray(new Ring[0])[index-2-artifacts.size()-miscs.size()] = null;
			} else if (index == 2+artifacts.size()+miscs.size()+rings.size()+1){
				equipped[2+artifacts.size()+miscs.size()+rings.size()+1] = secondWep = null;
			} else {
				backpackIterator.remove();
			}
		}
	}
}
