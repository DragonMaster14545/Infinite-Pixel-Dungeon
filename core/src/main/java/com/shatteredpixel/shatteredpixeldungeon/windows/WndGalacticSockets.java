package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.EnchantmentRune;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Galactic;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Component;

public class WndGalacticSockets extends Window {

	private static final int WIDTH = 130;
	private static final int GAP = 2;
	private static final int ROW_H = 18;
	private static final int BTN_W = 40;
	private static final int BTN_H = 16;

	private final Weapon weapon;
	private final Galactic galactic;

	public WndGalacticSockets( Weapon weapon ) {
		super();
		this.weapon = weapon;
		this.galactic = (Galactic) weapon.enchantment;

		float y = 0;

		RenderedTextBlock title = PixelScene.renderTextBlock(
				Messages.get( this, "title", galactic.used(), galactic.capacity() ), 9 );
		title.hardlight( TITLE_COLOR );
		title.maxWidth( WIDTH );
		title.setPos( 0, y );
		add( title );
		y += title.height() + GAP;

		RenderedTextBlock info = PixelScene.renderTextBlock( Messages.get( this, "info" ), 6 );
		info.maxWidth( WIDTH );
		info.hardlight( 0xB0B0B0 );
		info.setPos( 0, y );
		add( info );
		y += info.height() + GAP * 2;

		int rows = galactic.capacity();
		float contentH = rows * ROW_H;
		float maxList = PixelScene.uiCamera.height - 60 - y - chrome.marginVer();
		float listH = Math.max( ROW_H * 2, Math.min( contentH, maxList ) );

		resize( WIDTH, (int) Math.ceil( y + listH ) );

		ScrollPane pane = new ScrollPane( new Component() );
		add( pane );
		pane.setRect( 0, y, WIDTH, listH );
		Component content = pane.content();
		content.clear();

		float cy = 0;
		for (int i = 0; i < rows; i++) {
			final int socket = i;
			boolean filled = i < galactic.used();

			String label;
			int color;
			if ( filled ) {
				Weapon.Enchantment e = galactic.sockets().get( i );
				label = Messages.titleCase( e.name( Messages.get( Galactic.class, "enchantment" ) ) );
				color = 0xFFFFFF;
			} else {
				label = Messages.get( this, "empty" );
				color = 0x808080;
			}

			RenderedTextBlock text = PixelScene.renderTextBlock( label, 7 );
			text.hardlight( color );
			text.maxWidth( WIDTH - BTN_W - GAP * 2 );
			text.setPos( GAP, cy + ( ROW_H - text.height() ) / 2f );
			PixelScene.align( text );
			content.add( text );

			RedButton button = new RedButton( Messages.get( this, filled ? "remove" : "insert" ), 7 ) {
				@Override
				public void onClick() {
					if ( socket < galactic.used() ) {
						WndGalacticSockets.this.remove( socket );
					} else {
						insert();
					}
				}
			};
			button.setRect( WIDTH - BTN_W, cy + ( ROW_H - BTN_H ) / 2f, BTN_W, BTN_H );
			content.add( button );

			cy += ROW_H;
		}
		content.setSize( WIDTH, cy );
		pane.setSize( WIDTH, listH );
	}

	private void reopen() {
		hide();
		if ( weapon.enchantment instanceof Galactic ) {
			GameScene.show( new WndGalacticSockets( weapon ) );
		}
	}

	private void remove( int socket ) {
		if ( Dungeon.hero == null || !Dungeon.hero.isAlive() ) return;

		Weapon.Enchantment e = galactic.remove( socket );
		if ( e == null ) return;
		weapon.enchant( galactic ); //refresh quickslot, sprite glow

		EnchantmentRune rune = EnchantmentRune.of( e );
		if ( !rune.collect( Dungeon.hero.belongings.backpack ) ) {
			Dungeon.level.drop( rune, Dungeon.hero.pos ).sprite.drop();
			GLog.w( Messages.get( this, "dropped" ) );
		}
		Sample.INSTANCE.play( Assets.Sounds.ITEM );
		reopen();
	}

	private void insert() {
		if ( Dungeon.hero == null || !Dungeon.hero.isAlive() ) return;

		GameScene.selectItem( new WndBag.ItemSelector() {
			@Override
			public String textPrompt() {
				return Messages.get( WndGalacticSockets.class, "pick" );
			}

			@Override
			public Class<? extends Bag> preferredBag() {
				return null;
			}

			@Override
			public boolean itemSelectable( Item item ) {
				return item instanceof EnchantmentRune
						&& galactic.canInsert( ( (EnchantmentRune) item ).enchantment() );
			}

			@Override
			public void onSelect( Item item ) {
				if ( item instanceof EnchantmentRune
						&& ( (EnchantmentRune) item ).insertInto( weapon, Dungeon.hero ) ) {
					item.detach( Dungeon.hero.belongings.backpack );
					Sample.INSTANCE.play( Assets.Sounds.READ );
				}

				GameScene.show( new WndGalacticSockets( weapon ) );
			}
		} );
		hide();
	}
}
