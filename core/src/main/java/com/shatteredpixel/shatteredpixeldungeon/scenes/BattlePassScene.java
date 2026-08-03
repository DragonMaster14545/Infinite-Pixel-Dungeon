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
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;

public class BattlePassScene extends PixelScene {

    private static final int ROW_HEIGHT = 24;
    private static final int ROW_GAP    = 2;
    private static final int MARGIN     = 8;
    private static final int HEADER_H   = 40;
    private static final int FOOTER_H   = 28;

    private static String pendingViewMonth = null;

    public static void seeMonth(String monthKey) {
        pendingViewMonth = monthKey;
        ShatteredPixelDungeon.switchNoFade(BattlePassScene.class);
    }

    public static void seeCurrentMonth() {
        pendingViewMonth = null;
        ShatteredPixelDungeon.switchNoFade(BattlePassScene.class);
    }

    private String viewMonthKey;
    private BattlePass.MonthRecord viewRecord;
    private final ArrayList<TierRow> rows = new ArrayList<>();

    @Override
    public void create() {
        super.create();

        viewMonthKey = pendingViewMonth;
        pendingViewMonth = null;
        if (viewMonthKey != null) {
            viewRecord = BattlePass.historyRecord(viewMonthKey);
            if (viewRecord == null) {
                viewMonthKey = null;
            }
        }
        boolean live = viewMonthKey == null;

        int w = Camera.main.width;
        int h = Camera.main.height;

        Archs archs = new Archs();
        archs.setSize( w, h );
        add( archs );

        String titleStr = live
                ? Messages.get( this, "title" )
                : Messages.get( this, "title_past", viewRecord.label() );
        RenderedTextBlock title = renderTextBlock( titleStr, 12 );
        title.hardlight( 0xFFFF44 );
        title.setPos( (w - title.width()) / 2f, 6 );
        align( title );
        add( title );

        String progressStr;
        if (live) {
            int reached = BattlePass.tiersReached();
            progressStr = Messages.get( this, "progress",
                    reached, BattlePass.xpIntoCurrentTier(), BattlePass.xpForCurrentTier() );
            int bonusReached = BattlePass.repeatableTiersUnlocked();
            if (bonusReached > 0) {
                progressStr += "\n" + Messages.get( this, "bonus_tiers", bonusReached );
            }
            progressStr += "\n" + Messages.get( this, "days_left", BattlePass.daysRemainingInMonth() );
        } else {
            progressStr = Messages.get( this, "progress_past", viewRecord.tiersReached(), totalTiers() );
            int bonusReachedPast = viewRecord.repeatableTiersReached();
            if (bonusReachedPast > 0) {
                progressStr += "\n" + Messages.get( this, "bonus_tiers", bonusReachedPast );
            }
        }
        RenderedTextBlock progress = renderTextBlock( progressStr, 9 );
        progress.hardlight( 0xCACFC2 );
        progress.setPos( (w - progress.width()) / 2f, title.bottom() + 4 );
        align( progress );
        add( progress );

        Component content = new Component();

        rows.clear();
        int totalTiers = totalTiers();
        int y = 0;
        for (int tier = 1; tier <= totalTiers; tier++){
            TierRow row = new TierRow( tier );
            row.setRect( 0, y, w - MARGIN*2, ROW_HEIGHT );
            content.add( row );
            rows.add( row );
            y += ROW_HEIGHT + ROW_GAP;
        }

        TierRow repeatRow = new TierRow( BattlePass.REPEATABLE_TIER );
        repeatRow.setRect( 0, y, w - MARGIN*2, ROW_HEIGHT );
        content.add( repeatRow );
        rows.add( repeatRow );
        y += ROW_HEIGHT + ROW_GAP;

        content.setSize( w - MARGIN*2, Math.max( 0, y - ROW_GAP ) );

        int listTop = HEADER_H + (live ? 0 : 10); //extra room for the wrapped "past" progress line
        int listHeight = h - listTop - FOOTER_H;
        ScrollPane list = new ScrollPane( content ) {
            @Override
            public void onClick( float x, float y ) {
                for (TierRow row : rows) {
                    if (row.tryClaimClick( x, y )) {
                        break;
                    }
                }
            }
        };
        add( list );
        list.setRect( MARGIN, listTop, w - MARGIN*2, listHeight );

        StyledButton btnBack = new StyledButton( Chrome.Type.GREY_BUTTON_TR, Messages.get( this, "back" ) ){
            @Override
            protected void onClick(){
                onBackPressed();
            }
        };

        if (live) {
            int btnW = 100;
            btnBack.setRect( w/2f - btnW - 4, h - FOOTER_H + 4, btnW, FOOTER_H - 8 );
            add( btnBack );

            StyledButton btnHistory = new StyledButton( Chrome.Type.GREY_BUTTON_TR, Messages.get( this, "history" ) ){
                @Override
                protected void onClick(){
                    ShatteredPixelDungeon.switchNoFade( BattlePassHistoryScene.class );
                }
            };
            btnHistory.setRect( w/2f + 4, h - FOOTER_H + 4, btnW, FOOTER_H - 8 );
            add( btnHistory );
        } else {
            btnBack.setRect( (w - 100) / 2f, h - FOOTER_H + 4, 100, FOOTER_H - 8 );
            add( btnBack );
        }

        fadeIn();
    }

