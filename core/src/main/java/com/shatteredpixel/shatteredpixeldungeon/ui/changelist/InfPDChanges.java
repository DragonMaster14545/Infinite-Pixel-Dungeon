package com.shatteredpixel.shatteredpixeldungeon.ui.changelist;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.BlackPsycheChest;
import com.shatteredpixel.shatteredpixeldungeon.items.PsycheChest;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.EquipmentBag;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfWealth;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.IdentificationBomb;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.watabou.noosa.Image;

import java.util.ArrayList;

public class InfPDChanges {
    public static void addAllChanges( ArrayList<ChangeInfo> changeInfos ) {
        ChangeInfo changes = new ChangeInfo("InfPD-0.1.0", true, "");
        changes.hardlight(0x00FFFF);
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(new RingOfWealth(), "Added back ring of wealth"));
        changes.addButton(new ChangeButton(new EquipmentBag(), "Moved the ring, artifact and misc slots into their own bag"));
        changes.addButton(new ChangeButton(new BlackPsycheChest(), "Changed the cycle system to allow for basicly infinite cycles, also changed hardcoded values to be calculated exponentially"));
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), "Equipment system changes",
                "Allowed the amount of ring, artifact and misc slots to be variable, for use in later Content"));
    }
}
