/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.items.trinkets;



import com.shatteredpixel.shatteredpixeldungeon.utils.Big;
import java.math.BigInteger;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class FerretTuft extends Trinket {

    {
        image = ItemSpriteSheet.FERRET_TUFT;
    }

    @Override
    protected BigInteger upgradeEnergyCost() {
        //6 -> 8(14) -> 10(24) -> 12(36)
        return BigInteger.valueOf(6).add(BigInteger.valueOf(2).multiply(level()));
    }

    @Override
    public String statsDesc() {
        if (isIdentified()){
            return Messages.get(this, "stats_desc", Messages.decimalFormat("#.##", 100 * (evasionMultiplier(buffedLvl())-1f)));
        } else {
            return Messages.get(this, "typical_stats_desc", Messages.decimalFormat("#.##", 100 * (evasionMultiplier(BigInteger.ZERO)-1f)));
        }
    }

    public static float evasionMultiplier(){
        return evasionMultiplier(BigInteger.valueOf(trinketLevel(FerretTuft.class)));
    }

    public static float evasionMultiplier( BigInteger level ){
        if ((level.compareTo(BigInteger.valueOf(-1)) <= 0)){
            return 1;


        } else {
            return Big.dec(1).add(Big.dec(0.125f).multiply(Big.dec((level.add(BigInteger.ONE))))).floatValue();
        }
    }

}