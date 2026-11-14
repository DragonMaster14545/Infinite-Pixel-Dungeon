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



import com.shatteredpixel.shatteredpixeldungeon.utils.Big;
import java.math.BigInteger;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SlimeSprite;
import com.watabou.utils.Random;

public class Slime extends Mob {
	
	{
		spriteClass = SlimeSprite.class;
		
		HP = HT = Dungeon.getCycleMultiplier(BigInteger.valueOf(20));
		defenseSkill = Dungeon.getCycleMultiplier(BigInteger.valueOf(5));
		
		EXP = Dungeon.getCycleMultiplier(BigInteger.valueOf(4));
		maxLvl = 9;
		
		lootChance = 0.2f; //by default, see lootChance()
	}
	
	@Override
	public BigInteger damageRoll() {
	    return Big.normalRange(Dungeon.getCycleMultiplier(BigInteger.valueOf(2)), Dungeon.getCycleMultiplier(BigInteger.valueOf(5)));
	}
	
	@Override
	public BigInteger attackSkill(Char target ) {
		return Dungeon.getCycleMultiplier(BigInteger.valueOf(12));
	}
	
	@Override
	public void damage(BigInteger dmg, Object src) {
		double scaleFactor = AscensionChallenge.statModifier(this);
		BigInteger scaledDmg = Big.round(Big.div(Big.dec(dmg), Big.dec(scaleFactor)));
		if ((scaledDmg.compareTo(BigInteger.valueOf(5 + Dungeon.cycle * 75L)) >= 0) && Dungeon.cycle < 2){
			//takes 5/6/7/8/9/10 dmg at 5/7/10/14/19/25 incoming dmg
			scaledDmg = BigInteger.valueOf(4 + Dungeon.cycle * 75L + (int)(Math.sqrt(BigInteger.valueOf(8).multiply((scaledDmg.subtract(BigInteger.valueOf(4)))).add(BigInteger.ONE).doubleValue()) - 1)/2);
		}
		dmg = Big.trunc((Big.dec(scaledDmg).multiply(Big.dec(AscensionChallenge.statModifier(this)))));
		super.damage(dmg, src);
	}

	@Override
	public float lootChance(){
		//each drop makes future drops 1/4 as likely
		// so loot chance looks like: 1/5, 1/20, 1/80, 1/320, etc.
		return super.lootChance() * (float)Math.pow(1/4f, Dungeon.LimitedDrops.SLIME_WEP.count);
	}
	
	@Override
	public Item createLoot() {
		Dungeon.LimitedDrops.SLIME_WEP.count++;
		MeleeWeapon w = (MeleeWeapon)Generator.randomUsingDefaults(Generator.Category.WEP_T2);
		w.level(BigInteger.ZERO);
		return w;
	}
}
