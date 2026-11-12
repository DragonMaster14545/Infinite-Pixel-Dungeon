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

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;


import java.math.BigInteger;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.sprites.BatSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;

public class Bat extends Mob {

	{
		spriteClass = BatSprite.class;
		
		HP = HT = Dungeon.getCycleMultiplier(BigInteger.valueOf(30));
		defenseSkill = Dungeon.getCycleMultiplier(BigInteger.valueOf(15));
		baseSpeed = 2f;
		
		EXP = Dungeon.getCycleMultiplier(BigInteger.valueOf(7));
		maxLvl = 15;
		
		flying = true;
		
		loot = new PotionOfHealing();
		lootChance = 0.1667f; //by default, see lootChance()
	}
	
	@Override
	public BigInteger damageRoll() {
		return Dungeon.getCycleMultiplier(Dungeon.NormalLongRange( BigInteger.valueOf(5), BigInteger.valueOf(18) ));
	}
	
	@Override
	public BigInteger attackSkill(Char target ) {
        return Dungeon.getCycleMultiplier(BigInteger.valueOf(16));
	}
	
	@Override
	public BigInteger cycledDrRoll() {
        return Dungeon.getCycleMultiplier(Dungeon.NormalLongRange(BigInteger.ZERO,BigInteger.valueOf(4)));
	}

    @Override
    public void die(Object cause) {
        flying = false;
        super.die(cause);
    }

	@Override
	public BigInteger attackProc( Char enemy, BigInteger damage ) {
		damage = super.attackProc( enemy, damage );
		BigInteger reg = damage.subtract(BigInteger.valueOf(4)).subtract(BigInteger.valueOf(Dungeon.cycle * 60L)).min(HT.subtract(HP));
		
		if ((reg.compareTo(BigInteger.ZERO) > 0)) {
			HP = HP.add(reg);
			sprite.showStatusWithIcon(CharSprite.POSITIVE, reg.toString(), FloatingText.HEALING);
		}
		
		return damage;
	}
	
	@Override
	public float lootChance(){
		return super.lootChance() * ((7f - Dungeon.LimitedDrops.BAT_HP.count) / 7f);
	}
	
	@Override
	public Item createLoot(){
		Dungeon.LimitedDrops.BAT_HP.count++;
		return super.createLoot();
	}
	
}
