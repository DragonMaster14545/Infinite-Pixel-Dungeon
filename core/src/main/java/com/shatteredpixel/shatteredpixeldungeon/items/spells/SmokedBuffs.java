package com.shatteredpixel.shatteredpixeldungeon.items.spells;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.BuffSmoke;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.DebuffSmoke;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfExperience;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTransmutation;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

import java.math.BigInteger;

public class SmokedBuffs extends Spell {

	private static final int SMOKES = 4;
	private static final int RADIUS = 3;
	private static final int VOLUME = 90;

	{
		image = ItemSpriteSheet.SMOKER;

		talentChance = 1/(float) Recipe.OUT_QUANTITY;
	}

	@Override
	protected void onCast(Hero hero) {
		int width = Dungeon.level.width();
		int height = Dungeon.level.height();
		int hx = hero.pos % width;
		int hy = hero.pos / width;

		int good = 0;
		int bad = 0;
		int placed = 0;
		int attempts = 0;

		while (placed < SMOKES && attempts < 40) {
			attempts++;
			int x = hx + Random.IntRange(-RADIUS, RADIUS);
			int y = hy + Random.IntRange(-RADIUS, RADIUS);
			if (x < 0 || y < 0 || x >= width || y >= height) continue;
			int cell = x + y * width;
			if (Dungeon.level.solid[cell]) continue;

			if (Random.Int(2) == 0) {
				GameScene.add(Blob.seed(cell, BigInteger.valueOf(VOLUME), BuffSmoke.class));
				good++;
			} else {
				GameScene.add(Blob.seed(cell, BigInteger.valueOf(VOLUME), DebuffSmoke.class));
				bad++;
			}
			placed++;
		}

		GLog.i(Messages.get(this, "cast", good, bad));

		detach(curUser.belongings.backpack);
		hero.spendAndNext(1f);
	}

	@Override
	public BigInteger value() {
		return quantity.multiply(BigInteger.valueOf(30));
	}

	@Override
	public BigInteger energyVal() {
		return quantity.multiply(BigInteger.valueOf(6));
	}

	public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe.SimpleRecipe {

		private static final int OUT_QUANTITY = 2;

		{
			inputs =  new Class[]{PotionOfExperience.class, ScrollOfTransmutation.class};
			inQuantity = new int[]{1, 1};

			cost = 20;

			output = SmokedBuffs.class;
			outQuantity = OUT_QUANTITY;
		}

	}
}
