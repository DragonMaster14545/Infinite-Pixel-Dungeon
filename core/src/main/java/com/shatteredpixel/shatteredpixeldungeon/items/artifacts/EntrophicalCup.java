package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Ankh;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;

import java.util.ArrayList;

public class EntrophicalCup extends Artifact {
    {
        image = ItemSpriteSheet.ARTIFACT_TOOLKIT;
        defaultAction = AC_SACRIFICE;

        levelCap = 10;

        charge = 0;
        partialCharge = 0;
    }

    // this artifact would give some buffs to hero in exchange for their lifesaver, the ankhs.
    // canonically needed for surviving a strike.

    public static final String AC_SACRIFICE = "SACRIFICE";

    @Override
    public ArrayList<String> actions(Hero hero ) {
        ArrayList<String> actions = super.actions( hero );
        if (isEquipped( hero ) && !cursed && hero.buff(MagicImmune.class) == null) {
            if (Dungeon.hero.belongings.contains(new Ankh())) {
                actions.add(AC_SACRIFICE);
            }
        }
        return actions;
    }

    @Override
    public void execute(Hero hero, String action ) {

        super.execute(hero, action);

        if (hero.buff(MagicImmune.class) != null) return;

        if (action.equals(AC_SACRIFICE)){
            if (!isEquipped(hero))                                          GLog.i( Messages.get(this, "need_to_equip") );
            else if (cursed)                                                GLog.w( Messages.get(this, "cursed") );
            else if (!Dungeon.hero.belongings.contains(new Ankh()))         GLog.w( Messages.get(this, "need_ankhs") );
            else {
                // we need a window to appear to sacrifice ankhs
            }
        }

        updateQuickslot();
    }

    // the sacrificer window lol
    public WndBag.ItemSelector itemSelector2 = new WndBag.ItemSelector() {

        @Override
        public String textPrompt() {
            return "Pick an Ankh to sacrifice";
        }

        @Override
        public Class<? extends Bag> preferredBag() {
            return Bag.class;
        }

        @Override
        public boolean itemSelectable(Item item) {
            return item instanceof Ankh;
        }

        @Override
        public void onSelect( Item item ) {
            if (!(item instanceof Ankh)) {
                return;
            }
        }
    };
}
