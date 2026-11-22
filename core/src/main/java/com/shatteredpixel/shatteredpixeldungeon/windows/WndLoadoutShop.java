package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Loadout;
import com.shatteredpixel.shatteredpixeldungeon.items.RubyBank;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.Big;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;

public class WndLoadoutShop extends Window {

    private static final int WIDTH = 140;
    private static final int GAP = 2;
    private static final int BTN_H = 16;
    private static final int ICON = 16;
    private static final int ROW_H = ICON + BTN_H + GAP + 3;

    private final RenderedTextBlock status;
    private final ScrollPane pane;
    private final Component content;
    private final float listH;

    public WndLoadoutShop() {
        super();

        float y = 0;
        RenderedTextBlock title = PixelScene.renderTextBlock(Messages.get(this, "title"), 9);
        title.hardlight(TITLE_COLOR);
        title.maxWidth(WIDTH);
        title.setPos(0, y);
        add(title);
        y += title.height() + GAP;

        RenderedTextBlock info = PixelScene.renderTextBlock(Messages.get(this, "info", Loadout.MAX_ITEMS), 6);
        info.maxWidth(WIDTH);
        info.setPos(0, y);
        add(info);
        y += info.height() + GAP;

        status = PixelScene.renderTextBlock("", 7);
        status.maxWidth(WIDTH);
        status.hardlight(0xFF4466);
        status.setPos(0, y);
        add(status);
        y += 18 + GAP;

        float maxList = PixelScene.uiCamera.height - 60 - y - BTN_H - chrome.marginVer();
        listH = Math.max(ROW_H * 2, Math.min(Loadout.STOCK_SIZE * ROW_H, maxList));
        resize(WIDTH, (int) Math.ceil(y + listH + GAP + BTN_H));

        pane = new ScrollPane(new Component());
        add(pane);
        pane.setRect(0, y, WIDTH, listH);
        content = pane.content();
        y += listH + GAP;

        float half = (WIDTH - GAP) / 2f;
        RedButton reroll = new RedButton(Messages.get(this, "reroll", Loadout.REROLL_COST)) {
            @Override
            public void onClick() {
                if (RubyBank.spend(java.math.BigInteger.valueOf(Loadout.REROLL_COST))) {
                    Loadout.reroll();
                    Sample.INSTANCE.play(Assets.Sounds.EVOKE);
                    refresh();
                } else {
                    Game.scene().addToFront(new WndMessage(Messages.get(WndLoadoutShop.class, "no_ruby")));
                }
            }
        };
        reroll.setRect(0, y, half, BTN_H);
        add(reroll);

        RedButton close = new RedButton(Messages.get(this, "close")) {
            @Override
            public void onClick() {
                hide();
            }
        };
        close.setRect(half + GAP, y, half, BTN_H);
        add(close);

        refresh();
    }

    private static String safeTitle(Item item) {
        try {
            return item.title();
        } catch (Throwable t) {
            try {
                return item.name();
            } catch (Throwable t2) {
                return item.getClass().getSimpleName();
            }
        }
    }

    private static void showInfo(Item item) {
        try {
            Game.scene().addToFront(new WndInfoItem(item));
        } catch (Throwable t) {
            String text;
            try {
                text = item.desc();
            } catch (Throwable t2) {
                text = safeTitle(item);
            }
            Game.scene().addToFront(new WndMessage(text));
        }
    }

    private float checkTimer = 0f;

    @Override
    public void update() {
        super.update();
        checkTimer += Game.elapsed;
        if (checkTimer >= 1f) {
            checkTimer = 0f;
            if (Loadout.expired()) {
                refresh();
            } else {
                updateStatus();
            }
        }
    }

    private void updateStatus() {
        long ms = Loadout.msLeft();
        long hours = ms / 3600000L;
        long minutes = (ms % 3600000L) / 60000L;
        status.text(Messages.get(this, "status", Big.fmt(RubyBank.get()), Loadout.pack().size(), Loadout.MAX_ITEMS)
                + "\n" + Messages.get(this, "restock_in", hours, minutes));
    }

    private void refresh() {
        updateStatus();
        float scrollY = content.camera != null ? content.camera.scroll.y : 0;
        content.clear();
        ArrayList<Loadout.Entry> list = Loadout.stock();
        float cy = 0;
        for (final Loadout.Entry entry : list) {
            final Item item = entry.item;
            try {
                IconButton iconBtn = new IconButton(new ItemSprite(item)) {
                    @Override
                    public void onClick() {
                        showInfo(item);
                    }
                };
                iconBtn.setRect(0, cy, ICON, ICON);
                content.add(iconBtn);
            } catch (Throwable ignored) {
            }

            RenderedTextBlock label = PixelScene.renderTextBlock(Messages.titleCase(safeTitle(item)), 6);
            label.maxWidth(WIDTH - ICON - 2);
            label.hardlight(entry.sold ? 0x808080 : 0xFFFFFF);
            label.setPos(ICON + 2, cy + (ICON - label.height()) / 2f);
            PixelScene.align(label);
            content.add(label);

            RedButton btn = new RedButton(entry.sold ? Messages.get(this, "sold")
                    : Messages.get(this, "buy", Big.fmt(entry.price)), 7) {
                @Override
                public void onClick() {
                    String err;
                    try {
                        err = Loadout.buy(entry);
                    } catch (Throwable t) {
                        err = "failed";
                    }
                    if (err != null) {
                        Game.scene().addToFront(new WndMessage(Messages.get(WndLoadoutShop.class, err)));
                    } else {
                        Sample.INSTANCE.play(Assets.Sounds.GOLD);
                    }
                    refresh();
                }
            };
            btn.setRect(0, cy + ICON, WIDTH, BTN_H);
            btn.enable(!entry.sold && RubyBank.get().compareTo(entry.price) >= 0
                    && Loadout.pack().size() < Loadout.MAX_ITEMS);
            content.add(btn);
            cy += ROW_H;
        }
        content.setSize(WIDTH, cy);
        pane.setSize(WIDTH, listH);
        pane.scrollTo(0, scrollY);
    }
}
