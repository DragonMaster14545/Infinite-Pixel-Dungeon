package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.items.VaultStore;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.VaultLink;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.ui.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class WndVaultHistory extends Window {

    private static final int WIDTH = 124;
    private static final int BTN_H = 16;
    private static final int GAP = 2;
    private static final int SIDE = 3;
    private static final int SCROLLBAR = 4;
    private static final int ENTRY_GAP = 4;

    private static int filter = 0;

    private final Runnable back;

    public WndVaultHistory(Runnable back) {
       super();
       this.back = back;

       int height = Math.min(150, (int) (PixelScene.uiCamera.height - chrome.marginVer() - 8));

       RenderedTextBlock title = PixelScene.renderTextBlock(
             Messages.get(WndVaultHistory.class, "title",
                   Messages.get(VaultLink.class, "mode_" + VaultStore.mode().name().toLowerCase())), 9);
       title.hardlight(TITLE_COLOR);
       title.maxWidth(WIDTH);
       title.setPos(0, 0);
       add(title);

       resize(WIDTH, height);

       float top = title.height() + GAP;

       RedButton filterBtn = new RedButton(Messages.get(WndVaultHistory.class, "filter_" + filter), 7) {
          @Override
          public void onClick() {
             filter = (filter + 1) % 3;
             hide();
             GameScene.show(new WndVaultHistory(WndVaultHistory.this.back));
          }
       };
       filterBtn.setRect(0, top, WIDTH, BTN_H);
       add(filterBtn);
       top += BTN_H + GAP;

       float bottom = height - BTN_H;

       ScrollPane pane = new ScrollPane(new Component());
       add(pane);
       pane.setRect(0, top, WIDTH, bottom - top - GAP);
       Component content = pane.content();

       float textWidth = WIDTH - SIDE * 2 - SCROLLBAR;

       SimpleDateFormat fmt = new SimpleDateFormat("MM-dd HH:mm", Locale.US);
       ArrayList<VaultStore.Entry> all = VaultStore.history();
       float y = 0;
       int shown = 0;
       for (int i = all.size() - 1; i >= 0; i--) {
          VaultStore.Entry e = all.get(i);
          boolean transfer = e.type.startsWith("transfer");
          if (filter == 1 && transfer) continue;
          if (filter == 2 && !transfer) continue;

          RenderedTextBlock stamp = PixelScene.renderTextBlock(
                fmt.format(new Date(e.time)) + "  " + Messages.get(WndVaultHistory.class, e.type), 6);
          stamp.hardlight(0x888888);
          stamp.maxWidth((int) textWidth);
          stamp.setPos(SIDE, y);
          content.add(stamp);
          y = stamp.bottom() + 1;

          RenderedTextBlock row = PixelScene.renderTextBlock(e.text, 6);
          row.maxWidth((int) textWidth);
          row.setPos(SIDE, y);
          content.add(row);
          y = row.bottom() + ENTRY_GAP;
          shown++;
       }
       if (shown == 0) {
          RenderedTextBlock none = PixelScene.renderTextBlock(Messages.get(WndVaultHistory.class, "empty"), 7);
          none.maxWidth((int) textWidth);
          none.setPos(SIDE, 0);
          content.add(none);
          y = none.bottom() + ENTRY_GAP;
       }
       content.setSize(WIDTH - SCROLLBAR, y);
       pane.setSize(pane.width(), pane.height());
       pane.scrollTo(0, 0);

       RedButton close = new RedButton(Messages.get(WndVaultHistory.class, "close")) {
          @Override
          public void onClick() {
             hide();
             if (WndVaultHistory.this.back != null) WndVaultHistory.this.back.run();
          }
       };
       close.setRect(0, bottom, WIDTH, BTN_H);
       add(close);
    }

    @Override
    public void onBackPressed() {
       hide();
       if (back != null) back.run();
    }
}
