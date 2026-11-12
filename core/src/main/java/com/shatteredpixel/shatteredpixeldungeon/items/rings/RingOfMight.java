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

package com.shatteredpixel.shatteredpixeldungeon.items.rings;




import com.shatteredpixel.shatteredpixeldungeon.utils.Big;
import java.math.BigInteger;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class RingOfMight extends Ring {

	{
		icon = ItemSpriteSheet.Icons.RING_MIGHT;
	}

	@Override
	public boolean doEquip(Hero hero) {
		if (super.doEquip(hero)){
			hero.updateHT( false );
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean doUnequip(Hero hero, boolean collect, boolean single) {
		if (super.doUnequip(hero, collect, single)){
			hero.updateHT( false );
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Item upgrade() {
		super.upgrade();
		updateTargetHT();
		return this;
	}

	@Override
	public void level(BigInteger value) {
		super.level(value);
		updateTargetHT();
	}
	
	private void updateTargetHT(){
		if (buff != null && buff.target instanceof Hero){
			((Hero) buff.target).updateHT( false );
		}
	}
	
	public String statsInfo() {
		if (isIdentified()){
			String info = Messages.get(this, "stats",
					soloBuffedBonus().subtract(BigInteger.ONE), (soloBuffedBonus().subtract(BigInteger.ONE)).multiply(BigInteger.TWO));
			if (isEquipped(Dungeon.hero) && (soloBuffedBonus().compareTo(combinedBuffedBonus(Dungeon.hero)) != 0)){
				info += "\n\n" + Messages.get(this, "combined_stats",
						combinedBuffedBonus(Dungeon.hero).subtract(BigInteger.ONE), (combinedBuffedBonus(Dungeon.hero).subtract(BigInteger.ONE)).multiply(BigInteger.TWO));
			}
			return info;
		} else {
			return Messages.get(this, "typical_stats", 0, 2);
		}
	}

	@Override
	public String upgradeStat1(BigInteger level) {
		if (cursed && cursedKnown) level = BigInteger.valueOf(-1).min(level.subtract(BigInteger.valueOf(3))).subtract(BigInteger.ONE);
		return level.add(BigInteger.ONE).toString();
	}

	@Override
	public String upgradeStat2(BigInteger level) {
		if (cursed && cursedKnown) level = BigInteger.valueOf(-1).min(level.subtract(BigInteger.valueOf(3))).subtract(BigInteger.ONE);
		return level.multiply(BigInteger.TWO).toString();
	}

	@Override
	protected RingBuff buff( ) {
		return new Might();
	}
	
	public static int strengthBonus( Char target ){
		int boost = Big.toInt(BigInteger.valueOf(2000000000L).min(getBuffedBonus(target, Might.class)));
		return (boost >= 1) ? Math.min(boost, 10 * (1 + Dungeon.cycle)) : 0;
    }
	
	public static int HTMultiplier( Char target ){
		int multiplier = 0;
		if ((getBuffedBonus(target, Might.class).compareTo(BigInteger.ONE) == 0)) multiplier = 2;
		if ((getBuffedBonus(target, Might.class).compareTo(BigInteger.ONE) > 0)) multiplier += Big.toLong(getBuffedBonus(target, Might.class).multiply(BigInteger.TWO));
		return Math.min(multiplier, 20 * (1 + Dungeon.cycle + Dungeon.hero.upgrades.ringExpansion()));
	}

	public class Might extends RingBuff {
	}
}

