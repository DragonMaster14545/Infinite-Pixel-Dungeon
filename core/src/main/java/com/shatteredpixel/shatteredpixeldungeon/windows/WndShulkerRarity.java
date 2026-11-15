package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.ShulkerBox;
import com.shatteredpixel.shatteredpixeldungeon.items.ShulkerFilter;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class WndShulkerRarity extends Window {

	private static final int WIDTH = 126;
	private static final int BTN_H = 16;
	private static final int GAP = 2;

	public WndShulkerRarity(final ShulkerBox box, final float returnScroll) {
		super();

		RenderedTextBlock title = PixelScene.renderTextBlock(Messages.get(this, "title"), 9);
		title.hardlight(TITLE_COLOR);
		title.maxWidth(WIDTH);
		title.setPos(0, 0);
		add(title);

		float top = title.height() + GAP * 2;

		ArrayList<Item.Rarity> rarities = new ArrayList<>();
		for (Item.Rarity r : Item.Rarity.values()) {
			if (r != Item.Rarity.NONE) rarities.add(r);
		}
		Collections.sort(rarities, new Comparator<Item.Rarity>() {
			@Override
			public int compare(Item.Rarity a, Item.Rarity b) {
				return a.multiplier.compareTo(b.multiplier);
			}
		});

		ScrollPane pane = new ScrollPane(new Component());
		add(pane);
		Component content = pane.content();

		float y = 0;

		RedButton any = new RedButton(Messages.get(WndShulkerFilter.class, "any"), 6) {
			@Override
			public void onClick() {
				choose(box, null, returnScroll);
			}
		};
		any.setRect(0, y, WIDTH, BTN_H);
		content.add(any);
		y += BTN_H + GAP;

		for (final Item.Rarity r : rarities) {
			RedButton b = new RedButton(ShulkerFilter.plainRarityName(r), 6) {
				@Override
				public void onClick() {
					choose(box, r, returnScroll);
				}
			};
			b.textColor(r.color);
			b.setRect(0, y, WIDTH, BTN_H);
			content.add(b);
			y += BTN_H + GAP;
		}

		content.setSize(WIDTH, y);

		float paneH = Math.max(BTN_H * 3, Math.min(y, PixelScene.uiCamera.height - 40 - top - chrome.marginVer()));
		resize(WIDTH, (int) Math.ceil(top + paneH));
		pane.setRect(0, top, WIDTH, paneH);
	}

	private void choose(ShulkerBox box, Item.Rarity rarity, float returnScroll) {
		box.filter().rarityMin = rarity;
		hide();
		GameScene.show(new WndShulkerFilter(box, returnScroll));
	}
}
