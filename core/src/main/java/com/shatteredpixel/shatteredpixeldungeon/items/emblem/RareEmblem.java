package com.shatteredpixel.shatteredpixeldungeon.items.emblem;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

public class RareEmblem extends EmblemSystem {

    public Rarity randomizeE_RareRarity() {
        //rarity = Rarity.RARE;
        double random = Dungeon.Double(Rarity.RARE.chance, Dungeon.LuckDirection.DOWN);
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
        return price * Rarity.RARE.multiplier;
    }

    @Override
    protected void onItemSelected(Item item) {
        //TODO make this an unobtainable item (which you cant get naturally)
        item.rarity = randomizeE_RareRarity();
    }

    @Override
    public ItemSprite.Glowing glowing() {
        return new ItemSprite.Glowing( Rarity.RARE.color );
    }

    @Override
    public String desc() {
        return super.desc();
    }
}
