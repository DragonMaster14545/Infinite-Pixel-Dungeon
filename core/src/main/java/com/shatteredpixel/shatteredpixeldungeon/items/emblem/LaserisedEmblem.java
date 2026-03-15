package com.shatteredpixel.shatteredpixeldungeon.items.emblem;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Laserised;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Summoner;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

public class LaserisedEmblem extends EmblemSystem {

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
            ((Weapon) item).enchant(new Laserised());
        } else {
            GLog.w("You must use this only on weapons.");
            collect();
        }
    }

    @Override
    public ItemSprite.Glowing glowing() {
        return new ItemSprite.Glowing(0xfc3503);
    }

    @Override
    public String desc() {
        return super.desc();
    }
}
