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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.Wound;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.KingBlade;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Longsword;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.noosa.Group;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

public class Trihit extends Weapon.Enchantment {

	private static ItemSprite.Glowing COLOR = new ItemSprite.Glowing( 0x03fcec );

	@Override
	public long proc( Weapon weapon, Char attacker, Char defender, long damage ) {
        int[] targets = new int[2];
        int direction = -1;
        int direction1 = -1, direction2 = -1;
        for (int i = 0; i < PathFinder.NEIGHBOURS8.length; i++){
            if (Actor.findChar(attacker.pos + PathFinder.NEIGHBOURS8[i]) == defender){
                direction = i;
            }
        }
        if (direction != -1) {
            switch (direction) {
                case 0:
                    direction1 = 4;
                    direction2 = 6;
                    break;
                case 1:
                case 6:
                    direction1 = 3;
                    direction2 = 4;
                    break;
                case 2:
                    direction1 = 3;
                    direction2 = 6;
                    break;
                case 3:
                case 4:
                    direction1 = 1;
                    direction2 = 6;
                    break;
                case 5:
                    direction1 = 1;
                    direction2 = 4;
                    break;
                case 7:
                    direction1 = 1;
                    direction2 = 3;
                    break;
            }
            targets[0] = defender.pos + PathFinder.NEIGHBOURS8[direction1];
            targets[1] = defender.pos + PathFinder.NEIGHBOURS8[direction2];
            TrihitWound.hit(defender.pos, 315, 0x03fcec);
            for (int pos: targets){
                TrihitWound.hit(pos, 45, 0x03fcec);
                if (Actor.findChar(pos) != null){
                    Char ch = Actor.findChar(pos);
                    assert ch != null;
                    if (ch.alignment != attacker.alignment){
                        long dmg = Math.round(damage*0.6f);
                        Sample.INSTANCE.play(Assets.Sounds.HIT_STAB, 1f, 0.75f);
                        ch.damage(dmg, this);
                    }
                }
            }
        }
		
		return damage;
	}

    public static class TrihitWound extends Wound {
        int color;

        @Override
        public void update() {
            super.update();

            hardlight(color);
        }

        public static void hit(int pos, float angle, int color ) {
            Group parent = Dungeon.hero.sprite.parent;
            TrihitWound w = (TrihitWound)parent.recycle( TrihitWound.class );
            parent.bringToFront( w );
            w.reset( pos );
            w.angle = angle;
            w.color = color;
        }
    }

	@Override
	public ItemSprite.Glowing glowing() {
		return COLOR;
	}
}
