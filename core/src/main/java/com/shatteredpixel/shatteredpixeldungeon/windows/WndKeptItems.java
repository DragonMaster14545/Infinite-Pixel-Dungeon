package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KeptItems;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.ItemSlot;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Game;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;

public class WndKeptItems extends Window {

    private static final int WIDTH = 140;
    private static final int GAP = 2;
    private static final int BTN_H = 16;
    private static final int ICON = 16;
    private static final int ROW_H = ICON + BTN_H + GAP + 3;

    private final Component content;
    private final ScrollPane pane;
    private final float listH;

    public WndKeptItems() {
        super();
        ArrayList<Item> list = KeptItems.stored();

        float y = 0;
        RenderedTextBlock title = PixelScene.renderTextBlock(Messages.get(this, "title"), 9);
        title.hardlight(TITLE_COLOR);
        title.maxWidth(WIDTH);
        title.setPos(0, y);
        add(title);
        y += title.height() + GAP;

        RenderedTextBlock info = PixelScene.renderTextBlock(Messages.get(this, "info"), 6);
        info.maxWidth(WIDTH);
        info.setPos(0, y);
        add(info);
        y += info.height() + GAP + 4;

        float maxList = PixelScene.uiCamera.height - 60 - y - BTN_H - chrome.marginVer();
        listH = Math.max(ROW_H * 2, Math.min(list.size() * ROW_H, maxList));
        resize(WIDTH, (int) Math.ceil(y + listH + GAP + BTN_H));

        pane = new ScrollPane(new Component());
        add(pane);
        pane.setRect(0, y, WIDTH, listH);
        content = pane.content();
        y += listH + GAP;

        float half = (WIDTH - GAP) / 2f;
        RedButton all = new RedButton(Messages.get(this, "all")) {
            @Override
            public void onClick() {
                while (!KeptItems.stored().isEmpty()) claim(0);
                hide();
            }
        };
        all.setRect(0, y, half, BTN_H);
        add(all);

        RedButton later = new RedButton(Messages.get(this, "later")) {
            @Override
            public void onClick() {
                hide();
            }
        };
        later.setRect(half + GAP, y, half, BTN_H);
        add(later);

        refresh();
    }

    private void claim(int index) {
        Item item = KeptItems.take(index);
        if (item == null) return;
        item.keptBound = true;
        if (!item.collect(Dungeon.hero.belongings.backpack)) {
            Dungeon.level.drop(item, Dungeon.hero.pos).sprite.drop();
        }
    }

    private void refresh() {
        ArrayList<Item> list = KeptItems.stored();
        float scrollY = content.camera != null ? content.camera.scroll.y : 0;
        content.clear();
        float cy = 0;
        for (int i = 0; i < list.size(); i++) {
            final int index = i;
            final Item item = list.get(i);
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
            label.setPos(ICON + 2, cy + (ICON - label.height()) / 2f);
            PixelScene.align(label);
            content.add(label);

            RedButton btn = new RedButton(Messages.get(this, "claim"), 7) {
                @Override
                public void onClick() {
                    claim(index);
                    if (KeptItems.stored().isEmpty()) hide(); else refresh();
                }
            };
            btn.setRect(0, cy + ICON, WIDTH, BTN_H);
            content.add(btn);
            cy += ROW_H;
        }
        content.setSize(WIDTH, cy);
        pane.setSize(WIDTH, listH);
        pane.scrollTo(0, scrollY);
    }
}
