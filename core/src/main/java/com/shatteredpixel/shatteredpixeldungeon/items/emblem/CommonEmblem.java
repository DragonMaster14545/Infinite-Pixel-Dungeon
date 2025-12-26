package com.shatteredpixel.shatteredpixeldungeon.items.emblem;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class CommonEmblem extends EmblemSystem {

    public Rarity randomizeE_CommonRarity() {
        //rarity = Rarity.COMMON;
        double random = Dungeon.Double(Rarity.COMMON.chance, Dungeon.LuckDirection.DOWN);
        for (Rarity r : Rarity.values()) {
            if (random <= r.chance) {
                rarity = r;
                break;
            }
        }
        return rarity;
    }

    @Override
    protected void onItemSelected(Item item) {
        //TODO make this an evitable item (which you cant get naturally)
        item.rarity = randomizeE_CommonRarity();
    }

}
