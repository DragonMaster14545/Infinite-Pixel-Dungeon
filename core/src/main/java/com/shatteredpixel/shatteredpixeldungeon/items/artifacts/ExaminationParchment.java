/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * Experienced Pixel Dungeon
 * Copyright (C) 2019-2024 Trashbox Bobylev
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEnergy;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTextInput;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class ExaminationParchment extends Artifact {

    {
        image = ItemSpriteSheet.EXAM_PARCHMENT;
        defaultAction = AC_ANSWER;

        levelCap = 10;

        charge = 0;
        partialCharge = 0;
    }

    public static final String AC_ANSWER = "ANSWER";
    public static final String AC_ENERGIZE = "ENERGIZE";

    private int parameter = 0;
    private int parameter2 = 0;
    private String ANSWER = "";
    private int operation = 0;
    private String body = "";
    private int reward = 0;
    public static int totalAnswers = 0;

    private float warmUpDelay;

    @Override
    public ArrayList<String> actions( Hero hero ) {
        ArrayList<String> actions = super.actions( hero );
        if (isEquipped( hero ) && !cursed && hero.buff(MagicImmune.class) == null) {
            actions.add(AC_ANSWER);
            if (level() < levelCap) {
                actions.add(AC_ENERGIZE);
            }
        }
        return actions;
    }

    @Override
    public void execute(Hero hero, String action ) {

        super.execute(hero, action);

        if (hero.buff(MagicImmune.class) != null) return;

        if (action.equals(AC_ANSWER)){
            if (!isEquipped(hero))              GLog.i( Messages.get(this, "need_to_equip") );
            else if (cursed)                    GLog.w( Messages.get(this, "cursed") );
            else if (warmUpDelay > 0)           GLog.w( Messages.get(this, "not_ready") );
            else {
                operation = Random.Int(4);
                if (charge < 51) {
                    parameter = Random.Int(99);
                    parameter2 = Random.Int(99);
                } else if (charge < 101) {
                    parameter = Random.Int(999);
                    parameter2 = Random.Int(999);
                } else if (charge < 501) {
                    parameter = Random.Int(9999);
                    parameter2 = Random.Int(9999);
                } else if (charge < 1001) {
                    parameter = Random.Int(99999);
                    parameter2 = Random.Int(9999);
                } else if (charge < 5001) {
                    parameter = Random.Int(99999);
                    parameter2 = Random.Int(99999);
                } else if (charge > 10001) {
                    parameter = Random.Int(999999);
                    parameter2 = Random.Int(999999);
                } else {
                    parameter = Random.Int(99);
                    parameter2 = Random.Int(99);
                }
                switch (operation) {
                    case 0: default:
                        body = parameter + "+" + parameter2;
                        ANSWER = String.valueOf(parameter + parameter2);
                        reward = Random.Int(5) + 1;
                        break;
                    case 1:
                        body = parameter + "-" + parameter2;
                        ANSWER = String.valueOf(parameter - parameter2);
                        reward = Random.Int(5) + 1;
                        break;
                    case 2:
                        body = parameter + "*" + parameter2;
                        ANSWER = String.valueOf(parameter * parameter2);
                        reward = Random.Int(10) + 1;
                        break;
                    case 3:
                        body = parameter + "/" + parameter2 + "\n_Always round down the answer!_";
                        ANSWER = String.valueOf(parameter / parameter2);
                        reward = Random.Int(7) + 1;
                        break;
                }
                askparameter();
            }

        } else if (action.equals(AC_ENERGIZE)) {
            if (!isEquipped(hero))              GLog.i( Messages.get(this, "need_to_equip") );
            else if (cursed)                    GLog.w( Messages.get(this, "cursed") );
            else if (Dungeon.energy < 10)        GLog.w( Messages.get(this, "need_energy") );
            else {

                final int maxLevels = (int) Math.min(levelCap - level(), Dungeon.energy/10);

                String[] options;
                if (maxLevels > 1){
                    options = new String[]{ Messages.get(this, "energize_1"), Messages.get(this, "energize_all", 10*maxLevels, maxLevels)};
                } else {
                    options = new String[]{ Messages.get(this, "energize_1")};
                }

                GameScene.show(new WndOptions(new ItemSprite(image),
                        Messages.titleCase(name()),
                        Messages.get(this, "energize_desc"),
                        options){
                    @Override
                    protected void onSelect(int index) {
                        super.onSelect(index);

                        if (index == 0){
                            Dungeon.energy -= 10;
                            Sample.INSTANCE.play(Assets.Sounds.DRINK);
                            Sample.INSTANCE.playDelayed(Assets.Sounds.PUFF, 0.5f);
                            Dungeon.hero.sprite.operate(Dungeon.hero.pos);
                            upgrade();
                            Catalog.countUse(ExaminationParchment.class);
                        } else if (index == 1){
                            Dungeon.energy -= 10L *maxLevels;
                            Sample.INSTANCE.play(Assets.Sounds.DRINK);
                            Sample.INSTANCE.playDelayed(Assets.Sounds.PUFF, 0.5f);
                            Dungeon.hero.sprite.operate(Dungeon.hero.pos);
                            upgrade((int) maxLevels);
                            Catalog.countUses(ExaminationParchment.class, maxLevels);
                        }

                    }

                    @Override
                    protected boolean hasIcon(int index) {
                        return true;
                    }

                    @Override
                    protected Image getIcon(int index) {
                        return new ItemSprite(ItemSpriteSheet.ENERGY);
                    }
                });
            }
        }

        updateQuickslot();
    }

    private void askparameter() {
        GameScene.show(new WndTextInput( "Input Answer","Evaluate the following: " + body, "", 5000, false, "Done", "Cancel" ) {
            @Override
            public void onSelect( boolean positive, String text ) {
                if (text.equals(ANSWER)) {
                    GLog.h("You answered the question correctly!");
                    curUser.sprite.emitter().start( Speck.factory( Speck.UP ), 0.2f, 5 );

                    totalAnswers += 1;
                    charge += reward;

                    updateQuickslot();
                    Catalog.countUse(ExaminationParchment.class);

                } else if (text.isEmpty()) {
                    GLog.w("You didn't answer the question.");
                } else {
                    GLog.w("That answer is not equals as the given, try again.");
                    curUser.sprite.emitter().start( Speck.factory( Speck.CONFUSION ), 0.2f, 3 );
                }
            }

            @Override
            public void onBackPressed() {
                GLog.w("You didn't answer the question.");
                this.hide();
            }
        } );
    }

    public int consumeEnergy(long amount){
        long result = amount - charge;
        charge = (int) Math.max(0, charge - amount);
        Talent.onArtifactUsed(Dungeon.hero);
        return (int) Math.max(0, result);
    }

    @Override
    public String status() {
        if (isEquipped(Dungeon.hero) && warmUpDelay > 0 && !cursed){
            return Messages.format( "%d%%", Math.max(0, 100 - (int)warmUpDelay) );
        } else {
            return super.status();
        }
    }

    @Override
    protected ArtifactBuff passiveBuff() {
        return new questionnaireEnergy();
    }

    @Override
    public void charge(Hero target, float amount) {
        if (target.buff(MagicImmune.class) != null) return;
        partialCharge += 0.25f*amount;
        if (partialCharge >= 1){
            long charge = (long)partialCharge;
            partialCharge -= charge;
            this.charge += charge;
            updateQuickslot();
        }
    }

    @Override
    public String desc() {
        String result = Messages.get(this, "desc");

        if (isEquipped(Dungeon.hero)) {
            if (cursed)                 result += "\n\n" + Messages.get(this, "desc_cursed");
            else if (warmUpDelay > 0)   result += "\n\n" + Messages.get(this, "desc_warming");
            else                        result += "\n\n" + Messages.get(this, "desc_hint");
        }

        return result;
    }

    @Override
    public boolean doEquip(Hero hero) {
        if (super.doEquip(hero)){
            warmUpDelay = 101f;
            return true;
        } else {
            return false;
        }
    }

    private static final String WARM_UP = "warm_up";
    private static final String TOTAL_ANSWER = "total_answer";
    private static final String FIRST_ANSWER = "first";
    private static final String SECOND_ANSWER = "second";
    private static final String COMBINED_ANSWER = "second";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(WARM_UP, warmUpDelay);
        bundle.put(TOTAL_ANSWER, totalAnswers);
        bundle.put(FIRST_ANSWER, parameter);
        bundle.put(SECOND_ANSWER, parameter2);
        bundle.put(COMBINED_ANSWER, ANSWER);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        warmUpDelay = bundle.getFloat(WARM_UP);
        totalAnswers = bundle.getInt( TOTAL_ANSWER );
        parameter = bundle.getInt( FIRST_ANSWER );
        parameter2 = bundle.getInt( SECOND_ANSWER );
        ANSWER = bundle.getString( COMBINED_ANSWER );
    }

    public class questionnaireEnergy extends ArtifactBuff {

        @Override
        public boolean act() {

            if (warmUpDelay > 0){
                if (level() >= 10){
                    warmUpDelay = 0;
                } else if (warmUpDelay == 101){
                    warmUpDelay = 100f;
                } else if (!cursed && target.buff(MagicImmune.class) == null) {
                    float turnsToWarmUp = (int) Math.pow(10 - level(), 2);
                    warmUpDelay -= 100 / turnsToWarmUp;
                }
                updateQuickslot();
            }

            spend(TICK);
            return true;
        }

        public void gainCharge(float levelPortion) {
            if (cursed || target.buff(MagicImmune.class) != null) return;

            //generates 2 energy every hero level, +1 energy per toolkit level
            //to a max of 12 energy per hero level
            //This means that energy absorbed into the kit is recovered in 5 hero levels
            double chargeGain = (1 + level()) * levelPortion;
            chargeGain *= RingOfEnergy.artifactChargeMultiplier(target);
            chargeGain *= getRarityMultiplier();
            chargeGain = Math.min(50, chargeGain);
            partialCharge += chargeGain;

            //charge is in increments of 1 energy.
            while (partialCharge >= 1) {
                charge++;
                partialCharge -= 1;

                updateQuickslot();
            }
        }

        public void obtainItem(Item item) {
            int chargesUsed = chargesToUse(item);
            charge -= chargesUsed;
            exp += chargesUsed;
            GLog.i(Messages.get(ExaminationParchment.class, "obtain_item", item.name()));

            Talent.onArtifactUsed(Dungeon.hero);
            while (exp >= (300L * Math.round(1.33f * level())) && level() < levelCap) {
                exp -= 300L * Math.round(1.33f * level());
                Catalog.countUse(ExaminationParchment.class);
                GLog.p(Messages.get(ExaminationParchment.class, "level_up"));
                upgrade();
            }
            updateQuickslot();
        }

        public int chargesToUse(Item item){
            long value = item.value();
            float valUsing = 1;
            int chargesUsed = 0;
            while (valUsing < value){
                valUsing += 2 + item.level()/2f + item.quantity();
                chargesUsed++;
            }
            return (int) (chargesUsed + (availableEnergy() * 0.05f));
        }

        public void removeBuff(Buff buff) {
            int chargesUsed = chargesToUseInbuff(buff);
            buff.detach();
            charge -= chargesUsed;
            exp += chargesUsed;
            GLog.i(Messages.get(ExaminationParchment.class, "remove_buff", buff.name()));

            Talent.onArtifactUsed(Dungeon.hero);
            while (exp >= (300L * Math.round(1.33f * level())) && level() < levelCap) {
                exp -= 300L * Math.round(1.33f * level());
                Catalog.countUse(ExaminationParchment.class);
                GLog.p(Messages.get(ExaminationParchment.class, "level_up"));
                upgrade();
            }
            updateQuickslot();
        }

        public int chargesToUseInbuff(Buff buff){
            int chargesUsed;
            if (buff.type == buffType.NEGATIVE) {
                chargesUsed = 50;
            } else if (buff.type == buffType.POSITIVE) {
                chargesUsed = 20;
            } else {
                chargesUsed = 5;
            }
            if (buff instanceof Hunger) {
                chargesUsed = 1500;
            }
            return (int) (chargesUsed + (availableEnergy() * 0.05f));
        }

        public long availableEnergy(){
            return charge;
        }

    }

}
