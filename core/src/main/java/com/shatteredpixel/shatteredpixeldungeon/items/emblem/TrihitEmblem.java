package com.shatteredpixel.shatteredpixeldungeon.items.emblem;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.fishingrods.FishingRod;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Trihit;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

public class TrihitEmblem extends EmblemSystem {

    @Override
    public long value() {
        long price = 400;
        return price * Rarity.LEGENDARY.multiplier;
    }

    @Override
    protected boolean usableOnItem(Item item) {
        return item instanceof Weapon && item.emblemUse < 5;
    }

    @Override
    protected void onItemSelected(Item item) {
        if (item instanceof Weapon) {
            ((Weapon) item).enchant(new Trihit());
        } else {
            GLog.w("You must use this only on weapons.");
            collect();
        }
    }

    @Override
    public ItemSprite.Glowing glowing() {
        return new ItemSprite.Glowing(0x03fcec);
    }

    @Override
    public String desc() {
        return super.desc();
    }
}
