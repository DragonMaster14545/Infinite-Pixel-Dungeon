package com.shatteredpixel.shatteredpixeldungeon.items.specialitems;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Blueprint;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.Elixir;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfMight;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.Alchemize;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.Recycle;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.UnstableSpell;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class DuelistsRapier extends Item {

    //TODO: this is the item that unlocks the duelist abilities on other classes

    {
        image = ItemSpriteSheet.RAPIER;

        levelKnown = true;
        cursedKnown = true;

        //this must not be stored on shulker boxes and its variant
        unique = true;
    }

	public static boolean owned( Hero hero ) {
		return hero != null && hero.belongings != null && hero.belongings.getItem(DuelistsRapier.class) != null;
	}

    @Override
    public boolean isIdentified() {
        return true;
    }

	@Override
	public boolean isUpgradable() {
		return false;
	}

    public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe.SimpleRecipe {

        private static final int OUT_QUANTITY = 1;

        {
            inputs =  new Class[]{PotionOfStrength.class, ElixirOfMight.class, Blueprint.class};
            inQuantity = new int[]{1, 1, 1};

            cost = 250;

            output = DuelistsRapier.class;
            outQuantity = OUT_QUANTITY;
        }

    }
}
