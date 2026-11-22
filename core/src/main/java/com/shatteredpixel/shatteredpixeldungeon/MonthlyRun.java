package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.MonthlyBox;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class MonthlyRun {

    private static boolean pending = false;

    private static final Generator.Category[] BOX_ROLL = {
            Generator.Category.WEAPON,
            Generator.Category.ARMOR,
            Generator.Category.RING,
            Generator.Category.WAND,
            Generator.Category.ARTIFACT,
            Generator.Category.POTION,
            Generator.Category.POTION,
            Generator.Category.SCROLL,
            Generator.Category.SCROLL,
            Generator.Category.MISSILE,
            Generator.Category.FOOD
    };

    private static GregorianCalendar utc(long time) {
        GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        c.setTimeInMillis(time);
        return c;
    }

    public static long monthStart(long time) {
        GregorianCalendar c = utc(time);
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    public static long nextStart(long start) {
        GregorianCalendar c = utc(monthStart(start));
        c.add(Calendar.MONTH, 1);
        return c.getTimeInMillis();
    }

    public static void scale(Mob mob) {
        if (!Dungeon.monthly || mob == null || mob.monthlyScaled) return;
        if (mob.alignment != Char.Alignment.ENEMY) return;
        BigInteger m = BigInteger.valueOf(10);
        mob.HP = mob.HP.multiply(m);
        mob.HT = mob.HT.multiply(m);
        mob.monthlyScaled = true;
    }

    public static void markPending() {
        pending = Dungeon.monthly;
    }

    public static boolean consumePending() {
        boolean p = pending;
        pending = false;
        return p;
    }

    public static ArrayList<Item> roll() {
        ArrayList<Item> list = new ArrayList<>();
        Random.pushGenerator(Dungeon.seed * 31L + 123456L);
        try {
            for (Generator.Category cat : BOX_ROLL) {
                Item item = null;
                for (int tries = 0; tries < 8; tries++) {
                    try {
                        item = Generator.random(cat);
                    } catch (Throwable t) {
                        item = null;
                    }
                    if (item != null && !item.cursed && !hasCurse(item)) break;
                    item = null;
                }
                if (item == null) continue;
                try {
                    item.identify();
                    if (item.stackable) item.quantity(BigInteger.valueOf(Random.IntRange(1, 5)));
                    if (item.isUpgradable()) item.level(BigInteger.valueOf(Random.IntRange(1, 3)));
                } catch (Throwable ignored) {
                }
                list.add(item);
            }
        } finally {
            Random.popGenerator();
        }
        return list;
    }

    private static boolean hasCurse(Item item) {
        try {
            if (item instanceof Weapon) return ((Weapon) item).hasCurseEnchant();
            if (item instanceof Armor) return ((Armor) item).hasCurseGlyph();
        } catch (Throwable ignored) {
        }
        return false;
    }

    public static void deliver(Hero hero) {
        if (hero == null) return;
        ArrayList<Item> list = roll();
        if (list.isEmpty()) return;
        MonthlyBox box = new MonthlyBox();
        box.fill(list);
        if (!box.collect(hero.belongings.backpack)) {
            Dungeon.level.drop(box, hero.pos).sprite.drop();
        }
        GLog.p("You've got a monthly box, please check it in your inventory.");
    }
}
