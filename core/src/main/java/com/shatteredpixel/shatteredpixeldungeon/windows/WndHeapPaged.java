package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
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
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Image;
import com.watabou.utils.RectF;

import java.util.ArrayList;

public class WndHeapPaged extends Window {

    public static final int ITEMS_PER_PAGE = 40;
    private static final int MIN_ROWS = 2;

    private static final int WIDTH        = 128;
    private static final int TITLE_HEIGHT = 16;
    private static final int CELL_PITCH   = 18;
    private static final int GRID_COLS    = WIDTH / CELL_PITCH;
    private static final int MAX_ROWS     = (int)Math.ceil( ITEMS_PER_PAGE / (float)GRID_COLS );

    private static final int GAP          = 2;
    private static final int NAV_HEIGHT   = 16;
    private static final int NAV_WIDTH    = 32;

    private final ArrayList<Item> items;
    private final Listener listener;

    private final int perPage;
    private final int gridHeight;

    private int page = 0;

    private final ScrollingGridPane grid;
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
        title.icon( Icons.CATALOG.get() );
        title.label( Messages.get( this, "title" ) );
        title.setRect( 0, 0, WIDTH, TITLE_HEIGHT );
        add( title );

        int availHeight = PixelScene.uiCamera.height - chrome.marginVer() - TITLE_HEIGHT - GAP * 3 - NAV_HEIGHT - 10;
        int rows = Math.max( MIN_ROWS, Math.min( MAX_ROWS, availHeight / CELL_PITCH ) );
        int needed = Math.max( 1, (int)Math.ceil( items.size() / (float)GRID_COLS ) );
        rows = Math.min( rows, needed );
        perPage = rows * GRID_COLS;
        gridHeight = rows * CELL_PITCH;

        grid = new ScrollingGridPane();
        add( grid );
        grid.setRect( 0, TITLE_HEIGHT, WIDTH, gridHeight );

        int totalPages = totalPages();

        pageLabel = PixelScene.renderTextBlock( (page + 1) + " / " + totalPages, 9 );
        pageLabel.hardlight( 0xCACFC2 );
        pageLabel.setPos( (WIDTH - pageLabel.width()) / 2f, TITLE_HEIGHT + gridHeight + GAP );
        add( pageLabel );

        btnPrev = new RedButton( "<" ) {
            @Override
            public void onClick() {
                if (page > 0) {
                    page--;
                    updateList();
                }
            }
        };
        add( btnPrev );

        btnNext = new RedButton( ">" ) {
            @Override
            public void onClick() {
                if ((page + 1) * perPage < items.size()) {
                    page++;
                    updateList();
                }
            }
        };
        add( btnNext );

        float navY = TITLE_HEIGHT + gridHeight + GAP + pageLabel.height() + GAP;
        btnPrev.setRect( 0, navY, NAV_WIDTH, NAV_HEIGHT );
        btnNext.setRect( WIDTH - NAV_WIDTH, navY, NAV_WIDTH, NAV_HEIGHT );

        resize( WIDTH, (int)(navY + NAV_HEIGHT) );

        updateList();
    }

    private int totalPages() {
        return Math.max( 1, (int)Math.ceil( items.size() / (float)perPage ) );
    }

    private void updateList() {
        grid.clear();

        int from = page * perPage;
        int to = Math.min( from + perPage, items.size() );

        for (int i = from; i < to; i++) {
            addGridItem( items.get( i ) );
        }

        int itemsOnPage = Math.max( 1, to - from );
        int rowsUsed = (int)Math.ceil( itemsOnPage / (float)GRID_COLS );
        int usedHeight = rowsUsed * CELL_PITCH;
        float gridY = TITLE_HEIGHT + (gridHeight - usedHeight) / 2f;
        grid.setRect( 0, gridY, WIDTH, usedHeight );

        pageLabel.text( (page + 1) + " / " + totalPages() );
        pageLabel.setPos( (WIDTH - pageLabel.width()) / 2f, TITLE_HEIGHT + gridHeight + GAP );

        btnPrev.enable( page > 0 );
        btnNext.enable( to < items.size() );
    }

    private void addGridItem( final Item item ) {

        ItemSprite sprite = new ItemSprite( item.image, item.glowing() );

        //mage's staff has extra top padding for particle effects, trimmed
        //the same way CatalogTab does it
        if (item instanceof MagesStaff) {
            RectF frame = sprite.frame();
            frame.top += frame.height() / 8f;
            sprite.frame( frame );
        }

        Image secondIcon = null;
        if (item.icon != -1 && item.isIdentified()) {
            secondIcon = new Image( Assets.Sprites.ITEM_ICONS );
            secondIcon.frame( ItemSpriteSheet.Icons.film.get( item.icon ) );
        }

        final String status = item.status();

        ScrollingGridPane.GridItem gridItem = new ScrollingGridPane.GridItem( sprite ) {
            private BitmapText statusText;

            {
                if (status != null) {
                    statusText = new BitmapText( status, PixelScene.pixelFont );
                    statusText.measure();
                    add( statusText );
                    layout();
                }
            }

            @Override
            protected void layout() {
                super.layout();
                if (statusText != null) {
                    statusText.x = x + width() - statusText.width();
                    statusText.y = y + height() - statusText.height();
                    PixelScene.align( statusText );
                }
            }

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