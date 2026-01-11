package com.shatteredpixel.shatteredpixeldungeon.items.emblem;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

public class UncommonEmblem extends EmblemSystem {

    public Rarity randomizeE_UncommonRarity() {
        //rarity = Rarity.UNCOMMON;
        double random = Dungeon.Double(Rarity.UNCOMMON.chance, Dungeon.LuckDirection.DOWN);
        for (Rarity r : Rarity.values()) {
            if (random <= r.chance) {
                rarity = r;
                break;
            }
        }
        return rarity;
    }

    @Override
    public long value() {
        long price = 400;
        return price * Rarity.UNCOMMON.multiplier;
    }

    @Override
    protected void onItemSelected(Item item) {
        //TODO make this an unobtainable item (which you cant get naturally)
        item.rarity = randomizeE_UncommonRarity();
    }

    @Override
    public ItemSprite.Glowing glowing() {
        return new ItemSprite.Glowing( Rarity.UNCOMMON.color );
    }

    @Override
    public String desc() {
        return super.desc();
    }
}
