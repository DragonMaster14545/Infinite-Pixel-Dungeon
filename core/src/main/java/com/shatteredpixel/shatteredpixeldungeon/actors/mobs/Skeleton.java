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
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLivingEarth;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Earthroot;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SkeletonSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;

public class Skeleton extends Mob {
	
	{
		spriteClass = SkeletonSprite.class;
		
		HP = HT = Dungeon.getCycleMultiplier(BigInteger.valueOf(25));
		defenseSkill = Dungeon.getCycleMultiplier(BigInteger.valueOf(9));
		
		EXP = Dungeon.getCycleMultiplier(BigInteger.valueOf(5));
		maxLvl = 10;

		loot = Generator.Category.WEAPON;
		lootChance = 0.1667f; //by default, see lootChance()

		properties.add(Property.UNDEAD);
		properties.add(Property.INORGANIC);
	}
	
	@Override
	public BigInteger damageRoll() {
		return Dungeon.NormalLongRange( Dungeon.getCycleMultiplier(BigInteger.valueOf(2)), Dungeon.getCycleMultiplier(BigInteger.TEN) );
	}
	
	@Override
	public void die( Object cause ) {
		
		super.die( cause );
		
		if (cause == Chasm.class) return;
		
		boolean heroKilled = false;
		for (int i = 0; i < PathFinder.NEIGHBOURS8.length; i++) {
			Char ch = findChar( pos + PathFinder.NEIGHBOURS8[i] );
			if (ch != null && ch.isAlive()) {
				BigInteger damage = Big.round(Big.dec(damageRoll()).multiply(Big.dec(1.5d)));
				damage = Big.round(Big.dec(damage).multiply(Big.dec(AscensionChallenge.statModifier(this))));

				//all sources of DR are 2x effective vs. bone explosion
				//this does not consume extra uses of rock armor and earthroot armor

				WandOfLivingEarth.RockArmor rockArmor = ch.buff(WandOfLivingEarth.RockArmor.class);
				if (rockArmor != null) {
					BigInteger preDmg = damage;
					damage = rockArmor.absorb(damage);
					damage = damage.multiply(Big.round(Big.div(Big.dec(damage), Big.dec(preDmg.floatValue())))); //apply the % reduction twice
				}

				Earthroot.Armor armor = ch.buff( Earthroot.Armor.class );
				if ((damage.compareTo(BigInteger.ZERO) > 0) && armor != null) {
					BigInteger preDmg = damage;
					damage = armor.absorb( damage );
					damage = damage.subtract((preDmg.subtract(damage))); //apply the flat reduction twice
				}

				//apply DR twice (with 2 rolls for more consistency)
				damage = BigInteger.ZERO.max(damage.subtract((ch.drRoll().add(ch.drRoll()))));
				ch.damage( damage, this );
				if (ch == Dungeon.hero && !ch.isAlive()) {
					heroKilled = true;
				}
			}
		}
		
		if (Dungeon.level.heroFOV[pos]) {
			Sample.INSTANCE.play( Assets.Sounds.BONES );
		}
		
		if (heroKilled) {
			Dungeon.fail( this );
			GLog.n( Messages.get(this, "explo_kill") );
		}
	}

	@Override
	public float lootChance() {
		//each drop makes future drops 1/3 as likely
		// so loot chance looks like: 1/6, 1/18, 1/54, 1/162, etc.
		return super.lootChance() * (float)Math.pow(1/3f, Dungeon.LimitedDrops.SKELE_WEP.count);
	}

	@Override
	public Item createLoot() {
		Dungeon.LimitedDrops.SKELE_WEP.count++;
		return super.createLoot();
	}

	@Override
	public BigInteger attackSkill(Char target ) {
		return Dungeon.getCycleMultiplier(BigInteger.valueOf(12));
	}
	
	@Override
	public BigInteger cycledDrRoll() {
		return Dungeon.NormalLongRange(Dungeon.getCycleMultiplier(BigInteger.ZERO), Dungeon.getCycleMultiplier(BigInteger.valueOf(5)));
	}

}
