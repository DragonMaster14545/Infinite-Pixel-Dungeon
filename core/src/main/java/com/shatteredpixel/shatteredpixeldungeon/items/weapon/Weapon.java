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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Berserk;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.duelist.ElementalStrike;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.KingBlade;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfArcana;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfForce;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfFuror;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.ParchmentScrap;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.ShardOfOblivion;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Annoying;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Dazzling;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Displacing;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Explosive;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Friendly;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Polarized;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Sacrificial;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Wayward;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blazing;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blocking;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blooming;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Chilling;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Corrupting;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Elastic;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Galactic;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Grim;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Kinetic;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Lucky;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Projecting;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Shocking;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Unstable;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Vampiric;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.RunicBlade;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Scimitar;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Arrays;

abstract public class Weapon extends KindOfWeapon implements EquipableItem.Tierable {

	public float    ACC = 1f;	// Accuracy modifier
	public float	DLY	= 1f;	// Speed modifier
	public int      RCH = 1;    // Reach modifier (only applies to melee hits)
    public int tier;
    public int internalTier;

    public enum Augment {
		SPEED   (0.7d, 2/3f),
		DAMAGE  (1.5d, 5/3f),
		NONE	(1.0d, 1f);

		private double damageFactor;
		private float delayFactor;

		Augment(double dmg, float dly){
			damageFactor = dmg;
			delayFactor = dly;
		}

		public long damageFactor(long dmg){
            return Math.round(dmg * damageFactor);
		}

		public float delayFactor(float dly){
			return dly * delayFactor;
		}
	}
	
	public Augment augment = Augment.NONE;
	
	private static final int USES_TO_ID = 20;
	private float usesLeftToID = USES_TO_ID;
	private float availableUsesToID = USES_TO_ID/2f;
	
	public Enchantment enchantment;
	public boolean enchantHardened = false;
	public boolean curseInfusionBonus = false;
	public boolean masteryPotionBonus = false;

	public static float hardenBoost(long upgrades){
		return 0.0002f * upgrades;
	}

	protected Weapon(){
		//randomizeRarity();
	}

	@Override
	public long proc( Char attacker, Char defender, long damage ) {
		
		if (enchantment != null && attacker.buff(MagicImmune.class) == null) {
			damage = enchantment.proc( this, attacker, defender, damage );
		}
		if (attacker instanceof Hero){
		    for (Item item: ((Hero) attacker).belongings.backpack){
		        if (item instanceof KingBlade){
		            damage = new Unstable().proc(this,attacker,defender,damage);
                    damage = new Unstable().proc(this,attacker,defender,damage);
                }
            }
        }

		if (!levelKnown && attacker == Dungeon.hero) {
			float uses = Math.min( availableUsesToID, Talent.itemIDSpeedFactor(Dungeon.hero, this) );
			availableUsesToID -= uses;
			usesLeftToID -= uses;
			if (usesLeftToID <= 0) {
				if (ShardOfOblivion.passiveIDDisabled()){
					if (usesLeftToID > -1){
						GLog.p(Messages.get(ShardOfOblivion.class, "identify_ready"), name());
					}
					usesLeftToID = -1;
				} else {
					identify();
					GLog.p(Messages.get(Weapon.class, "identify"));
					Badges.validateItemLevelAquired(this);
				}
			}
		}

		return damage;
	}
	
	public void onHeroGainExp( float levelPercent, Hero hero ){
		levelPercent *= Talent.itemIDSpeedFactor(hero, this);
		if (!levelKnown && isEquipped(hero) && availableUsesToID <= USES_TO_ID/2f) {
			//gains enough uses to ID over 0.5 levels
			availableUsesToID = Math.min(USES_TO_ID/2f, availableUsesToID + levelPercent * USES_TO_ID);
		}
	}
	
