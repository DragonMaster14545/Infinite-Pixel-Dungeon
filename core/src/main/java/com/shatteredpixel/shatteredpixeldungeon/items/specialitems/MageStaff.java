package com.shatteredpixel.shatteredpixeldungeon.items.specialitems;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Blueprint;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLiquidFlame;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import java.math.BigInteger;
import java.util.ArrayList;

public class MageStaff extends Item {

	private static final String AC_FORGE = "FORGE";

	{
		image = ItemSpriteSheet.BROKEN_STAFF;

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
			if (hero.belongings.getItem(MagesStaff.class) != null) {
				GLog.w( Messages.get(this, "already") );
				return;
			}

			MagesStaff reward = new MagesStaff(new WandOfMagicMissile());
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
			inputs =  new Class[]{ScrollOfUpgrade.class, PotionOfLiquidFlame.class, Blueprint.class};
			inQuantity = new int[]{1, 1, 1};

			cost = 250;

			output = MageStaff.class;
			outQuantity = OUT_QUANTITY;
		}

	}
}
