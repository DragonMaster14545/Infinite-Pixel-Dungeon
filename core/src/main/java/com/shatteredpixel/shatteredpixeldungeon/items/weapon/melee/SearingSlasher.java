package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;



import com.shatteredpixel.shatteredpixeldungeon.utils.Big;
import java.math.BigInteger;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Electricity;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Blueprint;
import com.shatteredpixel.shatteredpixeldungeon.items.BlueprintRecipe;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.blueprintitems.SearingSlasherBlueprint;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTransmutation;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.Alchemize;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfTerraforming;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfTransfusion;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.AttackIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

import java.util.ArrayList;

public class SearingSlasher extends MeleeWeapon {

    // TODO make a recipe for this.. I deleted the last recipe function before

    private static final int DASH_RANGE = 4;

    {
        image = ItemSpriteSheet.SEARING_SLASHER;
        hitSound = Assets.Sounds.HIT_SLASH;
        hitSoundPitch = 1.05f;

        tier = 4;
        DLY = 1.1f; //slightly slower than a plain sword, the ability carries the weight
    }

    @Override
    public BigInteger max(BigInteger lvl) {
        return  BigInteger.valueOf(Math.round(4.5d*(tier+1))).add(lvl.multiply(BigInteger.valueOf(Math.round(0.9d*(tier+1)))));  //+4-5 per level
    }

    @Override
    public BigInteger proc(Char attacker, Char defender, BigInteger damage) {

        defender.damage(Big.round(Big.dec(Dungeon.hero.damageRoll()).multiply(Big.dec(0.05d))), new Burning());
        defender.damage(Big.round(Big.dec(Dungeon.hero.damageRoll()).multiply(Big.dec(0.05d))), new Chill());
        defender.damage(Big.round(Big.dec(Dungeon.hero.damageRoll()).multiply(Big.dec(0.05d))), new Electricity());

        return super.proc(attacker, defender, damage);
    }

    @Override
    public String targetingPrompt() {
        return Messages.get(this, "prompt");
    }

    @Override
    protected void duelistAbility(Hero hero, Integer target) {
        BigInteger dmgBoost = augment.damageFactor(BigInteger.valueOf(6).add(buffedLvl()));
        SearingSlasher.searingDashAbility(hero, target, BigInteger.ONE, dmgBoost, this);
    }

    @Override
    public String abilityInfo() {
        BigInteger dmgBoost = levelKnown ? BigInteger.valueOf(6).add(buffedLvl()) : BigInteger.valueOf(6);
        if (levelKnown){
            return Messages.get(this, "ability_desc", augment.damageFactor(min().add(dmgBoost)), augment.damageFactor(max().add(dmgBoost)));
        } else {
            return Messages.get(this, "typical_ability_desc", min(BigInteger.ZERO).add(dmgBoost), max(BigInteger.ZERO).add(dmgBoost));
        }
    }

    public String upgradeAbilityStat(int level){
        int dmgBoost = 6 + level;
        return augment.damageFactor(min(BigInteger.valueOf(level)).add(BigInteger.valueOf(dmgBoost))) + "-" + augment.damageFactor(max(BigInteger.valueOf(level)).add(BigInteger.valueOf(dmgBoost)));
    }

    public static void searingDashAbility(Hero hero, Integer target, BigInteger dmgMulti, BigInteger dmgBoost, MeleeWeapon wep){
        if (target == null) {
            return;
        }

        if (target == hero.pos || !Dungeon.level.heroFOV[target]) {
            GLog.w(Messages.get(wep, "ability_no_target"));
            return;
        }

        hero.belongings.abilityWeapon = wep;

        Ballistica dash = new Ballistica(hero.pos, target, Ballistica.STOP_SOLID);
        if (dash.path.size() <= 1) {
            GLog.w(Messages.get(wep, "ability_no_target"));
            hero.belongings.abilityWeapon = null;
            return;
        }

        hero.belongings.abilityWeapon = null;

        final int endIdx = Math.min(dash.path.size() - 1, DASH_RANGE);
        final int endCell = dash.path.get(endIdx);

        hero.sprite.attack(endCell, new Callback() {
            @Override
            public void call() {

                wep.beforeAbilityUsed(hero, null);

                //every char standing on a crossed tile (excluding the hero's own tile)
                ArrayList<Char> struck = new ArrayList<>();
                for (int i = 1; i <= endIdx; i++) {
                    Char ch = Actor.findChar(dash.path.get(i));
                    if (ch != null && ch != hero && !hero.isCharmedBy(ch)) {
                        struck.add(ch);
                    }
                }

                boolean hitAnything = false;
                for (Char enemy : struck) {
                    AttackIndicator.target(enemy);
                    if (hero.attack(enemy, dmgMulti.floatValue(), dmgBoost.floatValue(), Char.INFINITE_ACCURACY)) {
                        hitAnything = true;
                        CellEmitter.get(enemy.pos).burst(FlameParticle.FACTORY, 2);
                        if (enemy.isAlive()) {
                            Buff.affect(enemy, Burning.class).reignite(enemy, 2f);
                        } else {
                            wep.onAbilityKill(hero, enemy);
                        }
                    }
                }

                //physically move the hero to the far end of the dash - reuses the
                //same push mechanism Spear/WandOfBlastWave use to move a char
                //along a Ballistica, just aimed at the hero instead of a target
                WandOfBlastWave.throwChar(hero, dash, BigInteger.valueOf(endIdx), true, false, hero);

                Sample.INSTANCE.play(hitAnything ? Assets.Sounds.HIT_STRONG : Assets.Sounds.MISS);

                Invisibility.dispel();
                hero.spendAndNext(hero.attackDelay());
                wep.afterAbilityUsed(hero);
            }
        });
    }

    public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe.SimpleRecipe {

        {
            inputs =  new Class[]{Blueprint.class, ScrollOfTransmutation.class, WandOfTerraforming.class};
            inQuantity = new int[]{1, 1, 1};

            cost = 150;

            output = SearingSlasherBlueprint.class;
            outQuantity = 1;
        }

    }
}