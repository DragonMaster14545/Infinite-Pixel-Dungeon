package com.shatteredpixel.shatteredpixeldungeon.items.tempfolder;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Cheese;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTransmutation;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfEarthblast;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFireblast;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLivingEarth;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

import java.util.ArrayList;

public class SearingSlasher extends Item {

    {
        image = ItemSpriteSheet.SOMETHING;
        unique = true;
    }

    //TODO this weapon is slightly overpowered to creative gloves, and also craftable.
    //     I want the duelist ability of this to damage every enemy on the dashing path of the hero

    public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe {

        @Override
        public boolean testIngredients(ArrayList<Item> ingredients) {
            boolean stock = true;
            boolean shard = false;
            boolean trans = false;

            //TODO placeholder (a check for a limited crafting item)
            //if (Dungeon.ssLimit) stock = false;

            for (Item ingredient : ingredients){
                if (ingredient.quantity() > 0) {
                    if (ingredient instanceof ShardOfInstability) {
                        shard = true;
                    } else if (ingredient instanceof ScrollOfTransmutation) {
                        trans = true;
                    }
                }
            }

            return stock && shard && trans;
        }

        @Override
        public long cost(ArrayList<Item> ingredients) {
            return 1;
        }

        @Override
        public Item brew(ArrayList<Item> ingredients) {
            if (!testIngredients(ingredients)) return null;

            for (Item ingredient : ingredients){
                ingredient.quantity(ingredient.quantity() - 1);
            }

            return sampleOutput(null);
        }

        @Override
        public Item sampleOutput(ArrayList<Item> ingredients) {
            return new WandOfEarthblast().identify();
        }
    }
}
