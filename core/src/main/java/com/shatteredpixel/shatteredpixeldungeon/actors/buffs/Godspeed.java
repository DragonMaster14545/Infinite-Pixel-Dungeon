package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

public class Godspeed extends TimescaleBuff {
    public static final float DURATION = 10f;

    {
        type = buffType.POSITIVE;
    }

    @Override
    public float speedFactor() {
        return 2f;
    }

    @Override
    public int icon() {
        return BuffIndicator.HASTE;
    }

    @Override
    public String toString() {
        return Messages.get(this, "name");
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc", visualcooldown());
    }
}
