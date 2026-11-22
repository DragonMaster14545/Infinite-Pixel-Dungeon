package com.shatteredpixel.shatteredpixeldungeon.items;

import java.util.List;

public class LoadoutBox extends ShulkerBox {

    public void fill(List<Item> items) {
        for (Item item : items) {
            contents.add(item);
        }
    }

    @Override
    public boolean isUpgradable() {
        return false;
    }
}
