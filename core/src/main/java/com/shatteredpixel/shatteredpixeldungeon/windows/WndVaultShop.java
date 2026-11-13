package com.shatteredpixel.shatteredpixeldungeon.windows;



import com.shatteredpixel.shatteredpixeldungeon.utils.Big;
import java.math.BigInteger;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.ShulkerBox;
import com.shatteredpixel.shatteredpixeldungeon.items.VaultStore;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.VaultLink;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Component;

import java.util.Locale;

public class WndVaultShop extends Window {

	private static final int WIDTH = 140;
	private static final int GAP = 2;
	private static final int BTN_H = 16;
	private static final int ICON = 18;
	private static final int ROW_H = ICON + BTN_H + GAP + 3;

	private final VaultLink link;
	private final Runnable back;

	private final RenderedTextBlock title, gold, energy, note;
	private final RedButton[] goldBtn, energyBtn;
	private final RenderedTextBlock[] slotText = new RenderedTextBlock[VaultLink.SLOT_PACKS.length];

	private static String fmt(long n) {
		return String.format(Locale.US, "%,d", n);
	}

	public WndVaultShop(VaultLink link, Runnable back) {
		super();
		this.link = link;
		this.back = back;

		if (WndBag.INSTANCE != null) {
			WndBag.INSTANCE.hide();
		}
		WndBag.INSTANCE = this;

		final BigInteger[] levels = VaultLink.SHOP_LEVELS;
		final BigInteger[] packs = VaultLink.SLOT_PACKS;
		final int rows = levels.length + packs.length;
		goldBtn = new RedButton[rows];
		energyBtn = new RedButton[rows];

		float y = 0;

		title = PixelScene.renderTextBlock("", 9);
		title.hardlight(TITLE_COLOR);
		title.maxWidth(WIDTH);
		title.setPos(0, y);
		add(title);
		y += 11 + GAP;

		float lineH = PixelScene.renderTextBlock("A", 7).height();
		gold = PixelScene.renderTextBlock("", 7);
		gold.maxWidth(WIDTH / 2 - 2);
		gold.hardlight(0xFFE060);
		gold.setPos(0, y);
		add(gold);
		energy = PixelScene.renderTextBlock("", 7);
		energy.maxWidth(WIDTH / 2 - 2);
		energy.hardlight(0x60E0FF);
		energy.setPos(WIDTH / 2, y);
		add(energy);
		y += lineH * 2 + GAP;

		note = PixelScene.renderTextBlock("", 6);
		note.maxWidth(WIDTH);
		note.hardlight(0xB0B0B0);
		note.text(Messages.get(WndVaultShop.class, "note", link.inflationPercent()));
		float noteH = note.height() + GAP;

		float footer = noteH + BTN_H;
		float contentH = rows * ROW_H;
		float maxList = PixelScene.uiCamera.height - 60 - y - footer - chrome.marginVer();
		float listH = Math.max(ROW_H * 2, Math.min(contentH, maxList));

		resize(WIDTH, (int) Math.ceil(y + listH + GAP + footer));

		ScrollPane pane = new ScrollPane(new Component());
		add(pane);
		pane.setRect(0, y, WIDTH, listH);
		Component content = pane.content();
		content.clear();

		float bw = (WIDTH - GAP) / 2f;
		float cy = 0;
		for (int i = 0; i < rows; i++) {
			final int row = i;
			final boolean slotRow = i >= levels.length;

			ItemSprite icon;
			String rowText;
			if (!slotRow) {
				final BigInteger level = levels[i].min(BigInteger.valueOf(ShulkerBox.MAX_LEVEL));
				ShulkerBox preview = new ShulkerBox();
				preview.level(BigInteger.valueOf(Big.toInt(level)));
				icon = new ItemSprite(preview);
				rowText = Messages.get(WndVaultShop.class, "row",
						level, preview.capacity(), preview.maxItemLevel(), preview.maxQuantity());
			} else {
				icon = new ItemSprite(link);
				rowText = "";
			}
			icon.x = 0;
			icon.y = cy;
			content.add(icon);

			RenderedTextBlock text = PixelScene.renderTextBlock(rowText, 6);
			if (slotRow) slotText[i - levels.length] = text;
			text.maxWidth(WIDTH - ICON - 2);
			text.setPos(ICON + 2, cy + (ICON - text.height()) / 2f);
			PixelScene.align(text);
			content.add(text);

			goldBtn[i] = new RedButton("", 7) {
				@Override
				public void onClick() {
					buy(row, false);
				}
			};
			goldBtn[i].setRect(0, cy + ICON, bw, BTN_H);
			content.add(goldBtn[i]);

			energyBtn[i] = new RedButton("", 7) {
				@Override
				public void onClick() {
					buy(row, true);
				}
			};
			energyBtn[i].setRect(bw + GAP, cy + ICON, bw, BTN_H);
			content.add(energyBtn[i]);

			cy += ROW_H;
		}
		content.setSize(WIDTH, cy);
		pane.setSize(WIDTH, listH);

		y += listH + GAP;

		note.setPos(0, y);
		add(note);
		y += noteH;

		RedButton backBtn = new RedButton(Messages.get(WndVaultShop.class, "back")) {
			@Override
			public void onClick() {
				hide();
				if (WndVaultShop.this.back != null) WndVaultShop.this.back.run();
			}
		};
		backBtn.setRect(0, y, WIDTH, BTN_H);
		add(backBtn);

		refresh();
	}

