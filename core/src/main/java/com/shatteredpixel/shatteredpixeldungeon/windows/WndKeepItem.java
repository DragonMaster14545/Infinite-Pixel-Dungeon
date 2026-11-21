package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KeptItems;
import com.shatteredpixel.shatteredpixeldungeon.items.RubyBank;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.ItemSlot;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.Big;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;

public class WndKeepItem extends Window {

    private static final int WIDTH = 140;
    private static final int GAP = 2;
    private static final int BTN_H = 16;
    private static final int ICON = 16;
    private static final int ROW_H = ICON + BTN_H + GAP + 3;

    private final Runnable done;
    private boolean finished = false;
    private int keptNow = 0;
    private final ArrayList<Item> candidates;
    private final ArrayList<Item> kept = new ArrayList<>();
    private final RenderedTextBlock status;
    private final Component content;
    private final ScrollPane pane;
    private final float listH;

    public static void offer(Runnable done) {
        if (RubyBank.get().signum() <= 0 || KeptItems.candidates(Dungeon.hero).isEmpty()) {
            if (done != null) done.run();
            return;
        }
        ShatteredPixelDungeon.scene().addToFront(new WndKeepItem(done));
    }

    public WndKeepItem(Runnable done) {
        super();
        this.done = done;
        candidates = KeptItems.candidates(Dungeon.hero);

        float y = 0;
        RenderedTextBlock title = PixelScene.renderTextBlock(Messages.get(this, "title"), 9);
        title.hardlight(TITLE_COLOR);
        title.maxWidth(WIDTH);
        title.setPos(0, y);
        add(title);
        y += title.height() + GAP;

        RenderedTextBlock info = PixelScene.renderTextBlock(Messages.get(this, "info", KeptItems.MAX_PER_ENDING), 6);
        info.maxWidth(WIDTH);
        info.setPos(0, y);
        add(info);
        y += info.height() + GAP;

        status = PixelScene.renderTextBlock("", 7);
        status.maxWidth(WIDTH);
        status.hardlight(0xFF4466);
        status.setPos(0, y);
        add(status);
        y += status.height() + GAP + 6;

        float maxList = PixelScene.uiCamera.height - 60 - y - BTN_H - chrome.marginVer();
        listH = Math.max(ROW_H * 2, Math.min(candidates.size() * ROW_H, maxList));
        resize(WIDTH, (int) Math.ceil(y + listH + GAP + BTN_H));

        pane = new ScrollPane(new Component());
        add(pane);
        pane.setRect(0, y, WIDTH, listH);
        content = pane.content();
        y += listH + GAP;

        RedButton close = new RedButton(Messages.get(this, "done")) {
            @Override
            public void onClick() {
                hide();
            }
        };
        close.setRect(0, y, WIDTH, BTN_H);
        add(close);

        refresh();
    }

    private void refresh() {
        status.text(Messages.get(this, "status", Big.fmt(RubyBank.get()), keptNow, KeptItems.MAX_PER_ENDING));
        float scrollY = content.camera != null ? content.camera.scroll.y : 0;
        content.clear();
        float cy = 0;
        for (final Item item : candidates) {
            ItemSlot slot = new ItemSlot(item) {
                @Override
                public void onClick() {
                    Game.scene().addToFront(new WndInfoItem(item));
                }
            };
            slot.setRect(0, cy, ICON, ICON);
            content.add(slot);

            RenderedTextBlock label = PixelScene.renderTextBlock(Messages.titleCase(item.title()), 6);
            label.maxWidth(WIDTH - ICON - 2);
            label.hardlight(kept.contains(item) ? 0x808080 : 0xFFFFFF);
            label.setPos(ICON + 2, cy + (ICON - label.height()) / 2f);
            PixelScene.align(label);
            content.add(label);

            final java.math.BigInteger price = KeptItems.cost(item);
            boolean isKept = kept.contains(item);
            RedButton btn = new RedButton(isKept ? Messages.get(this, "kept")
                    : Messages.get(this, "keep", Big.fmt(price)), 7) {
                @Override
                public void onClick() {
                    keep(item, price);
                }
            };
            btn.setRect(0, cy + ICON, WIDTH, BTN_H);
            btn.enable(!isKept && keptNow < KeptItems.MAX_PER_ENDING && RubyBank.get().compareTo(price) >= 0);
            content.add(btn);
            cy += ROW_H;
        }
        content.setSize(WIDTH, cy);
        pane.setSize(WIDTH, listH);
        pane.scrollTo(0, scrollY);
    }

    private void keep(Item item, java.math.BigInteger price) {
        if (KeptItems.stored().size() >= KeptItems.MAX_STORED) {
            Game.scene().addToFront(new WndMessage(Messages.get(this, "full")));
            return;
        }
        if (!RubyBank.spend(price)) return;
        KeptItems.add(KeptItems.copyOf(item));
        kept.add(item);
        keptNow++;
        Sample.INSTANCE.play(Assets.Sounds.EVOKE);
        refresh();
    }

    @Override
    public void hide() {
        super.hide();
        if (!finished) {
            finished = true;
            if (done != null) done.run();
        }
    }

    @Override
    public void onBackPressed() {
        hide();
    }
}
