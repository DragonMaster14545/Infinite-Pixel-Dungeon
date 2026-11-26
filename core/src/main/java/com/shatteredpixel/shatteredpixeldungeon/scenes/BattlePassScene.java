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


import java.math.BigInteger;
import static com.shatteredpixel.shatteredpixeldungeon.BattlePass.claimPremium;

import com.shatteredpixel.shatteredpixeldungeon.BattlePass;
import com.shatteredpixel.shatteredpixeldungeon.BattlePassTiers;
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.Archs;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndMessage;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;

public class BattlePassScene extends PixelScene {

    private static final int ROW_HEIGHT = 40;
    private static final int ROW_GAP    = 3;
    private static final int MARGIN     = BattlePassUI.MARGIN;
    private static final int BTN_H      = 18;
    private static final int BTN_GAP    = 3;
    private static final int FOOTER_H   = BTN_H * 4 + BTN_GAP * 5;
    private static final int BADGE_W    = 30;
    private static final int CAPTION_H  = 14;

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
    private BattlePassUI.Bar xpBar;
    private RenderedTextBlock timeLine;
    private RenderedTextBlock bonusLine;
    private String lastTime = "";
    private ScrollPane list;
    private float listHeight;
    private float contentHeight;
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
        float inner = w - MARGIN * 2;

        Archs archs = new Archs();
        archs.setSize( w, h );
        add( archs );

        String titleStr = live
                ? Messages.get( this, "title", BattlePass.currentSeasonName() )
                : Messages.get( this, "title_past", viewRecord.seasonName );
        RenderedTextBlock title = renderTextBlock( titleStr, 12 );
        title.hardlight( BattlePassUI.GOLD );
        title.setPos( (w - title.width()) / 2f, 6 );
        align( title );
        add( title );

        float y = title.bottom() + 3;

        boolean premiumOwned = live ? BattlePass.isPremiumUnlocked() : viewRecord.premium;
        if (live) {
            String prevSeason = BattlePass.previousSeasonName();
            if (prevSeason != null) {
                RenderedTextBlock prevLine = renderTextBlock( Messages.get( this, "previous_season", prevSeason ), 7 );
                prevLine.hardlight( 0x888888 );
                prevLine.setPos( (w - prevLine.width()) / 2f, y );
                align( prevLine );
                add( prevLine );
                y = prevLine.bottom() + 2;
            }
        }

        if (premiumOwned) {
            RenderedTextBlock badge = renderTextBlock( Messages.get( this, live ? "premium_active" : "premium_badge" ), 8 );
            badge.hardlight( BattlePassUI.GOLD );
            badge.setPos( (w - badge.width()) / 2f, y );
            align( badge );
            add( badge );
            y = badge.bottom() + 2;
        }

        if (live) {
            timeLine = renderTextBlock( Messages.get( this, "days_left", BattlePass.timeRemainingInMonth() ), 8 );
            timeLine.hardlight( BattlePassUI.GREY );
            timeLine.setPos( (w - timeLine.width()) / 2f, y );
            align( timeLine );
            add( timeLine );
            y = timeLine.bottom() + 4;
        }

        xpBar = new BattlePassUI.Bar();
        xpBar.setRect( MARGIN, y, inner, 14 );
        add( xpBar );
        y += 14 + 2;

        bonusLine = renderTextBlock( "", 7 );
        bonusLine.hardlight( BattlePassUI.GREEN );
        add( bonusLine );
        updateHeader();
        bonusLine.setPos( (w - bonusLine.width()) / 2f, y );
        align( bonusLine );
        y = bonusLine.bottom() + 6;

        Component content = new Component();

        rows.clear();
        int totalTiers = totalTiers();
        int cy = 0;
        for (int tier = 1; tier <= totalTiers; tier++){
            TierRow row = new TierRow( tier );
            row.setRect( 0, cy, inner, ROW_HEIGHT );
            content.add( row );
            rows.add( row );
            cy += ROW_HEIGHT + ROW_GAP;
        }

