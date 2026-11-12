package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;



import com.shatteredpixel.shatteredpixeldungeon.utils.Big;
import java.math.BigInteger;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ShieldBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Ankh;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;

import java.util.ArrayList;

public class EntrophicalCup extends Artifact {

    public static final String AC_SACRIFICE = "SACRIFICE";
    private static final int SHIELD_BASE = 5;
    private static final int SHIELD_PER_LEVEL = 5;
    private static final float REGEN_BASE = 0.1f;
    private static final float REGEN_PER_LEVEL = 0.05f;

    {
        image = ItemSpriteSheet.TROPHY;
        defaultAction = AC_SACRIFICE;

        levelCap = 10;

        charge = BigInteger.ZERO;
        partialCharge = 0;
        chargeCap = BigInteger.ZERO;
    }

    public BigInteger maxShield() {
        return (level().compareTo(BigInteger.ZERO) <= 0) ? BigInteger.ZERO : (BigInteger.valueOf(SHIELD_BASE).add((BigInteger.valueOf(SHIELD_PER_LEVEL).multiply(level())))).multiply(getRarityMultiplier());
    }

    public float regenPerTurn() {
        return ((level().compareTo(BigInteger.ZERO) <= 0) ? Big.dec(0f) : (Big.dec(REGEN_BASE).add((Big.dec(REGEN_PER_LEVEL).multiply(Big.dec(level()))))).multiply(Big.dec(getRarityMultiplier()))).floatValue();
    }

    private static Ankh findAnkh(Hero hero) {
        return hero.belongings.getItem(Ankh.class);
    }

    @Override
    public ArrayList<String> actions(Hero hero ) {
        ArrayList<String> actions = super.actions( hero );
        if (isEquipped( hero ) && !cursed && hero.buff(MagicImmune.class) == null
                && (level().compareTo(BigInteger.valueOf(levelCap)) < 0) && findAnkh(hero) != null) {
                actions.add(AC_SACRIFICE);
        }
        return actions;
    }

    @Override
    public void execute(Hero hero, String action ) {

        super.execute(hero, action);

        if (hero.buff(MagicImmune.class) != null) return;

        if (action.equals(AC_SACRIFICE)){
            if (!isEquipped(hero))              GLog.i( Messages.get(this, "need_to_equip"));
            else if (cursed)                    GLog.w( Messages.get(this, "cursed"));
            else if ((level().compareTo(BigInteger.valueOf(levelCap)) >= 0))       GLog.i( Messages.get(this, "max_level"));
            else if (findAnkh(hero) == null)    GLog.w( Messages.get(this, "need_ankhs"));
            else {
                GameScene.selectItem(sacrificeSelector);
            }
        }

        updateQuickslot();
    }

    // the sacrificer window lol
    private final WndBag.ItemSelector sacrificeSelector = new WndBag.ItemSelector() {

        @Override
        public String textPrompt() {
            return Messages.get(EntrophicalCup.this, "prompt");
        }

        @Override
        public Class<? extends Bag> preferredBag() {
            return Belongings.Backpack.class;
        }

        @Override
        public boolean itemSelectable(Item item) {
            return item instanceof Ankh;
        }

        @Override
        public void onSelect( Item item ) {
            if (!(item instanceof Ankh)) return;
            sacrifice(Dungeon.hero, (Ankh) item);
        }
    };

    private void sacrifice(Hero hero, Ankh ankh) {
        if (!isEquipped(hero) || cursed || hero.buff(MagicImmune.class) != null) return;
        if ((level().compareTo(BigInteger.valueOf(levelCap)) >= 0)) {
            GLog.i(Messages.get(this, "max_level"));
            return;
        }

        BigInteger gain = BigInteger.valueOf(ankh.isBlessed() ? 2 : 1).min(BigInteger.valueOf(levelCap).subtract(level()));

        ankh.detach(hero.belongings.backpack);

        for (int i = 0; (BigInteger.valueOf(i).compareTo(gain) < 0); i++) {
            upgrade();
        }

        hero.sprite.operate(hero.pos);
        hero.sprite.emitter().burst(ShadowParticle.UP, Big.toInt(BigInteger.valueOf(4).multiply(gain).min(BigInteger.valueOf(40))));
        hero.spend(1f);
        hero.busy();
        GLog.p(Messages.get(this, "sacrificed", level()));

        refillShield(hero);
        updateQuickslot();
    }

    @Override
    public Item upgrade() {
        super.upgrade();
        chargeCap = maxShield();
        return this;
    }

    @Override
    public void restoreFromBundle(com.watabou.utils.Bundle bundle) {
        super.restoreFromBundle(bundle);
        chargeCap = maxShield();
    }

    private void refillShield(Hero hero) {
        BigInteger max = maxShield();
        if ((max.compareTo(BigInteger.ZERO) <= 0)) return;
        EntropyShield shield = Buff.affect(hero, EntropyShield.class);
        shield.setShield(max);
        syncCharge(shield);
    }

    private void syncCharge(ShieldBuff shield) {
        BigInteger cur = shield == null ? BigInteger.ZERO : shield.shielding();
        charge = BigInteger.valueOf(Big.toInt(cur.min(maxShield())));
    }

    @Override
    public boolean doUnequip(Hero hero, boolean collect, boolean single) {
        if (super.doUnequip(hero, collect, single)) {
            EntropyShield shield = hero.buff(EntropyShield.class);
            if (shield != null) shield.detach();
            charge = BigInteger.ZERO;
            partialCharge = 0;
            return true;
        }
        return false;
    }

    @Override
    protected ArtifactBuff passiveBuff() {
        return new entropyRegen();
    }

    public class entropyRegen extends ArtifactBuff {

        @Override
        public boolean act() {
            Hero hero = target instanceof Hero ? (Hero) target : null;

            if (hero != null && !cursed && hero.buff(MagicImmune.class) == null
                    && (level().compareTo(BigInteger.ZERO) > 0) && hero.isAlive()) {

                chargeCap = maxShield();
                EntropyShield shield = hero.buff(EntropyShield.class);
                BigInteger cur = shield == null ? BigInteger.ZERO : shield.shielding();

                if ((cur.compareTo(chargeCap) < 0)) {
                    partialCharge += regenPerTurn();
                    while (partialCharge >= 1f && (cur.compareTo(chargeCap) < 0)) {
                        partialCharge -= 1f;
                        if (shield == null) shield = Buff.affect(hero, EntropyShield.class);
                        shield.incShield(BigInteger.ONE);
                        cur = cur.add(BigInteger.ONE);
                    }
                } else {
                    partialCharge = 0;
                }
                syncCharge(shield);
            }

            updateQuickslot();
            spend(TICK);
            return true;
        }
    }

    public static class EntropyShield extends ShieldBuff {

        {
            type = buffType.POSITIVE;
        }

    }

    @Override
    public String desc() {
        String desc = super.desc();

        if (isEquipped(Dungeon.hero)) {
            desc += "\n\n";
            if (cursed) {
                desc += Messages.get(this, "desc_cursed");
            } else if ((level().compareTo(BigInteger.ZERO) == 0)) {
                desc += Messages.get(this, "desc_empty");
            } else {
                desc += Messages.get(this, "desc_active", maxShield(), regenPerTurn());
            }
        }

        if ((level().compareTo(BigInteger.valueOf(levelCap)) < 0)) {
            desc += "\n\n" + Messages.get(this, "desc_sacrifice");
        } else {
            desc += "\n\n" + Messages.get(this, "desc_max");
        }
        return desc;
    }
}