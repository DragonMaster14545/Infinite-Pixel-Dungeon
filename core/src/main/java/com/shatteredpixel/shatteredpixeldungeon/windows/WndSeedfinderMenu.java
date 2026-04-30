package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox;
import com.shatteredpixel.shatteredpixeldungeon.ui.OptionSlider;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.ColorBlock;

import java.util.stream.IntStream;

public class WndSeedfinderMenu extends Window {

    private static final int WIDTH_P	    = 122;
    private static final int WIDTH_L	    = 223;

    protected static final int MARGIN 		= 2;

    private static final int SLIDER_HEIGHT	= 24;
    private static final int BTN_HEIGHT	    = 20;
    private static final int INFO_SIZE      = 14;
    private static final float GAP          = 2;

    IconTitle title;

    OptionSlider slideFloors;
    IconButton infoFloors;
    OptionSlider slideFontSize;
    IconButton infoFontSize;
    RedButton btnMode;
    IconButton infoMode;
    RedButton btnLoggingSettings;
    RedButton btnChallenges;

    public WndSeedfinderMenu() {
        super();

        title = new IconTitle(Icons.PREFS.get(), Messages.get(this, "title"));
        add(title);

        slideFloors = new OptionSlider(Messages.get(this, "floors_title", SPDSettings.seedfinderFloors()),
                "1", "26", 1, 26) {
            @Override
            protected void onChange() {
                SPDSettings.seedfinderFloors(getSelectedValue());

                ShatteredPixelDungeon.seamlessResetScene();
            }
        };
        slideFloors.setSelectedValue(SPDSettings.seedfinderFloors());
        add(slideFloors);

        infoFloors = new IconButton(Icons.INFO.get()) {
            @Override
            protected void onClick() {
                ShatteredPixelDungeon.scene().addToFront(
                        new WndMessage(Messages.get(WndSeedfinderMenu.class, "floors_info")) );
            }
        };
        add(infoFloors);

        slideFontSize = new OptionSlider(Messages.get(this, "fontsize_title", SPDSettings.seedfinderFontSize()),
                "small", "large", 3, 9) {
            @Override
            protected void onChange() {
                SPDSettings.seedfinderFontSize(getSelectedValue());

                ShatteredPixelDungeon.seamlessResetScene();
            }
        };
        slideFontSize.setSelectedValue(SPDSettings.seedfinderFontSize());
        add(slideFontSize);

        infoFontSize = new IconButton(Icons.INFO.get()) {
            @Override
            protected void onClick() {
                ShatteredPixelDungeon.scene().addToFront(
                        new WndMessage(Messages.get(WndSeedfinderMenu.class, "fontsize_info")) );
            }
        };
        add(infoFontSize);

        String modeBtnDescKey = SPDSettings.seedfinderConditionANY() ? "mode_any" : "mode_all";
        btnMode = new RedButton(Messages.get(this, modeBtnDescKey)) {
            @Override
            protected void onClick() {
                SPDSettings.seedfinderConditionANY(!SPDSettings.seedfinderConditionANY());

                ShatteredPixelDungeon.seamlessResetScene();
            }
        };
        add(btnMode);

        infoMode = new IconButton(Icons.INFO.get()) {
            @Override
            protected void onClick() {
                ShatteredPixelDungeon.scene().addToFront(
                        new WndMessage(Messages.get(WndSeedfinderMenu.class, "mode_info")) );
            }
        };
        add(infoMode);

        btnLoggingSettings = new RedButton(Messages.get(this, "logoptions_button"), 9) {
            @Override
            protected void onClick() {
                ShatteredPixelDungeon.scene().addToFront(new WndLoggingOpts());
            }
        };
        add(btnLoggingSettings);

        btnChallenges = new RedButton(Messages.get(this, "challenges")) {
            @Override
            protected void onClick() {
                ShatteredPixelDungeon.scene().addToFront(new WndChallenges(SPDSettings.challenges(), true) {
                    public void onBackPressed() {
                        super.onBackPressed();

                        ShatteredPixelDungeon.seamlessResetScene();
                    }
                });
            }
        };
        btnChallenges.textColor(IntStream.range(0, SPDSettings.challenges().length).anyMatch(i -> SPDSettings.challenges()[i]) ? TITLE_COLOR : WHITE);
        add(btnChallenges);

        layout();
    }

