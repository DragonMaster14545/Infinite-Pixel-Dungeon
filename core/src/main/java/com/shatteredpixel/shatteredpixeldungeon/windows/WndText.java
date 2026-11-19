package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;

public class WndText extends Window {

	private static final int WIDTH_P = 120;
	private static final int WIDTH_L = 160;
	private static final int GAP = 2;

	public WndText(String body) {
		this(null, null, body);
	}

	public WndText(String title, String body) {
		this(null, title, body);
	}

	public WndText(Image icon, String title, String body) {
		super();

		int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;
		int maxHeight = (int) (PixelScene.uiCamera.height - chrome.marginVer() - 8);

		float top = 0;
		IconTitle iconTitle = null;
		RenderedTextBlock plainTitle = null;

		if (title != null && !title.isEmpty()) {
			if (icon != null) {
				iconTitle = new IconTitle(icon, title);
				iconTitle.setRect(0, 0, width, 0);
				top = iconTitle.bottom() + GAP;
			} else {
				plainTitle = PixelScene.renderTextBlock(title, 9);
				plainTitle.hardlight(TITLE_COLOR);
				plainTitle.maxWidth(width);
				top = plainTitle.height() + GAP;
			}
		}

		Component content = new Component();
		RenderedTextBlock text = PixelScene.renderTextBlock(body == null ? "" : body, 6);
		text.maxWidth(width);
		text.setPos(0, 0);
		content.add(text);
		int contentHeight = (int) Math.ceil(text.bottom()) + GAP;
		content.setSize(width, contentHeight);

		int viewHeight = Math.min(contentHeight, Math.max(20, maxHeight - (int) Math.ceil(top)));

		resize(width, (int) Math.ceil(top) + viewHeight);

		if (iconTitle != null) add(iconTitle);
		if (plainTitle != null) {
			plainTitle.setPos(0, 0);
			add(plainTitle);
		}

		ScrollPane pane = new ScrollPane(content);
		add(pane);
		pane.setRect(0, (int) Math.ceil(top), width, viewHeight);
		pane.setSize(pane.width(), pane.height());
		pane.scrollTo(0, 0);
	}
}
