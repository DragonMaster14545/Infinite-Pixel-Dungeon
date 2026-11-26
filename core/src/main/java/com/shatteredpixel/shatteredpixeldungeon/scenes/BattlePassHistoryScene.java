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
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.Archs;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;
import java.util.List;

public class BattlePassHistoryScene extends PixelScene {

    private static final int ROW_HEIGHT = 30;
    private static final int ROW_GAP    = 3;
    private static final int MARGIN     = BattlePassUI.MARGIN;
    private static final int BTN_H      = 20;
    private static final int FOOTER_H   = BTN_H + 12;

    @Override
    public void create() {
        super.create();

        int w = Camera.main.width;
        int h = Camera.main.height;
        float inner = w - MARGIN * 2;

        Archs archs = new Archs();
        archs.setSize( w, h );
        add( archs );

        RenderedTextBlock title = renderTextBlock( Messages.get( this, "title" ), 12 );
        title.hardlight( BattlePassUI.GOLD );
        title.setPos( (w - title.width()) / 2f, 6 );
        align( title );
        add( title );

        List<BattlePass.MonthRecord> history = BattlePass.history();

        RenderedTextBlock summary = renderTextBlock( Messages.get( this, "summary", history.size() ), 8 );
        summary.hardlight( BattlePassUI.GREY );
        summary.setPos( (w - summary.width()) / 2f, title.bottom() + 3 );
        align( summary );
        add( summary );

        Component content = new Component();
        final ArrayList<SeasonRow> rows = new ArrayList<>();

        int y = 0;
        if (history.isEmpty()) {
            RenderedTextBlock empty = renderTextBlock( Messages.get( this, "empty" ), 9 );
            empty.maxWidth( (int) inner );
            empty.setPos( 0, 0 );
            content.add( empty );
            y = (int) empty.height();
        } else {
            for (final BattlePass.MonthRecord rec : history) {
                SeasonRow row = new SeasonRow( rec );
                row.setRect( 0, y, inner, ROW_HEIGHT );
                content.add( row );
                rows.add( row );
                y += ROW_HEIGHT + ROW_GAP;
            }
        }
        content.setSize( inner, Math.max( 0, y - ROW_GAP ) );

        int listTop = (int) summary.bottom() + 6;
        ScrollPane list = new ScrollPane( content ) {
            @Override
            public void onClick( float x, float y ) {
                for (SeasonRow row : rows) {
                    if (row.hit( x, y )) {
                        row.open();
                        break;
                    }
                }
            }
        };
        add( list );
        list.setRect( MARGIN, listTop, inner, h - listTop - FOOTER_H );

        StyledButton btnBack = new StyledButton( Chrome.Type.GREY_BUTTON_TR, Messages.get( this, "back" ), 8 ){
            @Override
            public void onClick(){
                onBackPressed();
            }
        };
        btnBack.setRect( MARGIN, h - FOOTER_H + 6, inner, BTN_H );
        add( btnBack );

        fadeIn();
    }

    @Override
    protected void onBackPressed(){
        ShatteredPixelDungeon.switchNoFade( BattlePassScene.class );
    }

    private class SeasonRow extends Component {

        private final BattlePass.MonthRecord rec;
        private BattlePassUI.Panel panel;
        private RenderedTextBlock name;
        private RenderedTextBlock tag;
        private BattlePassUI.Bar bar;

        SeasonRow( BattlePass.MonthRecord rec ){
            super();
            this.rec = rec;
            layout();
        }

        boolean hit( float px, float py ){
            return px >= x && px <= x + width && py >= y && py <= y + height;
        }

        void open(){
            BattlePassScene.seeMonth( rec.monthKey );
        }

        @Override
        protected void createChildren(){
            panel = new BattlePassUI.Panel();
            add( panel );

            name = PixelScene.renderTextBlock( 8 );
            add( name );

            tag = PixelScene.renderTextBlock( 7 );
            add( tag );

            bar = new BattlePassUI.Bar();
            add( bar );
        }

        @Override
        protected void layout(){
            if (rec == null || panel == null) return;
            panel.setRect( x, y, width, height );
            panel.style( rec.premium ? BattlePassUI.PURPLE : 0xFFFFFF, 0.10f, 0x000000, 0.8f );

            name.text( rec.label() );
            name.hardlight( BattlePassUI.GREY );
            name.setPos( x + 5, y + 4 );

            if (rec.premium) {
                tag.text( Messages.get( BattlePassHistoryScene.class, "premium" ) );
                tag.hardlight( BattlePassUI.GOLD );
                tag.setPos( x + width - tag.width() - 5, y + 4 );
                tag.visible = true;
            } else {
                tag.visible = false;
            }

            int total = BattlePass.TIER_XP.length;
            int reached = rec.tiersReached();
            bar.setRect( x + 5, y + height - 13, width - 10, 9 );
            bar.set( reached / (float) Math.max( 1, total ),
                    Messages.get( BattlePassHistoryScene.class, "tiers", reached, total ),
                    reached >= total ? BattlePassUI.GREEN : BattlePassUI.GOLD );
        }
    }
}
