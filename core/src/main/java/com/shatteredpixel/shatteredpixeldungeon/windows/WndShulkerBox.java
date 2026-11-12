package com.shatteredpixel.shatteredpixeldungeon.windows;



import com.shatteredpixel.shatteredpixeldungeon.utils.Big;
import java.math.BigInteger;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
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
	private static final int PAGE_SIZE = ROWS * COLS;

    private final ShulkerBox box;
	private final long page;

	public WndShulkerBox(ShulkerBox box) {
		this(box, 0);
	}

    public WndShulkerBox(ShulkerBox box, int page) {
        super();
        this.box = box;

        if (WndBag.INSTANCE != null) {
            WndBag.INSTANCE.hide();
        }
        WndBag.INSTANCE = this;

		final BigInteger capacity = box.capacity();
		final long pages = Big.toLong(BigInteger.ONE.max((capacity.add(BigInteger.valueOf(PAGE_SIZE)).subtract(BigInteger.ONE)).divide(BigInteger.valueOf(PAGE_SIZE))));
		int usedPages = Math.max(1, (box.contents().size() + PAGE_SIZE - 1) / PAGE_SIZE);
		this.page = Math.max(0, Math.min(page, Math.min(pages, usedPages) - 1));

		int slotWidth = 28;
		int slotHeight = 28;
		int nav = pages > 1 ? NAV_HEIGHT + 2 : 0;

        int windowWidth = slotWidth * COLS + SLOT_MARGIN * (COLS - 1);
        while (slotWidth >= 20 && (windowWidth + chrome.marginHor()) > PixelScene.uiCamera.width) {
            slotWidth--;
            windowWidth -= COLS;
        }
        int windowHeight = TITLE_HEIGHT + slotHeight * ROWS + SLOT_MARGIN * (ROWS - 1) + nav + ACTION_HEIGHT + 2 + FOOTER;
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

        ArrayList<Item> items = box.contents();
		for (int i = 0; i < PAGE_SIZE; i++) {
			long index = this.page * PAGE_SIZE + i;
			final Item item = index < items.size() ? items.get((int) index) : null;

            int x = (i % COLS) * (slotWidth + SLOT_MARGIN);
            int y = TITLE_HEIGHT + (i / COLS) * (slotHeight + SLOT_MARGIN);

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
            else if (index < box.pinnedCount()) WndShulkerItem.markPinned(this, slot);
        }

		if (pages > 1) {
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
			next.enable(this.page < pages - 1);
			add(next);

			RenderedTextBlock label = PixelScene.renderTextBlock((this.page + 1) + "/" + pages, 8);
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
				GameScene.show(new WndShulkerBox(box, 0));
			}
		};
		sort.setRect(0, actionY, windowWidth, ACTION_HEIGHT);
		sort.enable(box.size() > 1);
		add(sort);

		resize(windowWidth, windowHeight);
	}

	private void goTo(int newPage) {
		hide();
		GameScene.show(new WndShulkerBox(box, newPage));
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

		Game.scene().addToFront(new WndShulkerItem(item, labels, runs));
		return true;
	}

	private void togglePin(Item item) {
		if (!box.togglePin(item)) return;

		int index = Math.max(0, box.indexOf(item));
		hide();
		GameScene.show(new WndShulkerBox(box, index / PAGE_SIZE));
	}

	private void takeOut(Item item) {
		if (item == null || Dungeon.hero == null || !Dungeon.hero.isAlive()) return;

		hide();
		box.takeOut(Dungeon.hero, item);

        if (!box.isEmpty()) {
            GameScene.show(new WndShulkerBox(box, (int) page));
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