	private static final String USES_LEFT_TO_ID = "uses_left_to_id";
	private static final String AVAILABLE_USES  = "available_uses";
	private static final String ENCHANTMENT	    = "enchantment";
	private static final String ENCHANT_HARDENED = "enchant_hardened";
	private static final String CURSE_INFUSION_BONUS = "curse_infusion_bonus";
	private static final String MASTERY_POTION_BONUS = "mastery_potion_bonus";
	private static final String AUGMENT	        = "augment";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( USES_LEFT_TO_ID, usesLeftToID );
		bundle.put( AVAILABLE_USES, availableUsesToID );
		bundle.put( ENCHANTMENT, enchantment );
		bundle.put( ENCHANT_HARDENED, enchantHardened );
		bundle.put( CURSE_INFUSION_BONUS, curseInfusionBonus );
		bundle.put( MASTERY_POTION_BONUS, masteryPotionBonus );
		bundle.put( AUGMENT, augment );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		usesLeftToID = bundle.getFloat( USES_LEFT_TO_ID );
		availableUsesToID = bundle.getFloat( AVAILABLE_USES );
		enchantment = (Enchantment)bundle.get( ENCHANTMENT );
		enchantHardened = bundle.getBoolean( ENCHANT_HARDENED );
		curseInfusionBonus = bundle.getBoolean( CURSE_INFUSION_BONUS );
		masteryPotionBonus = bundle.getBoolean( MASTERY_POTION_BONUS );

