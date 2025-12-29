package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class Upgrades implements Bundlable {

    @Override
    public void storeInBundle(Bundle bundle) {
        Bundle upgradesBundle = new Bundle();

        for (Upgrade upgrade : upgrades) {
            upgradesBundle.put(String.valueOf(upgrade.data.ordinal()), upgrade.level);
        }

        bundle.put("upgrades", upgradesBundle);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        upgrades = new ArrayList<>();
        Bundle upgradesBundle = bundle.getBundle("upgrades");

        for (String key : upgradesBundle.getKeys()) {
            int level = upgradesBundle.getInt(key);
            Upgrade upgrade = new Upgrade(UpgradeData.values()[Integer.parseInt(key)], level);
            upgrades.add(upgrade);
        }
    }

    public enum UpgradeData {
        RING_SLOTS(Integer.MAX_VALUE,"Ring slots","Adds one extra ring slot per level") {
            @Override
            public int ringSlots() {
                return 1;
            }

            @Override
            public LinkedHashMap<Class<? extends Item>, Long> cost(int level) {
                LinkedHashMap<Class<? extends Item>, Long> cost = new LinkedHashMap<>();
                cost.put(ScrollOfUpgrade.class, (long) Math.pow(25,level));
                return cost;
            }
        },
        MISC_SLOTS(Integer.MAX_VALUE,"Misc slots","Adds one extra misc slot per level") {
            @Override
            public int miscSlots() {
                return 1;
            }

            @Override
            public LinkedHashMap<Class<? extends Item>, Long> cost(int level) {
                LinkedHashMap<Class<? extends Item>, Long> cost = new LinkedHashMap<>();
                cost.put(ScrollOfUpgrade.class, (long) Math.pow(25,level));
                return cost;
            }
        },
        ARTIFACT_SLOTS(Integer.MAX_VALUE,"Artifact slots","Adds one extra artifact slot per level") {
            @Override
            public int artifactSlots() {
                return 1;
            }

            @Override
            public LinkedHashMap<Class<? extends Item>, Long> cost(int level) {
                LinkedHashMap<Class<? extends Item>, Long> cost = new LinkedHashMap<>();
                cost.put(ScrollOfUpgrade.class, (long) Math.pow(25,level));
                return cost;
            }
        },

        MORE_LOOTS(Integer.MAX_VALUE,"More Loots","Adds extra loots per level") {
            @Override
            public int extraLoots() {
                return 1;
            }

            @Override
            public LinkedHashMap<Class<? extends Item>, Long> cost(int level) {
                LinkedHashMap<Class<? extends Item>, Long> cost = new LinkedHashMap<>();
                cost.put(ScrollOfUpgrade.class, (long) Math.pow(2,level));
                cost.put(ScrollOfMagicMapping.class, (long) Math.pow(3,level));
                return cost;
            }
        },;

        public final int maxLevel;
        public final String name;
        public final String desc;

        UpgradeData(int maxLevel, String name, String desc) {
            this.maxLevel = maxLevel;
            this.name = name;
            this.desc = desc;
        }
        public LinkedHashMap<Class<? extends Item>,Long> cost(int level) {
            return new LinkedHashMap<>();
        }
        public int ringSlots() {
            return 0;
        }
        public int miscSlots() {
            return 0;
        }
        public int artifactSlots() {
            return 0;
        }
        public int extraLoots() {
            return 0;
        }
    }

    public List<Upgrade> upgrades = new ArrayList<>();

    public Upgrades () {
        for (UpgradeData data : UpgradeData.values()) {
            upgrades.add(new Upgrade(data));
        }
    }

    public int ringSlots() {
        int slots = 0;
        for (Upgrade upgrade : upgrades) {
            slots += upgrade.data.ringSlots()*upgrade.level;
        }
        return slots;
    }
    public int miscSlots() {
        int slots = 0;
        for (Upgrade upgrade : upgrades) {
            slots += upgrade.data.miscSlots()*upgrade.level;
        }
        return slots;
    }
    public int artifactSlots() {
        int slots = 0;
        for (Upgrade upgrade : upgrades) {
            slots += upgrade.data.artifactSlots()*upgrade.level;
        }
        return slots;
    }

    public int extraLoots() {
        int loots = 0;
        for (Upgrade upgrade : upgrades) {
            loots += upgrade.data.extraLoots()*upgrade.level;
        }
        return loots;
    }

    public class Upgrade {
        public UpgradeData data;
        public int level = 0;

        public Upgrade (UpgradeData data) {
            this.data = data;
        }
        public Upgrade (UpgradeData data, int level) {
            this.data = data;
            this.level = level;
        }

        public LinkedHashMap<Class<? extends Item>,Long> getUpgradeCost() {
            return data.cost(level+1);
        }

        public boolean tryUpgrade() {
            if (!(level < data.maxLevel)) return false;
            if (!Dungeon.hero.belongings.hasItems(getUpgradeCost())) return false;
            Dungeon.hero.belongings.removeItems(getUpgradeCost());
            level++;
            return true;
        }

        public boolean canUpgrade() {
            return level < data.maxLevel && Dungeon.hero.belongings.hasItems(getUpgradeCost());
        }
    }
}
