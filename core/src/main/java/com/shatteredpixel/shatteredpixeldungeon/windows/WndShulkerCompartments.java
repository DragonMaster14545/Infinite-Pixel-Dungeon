package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.items.CompartmentBox;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

public class WndShulkerCompartments extends Window {

	private static final int MIN_WIDTH = 124;
	private static final int BTN_H = 16;
	private static final int SMALL_W = 28;
	private static final int GAP = 2;

	private final CompartmentBox box;
	private final Item moving;
	private final Runnable onClose;
	private boolean closed = false;

	public static RedButton dropdown(final CompartmentBox box, final Window host, final Runnable reopen, float width) {
		String name = box.compartmentName(box.currentCompartment());
		int size = box.compartmentSize(box.currentCompartment());
		String label = Messages.get(WndShulkerCompartments.class, "dropdown", name, size);
		while (name.length() > 1 && PixelScene.renderTextBlock(label, 6).width() > width - 8) {
			name = name.substring(0, name.length() - 1);
			label = Messages.get(WndShulkerCompartments.class, "dropdown", name + "..", size);
		}
		return new RedButton(label, 6) {
			@Override
			public void onClick() {
				host.hide();
				GameScene.show(new WndShulkerCompartments(box, null, reopen));
			}
		};
	}

	public static void chooseTarget(CompartmentBox box, Item item, Runnable onClose) {
		GameScene.show(new WndShulkerCompartments(box, item, onClose));
	}

	public WndShulkerCompartments(CompartmentBox box, Item moving, Runnable onClose) {
		super();
		this.box = box;
		this.moving = moving;
		this.onClose = onClose;
		final int WIDTH = fitWidth(box, moving);

		RenderedTextBlock title = PixelScene.renderTextBlock(
				Messages.get(this, moving == null ? "title" : "title_move"), 9);
		title.hardlight(TITLE_COLOR);
		title.maxWidth(WIDTH);
		title.setPos(0, 0);
		add(title);

		float y = title.height() + GAP * 2;

		for (int i = 0; i < box.compartmentCount(); i++) {
			final int index = i;
			boolean isCurrent = i == box.currentCompartment();

			if (moving != null && isCurrent) continue;

			String label = (isCurrent ? "* " : "") + box.compartmentName(i)
					+ " (" + box.compartmentSize(i) + "/" + box.capacity() + ")";

			boolean manage = moving == null && i > 0;
			float w = manage ? WIDTH - SMALL_W * 2 - GAP * 2 : WIDTH;

			RedButton pick = new RedButton(label, 6) {
				@Override
				public void onClick() {
					if (WndShulkerCompartments.this.moving != null) {
						WndShulkerCompartments.this.box.moveToCompartment(WndShulkerCompartments.this.moving, index);
					} else {
						WndShulkerCompartments.this.box.selectCompartment(index);
					}
					close();
				}
			};
			pick.setRect(0, y, w, BTN_H);
			add(pick);

			if (manage) {
				RedButton rename = new RedButton(Messages.get(this, "rename"), 6) {
					@Override
					public void onClick() {
						askName(index);
					}
				};
				rename.setRect(w + GAP, y, SMALL_W, BTN_H);
				add(rename);

				RedButton delete = new RedButton(Messages.get(this, "delete"), 6) {
					@Override
					public void onClick() {
						if (WndShulkerCompartments.this.box.deleteCompartment(index)) refresh();
					}
				};
				delete.setRect(w + GAP * 2 + SMALL_W, y, SMALL_W, BTN_H);
				add(delete);
			}

			y += BTN_H + GAP;
		}

		if (moving == null && box.canAddCompartment()) {
			RedButton add = new RedButton(Messages.get(this, "add")) {
				@Override
				public void onClick() {
					askName(-1);
				}
			};
			add.setRect(0, y, WIDTH, BTN_H);
			add(add);
			y += BTN_H + GAP;
		}

		RedButton close = new RedButton(Messages.get(this, "close")) {
			@Override
			public void onClick() {
				close();
			}
		};
		close.setRect(0, y, WIDTH, BTN_H);
		add(close);

		resize(WIDTH, (int) (y + BTN_H));
	}

	private int fitWidth(CompartmentBox box, Item moving) {
		float need = MIN_WIDTH;
		for (int i = 0; i < box.compartmentCount(); i++) {
			if (moving != null && i == box.currentCompartment()) continue;
			String label = (i == box.currentCompartment() ? "* " : "") + box.compartmentName(i)
					+ " (" + box.compartmentSize(i) + "/" + box.capacity() + ")";
			float w = PixelScene.renderTextBlock(label, 6).width() + 8;
			if (moving == null && i > 0) w += SMALL_W * 2 + GAP * 2;
			need = Math.max(need, w);
		}
		float max = PixelScene.uiCamera.width - chrome.marginHor() - 4;
		return (int) Math.ceil(Math.min(need, Math.max(MIN_WIDTH, max)));
	}

	private void askName(final int index) {
		String initial = index > 0 ? box.compartmentName(index) : "";
		GameScene.show(new WndTextInput(
				Messages.get(WndShulkerCompartments.class, index > 0 ? "rename_title" : "add_title"),
				Messages.get(WndShulkerCompartments.class, "name_body", CompartmentBox.NAME_LIMIT),
				initial,
				CompartmentBox.NAME_LIMIT,
				false,
				Messages.get(WndShulkerCompartments.class, "confirm"),
				Messages.get(WndShulkerCompartments.class, "cancel")) {
			@Override
			public void onSelect(boolean positive, String text) {
				if (!positive) return;
				if (index > 0) box.renameCompartment(index, text);
				else box.addCompartment(text);
				refresh();
			}
		});
	}

	private void refresh() {
		closed = true;
		hide();
		GameScene.show(new WndShulkerCompartments(box, moving, onClose));
	}

	private void close() {
		if (closed) return;
		closed = true;
		hide();
		if (onClose != null) onClose.run();
	}

	@Override
	public void onBackPressed() {
		close();
	}
}
