/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * Experienced Pixel Dungeon
 * Copyright (C) 2019-2024 Trashbox Bobylev
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

import com.shatteredpixel.shatteredpixeldungeon.BattlePass;
import com.shatteredpixel.shatteredpixeldungeon.BattlePassTiers;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.Archs;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;

public class BattlePassPreviewScene extends PixelScene {

    private static final int ROW_HEIGHT = 48;
    private static final int ROW_GAP    = 4;
    private static final int MARGIN     = 8;
    private static final int HEADER_H   = 40;
    private static final int FOOTER_H   = 28;

    private RenderedTextBlock info;

    public static void see() {
        ShatteredPixelDungeon.switchNoFade( BattlePassPreviewScene.class );
    }

    @Override
    public void create() {
        super.create();

        int w = Camera.main.width;
        int h = Camera.main.height;

        Archs archs = new Archs();
        archs.setSize( w, h );
        add( archs );

        RenderedTextBlock title = renderTextBlock( Messages.get( this, "title", BattlePass.currentSeasonName() ), 12 );
        title.hardlight( 0xFFFF44 );
        title.setPos( (w - title.width()) / 2f, 6 );
        align( title );
        add( title );

        RenderedTextBlock notice = renderTextBlock( Messages.get( this, "notice" ), 9 );
        notice.hardlight( 0xCACFC2 );
        notice.setPos( (w - notice.width()) / 2f, title.bottom() + 4 );
        align( notice );
        add( notice );

        String infoStr = Messages.get( this, "info",
                BattlePass.tiersReached(),
                BattlePass.xpIntoCurrentTier(),
                BattlePass.xpForCurrentTier(),
                BattlePass.timeRemainingInMonth() );

        info = renderTextBlock( infoStr, 9 );
        info.hardlight( 0xCACFC2 );
        info.setPos( (w - info.width()) / 2f, notice.bottom() + 4 );
        align( info );
        add( info );

        Component content = new Component();

        int totalTiers = BattlePass.TIER_XP.length;
        int y = 0;
        for (int tier = 1; tier <= totalTiers; tier++){
            PreviewRow row = new PreviewRow( tier );
            row.setRect( 0, y, w - MARGIN*2, ROW_HEIGHT );
            content.add( row );
            y += ROW_HEIGHT + ROW_GAP;
        }

        PreviewRow repeatRow = new PreviewRow( BattlePass.REPEATABLE_TIER );
        repeatRow.setRect( 0, y, w - MARGIN*2, ROW_HEIGHT );
        content.add( repeatRow );
        y += ROW_HEIGHT + ROW_GAP;

        content.setSize( w - MARGIN*2, Math.max( 0, y - ROW_GAP ) );

        int listTop = (int) info.bottom() + 6;
        int listHeight = h - listTop - FOOTER_H;
        ScrollPane list = new ScrollPane( content );
        add( list );
        list.setRect( MARGIN, listTop, w - MARGIN*2, listHeight );

        StyledButton btnBack = new StyledButton( com.shatteredpixel.shatteredpixeldungeon.Chrome.Type.GREY_BUTTON_TR,
                Messages.get( this, "back" ) ){
            @Override
            protected void onClick(){
                onBackPressed();
            }
        };
        btnBack.setRect( (w - 100) / 2f, h - FOOTER_H + 4, 100, FOOTER_H - 8 );
        add( btnBack );

        BattlePass.saveGlobal();

        fadeIn();
    }

    @Override
    protected void onBackPressed(){
        ShatteredPixelDungeon.switchNoFade( TitleScene.class );
    }

    @Override
    public void update() {
        super.update();
        info.text( info.text().replaceAll( "\\S+d \\d{2}h \\d{2}m", BattlePass.timeRemainingInMonth() ) );
    }

    private static class PreviewRow extends Component {

        private final int tier;

        private RenderedTextBlock label;
        private ColorBlock normalBox;
        private ColorBlock premiumBox;
        private Image rewardIcon;
        private Image premiumIcon;
        private RenderedTextBlock rewardNameLabel;
        private RenderedTextBlock premiumNameLabel;
        private RenderedTextBlock qtyLabel;
        private RenderedTextBlock premiumQtyLabel;

        PreviewRow( int tier ){
            this.tier = tier;
        }

        @Override
        protected void createChildren(){
            label = PixelScene.renderTextBlock( 9 );
            add( label );

            normalBox = new ColorBlock( 1, 1, 0x30FFFFFF );
            add( normalBox );

            premiumBox = new ColorBlock( 1, 1, 0x30FFFFFF );
            add( premiumBox );

            rewardIcon = new ItemSprite();
            rewardIcon.visible = false;
            add( rewardIcon );

            premiumIcon = new ItemSprite();
            premiumIcon.visible = false;
            add( premiumIcon );

            rewardNameLabel = PixelScene.renderTextBlock( 8 );
            rewardNameLabel.hardlight( 0xCACFC2 );
            add( rewardNameLabel );

            premiumNameLabel = PixelScene.renderTextBlock( 8 );
            premiumNameLabel.hardlight( 0xFFD700 );
            add( premiumNameLabel );

            qtyLabel = PixelScene.renderTextBlock( 8 );
            qtyLabel.hardlight( 0xCACFC2 );
            add( qtyLabel );

            premiumQtyLabel = PixelScene.renderTextBlock( 8 );
            premiumQtyLabel.hardlight( 0xFFD700 );
            add( premiumQtyLabel );
        }

