package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Amulet;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.ShulkerBox;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.VaultLink;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.Key;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;
import java.util.Locale;

public class WndVaultSell extends Window {

	private static final int WIDTH = 140;
	private static final int GAP = 2;
	private static final int BTN_H = 16;
	private static final int ICON = 16;
	private static final int ROW_H = ICON + BTN_H + GAP + 3;
	private static final long CONFIRM_VALUE = 1000;

	private final Runnable back;

	private final RenderedTextBlock title, gold, energy, note;
	private final ScrollPane pane;
	private final Component content;
	private final float listH;

	private static String fmt( long n ) {
		return String.format( Locale.US, "%,d", n );
	}

	public WndVaultSell( Runnable back ) {
		super();
		this.back = back;

		if (WndBag.INSTANCE != null) {
			WndBag.INSTANCE.hide();
		}
		WndBag.INSTANCE = this;

		float y = 0;

		title = PixelScene.renderTextBlock( Messages.get( this, "title" ), 9 );
		title.hardlight( TITLE_COLOR );
		title.maxWidth( WIDTH );
		title.setPos( 0, y );
		add( title );
		y += 11 + GAP;

		float lineH = PixelScene.renderTextBlock( "A", 7 ).height();
		gold = PixelScene.renderTextBlock( "", 7 );
		gold.maxWidth( WIDTH / 2 - 2 );
		gold.hardlight( 0xFFE060 );
		gold.setPos( 0, y );
		add( gold );
		energy = PixelScene.renderTextBlock( "", 7 );
		energy.maxWidth( WIDTH / 2 - 2 );
		energy.hardlight( 0x60E0FF );
		energy.setPos( WIDTH / 2, y );
		add( energy );
		y += lineH * 2 + GAP;

		note = PixelScene.renderTextBlock( Messages.get( this, "note" ), 6 );
		note.maxWidth( WIDTH );
		note.hardlight( 0xB0B0B0 );
		float noteH = note.height() + GAP;

		float footer = noteH + BTN_H;
		int rows = Math.max( 1, sellable().size() );
		float maxList = PixelScene.uiCamera.height - 60 - y - footer - chrome.marginVer();
		listH = Math.max( ROW_H * 2, Math.min( rows * ROW_H, maxList ) );

		resize( WIDTH, (int) Math.ceil( y + listH + GAP + footer ) );

		pane = new ScrollPane( new Component() );
		add( pane );
		pane.setRect( 0, y, WIDTH, listH );
		content = pane.content();

		y += listH + GAP;

		note.setPos( 0, y );
		add( note );
		y += noteH;

		RedButton backBtn = new RedButton( Messages.get( this, "back" ) ) {
			@Override
			public void onClick() {
				hide();
				if (WndVaultSell.this.back != null) WndVaultSell.this.back.run();
			}
		};
		backBtn.setRect( 0, y, WIDTH, BTN_H );
		add( backBtn );

		refresh();
	}

	private static boolean canSell( Item item ) {
		return !( item instanceof Gold )
				&& !( item instanceof Bag )
				&& !( item instanceof Key )
				&& !( item instanceof Amulet )
				&& !( item instanceof ShulkerBox )
				&& !( item instanceof VaultLink )
				&& ( item.value() > 0 || item.energyVal() > 0 );
	}

	private static ArrayList<Item> sellable() {
		ArrayList<Item> list = new ArrayList<>();
		if (Dungeon.hero != null) collect( Dungeon.hero.belongings.backpack, list );
		return list;
	}

	private static void collect( Bag bag, ArrayList<Item> out ) {
		for (Item item : bag.items.toArray( new Item[0] )) {
			if (item instanceof Bag) {
				collect( (Bag) item, out );
			}
			if (canSell( item )) out.add( item );
		}
	}