    private void layout() {
        int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;

        title.setSize(width, 0);
        title.setPos((width - title.reqWidth())/2, 0);

        slideFloors.setRect(0, title.bottom() + GAP, width - INFO_SIZE - GAP, SLIDER_HEIGHT);
        infoFloors.setRect(slideFloors.right() + GAP, slideFloors.centerY() - INFO_SIZE/2f, INFO_SIZE, INFO_SIZE);

        slideFontSize.setRect(0, slideFloors.bottom() + GAP, width - INFO_SIZE - GAP, SLIDER_HEIGHT);
        infoFontSize.setRect(slideFontSize.right() + GAP, slideFontSize.centerY() - INFO_SIZE/2f, INFO_SIZE, INFO_SIZE);

        btnMode.setRect(0, slideFontSize.bottom() + GAP, width - INFO_SIZE - GAP, BTN_HEIGHT);
        infoMode.setRect(btnMode.right() + GAP, btnMode.centerY() - INFO_SIZE/2f, INFO_SIZE, INFO_SIZE);

        btnLoggingSettings.setRect(0, btnMode.bottom() + GAP, width, BTN_HEIGHT);
        btnChallenges.setRect(0, btnLoggingSettings.bottom() + GAP, width, BTN_HEIGHT);

        resize(width, (int) btnChallenges.bottom());
    }

    public static class WndLoggingOpts extends Window {
        private static final int TTL_HEIGHT = 16;
        private static final int BTN_HEIGHT = 16;

        RenderedTextBlock title;

        CheckBox chkTrinkets;
        CheckBox chkEquipment;
        CheckBox chkScrolls;
        CheckBox chkPotions;
        CheckBox chkRings;
        CheckBox chkWands;
        CheckBox chkArtifacts;
        CheckBox chkMisc;

        ColorBlock sep3;

        CheckBox chkRooms;
        CheckBox chkBlacklist;
        CheckBox chkShops;
        IconButton infoRooms;
        IconButton infoBlacklist;
        IconButton infoShops;

