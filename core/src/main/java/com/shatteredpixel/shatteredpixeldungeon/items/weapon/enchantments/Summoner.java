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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Doom;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Statue;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.DriedRose;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfDisintegration;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Greatsword;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.StatueSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class Summoner extends Weapon.Enchantment {

	private static ItemSprite.Glowing COLOR = new ItemSprite.Glowing( 0xc2fc03 );

	@Override
	public long proc( Weapon weapon, Char attacker, Char defender, long damage ) {
        if (Random.Float() >= 0.25f) {
            for (int i : PathFinder.NEIGHBOURS9) {

                if (!Dungeon.level.solid[attacker.pos + i]
                        && !Dungeon.level.pit[attacker.pos + i]
                        && Actor.findChar(attacker.pos + i) == null
                        && (attacker == Dungeon.hero || attacker instanceof DriedRose.GhostHero)) {

                    AlephKnight guardianKnight = new AlephKnight();
                    Weapon copy = weapon;
                    copy.level(weapon.level());
                    copy.enchant();
                    copy.augment = weapon.augment;
                    guardianKnight.weapon = copy;
                    guardianKnight.pos = attacker.pos + i;
                    guardianKnight.aggro(defender);
                    GameScene.add(guardianKnight);
                    Dungeon.level.occupyCell(guardianKnight);

                    CellEmitter.get(guardianKnight.pos).burst(Speck.factory(Speck.EVOKE), 4);
                    break;
                }
            }
        }
		return damage;
	}

    public static class AlephKnight extends Statue {
        {
            state = WANDERING;
            spriteClass = AlephSprite.class;
            alignment = Alignment.ALLY;

            levelGenStatue = false;
        }

        public AlephKnight() {
            HP = HT = 4 + Dungeon.escalatingDepth() * 2;
            defenseSkill = 6 + Dungeon.escalatingDepth();
        }

        @Override
        public void die(Object cause) {
            weapon = null;
            super.die(cause);
        }

        @Override
        public String description() {
            return Messages.get(this, "desc", weapon.trueName());
        }

        @Override
        public long drRoll() {
            return Dungeon.escalatingDepth();
        }
    }

    public static class AlephSprite extends StatueSprite {

        public AlephSprite(){
            super();
            tint(1, 1, 1, 0.4f);
        }

        @Override
        public void resetColor() {
            super.resetColor();
            tint(1, 1, 1, 0.4f);
        }
    }

	@Override
	public ItemSprite.Glowing glowing() {
		return COLOR;
	}
}
