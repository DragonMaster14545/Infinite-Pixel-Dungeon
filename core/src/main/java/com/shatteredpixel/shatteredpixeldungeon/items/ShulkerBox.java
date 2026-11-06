package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
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

    private static final int BASE_VALUE = 60;
	private static final int VALUE_PER_LEVEL = 40;

    {
        image = ItemSpriteSheet.SHULK_BOX;

        defaultAction = AC_OPEN;
        levelKnown = true;
		cursedKnown = true;
    }

    private final ArrayList<Item> contents = new ArrayList<>();

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

	public long capacity() {
		return BASE_CAPACITY + CAPACITY_PER_LEVEL * level();
	}

	public long maxItemLevel() {
		return scaledLimit();
	}

	public long maxQuantity() {
		return scaledLimit();
	}

	private long scaledLimit() {
		long normal = BASE_LIMIT + LIMIT_PER_LEVEL * level();
		return Math.max(1, Math.round(normal * VaultStore.limitPercent() / 100f));
	}

	private long quantityRoomFor(Item item) {
		long existing = 0;
		if (item.stackable) {
			for (Item i : contents) {
				if (item.isSimilar(i)) {
					existing = i.quantity();
					break;
				}
			}
		}
		return Math.max(0, maxQuantity() - existing);
	}

    private boolean hasRoomFor(Item item) {
        if (contents.size() < capacity()) return true;
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
        if (!canStore(item)) {
            GLog.w(Messages.get(this, "cannot_store"));
            return;
        }

        if (item.level() > maxItemLevel()) {
			GLog.w(Messages.get(this, "level_limit", maxItemLevel()));
			return;
		}

		long room = quantityRoomFor(item);
		if (room <= 0) {
			GLog.w(Messages.get(this, "stack_limit", maxQuantity()));
			return;
		}

		Item toStore = item;
		if (item.quantity() > room) {
			Item part = item.split(room);
			if (part == null) {
				GLog.w(Messages.get(this, "stack_limit", maxQuantity()));
				return;
			}
			toStore = part;
			GLog.i(Messages.get(this, "stack_split", room, maxQuantity()));
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
        if (!contents.contains(item)) return false;

        contents.remove(item);

        if (!item.collect(hero.belongings.backpack)) {
            contents.add(item);
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
            public void onSelect(Item item) {
                if (item != null) store(hero, item);
            }
        });
    }

    private boolean customColor = true;
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
                if (positive) applyColorText(text);
            }
        });
    }

    private void applyColorText(String text) {
        String t = text == null ? "" : text.trim().toLowerCase();

        if (t.isEmpty() || t.equals("reset") || t.equals("default")
                || t.equals("none") || t.equals("off")) {
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
                    boxId = newId();
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
        Item dupe = super.duplicate();
        if (dupe instanceof ShulkerBox) {
            ((ShulkerBox) dupe).boxId = newId();
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
        return level() < MAX_LEVEL;
	}

	@Override
	public Item upgrade() {
		if (level() >= MAX_LEVEL) return this;
		super.upgrade();
		levelKnown = true;
		GLog.p(Messages.get(this, "upgraded", capacity()));
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
	public long value() {
		long total = BASE_VALUE + (long) VALUE_PER_LEVEL * level();
		for (Item items : contents) {
			total += items.value();
		}
		return (int) Math.min(total, Integer.MAX_VALUE);
	}

    @Override
    public String desc() {
        String desc = super.desc();

		desc += "\n\n" + Messages.get(this, "desc_id",
				Long.toHexString(boxId).toUpperCase(), contents.size(), capacity());
		desc += "\n" + Messages.get(this, "desc_limits", maxItemLevel(), maxQuantity());

		if (!contents.isEmpty()) {
			desc += "\n\n" + Messages.get(this, "desc_value", value());
			desc += "\n" + Messages.get(this, "desc_inspect");
		}
		return desc;
	}

    private static final String BOX_ID = "box_id";
    private static final String CONTENTS = "contents";
    private static final String CUSTOM_COLOR = "custom_color";
    private static final String COLOR = "color";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(BOX_ID, boxId);
        bundle.put(CONTENTS, contents);
        bundle.put(CUSTOM_COLOR, customColor);
        bundle.put(COLOR, color);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        if (bundle.contains(BOX_ID)) {
            boxId = bundle.getLong(BOX_ID);
        }
        contents.clear();
        for (Bundlable b : bundle.getCollection(CONTENTS)) {
            if (b != null) contents.add((Item) b);
        }
		levelKnown = true;
		cursedKnown = true;
		if (level() > MAX_LEVEL) level(MAX_LEVEL);
        if (bundle.contains(CUSTOM_COLOR)) {
            customColor = bundle.getBoolean(CUSTOM_COLOR);
            color = bundle.getInt(COLOR);
        }
    }
}
