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

package com.shatteredpixel.shatteredpixeldungeon.items.wands;



import com.shatteredpixel.shatteredpixeldungeon.utils.Big;
import java.math.BigInteger;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.WandEmpower;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.noosa.audio.Sample;

//for wands that directly damage a target
//wands with AOE or circumstantial direct damage count here (e.g. fireblast, transfusion), but wands with indirect damage do not (e.g. corrosion)
public abstract class DamageWand extends Wand{

	public BigInteger min(){
		return Big.round(Big.dec(min(buffedLvl())).multiply(Big.dec((1+ Dungeon.hero.lvl/150d))));
	}

	public abstract BigInteger min(BigInteger lvl);

	public BigInteger max(){
		return Big.round(Big.dec(max(buffedLvl())).multiply(Big.dec((1+ Dungeon.hero.lvl/150d))));
	}

	public abstract BigInteger max(BigInteger lvl);

	public BigInteger damageRoll(){
		return damageRoll(buffedLvl());
	}

	public BigInteger damageRoll(BigInteger lvl){
		BigInteger dmg = Hero.heroDamageIntRange(min(lvl), max(lvl));
		WandEmpower emp = Dungeon.hero.buff(WandEmpower.class);
		if (emp != null){
			dmg = dmg.add(BigInteger.valueOf(emp.dmgBoost));
			emp.left--;
			if (emp.left <= 0) {
				emp.detach();
			}
			Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG, 0.75f, 1.2f);
		}
		return dmg;
	}

	@Override
	public String statsDesc() {
		if (levelKnown)
			return Messages.get(this, "stats_desc", min(), max()) + "\n\n" + Messages.get(Wand.class, "charges", curCharges, getMaxCharges());
		else
			return Messages.get(this, "stats_desc", min(BigInteger.ZERO), max(BigInteger.ZERO));
	}

	@Override
	public String upgradeStat1(BigInteger level) {
		return min(level) + "-" + max(level);
	}
}