        TierRow repeatRow = new TierRow( BattlePass.REPEATABLE_TIER );
        repeatRow.setRect( 0, cy, inner, ROW_HEIGHT + CAPTION_H );
        content.add( repeatRow );
        rows.add( repeatRow );
        cy += ROW_HEIGHT + CAPTION_H + ROW_GAP;

        contentHeight = Math.max( 0, cy - ROW_GAP );
        content.setSize( inner, contentHeight );

        int listTop = (int) y;
        listHeight = h - listTop - FOOTER_H;
        list = new ScrollPane( content ) {
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
        list.setRect( MARGIN, listTop, inner, listHeight );

        float btnW = (inner - BTN_GAP) / 2f;
        float row1Y = h - FOOTER_H + BTN_GAP;
        float row2Y = row1Y + BTN_H + BTN_GAP;
        float row3Y = row2Y + BTN_H + BTN_GAP;
        float row4Y = row3Y + BTN_H + BTN_GAP;
        float rightX = MARGIN + btnW + BTN_GAP;

        StyledButton btnBack = new StyledButton( Chrome.Type.GREY_BUTTON_TR, Messages.get( this, "back" ), 8 ){
            @Override
            public void onClick(){
                onBackPressed();
            }
        };

        if (live) {
            StyledButton btnClaimAll = new StyledButton( Chrome.Type.RED_BUTTON, Messages.get( this, "claim_all" ), 8 ){
                @Override
                public void onClick(){
                    claimAll();
                }
            };
            btnClaimAll.setRect( MARGIN, row1Y, btnW, BTN_H );
            add( btnClaimAll );

            StyledButton btnNext = new StyledButton( Chrome.Type.GREY_BUTTON_TR, Messages.get( this, "next_reward" ), 8 ){
                @Override
                public void onClick(){
                    jumpToNext();
                }
            };
            btnNext.setRect( rightX, row1Y, btnW, BTN_H );
            add( btnNext );

            StyledButton btnSeasonal = new StyledButton( Chrome.Type.GREY_BUTTON_TR, Messages.get( this, "seasonal_tasks" ), 8 ){
                @Override
                public void onClick(){
                    ShatteredPixelDungeon.switchNoFade( BattlePassSeasonalTasksScene.class );
                }
            };
            btnSeasonal.setRect( MARGIN, row2Y, btnW, BTN_H );
            add( btnSeasonal );

            StyledButton btnHistory = new StyledButton( Chrome.Type.GREY_BUTTON_TR, Messages.get( this, "history" ), 8 ){
                @Override
                public void onClick(){
                    ShatteredPixelDungeon.switchNoFade( BattlePassHistoryScene.class );
                }
            };
            btnHistory.setRect( rightX, row2Y, btnW, BTN_H );
            add( btnHistory );

            String premiumLabel;
            if (BattlePass.isPremium()) {
                premiumLabel = Messages.get( this, "premium_owned" );
            } else {
                try {
                    Item sample = BattlePass.premiumCostItem().newInstance();
                    premiumLabel = Messages.get( this, "buy_premium_item",
                            BattlePass.premiumCostItemQuantity, sample.name() );
                } catch (Exception e) {
                    premiumLabel = Messages.get( this, "buy_premium", BattlePass.PREMIUM_COST_GOLD );
                }
            }
            StyledButton btnPremium = new StyledButton( Chrome.Type.GREY_BUTTON_TR, premiumLabel, 8 ){
                @Override
                public void onClick(){
                    BattlePass.unlockPremium();
                }
            };
            btnPremium.setRect( MARGIN, row3Y, inner, BTN_H );
            add( btnPremium );

            btnBack.setRect( MARGIN, row4Y, btnW, BTN_H );
            add( btnBack );

            StyledButton btnReset = new StyledButton( Chrome.Type.GREY_BUTTON_TR, Messages.get( this, "reset" ), 8 ){
                @Override
                public void onClick(){
                    resetPass();
                }
            };
            btnReset.setRect( rightX, row4Y, btnW, BTN_H );
            btnReset.active = BattlePass.isBattlePassFinished();
            add( btnReset );
        } else {
            btnBack.setRect( MARGIN, h - FOOTER_H + BTN_GAP, inner, FOOTER_H - BTN_GAP * 2 );
            add( btnBack );
        }

        scrollToInitial();

        BattlePass.saveGlobal();
        fadeIn();
    }

