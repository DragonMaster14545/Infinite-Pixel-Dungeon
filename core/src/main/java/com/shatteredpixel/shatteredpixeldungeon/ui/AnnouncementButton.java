package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.custom.AnnouncementStore;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.AnnouncementScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.Game;
import com.watabou.utils.ColorMath;

public class AnnouncementButton extends StyledButton {

    private boolean flashing = false;

    public AnnouncementButton(Chrome.Type type, String label) {
        super(type, label);
        AnnouncementStore.check();
    }

    @Override
    public void update() {
        super.update();
        boolean fresh = AnnouncementStore.hasNew();
        if (fresh && !flashing) {
            flashing = true;
            text(Messages.get(AnnouncementButton.class, "new"));
        }
        if (flashing) {
            textColor(ColorMath.interpolate(0xFFFFFF, Window.SHPX_COLOR, 0.5f + (float) Math.sin(Game.timeTotal * 5) / 2f));
        }
    }

    @Override
    public void onClick() {
        if (AnnouncementStore.hasNew()) {
            ShatteredPixelDungeon.scene().addToFront(new WndOptions(
                    Icons.get(Icons.NEWS),
                    Messages.get(AnnouncementButton.class, "popup_title"),
                    Messages.get(AnnouncementButton.class, "popup_desc"),
                    Messages.get(AnnouncementButton.class, "read"),
                    Messages.get(AnnouncementButton.class, "later")
            ) {
                @Override
                protected void onSelect(int index) {
                    if (index == 0) {
                        ShatteredPixelDungeon.switchNoFade(AnnouncementScene.class);
                    }
                }
            });
        } else {
            ShatteredPixelDungeon.switchNoFade(AnnouncementScene.class);
        }
    }
}
