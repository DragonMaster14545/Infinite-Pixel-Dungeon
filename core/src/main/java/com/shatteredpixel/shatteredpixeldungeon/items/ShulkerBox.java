package com.shatteredpixel.shatteredpixeldungeon.items;



import com.shatteredpixel.shatteredpixeldungeon.utils.Big;
import java.math.BigInteger;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndShulkerBox;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndShulkerInspect;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTextInput;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

public class ShulkerBox extends Item {

    public static final String AC_OPEN  = "OPEN";
    public static final String AC_STORE = "STORE";
    public static final String AC_COLOR = "COLOR";
	public static final String AC_INSPECT = "INSPECT";

    public static final int BASE_CAPACITY = 25;
	public static final int CAPACITY_PER_LEVEL = 5;
	public static final int MAX_LEVEL = 100;
	public static final int BASE_LIMIT = 10;
	public static final int LIMIT_PER_LEVEL = 10;

    {
        image = ItemSpriteSheet.SHULK_BOX;

        defaultAction = AC_OPEN;
        levelKnown = true;
		cursedKnown = true;
    }

    private final ArrayList<Item> contents = new ArrayList<>();

    private int pinned = 0;

    public int pinnedCount() {
        return Math.max(0, Math.min(pinned, contents.size()));
    }

    public int indexOf(Item item) {
        for (int i = 0; i < contents.size(); i++) {
            if (contents.get(i) == item) return i;
        }
        return -1;
    }

    public boolean isPinned(Item item) {
        int i = indexOf(item);
        return i >= 0 && i < pinnedCount();
    }

    public boolean togglePin(Item item) {
        int i = indexOf(item);
        if (i < 0) return false;
        int p = pinnedCount();

        contents.remove(i);
        if (i < p) {
            contents.add(p - 1, item);
            pinned = p - 1;
        } else {
            contents.add(p, item);
            pinned = p + 1;
        }
        return true;
    }

    private long boxId = newId();

    public ArrayList<Item> contents() {
        return new ArrayList<>(contents);
    }

    public int size() {
        return contents.size();
    }

    public boolean isEmpty() {
        return contents.isEmpty();
    }

	public BigInteger capacity() {
		return BigInteger.valueOf(BASE_CAPACITY).add(BigInteger.valueOf(CAPACITY_PER_LEVEL).multiply(level()));
	}

	public BigInteger maxItemLevel() {
		return scaledLimit();
	}

	public BigInteger maxQuantity() {
		return scaledLimit();
	}

	private BigInteger scaledLimit() {
		BigInteger normal = BigInteger.valueOf(BASE_LIMIT).add(BigInteger.valueOf(LIMIT_PER_LEVEL).multiply(level()));
		return BigInteger.ONE.max(Big.round(Big.div(Big.dec(normal.multiply(BigInteger.valueOf(VaultStore.limitPercent()))), Big.dec(100f))));
	}

	private BigInteger quantityRoomFor(Item item) {
		BigInteger existing = BigInteger.ZERO;
		if (item.stackable) {
			for (Item i : contents) {
				if (item.isSimilar(i)) {
					existing = i.quantity();
					break;
				}
			}
		}
		return BigInteger.ZERO.max(maxQuantity().subtract(existing));
	}

    private boolean hasRoomFor(Item item) {
        if ((BigInteger.valueOf(contents.size()).compareTo(capacity()) < 0)) return true;
        if (item.stackable) {
            for (Item i : contents) {
                if (item.isSimilar(i)) return true;
            }
        }
        return false;
    }

    private boolean canStore(Item item) {
        Hero hero = Dungeon.hero;
        return hero != null
                && item != null
                && !(item instanceof Bag)
                && !(item instanceof ShulkerBox)
                && !item.isEquipped(hero)
                && !contents.contains(item)
                && hero.belongings.backpack.contains(this)
                && hasRoomFor(item);
    }