	private void refresh() {
		gold.text( Messages.get( this, "gold", fmt( Dungeon.gold ) ) );
		energy.text( Messages.get( this, "energy", fmt( Dungeon.energy ) ) );

		content.clear();
		ArrayList<Item> items = sellable();

		float cy = 0;
		if (items.isEmpty()) {
			RenderedTextBlock none = PixelScene.renderTextBlock( Messages.get( this, "empty" ), 7 );
			none.maxWidth( WIDTH );
			none.hardlight( 0x808080 );
			none.setPos( 0, 2 );
			content.add( none );
			cy = none.height() + 4;
		}

		float bw = ( WIDTH - GAP ) / 2f;
		for (final Item item : items) {
			ItemSprite icon = new ItemSprite( item );
			icon.x = 0;
			icon.y = cy;
			content.add( icon );

			RenderedTextBlock text = PixelScene.renderTextBlock( Messages.titleCase( item.name() ), 6 );
			text.maxWidth( WIDTH - ICON - 2 );
			text.setPos( ICON + 2, cy + ( ICON - text.height() ) / 2f );
			PixelScene.align( text );
			content.add( text );

			final long g = item.value();
			final long e = item.energyVal();

			RedButton goldBtn = new RedButton( Messages.get( this, "sell_gold", fmt( g ) ), 7 ) {
				@Override
				public void onClick() {
					sell( item, false );
				}
			};
			goldBtn.setRect( 0, cy + ICON, bw, BTN_H );
			goldBtn.enable( g > 0 );
			content.add( goldBtn );

			RedButton energyBtn = new RedButton( Messages.get( this, "sell_energy", fmt( e ) ), 7 ) {
				@Override
				public void onClick() {
					sell( item, true );
				}
			};
			energyBtn.setRect( bw + GAP, cy + ICON, bw, BTN_H );
			energyBtn.enable( e > 0 );
			content.add( energyBtn );

			cy += ROW_H;
		}
		content.setSize( WIDTH, cy );
		pane.setSize( WIDTH, listH );
	}

	private void sell( final Item item, final boolean forEnergy ) {
		Hero hero = Dungeon.hero;
		if (hero == null || !hero.isAlive()) return;

		long worth = forEnergy ? item.energyVal() : item.value();
		boolean valuable = worth >= CONFIRM_VALUE || ( item.isUpgradable() && item.level() > 0 );

		if (!valuable) {
			complete( item, forEnergy );
			return;
		}

		GameScene.show( new WndOptions(
				new ItemSprite( item ),
				Messages.titleCase( item.name() ),
				Messages.get( WndVaultSell.class, "confirm", fmt( worth ),
						Messages.get( WndVaultSell.class, forEnergy ? "unit_energy" : "unit_gold" ) ),
				Messages.get( WndVaultSell.class, "yes" ),
				Messages.get( WndVaultSell.class, "no" ) ) {
			@Override
			protected void onSelect( int index ) {
				if (index == 0) complete( item, forEnergy );
			}
		} );
	}

	private void complete( Item item, boolean forEnergy ) {
		Hero hero = Dungeon.hero;
		if (hero == null || !hero.isAlive()) return;
		if (!canSell( item ) || !hero.belongings.backpack.contains( item )) {
			refresh();
			return;
		}

		long worth = forEnergy ? item.energyVal() : item.value();
		String name = item.name();

		item.detachAll( hero.belongings.backpack );
		if (forEnergy) {
			Dungeon.energy += worth;
		} else {
			Dungeon.gold += worth;
		}

		Sample.INSTANCE.play( Assets.Sounds.GOLD );
		GLog.p( Messages.get( WndVaultSell.class, "sold", name, fmt( worth ),
				Messages.get( WndVaultSell.class, forEnergy ? "unit_energy" : "unit_gold" ) ) );
		refresh();
	}

	@Override
	public void hide() {
		super.hide();
		if (WndBag.INSTANCE == this) {
			WndBag.INSTANCE = null;
		}
	}
}
