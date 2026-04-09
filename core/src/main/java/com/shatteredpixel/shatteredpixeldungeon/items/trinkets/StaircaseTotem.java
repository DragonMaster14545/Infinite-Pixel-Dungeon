/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
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

import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class StaircaseTotem extends Trinket {

	{
		image = ItemSpriteSheet.STAIRCASE_TOTEM;
	}

	@Override
	protected long upgradeEnergyCost() {
		//15 -> 17(32) -> 19(51) -> 21(72)
		return 15+2*level();
	}

    @Override
    public long maxUpgrade() {
        return 4;
    }

	@Override
	public String statsDesc() {
		if (isIdentified()){
			return Messages.get(this, "stats_desc", Messages.decimalFormat("#.##", 100*(additionalRoomMulti((int)buffedLvl())-1)),
                    Messages.decimalFormat("#.##", 100*(additionalMobMulti((int)buffedLvl())-1)));
		} else {
			return Messages.get(this, "typical_stats_desc", Messages.decimalFormat("#.##", 100*(additionalRoomMulti(0)-1)),
                    Messages.decimalFormat("#.##", 100*(additionalMobMulti(0)-1)));
		}
	}

	public static float additionalRoomMulti(){
		return additionalRoomMulti(trinketLevel(StaircaseTotem.class));
	}

	public static float additionalRoomMulti( int level ){
		if (level == -1){
			return 1f;
		} else {
			return 1 + (0.125f + 0.125f*level);
		}
	}

    public static float additionalMobMulti(){
        return additionalMobMulti(trinketLevel(StaircaseTotem.class));
    }

    public static float additionalMobMulti( int level ){
        if (level == -1){
            return 1f;
        } else {
            return 1 + (0.075f + 0.075f*level);
        }
    }

}
