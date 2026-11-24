package com.shatteredpixel.shatteredpixeldungeon.scenes;

import com.badlogic.gdx.Gdx;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.custom.AnnouncementStore;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.Archs;
import com.shatteredpixel.shatteredpixeldungeon.ui.ExitButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndMessage;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ui.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AnnouncementScene extends PixelScene {

    private int seen;

    @Override
    public void create() {
        super.create();
        uiCamera.visible = false;

        int w = Camera.main.width;
        int h = Camera.main.height;
        int fullWidth = PixelScene.landscape() ? 202 : 120;
        int left = (w - fullWidth) / 2;

        Archs archs = new Archs();
        archs.setSize(w, h);
        add(archs);

        ExitButton btnExit = new ExitButton();
        btnExit.setPos(w - btnExit.width(), 0);
        add(btnExit);

        RenderedTextBlock title = PixelScene.renderTextBlock(Messages.get(this, "title"), 12);
        title.hardlight(0xFFFF44);
        title.setPos((w - title.width()) / 2f, 4);
        align(title);
        add(title);

        String status;
        if (AnnouncementStore.state == AnnouncementStore.LOADING) {
            status = Messages.get(this, "loading");
        } else if (AnnouncementStore.url().isEmpty()) {
            status = Messages.get(this, "no_url");
        } else if (AnnouncementStore.time() == 0) {
            status = Messages.get(this, "never");
        } else {
            status = Messages.get(this, AnnouncementStore.state == AnnouncementStore.FAILED ? "offline" : "updated",
                    new Date(AnnouncementStore.time()).toString());
        }
        RenderedTextBlock info = PixelScene.renderTextBlock(status, 6);
        info.maxWidth(fullWidth);
        info.setPos(left, title.bottom() + 2);
        add(info);

        float btnH = 18;
        float bottom = h - btnH - 2;
        float btnW = (fullWidth - 2) / 2f;

        RedButton refresh = new RedButton(Messages.get(this, "refresh"), 7) {
            @Override
            public void onClick() {
                AnnouncementStore.refresh();
                ShatteredPixelDungeon.seamlessResetScene();
            }
        };
        refresh.setRect(left, bottom, btnW, btnH);
        add(refresh);

        RedButton link = new RedButton(Messages.get(this, "open_link"), 7) {
            @Override
            public void onClick() {
                String u = AnnouncementStore.url();
                if (u.isEmpty()) {
                    ShatteredPixelDungeon.scene().addToFront(new WndMessage(Messages.get(AnnouncementScene.class, "no_url")));
                } else {
                    try {
                        Gdx.net.openURI("https://infpixeldungeon.blogspot.com/");
                    } catch (Throwable t) {
                        ShatteredPixelDungeon.scene().addToFront(new WndMessage(Messages.get(AnnouncementScene.class, "open_failed")));
                    }
                }
            }
        };
        link.setRect(left + btnW + 2, bottom, btnW, btnH);
        add(link);

        float top = info.bottom() + 4;
        ScrollPane pane = new ScrollPane(new Component());
        add(pane);
        pane.setRect(left, top, fullWidth, bottom - top - 2);

        Component content = pane.content();
        ArrayList<AnnouncementStore.Entry> entries = AnnouncementStore.entries();
        float y = 0;
        if (entries.isEmpty()) {
            RenderedTextBlock none = PixelScene.renderTextBlock(Messages.get(this, "empty"), 7);
            none.maxWidth(fullWidth);
            none.setPos(0, 0);
            content.add(none);
            y = none.height();
        }
        for (AnnouncementStore.Entry e : entries) {
            RenderedTextBlock t = PixelScene.renderTextBlock(e.title, 9);
            t.hardlight(0xFFFF44);
            t.maxWidth(fullWidth);
            t.setPos(0, y);
            content.add(t);
            y += t.height() + 2;
            if (e.time > 0) {
                @SuppressWarnings("SimpleDateFormat")
                RenderedTextBlock d = PixelScene.renderTextBlock(new SimpleDateFormat("yyyy-MM-dd HH:mma").format(new Date(e.time)), 6);
                d.hardlight(0x888888);
                d.setPos(0, y);
                content.add(d);
                y += d.height() + 2;
            }
            if (!e.body.isEmpty()) {
                RenderedTextBlock b = PixelScene.renderTextBlock(e.body, 6);
                b.maxWidth(fullWidth);
                b.setPos(0, y);
                content.add(b);
                y += b.height();
            }
            y += 8;
        }
        content.setSize(fullWidth, y);
        pane.setSize(pane.width(), pane.height());

        seen = AnnouncementStore.version;
        AnnouncementStore.markSeen();
        AnnouncementStore.check();
    }

    @Override
    public void update() {
        if (seen != AnnouncementStore.version) {
            seen = AnnouncementStore.version;
            ShatteredPixelDungeon.seamlessResetScene();
            return;
        }
        super.update();
    }

    @Override
    protected void onBackPressed() {
        ShatteredPixelDungeon.switchNoFade(TitleScene.class);
    }
}
