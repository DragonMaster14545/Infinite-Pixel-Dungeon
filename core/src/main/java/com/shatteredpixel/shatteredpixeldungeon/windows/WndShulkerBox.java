package com.shatteredpixel.shatteredpixeldungeon.windows;



import com.shatteredpixel.shatteredpixeldungeon.utils.Big;
import java.math.BigInteger;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.CompartmentBox;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.ShulkerBox;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.InventorySlot;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Game;

import java.util.ArrayList;

public class WndShulkerBox extends Window {

    private static final int COLS = 5;
    private static final int ROWS = 5;
    private static final int SLOT_MARGIN = 1;
    private static final int TITLE_HEIGHT = 14;
	private static final int NAV_HEIGHT = 16;
	private static final int FOOTER = 2;
	private static final int ACTION_HEIGHT = 16;
	private static final int COMP_HEIGHT = 16;
	private static final int PAGE_SIZE = ROWS * COLS;

    private final ShulkerBox box;
	private final long page;
	private final String query;

	public WndShulkerBox(ShulkerBox box) {
		this(box, 0);
	}

    public WndShulkerBox(ShulkerBox box, int page) {
        this(box, page, "");
    }

    public WndShulkerBox(ShulkerBox box, int page, String query) {
        super();
        this.box = box;
        this.query = query == null ? "" : query.trim();

        if (WndBag.INSTANCE != null) {
            WndBag.INSTANCE.hide();
        }
        WndBag.INSTANCE = this;

		final BigInteger capacity = box.capacity();
		final long pages = Big.toLong(BigInteger.ONE.max((capacity.add(BigInteger.valueOf(PAGE_SIZE)).subtract(BigInteger.ONE)).divide(BigInteger.valueOf(PAGE_SIZE))));
		final ArrayList<Item> shown = filtered(box, this.query);
		long pagesShown = pages;
		int usedPages = Math.max(1, (box.contents().size() + PAGE_SIZE - 1) / PAGE_SIZE);
		long lastPage = Math.min(pages, usedPages) - 1;
		if (!this.query.isEmpty()) {
			pagesShown = Math.max(1, (shown.size() + PAGE_SIZE - 1) / PAGE_SIZE);
			lastPage = pagesShown - 1;
		}
		final long pageCount = pagesShown;
		this.page = Math.max(0, Math.min(page, lastPage));

		int slotWidth = 28;
		int slotHeight = 28;
		int nav = pageCount > 1 ? NAV_HEIGHT + 2 : 0;
		final boolean compartments = box instanceof CompartmentBox;
		int compRow = box instanceof CompartmentBox ? COMP_HEIGHT + 2 : 0;

        int windowWidth = slotWidth * COLS + SLOT_MARGIN * (COLS - 1);
        while (slotWidth >= 20 && (windowWidth + chrome.marginHor()) > PixelScene.uiCamera.width) {
            slotWidth--;
            windowWidth -= COLS;
        }
        int windowHeight = compRow + TITLE_HEIGHT + slotHeight * ROWS + SLOT_MARGIN * (ROWS - 1) + nav + ACTION_HEIGHT + 2 + FOOTER;
        while (slotHeight >= 20 && (windowHeight + 20 + chrome.marginTop()) > PixelScene.uiCamera.height) {
            slotHeight--;
            windowHeight -= ROWS;
        }

        RenderedTextBlock title = PixelScene.renderTextBlock(Messages.titleCase(box.name()), 8);
        title.hardlight(TITLE_COLOR);
        title.maxWidth(windowWidth - 2);
        title.setPos(1, (TITLE_HEIGHT - title.height()) / 2f - 1);
        PixelScene.align(title);
        add(title);

        if (compartments) {
			final CompartmentBox cbox = (CompartmentBox) box;
			RedButton drop = WndShulkerCompartments.dropdown(cbox, this, new Runnable() {
				@Override
				public void run() {
					GameScene.show(new WndShulkerBox(cbox, 0));
				}
			}, windowWidth);
			drop.setRect(0, TITLE_HEIGHT, windowWidth, COMP_HEIGHT);
			add(drop);
		}

        ArrayList<Item> items = shown;
		for (int i = 0; i < PAGE_SIZE; i++) {
			long index = this.page * PAGE_SIZE + i;
			final Item item = index < items.size() ? items.get((int) index) : null;

            int x = (i % COLS) * (slotWidth + SLOT_MARGIN);
            int y = compRow + TITLE_HEIGHT + (i / COLS) * (slotHeight + SLOT_MARGIN);

            InventorySlot slot = new InventorySlot(item) {
                @Override
                public void onClick() {
                    inspect(item);
                }

                @Override
                protected void onRightClick() {
                    inspect(item);
                }

                @Override
                protected boolean onLongClick() {
                    return inspect(item);
                }
            };
            slot.setRect(x, y, slotWidth, slotHeight);
            add(slot);

            if (item == null) slot.enable(false);
            else if (box.isPinned(item)) WndShulkerItem.markPinned(this, slot);
        }

		if (pageCount > 1) {
			float navY = windowHeight - FOOTER - ACTION_HEIGHT - 2 - NAV_HEIGHT;

			RedButton prev = new RedButton("<") {
				@Override
				public void onClick() {
					goTo((int) (WndShulkerBox.this.page - 1));
				}
			};
			prev.setRect(0, navY, 36, NAV_HEIGHT);
			prev.enable(this.page > 0);
			add(prev);

			RedButton next = new RedButton(">") {
				@Override
				public void onClick() {
					goTo((int) (WndShulkerBox.this.page + 1));
				}
			};
			next.setRect(windowWidth - 36, navY, 36, NAV_HEIGHT);
			next.enable(this.page < pageCount - 1);
			add(next);

			RenderedTextBlock label = PixelScene.renderTextBlock((this.page + 1) + "/" + pageCount, 8);
			label.setPos((windowWidth - label.width()) / 2f, navY + (NAV_HEIGHT - label.height()) / 2f);
			PixelScene.align(label);
			add(label);
		}

		float actionY = windowHeight - FOOTER - ACTION_HEIGHT;

		RedButton sort = new RedButton(Messages.get(WndShulkerBox.class, "sort")) {
			@Override
			public void onClick() {
				box.sortContents();
				hide();
				GameScene.show(new WndShulkerBox(box, 0, WndShulkerBox.this.query));
			}
		};
		float half = (windowWidth - SLOT_MARGIN) / 2f;
		sort.setRect(0, actionY, half, ACTION_HEIGHT);
		sort.enable(box.size() > 1);
		add(sort);

		String searchLabel = this.query.isEmpty()
				? Messages.get(WndShulkerBox.class, "search")
				: Messages.get(WndShulkerBox.class, "search_active", this.query);
		RedButton search = new RedButton(searchLabel, 7) {
			@Override
			public void onClick() {
				askSearch();
			}
		};
		search.setRect(half + SLOT_MARGIN, actionY, half, ACTION_HEIGHT);
		add(search);

		resize(windowWidth, windowHeight);
	}

