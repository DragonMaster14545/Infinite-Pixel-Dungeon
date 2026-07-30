package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
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
        image = ItemSpriteSheet.SOMETHING;
        hitSound = Assets.Sounds.HIT_SLASH;
        hitSoundPitch = 1.05f;

        tier = 4;
        DLY = 1.1f; //slightly slower than a plain sword, the ability carries the weight
    }

    @Override
    public long max(long lvl) {
        return  Math.round(4.5d*(tier+1)) +    //22 base
                lvl*Math.round(0.9d*(tier+1));  //+4-5 per level
    }

    @Override
    public String targetingPrompt() {
        return Messages.get(this, "prompt");
    }

    @Override
    protected void duelistAbility(Hero hero, Integer target) {
        long dmgBoost = augment.damageFactor(6 + buffedLvl());
        SearingSlasher.searingDashAbility(hero, target, 1, dmgBoost, this);
    }

    @Override
    public String abilityInfo() {
        long dmgBoost = levelKnown ? 6 + buffedLvl() : 6;
        if (levelKnown){
            return Messages.get(this, "ability_desc", augment.damageFactor(min()+dmgBoost), augment.damageFactor(max()+dmgBoost));
        } else {
            return Messages.get(this, "typical_ability_desc", min(0)+dmgBoost, max(0)+dmgBoost);
        }
    }

    public String upgradeAbilityStat(int level){
        int dmgBoost = 6 + level;
        return augment.damageFactor(min(level)+dmgBoost) + "-" + augment.damageFactor(max(level)+dmgBoost);
    }

    public static void searingDashAbility(Hero hero, Integer target, long dmgMulti, long dmgBoost, MeleeWeapon wep){
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
                    if (hero.attack(enemy, dmgMulti, dmgBoost, Char.INFINITE_ACCURACY)) {
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
                WandOfBlastWave.throwChar(hero, dash, endIdx, true, false, hero);

                Sample.INSTANCE.play(hitAnything ? Assets.Sounds.HIT_STRONG : Assets.Sounds.MISS);

                Invisibility.dispel();
                hero.spendAndNext(hero.attackDelay());
                wep.afterAbilityUsed(hero);
            }
        });
    }
}