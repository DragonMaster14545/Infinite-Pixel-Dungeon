package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.ShulkerBox;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.VaultLink;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.InventorySlot;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Game;

import java.util.ArrayList;

public class WndVaultLink extends Window {

	private static final int COLS = 5;
	private static final int ROWS = 5;
	private static final int PAGE_SIZE = COLS * ROWS;
	private static final int SLOT_MARGIN = 1;
	private static final int TITLE_HEIGHT = 14;
	private static final int BUTTON_HEIGHT = 18;
	private static final int NAV_HEIGHT = 16;

	private final VaultLink link;
	private final long page;

	public WndVaultLink(VaultLink link) {
		this(link, 0);
	}

	public WndVaultLink(VaultLink link, long page) {
		super();
		this.link = link;

		if (WndBag.INSTANCE != null) {
			WndBag.INSTANCE.hide();
		}
		WndBag.INSTANCE = this;

		final long capacity = link.slots();
		final long pages = Math.max(1, (capacity + PAGE_SIZE - 1) / PAGE_SIZE);
		this.page = Math.max(0, Math.min(page, pages - 1));
		int nav = pages > 1 ? NAV_HEIGHT + 2 : 0;

		int slotWidth = 28;
		int slotHeight = 28;

		int windowWidth = slotWidth * COLS + SLOT_MARGIN * (COLS - 1);
		while (slotWidth >= 20 && (windowWidth + chrome.marginHor()) > PixelScene.uiCamera.width) {
			slotWidth--;
			windowWidth -= COLS;
		}
		int gridHeight = slotHeight * ROWS + SLOT_MARGIN * (ROWS - 1);
		while (slotHeight >= 20
				&& (TITLE_HEIGHT + gridHeight + nav + BUTTON_HEIGHT + 2 + 20 + chrome.marginTop()) > PixelScene.uiCamera.height) {
			slotHeight--;
			gridHeight -= ROWS;
		}

		final ArrayList<ShulkerBox> stored = link.stored();

		RenderedTextBlock title = PixelScene.renderTextBlock(
				Messages.titleCase(link.name()) + "  " + stored.size() + "/" + capacity, 8);
		title.hardlight(TITLE_COLOR);
		title.maxWidth(windowWidth - 2);
		title.setPos(1, (TITLE_HEIGHT - title.height()) / 2f - 1);
		PixelScene.align(title);
		add(title);

		for (int i = 0; i < PAGE_SIZE; i++) {
			long index = this.page * PAGE_SIZE + i;
			final ShulkerBox box = index < stored.size() ? stored.get((int) index) : null;
			final boolean locked = index >= capacity;

			int x = (i % COLS) * (slotWidth + SLOT_MARGIN);
			int y = TITLE_HEIGHT + (i / COLS) * (slotHeight + SLOT_MARGIN);

			InventorySlot slot = new InventorySlot(box) {
				@Override
				public void onClick() {
					activate(box, locked);
				}

				@Override
				protected void onRightClick() {
					activate(box, locked);
				}

				@Override
				protected boolean onLongClick() {
					if (box != null) {
						Game.scene().addToFront(new WndShulkerInspect(box));
						return true;
					}
					return false;
				}
			};
			slot.setRect(x, y, slotWidth, slotHeight);
			add(slot);

			if (locked) slot.enable(false);
		}

		float y = TITLE_HEIGHT + gridHeight + 2;

		if (pages > 1) {
			RedButton prev = new RedButton("<") {
				@Override
				public void onClick() {
					goTo(WndVaultLink.this.page - 1);
				}
			};
			prev.setRect(0, y, 36, NAV_HEIGHT);
			prev.enable(this.page > 0);
			add(prev);

			RedButton next = new RedButton(">") {
				@Override
				public void onClick() {
					goTo(WndVaultLink.this.page + 1);
				}
			};
			next.setRect(windowWidth - 36, y, 36, NAV_HEIGHT);
			next.enable(this.page < pages - 1);
			add(next);

			RenderedTextBlock label = PixelScene.renderTextBlock((this.page + 1) + "/" + pages, 8);
			label.setPos((windowWidth - label.width()) / 2f, y + (NAV_HEIGHT - label.height()) / 2f);
			PixelScene.align(label);
			add(label);

			y += NAV_HEIGHT + 2;
		}

		float half = (windowWidth - SLOT_MARGIN) / 2f;

		RedButton deposit = new RedButton(Messages.get(link, "btn_deposit")) {
			@Override
			public void onClick() {
				hide();
				WndVaultLink.this.link.startDeposit(Dungeon.hero, reopen());
			}
		};
		deposit.setRect(0, y, half, BUTTON_HEIGHT);
		add(deposit);

		RedButton all = new RedButton(Messages.get(link, "btn_deposit_all")) {
			@Override
			public void onClick() {
				hide();
				WndVaultLink.this.link.depositAll(Dungeon.hero);
				reopen().run();
			}
		};
		all.setRect(half + SLOT_MARGIN, y, half, BUTTON_HEIGHT);
		add(all);

		resize(windowWidth, (int) (y + BUTTON_HEIGHT));
	}

	private void goTo(long newPage) {
		hide();
		GameScene.show(new WndVaultLink(link, newPage));
	}

	private Runnable reopen() {
		return new Runnable() {
			@Override
			public void run() {
				GameScene.show(new WndVaultLink(link, page));
			}
		};
	}

	private void activate(ShulkerBox box, boolean locked) {
		if (locked || Dungeon.hero == null || !Dungeon.hero.isAlive()) return;

		if (box == null) {
			hide();
			link.startDeposit(Dungeon.hero, reopen());
		} else {
			show(box);
		}
	}

	private void show(final ShulkerBox box) {
		ArrayList<String> labels = new ArrayList<>();
		ArrayList<Runnable> runs = new ArrayList<>();

		labels.add(Messages.get(WndShulkerItem.class, "take"));
		runs.add(new Runnable() {
			@Override
			public void run() {
				hide();
				link.withdraw(Dungeon.hero, box);
				reopen().run();
			}
		});

		labels.add(Messages.get(WndShulkerItem.class, "inspect"));
		runs.add(new Runnable() {
			@Override
			public void run() {
				Game.scene().addToFront(new WndShulkerInspect(box));
			}
		});

		Game.scene().addToFront(new WndShulkerItem(box, labels, runs));
	}

	@Override
	public void hide() {
		super.hide();
		if (WndBag.INSTANCE == this) {
			WndBag.INSTANCE = null;
		}
	}
}
