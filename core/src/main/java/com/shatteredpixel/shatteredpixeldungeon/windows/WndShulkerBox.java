package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.ShulkerBox;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.InventorySlot;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Game;

import java.util.ArrayList;

public class WndShulkerBox extends Window {

    private static final int COLS = 5;
    private static final int ROWS = 5;
    private static final int SLOT_MARGIN = 1;
    private static final int TITLE_HEIGHT = 14;

    private final ShulkerBox box;

    public WndShulkerBox(ShulkerBox box) {
        super();
        this.box = box;

        if (WndBag.INSTANCE != null) {
            WndBag.INSTANCE.hide();
        }
        WndBag.INSTANCE = this;

        int slotWidth = 28;
        int slotHeight = 28;

        int windowWidth = slotWidth * COLS + SLOT_MARGIN * (COLS - 1);
        while (slotWidth >= 20 && (windowWidth + chrome.marginHor()) > PixelScene.uiCamera.width) {
            slotWidth--;
            windowWidth -= COLS;
        }
        int windowHeight = TITLE_HEIGHT + slotHeight * ROWS + SLOT_MARGIN * (ROWS - 1);
        while (slotHeight >= 20 && (windowHeight + 20 + chrome.marginTop()) > PixelScene.uiCamera.height) {
            slotHeight--;
            windowHeight -= ROWS;
        }

        RenderedTextBlock title = PixelScene.renderTextBlock(Messages.titleCase(box.name()), 8);
        title.hardlight(TITLE_COLOR);
        title.maxWidth(windowWidth - 2);
        title.setPos(1, (TITLE_HEIGHT - title.height()) / 2f - 1);
        PixelScene.align(title);
        add(title);

        ArrayList<Item> items = box.contents();
        for (int i = 0; i < ShulkerBox.CAPACITY; i++) {
            final Item item = i < items.size() ? items.get(i) : null;

            int x = (i % COLS) * (slotWidth + SLOT_MARGIN);
            int y = TITLE_HEIGHT + (i / COLS) * (slotHeight + SLOT_MARGIN);

            InventorySlot slot = new InventorySlot(item) {
                @Override
                public void onClick() {
                    takeOut(item);
                }

                @Override
                protected void onRightClick() {
                    takeOut(item);
                }

                @Override
                protected boolean onLongClick() {
                    if (item != null) {
                        Game.scene().addToFront(new WndInfoItem(item));
                        return true;
                    }
                    return false;
                }
            };
            slot.setRect(x, y, slotWidth, slotHeight);
            add(slot);

            if (item == null) slot.enable(false);
        }

        resize(windowWidth, windowHeight);
    }

    private void takeOut(Item item) {
        if (item == null || Dungeon.hero == null || !Dungeon.hero.isAlive()) return;

        hide();
        box.takeOut(Dungeon.hero, item);

        if (!box.isEmpty()) {
            GameScene.show(new WndShulkerBox(box));
        }
    }

    @Override
    public void hide() {
        super.hide();
        if (WndBag.INSTANCE == this) {
            WndBag.INSTANCE = null;
        }
    }
}