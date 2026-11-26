package com.shatteredpixel.shatteredpixeldungeon.scenes;

import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.ui.Component;

import java.math.BigInteger;

public class BattlePassUI {

	public static final int MARGIN = 8;
	public static final int GOLD = 0xFFD700;
	public static final int GREY = 0xCACFC2;
	public static final int GREEN = 0x9BFF9B;
	public static final int PURPLE = 0xB88CFF;
	public static final int CYAN = 0x66DDFF;

	public static class Panel extends Component {

		private ColorBlock border;
		private ColorBlock fill;
		private int fillRgb = 0xFFFFFF;
		private float fillAlpha = 0.1f;
		private int borderRgb = 0x000000;
		private float borderAlpha = 1f;

		@Override
		protected void createChildren() {
			border = new ColorBlock( 1, 1, 0xFFFFFFFF );
			add( border );
			fill = new ColorBlock( 1, 1, 0xFFFFFFFF );
			add( fill );
		}

		public Panel style( int fillRgb, float fillAlpha, int borderRgb, float borderAlpha ) {
			this.fillRgb = fillRgb;
			this.fillAlpha = fillAlpha;
			this.borderRgb = borderRgb;
			this.borderAlpha = borderAlpha;
			layout();
			return this;
		}

		@Override
		protected void layout() {
			border.x = x;
			border.y = y;
			border.size( width, height );
			border.hardlight( borderRgb );
			border.alpha( borderAlpha );
			fill.x = x + 1;
			fill.y = y + 1;
			fill.size( Math.max( 0, width - 2 ), Math.max( 0, height - 2 ) );
			fill.hardlight( fillRgb );
			fill.alpha( fillAlpha );
		}
	}

	public static class Bar extends Component {

		private Panel track;
		private ColorBlock bar;
		private RenderedTextBlock label;
		private float frac = 0f;
		private int barRgb = GOLD;
		private String text = "";
		@Override
		protected void createChildren() {
			track = new Panel().style( 0x000000, 0.55f, 0x000000, 1f );
			add( track );
			bar = new ColorBlock( 1, 1, 0xFFFFFFFF );
			add( bar );
			label = PixelScene.renderTextBlock( 8 );
			add( label );
		}

		public Bar set( float frac, String text, int rgb ) {
			this.frac = Math.max( 0f, Math.min( 1f, frac ) );
			this.text = text == null ? "" : text;
			this.barRgb = rgb;
			layout();
			return this;
		}

		@Override
		protected void layout() {
			if (track == null) return;
			track.setRect( x, y, width, height );
			bar.x = x + 1;
			bar.y = y + 1;
			bar.size( Math.max( 0, ( width - 2 ) * frac ), Math.max( 0, height - 2 ) );
			bar.hardlight( barRgb );
			bar.alpha( 0.85f );
			label.text( text );
			label.setPos( x + ( width - label.width() ) / 2f, y + ( height - label.height() ) / 2f );
		}
	}

	public static float frac( BigInteger num, BigInteger den ) {
		if (den == null || den.signum() <= 0) return 0f;
		if (num.compareTo( den ) >= 0) return 1f;
		return num.multiply( BigInteger.valueOf( 1000 ) ).divide( den ).intValue() / 1000f;
	}
}
