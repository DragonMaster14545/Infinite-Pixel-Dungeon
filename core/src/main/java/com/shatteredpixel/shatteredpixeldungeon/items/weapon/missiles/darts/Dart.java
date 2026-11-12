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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts;



import com.shatteredpixel.shatteredpixeldungeon.utils.Big;
import java.math.BigInteger;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.VelvetPouch;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Crossbow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Dart extends MissileWeapon {

	{
		levelKnown = true;

		image = ItemSpriteSheet.DART;
		hitSound = Assets.Sounds.HIT_ARROW;
		hitSoundPitch = 1.3f;
		
		internalTier = tier = 1;
		
		//infinite, even with penalties
		baseUses = 1000;

		//all darts share a set ID
		setID = 0L;
	}
	
	protected static final String AC_TIP = "TIP";
	
	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_TIP );
		return actions;
	}
	
	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);
		if (action.equals(AC_TIP)){
			GameScene.selectItem(itemSelector);
		}
	}
	
	@Override
	public BigInteger min(BigInteger lvl) {
		if (bow != null){
			if (!(this instanceof TippedDart) && Dungeon.hero.buff(Crossbow.ChargedShot.class) != null){
				//ability increases base dmg by 37.5%, scaling by 50%
				return  BigInteger.valueOf(7).add(BigInteger.TWO.multiply(bow.buffedLvl())).add(lvl);//+2 per bow level, +1 per level
			} else {
				return  BigInteger.valueOf((tier()+3)).add(bow.buffedLvl()).add(lvl);  //+1 per level or bow level
			}
		} else {
			return  BigInteger.valueOf(tier()).add(lvl.multiply(BigInteger.valueOf(tier())));    //scaling unchanged
		}
	}

	@Override
	public BigInteger max(BigInteger lvl) {
		if (bow != null){
			if (!(this instanceof TippedDart) && Dungeon.hero.buff(Crossbow.ChargedShot.class) != null){
				//ability increases base dmg by 37.5%, scaling by 50%
				return  BigInteger.valueOf(15).add(BigInteger.valueOf(4).multiply(bow.buffedLvl())).add(BigInteger.TWO.multiply(lvl)); //+4 per bow level, +2 per level
			} else {
				return  BigInteger.valueOf(4L*(tier()+3)).add(BigInteger.valueOf((tier()+2)).multiply(bow.buffedLvl())).add(BigInteger.valueOf((tier()+1)).multiply(lvl)); //+3 per bow level, +2 per level
			}
		} else {
			return  BigInteger.valueOf(tier()*2L).add(BigInteger.TWO.multiply(lvl).multiply(BigInteger.valueOf(tier())));  //scaling unchanged
		}
	}
	
	protected static Crossbow bow;
	
	private void updateCrossbow(){
		if (Dungeon.hero == null) {
			bow = null;
		} else if (Dungeon.hero.belongings.weapon() instanceof Crossbow){
			bow = (Crossbow) Dungeon.hero.belongings.weapon();
		} else if (Dungeon.hero.belongings.secondWep() instanceof Crossbow) {
			//player can instant swap anyway, so this is just QoL
			bow = (Crossbow) Dungeon.hero.belongings.secondWep();
		} else {
			bow = null;
		}
        tier = 1 + Dungeon.cycle * 5;
	}

	public boolean crossbowHasEnchant( Char owner ){
		return bow != null && bow.enchantment != null && owner.buff(MagicImmune.class) == null;
	}
	
	@Override
	public boolean hasEnchant(Class<? extends Enchantment> type, Char owner) {
		if (bow != null && bow.hasEnchant(type, owner)){
			return true;
		} else {
			return super.hasEnchant(type, owner);
		}
	}

	@Override
	public float accuracyFactor(Char owner, Char target) {
		//don't update xbow here, as dart is the active weapon atm
		if (bow != null && owner.buff(Crossbow.ChargedShot.class) != null){
			return Char.INFINITE_ACCURACY;
		} else {
			return super.accuracyFactor(owner, target);
		}
	}

	@Override
	public BigInteger proc(Char attacker, Char defender, BigInteger damage) {
		if (bow != null && !processingChargedShot){
			damage = bow.proc(attacker, defender, damage);
		}

		BigInteger dmg = super.proc(attacker, defender, damage);
		if (!processingChargedShot) {
			processChargedShot(defender, damage);
		}
		return dmg;
	}

	@Override
	public int throwPos(Hero user, int dst) {
		updateCrossbow();
		return super.throwPos(user, dst);
	}

	@Override
	protected void onThrow(int cell) {
		updateCrossbow();
		//we have to set this here, as on-hit effects can move the target we aim at
		chargedShotPos = cell;
		super.onThrow(cell);
	}

	protected boolean processingChargedShot = false;
	private int chargedShotPos;
	protected void processChargedShot( Char target, BigInteger dmg ){
		//don't update xbow here, as dart may be the active weapon atm
		processingChargedShot = true;
		if (chargedShotPos != -1 && bow != null && Dungeon.hero.buff(Crossbow.ChargedShot.class) != null) {
			PathFinder.buildDistanceMap(chargedShotPos, Dungeon.level.passable, 3);
			//necessary to clone as some on-hit effects use Pathfinder
			int[] distance = PathFinder.distance.clone();
			for (Char ch : Actor.chars()){
				if (ch == target){
					Actor.add(new Actor() {
						{ actPriority = VFX_PRIO; }
						@Override
						protected boolean act() {
							if (!ch.isAlive()){
								bow.onAbilityKill(Dungeon.hero, ch);
							}
							Actor.remove(this);
							return true;
						}
					});
				} else if (distance[ch.pos] != Integer.MAX_VALUE){
					proc(Dungeon.hero, ch, dmg);
				}
			}
		}
		chargedShotPos = -1;
		processingChargedShot = false;
	}

	@Override
	protected void decrementDurability() {
		super.decrementDurability();
		if (Dungeon.hero.buff(Crossbow.ChargedShot.class) != null) {
			Dungeon.hero.buff(Crossbow.ChargedShot.class).detach();
		}
	}

	@Override
	public void throwSound() {
		updateCrossbow();
		if (bow != null) {
			Sample.INSTANCE.play(Assets.Sounds.ATK_CROSSBOW, 1, Random.Float(0.87f, 1.15f));
		} else {
			super.throwSound();
		}
	}
	
	@Override
	public String info() {

		updateCrossbow();
		if (bow != null && !bow.isIdentified()){
			BigInteger level = bow.level();
			//temporarily sets the level of the bow to 0 for IDing purposes
			bow.level(BigInteger.ZERO);
			String info = super.info();
			bow.level(level);
			return info;
		} else {
			return super.info();
		}
	}
	
	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	@Override
	public BigInteger value() {
		return Big.round(Big.div(Big.dec(super.value()), Big.dec(2f))); //half normal value
	}
	
	private final WndBag.ItemSelector itemSelector = new WndBag.ItemSelector() {

		@Override
		public String textPrompt() {
			return Messages.get(Dart.class, "prompt");
		}

		@Override
		public Class<?extends Bag> preferredBag(){
			return VelvetPouch.class;
		}

		@Override
		public boolean itemSelectable(Item item) {
			return item instanceof Plant.Seed;
		}

		@Override
		public void onSelect(final Item item) {
			
			if (item == null) return;
			
			final BigInteger maxToTip = curItem.quantity().min(item.quantity().multiply(BigInteger.TWO));
			final long maxSeedsToUse = Big.toLong((maxToTip.add(BigInteger.ONE)).divide(BigInteger.TWO));
			
			final int singleSeedDarts;
			
			final String[] options;
			
			if ((curItem.quantity().compareTo(BigInteger.ONE) == 0)){
				singleSeedDarts = 1;
				options = new String[]{
						Messages.get(Dart.class, "tip_one"),
						Messages.get(Dart.class, "tip_cancel")};
			} else {
				singleSeedDarts = 2;
				if ((maxToTip.compareTo(BigInteger.TWO) <= 0)){
					options = new String[]{
							Messages.get(Dart.class, "tip_two"),
							Messages.get(Dart.class, "tip_cancel")};
				} else {
					options = new String[]{
							Messages.get(Dart.class, "tip_all", maxToTip, maxSeedsToUse),
							Messages.get(Dart.class, "tip_two"),
							Messages.get(Dart.class, "tip_cancel")};
				}
			}
			
			TippedDart tipResult = TippedDart.getTipped((Plant.Seed) item, BigInteger.ONE);
			
			GameScene.show(new WndOptions( new ItemSprite(item),
					Messages.titleCase(item.name()),
					Messages.get(Dart.class, "tip_desc", tipResult.name()) + "\n\n" + tipResult.desc(),
					options){
				
				@Override
				protected void onSelect(int index) {
					super.onSelect(index);
					
					if (index == 0 && options.length == 3){
						if ((item.quantity().compareTo(BigInteger.valueOf(maxSeedsToUse)) <= 0)){
							item.detachAll( curUser.belongings.backpack );
						} else {
							item.quantity(item.quantity().subtract(BigInteger.valueOf(maxSeedsToUse)));
						}
						
						if ((maxToTip.compareTo(curItem.quantity()) < 0)){
							curItem.quantity(curItem.quantity().subtract(maxToTip));
						} else {
							curItem.detachAll(curUser.belongings.backpack);
						}
						
						TippedDart newDart = TippedDart.getTipped((Plant.Seed) item, maxToTip);
						if (!newDart.collect()) Dungeon.level.drop(newDart, curUser.pos).sprite.drop();
						
						curUser.spend( 1f );
						curUser.busy();
						curUser.sprite.operate(curUser.pos);
						
					} else if ((index == 1 && options.length == 3) || (index == 0 && options.length == 2)){
						item.detach( curUser.belongings.backpack );
						
						if ((curItem.quantity().compareTo(BigInteger.valueOf(singleSeedDarts)) <= 0)){
							curItem.detachAll( curUser.belongings.backpack );
						} else {
							curItem.quantity(curItem.quantity().subtract(BigInteger.valueOf(singleSeedDarts)));
						}
						
						TippedDart newDart = TippedDart.getTipped((Plant.Seed) item, BigInteger.valueOf(singleSeedDarts));
						if (!newDart.collect()) Dungeon.level.drop(newDart, curUser.pos).sprite.drop();
						
						curUser.spend( 1f );
						curUser.busy();
						curUser.sprite.operate(curUser.pos);
					}
				}
			});
			
		}
		
	};
}
