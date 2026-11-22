package com.shatteredpixel.shatteredpixeldungeon.items.wands;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ConquestZone;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ElementZone;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.Big;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

import java.math.BigInteger;
import java.util.ArrayList;

public class WandOfElemental extends Wand {

    public static final String AC_MODE = "MODE";

    {
        image = ItemSpriteSheet.ELEMENTAL_WAND;
    }

    private int mode = -1;

    @Override
    public int collisionProperties(int target) {
        return Ballistica.STOP_TARGET;
    }

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        actions.add(AC_MODE);
        return actions;
    }

    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);
        if (action.equals(AC_MODE)) chooseMode();
    }

    private void chooseMode() {
        GameScene.show(new WndOptions(new ItemSprite(this),
                Messages.titleCase(name()),
                Messages.get(this, "choose"),
                Messages.get(this, "mode_0"),
                Messages.get(this, "mode_1"),
                Messages.get(this, "mode_2"),
                Messages.get(this, "mode_3")) {
            @Override
            protected void onSelect(int index) {
                mode = index;
                GLog.p(Messages.get(WandOfElemental.class, "chosen", Messages.get(WandOfElemental.class, "mode_" + index)));
            }
        });
    }

    @Override
    public boolean tryToZap(Hero owner, int target) {
        if (mode < 0) {
            chooseMode();
            return false;
        }
        return super.tryToZap(owner, target);
    }

    private int levelInt() {
        return Big.toInt(buffedLvl().max(BigInteger.ZERO).min(BigInteger.valueOf(100)));
    }

    private static int radiusFor(int lvl) {
        return Math.min(6, 1 + lvl / 15);
    }

    private static int durationFor(int lvl) {
        return 8 + Math.min(lvl, 120);
    }

    @Override
    public void onZap(Ballistica bolt) {
        if (mode < 0) mode = 0;
        int lvl = levelInt();
        Class<? extends ConquestZone> type;
        switch (mode) {
            case 0:
                type = ElementZone.Blaze.class;
                break;
            case 1:
                type = ElementZone.Frost.class;
                break;
            case 2:
                type = ElementZone.Venom.class;
                break;
            default:
                type = ElementZone.Storm.class;
        }
        ConquestZone.cast(type, bolt.collisionPos, radiusFor(lvl), durationFor(lvl), buffedLvl().max(BigInteger.ZERO));
    }

    @Override
    public void fx(Ballistica bolt, Callback callback) {
        MagicMissile m = MagicMissile.boltFromChar(curUser.sprite.parent,
                MagicMissile.ELEMENTAL,
                curUser.sprite,
                bolt.collisionPos,
                callback);
        if (bolt.dist > 10) {
            m.setSpeed(bolt.dist * 20);
        }
        Sample.INSTANCE.play(Assets.Sounds.ZAP);
    }

    @Override
    public void onHit(MagesStaff staff, Char attacker, Char defender, BigInteger damage) {
    }

    @Override
    public void staffFx(MagesStaff.StaffParticle particle) {
        particle.color(0xff171b);
        particle.am = 0.3f;
        particle.setLifespan(3f);
        particle.speed.polar(Random.Float(PointF.PI2), 0.3f);
        particle.setSize(1f, 2f);
        particle.radiateXY(2.5f);
    }

    @Override
    public String statsDesc() {
        int lvl = levelKnown ? levelInt() : 0;
        int side = radiusFor(lvl) * 2 + 1;
        String modeName = mode < 0 ? Messages.get(this, "mode_none") : Messages.get(this, "mode_" + mode);
        String s = Messages.get(this, "stats_desc", modeName, side, side, durationFor(lvl));
        if (levelKnown) s += "\n\n" + Messages.get(Wand.class, "charges", curCharges, getMaxCharges());
        return s;
    }

    @Override
    public String upgradeStat1(BigInteger level) {
        int l = Big.toInt(level.max(BigInteger.ZERO).min(BigInteger.valueOf(100)));
        int side = radiusFor(l) * 2 + 1;
        return side + "x" + side;
    }

    @Override
    public String upgradeStat2(BigInteger level) {
        int l = Big.toInt(level.max(BigInteger.ZERO).min(BigInteger.valueOf(100)));
        return Integer.toString(durationFor(l));
    }

    private static final String MODE = "conquest_mode";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(MODE, mode);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        if (bundle.contains(MODE)) mode = bundle.getInt(MODE);
    }
}
