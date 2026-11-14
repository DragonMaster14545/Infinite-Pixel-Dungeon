package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.InventorySlot;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.watabou.noosa.Group;

import java.util.ArrayList;

public class WndShulkerItem extends WndInfoItem {

	private static final float GAP = 2;
	private static final int BUTTON_HEIGHT = 16;
	private static final int PER_ROW = 3;

	public static final int PIN_COLOR = 0xFFD040;

	public static void markPinned(Group parent, InventorySlot slot) {
		RenderedTextBlock mark = PixelScene.renderTextBlock("*", 9);
		mark.hardlight(PIN_COLOR);
		mark.setPos(slot.left() + 1, slot.bottom() - mark.height() - 1);
		PixelScene.align(mark);
		parent.add(mark);
	}

	public static String pinLabel(boolean pinned) {
		return Messages.get(WndShulkerItem.class, pinned ? "unpin" : "pin");
	}

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