    private static int totalTiers(){
        return BattlePass.TIER_XP.length;
    }

    private boolean unlocked(int tier){
        if (tier == BattlePass.REPEATABLE_TIER) {
            return viewRecord != null
                    ? viewRecord.repeatableTiersReached() > 0
                    : BattlePass.isUnlocked( tier );
        }
        return tier <= (viewRecord != null ? viewRecord.tiersReached() : BattlePass.tiersReached());
    }

    private boolean tierClaimed(int tier){
        if (tier == BattlePass.REPEATABLE_TIER) {
            return viewRecord != null
                    ? viewRecord.repeatableTiersClaimed >= viewRecord.repeatableTiersReached()
                    : BattlePass.isClaimed( tier );
        }
        return viewRecord != null ? viewRecord.claimedTiers.contains(tier) : BattlePass.isClaimed(tier);
    }

    private boolean tierClaimable(int tier){
        return viewRecord == null && BattlePass.isClaimable(tier);
    }

    //how many unclaimed repeats of the infinite tier are currently stacked up
    private int repeatableAvailable(){
        if (viewRecord != null) {
            return Math.max( 0, viewRecord.repeatableTiersReached() - viewRecord.repeatableTiersClaimed );
        }
        return BattlePass.repeatableTiersAvailable();
    }

    @Override
    protected void onBackPressed(){
        if (viewMonthKey != null) {
            ShatteredPixelDungeon.switchNoFade( BattlePassHistoryScene.class );
        } else if (Dungeon.hero != null && Dungeon.hero.isAlive() && Dungeon.level != null){
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

            rewardIcon = new ItemSprite();
            add( rewardIcon );

            btnClaim = new StyledButton( Chrome.Type.RED_BUTTON, Messages.get( BattlePassScene.class, "claim" ), 8 ){
                @Override
                protected void onClick(){
                    claim();
                }
            };
            add( btnClaim );
        }

        private void claim(){
            if (!tierClaimable( tier )) {
                return;
            }
            Item reward = BattlePass.claim( tier );
            if (reward != null) {
                GLog.p( Messages.get( BattlePassScene.class, "claimed_item", reward.title() ) );
            } else {
                GLog.p( Messages.get( BattlePassScene.class, "claimed_gold" ) );
            }
            BattlePassScene.seeCurrentMonth();
        }

        boolean tryClaimClick( float x, float y ){
            if (btnClaim.visible && btnClaim.active
                    && x >= btnClaim.left() && x <= btnClaim.right()
                    && y >= btnClaim.top() && y <= btnClaim.bottom()) {
                claim();
                return true;
            }
            return false;
        }

        @Override
        protected void layout(){
            bg.x = x;
            bg.y = y;
            bg.size( width(), height() );

            boolean unlocked  = unlocked( tier );
            boolean claimed   = tierClaimed( tier );
            boolean claimable = tierClaimable( tier );

            bg.alpha( unlocked ? (claimed ? 0.15f : 0.3f) : 0.08f );

            if (tier == BattlePass.REPEATABLE_TIER) {
                int available = repeatableAvailable();
                String status = !unlocked
                        ? Messages.get( BattlePassScene.class, "locked" )
                        : available > 0
                        ? Messages.get( BattlePassScene.class, "ready_count", available )
                        : Messages.get( BattlePassScene.class, "claimed" );
                label.text( Messages.get( BattlePassScene.class, "tier_row_repeatable", status ) );
            } else {
                String status = claimed
                        ? Messages.get( BattlePassScene.class, "claimed" )
                        : unlocked
                        ? Messages.get( BattlePassScene.class, "ready" )
                        : Messages.get( BattlePassScene.class, "locked" );
                label.text( Messages.get( BattlePassScene.class, "tier_row", tier, status ) );
            }
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

            btnClaim.visible = btnClaim.active = claimable && Dungeon.hero != null && Dungeon.hero.isAlive() && Dungeon.level != null   ;
            btnClaim.setRect( x + width() - 44, y + 2, 40, height() - 4 );
        }
    }
}