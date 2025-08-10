package com.shatteredpixel.shatteredpixeldungeon.ui.changelist;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.BlackPsycheChest;
import com.shatteredpixel.shatteredpixeldungeon.items.LostBackpack;
import com.shatteredpixel.shatteredpixeldungeon.items.PsycheChest;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.EquipmentBag;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.SackOfHolding;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfWealth;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.IdentificationBomb;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.watabou.noosa.Image;

import java.util.ArrayList;

public class InfPDChanges {
    public static void addAllChanges( ArrayList<ChangeInfo> changeInfos ) {
        ChangeInfo changes = new ChangeInfo("Future Updates Announcement", true, "");
        changes.hardlight(0xFF2400);
        changeInfos.add(changes);
        changes.addButton( new ChangeButton(Icons.get(Icons.PREFS), "Tentative Changes",
                "This is a tentative changes that maybe added/changed in the future updates: \n\n" +
                        "_-_ Buffs and Nerfs (as always)\n" +
                        "_-_ New Challenges (harder than the previous mods)\n" +
                        "_-_ Raritized Mobs (like some sort of champions)\n" +
                        "_-_ New Spells\n" +
                        "_-_ New Scrolls and Potions?\n" +
                        "_-_ New Lores (infinity, and beyond)\n\n" +
                        "So these things are maybe added in the future updates and are subject to change, maybe if the devs were not lazy, I guess?"
        ));

        changes = new ChangeInfo("InfPD-0.1.3", true, "");
        changes.hardlight(0x00FFFF);
        changeInfos.add(changes);
        changes.addButton( new ChangeButton(Icons.get(Icons.PREFS), "Other Changes",
                "_-_ Added true names for Rarities\n" +
                        "_-_ Reverted back Ring of Haste\n"
        ));
        changes.addButton( new ChangeButton(Icons.BUFFS.get(), "Buffs and Nerfs",
                "_-_ Nerfed Ring of Haste at its finest\n"
        ));

        changes = new ChangeInfo("InfPD-0.1.2", true, "");
        changes.hardlight(0x00FFFF);
        changeInfos.add(changes);
        changes.addButton( new ChangeButton(Icons.get(Icons.PREFS), "Other Changes",
                "_-_ Added item renaming\n" +
                        "_-_ Made challenge window scrollable\n" +
                        "_-_ Aligned the update link to github (you can now get updates in-game)\n"
        ));
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), "Bug Fixes",
                "_-_ Fixed crashes on the perk Raritizing Magic\n"
        ));
        changes.addButton( new ChangeButton(Icons.BUFFS.get(), "Buffs and Nerfs",
                "_-_ Nerfed hero max exp\n" +
                        "_-_ Reduced the entire loot multiplier\n"
        ));


        changes = new ChangeInfo("InfPD-0.1.1", true, "");
        changes.hardlight(0x00FFFF);
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), "Bug Fixes",
                "_-_ Fixed Potion of Strength not being droppped on the dungeon\n"
        ));
        changes.addButton( new ChangeButton(Icons.get(Icons.PREFS), "Other Changes",
                "_-_ Added a sprite for Raritize spell\n" +
                        "_-_ Added _Extractor_ spell\n" +
                        "_-_ Increased custom note size to 30\n" +
                        "_-_ Added new spells on the Catalog\n" +
                        "_-_ Added some useful info on mobs\n"
        ));
        changes.addButton( new ChangeButton(Icons.BUFFS.get(), "Buffs and Nerfs",
                "_-_ Drastically nerfed gold drops\n" +
                        "_-_ Fire Booster now multiplies the fire damage by 1.15x\n" +
                        "_-_ Recharging buffs now gives you 35 turns of duration\n" +
                        "_-_ Stone of Clairvoyance now scans within 23 cells radius\n" +
                        "_-_ Arena now spawns with 2x HP and gains a champion title after power 30\n"
        ));

        changes = new ChangeInfo("InfPD-0.1.0", true, "");
        changes.hardlight(0x00FFFF);
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(new RingOfWealth(), "Added back ring of wealth"));
        changes.addButton(new ChangeButton(new EquipmentBag(), "Moved the ring, artifact and misc slots into their own bag"));
        changes.addButton(new ChangeButton(new SackOfHolding(), "Added the sack of holding, a bag that can hold any item, its sold on the shop at floor 16"));
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), "System changes",
                "Changed the cycle system to allow for basicly infinite cycles, also changed hardcoded values to be calculated exponentially",
                "Allowed the amount of ring, artifact and misc slots to be variable, for use in later Content",
                "Added variables for multiplying the amount of rooms, monsters, loot, traps and the size of rooms in a layer"));
        changes.addButton(new ChangeButton(new LostBackpack(), "Added an optional second row of quickslots"));
        changes.addButton( new ChangeButton(Icons.BUFFS.get(), "Buffs and Nerfs",
                "_-_ Nerfed all rarities' effects\n" +
                        "_-_ Rebalanced chances and multipliers of rarities\n" +
                        "_-_ Buffed Mimic and Cycle Multiplier\n" +
                        "_-_ The chances of getting luck potion is decreased\n" +
                        "_-_ Bosses now gains 2.5% HP bonus per escalating depth\n"
        ));
        changes.addButton( new ChangeButton(Icons.get(Icons.PREFS), "Other Changes",
                "_-_ Added _Galactic_ enchanment\n" +
                        "_-_ Added _Raritize_ spell\n" +
                        "_-_ Extended other bags from 36 to 57\n" +
                        "_-_ Added Raritizing Magic perk\n"
        ));
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), "Bug Fixes",
                "_-_ Fixed fishing rods not working\n"
        ));
    }
}
