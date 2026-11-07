package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.PrismBox;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.ui.Component;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.audio.Sample;

public class WndPrismBox extends Window {

	private static final int WIDTH = 124;
	private static final int GAP = 2;
	private static final int BTN_H = 16;
	private static final int ROW_H = 10 + 1 + BTN_H + GAP;
	private static final int[] STEPS = {-16, -1, 1, 16};
	private static final int LOCK_COLOR = 0xA0A0A0;
	private static final int[] HEAT_COLORS = {0x40FF40, 0xFF8030, 0xFFD040, 0xC0C0C0, 0x6080FF};

	private final PrismBox lock;
	private final ColorBlock colorBlock;
	private final RenderedTextBlock hex, info;
	private final RenderedTextBlock[] labels;
	private final RedButton[][] buttons;

	public WndPrismBox(PrismBox lock) {
		super();
		this.lock = lock;
		int n = lock.dialCount();
		labels = new RenderedTextBlock[n];
		buttons = new RedButton[n][STEPS.length + 1];

		float y = 0;

		RenderedTextBlock title = PixelScene.renderTextBlock(Messages.titleCase(lock.name()), 9);
		title.hardlight(TITLE_COLOR);
		title.setPos(0, y);
		add(title);
		y += title.height() + GAP;

		colorBlock = new ColorBlock(WIDTH, 14, 0xFFFFFFFF);
		colorBlock.y = y;
		add(colorBlock);
		y += 14 + GAP;

		hex = PixelScene.renderTextBlock("#000000", 7);
		hex.setPos(0, y);
		add(hex);
		y += hex.height() + GAP;

		RedButton tryBtn = new RedButton(Messages.get(PrismBox.class, "try")) {
			@Override
			public void onClick() {
				boolean done = lock.attempt(Dungeon.hero);
				if (done) {
					hide();
				} else {
					Sample.INSTANCE.play(Assets.Sounds.CLICK);
					refresh();
				}
			}
		};

		// measure info text BEFORE sizing the window
		info = PixelScene.renderTextBlock("", 6);
		info.maxWidth(WIDTH);
		updateInfo();
		float infoH = info.height() + 4; // small reserve in case the text wraps differently later

		float footer = BTN_H + GAP + BTN_H + GAP + infoH;
		float contentH = n * ROW_H;
		float maxList = PixelScene.uiCamera.height - 60 - y - footer - chrome.marginVer();
		float listH = Math.max(ROW_H * 2, Math.min(contentH, maxList));

		// size the window FIRST so the ScrollPane's camera is set up against the final position
		resize(WIDTH, (int) Math.ceil(y + listH + GAP + footer));

		ScrollPane pane = new ScrollPane(new Component());
		add(pane);
		pane.setRect(0, y, WIDTH, listH);
		Component content = pane.content();
		content.clear();

		float cy = 0;
		for (int d = 0; d < n; d++) {
			final int dial = d;

			labels[d] = PixelScene.renderTextBlock("", 8);
			labels[d].setPos(0, cy);
			content.add(labels[d]);
			cy += 11;

			float bw = (WIDTH - GAP * 4) / 5f;
			for (int i = 0; i < STEPS.length; i++) {
				final int step = STEPS[i];
				RedButton btn = new RedButton((step > 0 ? "+" : "") + step) {
					@Override
					public void onClick() {
						lock.turn(dial, step);
						refresh();
					}
				};
				btn.setRect(i * (bw + GAP), cy, bw, BTN_H);
				content.add(btn);
				buttons[d][i] = btn;
			}

			RedButton lockBtn = new RedButton(Messages.get(PrismBox.class, "lock")) {
				@Override
				public void onClick() {
					if (lock.lockDial(Dungeon.hero, dial)) hide();
					else refresh();
				}
			};
			lockBtn.setRect(STEPS.length * (bw + GAP), cy, bw, BTN_H);
			content.add(lockBtn);
			buttons[d][STEPS.length] = lockBtn;
			cy += BTN_H + GAP;
		}
		content.setSize(WIDTH, cy);
		pane.setSize(WIDTH, listH);

		y += listH + GAP;

		tryBtn.setRect(0, y, WIDTH, BTN_H);
		add(tryBtn);
		y += BTN_H + GAP;

		RedButton lockAll = new RedButton(Messages.get(PrismBox.class, "lock_all")) {
			@Override
			public void onClick() {
				GameScene.show(new WndOptions(
						Messages.titleCase(lock.name()),
						Messages.get(PrismBox.class, "lock_all_body"),
						Messages.get(PrismBox.class, "lock_all_yes"),
						Messages.get(PrismBox.class, "lock_all_no")) {
					@Override
					protected void onSelect(int index) {
						if (index == 0) {
							lock.lockAll(Dungeon.hero);
							WndPrismBox.this.hide();
						}
					}
				});
			}
		};
		lockAll.setRect(0, y, WIDTH, BTN_H);
		add(lockAll);
		y += BTN_H + GAP;

		info.setPos(0, y);
		add(info);

		refresh();
	}

	private void updateInfo() {
		info.text(Messages.get(PrismBox.class, "info",
				lock.lives(), lock.lockedCount(), lock.dialCount(), lock.attempts(),
				lock.burning(), lock.hot(), lock.warm()));
	}

	private void refresh() {
		colorBlock.hardlight(lock.rgb());
		hex.text(String.format("#%06X", lock.rgb()));

		for (int d = 0; d < lock.dialCount(); d++) {
			String text = (d + 1) + ": " + lock.dial(d);
			int color = 0xFFFFFF;
			if (lock.isForfeit(d)) {
				text += "  " + Messages.get(PrismBox.class, "locked");
				color = LOCK_COLOR;
			} else if (lock.isLocked(d)) {
				text += "  " + Messages.get(PrismBox.class, "heat_0");
				color = HEAT_COLORS[0];
			} else if (lock.heat(d) >= 0) {
				text += "  (" + Messages.get(PrismBox.class, "heat_" + lock.heat(d)) + ")";
				color = HEAT_COLORS[lock.heat(d)];
			}
			labels[d].text(text);
			labels[d].hardlight(color);
			for (RedButton b : buttons[d]) b.enable(!lock.isLocked(d));
		}

		info.text(Messages.get(PrismBox.class, "info",
				lock.lives(), lock.lockedCount(), lock.dialCount(), lock.attempts(),
				lock.burning(), lock.hot(), lock.warm()));
	}
}
