package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndPrismBox;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class PrismBox extends Item {

	public static final String AC_DIAL = "DIAL";
	public static final int EXACT = 0, BURNING = 1, HOT = 2, WARM = 3, COLD = 4;

	public enum Difficulty {
		EASY(3, 3, 1, 2, 6, 1f),
		MODERATE(5, 7, 4, 8, 9, 0.75f),
		HARD(8, 10, 10, 20, 12, 0.5f),
		LUNATIC(12, 16, 25, 50, 16, 0.3f),
		IMPOSSIBLE(20, 30, 75, 150, 22, 0.15f);

		public final int minDials, maxDials, minItems, maxItems, lives;
		public final float feel;

		Difficulty(int minDials, int maxDials, int minItems, int maxItems, int lives, float feel) {
			this.feel = feel;
			this.minDials = minDials;
			this.maxDials = maxDials;
			this.minItems = minItems;
			this.maxItems = maxItems;
			this.lives = lives;
		}
	}

	{
		image = ItemSpriteSheet.PRISM_BOX;
		defaultAction = AC_DIAL;
		stackable = true;
		levelKnown = true;
		cursedKnown = true;
	}

	private Difficulty difficulty;
	private int[] dials;
	private int[] target;
	private boolean[] locked;
	private boolean[] forfeit;
	private int[] heat;
	private int attempts = 0;
	private int lives;
	private float feel = 1f;
	private int minItems = 1, maxItems = 2;
	private boolean wildcard;
	private int lastFloor = -1;
	private int shifts = 0;

	public static final int WILDCARD_CHANCE = 5;

	public PrismBox() {
		wildcard = ThreadLocalRandom.current().nextInt(1000) < WILDCARD_CHANCE;
		freshPuzzle();
		lastFloor = currentFloor();
	}

	public static PrismBox of(Difficulty difficulty) {
		PrismBox lock = new PrismBox();
		lock.wildcard = false;
		lock.build(difficulty);
		return lock;
	}

	private static Difficulty rollDifficulty() {
		int roll = ThreadLocalRandom.current().nextInt(100);
		return roll < 45 ? Difficulty.EASY
				: roll < 73 ? Difficulty.MODERATE
				: roll < 88 ? Difficulty.HARD
				: roll < 96 ? Difficulty.LUNATIC
				: Difficulty.IMPOSSIBLE;
	}

	private void freshPuzzle() {
		if (wildcard) buildWild();
		else build(rollDifficulty());
	}

	private static int currentFloor() {
		return Dungeon.depth * 100 + Dungeon.branch;
	}

	private void checkFloor() {
		if (!wildcard || Dungeon.hero == null || !Dungeon.hero.belongings.backpack.contains(this)) return;
		int floor = currentFloor();
		if (lastFloor == -1) {
			lastFloor = floor;
			return;
		}
		if (floor == lastFloor) return;
		lastFloor = floor;

		buildWild();
		shifts++;

		GLog.w(Messages.get(this, "shifted"));
		GameScene.updateItemDisplays = true;
		updateQuickslot();
	}

	@Override
	public String status() {
		checkFloor();
		return super.status();
	}

	private void build(Difficulty d) {
		ThreadLocalRandom rng = ThreadLocalRandom.current();
		int n = d.minDials + rng.nextInt(d.maxDials - d.minDials + 1);
		setup(d, n, d.lives, d.feel, d.minItems, d.maxItems);
	}

	private void buildWild() {
		ThreadLocalRandom rng = ThreadLocalRandom.current();
		Difficulty[] all = Difficulty.values();
		Difficulty label = all[rng.nextInt(all.length)];
		int n = 3 + rng.nextInt(28);
		int l = 3 + rng.nextInt(20);
		float f = 0.15f + rng.nextFloat() * 0.85f;
		int min = 1 + rng.nextInt(75);
		int max = min + 1 + rng.nextInt(min * 2 + 1);
		setup(label, n, l, f, min, max);
	}

	private void setup(Difficulty d, int n, int startLives, float heatFeel, int minI, int maxI) {
		ThreadLocalRandom rng = ThreadLocalRandom.current();
		difficulty = d;
		feel = heatFeel;
		minItems = minI;
		maxItems = maxI;
		dials = new int[n];
		target = new int[n];
		locked = new boolean[n];
		forfeit = new boolean[n];
		heat = new int[n];
		for (int i = 0; i < n; i++) {
			dials[i] = 128;
			target[i] = rng.nextInt(256);
			heat[i] = -1;
		}
		attempts = 0;
		lives = startLives;
	}

	public int burning() {
		return Math.max(1, Math.round(8 * feel));
	}

	public int hot() {
		return Math.max(burning() + 1, Math.round(32 * feel));
	}

	public int warm() {
		return Math.max(hot() + 1, Math.round(80 * feel));
	}

	public int lives() {
		return lives;
	}

	public Difficulty difficulty() {
		return difficulty;
	}

	public int dialCount() {
		return dials.length;
	}

	public int dial(int i) {
		return dials[i];
	}

	public boolean isLocked(int i) {
		return locked[i];
	}

	public boolean isForfeit(int i) {
		return forfeit[i];
	}

	public int heat(int i) {
		return heat[i];
	}

	public int attempts() {
		return attempts;
	}

	public int lockedCount() {
		int c = 0;
		for (boolean b : locked) if (b) c++;
		return c;
	}

	public void turn(int i, int delta) {
		if (locked[i]) return;
		dials[i] = Math.max(0, Math.min(255, dials[i] + delta));
		GameScene.updateItemDisplays = true;
		updateQuickslot();
	}

	public int rgb() {
		int[] sum = new int[3], count = new int[3];
		for (int i = 0; i < dials.length; i++) {
			sum[i % 3] += dials[i];
			count[i % 3]++;
		}
		int r = sum[0] / Math.max(1, count[0]);
		int g = sum[1] / Math.max(1, count[1]);
		int b = sum[2] / Math.max(1, count[2]);
		return (r << 16) | (g << 8) | b;
	}

	@Override
	public int tint() {
		return rgb();
	}

	public boolean attempt(Hero hero) {
		attempts++;
		boolean anyWrong = false;
		int newlyLocked = 0;
		for (int i = 0; i < dials.length; i++) {
			if (locked[i]) {
				heat[i] = EXACT;
				continue;
			}
			int d = Math.abs(dials[i] - target[i]);
			heat[i] = d == 0 ? EXACT : d <= burning() ? BURNING : d <= hot() ? HOT : d <= warm() ? WARM : COLD;
			if (d == 0) {
				locked[i] = true;
				newlyLocked++;
			} else {
				anyWrong = true;
			}
		}

		if (newlyLocked > 0) {
			Sample.INSTANCE.play(Assets.Sounds.UNLOCK);
			GLog.p(Messages.get(this, "dial_locked", newlyLocked));
		}

		if (!anyWrong) {
			return finish(hero, "opened");
		}

		lives--;
		if (lives <= 0) {
			return finish(hero, "out_of_lives");
		}
		GLog.w(Messages.get(this, "life_lost", lives));
		GameScene.updateItemDisplays = true;
		return false;
	}

	public boolean lockDial(Hero hero, int i) {
		if (locked[i]) return false;
		locked[i] = true;
		forfeit[i] = true;
		heat[i] = -1;
		if (lockedCount() == dials.length) return finish(hero, "opened");
		return false;
	}

	public void lockAll(Hero hero) {
		finish(hero, "cashed_out");
	}

	private boolean finish(Hero hero, String messageKey) {
		int correct = 0, items = 0;
		for (int i = 0; i < dials.length; i++) {
			if (locked[i] && !forfeit[i]) {
				correct++;
				items += ThreadLocalRandom.current().nextInt(minItems, maxItems + 1);
			} else if (!locked[i]) {
				locked[i] = true;
				forfeit[i] = true;
			}
		}
		int totalDials = dials.length;
		int usedAttempts = attempts;

		if (quantity > 1) {
			quantity--;
			freshPuzzle();
			lastFloor = currentFloor();
			updateQuickslot();
		} else {
			detach(hero.belongings.backpack);
		}

		if (items > 0) payOut(hero, items);
		Sample.INSTANCE.play(Assets.Sounds.UNLOCK);
		GLog.p(Messages.get(this, messageKey, usedAttempts, correct, totalDials, items));
		hero.spendAndNext(1f);
		GameScene.updateItemDisplays = true;
		return true;
	}

	private void payOut(Hero hero, int count) {
		for (int i = 0; i < count; i++) {
			Item reward = Generator.random();
			if (reward != null && !reward.doPickUp(hero)) {
				Dungeon.level.drop(reward, hero.pos).sprite.drop();
			}
		}
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		checkFloor();
		ArrayList<String> actions = super.actions(hero);
		actions.add(0, AC_DIAL);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		checkFloor();
		super.execute(hero, action);
		if (action.equals(AC_DIAL)) {
			GameScene.show(new WndPrismBox(this));
		}
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public long value() {
		return 15 * (difficulty.ordinal() + 1);
	}

	@Override
	public Item split(long amount) {
	    Item split = super.split(amount);
	    if (split instanceof PrismBox) {
	        PrismBox box = (PrismBox) split;
	        box.freshPuzzle();
	        box.lastFloor = currentFloor();
	    }
	    return split;
	}

	@Override
	public String desc() {
		checkFloor();
		return Messages.get(this, "desc")
				+ (wildcard && shifts > 0 ? "\n\n" + Messages.get(this, "desc_wild") : "")
				+ "\n\n" + Messages.get(this, "desc_locked", lockedCount(), dials.length, lives, attempts);
	}

	private static final String DIFFICULTY = "difficulty";
	private static final String DIALS = "dials";
	private static final String TARGET = "target";
	private static final String LOCKED = "locked";
	private static final String FORFEIT = "forfeit";
	private static final String HEAT = "heat";
	private static final String ATTEMPTS = "attempts";
	private static final String LIVES = "lives";
	private static final String WILDCARD = "wildcard";
	private static final String LAST_FLOOR = "last_floor";
	private static final String SHIFTS = "shifts";
	private static final String FEEL = "feel";
	private static final String MIN_ITEMS = "min_items";
	private static final String MAX_ITEMS = "max_items";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(DIFFICULTY, difficulty.name());
		bundle.put(DIALS, dials);
		bundle.put(TARGET, target);
		bundle.put(LOCKED, locked);
		bundle.put(FORFEIT, forfeit);
		bundle.put(HEAT, heat);
		bundle.put(ATTEMPTS, attempts);
		bundle.put(LIVES, lives);
		bundle.put(WILDCARD, wildcard);
		bundle.put(LAST_FLOOR, lastFloor);
		bundle.put(SHIFTS, shifts);
		bundle.put(FEEL, feel);
		bundle.put(MIN_ITEMS, minItems);
		bundle.put(MAX_ITEMS, maxItems);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		difficulty = Difficulty.valueOf(bundle.getString(DIFFICULTY));
		dials = bundle.getIntArray(DIALS);
		target = bundle.getIntArray(TARGET);
		locked = bundle.getBooleanArray(LOCKED);
		forfeit = bundle.getBooleanArray(FORFEIT);
		if (forfeit == null || forfeit.length != dials.length) forfeit = new boolean[dials.length];
		heat = bundle.getIntArray(HEAT);
		attempts = bundle.getInt(ATTEMPTS);
		lives = bundle.contains(LIVES) ? bundle.getInt(LIVES) : difficulty.lives;
		wildcard = bundle.getBoolean(WILDCARD);
		lastFloor = bundle.contains(LAST_FLOOR) ? bundle.getInt(LAST_FLOOR) : -1;
		shifts = bundle.getInt(SHIFTS);
		feel = bundle.contains(FEEL) ? bundle.getFloat(FEEL) : difficulty.feel;
		minItems = bundle.contains(MIN_ITEMS) ? bundle.getInt(MIN_ITEMS) : difficulty.minItems;
		maxItems = bundle.contains(MAX_ITEMS) ? bundle.getInt(MAX_ITEMS) : difficulty.maxItems;
	}
}
