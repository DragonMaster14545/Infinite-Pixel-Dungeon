/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.scenes.HeroSelectScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.watabou.noosa.Image;
import com.watabou.noosa.PointerArea;

import com.shatteredpixel.shatteredpixeldungeon.SeedFinder.SeedfinderLogResult;

import java.util.ArrayList;

public class WndSeedfinderLog extends WndTabbed {

    protected static final int WIDTH_MIN = 120;
    protected static final int WIDTH_MAX = 280;
    protected static final int BTN_SIZE_SMALL = 11;
    protected static final int BTN_HEIGHT = 16;

    protected static final int GAP = 2;
    private final int fontSize = SPDSettings.seedfinderFontSize();

    private RenderedTextBlock text;
    private ScrollPane scroll;
    private int selectedCategory = 0;
    private static final int ITEMS = 0;
    private static final int ROOMS = 1;
    private int selectedIndex = 0;
    private int offset = 0;
    private static final int MAX_VISIBLE_TABS = 6;

    private final SeedfinderLogResult result;

    public WndSeedfinderLog(Image icon, String title, SeedfinderLogResult result) {
        super();

        this.result = result;
        int numTabs = result.main.length;

        int width = WIDTH_MIN;
        int height = PixelScene.uiCamera.height - 20 - tabHeight();
        int bottomPadding = BTN_SIZE_SMALL + 2*GAP;

        PointerArea blocker = new PointerArea(0, 0, PixelScene.uiCamera.width, PixelScene.uiCamera.height);
        //do not go back on screen click
        blocker.camera = PixelScene.uiCamera;
        add(blocker);

        IconTitle titlebar = new IconTitle(icon, title);
        titlebar.setRect(0, 0, width - BTN_SIZE_SMALL, 0);
        add(titlebar);

        IconButton btnClose = new IconButton(Icons.CLOSE.get()) {
            @Override
            protected void onClick() {
                WndSeedfinderLog.this.hide();
            }
        };
        btnClose.setRect(titlebar.right(), (titlebar.height()- BTN_SIZE_SMALL)/2f, BTN_SIZE_SMALL, BTN_SIZE_SMALL);
        add( btnClose );

        RedButton btnItems = new RedButton("Items") {
            @Override
            protected void onClick() {
                selectedCategory = ITEMS;
                updateText();
            }
        };
        btnItems.setRect(0, titlebar.bottom() + GAP, (width-GAP)/2f, BTN_HEIGHT);
        add( btnItems );

        RedButton btnRooms = new RedButton("Rooms") {
            @Override
            protected void onClick() {
                selectedCategory = ROOMS;
                updateText();
            }
        };
        btnRooms.setRect((width+GAP)/2f, titlebar.bottom() + GAP, (width-GAP)/2f, BTN_HEIGHT);
        add( btnRooms );

        text = PixelScene.renderTextBlock( fontSize );
        text.text(result.main[selectedIndex]);
        text.maxWidth( width );
        text.setPos( titlebar.left(), btnItems.bottom() + 2*GAP );

        while (PixelScene.landscape()
                && text.bottom() > (PixelScene.MIN_HEIGHT_L - 10)
                && width < WIDTH_MAX) {
            width += 20;
            titlebar.setRect(0, 0, width - BTN_SIZE_SMALL, 0);
            btnClose.setRect(titlebar.right(), (titlebar.height()- BTN_SIZE_SMALL)/2f, BTN_SIZE_SMALL, BTN_SIZE_SMALL);
            btnItems.setRect(0, titlebar.bottom() + GAP, (width-GAP)/2f, BTN_HEIGHT);
            btnRooms.setRect((width+GAP)/2f, titlebar.bottom() + GAP, (width-GAP)/2f, BTN_HEIGHT);

            text.setPos( titlebar.left(), btnItems.bottom() + 2*GAP );
            text.maxWidth(width);
        }

        scroll = new ScrollPane(text);
        add( scroll );

        bringToFront(titlebar);
        resize( width, height );
        scroll.setRect(0, btnItems.bottom() + 2*GAP,
                width, height - (btnItems.bottom() + 2*GAP) - bottomPadding);

        if (numTabs > MAX_VISIBLE_TABS) {
            RedButton btnLeft = new RedButton("<", 7) {
                @Override
                protected void onClick() {
                    if (offset > 0) {
                        offset -= 1;
                    }
                    updateTabs();
                    updateText();
                }
            };
            btnLeft.setRect(0, scroll.bottom() + 2*GAP, BTN_SIZE_SMALL, BTN_SIZE_SMALL);
            add(btnLeft);

            RedButton btnRight = new RedButton(">", 7) {
                @Override
                protected void onClick() {
                    if (offset + 1 <= numTabs - MAX_VISIBLE_TABS) {
                        offset += 1;
                    }
                    updateTabs();
                    updateText();
                }
            };
            btnRight.setRect(width-BTN_SIZE_SMALL, scroll.bottom() + 2*GAP, BTN_SIZE_SMALL, BTN_SIZE_SMALL);
            add(btnRight);

            for (int i = 0; i < MAX_VISIBLE_TABS; i++) {
                String tabLabel = ((i==0 && offset > 0)? "..." : "") + numToTabTitle(i) +
                        ((i == MAX_VISIBLE_TABS -1 && (i+offset) < numTabs-1) ? "...":"");
                final int finalI = i;
                add(new LabeledTab(tabLabel) {
                    @Override
                    protected void select(boolean value) {
                        super.select(value);
                        if (value) {
                            selectedIndex = finalI;
                        }
                        updateText();
                    }
                });
            }
        } else {
            for (int i = 0; i < this.result.main.length; i++) {
                final int finalI = i;
                add(new LabeledTab(numToTabTitle(i)) {
                    @Override
                    protected void select(boolean value) {
                        super.select(value);
                        if (value) {
                            selectedIndex = finalI;
                        }
                        updateText();
                    }
                });
            }
        }

        RedButton btnUse = new RedButton("Use Seed", 7) {
            @Override
            protected void onClick() {
                ShatteredPixelDungeon.switchNoFade( HeroSelectScene.class );
            }
        };
        btnUse.setRect(BTN_SIZE_SMALL + GAP, scroll.bottom() + 2*GAP,
                width - 2*(BTN_SIZE_SMALL+GAP), BTN_SIZE_SMALL);
        add(btnUse);

        layoutTabs();
        select(0);
    }

    private void updateText() {
        if (selectedCategory == ITEMS) {
            text.text(result.main[selectedIndex+offset]);
        } else if (selectedCategory == ROOMS) {
            text.text(result.rooms[selectedIndex+offset]);
        }
        scroll.scrollTo(0, 0);
    }

    private void updateTabs() {
        if (!tabs.isEmpty()) {
            for (Tab tab : tabs) {
                remove(tab);
            }
            tabs = new ArrayList<>();
        }
        int numTabs = result.main.length;
        for (int i = 0; i < MAX_VISIBLE_TABS; i++) {
            String tabLabel = ((i==0 && offset > 0)? "..." : "") + numToTabTitle(i) +
                    ((i == MAX_VISIBLE_TABS -1 && (i+offset) < numTabs-1) ? "...":"");
            final int finalI = i;
            add(new LabeledTab(tabLabel) {
                @Override
                protected void select(boolean value) {
                    super.select(value);
                    if (value) {
                        selectedIndex = finalI;
                    }
                    updateText();
                }
            });
        }
        select(selectedIndex);
        layoutTabs();
    }

    private String numToTabTitle(int num) {
        if (num + offset == 0) {
            return "*";
        } else {
            return Integer.toString(num + offset);
        }
    }
}