	private void buy(int row, boolean withEnergy) {
		if (Dungeon.hero == null || !Dungeon.hero.isAlive()) return;

		int boxRows = VaultLink.SHOP_LEVELS.length;
		String error = row < boxRows
				? link.buy(Dungeon.hero, VaultLink.SHOP_LEVELS[row], withEnergy)
				: link.buySlots(Dungeon.hero, VaultLink.SLOT_PACKS[row - boxRows], withEnergy);
		if (error != null) {
			Game.scene().addToFront(new WndMessage(Messages.get(link, error)));
		} else {
			Sample.INSTANCE.play(Assets.Sounds.GOLD);
		}
		refresh();
	}

	private void refresh() {
		title.text(Messages.get(WndVaultShop.class, "title", VaultStore.boxes().size(), link.slots()));
		gold.text(Messages.get(WndVaultShop.class, "gold", Big.fmt(Dungeon.gold)));
		energy.text(Messages.get(WndVaultShop.class, "energy", Big.fmt(Dungeon.energy)));
		note.text(Messages.get(WndVaultShop.class, "note", link.inflationPercent()));

		BigInteger[] levels = VaultLink.SHOP_LEVELS;
		for (int i = 0; i < levels.length; i++) {
			BigInteger level = levels[i].min(BigInteger.valueOf(ShulkerBox.MAX_LEVEL));
			setRow(i, link.goldPrice(level), link.energyPrice(level));
		}

		BigInteger[] packs = VaultLink.SLOT_PACKS;
		for (int p = 0; p < packs.length; p++) {
			int i = levels.length + p;
			BigInteger count = link.packSize(packs[p]);
			RenderedTextBlock t = slotText[p];
			t.text(Messages.get(WndVaultShop.class, "slots_row", count.max(packs[p]), link.extraSlots(), link.slots()));
			t.setPos(ICON + 2, i * ROW_H + (ICON - t.height()) / 2f);
			PixelScene.align(t);
			if ((count.compareTo(BigInteger.ZERO) <= 0)) {
				goldBtn[i].text(Messages.get(WndVaultShop.class, "slots_max"));
				goldBtn[i].enable(false);
				energyBtn[i].text(Messages.get(WndVaultShop.class, "slots_max"));
				energyBtn[i].enable(false);
			} else {
				setRow(i, link.slotGoldPrice(packs[p]), link.slotEnergyPrice(packs[p]));
			}
		}
	}

	private void setRow(int i, BigInteger g, BigInteger e) {
		goldBtn[i].text(Messages.get(WndVaultShop.class, "buy_gold", Big.fmt(g)));
		goldBtn[i].enable((Dungeon.gold.compareTo(g) >= 0));
		energyBtn[i].text(Messages.get(WndVaultShop.class, "buy_energy", Big.fmt(e)));
		energyBtn[i].enable((Dungeon.energy.compareTo(e) >= 0));
	}

	@Override
	public void hide() {
		super.hide();
		if (WndBag.INSTANCE == this) {
			WndBag.INSTANCE = null;
		}
	}
}
