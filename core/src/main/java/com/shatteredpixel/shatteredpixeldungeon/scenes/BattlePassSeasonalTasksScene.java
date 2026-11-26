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

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.SeasonalTasks;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.Archs;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.Big;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;

public class BattlePassSeasonalTasksScene extends PixelScene {

    private static final int ROW_HEIGHT = 36;
    private static final int ROW_GAP    = 3;
    private static final int MARGIN     = BattlePassUI.MARGIN;
    private static final int BTN_H      = 20;
    private static final int FOOTER_H   = BTN_H + 12;

    private final ArrayList<TaskRow> rows = new ArrayList<>();
    private RenderedTextBlock summary;

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

        RenderedTextBlock notice = renderTextBlock( Messages.get( this, "notice" ), 8 );
        notice.hardlight( BattlePassUI.GREY );
        notice.maxWidth( (int) inner );
        notice.setPos( (w - notice.width()) / 2f, title.bottom() + 3 );
        align( notice );
        add( notice );

        summary = renderTextBlock( "", 8 );
        summary.hardlight( BattlePassUI.GREEN );
        add( summary );
        updateSummary();
        summary.setPos( (w - summary.width()) / 2f, notice.bottom() + 3 );
        align( summary );

        Component content = new Component();

        rows.clear();
        int y = 0;
        if (SeasonalTasks.tasks.isEmpty()) {
            RenderedTextBlock empty = renderTextBlock( Messages.get( this, "empty" ), 9 );
            empty.maxWidth( (int) inner );
            empty.setPos( 0, 0 );
            content.add( empty );
            y = (int) empty.height();
        } else {
            for (SeasonalTasks.Task task : SeasonalTasks.tasks) {
                TaskRow row = new TaskRow( task );
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
                for (TaskRow row : rows) {
                    if (row.tryClaimClick( x, y )) {
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

    private void updateSummary(){
        int done = 0;
        for (SeasonalTasks.Task t : SeasonalTasks.tasks) if (t.claimed) done++;
        summary.text( Messages.get( this, "summary", done, SeasonalTasks.tasks.size() ) );
    }

    @Override
    protected void onBackPressed(){
        ShatteredPixelDungeon.switchNoFade( BattlePassScene.class );
    }

    private void refresh(){
        for (TaskRow row : rows) {
            row.layout();
        }
        updateSummary();
    }

    private class TaskRow extends Component {

        private final SeasonalTasks.Task task;
        private BattlePassUI.Panel panel;
        private RenderedTextBlock label;
        private RenderedTextBlock status;
        private BattlePassUI.Bar bar;
        private StyledButton btnClaim;

        TaskRow( SeasonalTasks.Task task ){
            super();
            this.task = task;
            layout();
        }

        @Override
        protected void createChildren(){
            panel = new BattlePassUI.Panel();
            add( panel );

            label = PixelScene.renderTextBlock( 8 );
            add( label );

            status = PixelScene.renderTextBlock( 7 );
            add( status );

            bar = new BattlePassUI.Bar();
            add( bar );

            btnClaim = new StyledButton( Chrome.Type.RED_BUTTON, Messages.get( BattlePassSeasonalTasksScene.class, "claim" ), 8 ){
                @Override
                public void onClick(){
                    claim();
                }
            };
            add( btnClaim );
        }

        private void claim(){
            if (SeasonalTasks.claim( task )) {
                GLog.p( Messages.get( BattlePassSeasonalTasksScene.class, "task_claimed" ) );
                refresh();
            }
        }

        boolean tryClaimClick( float px, float py ){
            if (btnClaim.visible && btnClaim.active
                    && px >= btnClaim.left() && px <= btnClaim.right()
                    && py >= btnClaim.top() && py <= btnClaim.bottom()) {
                claim();
                return true;
            }
            return false;
        }

        @Override
        protected void layout(){
            if (task == null || panel == null) return;

            boolean ready = task.completed && !task.claimed;
            if (ready) {
                panel.style( BattlePassUI.GOLD, 0.22f, BattlePassUI.GOLD, 1f );
            } else if (task.claimed) {
                panel.style( BattlePassUI.GREEN, 0.10f, 0x000000, 0.8f );
            } else {
                panel.style( 0xFFFFFF, 0.10f, 0x000000, 0.8f );
            }
            panel.setRect( x, y, width, height );

            float btnW = 40;
            btnClaim.visible = btnClaim.active = ready;
            btnClaim.setRect( x + width - btnW - 4, y + 4, btnW, height - 8 );

            String st = task.claimed
                    ? Messages.get( BattlePassSeasonalTasksScene.class, "claimed" )
                    : task.completed
                      ? Messages.get( BattlePassSeasonalTasksScene.class, "ready" )
                      : "";
            status.text( st );
            status.hardlight( task.claimed ? BattlePassUI.GREEN : BattlePassUI.GOLD );
            status.visible = !st.isEmpty() && !ready;
            status.setPos( x + width - status.width() - 5, y + 4 );

            float textRight = (ready ? btnClaim.left() : (status.visible ? status.left() : x + width - 4)) - 4;
            label.text( task.description() );
            label.hardlight( task.claimed ? 0x888888 : BattlePassUI.GREY );
            label.maxWidth( (int) Math.max( 20, textRight - (x + 5) ) );
            label.setPos( x + 5, y + 4 );

            float barW = (ready ? btnClaim.left() - 4 : x + width - 5) - (x + 5);
            bar.setRect( x + 5, y + height - 13, Math.max( 10, barW ), 9 );
            BigInteger target = task.target == null ? BigInteger.ONE : task.target;
            BigInteger prog = task.progress == null ? BigInteger.ZERO : task.progress;
            bar.set( BattlePassUI.frac( prog.min( target ), target ),
                    Big.fmt( prog.min( target ) ) + " / " + Big.fmt( target ),
                    task.claimed ? BattlePassUI.GREEN : BattlePassUI.GOLD );
        }
    }
}
