package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

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

        charge = 0;
        partialCharge = 0;
        chargeCap = 0;
    }

    public long maxShield() {
        return level() <= 0 ? 0 : (SHIELD_BASE + (SHIELD_PER_LEVEL * level())) * getRarityMultiplier();
    }

    public float regenPerTurn() {
        return level() <= 0 ? 0f : (REGEN_BASE + (REGEN_PER_LEVEL * level())) * getRarityMultiplier();
    }

    private static Ankh findAnkh(Hero hero) {
        return hero.belongings.getItem(Ankh.class);
    }

    @Override
    public ArrayList<String> actions(Hero hero ) {
        ArrayList<String> actions = super.actions( hero );
        if (isEquipped( hero ) && !cursed && hero.buff(MagicImmune.class) == null
                && level() < levelCap && findAnkh(hero) != null) {
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
            else if (level() >= levelCap)       GLog.i( Messages.get(this, "max_level"));
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
        if (level() >= levelCap) {
            GLog.i(Messages.get(this, "max_level"));
            return;
        }

        long gain = Math.min(ankh.isBlessed() ? 2 : 1, levelCap - level());

        ankh.detach(hero.belongings.backpack);

        for (int i = 0; i < gain; i++) {
            upgrade();
        }

        hero.sprite.operate(hero.pos);
        hero.sprite.emitter().burst(ShadowParticle.UP, (int) Math.min(4 * gain, 40));
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
        long max = maxShield();
        if (max <= 0) return;
        EntropyShield shield = Buff.affect(hero, EntropyShield.class);
        shield.setShield(max);
        syncCharge(shield);
    }

    private void syncCharge(ShieldBuff shield) {
        long cur = shield == null ? 0 : shield.shielding();
        charge = (int) Math.min(cur, maxShield());
    }

    @Override
    public boolean doUnequip(Hero hero, boolean collect, boolean single) {
        if (super.doUnequip(hero, collect, single)) {
            EntropyShield shield = hero.buff(EntropyShield.class);
            if (shield != null) shield.detach();
            charge = 0;
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
                    && level() > 0 && hero.isAlive()) {

                chargeCap = maxShield();
                EntropyShield shield = hero.buff(EntropyShield.class);
                long cur = shield == null ? 0 : shield.shielding();

                if (cur < chargeCap) {
                    partialCharge += regenPerTurn();
                    while (partialCharge >= 1f && cur < chargeCap) {
                        partialCharge -= 1f;
                        if (shield == null) shield = Buff.affect(hero, EntropyShield.class);
                        shield.incShield(1);
                        cur++;
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
            } else if (level() == 0) {
                desc += Messages.get(this, "desc_empty");
            } else {
                desc += Messages.get(this, "desc_active", maxShield(), regenPerTurn());
            }
        }

        if (level() < levelCap) {
            desc += "\n\n" + Messages.get(this, "desc_sacrifice");
        } else {
            desc += "\n\n" + Messages.get(this, "desc_max");
        }
        return desc;
    }
}