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
		
		HP = HT = Dungeon.getCycleMultiplier(20);
		defenseSkill = Dungeon.getCycleMultiplier(5);
		
		EXP = Dungeon.getCycleMultiplier(4);
		maxLvl = 9;
		
		lootChance = 0.2f; //by default, see lootChance()
	}
	
	@Override
	public long damageRoll() {
	    return Random.NormalLongRange( Dungeon.getCycleMultiplier(2), Dungeon.getCycleMultiplier(5) );
	}
	
	@Override
	public long attackSkill(Char target ) {
		return Dungeon.getCycleMultiplier(12);
	}
	
	@Override
	public void damage(long dmg, Object src) {
		double scaleFactor = AscensionChallenge.statModifier(this);
		long scaledDmg = Math.round(dmg/scaleFactor);
		if (scaledDmg >= 5 + Dungeon.cycle * 75L && Dungeon.cycle < 2){
			//takes 5/6/7/8/9/10 dmg at 5/7/10/14/19/25 incoming dmg
			scaledDmg = 4 + Dungeon.cycle * 75L + (int)(Math.sqrt(8*(scaledDmg - 4) + 1) - 1)/2;
		}
		dmg = (long) (scaledDmg*AscensionChallenge.statModifier(this));
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
		w.level(0);
		return w;
	}
}