        public WndLoggingOpts() {
            title = PixelScene.renderTextBlock(Messages.get(this, "title"), 9);
            title.hardlight( TITLE_COLOR );
            add(title);

            chkTrinkets = new CheckBox(Messages.get(this, "trinkets")) {
                @Override
                protected void onClick() {
                    super.onClick();
                    SPDSettings.logTrinkets(checked());
                }
            };
            chkTrinkets.checked(SPDSettings.logTrinkets());
            add(chkTrinkets);

            chkEquipment = new CheckBox(Messages.get(this, "equipment")) {
                @Override
                protected void onClick() {
                    super.onClick();
                    SPDSettings.logEquipment(checked());
                }
            };
            chkEquipment.checked(SPDSettings.logEquipment());
            add(chkEquipment);

            chkScrolls = new CheckBox(Messages.get(this, "scrolls")) {
                @Override
                protected void onClick() {
                    super.onClick();
                    SPDSettings.logScrolls(checked());
                }
            };
            chkScrolls.checked(SPDSettings.logScrolls());
            add(chkScrolls);

            chkPotions = new CheckBox(Messages.get(this, "potions")) {
                @Override
                protected void onClick() {
                    super.onClick();
                    SPDSettings.logPotions(checked());
                }
            };
            chkPotions.checked(SPDSettings.logPotions());
            add(chkPotions);

            chkRings = new CheckBox(Messages.get(this, "rings")) {
                @Override
                protected void onClick() {
                    super.onClick();
                    SPDSettings.logRings(checked());
                }
            };
            chkRings.checked(SPDSettings.logRings());
            add(chkRings);

            chkWands = new CheckBox(Messages.get(this, "wands")) {
                @Override
                protected void onClick() {
                    super.onClick();
                    SPDSettings.logWands(checked());
                }
            };
            chkWands.checked(SPDSettings.logWands());
            add(chkWands);

            chkArtifacts = new CheckBox(Messages.get(this, "artifacts")) {
                @Override
                protected void onClick() {
                    super.onClick();
                    SPDSettings.logArtifacts(checked());
                }
            };
            chkArtifacts.checked(SPDSettings.logArtifacts());
            add(chkArtifacts);

            chkMisc = new CheckBox(Messages.get(this, "misc")) {
                @Override
                protected void onClick() {
                    super.onClick();
                    SPDSettings.logMisc(checked());
                }
            };
            chkMisc.checked(SPDSettings.logMisc());
            add(chkMisc);

            sep3 = new ColorBlock(1, 1, 0xFF000000);
            add(sep3);

            chkRooms = new CheckBox(Messages.get(this, "use_rooms")) {
                @Override
                protected void onClick() {
                    super.onClick();
                    SPDSettings.useRooms(checked());
                }
            };
            chkRooms.checked(SPDSettings.useRooms());
            add(chkRooms);

            infoRooms = new IconButton(Icons.INFO.get()) {
                @Override
                protected void onClick() {
                    super.onClick();
                    ShatteredPixelDungeon.scene().add(
                            new WndMessage(Messages.get(WndLoggingOpts.class, "rooms_info"))
                    );
                }
            };
            add( infoRooms );

            chkBlacklist = new CheckBox(Messages.get(this, "blacklist")) {
                @Override
                protected void onClick() {
                    super.onClick();
                    SPDSettings.ignoreBlacklist(checked());
                }
            };
            chkBlacklist.checked(SPDSettings.ignoreBlacklist());
            add(chkBlacklist);

            infoBlacklist = new IconButton(Icons.INFO.get()) {
                @Override
                protected void onClick() {
                    super.onClick();
                    ShatteredPixelDungeon.scene().add(
                            new WndMessage(Messages.get(WndLoggingOpts.class, "blacklist_info"))
                    );
                }
            };
            add( infoBlacklist );

            chkShops = new CheckBox(Messages.get(this, "shops")) {
                @Override
                protected void onClick() {
                    super.onClick();
                    SPDSettings.checkShops(checked());
                }
            };
            chkShops.checked(SPDSettings.checkShops());
            add(chkShops);

            infoShops = new IconButton(Icons.INFO.get()) {
                @Override
                protected void onClick() {
                    super.onClick();
                    ShatteredPixelDungeon.scene().add(
                            new WndMessage(Messages.get(WndLoggingOpts.class, "shops_info"))
                    );
                }
            };
            add( infoShops );

            layout();
        }

        public void layout() {
            int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;

            title.setPos((width - title.width()) / 2f, (TTL_HEIGHT - title.height()) / 2);
            PixelScene.align(title);

            chkTrinkets.setRect(0, TTL_HEIGHT + GAP, width, BTN_HEIGHT);
            chkEquipment.setRect(0, chkTrinkets.bottom() + GAP, width, BTN_HEIGHT);
            chkScrolls.setRect(0, chkEquipment.bottom() + GAP, width, BTN_HEIGHT);
            chkPotions.setRect(0, chkScrolls.bottom() + GAP, width, BTN_HEIGHT);
            chkRings.setRect(0, chkPotions.bottom() + GAP, width, BTN_HEIGHT);
            chkWands.setRect(0, chkRings.bottom() + GAP, width, BTN_HEIGHT);
            chkArtifacts.setRect(0, chkWands.bottom() + GAP, width, BTN_HEIGHT);
            chkMisc.setRect(0, chkArtifacts.bottom() + GAP, width, BTN_HEIGHT);

            sep3.size(width, 1);
            sep3.y = chkMisc.bottom() + GAP;

            chkRooms.setRect(0, sep3.y + 1 + GAP, width - BTN_HEIGHT, BTN_HEIGHT);
            chkBlacklist.setRect(0, chkRooms.bottom() + GAP, width - BTN_HEIGHT, BTN_HEIGHT);
            chkShops.setRect(0, chkBlacklist.bottom() + GAP, width - BTN_HEIGHT, BTN_HEIGHT);

            infoRooms.setRect(chkRooms.right(), chkRooms.top(), BTN_HEIGHT, BTN_HEIGHT);
            infoBlacklist.setRect(chkBlacklist.right(), chkBlacklist.top(), BTN_HEIGHT, BTN_HEIGHT);
            infoShops.setRect(chkShops.right(), chkShops.top(), BTN_HEIGHT, BTN_HEIGHT);

            resize(width, (int)chkShops.bottom());

        }
    }
}