    private void updateHeader(){
        if (viewMonthKey == null) {
            int reached = BattlePass.tiersReached();
            int into = BattlePass.xpIntoCurrentTier();
            int need = BattlePass.xpForCurrentTier();
            xpBar.set( need <= 0 ? 1f : into / (float) need,
                    Messages.get( this, "xp_bar", reached, into, need ), BattlePassUI.GOLD );
            int bonus = BattlePass.repeatableTiersUnlocked();
            bonusLine.text( bonus > 0 ? Messages.get( this, "bonus_tiers", bonus ) : "" );
            bonusLine.setPos( (Camera.main.width - bonusLine.width()) / 2f, bonusLine.top() );
        } else {
            int reached = viewRecord.tiersReached();
            xpBar.set( reached / (float) Math.max( 1, totalTiers() ),
                    Messages.get( this, "progress_past", reached, totalTiers() ), BattlePassUI.PURPLE );
            int bonus = viewRecord.repeatableTiersReached();
            bonusLine.text( bonus > 0 ? Messages.get( this, "bonus_tiers", bonus ) : "" );
        }
    }

    private void refresh(){
        for (TierRow row : rows) {
            row.layout();
        }
        updateHeader();
    }

    @Override
    public void update() {
        super.update();
        if (timeLine != null) {
            String t = BattlePass.timeRemainingInMonth();
            if (!t.equals( lastTime )) {
                lastTime = t;
                timeLine.text( Messages.get( this, "days_left", t ) );
            }
        }
    }

    private boolean canClaimNow(){
        return Dungeon.hero != null && Dungeon.hero.isAlive() && Dungeon.level != null;
    }

    private void claimAll(){
        if (viewMonthKey != null) return;
        if (!canClaimNow()) {
            GLog.w( Messages.get( this, "claim_all_ingame" ) );
            return;
        }
        int count = 0;
        for (int tier = 1; tier <= totalTiers(); tier++) {
            if (BattlePass.isClaimable( tier )) {
                BattlePass.claim( tier );
                count++;
            }
            if (BattlePass.isPremiumClaimable( tier )) {
                BattlePass.claimPremium( tier );
                count++;
            }
        }
        if (count == 0) {
            GLog.i( Messages.get( this, "nothing_to_claim" ) );
        } else {
            GLog.p( Messages.get( this, "claimed_all", count ) );
        }
        refresh();
    }

    private int nextClaimableRow(){
        if (viewMonthKey != null) return -1;
        for (int i = 0; i < rows.size(); i++) {
            int tier = i < totalTiers() ? i + 1 : BattlePass.REPEATABLE_TIER;
            if (BattlePass.isClaimable( tier ) || BattlePass.isPremiumClaimable( tier )) return i;
        }
        return -1;
    }

    private void scrollToRow( int index ){
        float target = index * (ROW_HEIGHT + ROW_GAP);
        float max = Math.max( 0, contentHeight - listHeight );
        list.scrollTo( 0, Math.max( 0, Math.min( target, max ) ) );
    }

    private void jumpToNext(){
        int idx = nextClaimableRow();
        if (idx < 0) {
            GLog.i( Messages.get( this, "nothing_to_claim" ) );
        } else {
            scrollToRow( idx );
        }
    }