        @Override
        protected void layout(){
            boolean unlocked = BattlePass.isUnlocked( tier );
            String status = unlocked
                    ? Messages.get( BattlePassPreviewScene.class, "unlocked" )
                    : Messages.get( BattlePassPreviewScene.class, "not_unlocked" );

            String tierLabel = tier == BattlePass.REPEATABLE_TIER
                    ? Messages.get( BattlePassScene.class, "tier_row_repeatable", status )
                    : Messages.get( BattlePassScene.class, "tier_row", tier, status );
            label.text( tierLabel );
            label.setPos( x + (width() - label.width()) / 2f, y + 2 );

            float boxY = y + label.height() + 6;
            float boxH = height() - label.height() - 8;
            float gap  = 4;
            float boxW = (width() - gap) / 2f;

            float normalX  = x;
            float premiumX = x + boxW + gap;

            normalBox.x = normalX;
            normalBox.y = boxY;
            normalBox.size( boxW, boxH );
            normalBox.alpha( 0.25f );

            premiumBox.x = premiumX;
            premiumBox.y = boxY;
            premiumBox.size( boxW, boxH );
            premiumBox.alpha( 0.25f );

            Item reward = BattlePassTiers.rewardFor( tier );
            rewardIcon.visible = true;
            if (reward != null){
                if (reward instanceof Scroll) {
                    ((Scroll) reward).anonymize();
                } else if (reward instanceof Ring) {
                    ((Ring) reward).anonymize();
                } else if (reward instanceof Potion) {
                    ((Potion) reward).anonymize();
                }
                ((ItemSprite) rewardIcon).view( reward );
            } else {
                ((ItemSprite) rewardIcon).view( ItemSpriteSheet.GOLD, null );
            }
            rewardIcon.x = normalX + 6;
            rewardIcon.y = boxY + (boxH - rewardIcon.height()) / 2f;

            rewardNameLabel.visible = reward != null;
            if (reward != null) {
                rewardNameLabel.text( reward.name() );
                rewardNameLabel.maxWidth( (int)(boxW - (rewardIcon.width() + 12) - 4) );
                rewardNameLabel.setPos(
                        rewardIcon.x + rewardIcon.width() + 6,
                        rewardIcon.y + (rewardIcon.height() - rewardNameLabel.height()) / 2f
                );
            }

            qtyLabel.visible = reward != null && reward.quantity() > 1;
            if (qtyLabel.visible) {
                qtyLabel.text( "x" + reward.quantity() );
                qtyLabel.setPos( rewardIcon.x + (rewardIcon.width() - qtyLabel.width()) / 2f,
                        rewardIcon.y + rewardIcon.height() - 2 );
            }

            boolean showPremium = tier != BattlePass.REPEATABLE_TIER && BattlePassTiers.hasPremiumReward( tier );
            if (showPremium) {
                Item premiumReward = BattlePassTiers.premiumRewardFor( tier );

                premiumIcon.visible = true;
                if (premiumReward != null) {
                    if (premiumReward instanceof Scroll) {
                        ((Scroll) premiumReward).anonymize();
                    } else if (premiumReward instanceof Ring) {
                        ((Ring) premiumReward).anonymize();
                    } else if (premiumReward instanceof Potion) {
                        ((Potion) premiumReward).anonymize();
                    }
                    ((ItemSprite) premiumIcon).view( premiumReward );
                } else {
                    ((ItemSprite) premiumIcon).view( ItemSpriteSheet.GOLD, null );
                }
                premiumIcon.x = premiumX + 6;
                premiumIcon.y = boxY + (boxH - premiumIcon.height()) / 2f;

                premiumNameLabel.visible = premiumReward != null;
                if (premiumReward != null) {
                    premiumNameLabel.text( premiumReward.name() );
                    premiumNameLabel.maxWidth( (int)(boxW - (premiumIcon.width() + 12) - 4) );
                    premiumNameLabel.setPos(
                            premiumIcon.x + premiumIcon.width() + 6,
                            premiumIcon.y + (premiumIcon.height() - premiumNameLabel.height()) / 2f
                    );
                }

                premiumQtyLabel.visible = premiumReward != null && premiumReward.quantity() > 1;
                if (premiumQtyLabel.visible) {
                    premiumQtyLabel.text( "x" + premiumReward.quantity() );
                    premiumQtyLabel.setPos( premiumIcon.x + (premiumIcon.width() - premiumQtyLabel.width()) / 2f,
                            premiumIcon.y + premiumIcon.height() - 2 );
                }
            } else {
                premiumIcon.visible = false;
                premiumNameLabel.visible = false;
                premiumQtyLabel.visible = false;
            }

            float dimAlpha = unlocked ? 1f : 0.35f;
            rewardIcon.alpha( dimAlpha );
            premiumIcon.alpha( dimAlpha );
        }
    }
}