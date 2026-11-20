package com.shatteredpixel.shatteredpixeldungeon.actors.blobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.CustomBlobCellEmission;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.watabou.utils.Random;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.utils.Big;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Bundle;

import java.math.BigInteger;
import java.util.HashMap;

public abstract class ConquestZone extends Blob implements CustomBlobCellEmission {

    public BigInteger power = BigInteger.ONE;

    protected abstract int speck();

    protected abstract void affect(Char ch, int cell);

    protected int powerInt() {
        return Big.toInt(power.max(BigInteger.ONE).min(BigInteger.valueOf(1000)));
    }

    public static void cast(Class<? extends ConquestZone> type, int center, int radius, int duration, BigInteger power) {
        Level level = Dungeon.level;
        ConquestZone zone = Blob.seed(center, BigInteger.ZERO, type);
        if (zone.power.compareTo(power) < 0) zone.power = power;
        int w = level.width();
        int cx = center % w;
        int cy = center / w;
        BigInteger d = BigInteger.valueOf(duration);
        for (int y = cy - radius; y <= cy + radius; y++) {
            for (int x = cx - radius; x <= cx + radius; x++) {
                if (x < 0 || y < 0 || x >= w || y >= level.height()) continue;
                int c = x + y * w;
                if (!level.insideMap(c) || level.solid[c]) continue;
                zone.clear(c);
                zone.seed(level, c, d);
            }
        }
        GameScene.add(zone);
    }

    @Override
    protected void evolve() {
        Level level = Dungeon.level;
        int w = level.width();
        for (int i = area.top; i < area.bottom; i++) {
            for (int j = area.left; j < area.right; j++) {
                int cell = j + i * w;
                if (!level.insideMap(cell)) continue;
                BigInteger v = cur[cell];
                if (v.signum() > 0) {
                    Char ch = Actor.findChar(cell);
                    if (ch != null && ch.isAlive()) affect(ch, cell);
                    BigInteger n = v.subtract(BigInteger.ONE);
                    off[cell] = n;
                    volume = volume.add(n);
                } else {
                    off[cell] = BigInteger.ZERO;
                }
            }
        }
    }

    private boolean inside(int cell) {
        return cell >= 0 && cell < cur.length && cur[cell].signum() > 0;
    }

    protected abstract int color();

    @Override
    public void use(BlobEmitter emitter) {
        super.use(emitter);
        emitter.pour(ZoneFx.factory(color()), 0.05f);
    }

    @Override
    public void emit(BlobEmitter emitter, Emitter.Factory factory, int index, int cellX, int cellY, int cell, float tileSize) {
        int w = Dungeon.level.width();
        boolean top = !inside(cell - w);
        boolean left = !inside(cell - 1);
        boolean right = !inside(cell + 1);
        boolean bottom = !inside(cell + w);

        for (int n = 0; n < 2; n++) {
            int k = Random.Int(4);
            float progress = Random.Float(1f);
            float ox = 0f;
            float oy = 0f;
            boolean emit = false;
            if (k == 0 && top) {
                ox = progress;
                oy = 0f;
                emit = true;
            } else if (k == 1 && right) {
                ox = 1f;
                oy = progress;
                emit = true;
            } else if (k == 2 && bottom) {
                ox = 1 - progress;
                oy = 1f;
                emit = true;
            } else if (k == 3 && left) {
                ox = 0f;
                oy = 1 - progress;
                emit = true;
            }
            if (emit) {
                float shrunk = tileSize * 0.9f;
                factory.emit(emitter, index, cellX * tileSize + ox * shrunk, cellY * tileSize + oy * shrunk);
            }
        }
    }

    public static class ZoneFx extends FlameParticle {
        public static ZoneFx emitOne(Emitter emitter, float x, float y, int color) {
            ZoneFx fx = (ZoneFx) emitter.recycle(ZoneFx.class);
            fx.reset(x, y);
            fx.color(color);
            fx.acc.set(0f, -80f);
            fx.size = 3f;
            return fx;
        }

        public static Emitter.Factory factory(final int color) {
            return new Emitter.Factory() {
                @Override
                public void emit(Emitter emitter, int index, float x, float y) {
                    emitOne(emitter, x, y, color);
                }

                @Override
                public boolean lightMode() {
                    return true;
                }
            };
        }
    }

    @Override
    public String tileDesc() {
        return Messages.get(this, "desc");
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("power", power.toString());
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        if (bundle.contains("power")) {
            try {
                power = new BigInteger(bundle.getString("power"));
            } catch (Exception e) {
                power = BigInteger.ONE;
            }
        }
    }

    public static class Harm extends ConquestZone {
        @Override
        protected int color() {
            return 0xCC3333;
        }

        @Override
        protected int speck() {
            return Speck.RED_LIGHT;
        }

        @Override
        protected void affect(Char ch, int cell) {
            if (ch.alignment != Char.Alignment.ENEMY) return;
            BigInteger p = power.max(BigInteger.ONE);
            BigInteger dmg = Hero.heroDamageIntRange(BigInteger.ONE.add(p.divide(BigInteger.valueOf(2))), BigInteger.valueOf(3).add(p));
            ch.damage(dmg, this);
        }
    }

    public static class Mend extends ConquestZone {
        @Override
        protected int color() {
            return 0x33CC66;
        }

        @Override
        protected int speck() {
            return Speck.HEALING;
        }

        @Override
        protected void affect(Char ch, int cell) {
            if (ch.alignment != Char.Alignment.ALLY || ch.HP.compareTo(ch.HT) >= 0) return;
            int pct = Math.min(25, 3 + powerInt() / 2);
            BigInteger amount = ch.HT.multiply(BigInteger.valueOf(pct)).divide(BigInteger.valueOf(100)).max(BigInteger.ONE);
            ch.HP = ch.HT.min(ch.HP.add(amount));
            if (ch.sprite != null) {
                ch.sprite.emitter().burst(Speck.factory(Speck.HEALING), 1);
                ch.sprite.showStatusWithIcon(CharSprite.POSITIVE, amount.toString(), FloatingText.HEALING);
            }
        }
    }

    public static class Aegis extends ConquestZone {
        @Override
        protected int color() {
            return 0x66AAFF;
        }

        @Override
        protected int speck() {
            return Speck.LIGHT;
        }

        @Override
        protected void affect(Char ch, int cell) {
            if (ch.alignment != Char.Alignment.ALLY) return;
            int pct = Math.min(30, 5 + powerInt());
            BigInteger amount = ch.HT.multiply(BigInteger.valueOf(pct)).divide(BigInteger.valueOf(100)).max(BigInteger.ONE);
            Barrier b = Buff.affect(ch, Barrier.class);
            if (b.shielding().compareTo(amount) < 0) b.setShield(amount);
        }
    }

    public static class Stasis extends ConquestZone {
        @Override
        protected int color() {
            return 0xFFDD33;
        }

        private final HashMap<Integer, Float> rest = new HashMap<>();

        @Override
        protected int speck() {
            return Speck.PARALYSIS;
        }

        @Override
        protected void affect(Char ch, int cell) {
            if (ch.alignment != Char.Alignment.ENEMY) return;
            if (Char.hasProp(ch, Char.Property.BOSS) || Char.hasProp(ch, Char.Property.MINIBOSS)) return;
            Float until = rest.get(ch.id());
            if (until != null && until > Actor.now()) return;
            Buff.prolong(ch, Paralysis.class, 1f);
            rest.put(ch.id(), Actor.now() + 8f);
        }
    }
}