    private void scrollToInitial(){
        int idx = nextClaimableRow();
        if (idx < 0) {
            int reached = viewMonthKey == null ? BattlePass.tiersReached() : viewRecord.tiersReached();
            idx = Math.max( 0, reached - 1 );
        }
        scrollToRow( idx );
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
        if (viewRecord != null) {
            boolean tierWasUnlocked = tier == BattlePass.REPEATABLE_TIER
                    ? viewRecord.repeatableTiersReached() > 0
                    : tier <= viewRecord.tiersReached();
            boolean alreadyClaimed = tier == BattlePass.REPEATABLE_TIER
                    ? viewRecord.repeatableTiersClaimed >= viewRecord.repeatableTiersReached()
                    : viewRecord.claimedTiers.contains( tier );
            return tierWasUnlocked && !alreadyClaimed;
        }
        return BattlePass.isClaimable(tier);
    }

    private boolean premiumClaimable(int tier){
        if (viewRecord != null) {
            return viewRecord.premium && !( tier == BattlePass.REPEATABLE_TIER
                            ? viewRecord.premiumRepeatableTiersClaimed >= viewRecord.repeatableTiersReached()
                            : viewRecord.premiumClaimedTiers.contains( tier ) )
                    && ( tier == BattlePass.REPEATABLE_TIER
                            ? viewRecord.repeatableTiersReached() > 0
                            : tier <= viewRecord.tiersReached() );
        }
        return BattlePass.isPremiumClaimable( tier );
    }

    private boolean premiumClaimed(int tier){
        if (viewRecord != null) {
            return tier == BattlePass.REPEATABLE_TIER
                    ? viewRecord.premiumRepeatableTiersClaimed >= viewRecord.repeatableTiersReached()
                    : viewRecord.premiumClaimedTiers.contains( tier );
        }
        return BattlePass.isPremiumClaimed( tier );
    }

    private void resetPass(){
        if (!BattlePass.isBattlePassFinished()) {
            ShatteredPixelDungeon.scene().addToFront( new WndMessage(
                    Messages.get( this, "reset_not_finished" ) ) );
            return;
        }
        if (!BattlePass.canAffordReset()) {
            ShatteredPixelDungeon.scene().addToFront( new WndMessage(
                    Messages.get( this, "reset_cant_afford", BattlePass.RESET_ENERGY_COST ) ) );
            return;
        }

        ShatteredPixelDungeon.scene().addToFront( new WndOptions(
                new ItemSprite(),
                Messages.get( this, "reset_confirm_title" ),
                Messages.get( this, "reset_confirm_body", BattlePass.RESET_ENERGY_COST ),
                Messages.get( this, "reset_confirm_yes" ),
                Messages.get( this, "reset_confirm_no" ) ){
            @Override
            protected void onSelect( int index ){
                if (index == 0 && BattlePass.resetImmediately()) {
                    GLog.p( Messages.get( BattlePassScene.class, "reset_done" ) );
                    BattlePassScene.seeCurrentMonth();
                }
            }
        } );
    }

    private int repeatableAvailable(){
        if (viewRecord != null) {
            return Math.max( 0, viewRecord.repeatableTiersReached() - viewRecord.repeatableTiersClaimed );
        }
        return BattlePass.repeatableTiersAvailable();
    }

