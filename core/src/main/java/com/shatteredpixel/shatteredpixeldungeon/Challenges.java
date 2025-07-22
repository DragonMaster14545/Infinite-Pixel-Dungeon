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

package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.items.Dewdrop;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;

public enum Challenges {
	NO_FOOD("no_food"),
	NO_ARMOR("no_armor"),
	NO_HEALING("no_healing"),
	NO_HERBALISM("no_herbalism"),
	SWARM_INTELLIGENCE("swarm_intelligence"),
	DARKNESS("darkness"),
	NO_SCROLLS("no_scrolls"),
	CHAMPION_ENEMIES("champion_enemies"),
	STRONGER_BOSSES("stronger_bosses");

	public final String nameId;

	Challenges(String nameId) {
		this.nameId = nameId;
	}

	public static int activeChallenges() {
		int count = 0;
		for (boolean challengeActive : Dungeon.challenges) {
			if (challengeActive) count++;
		}
		return count;
	}


	public static boolean isItemBlocked( Item item ){

		if (Dungeon.isChallenged(NO_HERBALISM) && item instanceof Dewdrop){
			return true;
		}

		return false;

	}

}