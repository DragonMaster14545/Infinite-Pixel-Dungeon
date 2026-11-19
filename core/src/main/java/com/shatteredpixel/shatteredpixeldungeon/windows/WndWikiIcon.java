package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.journal.Wiki;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollingGridPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

import java.util.Locale;
import java.util.Map;

public class WndWikiIcon extends Window {

	public interface Callback {
		void onPick(int sprite);
	}

	private static final int WIDTH = 124;
	private static final int BTN_H = 16;
	private static final int GAP = 2;

	private static String filter = "";

	private final Callback callback;

	private static void showWindow(Window window) {
		if (ShatteredPixelDungeon.scene() instanceof GameScene) {
			GameScene.show(window);
		} else {
			ShatteredPixelDungeon.scene().addToFront(window);
		}
	}

	public WndWikiIcon(Callback callback) {
		super();
		this.callback = callback;

		int height = Math.min(150, (int) (PixelScene.uiCamera.height - chrome.marginVer() - 8));
		resize(WIDTH, height);

		RenderedTextBlock title = PixelScene.renderTextBlock(Messages.get(WndWikiIcon.class, "title"), 9);
		title.hardlight(TITLE_COLOR);
		title.maxWidth(WIDTH);
		title.setPos(0, 0);
		add(title);

		float top = title.height() + GAP;

		String label = filter.isEmpty()
				? Messages.get(WndWikiIcon.class, "search")
				: Messages.get(WndWikiIcon.class, "search_active", filter);
		RedButton search = new RedButton(label, 7) {
			@Override
			public void onClick() {
				showWindow(new WndTextInput(
						Messages.get(WndWikiIcon.class, "search_title"),
						Messages.get(WndWikiIcon.class, "search_body"),
						filter,
						30,
						false,
						Messages.get(WndWikiIcon.class, "search_confirm"),
						Messages.get(WndWikiIcon.class, "search_clear")) {
					@Override
					public void onSelect(boolean positive, String text) {
						filter = positive && text != null ? text.trim() : "";
						WndWikiIcon.this.hide();
						showWindow(new WndWikiIcon(WndWikiIcon.this.callback));
					}
				});
			}
		};
		search.setRect(0, top, WIDTH, BTN_H);
		add(search);
		top += BTN_H + GAP;

		float bottom = height - BTN_H;

		ScrollingGridPane grid = new ScrollingGridPane();
		add(grid);
		grid.setRect(0, top, WIDTH, bottom - top - GAP);

		String f = filter.toLowerCase(Locale.ENGLISH).replace(' ', '_');
		int shown = 0;
		for (Map.Entry<String, Integer> e : Wiki.spriteNames().entrySet()) {
			if (!f.isEmpty() && !e.getKey().toLowerCase(Locale.ENGLISH).contains(f)) continue;
			final int sprite = e.getValue();
			ScrollingGridPane.GridItem cell = new ScrollingGridPane.GridItem(new ItemSprite(sprite)) {
				@Override
				public boolean onClick(float x, float y) {
					if (inside(x, y)) {
						WndWikiIcon.this.hide();
						if (WndWikiIcon.this.callback != null) WndWikiIcon.this.callback.onPick(sprite);
						return true;
					}
					return false;
				}
			};
			grid.addItem(cell);
			shown++;
		}
		if (shown == 0) {
			grid.addHeader(Messages.get(WndWikiIcon.class, "empty"));
		}
		grid.setRect(0, top, WIDTH, bottom - top - GAP);

		RedButton cancel = new RedButton(Messages.get(WndWikiIcon.class, "cancel")) {
			@Override
			public void onClick() {
				hide();
			}
		};
		cancel.setRect(0, bottom, WIDTH, BTN_H);
		add(cancel);
	}
}
