package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.items.ShulkerBox;
import com.shatteredpixel.shatteredpixeldungeon.items.ShulkerFilter;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.Big;
import com.watabou.noosa.ui.Component;

import java.math.BigInteger;

public class WndShulkerFilter extends Window {

	private static final int WIDTH = 126;
	private static final int BTN_H = 16;
	private static final int GAP = 2;

	private final ShulkerBox box;
	private final ShulkerFilter filter;
	private final ScrollPane pane;
	private final Component content;

	public WndShulkerFilter(ShulkerBox box) {
		this(box, 0f);
	}

	public WndShulkerFilter(ShulkerBox box, float scroll) {
		super();
		this.box = box;
		this.filter = box.filter();

		RenderedTextBlock title = PixelScene.renderTextBlock(Messages.get(this, "title"), 9);
		title.hardlight(TITLE_COLOR);
		title.maxWidth(WIDTH);
		title.setPos(0, 0);
		add(title);

		float top = title.height() + GAP * 2;
		float footer = BTN_H + GAP;

		pane = new ScrollPane(new Component());
		add(pane);
		content = pane.content();

		float y = 0;
		float half = (WIDTH - GAP) / 2f;

		y = section(y, "section_categories");
		for (int i = 0; i < ShulkerFilter.CATEGORY_KEYS.length; i += 2) {
			y = categoryButton(i, 0, y, half);
			if (i + 1 < ShulkerFilter.CATEGORY_KEYS.length) {
				categoryButton(i + 1, half + GAP, y, half);
			}
			y += BTN_H + GAP;
		}

		y = section(y, "section_state");
		y = triButton(y, "enchanted", filter.enchanted, new Setter() {
			public void set(int v) { filter.enchanted = v; }
		});
		y = triButton(y, "identified", filter.identified, new Setter() {
			public void set(int v) { filter.identified = v; }
		});
		y = triButton(y, "cursed", filter.cursed, new Setter() {
			public void set(int v) { filter.cursed = v; }
		});
		y = triButton(y, "upgradable", filter.upgradable, new Setter() {
			public void set(int v) { filter.upgradable = v; }
		});
		y = triButton(y, "stackable", filter.stackable, new Setter() {
			public void set(int v) { filter.stackable = v; }
		});

		y = section(y, "section_values");
		y = numberButton(y, "level_min", filter.levelMin, true, new BigSetter() {
			public void set(BigInteger v) { filter.levelMin = v; }
		});
		y = numberButton(y, "level_max", filter.levelMax, true, new BigSetter() {
			public void set(BigInteger v) { filter.levelMax = v; }
		});
		y = numberButton(y, "quantity_min", filter.quantityMin, false, new BigSetter() {
			public void set(BigInteger v) { filter.quantityMin = v; }
		});
		y = numberButton(y, "quantity_max", filter.quantityMax, false, new BigSetter() {
			public void set(BigInteger v) { filter.quantityMax = v; }
		});

		String rarityLabel = Messages.get(this, "rarity_min", filter.rarityMin == null
				? Messages.get(this, "any") : ShulkerFilter.plainRarityName(filter.rarityMin));
		RedButton rarity = new RedButton(rarityLabel, 6) {
			@Override
			public void onClick() {
				GameScene.show(new WndShulkerRarity(WndShulkerFilter.this.box, scrollY()));
				WndShulkerFilter.this.hide();
			}
		};
		rarity.setRect(0, y, WIDTH, BTN_H);
		content.add(rarity);
		y += BTN_H + GAP;

		String tierLabel = Messages.get(this, "tier_min", filter.tierMin <= 0
				? Messages.get(this, "any") : "T" + filter.tierMin);
		RedButton tier = new RedButton(tierLabel, 6) {
			@Override
			public void onClick() {
				filter.tierMin = (filter.tierMin + 1) % 6;
				refresh();
			}
		};
		tier.setRect(0, y, WIDTH, BTN_H);
		content.add(tier);
		y += BTN_H + GAP;

		String nameLabel = Messages.get(this, "name", filter.name == null || filter.name.trim().isEmpty()
				? Messages.get(this, "any") : filter.name.trim());
		RedButton nameBtn = new RedButton(nameLabel, 6) {
			@Override
			public void onClick() {
				GameScene.show(new WndTextInput(
						Messages.get(WndShulkerFilter.class, "name_title"),
						Messages.get(WndShulkerFilter.class, "name_body"),
						filter.name == null ? "" : filter.name,
						40,
						false,
						Messages.get(WndShulkerFilter.class, "confirm"),
						Messages.get(WndShulkerFilter.class, "cancel")) {
					@Override
					public void onSelect(boolean positive, String text) {
						if (!positive) return;
						filter.name = text == null ? "" : text.trim();
						refresh();
					}
				});
			}
		};
		nameBtn.setRect(0, y, WIDTH, BTN_H);
		content.add(nameBtn);
		y += BTN_H + GAP;

		y = section(y, "section_auto");
		RedButton auto = new RedButton(Messages.get(this, "auto_store",
				Messages.get(this, filter.autoStore ? "yes" : "no")), 6) {
			@Override
			public void onClick() {
				filter.autoStore = !filter.autoStore;
				refresh();
			}
		};
		auto.setRect(0, y, WIDTH, BTN_H);
		content.add(auto);
		y += BTN_H + GAP;

		content.setSize(WIDTH, y);

		float maxPane = PixelScene.uiCamera.height - 40 - top - footer - chrome.marginVer();
		float paneH = Math.max(BTN_H * 3, Math.min(y, maxPane));

		resize(WIDTH, (int) Math.ceil(top + paneH + GAP + footer));
		pane.setRect(0, top, WIDTH, paneH);
		pane.scrollTo(0, scroll);

		RedButton clear = new RedButton(Messages.get(this, "clear")) {
			@Override
			public void onClick() {
				filter.clear();
				refresh();
			}
		};
		clear.setRect(0, top + paneH + GAP, half, BTN_H);
		add(clear);

		RedButton done = new RedButton(Messages.get(this, "done")) {
			@Override
			public void onClick() {
				hide();
			}
		};
		done.setRect(half + GAP, top + paneH + GAP, half, BTN_H);
		add(done);
	}

