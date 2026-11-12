/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2019-2024 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.items.quest;



import com.shatteredpixel.shatteredpixeldungeon.utils.Big;
import java.math.BigInteger;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sai;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class TenguShuriken extends MissileWeapon {

    {
        image = ItemSpriteSheet.SHURIKEN;
        hitSound = Assets.Sounds.HIT_STAB;
        hitSoundPitch = 1.4f;

        internalTier = tier = 2;
        baseUses = 2500; //is unbreakable
    }

    @Override
    public BigInteger max(BigInteger lvl) {
        return  BigInteger.valueOf(10 * tier).add((tier == 1 ? BigInteger.TWO.multiply(lvl) : BigInteger.valueOf(tier).multiply(lvl))); //scaling unchanged
    }

    @Override
    public float baseDelay(Char owner) {
        return 0;
    }

    @Override
    public BigInteger proc(Char attacker, Char defender, BigInteger damage) {
        Buff.affect(defender, Sai.DefenseDebuff.class, 5f).stack = Big.toInt((max().divide(BigInteger.valueOf(3))));
        return super.proc(attacker, defender, damage);
    }
}

