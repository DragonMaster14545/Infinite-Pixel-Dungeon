package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.ItemSlot;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollingGridPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Image;

import java.util.ArrayList;

public class WndHeapPaged extends Window {

    public static final int ITEMS_PER_PAGE = 20;

    private static final int WIDTH        = 200;
    private static final int GRID_COLS    = 5;
    private static final int CELL_SIZE    = 36;
    private static final int TITLE_HEIGHT = 20;
    private static final int GAP          = 2;
    private static final int NAV_HEIGHT   = 16;
    private static final int NAV_WIDTH    = 36;

    private final ArrayList<Item> items;
    private final Listener listener;

    private int page = 0;

    private final ScrollingGridPane itemArea;
    private final RenderedTextBlock pageLabel;
    private final RedButton btnPrev;
    private final RedButton btnNext;

    public interface Listener {
        void onSelect( Item item );
    }

    public WndHeapPaged( ArrayList<Item> heap, Listener listener ) {
        super();

        this.items = heap;
        this.listener = listener;

        IconTitle title = new IconTitle();
        title.icon( Icons.get( Icons.CATALOG ) );
        title.label( Messages.get( this, "title" ) );
        title.setRect( 0, 0, WIDTH, TITLE_HEIGHT );
        add( title );

        itemArea = new ScrollingGridPane();
        add( itemArea );

        pageLabel = PixelScene.renderTextBlock( 9 );
        pageLabel.hardlight( 0xCACFC2 );
        add( pageLabel );

        btnPrev = new RedButton( "<" ) {
            @Override
            protected void onClick() {
                if (page > 0) {
                    page--;
                    layout();
                }
            }
        };
        add( btnPrev );

        btnNext = new RedButton( ">" ) {
            @Override
            protected void onClick() {
                if ((page + 1) * ITEMS_PER_PAGE < items.size()) {
                    page++;
                    layout();
                }
            }
        };
        add( btnNext );

        layout();
    }

    private void layout() {
        itemArea.clear();

        int from = page * ITEMS_PER_PAGE;
        int to = Math.min( from + ITEMS_PER_PAGE, items.size() );

        for (int i = from; i < to; i++) {
            addGridItem( itemArea, items.get( i ) );
        }

        int shown = to - from;
        int rows = Math.max( 1, (int)Math.ceil( shown / (float)GRID_COLS ) );
        int gridHeight = rows * (CELL_SIZE + GAP);

        itemArea.setRect( 0, TITLE_HEIGHT, WIDTH, gridHeight );
        PixelScene.align(itemArea);

        int totalPages = Math.max( 1, (int)Math.ceil( items.size() / (float)ITEMS_PER_PAGE ) );
        pageLabel.text( (page + 1) + " / " + totalPages );
        pageLabel.setPos(
                (WIDTH - pageLabel.width()) / 2f,
                TITLE_HEIGHT + gridHeight + GAP
        );

        float navY = pageLabel.top() + pageLabel.height() + GAP;

        btnPrev.setRect( 0, navY, NAV_WIDTH, NAV_HEIGHT );
        btnPrev.active = page > 0;

        btnNext.setRect( WIDTH - NAV_WIDTH, navY, NAV_WIDTH, NAV_HEIGHT );
        btnNext.active = to < items.size();

        resize( WIDTH, (int)(navY + NAV_HEIGHT) );
    }

    private void addGridItem( ScrollingGridPane grid, final Item item ) {

        ItemSprite sprite = new ItemSprite( item.image, item.glowing() );

        if (item instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff) {
            com.watabou.utils.RectF frame = sprite.frame();
            frame.top += frame.height() / 8f;
            sprite.frame( frame );
        }

        Image secondIcon = null;
        if (item.icon != -1) {
            secondIcon = new Image( Assets.Sprites.ITEM_ICONS );
            secondIcon.frame( ItemSpriteSheet.Icons.film.get( item.icon ) );
        }

        ScrollingGridPane.GridItem gridItem = new ScrollingGridPane.GridItem( sprite ) {
            @Override
            public boolean onClick( float x, float y ) {
                if (inside( x, y )) {
                    hide();
                    if (listener != null) {
                        listener.onSelect( item );
                    }
                    return true;
                }
                return false;
            }
        };

        if (secondIcon != null) {
            gridItem.addSecondIcon( secondIcon );
        }

        if (item.cursed && item.cursedKnown) {
            gridItem.hardLightBG( 1f, 0.2f, 0.2f );
        }

        grid.addItem( gridItem );
    }
}