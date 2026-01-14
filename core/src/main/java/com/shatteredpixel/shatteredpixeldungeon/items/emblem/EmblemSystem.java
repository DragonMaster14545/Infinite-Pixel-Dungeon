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

package com.shatteredpixel.shatteredpixeldungeon.items.emblem;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.fishingrods.FishingRod;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.MagicalInfusion;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.Spell;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

import java.util.ArrayList;

public abstract class EmblemSystem extends Item {

    public static final String AC_APPLY	= "APPLY";

    {

        defaultAction = AC_APPLY;
        unique = true;
        stackable = true;
        image = ItemSpriteSheet.EMBLEM;

    }

    @Override
    public ArrayList<String> actions(Hero hero ) {
        ArrayList<String> actions = super.actions( hero );
        actions.add( AC_APPLY );
        return actions;
    }

    @Override
    public void execute( final Hero hero, String action ) {

        super.execute( hero, action );

        if (action.equals( AC_APPLY )) {

            onCast( hero );
            Catalog.countUse(getClass());

        }
    }

    @Override
    public boolean isIdentified() {
        return true;
    }

    @Override
    public boolean isUpgradable() {
        return false;
    }

    protected void onCast(Hero hero) {
        GameScene.selectItem( itemSelector );
    }

    private String inventoryTitle(){
        return Messages.get(this, "inv_title");
    }

    protected Class<?extends Bag> preferredBag = null;

    protected boolean usableOnItem(Item item) {
        return (item instanceof Weapon || item instanceof Armor || item instanceof Ring || item instanceof Artifact
                || item instanceof Wand || item instanceof FishingRod) && item.emblemUse < 3;
    }

    protected abstract void onItemSelected( Item item );

    protected WndBag.ItemSelector itemSelector = new WndBag.ItemSelector() {

        @Override
        public String textPrompt() {
            return inventoryTitle();
        }

        @Override
        public Class<? extends Bag> preferredBag() {
            return preferredBag;
        }

        @Override
        public boolean itemSelectable(Item item) {
            return usableOnItem(item);
        }

        @Override
        public void onSelect( Item item ) {

            //FIXME this safety check shouldn't be necessary
            //it would be better to eliminate the curItem static variable.
            if (!(curItem instanceof EmblemSystem)){
                return;
            }

            if (item != null && item.emblemUse < 3) {

                //Infusion opens a separate window that can be cancelled
                //so we don't do a lot of logic here
                curItem = detach(curUser.belongings.backpack);

                ((EmblemSystem)curItem).onItemSelected( item );
                if (!(curItem instanceof MagicalInfusion)) {
                    curUser.spend(1f);
                    curUser.busy();
                    (curUser.sprite).operate(curUser.pos);
                    item.emblemUse++;

                    Sample.INSTANCE.play(Assets.Sounds.READ);

                    Catalog.countUse(curItem.getClass());
                    GLog.p( Messages.get(EmblemSystem.class,"raritize", 3 - item.emblemUse) );
                }

            }
        }
    };
}
