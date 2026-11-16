package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.Big;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.TreeMap;

public class FloorMod extends ChampionEnemy {

	public enum Type {
		LIFESTEAL(0.08f, 0.25f),
		BRUTE(1.2f, 1.6f),
		ARMORED(0.6f, 0.8f),
		FRAGILE(1.25f, 1.5f),
		HEARTY(1.3f, 2f),
		SWIFT(1.25f, 1.6f),
		SHARP(1.25f, 1.5f),
		CRIPPLING(2f, 5f);

		final float min, max;

		Type(float min, float max) {
			this.min = min;
			this.max = max;
		}

		boolean buff() {
			return this != FRAGILE;
		}
	}

	{
		color = 0xAA55FF;
	}

	private Type type = Type.BRUTE;
	private float value = 1f;

	public FloorMod set(Type type, float value) {
		this.type = type;
		this.value = value;
		return this;
	}

	public Type type() {
		return type;
	}

	public float value() {
		return value;
	}

	public static String describe(Type type, float value) {
		int pct;
		switch (type) {
			case LIFESTEAL:
				pct = Math.round(value * 100f);
				break;
			case ARMORED:
				pct = Math.round((1f - value) * 100f);
				break;
			case CRIPPLING:
				pct = Math.round(value);
				break;
			default:
				pct = Math.round((value - 1f) * 100f);
		}
		return Messages.get(FloorMod.class, type.name().toLowerCase() + "_short", pct);
	}

	@Override
	public void fx(boolean on) {
	}

	@Override
	public String name() {
		return Messages.get(FloorMod.class, type.name().toLowerCase() + "_name");
	}

	@Override
	public String desc() {
		return describe(type, value);
	}

	@Override
	public BigInteger onAttackDamage(Char enemy, BigInteger damage) {
		if (type == Type.LIFESTEAL && target != null && damage.signum() > 0) {
			BigInteger heal = Big.round(Big.dec(damage).multiply(Big.dec(value))).max(BigInteger.ONE);
			target.HP = target.HP.add(heal).min(target.HT);
		}
		return damage;
	}

	@Override
	public void onAttackProc(Char enemy) {
		if (type == Type.CRIPPLING && enemy != null) {
			Buff.prolong(enemy, Cripple.class, value);
		}
	}

	@Override
	public float meleeDamageFactor() {
		return type == Type.BRUTE ? value : 1f;
	}

	@Override
	public float damageTakenFactor() {
		return (type == Type.ARMORED || type == Type.FRAGILE) ? value : 1f;
	}

	@Override
	public float evasionAndAccuracyFactor() {
		return type == Type.SHARP ? value : 1f;
	}

	@Override
	public float speedFactor() {
		return type == Type.SWIFT ? value : 1f;
	}

	private static final String TYPE = "fm_type";
	private static final String VALUE = "fm_value";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(TYPE, type.name());
		bundle.put(VALUE, value);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		type = Type.valueOf(bundle.getString(TYPE));
		value = bundle.getFloat(VALUE);
	}

	public static void rollForLevel(Level level) {
		level.floorModText = null;
		if (Dungeon.branch != 0 || Dungeon.depth <= 0 || Dungeon.depth % 5 == 0) return;
		if (Random.Int(2) != 0) return;

		TreeMap<String, ArrayList<Mob>> groups = new TreeMap<>();
		for (Mob m : level.mobs) {
			if (m.alignment != Char.Alignment.ENEMY) continue;
			if (m.properties().contains(Char.Property.BOSS) || m.properties().contains(Char.Property.MINIBOSS)) continue;
			String k = m.getClass().getName();
			ArrayList<Mob> l = groups.get(k);
			if (l == null) groups.put(k, l = new ArrayList<>());
			l.add(m);
		}
		if (groups.isEmpty()) return;

		ArrayList<String> keys = new ArrayList<>(groups.keySet());
		ArrayList<Mob> targets = groups.get(Random.element(keys));

		ArrayList<Type> pool = new ArrayList<>();
		for (Type t : Type.values()) pool.add(t);
		int n = Random.Int(10) < 3 ? 2 : 1;

		StringBuilder sb = new StringBuilder();
		String mobName = targets.get(0).name();
		for (int i = 0; i < n; i++) {
			Type t = Random.element(pool);
			pool.remove(t);
			float v = Random.Float(t.min, t.max);
			if (t == Type.CRIPPLING) v = Math.round(v);
			else v = Math.round(v * 100f) / 100f;

			for (Mob m : targets) {
				if (t == Type.HEARTY) {
					m.HT = Big.round(Big.dec(m.HT).multiply(Big.dec(v)));
					m.HP = m.HT;
				}
				Buff.affect(m, FloorMod.class).set(t, v);
			}
			sb.append("\n_-_ ").append(describe(t, v));
		}
		level.floorModText = Messages.get(FloorMod.class, "note", mobName, sb.toString());
	}
}
