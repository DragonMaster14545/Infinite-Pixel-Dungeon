/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2019 Evan Debenham
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


import java.math.BigInteger;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class DisplacingDart extends TippedDart {
	
	{
		image = ItemSpriteSheet.DISPLACING_DART;
	}
	
	@Override
	public BigInteger proc(Char attacker, Char defender, BigInteger damage) {
		return super.proc(attacker, defender, damage);
	}

    @Override
    public int throwPos(Hero user, int dst) {
        return dst;
    }

    @Override
    public BigInteger max(BigInteger lvl) {
        return super.max(lvl).multiply(BigInteger.valueOf(4)).divide(BigInteger.valueOf(3));
    }

    @Override
    public BigInteger min(BigInteger lvl) {
        return super.min(lvl).multiply(BigInteger.valueOf(4)).divide(BigInteger.valueOf(3));
    }
}
