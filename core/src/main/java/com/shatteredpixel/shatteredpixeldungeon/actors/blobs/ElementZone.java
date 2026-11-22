package com.shatteredpixel.shatteredpixeldungeon.actors.blobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.watabou.utils.Random;

import java.math.BigInteger;

public abstract class ElementZone extends ConquestZone {

    public static final int[] COLORS = { 0xFF6A1F, 0x7FD8FF, 0x6FCF3F, 0xF2E34A };

    private static boolean foe(Char ch) {
        return ch.alignment == Char.Alignment.ENEMY;
    }

    public static class Blaze extends ElementZone {
        @Override
        protected int color() {
            return COLORS[0];
        }

        @Override
        protected int speck() {
            return Speck.RED_LIGHT;
        }

        @Override
        protected void affect(Char ch, int cell) {
            if (!foe(ch)) return;
            Buff.affect(ch, Burning.class).reignite(ch);
        }
    }

    public static class Frost extends ElementZone {
        @Override
        protected int color() {
            return COLORS[1];
        }

        @Override
        protected int speck() {
            return Speck.BLIZZARD;
        }

        @Override
        protected void affect(Char ch, int cell) {
            if (!foe(ch)) return;
            Buff.prolong(ch, Chill.class, 3f);
        }
    }

    public static class Venom extends ElementZone {
        @Override
        protected int color() {
            return COLORS[2];
        }

        @Override
        protected int speck() {
            return Speck.TOXIC;
        }

        @Override
        protected void affect(Char ch, int cell) {
            if (!foe(ch)) return;
            Buff.affect(ch, Poison.class).extend(1d + powerInt() / 3d);
        }
    }

    public static class Storm extends ElementZone {
        @Override
        protected int color() {
            return COLORS[3];
        }

        @Override
        protected int speck() {
            return Speck.PARALYSIS;
        }

        @Override
        protected void affect(Char ch, int cell) {
            if (!foe(ch)) return;
            if (Random.Int(3) != 0) return;
            BigInteger p = power.max(BigInteger.ONE);
            BigInteger dmg = Hero.heroDamageIntRange(BigInteger.valueOf(2).add(p), BigInteger.valueOf(6).add(p.multiply(BigInteger.valueOf(2))));
            if (ch.sprite != null) ch.sprite.centerEmitter().burst(SparkParticle.FACTORY, 3);
            ch.damage(dmg, this);
        }
    }
}