    private void addToContents(Item item) {
        if (item.stackable) {
            for (Item i : contents) {
                if (item.isSimilar(i)) {
                    i.merge(item);
                    return;
                }
            }
        }
        contents.add(item);
    }

    private void store(Hero hero, Item item) {
        store(hero, item, null);
    }

    private void store(Hero hero, Item item, BigInteger amount) {
        if (!canStore(item)) {
            GLog.w(Messages.get(this, "cannot_store"));
            return;
        }

        if ((item.level().compareTo(maxItemLevel()) > 0)) {
			GLog.w(Messages.get(this, "level_limit", maxItemLevel()));
			return;
		}

		BigInteger room = quantityRoomFor(item);
		if ((room.compareTo(BigInteger.ZERO) <= 0)) {
			GLog.w(Messages.get(this, "stack_limit", maxQuantity()));
			return;
		}

		BigInteger want = amount == null ? item.quantity() : amount.min(item.quantity());
		BigInteger count = want.min(room);
		if (count.signum() <= 0) return;

		Item toStore = item;
		if ((item.quantity().compareTo(count) > 0)) {
			Item part = item.split(count);
			if (part == null) {
				GLog.w(Messages.get(this, "cannot_split"));
				return;
			}
			toStore = part;
			if (want.compareTo(room) > 0) GLog.i(Messages.get(this, "stack_split", room, maxQuantity()));
		} else {
			item.detachAll(hero.belongings.backpack);
		}
        addToContents(toStore);

        hero.sprite.operate(hero.pos);
        hero.spend(1f);
        hero.busy();
        Sample.INSTANCE.play(Assets.Sounds.ITEM);
        GLog.i(Messages.get(this, "stored", toStore.name()));
        updateQuickslot();
    }

    public boolean takeOut(Hero hero, Item item) {
        int at = indexOf(item);
        if (at < 0) return false;
        int pinnedBefore = pinnedCount();

        contents.remove(at);
        if (at < pinnedBefore) pinned = pinnedBefore - 1;

        if (!item.collect(hero.belongings.backpack)) {
            contents.add(at, item);
            pinned = pinnedBefore;
            GLog.w(Messages.get(this, "pack_full"));
            return false;
        }

        hero.sprite.operate(hero.pos);
        hero.spend(1f);
        hero.busy();
        Sample.INSTANCE.play(Assets.Sounds.ITEM);
        GLog.i(Messages.get(this, "taken", item.name()));
        updateQuickslot();
        return true;
    }

    public boolean takeOut(Hero hero, Item item, BigInteger amount) {
        if (indexOf(item) < 0) return false;
        if (amount == null || !item.stackable || amount.compareTo(item.quantity()) >= 0) {
            return takeOut(hero, item);
        }
        if (amount.signum() <= 0) return false;

        Item part = item.split(amount);
        if (part == null) {
            GLog.w(Messages.get(this, "cannot_split"));
            return false;
        }
        if (!part.collect(hero.belongings.backpack)) {
            item.quantity(item.quantity().add(amount));
            GLog.w(Messages.get(this, "pack_full"));
            return false;
        }

        hero.sprite.operate(hero.pos);
        hero.spend(1f);
        hero.busy();
        Sample.INSTANCE.play(Assets.Sounds.ITEM);
        GLog.i(Messages.get(this, "taken", part.name()));
        updateQuickslot();
        return true;
    }

    public interface AmountCallback {
        void apply(BigInteger amount);
    }

