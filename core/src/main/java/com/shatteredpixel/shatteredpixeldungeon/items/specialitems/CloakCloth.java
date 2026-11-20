package com.shatteredpixel.shatteredpixeldungeon.items.specialitems;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Blueprint;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfInvisibility;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CloakOfShadows;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import java.math.BigInteger;
import java.util.ArrayList;

public class CloakCloth extends Item {

	private static final String AC_FORGE = "FORGE";

	{
		image = ItemSpriteSheet.CLOAK_SCRAP;

		defaultAction = AC_FORGE;

		levelKnown = true;
		cursedKnown = true;

		//this must not be stored on shulker boxes and its variant
		unique = true;
		bones = false;
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_FORGE );
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {
		super.execute( hero, action );

		if (action.equals( AC_FORGE )) {
			if (hero.belongings.getItem(CloakOfShadows.class) != null) {
				GLog.w( Messages.get(this, "already") );
				return;
			}

			CloakOfShadows reward = new CloakOfShadows();
			reward.identify();

			detach( hero.belongings.backpack );

			if (!reward.collect()) {
				Dungeon.level.drop( reward, hero.pos ).sprite.drop();
			}

			GLog.p( Messages.get(this, "forged", reward.name()) );
			hero.sprite.operate( hero.pos );
			hero.spendAndNext( 1f );
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

	@Override
	public BigInteger value() {
		return BigInteger.valueOf(250);
	}

	public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe.SimpleRecipe {

		private static final int OUT_QUANTITY = 1;

		{
			inputs =  new Class[]{ScrollOfMagicMapping.class, PotionOfInvisibility.class, Blueprint.class};
			inQuantity = new int[]{1, 1, 1};

			cost = 250;

			output = CloakCloth.class;
			outQuantity = OUT_QUANTITY;
		}

	}
}