	private static ArrayList<Item> filtered(ShulkerBox box, String query) {
		ArrayList<Item> all = box.contents();
		if (query == null || query.trim().isEmpty()) return all;
		String[] words = query.trim().toLowerCase().split("\\s+");
		ArrayList<Item> out = new ArrayList<>();
		for (Item item : all) {
			if (matches(item, words, 0)) out.add(item);
		}
		return out;
	}

	private static boolean matches(Item item, String[] words, int depth) {
		if (item == null) return false;
		String name = item.name() == null ? "" : item.name().toLowerCase();
		boolean ok = true;
		for (String word : words) {
			if (!name.contains(word)) {
				ok = false;
				break;
			}
		}
		if (ok) return true;
		if (item instanceof Bag && depth < 4) {
			for (Item inner : new ArrayList<>(((Bag) item).items)) {
				if (matches(inner, words, depth + 1)) return true;
			}
		}
		return false;
	}

	private void askSearch() {
		GameScene.show(new WndTextInput(
				Messages.get(WndShulkerBox.class, "search_title"),
				Messages.get(WndShulkerBox.class, "search_body"),
				query,
				60,
				false,
				Messages.get(WndShulkerBox.class, "search_go"),
				Messages.get(WndShulkerBox.class, "search_clear")) {
			@Override
			public void onSelect(boolean positive, String text) {
				WndShulkerBox.this.hide();
				GameScene.show(new WndShulkerBox(box, 0, positive ? text : ""));
			}
		});
	}

	private void goTo(int newPage) {
		hide();
		GameScene.show(new WndShulkerBox(box, newPage, query));
	}

	private boolean inspect(final Item item) {
		if (item == null || Dungeon.hero == null || !Dungeon.hero.isAlive()) return false;

		ArrayList<String> labels = new ArrayList<>();
		ArrayList<Runnable> runs = new ArrayList<>();

		labels.add(Messages.get(WndShulkerItem.class, "take"));
		runs.add(new Runnable() {
			@Override
			public void run() {
				takeOut(item);
			}
		});

		labels.add(WndShulkerItem.pinLabel(box.isPinned(item)));
		runs.add(new Runnable() {
			@Override
			public void run() {
				togglePin(item);
			}
		});

		if (box instanceof CompartmentBox && ((CompartmentBox) box).compartmentCount() > 1) {
			final CompartmentBox cbox = (CompartmentBox) box;
			labels.add(Messages.get(WndShulkerItem.class, "move"));
			runs.add(new Runnable() {
				@Override
				public void run() {
					WndShulkerCompartments.chooseTarget(cbox, item, new Runnable() {
						@Override
						public void run() {
							WndShulkerBox.this.hide();
							GameScene.show(new WndShulkerBox(cbox, (int) page));
						}
					});
				}
			});
		}

		Game.scene().addToFront(new WndShulkerItem(item, labels, runs));
		return true;
	}

	private void togglePin(Item item) {
		if (!box.togglePin(item)) return;

		int index = Math.max(0, box.indexOf(item));
		hide();
		GameScene.show(new WndShulkerBox(box, query.isEmpty() ? index / PAGE_SIZE : 0, query));
	}

	private void takeOut(final Item item) {
		if (item == null || Dungeon.hero == null || !Dungeon.hero.isAlive()) return;

		if (item.stackable && item.quantity().compareTo(BigInteger.ONE) > 0) {
			ShulkerBox.promptAmount(box, "take", item.quantity(), new ShulkerBox.AmountCallback() {
				@Override
				public void apply(BigInteger amount) {
					finishTake(item, amount);
				}
			});
		} else {
			finishTake(item, null);
		}
	}

	private void finishTake(Item item, BigInteger amount) {
		if (Dungeon.hero == null || !Dungeon.hero.isAlive()) return;

		hide();
		box.takeOut(Dungeon.hero, item, amount);

		if (!box.isEmpty()) {
			GameScene.show(new WndShulkerBox(box, (int) page, query));
		}
	}

    @Override
    public void hide() {
        super.hide();
        if (WndBag.INSTANCE == this) {
            WndBag.INSTANCE = null;
        }
    }
}