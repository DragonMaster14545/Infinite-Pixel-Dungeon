package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.ShulkerBox;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.InventorySlot;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Game;

import java.util.ArrayList;

public class WndShulkerInspect extends Window {

	private static final int COLS = 5;
	private static final int ROWS = 5;
	private static final long PAGE_SIZE = COLS * ROWS;
	private static final int SLOT_MARGIN = 1;
	private static final int TITLE_HEIGHT = 14;
	private static final int NAV_HEIGHT = 16;
	private static final int FOOTER_HEIGHT = 12;

	private final ShulkerBox box;
	private final long page;

	public WndShulkerInspect(ShulkerBox box) {
		this(box, 0);
	}

	public WndShulkerInspect(ShulkerBox box, long page) {
		super();
		this.box = box;

		final ArrayList<Item> items = box.contents();
		final long pages = Math.max(1, (box.capacity() + PAGE_SIZE - 1) / PAGE_SIZE);
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
				&& (TITLE_HEIGHT + gridHeight + nav + FOOTER_HEIGHT + 20 + chrome.marginTop()) > PixelScene.uiCamera.height) {
			slotHeight--;
			gridHeight -= ROWS;
		}

		RenderedTextBlock title = PixelScene.renderTextBlock(
				Messages.titleCase(box.name()) + "  " + items.size() + "/" + box.capacity(), 8);
		title.hardlight(TITLE_COLOR);
		title.maxWidth(windowWidth - 2);
		title.setPos(1, (TITLE_HEIGHT - title.height()) / 2f - 1);
		PixelScene.align(title);
		add(title);

		for (int i = 0; i < PAGE_SIZE; i++) {
			long index = this.page * PAGE_SIZE + i;
			final Item item = index < items.size() ? items.get((int) index) : null;

			int x = (i % COLS) * (slotWidth + SLOT_MARGIN);
			int y = TITLE_HEIGHT + (i / COLS) * (slotHeight + SLOT_MARGIN);

			InventorySlot slot = new InventorySlot(item) {
				@Override
				public void onClick() {
					showInfo(item);
				}

				@Override
				protected void onRightClick() {
					showInfo(item);
				}

				@Override
				protected boolean onLongClick() {
					return showInfo(item);
				}
			};
			slot.setRect(x, y, slotWidth, slotHeight);
			add(slot);

			if (item == null) slot.enable(false);
		}

		float y = TITLE_HEIGHT + gridHeight + 2;

		if (pages > 1) {
			RedButton prev = new RedButton("<") {
				@Override
				public void onClick() {
					goTo(WndShulkerInspect.this.page - 1);
				}
			};
			prev.setRect(0, y, 36, NAV_HEIGHT);
			prev.enable(this.page > 0);
			add(prev);

			RedButton next = new RedButton(">") {
				@Override
				public void onClick() {
					goTo(WndShulkerInspect.this.page + 1);
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

		//footer: how much everything inside is worth
		long total = 0;
		for (Item item : items) total += item.value();
		RenderedTextBlock footer = PixelScene.renderTextBlock(
				items.isEmpty() ? Messages.get(box, "inspect_empty") : Messages.get(box, "inspect_total", total), 6);
		footer.maxWidth(windowWidth - 2);
		footer.setPos((windowWidth - footer.width()) / 2f, y + 2);
		PixelScene.align(footer);
		add(footer);

		resize(windowWidth, (int) (y + FOOTER_HEIGHT));
	}

	private boolean showInfo(Item item) {
		if (item == null) return false;
		Game.scene().addToFront(new WndInfoItem(item));
		return true;
	}

	private void goTo(long newPage) {
		hide();
		GameScene.show(new WndShulkerInspect(box, newPage));
	}
}
