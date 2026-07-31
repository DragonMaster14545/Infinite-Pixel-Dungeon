package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.EnergyCrystal;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.test_tubes.Tubes;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Trinket;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
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
import com.watabou.noosa.ui.Component;
import com.watabou.utils.RectF;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Collection;

public class WndHeapPaged extends Window {

    // change this to raise/lower the per-page cap
    public static final int ITEMS_PER_PAGE = 10;

    private static final int WIDTH        = 244;
    private static final int HEIGHT       = 244;
    private static final int TITLE_HEIGHT = 24;
    private static final int SLOT_HEIGHT  = 30;
    private static final int GAP          = 2;
    private static final int NAV_HEIGHT   = 18;
    private static final int NAV_WIDTH    = 42;

    private static final int NUM_BUTTONS = 4;
    private static float[] scrollPositions = new float[NUM_BUTTONS];

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
        title.icon( Icons.get(Icons.CATALOG) );
        title.label( Messages.get( this, "title" ) );
        title.setRect( 0, 0, WIDTH, TITLE_HEIGHT );
        add( title );

        itemArea = new ScrollingGridPane(){
            @Override
            public synchronized void update() {
                super.update();
                scrollPositions[0] = content.camera.scroll.y;
            }
        };
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

    //rebuilds just the current page's contents and resizes the window
    private void layout() {
        itemArea.clear();

        int from = page * ITEMS_PER_PAGE;
        int to = Math.min( from + ITEMS_PER_PAGE, items.size() );

        int y = 0;
        for (int i = from; i < to; i++) {
            final Item item = items.get( i );

            ItemSlot slot = new ItemSlot( item ) {
                @Override
                protected void onClick() {
                    hide();
                    if (listener != null) {
                        listener.onSelect( item );
                    }
                }
            };
            slot.setRect( 0, y, WIDTH, SLOT_HEIGHT - GAP );
            addGridItems(itemArea, item, item.getClass());

            y += SLOT_HEIGHT;
        }

        itemArea.setSize( WIDTH, y );
        itemArea.setPos( 0, TITLE_HEIGHT );

        int totalPages = Math.max( 1, (int)Math.ceil( items.size() / (float)ITEMS_PER_PAGE ) );
        pageLabel.text( (page + 1) + " / " + totalPages );
        pageLabel.setPos(
                (WIDTH - pageLabel.width()) / 2f,
                TITLE_HEIGHT + y + GAP
        );

        float navY = pageLabel.top() + pageLabel.height() + GAP;

        btnPrev.setRect( 0, navY, NAV_WIDTH, NAV_HEIGHT );
        btnPrev.active = page > 0;

        btnNext.setRect( WIDTH - NAV_WIDTH, navY, NAV_WIDTH, NAV_HEIGHT );
        btnNext.active = to < items.size();

        resize( WIDTH, HEIGHT );
    }

    private static void addGridItems( ScrollingGridPane grid, Item item, Class<?> itemClass) {
        boolean seen = Catalog.isSeen(itemClass);
        ItemSprite sprite = null;
        Image secondIcon = null;
        String title = "";
        String desc = "";

        if (Item.class.isAssignableFrom(itemClass)) {

            item = (Item) Reflection.newInstance(itemClass);

            if (seen) {
                if (item instanceof Ring) {
                    ((Ring) item).anonymize();
                } else if (item instanceof Potion) {
                    ((Potion) item).anonymize();
                } else if (item instanceof Scroll) {
                    ((Scroll) item).anonymize();
                } else if (item instanceof Tubes) {
                    ((Tubes) item).anonymize();
                }
            }

            sprite = new ItemSprite(item.image, seen ? item.glowing() : null);
            if (!seen)  {
                sprite.lightness(0);
                title = "???";
                desc = Messages.get(WndJournal.CatalogTab.class, "not_seen_item");
            } else {
                title = Messages.titleCase(item.trueName());
                //some items don't include direct stats, generally when they're not applicable
                if (item instanceof SpiritBow || item instanceof Armor){
                    desc += item.desc();
                } else {
                    desc += item.info();
                }

                //mage's staff normally has 2 pixels extra at the top for particle effects, we chop that off here
                if (item instanceof MagesStaff){
                    RectF frame = sprite.frame();
                    frame.top += frame.height()/8f;
                    sprite.frame(frame);
                }

                if (item.icon != -1) {
                    secondIcon = new Image(Assets.Sprites.ITEM_ICONS);
                    secondIcon.frame(ItemSpriteSheet.Icons.film.get(item.icon));
                }
            }

        }

        String finalTitle = title;
        String finalDesc = desc;
        ScrollingGridPane.GridItem gridItem = new ScrollingGridPane.GridItem(sprite) {
            @Override
            public boolean onClick(float x, float y) {
                if (inside(x, y)) {
                    Image sprite = new ItemSprite();
                    sprite.copy(icon);
                    if (ShatteredPixelDungeon.scene() instanceof GameScene){
                        GameScene.show(new WndJournalItem(sprite, finalTitle, finalDesc));
                    } else {
                        ShatteredPixelDungeon.scene().addToFront(new WndJournalItem(sprite, finalTitle, finalDesc));
                    }
                    return true;
                } else {
                    return false;
                }
            }
        };
        if (secondIcon != null){
            gridItem.addSecondIcon(secondIcon);
        }
        if (!seen) {
            gridItem.hardLightBG(2f, 1f, 2f);
        }
        grid.addItem(gridItem);
    }
}