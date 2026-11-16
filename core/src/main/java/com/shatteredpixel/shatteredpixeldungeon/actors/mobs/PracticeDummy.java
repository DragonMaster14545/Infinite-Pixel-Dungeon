package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.StatueSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.Big;

import java.math.BigInteger;

public class PracticeDummy extends Mob {

	{
		spriteClass = StatueSprite.class;

		HP = HT = BigInteger.TEN.pow(12);
		defenseSkill = BigInteger.ZERO;
		EXP = BigInteger.ZERO;
		state = PASSIVE;
		alignment = Alignment.ENEMY;

		properties.add(Property.IMMOVABLE);
		properties.add(Property.INORGANIC);
	}

	@Override
	public void damage(BigInteger dmg, Object src) {
		if (dmg.signum() > 0 && sprite != null) {
			sprite.showStatus(CharSprite.WARNING, Big.fmt(dmg));
		}
		HP = HT;
	}

	@Override
	public void die(Object cause) {
		HP = HT;
	}

	@Override
	public BigInteger damageRoll() {
		return BigInteger.ZERO;
	}

	@Override
	public BigInteger attackSkill(Char target) {
		return BigInteger.ZERO;
	}

	@Override
	protected boolean act() {
		spend(TICK);
		return true;
	}

	@Override
	public boolean reset() {
		return true;
	}
}
