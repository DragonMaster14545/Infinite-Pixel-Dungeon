/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2023 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Clipboard;
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.SeedFinder;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.Archs;
import com.shatteredpixel.shatteredpixeldungeon.ui.ExitButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed;
import com.shatteredpixel.shatteredpixeldungeon.windows.IconTitle;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndSeedfinderLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndSeedfinderMenu;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTextInput;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTitledMessage;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Image;

public class SeedFindScene extends PixelScene {

    @Override
    public void create() {
        super.create();

        int w = Camera.main.width;
        int h = Camera.main.height;

        //RectF insets = getCommonInsets();

        Archs archs = new Archs();
        archs.setSize( w, h );
        add( archs );

        IconTitle title = new IconTitle();
        Image titleIcon = Icons.SEED.get();
        titleIcon.hardlight(1f, 1.5f, 0.67f);
        title.icon(titleIcon);
        title.label(Messages.get(this, "title"));
        title.setSize(200, 0);
        add(title);

        ExitButton btnExit = new ExitButton() {
            @Override
            protected void onClick() {
                ShatteredPixelDungeon.switchNoFade( HeroSelectScene.class );
            }
        };
        btnExit.setPos( Camera.main.width - btnExit.width(), 0 );
        add( btnExit );

        final Chrome.Type GREY_TR = Chrome.Type.GREY_BUTTON_TR;

        StyledButton btnSeedfinder = new StyledButton(GREY_TR, Messages.get(this, "find_seed_button")) {
            @Override
            protected void onClick() {
                SeedFindScene.this.addToFront(new WndTextInput(
                        Messages.get(SeedFindScene.class, "seedfinder_title"),
                        Messages.get(SeedFindScene.class, "seedfinder_info"),
                        SPDSettings.seedfinderPrompt(), 1000, true,
                        Messages.get(SeedFindScene.class, "seedfinder_yes"),
                        Messages.get(SeedFindScene.class, "seedfinder_no")) {
                    @Override
                    public void onSelect(boolean positive, String itemsPrompt) {
                        if (positive) {
                            SPDSettings.seedfinderPrompt(itemsPrompt);

                            final Thread[] searchThread = new Thread[1];
                            final WndOptions[] progressWnd = new WndOptions[1];

                            //run in new thread
                            searchThread[0] = new Thread(() -> {
                                final SeedFinder.SeedLog foundSeed = SeedFinder.findSeed();

                                //process results in rendering thread
                                Gdx.app.postRunnable(() -> {
                                    //search cancelled
                                    if (progressWnd[0].parent == null) return;

                                    progressWnd[0].hide();

                                    if (foundSeed == null) return;

                                    //copy seed to clipboard on success
                                    Clipboard clipboard = Gdx.app.getClipboard();
                                    clipboard.setContents(foundSeed.seed);

                                    //show log
                                    SeedFinder.SeedfinderLogResult result = foundSeed.toLogResult();

                                    SeedFindScene.this.addToFront( new WndSeedfinderLog(
                                            Icons.get(Icons.BACKPACK),
                                            Messages.get(SeedFindScene.class, "result_title"),
                                            result) );
                                });
                            });

                            progressWnd[0] = new WndOptions( Icons.get(Icons.MAGNIFY),
                                    Messages.get(SeedFindScene.class, "searching_title"),
                                    Messages.get(SeedFindScene.class, "searching_text"),
                                    Messages.get(SeedFindScene.class, "searching_cancel") ) {
                                @Override
                                protected void onSelect(int index) {
                                    if (index == 0) {
                                        if (searchThread[0] != null && searchThread[0].isAlive()) {
                                            searchThread[0].interrupt();
                                        }
                                        hide();
                                    }
                                }

                                @Override
                                public void onBackPressed() {
                                    // do nothing to prevent accidental cancellation
                                }
                            };

                            SeedFindScene.this.addToFront( progressWnd[0] );
                            searchThread[0].start();
                        } else {
                            SPDSettings.seedfinderPrompt("");
                        }
                    }
                });
            }
        };
        btnSeedfinder.icon(Icons.MAGNIFY.get());
        add(btnSeedfinder);

        StyledButton btnScout = new StyledButton(GREY_TR, Messages.get(this, "scout_seed_button")) {
            @Override
            protected void onClick() {
                SeedFindScene.this.addToFront(new WndTextInput(
                        Messages.get(SeedFindScene.class, "scout_title"),
                        Messages.get(SeedFindScene.class, "scout_info"),
                        SPDSettings.customSeed(), 20, false,
                        Messages.get(SeedFindScene.class, "scout_yes"),
                        Messages.get(SeedFindScene.class, "scout_no")) {
                    @Override
                    public void onSelect(boolean positive, String text) {
                        if (positive && text != null && !text.isEmpty()) {
                            text = DungeonSeed.formatText(text);

                            SeedFinder.SeedLog result = SeedFinder.scoutSeed(text);

                            SeedFindScene.this.addToFront(
                                    new WndSeedfinderLog(
                                            Icons.get(Icons.BACKPACK),
                                            Messages.get(SeedFindScene.class, "result_title"),
                                            result.toLogResult()) );
                        } else {
                            SPDSettings.customSeed("");
                        }
                    }
                });
            }
        };
        btnScout.icon(Icons.JOURNAL.get());
        add(btnScout);

        StyledButton btnDaily = new StyledButton(GREY_TR, Messages.get(this, "scout_daily_button")) {
            @Override
            protected void onClick() {
                SeedFindScene.this.addToFront(new WndOptions(Icons.CALENDAR.get(),
                        Messages.get(SeedFindScene.class, "scout_daily_title"),
                        Messages.get(SeedFindScene.class, "scout_daily_body"),
                        Messages.get(SeedFindScene.class, "scout_daily_yes"),
                        Messages.get(SeedFindScene.class, "scout_daily_no") ) {
                    @Override
                    protected void onSelect(int index) {
                        if (index == 0) {
                            SeedFinder.SeedLog result = SeedFinder.scoutDaily();

                            SeedFindScene.this.addToFront( new WndSeedfinderLog(
                                    Icons.get(Icons.BACKPACK),
                                    Messages.get(SeedFindScene.class, "result_title"),
                                    result.toLogResult()) );
                        }
                    }
                });
            }
        };
        btnDaily.icon(Icons.CALENDAR.get());
        add(btnDaily);

        StyledButton btnWeekly = new StyledButton(GREY_TR, Messages.get(this, "scout_weekly_button")) {
            @Override
            protected void onClick() {
                SeedFindScene.this.addToFront(new WndOptions(Icons.CALENDAR.get(),
                        Messages.get(SeedFindScene.class, "scout_weekly_title"),
                        Messages.get(SeedFindScene.class, "scout_weekly_body"),
                        Messages.get(SeedFindScene.class, "scout_weekly_yes"),
                        Messages.get(SeedFindScene.class, "scout_weekly_no") ) {
                    @Override
                    protected void onSelect(int index) {
                        if (index == 0) {
                            SeedFinder.SeedLog result = SeedFinder.scoutWeekly();

                            SeedFindScene.this.addToFront( new WndSeedfinderLog(
                                    Icons.get(Icons.BACKPACK),
                                    Messages.get(SeedFindScene.class, "result_title"),
                                    result.toLogResult()) );
                        }
                    }
                });
            }
        };
        btnWeekly.icon(Icons.CALENDAR.get());
        add(btnWeekly);

        StyledButton btnInfo = new StyledButton(GREY_TR, Messages.get(this, "info_button")) {
            @Override
            protected void onClick() {
                SeedFindScene.this.addToFront( new WndTitledMessage( Icons.SEED.get(),
                        Messages.get(SeedFindScene.class, "info_title"),
                        Messages.get(SeedFindScene.class, "info_body") )
                );
            }
        };
        btnInfo.icon(Icons.SEED.get());
        add(btnInfo);

        StyledButton btnOptions = new StyledButton(GREY_TR, Messages.get(SeedFindScene.class, "options_button")) {
            @Override
            protected void onClick() {
                SeedFindScene.this.addToFront(new WndSeedfinderMenu());
            }
        };
        btnOptions.icon(Icons.PREFS.get());
        add(btnOptions);

        final int BTN_HEIGHT = 20;
        final int GAP = 4;
        float titleTop = (h - title.height() - (BTN_HEIGHT+GAP) * 5)/2;

        title.setPos( (w - title.reqWidth()) / 2f, titleTop);
        align(title);

        float buttonAreaWidth = landscape() ? PixelScene.MIN_WIDTH_L-6 : PixelScene.MIN_WIDTH_P-2;
        float btnAreaLeft = (w - buttonAreaWidth) / 2f;

        btnSeedfinder.setRect(btnAreaLeft, title.bottom() + GAP, buttonAreaWidth, BTN_HEIGHT);
        btnScout.setRect(btnAreaLeft, btnSeedfinder.bottom() + GAP, buttonAreaWidth, BTN_HEIGHT);
        btnDaily.setRect(btnAreaLeft, btnScout.bottom() + GAP, buttonAreaWidth, BTN_HEIGHT);
        btnWeekly.setRect(btnAreaLeft, btnDaily.bottom() + GAP, buttonAreaWidth, BTN_HEIGHT);
        btnInfo.setRect(btnAreaLeft, btnWeekly.bottom() + GAP, buttonAreaWidth, BTN_HEIGHT);
        btnOptions.setRect(btnAreaLeft, btnInfo.bottom() + GAP, buttonAreaWidth, BTN_HEIGHT);

        addToBack( archs );
    }

    @Override
    protected void onBackPressed() {
        ShatteredPixelDungeon.switchScene(HeroSelectScene.class);
    }

}