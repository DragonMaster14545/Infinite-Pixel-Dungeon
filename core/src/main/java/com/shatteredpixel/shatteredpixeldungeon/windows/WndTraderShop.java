package com.shatteredpixel.shatteredpixeldungeon.windows;


import com.shatteredpixel.shatteredpixeldungeon.utils.Big;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.TraderStock;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.ItemSlot;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;
import java.util.Locale;

public class WndTraderShop extends Window {

	private static final int WIDTH = 140;
	private static final int GAP = 2;
	private static final int BTN_H = 16;
	private static final int ICON = 16;
	private static final int ROW_H = ICON + BTN_H + GAP + 3;

	private final Runnable back;

	private final RenderedTextBlock title, timer, gold, energy;
	private final ScrollPane pane;
	private final Component content;
	private final float listH;

	private long shownSeconds = -1;

	private static String fmt( long n ) {
		return String.format( Locale.US, "%,d", n );
	}

	public WndTraderShop( Runnable back ) {
		super();
		this.back = back;

		if (WndBag.INSTANCE != null) {
			WndBag.INSTANCE.hide();
		}
		WndBag.INSTANCE = this;

		TraderStock.ensureCurrent();

		float y = 0;

		title = PixelScene.renderTextBlock( Messages.get( this, "title" ), 9 );
		title.hardlight( TITLE_COLOR );
		title.maxWidth( WIDTH );
		title.setPos( 0, y );
		add( title );
		y += 11 + GAP;

		timer = PixelScene.renderTextBlock( "", 6 );
		timer.maxWidth( WIDTH );
		timer.hardlight( 0xB0B0B0 );
		timer.setPos( 0, y );
		add( timer );
		y += 8 + GAP;

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

		float footer = BTN_H;
		float maxList = PixelScene.uiCamera.height - 60 - y - footer - chrome.marginVer();
		listH = Math.max( ROW_H * 2, Math.min( TraderStock.OFFERS * ROW_H, maxList ) );

		resize( WIDTH, (int) Math.ceil( y + listH + GAP + footer ) );

		pane = new ScrollPane( new Component() );
		add( pane );
		pane.setRect( 0, y, WIDTH, listH );
		content = pane.content();

		y += listH + GAP;

		float half = ( WIDTH - GAP ) / 2f;

		RedButton sell = new RedButton( Messages.get( this, "sell" ) ) {
			@Override
			public void onClick() {
				hide();
				GameScene.show( new WndVaultSell( new Runnable() {
					@Override
					public void run() {
						GameScene.show( new WndTraderShop( WndTraderShop.this.back ) );
					}
				} ) );
			}
		};
		sell.setRect( 0, y, half, BTN_H );
		add( sell );

		RedButton backBtn = new RedButton( Messages.get( this, "back" ) ) {
			@Override
			public void onClick() {
				hide();
				if (WndTraderShop.this.back != null) WndTraderShop.this.back.run();
			}
		};
		backBtn.setRect( half + GAP, y, half, BTN_H );
		add( backBtn );

		refresh();
		updateTimer();
	}

	@Override
	public void update() {
		super.update();

		if (TraderStock.ensureCurrent()) {
			GLog.i( Messages.get( this, "restocked" ) );
			refresh();
		}
		updateTimer();
	}

	private void updateTimer() {
		long seconds = ( TraderStock.msLeft() + 999 ) / 1000;
		if (seconds == shownSeconds) return;
		shownSeconds = seconds;
		timer.text( Messages.get( this, "timer", seconds / 60, seconds % 60 ) );
	}

	private void refresh() {
		gold.text( Messages.get( this, "gold", Big.fmt(Dungeon.gold) ) );
		energy.text( Messages.get( this, "energy", Big.fmt(Dungeon.energy) ) );

		content.clear();
		ArrayList<TraderStock.Offer> offers = TraderStock.offers();

		float bw = ( WIDTH - GAP ) / 2f;
		float cy = 0;
		for (int i = 0; i < offers.size(); i++) {
			final int index = i;
			final TraderStock.Offer offer = offers.get( i );
			final Item item = offer.item;

			ItemSlot slot = new ItemSlot( item ) {
				@Override
				public void onClick() {
					Game.scene().addToFront( new WndInfoItem( item ) );
				}
			};
			slot.setRect( 0, cy, ICON, ICON );
			content.add( slot );

			String label = Messages.titleCase( item.title() );
			RenderedTextBlock text = PixelScene.renderTextBlock( label, 6 );
			text.maxWidth( WIDTH - ICON - 2 );
			text.hardlight( offer.sold ? 0x808080 : 0xFFFFFF );
			text.setPos( ICON + 2, cy + ( ICON - text.height() ) / 2f );
			PixelScene.align( text );
			content.add( text );

			RedButton goldBtn = new RedButton( offer.sold ? Messages.get( this, "sold_out" )
					: Messages.get( this, "buy_gold", Big.fmt(offer.gold) ), 7 ) {
				@Override
				public void onClick() {
					buy( index, false );
				}
			};
			goldBtn.setRect( 0, cy + ICON, bw, BTN_H );
			goldBtn.enable( !offer.sold && (Dungeon.gold.compareTo(offer.gold) >= 0) );
			content.add( goldBtn );

			RedButton energyBtn = new RedButton( offer.sold ? Messages.get( this, "sold_out" )
					: Messages.get( this, "buy_energy", Big.fmt(offer.energy) ), 7 ) {
				@Override
				public void onClick() {
					buy( index, true );
				}
			};
			energyBtn.setRect( bw + GAP, cy + ICON, bw, BTN_H );
			energyBtn.enable( !offer.sold && (Dungeon.energy.compareTo(offer.energy) >= 0) );
			content.add( energyBtn );

			cy += ROW_H;
		}
		content.setSize( WIDTH, cy );
		pane.setSize( WIDTH, listH );
	}

	private void buy( int index, boolean withEnergy ) {
		String error = TraderStock.buy( Dungeon.hero, index, withEnergy );
		if (error != null) {
			Game.scene().addToFront( new WndMessage( Messages.get( WndTraderShop.class, error ) ) );
		} else {
			Sample.INSTANCE.play( Assets.Sounds.GOLD );
		}
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
