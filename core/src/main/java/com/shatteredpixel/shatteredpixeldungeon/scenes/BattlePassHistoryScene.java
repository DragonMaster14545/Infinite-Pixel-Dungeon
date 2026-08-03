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

import java.util.List;

public class BattlePassHistoryScene extends PixelScene {

    private static final int ROW_HEIGHT = 24;
    private static final int ROW_GAP    = 2;
    private static final int MARGIN     = 8;
    private static final int HEADER_H   = 32;
    private static final int FOOTER_H   = 28;

    @Override
    public void create() {
        super.create();

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

        List<BattlePass.MonthRecord> history = BattlePass.history();

        Component content = new Component();

        if (history.isEmpty()) {

            RenderedTextBlock empty = renderTextBlock( Messages.get( this, "empty" ), 9 );
            empty.setPos( 0, 0 );
            content.add( empty );
            content.setSize( w - MARGIN*2, (int)empty.height() );

        } else {

            int y = 0;
            for (final BattlePass.MonthRecord rec : history) {
                StyledButton row = new StyledButton( Chrome.Type.GREY_BUTTON_TR,
                        Messages.get( this, "row", rec.label(), rec.tiersReached(), BattlePass.TIER_XP.length ) ){
                    @Override
                    protected void onClick(){
                        BattlePassScene.seeMonth( rec.monthKey );
                    }
                };
                row.setRect( 0, y, w - MARGIN*2, ROW_HEIGHT );
                content.add( row );
                y += ROW_HEIGHT + ROW_GAP;
            }
            content.setSize( w - MARGIN*2, Math.max( 0, y - ROW_GAP ) );
        }

        int listTop = HEADER_H;
        int listHeight = h - HEADER_H - FOOTER_H;
        ScrollPane list = new ScrollPane( content );
        add( list );
        list.setRect( MARGIN, listTop, w - MARGIN*2, listHeight );

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
        ShatteredPixelDungeon.switchNoFade( BattlePassScene.class );
    }
}