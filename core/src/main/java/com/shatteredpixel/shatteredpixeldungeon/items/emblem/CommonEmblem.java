package com.shatteredpixel.shatteredpixeldungeon.items.emblem;


import java.math.BigInteger;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
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
    public BigInteger value() {
        BigInteger price = BigInteger.valueOf(400);
        return price.multiply(Rarity.COMMON.multiplier);
    }

    @Override
    protected void onItemSelected(Item item) {
        //TODO make this an unobtainable item (which you cant get naturally)
        item.rarity = randomizeE_CommonRarity();
    }

    @Override
    public ItemSprite.Glowing glowing() {
        return new ItemSprite.Glowing( Rarity.COMMON.color );
    }

    @Override
    public String desc() {
        return super.desc();
    }
}
