package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;

public class WikiGridPane extends ScrollPane {

	private final ArrayList<Component> items = new ArrayList<>();
	private final ArrayList<ColorBlock> separators = new ArrayList<>();

	private static final int ITEM_SIZE = 17;

	public WikiGridPane() {
		super(new Component());
	}

	@Override
	public void onClick(float x, float y) {
		for (Component item : items) {
			if ((item instanceof ScrollingGridPane.GridItem) && ((ScrollingGridPane.GridItem) item).onClick(x, y)) {
				break;
			}
		}
	}

	public void addItem(ScrollingGridPane.GridItem item) {
		content.add(item);
		items.add(item);
	}

	public void addHeader(String text) {
		addHeader(text, 7, false);
	}

	public void addHeader(String text, int size, boolean center) {
		ScrollingGridPane.GridHeader header = new ScrollingGridPane.GridHeader(text, size, center);
		content.add(header);
		items.add(header);
	}

	public synchronized void clear() {
		content.clear();
		items.clear();
		separators.clear();
	}

	@Override
	protected void layout() {
		float colW = width();
		int perRow = Math.max(1, (int) ((colW + 1) / (ITEM_SIZE + 1)));

		float top = 0;
		float[] colTop = {0, 0};
		float sectionTop = 0;
		boolean sectionOpen = false;
		int sepsUsed = 0;

		int i = 0;
		while (i < items.size()) {
			Component item = items.get(i);

			if (item instanceof ScrollingGridPane.GridHeader && ((ScrollingGridPane.GridHeader) item).center) {
				if (sectionOpen) {
					top = Math.max(colTop[0], colTop[1]);
					sepsUsed = placeSeparator(sepsUsed, colW, sectionTop, top);
					sectionOpen = false;
				}
				item.setRect(0, top, width(), item.height());
				top += item.height() + 1;
				colTop[0] = colTop[1] = top;
				i++;
				continue;
			}

			if (!sectionOpen) {
				sectionTop = top;
				colTop[0] = colTop[1] = top;
				sectionOpen = true;
			}

			int c = 0;
			float left = c * (colW + 1);
			float y = colTop[c];

			if (item instanceof ScrollingGridPane.GridHeader) {
				item.setRect(left, y, colW, item.height());
				y += item.height() + 1;
				i++;
			}

			int n = 0;
			while (i < items.size() && items.get(i) instanceof ScrollingGridPane.GridItem) {
				if (n > 0 && n % perRow == 0) y += ITEM_SIZE + 1;
				items.get(i).setRect(left + (n % perRow) * (ITEM_SIZE + 1), y, ITEM_SIZE, ITEM_SIZE);
				n++;
				i++;
			}
			if (n > 0) y += ITEM_SIZE + 1;

			colTop[c] = y + 2;
		}

		if (sectionOpen) {
			top = Math.max(colTop[0], colTop[1]);
			sepsUsed = placeSeparator(sepsUsed, colW, sectionTop, top);
		}

		while (separators.size() > sepsUsed) {
			ColorBlock sep = separators.remove(separators.size() - 1);
			content.remove(sep);
		}

		content.setSize(width, top);
		super.layout();
	}

	private int placeSeparator(int used, float colW, float from, float to) {
		ColorBlock sep;
		if (separators.size() > used) {
			sep = separators.get(used);
		} else {
			sep = new ColorBlock(1, 1, 0xFF222222);
			separators.add(sep);
			content.add(sep);
		}
		sep.size(1, Math.max(1, to - from));
		sep.x = colW;
		sep.y = from;
		return used + 1;
	}
}