    public static void promptAmount(final ShulkerBox box, final String key, final BigInteger max, final AmountCallback callback) {
        GameScene.show(new WndTextInput(
                Messages.get(box, key + "_amount_title"),
                Messages.get(box, "amount_body", Big.fmt(max)),
                max.toString(),
                40,
                false,
                Messages.get(box, "amount_confirm"),
                Messages.get(box, "amount_cancel")) {
            @Override
            public void onSelect(boolean positive, String text) {
                if (!positive) return;
                String t = text == null ? "" : text.trim().toLowerCase();
                BigInteger n;
                if (t.isEmpty() || t.equals("all") || t.equals("max")) {
                    n = max;
                } else if (t.equals("half")) {
                    n = max.divide(BigInteger.TWO).max(BigInteger.ONE);
                } else {
                    try {
                        n = Big.parse(t);
                    } catch (NumberFormatException e) {
                        GLog.w(Messages.get(box, "amount_invalid", text.trim()));
                        return;
                    }
                }
                if (n.signum() <= 0) {
                    GLog.w(Messages.get(box, "amount_invalid", text == null ? "" : text.trim()));
                    return;
                }
                callback.apply(n.min(max));
            }
        });
    }

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        actions.add(0, AC_OPEN);
		actions.add(1, AC_INSPECT);
        if (hero.belongings.backpack.contains(this)) {
            actions.add(2, AC_STORE);
        }
        actions.add(AC_COLOR);
        return actions;
    }

    @Override
    public void execute(Hero hero, String action) {
		if (action.equals(AC_OPEN)) {
			ensureUnique();
			GameScene.show(new WndShulkerBox(this));
			defaultAction = AC_OPEN;
		} else if (action.equals(AC_STORE)) {
			ensureUnique();
			startStore(hero);
			defaultAction = AC_STORE;
		} else if (action.equals(AC_INSPECT)) {
			GameScene.show(new WndShulkerInspect(this));

		} else if (action.equals(AC_COLOR)) {
			startColor();

		} else {
			super.execute(hero, action);
		}
    }

    private void startStore(Hero hero) {
        GameScene.selectItem(new WndBag.ItemSelector() {
            @Override
            public String textPrompt() {
                return Messages.get(ShulkerBox.this, "store_prompt");
            }

            @Override
            public Class<? extends Bag> preferredBag() {
                return Belongings.Backpack.class;
            }

            @Override
            public boolean itemSelectable(Item item) {
                return canStore(item);
            }

            @Override
            public void onSelect(final Item item) {
                if (item == null) return;
                if (!canStore(item)) {
                    store(hero, item);
                    return;
                }
                BigInteger max = item.quantity().min(quantityRoomFor(item));
                if (item.stackable && max.compareTo(BigInteger.ONE) > 0) {
                    promptAmount(ShulkerBox.this, "store", max, new AmountCallback() {
                        @Override
                        public void apply(BigInteger amount) {
                            store(hero, item, amount);
                        }
                    });
                } else {
                    store(hero, item);
                }
            }
        });
    }

    private boolean customColor = true;
    private boolean playerColor = false;
    private int color = hueColor(boxId);

    public ShulkerBox setColor(int rgb) {
        customColor = true;
        color = rgb & 0xFFFFFF;
        updateQuickslot();
        return this;
    }

    public ShulkerBox resetColor() {
        customColor = false;
        updateQuickslot();
        return this;
    }

    public int color() {
        return customColor ? color : 0xFFFFFF;
    }

	private static int hueColor(long id) {
        float h = ((id >>> 8) % 360L) / 60f;
        float s = 0.6f, v = 1f;
        int i = (int) h;
        float f = h - i;
        float p = v * (1 - s), q = v * (1 - s * f), t = v * (1 - s * (1 - f));
        float r, g, b;
        switch (i % 6) {
            case 0:  r = v; g = t; b = p; break;
            case 1:  r = q; g = v; b = p; break;
            case 2:  r = p; g = v; b = t; break;
            case 3:  r = p; g = q; b = v; break;
            case 4:  r = t; g = p; b = v; break;
            default: r = v; g = p; b = q; break;
        }
        return ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (b * 255);
    }

	@Override
	public int tint() {
		if (!customColor) return -1;
		//mixes in a quarter of white so dark colors don't hide the sprite's shading
		int r = (color >> 16) & 0xFF, g = (color >> 8) & 0xFF, b = color & 0xFF;
		r += (255 - r) / 4;
		g += (255 - g) / 4;
		b += (255 - b) / 4;
		return (r << 16) | (g << 8) | b;
	}

    private static final String[] COLOR_NAMES = {
            "red", "orange", "yellow", "green", "cyan", "blue", "purple",
            "pink", "white", "gray", "brown", "black"
    };
    private static final int[] COLOR_VALUES = {
            0xFF4040, 0xFF8C1A, 0xFFE033, 0x40D040, 0x40E0E0, 0x4060FF, 0xA040E0,
            0xFF80C0, 0xFFFFFF, 0x909090, 0x9A6030, 0x303030
    };


    private static final String[] GROUPS = {
            ".items.weapon.", ".items.armor.", ".items.wands.", ".items.rings.", ".items.artifacts.",
            ".items.potions.", ".items.scrolls.", ".items.food.", ".plants.", ".items.stones.", ".items.bombs."
    };

    private static int group(Item item) {
        String n = item.getClass().getName();
        for (int i = 0; i < GROUPS.length; i++) {
            if (n.contains(GROUPS[i])) return i;
        }
        return GROUPS.length;
    }

    public void sortContents() {
        int p = pinnedCount();
        ArrayList<Item> rest = new ArrayList<>(contents.subList(p, contents.size()));
        Collections.sort(rest, (a, b) -> {
            int ga = group(a), gb = group(b);
            if (ga != gb) return ga - gb;
            int c = a.name().compareToIgnoreCase(b.name());
            if (c != 0) return c;
            return Long.compare(Big.toLong(b.level()), Big.toLong(a.level()));
        });
        for (int i = 0; i < rest.size(); i++) {
            contents.set(p + i, rest.get(i));
        }
    }

    private void startColor() {
        GameScene.show(new WndTextInput(
                Messages.get(this, "color_title"),
                Messages.get(this, "color_body"),
                "#" + String.format("%06X", color()),
                16,
                false,
                Messages.get(this, "color_confirm"),
                Messages.get(this, "color_cancel")) {
            @Override
            public void onSelect(boolean positive, String text) {
                if (positive) {
                    applyColorText(text);
                }
            }
        });
    }

    private void applyColorText(String text) {
        String t = text == null ? "" : text.trim().toLowerCase();

        if (t.isEmpty() || t.equals("reset") || t.equals("default")
                || t.equals("none") || t.equals("off")) {
            playerColor = false;
            resetColor();
            GLog.i(Messages.get(this, "color_reset"));
            return;
        }

        Integer rgb = parseColor(t);
        if (rgb == null) {
            GLog.w(Messages.get(this, "color_invalid", text.trim()));
            return;
        }
        setColor(rgb);
		playerColor = true;
        GLog.i(Messages.get(this, "color_set", String.format("#%06X", rgb)));
    }

    private static Integer parseColor(String t) {
        for (int i = 0; i < COLOR_NAMES.length; i++) {
            if (t.equals(COLOR_NAMES[i])) return COLOR_VALUES[i];
        }

        if (t.startsWith("#")) t = t.substring(1);
        else if (t.startsWith("0x")) t = t.substring(2);

        if (t.length() == 3) {
            t = "" + t.charAt(0) + t.charAt(0) + t.charAt(1) + t.charAt(1) + t.charAt(2) + t.charAt(2);
        }
        if (t.length() != 6) return null;

        try {
            return Integer.parseInt(t, 16);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static long newId() {
        long id = ThreadLocalRandom.current().nextLong() & Long.MAX_VALUE;
        return id == 0L ? 1L : id;
    }

    private void rerollId() {
        boxId = newId();
        if (!playerColor) color = hueColor(boxId);
    }

    public long boxId() {
        return boxId;
    }

    public String shortId() {
        return String.format("%06X", boxId & 0xFFFFFFL);
    }

    private void ensureUnique() {
        Hero hero = Dungeon.hero;
        if (hero == null) return;

        boolean clash;
        do {
            clash = false;
            for (Item i : hero.belongings) {
                if (i != this && i instanceof ShulkerBox && ((ShulkerBox) i).boxId == boxId) {
                    rerollId();
                    clash = true;
                    break;
                }
            }
        } while (clash);
    }

    @Override
    public boolean collect(Bag container) {
        if (super.collect(container)) {
            ensureUnique();
            return true;
        }
        return false;
    }

    @Override
    public Item duplicate() {
        ShulkerBox dupe = (ShulkerBox) super.duplicate();
        if (dupe != null) {
            dupe.contents.clear();
            dupe.pinned = 0;
            dupe.rerollId();
        }
        return dupe;
    }

    @Override
    public String name() {
        return super.name() + " #" + shortId();
    }

    @Override
    public String status() {
        return contents.isEmpty() ? null : Integer.toString(contents.size());
    }

    @Override
    public boolean isUpgradable() {
        return (level().compareTo(BigInteger.valueOf(MAX_LEVEL)) < 0);
	}

	@Override
	public Item upgrade() {
		if ((level().compareTo(BigInteger.valueOf(MAX_LEVEL)) >= 0)) return this;
		super.upgrade();
		levelKnown = true;
		return this;
	}

	@Override
	public Item upgrade(BigInteger n) {
		BigInteger room = BigInteger.valueOf(MAX_LEVEL).subtract(level()).max(BigInteger.ZERO);
		n = n.min(room);
		if (n.signum() <= 0) return this;
		super.upgrade(n);
		levelKnown = true;
		return this;
	}

	@Override
	public Item degrade() {
		return this;
    }

    @Override
    public boolean isIdentified() {
        return true;
    }

    @Override
    public BigInteger value() {
        BigInteger total = BigInteger.valueOf(420L).add(BigInteger.valueOf(40L).multiply(level()));
        for (Item i : contents) total = total.add(i.value());
        return total;
    }

    @Override
    public String desc() {
        String desc = super.desc();

		if (Dungeon.level != null || Dungeon.hero != null) {
            if (Badges.isUnlocked( Badges.Badge.KIT_UNLOCKED )) desc += "\n\n" + Messages.get(this, "desc_lore");
            desc += "\n\n" + Messages.get(this, "desc_id",
                    Long.toHexString(boxId).toUpperCase(), contents.size(), capacity());
            desc += "\n" + Messages.get(this, "desc_limits", maxItemLevel(), maxQuantity());

            if (!contents.isEmpty()) {
                desc += "\n\n" + Messages.get(this, "desc_value", value());
                desc += "\n" + Messages.get(this, "desc_inspect");
            }
        }
		return desc;
	}

    private static final String BOX_ID = "box_id";
    private static final String CONTENTS = "contents";
    private static final String PINNED = "pinned";
    private static final String CUSTOM_COLOR = "custom_color";
    private static final String COLOR = "color";
	private static final String PLAYER_COLOR = "player_color";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(BOX_ID, boxId);
        bundle.put(CONTENTS, contents);
        bundle.put(PINNED, pinnedCount());
        bundle.put(CUSTOM_COLOR, customColor);
        bundle.put(COLOR, color);
		bundle.put(PLAYER_COLOR, playerColor);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        if (bundle.contains(BOX_ID)) {
            boxId = Big.toLong(Big.getBig(bundle, BOX_ID));
        }
        contents.clear();
        for (Bundlable b : bundle.getCollection(CONTENTS)) {
            if (b != null) contents.add((Item) b);
        }
        pinned = bundle.contains(PINNED) ? Math.max(0, Math.min(bundle.getInt(PINNED), contents.size())) : 0;
		levelKnown = true;
		cursedKnown = true;
		if ((level().compareTo(BigInteger.valueOf(MAX_LEVEL)) > 0)) level(BigInteger.valueOf(MAX_LEVEL));
        if (bundle.contains(CUSTOM_COLOR)) {
            customColor = bundle.getBoolean(CUSTOM_COLOR);
            color = bundle.getInt(COLOR);
        }
		playerColor = bundle.getBoolean(PLAYER_COLOR);
    }
}
