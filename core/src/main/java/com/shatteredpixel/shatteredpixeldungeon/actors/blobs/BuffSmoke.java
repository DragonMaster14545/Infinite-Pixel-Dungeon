package com.shatteredpixel.shatteredpixeldungeon.actors.blobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bless;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Light;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MindVision;
import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.Random;

import java.math.BigInteger;

public class BuffSmoke extends Blob {

	private static final Object[][] EFFECTS = {
			new Object[]{ Bless.class, 8f },
			new Object[]{ Haste.class, 5f },
			new Object[]{ Invisibility.class, 5f },
			new Object[]{ MindVision.class, 8f },
			new Object[]{ Light.class, 15f }
	};

	@SuppressWarnings("unchecked")
	@Override
	protected void evolve() {
		super.evolve();

		Char ch;
		int cell;

		for (int i = area.left; i < area.right; i++){
			for (int j = area.top; j < area.bottom; j++){
				cell = i + j*Dungeon.level.width();
				if ((cur[cell].compareTo(BigInteger.ZERO) > 0) && (ch = Actor.findChar( cell )) != null) {
					if (Random.Float() >= 0.35f) continue;
					Object[] effect = EFFECTS[Random.Int(EFFECTS.length)];
					Class<? extends FlavourBuff> buffClass = (Class<? extends FlavourBuff>) effect[0];
					if (ch.isImmune(buffClass)) continue;
					Buff.prolong(ch, buffClass, (Float) effect[1]);
				}
			}
		}
	}

	@Override
	public void use( BlobEmitter emitter ) {
		super.use( emitter );

		emitter.pour( Speck.factory( Speck.LIGHT ), 0.4f );
	}

	@Override
	public String tileDesc() {
		return Messages.get(this, "desc");
	}
}
