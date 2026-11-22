package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.ChromeColors;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.ui.Component;

public class WndChromeColors extends Window {

    private static final int WIDTH = 120;
    private static final int GAP = 2;
    private static final int BTN_H = 16;

    private final Runnable onChange;

    public WndChromeColors(Runnable onChange) {
        super();
        this.onChange = onChange;

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
        y += info.height() + GAP * 2;

        int count = ChromeColors.COLORS.length;
        float full = count * (BTN_H + GAP);
        float maxList = PixelScene.uiCamera.height - 60 - y - chrome.marginVer();
        float listH = Math.max(BTN_H * 3, Math.min(full, maxList));
        resize(WIDTH, (int) Math.ceil(y + listH));

        ScrollPane pane = new ScrollPane(new Component());
        add(pane);
        pane.setRect(0, y, WIDTH, listH);
        Component content = pane.content();

        int current = ChromeColors.index();
        float cy = 0;
        for (int i = 0; i < count; i++) {
            final int index = i;
            String label = Messages.get(ChromeColors.class, "c" + i);
            if (i == current) label = "> " + label + " <";
            RedButton btn = new RedButton(label, 7) {
                @Override
                public void onClick() {
                    ChromeColors.index(index);
                    hide();
                    if (WndChromeColors.this.onChange != null) WndChromeColors.this.onChange.run();
                    ShatteredPixelDungeon.scene().addToFront(new WndChromeColors(WndChromeColors.this.onChange));
                }
            };
            btn.textColor(ChromeColors.labelColor(i));
            btn.setRect(0, cy, WIDTH, BTN_H);
            content.add(btn);
            cy += BTN_H + GAP;
        }
        content.setSize(WIDTH, cy);
        pane.setSize(WIDTH, listH);
    }
}
