package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfUnstable;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;

public class ItemIconTitle extends IconTitle {
    private IconButton aimButton = null;
    private IconButton renameButton = null;

    public ItemIconTitle(Item item, WndUseItem window ) {
        super(item);

        if (Dungeon.hero.isAlive() && Dungeon.hero.belongings.contains(item)) {
            if (!(item instanceof WandOfUnstable)) {
                renameButton = new IconButton(Icons.get( item.customName.equals("") ? Icons.SCROLL_GREY : Icons.SCROLL_COLOR)) {
                    @Override
                    protected void onClick() {
                        window.hide();
                        if (window.owner != null && window.owner.parent != null) {
                            window.owner.hide();
                        }
                        if (Dungeon.hero.isAlive() && Dungeon.hero.belongings.contains(item)) {
                            item.execute(Dungeon.hero, Item.AC_RENAME);
                        }
                        Item.updateQuickslot();
                    }
                };
                add(renameButton);
            }

            if (item.needsAim()) {
                aimButton = new IconButton(new BuffIcon(BuffIndicator.MARK, true)) {
                    @Override
                    protected void onClick() {
                        window.hide();
                        if (window.owner != null && window.owner.parent != null) {
                            window.owner.hide();
                        }
                        if (Dungeon.hero.isAlive() && Dungeon.hero.belongings.contains(item)){
                            item.execute( Dungeon.hero, Item.AC_AIM );
                        }
                        Item.updateQuickslot();
                    }
                };
                add(aimButton);
            }


        }
    }//vjm-dww-rxl
    private static class Notes extends Window {
        private static final int WIDTH_MIN=120, WIDTH_MAX=220;
        ScrollPane scrollPane;
        Notes(String message) {
            int width = WIDTH_MIN;

            RenderedTextBlock text = PixelScene.renderTextBlock(6);
            text.text(message, width);
            while (PixelScene.landscape()
                    && text.bottom() > (PixelScene.MIN_HEIGHT_L - 10)
                    && width < WIDTH_MAX) {
                text.maxWidth(width += 20);
            }

            int height = (int)text.bottom();
            int maxHeight = (int)(PixelScene.uiCamera.height * 0.9);
            boolean needScrollPane = height > maxHeight;
            if(needScrollPane) height = maxHeight;
            resize((int)text.width(), height);
            if(needScrollPane) {
                add(scrollPane = new ScrollPane(new Component()) {
                    {
                        content.add(text);
                    }
                    // vertical margin is required to prevent text from getting cut off.
                    final float VERTICAL_MARGIN = 1;
                    @Override
                    protected void layout() {
                        text.setPos(0, VERTICAL_MARGIN);
                        // also set the width of the scroll pane
                        content.setSize(width = text.right(), text.bottom()+VERTICAL_MARGIN);
                        width += 2; // padding on the right to cause the controller to be flush against the window.
                        super.layout();
                    }
                });
                scrollPane.setSize(width, height);
            }
            else {
                add(text);
            }
        }

        @Override // this should be removed for pre-v1.2 builds, this method was added in v1.2
        public void offset(int xOffset, int yOffset) {
            super.offset(xOffset, yOffset);
            // this prevents issues in the full ui mode.
            if(scrollPane != null) scrollPane.setSize(scrollPane.width(), scrollPane.height());
        }
    }

    @Override
    protected void layout() {
        int shift = 16;
        if (renameButton != null) {
            renameButton.setRect(x + width - shift, y, 16, 16);
            shift += 16 + 2;
            PixelScene.align(renameButton);
        }
        if (aimButton != null) {
            aimButton.setRect(x + width - shift, y, 16, 16);
            shift += 16 + 2;
            PixelScene.align(aimButton);
        }


        imIcon.x = x + (Math.max(0, 8 - imIcon.width()/2));
        imIcon.y = y + (Math.max(0, 8 - imIcon.height()/2));
        PixelScene.align(imIcon);

        int imWidth = (int)Math.max(imIcon.width(), 16);
        int imHeight = (int)Math.max(imIcon.height(), 16);

        tfLabel.maxWidth((int)(width - (imWidth + 2) - shift + 16));
        tfLabel.setPos(x + imWidth + 2,
                imHeight > tfLabel.height() ? y +(imHeight - tfLabel.height()) / 2 : y);
        PixelScene.align(tfLabel);

        if (health.visible) {
            health.setRect( tfLabel.left(), tfLabel.bottom(), tfLabel.maxWidth(), 0 );
            height = Math.max( imHeight, health.bottom() );
        } else {
            height = Math.max( imHeight, tfLabel.height() );
        }
    }
}