	private float scrollY() {
		return content.camera != null ? content.camera.scroll.y : 0f;
	}

	private void refresh() {
		float s = scrollY();
		hide();
		GameScene.show(new WndShulkerFilter(box, s));
	}

	private float section(float y, String key) {
		RenderedTextBlock t = PixelScene.renderTextBlock(Messages.get(this, key), 7);
		t.hardlight(0xB0B0B0);
		t.maxWidth(WIDTH);
		t.setPos(0, y);
		content.add(t);
		return y + t.height() + GAP;
	}

	private float categoryButton(final int index, float x, float y, float w) {
		String label = (filter.hasCategory(index) ? "[x] " : "[ ] ")
				+ Messages.get(this, "cat_" + ShulkerFilter.CATEGORY_KEYS[index]);
		RedButton b = new RedButton(label, 6) {
			@Override
			public void onClick() {
				filter.toggleCategory(index);
				refresh();
			}
		};
		b.setRect(x, y, w, BTN_H);
		content.add(b);
		return y;
	}

	private interface Setter {
		void set(int v);
	}

	private interface BigSetter {
		void set(BigInteger v);
	}

	private float triButton(float y, String key, final int value, final Setter setter) {
		String state = Messages.get(this, value == ShulkerFilter.YES ? "yes" : value == ShulkerFilter.NO ? "no" : "any");
		RedButton b = new RedButton(Messages.get(this, key, state), 6) {
			@Override
			public void onClick() {
				setter.set((value + 1) % 3);
				refresh();
			}
		};
		b.setRect(0, y, WIDTH, BTN_H);
		content.add(b);
		return y + BTN_H + GAP;
	}

	private float numberButton(float y, final String key, final BigInteger value, final boolean allowNegative, final BigSetter setter) {
		String shown = value == null ? Messages.get(this, "any") : Big.fmt(value);
		RedButton b = new RedButton(Messages.get(this, key, shown), 6) {
			@Override
			public void onClick() {
				GameScene.show(new WndTextInput(
						Messages.get(WndShulkerFilter.class, key + "_title"),
						Messages.get(WndShulkerFilter.class, "number_body"),
						value == null ? "" : value.toString(),
						40,
						false,
						Messages.get(WndShulkerFilter.class, "confirm"),
						Messages.get(WndShulkerFilter.class, "cancel")) {
					@Override
					public void onSelect(boolean positive, String text) {
						if (!positive) return;
						String t = text == null ? "" : text.trim();
						if (t.isEmpty()) {
							setter.set(null);
						} else {
							try {
								BigInteger n = Big.parse(t);
								if (!allowNegative && n.signum() < 0) n = BigInteger.ZERO;
								setter.set(n);
							} catch (NumberFormatException e) {
								com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w(Messages.get(WndShulkerFilter.class, "invalid", t));
								return;
							}
						}
						refresh();
					}
				});
			}
		};
		b.setRect(0, y, WIDTH, BTN_H);
		content.add(b);
		return y + BTN_H + GAP;
	}
}
