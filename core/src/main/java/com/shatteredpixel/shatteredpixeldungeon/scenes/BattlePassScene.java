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
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.Archs;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;

public class BattlePassScene extends PixelScene {

    private static final int ROW_HEIGHT = 24;
    private static final int ROW_GAP    = 2;
    private static final int MARGIN     = 8;
    private static final int HEADER_H   = 40;
    private static final int FOOTER_H   = 28;

    @Override
    public void create() {
        super.create();

        uiCamera.visible = false;

        int w = Camera.main.width;
        int h = Camera.main.height;

        Archs archs = new Archs();
        archs.setSize( w, h );
        add( archs );

        RenderedTextBlock title = renderTextBlock( Messages.get( this, "title" ), 12 );
        title.hardlight( 0xFFFF44 );
        title.setPos( (w - title.width()) / 2f, 6 );
        align( title );
        add( title );

        int reached = BattlePass.tiersReached();
        String progressStr = Messages.get( this, "progress",
                reached, BattlePass.xpIntoCurrentTier(), BattlePass.xpForCurrentTier() );
        RenderedTextBlock progress = renderTextBlock( progressStr, 9 );
        progress.hardlight( 0xCACFC2 );
        progress.setPos( (w - progress.width()) / 2f, title.bottom() + 4 );
        align( progress );
        add( progress );

        Component content = new Component();

        int totalTiers = BattlePass.TIER_XP.length;
        int y = 0;
        for (int tier = 1; tier <= totalTiers; tier++){
            TierRow row = new TierRow( tier );
            row.setRect( 0, y, w - MARGIN*2, ROW_HEIGHT );
            content.add( row );
            y += ROW_HEIGHT + ROW_GAP;
        }
        content.setSize( w - MARGIN*2, Math.max( 0, y - ROW_GAP ) );

        int listTop = HEADER_H;
        int listHeight = h - HEADER_H - FOOTER_H;
        ScrollPane list = new ScrollPane( content );
        list.setRect( MARGIN, listTop, w - MARGIN*2, listHeight );
        add( list );

        StyledButton btnBack = new StyledButton( Chrome.Type.GREY_BUTTON_TR, Messages.get( this, "back" ) ){
            @Override
            protected void onClick(){
                onBackPressed();
            }
        };
        btnBack.setRect( (w - 100) / 2f, h - FOOTER_H + 4, 100, FOOTER_H - 8 );
        add( btnBack );

        fadeIn();
    }

    @Override
    protected void onBackPressed(){
        if (Dungeon.hero != null){
            ShatteredPixelDungeon.switchNoFade( GameScene.class );
        } else {
            ShatteredPixelDungeon.switchNoFade( TitleScene.class );
        }
    }

    //one row: tier number, lock/claimed/claimable state, reward preview,
    //and (when claimable) a button to actually claim it
    private class TierRow extends Component {

        private final int tier;

        private RenderedTextBlock label;
        private Image rewardIcon;
        private StyledButton btnClaim;
        private ColorBlock bg;

        TierRow( int tier ){
            this.tier = tier;
        }

        @Override
        protected void createChildren(){
            bg = new ColorBlock( 1, 1, 0x40FFFFFF );
            add( bg );

            label = PixelScene.renderTextBlock( 9 );
            add( label );

            if (BattlePassTiers.isItemTier( tier )){
                rewardIcon = new ItemSprite();
            } else {
                rewardIcon = Icons.get( Icons.GOLD );
            }
            add( rewardIcon );

            btnClaim = new StyledButton( Chrome.Type.RED_BUTTON, Messages.get( BattlePassScene.class, "claim" ), 8 ){
                @Override
                protected void onClick(){
                    BattlePass.claim( tier );
                    ShatteredPixelDungeon.switchNoFade( BattlePassScene.class );
                }
            };
            add( btnClaim );
        }

        @Override
        protected void layout(){
            bg.x = x;
            bg.y = y;
            bg.size( width(), height() );

            boolean unlocked  = BattlePass.isUnlocked( tier );
            boolean claimed   = BattlePass.isClaimed( tier );
            boolean claimable = BattlePass.isClaimable( tier );

            bg.alpha( unlocked ? (claimed ? 0.15f : 0.3f) : 0.08f );

            String status = claimed
                    ? Messages.get( BattlePassScene.class, "claimed" )
                    : unlocked
                    ? Messages.get( BattlePassScene.class, "ready" )
                    : Messages.get( BattlePassScene.class, "locked" );
            label.text( Messages.get( BattlePassScene.class, "tier_row", tier, status ) );
            label.setPos( x + 4, y + (height() - label.height()) / 2f );

            if (rewardIcon instanceof ItemSprite){
                Item reward = BattlePassTiers.rewardFor( tier );
                if (reward != null){
                    ((ItemSprite) rewardIcon).view( reward );
                }
            }
            rewardIcon.x = x + width() - 60;
            rewardIcon.y = y + (height() - rewardIcon.height()) / 2f;
            rewardIcon.alpha( unlocked ? 1f : 0.3f );

            btnClaim.visible = btnClaim.active = claimable;
            btnClaim.setRect( x + width() - 44, y + 2, 40, height() - 4 );
        }
    }
}
