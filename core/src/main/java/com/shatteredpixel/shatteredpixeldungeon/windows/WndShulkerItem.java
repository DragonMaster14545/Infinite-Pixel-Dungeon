package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;

import java.util.ArrayList;

public class WndShulkerItem extends WndInfoItem {

	private static final float GAP = 2;
	private static final int BUTTON_HEIGHT = 16;
	private static final int PER_ROW = 3;

	public WndShulkerItem(Item item, ArrayList<String> labels, final ArrayList<Runnable> runs) {
		super(item);

		ArrayList<String> allLabels = new ArrayList<>(labels);
		ArrayList<Runnable> allRuns = new ArrayList<>(runs);

		allLabels.add(Messages.get(WndShulkerItem.class, "keep"));
		allRuns.add(null);

		float y = height + GAP;

		for (int start = 0; start < allLabels.size(); start += PER_ROW) {
			int n = Math.min(PER_ROW, allLabels.size() - start);
			float bw = (float) (width - (n - 1)) / n;

			for (int j = 0; j < n; j++) {
				final Runnable run = allRuns.get(start + j);
				RedButton btn = new RedButton(allLabels.get(start + j), 8) {
					@Override
					public void onClick() {
						hide();
						if (run != null) run.run();
					}
				};
				btn.setRect(j * (bw + 1), y, bw, BUTTON_HEIGHT);
				add(btn);
			}
			y += BUTTON_HEIGHT + 1;
		}

		resize(width, (int) y);
	}
}