    private int premiumRepeatableAvailable(){
        if (viewRecord != null) {
            return Math.max( 0, viewRecord.repeatableTiersReached() - viewRecord.premiumRepeatableTiersClaimed );
        }
        return BattlePass.repeatablePremiumTiersAvailable();
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

    private class RewardCard extends Component {

        private BattlePassUI.Panel panel;
        private Image icon;
        private RenderedTextBlock name;
        private RenderedTextBlock qty;
        private RenderedTextBlock bonus;
        private StyledButton btnClaim;
        private final boolean premiumTrack;

        RewardCard( boolean premiumTrack, Runnable onClaim ){
            super();
            this.premiumTrack = premiumTrack;
            this.onClaim = onClaim;
            btnClaim.text( Messages.get( BattlePassScene.class, "claim" ) );
        }

        private Runnable onClaim;

        @Override
        protected void createChildren(){
            panel = new BattlePassUI.Panel();
            add( panel );

            icon = new ItemSprite();
            add( icon );

            name = PixelScene.renderTextBlock( 8 );
            add( name );

            qty = PixelScene.renderTextBlock( 7 );
            add( qty );

            bonus = PixelScene.renderTextBlock( 7 );
            bonus.hardlight( BattlePassUI.GREEN );
            add( bonus );

            btnClaim = new StyledButton( Chrome.Type.RED_BUTTON, "", 8 ){
                @Override
                public void onClick(){
                    if (onClaim != null) onClaim.run();
                }
            };
            add( btnClaim );
        }

        boolean tryClick( float px, float py ){
            if (btnClaim.visible && btnClaim.active
                    && px >= btnClaim.left() && px <= btnClaim.right()
                    && py >= btnClaim.top() && py <= btnClaim.bottom()) {
                if (onClaim != null) onClaim.run();
                return true;
            }
            return false;
        }

        void show( Item reward, ArrayList<Item> extras, boolean unlocked, boolean owned, boolean claimed, boolean claimable ){
            panel.setRect( x, y, width, height );
            int accent = premiumTrack ? BattlePassUI.PURPLE : BattlePassUI.GREEN;
            if (claimable) {
                panel.style( BattlePassUI.GOLD, 0.22f, BattlePassUI.GOLD, 1f );
            } else if (claimed) {
                panel.style( accent, 0.10f, 0x000000, 0.8f );
            } else if (unlocked) {
                panel.style( premiumTrack ? BattlePassUI.PURPLE : 0xFFFFFF, 0.14f, 0x000000, 0.8f );
            } else {
                panel.style( 0xFFFFFF, 0.04f, 0x000000, 0.6f );
            }

            if (reward != null) {
                ((ItemSprite) icon).view( reward );
            } else {
                ((ItemSprite) icon).view( ItemSpriteSheet.GOLD, null );
            }
            icon.x = x + 5;
            icon.y = y + (height - icon.height()) / 2f;
            icon.alpha( !unlocked ? 0.3f : (premiumTrack && !owned ? 0.35f : (claimed ? 0.6f : 1f)) );

            btnClaim.visible = btnClaim.active = claimable && canClaimNow();
            float bw = 34;
            btnClaim.setRect( x + width - bw - 3, y + 3, bw, height - 6 );

            float textRight = (btnClaim.visible ? btnClaim.left() : x + width - 3) - 3;
            name.visible = reward != null;
            if (reward != null) {
                name.text( reward.name() );
                name.hardlight( premiumTrack ? BattlePassUI.GOLD : BattlePassUI.GREY );
                name.maxWidth( (int) Math.max( 10, textRight - (icon.x + icon.width() + 4) ) );
                name.setPos( icon.x + icon.width() + 4, y + (height - name.height()) / 2f );
            }

            boolean showQty = reward != null && reward.quantity().compareTo( BigInteger.ONE ) > 0;
            qty.visible = showQty;
            if (showQty) {
                qty.text( "x" + reward.quantity() );
                qty.hardlight( premiumTrack ? BattlePassUI.GOLD : BattlePassUI.GREY );
                qty.setPos( icon.x + (icon.width() - qty.width()) / 2f, icon.y + icon.height() - 1 );
            }

            int total = (reward != null ? 1 : 0) + (extras == null ? 0 : extras.size());
            bonus.visible = total > 1;
            if (bonus.visible) {
                bonus.text( "+" + (total - 1) );
                bonus.setPos( icon.x + icon.width() - bonus.width(), icon.y - 2 );
            }
        }
    }

    private class TierRow extends Component {

        private final int tier;

        private BattlePassUI.Panel rowBg;
        private BattlePassUI.Panel badge;
        private RenderedTextBlock number;
        private RenderedTextBlock status;
        private RenderedTextBlock caption;
        private RewardCard freeCard;
        private RewardCard premiumCard;

        TierRow( int tier ){
            this.tier = tier;
        }

        @Override
        protected void createChildren(){
            rowBg = new BattlePassUI.Panel();
            add( rowBg );

            badge = new BattlePassUI.Panel();
            add( badge );

            number = PixelScene.renderTextBlock( 10 );
            add( number );

            status = PixelScene.renderTextBlock( 6 );
            add( status );

            caption = PixelScene.renderTextBlock( 8 );
            add( caption );

            freeCard = new RewardCard( false, new Runnable() {
                @Override
                public void run(){
                    claim();
                }
            } );
            add( freeCard );

            premiumCard = new RewardCard( true, new Runnable() {
                @Override
                public void run(){
                    claimPremium();
                }
            } );
            add( premiumCard );
        }

        private void claim(){
            if (viewMonthKey != null) {
                if (!BattlePass.canAffordHistoryClaim()) {
                    ShatteredPixelDungeon.scene().addToFront( new WndMessage(
                            Messages.get( BattlePassScene.class, "history_cant_afford", BattlePass.HISTORY_CLAIM_ENERGY_COST ) ) );
                    return;
                }
                Item reward = BattlePass.buyHistoryTier( viewMonthKey, tier, false );
                if (reward != null) {
                    GLog.p( Messages.get( BattlePassScene.class, "claimed_item", reward.title() ) );
                }
                BattlePassScene.seeMonth( viewMonthKey );
                return;
            }
            if (!tierClaimable( tier )) return;
            Item reward = BattlePass.claim( tier );
            if (reward != null) {
                GLog.p( Messages.get( BattlePassScene.class, "claimed_item", reward.title() ) );
            } else {
                GLog.p( Messages.get( BattlePassScene.class, "claimed_gold" ) );
            }
            refresh();
        }

        private void claimPremium(){
            if (viewMonthKey != null) {
                if (!BattlePass.canAffordHistoryClaim()) {
                    ShatteredPixelDungeon.scene().addToFront( new WndMessage(
                            Messages.get( BattlePassScene.class, "history_cant_afford", BattlePass.HISTORY_CLAIM_ENERGY_COST ) ) );
                    return;
                }
                Item bonus = BattlePass.buyHistoryTier( viewMonthKey, tier, true );
                if (bonus != null) {
                    GLog.p( Messages.get( BattlePassScene.class, "claimed_item", bonus.title() ) );
                }
                BattlePassScene.seeMonth( viewMonthKey );
                return;
            }
            if (!BattlePass.isPremiumClaimable( tier )) return;
            Item bonus = BattlePass.claimPremium( tier );
            if (bonus != null) {
                GLog.p( Messages.get( BattlePassScene.class, "claimed_item", bonus.title() ) );
            }
            refresh();
        }

        boolean tryClaimClick( float px, float py ) {
            return freeCard.tryClick( px, py ) || premiumCard.tryClick( px, py );
        }

        @Override
        protected void layout(){
            boolean unlocked  = unlocked( tier );
            boolean claimed   = tierClaimed( tier );
            boolean claimable = tierClaimable( tier );
            boolean repeat    = tier == BattlePass.REPEATABLE_TIER;
            boolean current   = viewRecord == null && !repeat && tier == BattlePass.tiersReached() + 1;

            float top = repeat ? CAPTION_H : 0;
            float bodyH = height - top;

            rowBg.setRect( x, y + top, width, bodyH );
            rowBg.style( 0xFFFFFF, unlocked ? (claimed ? 0.05f : 0.10f) : 0.03f, current ? BattlePassUI.CYAN : 0x000000, current ? 1f : 0.6f );

            badge.setRect( x + 2, y + top + 2, BADGE_W, bodyH - 4 );
            int badgeRgb = claimable ? BattlePassUI.GOLD : (claimed ? BattlePassUI.GREEN : 0xFFFFFF);
            badge.style( badgeRgb, claimable ? 0.30f : (unlocked ? 0.16f : 0.05f), 0x000000, 0.8f );

            number.text( repeat ? "+" : Integer.toString( tier ) );
            number.hardlight( claimable ? BattlePassUI.GOLD : (unlocked ? 0xFFFFFF : 0x777777) );
            number.setPos( badge.left() + (BADGE_W - number.width()) / 2f, badge.top() + 6 );

            String st;
            if (repeat) {
                int available = repeatableAvailable();
                st = !unlocked ? Messages.get( BattlePassScene.class, "locked" )
                        : available > 0 ? Messages.get( BattlePassScene.class, "ready_count", available, premiumRepeatableAvailable() )
                        : Messages.get( BattlePassScene.class, "claimed" );
            } else {
                st = claimed ? Messages.get( BattlePassScene.class, "claimed" )
                        : unlocked ? Messages.get( BattlePassScene.class, "ready" )
                        : Messages.get( BattlePassScene.class, "locked" );
            }
            int stColor = claimable ? BattlePassUI.GOLD : (claimed ? BattlePassUI.GREEN : 0x888888);
            if (repeat) {
                status.visible = false;
                caption.visible = true;
                caption.text( st );
                caption.hardlight( stColor );
                caption.setPos( x + (width - caption.width()) / 2f, y + (CAPTION_H - caption.height()) / 2f );
                number.setPos( badge.left() + (BADGE_W - number.width()) / 2f, badge.top() + (badge.height() - number.height()) / 2f );
            } else {
                caption.visible = false;
                status.visible = true;
                status.text( st );
                status.maxWidth( BADGE_W - 2 );
                status.hardlight( stColor );
                status.setPos( badge.left() + (BADGE_W - status.width()) / 2f, number.bottom() + 3 );
            }

            float cardsX = x + BADGE_W + 5;
            float cardsW = width - BADGE_W - 7;
            float gap = 3;
            boolean showPremium = BattlePassTiers.hasPremiumReward( tier );
            float freeW = showPremium ? (cardsW - gap) / 2f : cardsW;

            Item reward = viewRecord != null ? viewRecord.rewardSnapshot.get( tier )
                    : repeat ? BattlePassTiers.repeatableRewardFor() : BattlePassTiers.rewardFor( tier );
            ArrayList<Item> rewardExtras = viewRecord != null
                    ? viewRecord.rewardExtraSnapshot.containsKey( tier ) ? viewRecord.rewardExtraSnapshot.get( tier ) : new ArrayList<Item>()
                    : BattlePassTiers.rewardExtrasFor( tier );

            freeCard.setRect( cardsX, y + top + 3, freeW, bodyH - 6 );
            freeCard.show( reward, rewardExtras, unlocked, true, claimed, claimable );

            if (showPremium) {
                Item premiumReward = viewRecord != null ? viewRecord.premiumRewardSnapshot.get( tier )
                        : repeat ? BattlePassTiers.premiumRepeatableRewardFor() : BattlePassTiers.premiumRewardFor( tier );
                ArrayList<Item> premiumExtras = viewRecord != null
                        ? viewRecord.premiumRewardExtraSnapshot.containsKey( tier ) ? viewRecord.premiumRewardExtraSnapshot.get( tier ) : new ArrayList<Item>()
                        : BattlePassTiers.premiumRewardExtrasFor( tier );
                boolean owned = viewRecord != null ? viewRecord.premium : BattlePass.isPremium();
                premiumCard.visible = true;
                premiumCard.setRect( cardsX + freeW + gap, y + top + 3, freeW, bodyH - 6 );
                premiumCard.show( premiumReward, premiumExtras, unlocked, owned, premiumClaimed( tier ) && owned, premiumClaimable( tier ) );
            } else {
                premiumCard.visible = false;
                premiumCard.active = false;
            }
        }
    }
}