		augment = bundle.getEnum(AUGMENT, Augment.class);
	}
	
	@Override
	public void reset() {
		super.reset();
		usesLeftToID = USES_TO_ID;
		availableUsesToID = USES_TO_ID/2f;
	}

	@Override
	public boolean collect(Bag container) {
		if(super.collect(container)){
			if (Dungeon.hero != null && Dungeon.hero.isAlive() && isIdentified() && enchantment != null){
				Catalog.setSeen(enchantment.getClass());
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Item identify(boolean byHero) {
		if (enchantment != null && byHero && Dungeon.hero != null && Dungeon.hero.isAlive()){
			Catalog.setSeen(enchantment.getClass());
		}
		return super.identify(byHero);
	}

	public boolean readyToIdentify(){
		return !isIdentified() && usesLeftToID <= 0;
	}

	@Override
	public float accuracyFactor(Char owner, Char target) {
		
		int encumbrance = 0;
		
		if( owner instanceof Hero ){
			encumbrance = STRReq() - ((Hero)owner).STR();
		}

		float ACC = this.ACC;

		if (owner.buff(Wayward.WaywardBuff.class) != null && enchantment instanceof Wayward){
			ACC /= 5;
		}

		return encumbrance > 0 ? (float)(ACC / Math.pow( 1.5, encumbrance )) : ACC;
	}
	
	@Override
	public float delayFactor( Char owner ) {
		return baseDelay(owner) * (1f/speedMultiplier(owner));
	}

	protected float baseDelay( Char owner ){
		float delay = augment.delayFactor(this.DLY);
		if (owner instanceof Hero) {
			int encumbrance = STRReq() - ((Hero)owner).STR();
			if (encumbrance > 0){
				delay *= Math.pow( 1.2, encumbrance );
			}
		}

		return delay;
	}

	protected float speedMultiplier(Char owner ){
		float multi = RingOfFuror.attackDelayMultiplier(owner);

		if (owner.buff(Scimitar.SwordDance.class) != null){
			multi += 0.6f;
		}

		return multi;
	}

	@Override
	public int reachFactor(Char owner) {
		int reach = RCH;
		if (owner instanceof Hero && RingOfForce.fightingUnarmed((Hero) owner)){
			reach = 1; //brawlers stance benefits from enchantments, but not innate reach
			if (!RingOfForce.unarmedGetsWeaponEnchantment((Hero) owner)){
				return reach;
			}
		}
		if (hasEnchant(Projecting.class, owner)){
			return reach + Math.round(enchantment.procChanceMultiplier(owner));
		} else {
			return reach;
		}
	}

	public int STRReq(){
		return STRReq(level());
	}

	public abstract int STRReq(long lvl);

	protected static int STRReq(int tier, long lvl){
		lvl = Math.max(0, lvl);

		//strength req decreases at +1,+3,+6,+10,etc.
		return (8 + tier * 2) - (int)(Math.sqrt(8 * lvl + 1) - 1)/2;
	}

	@Override
	public long level() {
		long level = super.level();
		if (curseInfusionBonus) level += 1 + level/6;
		return level;
	}

	@Override
	public int tier() {
		switch (Math.max(0, (tier-1) / 5)){
			case 0:
				return tier;
			case 1:
				return tier+8;
			case 2:
				return Math.round(tier*4f);
			case 3:
				return Math.round((tier+20)*5f);
			case 4:
				return Math.round((tier*10+75)*15f);
			case 5:
			default:
				return Math.round((tier*55+850)*95f);
		}
	}

	@Override
	public Item upgrade() {
		return upgrade(false);
	}
	
	public Item upgrade(boolean enchant ) {

		if (enchant){
			if (enchantment == null){
				enchant(Enchantment.random());
			}
		} else if (enchantment != null) {
			//chance to lose harden buff is 10/20/40/80/100% when upgrading from +6/7/8/9/10
			/*if (enchantHardened){
				if (level() >= 6 && Random.Float(10) < Math.pow(2, level()-6)){
					enchantHardened = false;
				}

			//chance to remove curse is a static 33%
			} else */if (hasCurseEnchant()) {
				if (Random.Int(3) == 0) enchant(null);
			}
		}
		
		cursed = false;

		return super.upgrade();
	}
	
	@Override
	public String name() {
		return enchantment != null && (cursedKnown || !enchantment.curse()) ? enchantment.name( super.name() ) : super.name();
	}
	
	@Override
	public Item random() {
		//+0: 75% (3/4)
		//+1: 20% (4/20)
		//+2: 5%  (1/20)
		int n = 0;
		if (Random.Int(4) == 0) {
			n++;
			if (Random.Int(5) == 0) {
				n++;
			}
		}
		level(n);

//we use a separate RNG here so that variance due to things like parchment scrap
		//does not affect levelgen
		Random.pushGenerator(Random.Long());

			//30% chance to be cursed
			//10% chance to be enchanted
			float effectRoll = Dungeon.Float();
			if (effectRoll < 0.3f * ParchmentScrap.curseChanceMultiplier()) {
				enchant(Enchantment.randomCurse());
				cursed = true;
			} else if (effectRoll >= 1f - (0.1f * ParchmentScrap.enchantChanceMultiplier())){
				enchant();
			}

		Random.popGenerator();

		return this;
	}
	
	public Weapon enchant( Enchantment ench ) {
		if (ench == null || !ench.curse()) curseInfusionBonus = false;
		enchantment = ench;
		updateQuickslot();
		if (ench != null && isIdentified() && Dungeon.hero != null
				&& Dungeon.hero.isAlive() && Dungeon.hero.belongings.contains(this)){
			Catalog.setSeen(ench.getClass());
		}
		return this;
	}

	public Weapon enchant() {

		Class<? extends Enchantment> oldEnchantment = enchantment != null ? enchantment.getClass() : null;
		Enchantment ench = Enchantment.random( oldEnchantment );

		return enchant( ench );
	}

	public boolean hasEnchant(Class<?extends Enchantment> type, Char owner) {
		if (enchantment instanceof Galactic && type != Galactic.class) {
			return ((Galactic) enchantment).hasEnchant(type, owner);
		}
		return enchantment != null && enchantment.getClass() == type && owner.buff(MagicImmune.class) == null;
	}
	
	//these are not used to process specific enchant effects, so magic immune doesn't affect them
	public boolean hasGoodEnchant(){
		return enchantment != null && !enchantment.curse();
	}

	public boolean hasCurseEnchant(){
		return enchantment != null && enchantment.curse();
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return enchantment != null && (cursedKnown || !enchantment.curse()) ? enchantment.glowing() : null;
	}

	public static abstract class Enchantment implements Bundlable {

		public static final Class<?>[] common = new Class<?>[]{
				Blazing.class, Chilling.class, Kinetic.class, Shocking.class};

		public static final Class<?>[] uncommon = new Class<?>[]{
				Blocking.class, Blooming.class, Elastic.class,
				Lucky.class, Projecting.class, Unstable.class};

		public static final Class<?>[] rare = new Class<?>[]{
				Corrupting.class, Grim.class, Vampiric.class,
				Galactic.class};

		public static final float[] typeChances = new float[]{
				50, //12.5% each
				40, //6.67% each
				10  //3.33% each
		};

		public static final Class<?>[] curses = new Class<?>[]{
				Annoying.class, Displacing.class, Dazzling.class, Explosive.class,
				Sacrificial.class, Wayward.class, Polarized.class, Friendly.class
		};
		
			
		public abstract long proc( Weapon weapon, Char attacker, Char defender, long damage );

		protected float procChanceMultiplier( Char attacker ){
			return genericProcChanceMultiplier( attacker );
		}

		public static float genericProcChanceMultiplier( Char attacker ){
			float multi = RingOfArcana.enchantPowerMultiplier(attacker);
			Berserk rage = attacker.buff(Berserk.class);
			if (rage != null) {
				multi = rage.enchantFactor(multi);
			}

			if (attacker.buff(RunicBlade.RunicSlashTracker.class) != null){
				multi += attacker.buff(RunicBlade.RunicSlashTracker.class).boost;
				attacker.buff(RunicBlade.RunicSlashTracker.class).detach();
			}

			if (attacker.buff(ElementalStrike.DirectedPowerTracker.class) != null){
				multi += attacker.buff(ElementalStrike.DirectedPowerTracker.class).enchBoost;
				attacker.buff(ElementalStrike.DirectedPowerTracker.class).detach();
			}

			if (attacker.buff(Talent.SpiritBladesTracker.class) != null
					&& ((Hero)attacker).pointsInTalent(Talent.SPIRIT_BLADES) == 4){
				multi += 0.1f;
			}
			if (attacker.buff(Talent.StrikingWaveTracker.class) != null
					&& ((Hero)attacker).pointsInTalent(Talent.STRIKING_WAVE) == 4){
				multi += 0.2f;
			}
			if (attacker instanceof Hero && ((Hero) attacker).heroClass == HeroClass.DUELIST){
				multi += 0.5f;
			}

			return multi;
		}

		public String name() {
			if (!curse())
				return name( Messages.get(this, "enchant"));
			else
				return name( Messages.get(Item.class, "curse"));
		}

		public String name( String weaponName ) {
			return Messages.get(this, "name", weaponName);
		}

		public String desc() {
			return Messages.get(this, "desc");
		}

		public boolean curse() {
			return false;
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
		}

		@Override
		public void storeInBundle( Bundle bundle ) {
		}
		
		public abstract ItemSprite.Glowing glowing();
		
		@SuppressWarnings("unchecked")
		public static Enchantment random( Class<? extends Enchantment> ... toIgnore ) {
			switch(Dungeon.chances(typeChances)){
				case 0: default:
					return randomCommon( toIgnore );
				case 1:
					return randomUncommon( toIgnore );
				case 2:
					return randomRare( toIgnore );
			}
		}
		
		@SuppressWarnings("unchecked")
		public static Enchantment randomCommon( Class<? extends Enchantment> ... toIgnore ) {
			ArrayList<Class<?>> enchants = new ArrayList<>(Arrays.asList(common));
			enchants.removeAll(Arrays.asList(toIgnore));
			if (enchants.isEmpty()) {
				return random();
			} else {
				return (Enchantment) Reflection.newInstance(Random.element(enchants));
			}
		}
		
		@SuppressWarnings("unchecked")
		public static Enchantment randomUncommon( Class<? extends Enchantment> ... toIgnore ) {
			ArrayList<Class<?>> enchants = new ArrayList<>(Arrays.asList(uncommon));
			enchants.removeAll(Arrays.asList(toIgnore));
			if (enchants.isEmpty()) {
				return random();
			} else {
				return (Enchantment) Reflection.newInstance(Random.element(enchants));
			}
		}
		
		@SuppressWarnings("unchecked")
		public static Enchantment randomRare( Class<? extends Enchantment> ... toIgnore ) {
			ArrayList<Class<?>> enchants = new ArrayList<>(Arrays.asList(rare));
			enchants.removeAll(Arrays.asList(toIgnore));
			if (enchants.isEmpty()) {
				return random();
			} else {
				return (Enchantment) Reflection.newInstance(Random.element(enchants));
			}
		}

		@SuppressWarnings("unchecked")
		public static Enchantment randomCurse( Class<? extends Enchantment> ... toIgnore ){
			ArrayList<Class<?>> enchants = new ArrayList<>(Arrays.asList(curses));
			enchants.removeAll(Arrays.asList(toIgnore));
			if (enchants.isEmpty()) {
				return random();
			} else {
				return (Enchantment) Reflection.newInstance(Random.element(enchants));
			}
		}
		
	}
}
