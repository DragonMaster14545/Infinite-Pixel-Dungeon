package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Rankings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class WndRecordBoard extends Window {

	private static final int WIDTH = 115;
	private static final int HEIGHT = 144;
	private static final int ROW = 16;

	public static final int HALL = 1;

	private static int hallPage = 0;

	public WndRecordBoard(int kind) {
		this(kind, hallPage);
	}

	public WndRecordBoard(int kind, final int page) {
		Rankings.INSTANCE.load();
		final boolean hall = kind == HALL;
		final int pages = Math.max(1, Rankings.INSTANCE.hallPages);
		final int current = hall ? Math.max(0, Math.min(page, pages - 1)) : 0;
		if (hall) hallPage = current;
		ArrayList<Rankings.Record> list = new ArrayList<>();
		for (Rankings.Record r : Rankings.INSTANCE.hallRecords) {
			if (r.hallPage == current) list.add(r);
		}
		String key = "hall";

		resize(WIDTH, HEIGHT);

		ScrollPane pane = new ScrollPane(new Component());
		add(pane);
		pane.setRect(0, 0, WIDTH, HEIGHT);
		Component content = pane.content();

		Image icon = Icons.BADGES.get();
		IconTitle title = new IconTitle(icon, Messages.get(WndRecordBoard.class, key + "_title"));
		title.setRect(0, 0, WIDTH, 0);
		content.add(title);

		float top = title.bottom() + 4;

		if (hall) {
			RedButton prev = new RedButton("<") {
				@Override
				public void onClick() {
					super.onClick();
					WndRecordBoard.this.hide();
					ShatteredPixelDungeon.scene().addToFront(new WndRecordBoard(HALL, current - 1));
				}
			};
			prev.enable(current > 0);
			prev.setRect(0, top, 20, 14);
			content.add(prev);

			RedButton next = new RedButton(">") {
				@Override
				public void onClick() {
					super.onClick();
					WndRecordBoard.this.hide();
					ShatteredPixelDungeon.scene().addToFront(new WndRecordBoard(HALL, current + 1));
				}
			};
			next.enable(current < pages - 1);
			next.setRect(WIDTH - 20, top, 20, 14);
			content.add(next);

			RenderedTextBlock label = PixelScene.renderTextBlock(Messages.get(WndRecordBoard.class, "hall_page_label", current + 1, pages), 7);
			label.setPos((WIDTH - label.width()) / 2f, top + (14 - label.height()) / 2f);
			content.add(label);

			top += 18;
		}

		if (list.isEmpty()) {
			RenderedTextBlock empty = PixelScene.renderTextBlock(Messages.get(WndRecordBoard.class, key + "_empty"), 6);
			empty.maxWidth(WIDTH);
			empty.setPos(0, top);
			content.add(empty);
			top = empty.bottom();
		} else {
			NumberFormat num = NumberFormat.getInstance(Locale.US);
			int rank = 1;
			for (final Rankings.Record rec : new ArrayList<>(list)) {
				String label = rank + ". "
						+ (!rec.customSeed.isEmpty() ? rec.customSeed : (rec.date != null ? rec.date : ""))
						+ "  " + num.format(rec.score);
				RedButton row = new RedButton(label, 6) {
					@Override
					public void onClick() {
						super.onClick();
						ShatteredPixelDungeon.scene().addToFront(new WndRanking(rec));
					}
				};
				row.setRect(0, top, WIDTH, ROW - 2);
				content.add(row);
				top += ROW;
				rank++;
			}
		}

		content.setSize(WIDTH, top);
		pane.setRect(0, 0, WIDTH, HEIGHT);
	}
}
