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

package com.watabou.noosa;

import com.badlogic.gdx.graphics.Pixmap;
import com.watabou.gltextures.SmartTexture;
import com.watabou.glwrap.Matrix;
import com.watabou.glwrap.Quad;
import com.watabou.glwrap.Vertexbuffer;
import com.watabou.utils.RectF;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class BitmapText extends Visual {

	protected String text;
	protected String plainText = "";
	protected Font font;

	protected float[] vertices = new float[16];
	protected FloatBuffer quads;
	protected Vertexbuffer buffer;
	
	public int realLength;
	
	protected boolean dirty = true;

	private int[] charColors = null;
	private int[] charAlphas = null;
	private int[] charEffects = null;
	private int[] charGlintColors = null;
	private float[] charX = null;
	private float[] charWidths = null;

	private ArrayList<ArrayList<ColorBlock>> charParticles = null;
	private ArrayList<ArrayList<float[]>> charParticleData = null;
	private static final int MAX_PARTICLES_PER_CHAR = 3;
	private static final float PARTICLE_LIFETIME = 0.9f;
	private static final float PARTICLE_SPAWN_INTERVAL = 0.25f;

	private boolean hasMarkup = false;
	private boolean hasEffects = false;
	private float animTime = 0f;

	private static final int EFFECT_RAINBOW = 1;
	private static final int EFFECT_GLINT   = 1 << 1;
	private static final int EFFECT_FLICKER = 1 << 2;
	private static final int EFFECT_PARTICLE = 1 << 3;

	private static final int DEFAULT_GLINT_COLOR = 0x9d7bd8;

	private static final HashMap<String, Integer> EFFECT_FLAGS = new HashMap<>();
	static {
		EFFECT_FLAGS.put("RAINBOW", EFFECT_RAINBOW);
		EFFECT_FLAGS.put("GLINT",   EFFECT_GLINT);
		EFFECT_FLAGS.put("FLICKER", EFFECT_FLICKER);
		EFFECT_FLAGS.put("PARTICLE", EFFECT_PARTICLE);
	}

	private static final HashMap<String, Integer> NAMED_COLORS = new HashMap<>();
	static {
		NAMED_COLORS.put("WHITE",      0xffffff);
		NAMED_COLORS.put("LIGHT_GRAY", 0xbfbfbf);
		NAMED_COLORS.put("GRAY",       0x7f7f7f);
		NAMED_COLORS.put("DARK_GRAY",  0x3f3f3f);
		NAMED_COLORS.put("BLACK",      0x000000);

		NAMED_COLORS.put("BLUE",       0x0000ff);
		NAMED_COLORS.put("NAVY",       0x000080);
		NAMED_COLORS.put("ROYAL",      0x4169e1);
		NAMED_COLORS.put("SLATE",      0x708090);
		NAMED_COLORS.put("SKY",        0x87ceeb);
		NAMED_COLORS.put("CYAN",       0x00ffff);
		NAMED_COLORS.put("TEAL",       0x008080);

		NAMED_COLORS.put("GREEN",      0x00ff00);
		NAMED_COLORS.put("CHARTREUSE", 0x7fff00);
		NAMED_COLORS.put("LIME",       0x32cd32);
		NAMED_COLORS.put("FOREST",     0x228b22);
		NAMED_COLORS.put("OLIVE",      0x6b8e23);

		NAMED_COLORS.put("YELLOW",     0xffff00);
		NAMED_COLORS.put("GOLD",       0xffd700);
		NAMED_COLORS.put("GOLDENROD",  0xdaa520);
		NAMED_COLORS.put("ORANGE",     0xffa500);

		NAMED_COLORS.put("BROWN",      0x8b4513);
		NAMED_COLORS.put("TAN",        0xd2b48c);
		NAMED_COLORS.put("FIREBRICK",  0xb22222);

		NAMED_COLORS.put("RED",        0xff0000);
		NAMED_COLORS.put("SCARLET",    0xff341c);
		NAMED_COLORS.put("CORAL",      0xff7f50);
		NAMED_COLORS.put("SALMON",     0xfa8072);
		NAMED_COLORS.put("PINK",       0xff69b4);
		NAMED_COLORS.put("MAGENTA",    0xff00ff);

		NAMED_COLORS.put("PURPLE",     0xa020f0);
		NAMED_COLORS.put("VIOLET",     0xee82ee);
		NAMED_COLORS.put("MAROON",     0xb03060);
	}

	public BitmapText() {
		this( "", null );
	}
	
	public BitmapText( Font font ) {
		this( "", font );
	}
	
	public BitmapText( String text, Font font ) {
		super( 0, 0, 0, 0 );

		this.font = font;
		this.text = text;
		this.plainText = parseMarkup( text == null ? "" : text );
	}

	@Override
	protected void updateMatrix() {
		// "origin" field is ignored
		Matrix.setIdentity( matrix );
		Matrix.translate( matrix, x, y );
		Matrix.scale( matrix, scale.x, scale.y );
		Matrix.rotate( matrix, angle );
	}

	@Override
	public void draw() {

		super.draw();

		if (dirty) {
			updateVertices();
			((Buffer)quads).limit(quads.position());
			if (buffer == null)
				buffer = new Vertexbuffer(quads);
			else
				buffer.updateVertices(quads);
		}
		
		NoosaScript script = NoosaScript.get();
		
		font.texture.bind();
		
		script.camera( camera() );
		
		script.uModel.valueM4( matrix );

		if (!hasMarkup || realLength == 0) {
			script.lighting(
					rm, gm, bm, am,
					ra, ga, ba, aa );
			script.drawQuadSet( buffer, realLength, 0 );
		} else {
			drawMarkupRuns( script );
		}

		if (hasEffects && charParticles != null) {
			//these dots aren't part of any scene-graph child list (BitmapText is a leaf
			//Visual, not a Group), so we draw them manually right here every frame
			for (ArrayList<ColorBlock> blocks : charParticles) {
				for (ColorBlock dot : blocks) {
					dot.draw();
				}
			}
		}

	}

	@Override
	public void update() {
		super.update();
		if (hasEffects) {
			animTime += Game.elapsed;
			if (charEffects != null) {
				for (int i = 0; i < charEffects.length; i++) {
					if ((charEffects[i] & EFFECT_PARTICLE) != 0) {
						updateParticles(i);
					}
				}
			}
		}
	}

	private void updateParticles(int i){
		ArrayList<ColorBlock> blocks = charParticles.get(i);
		ArrayList<float[]> data = charParticleData.get(i);

		float localPhase = animTime + i*0.11f;
		if (blocks.size() < MAX_PARTICLES_PER_CHAR
				&& (localPhase % PARTICLE_SPAWN_INTERVAL) < Game.elapsed){
			int accent = charGlintColors[i] != -1 ? charGlintColors[i]
					: (charColors[i] != -1 ? charColors[i] : 0xffffff);
			float size = 1f + (float)Math.random()*1.5f; //random size, ~1.0 to 2.5px
			ColorBlock dot = new ColorBlock(size, size, 0xFF000000 | (accent & 0xFFFFFF));
			blocks.add(dot);
			data.add(new float[]{
					(float)Math.random(),               //relX, 0..1 across this character's own width
					(float)Math.random(),               //relY, 0..1 across the text's height
					(float)(Math.random()-0.5f)*0.15f,  //slow horizontal drift
					0f                                   //age
			});
		}

		float charWidth = (charWidths != null && i < charWidths.length) ? charWidths[i] : 6f;

		for (int p = blocks.size()-1; p >= 0; p--){
			float[] d = data.get(p);
			d[3] += Game.elapsed;
			float lifeFrac = d[3] / PARTICLE_LIFETIME;

			if (lifeFrac >= 1f){
				blocks.remove(p);
				data.remove(p);
				continue;
			}

			ColorBlock dot = blocks.get(p);
			d[0] += d[2] * Game.elapsed;

			float relX = Math.max(0f, Math.min(1f, d[0]));
			float relY = d[1] + 0.15f*(float)Math.sin(lifeFrac*Math.PI*2 + p);
			relY = Math.max(0f, Math.min(1f, relY));

			dot.x = this.x + charX[i] + relX * charWidth;
			dot.y = this.y + relY * height;

			float alpha = lifeFrac < 0.2f ? lifeFrac/0.2f : (1f - lifeFrac)/0.8f;
			dot.alpha( Math.max(0f, Math.min(1f, alpha)) );
		}
	}

	private void drawMarkupRuns( NoosaScript script ) {

		float rm0=rm, gm0=gm, bm0=bm, am0=am;
		float ra0=ra, ga0=ga, ba0=ba, aa0=aa;

		int length = realLength;
		int i = 0;
		while (i < length) {
			int runStart = i;
			int runColor = charOverrideColor( i );
			float runAlpha = charOverrideAlpha( i );
			i++;
			while (i < length && charOverrideColor( i ) == runColor && charOverrideAlpha( i ) == runAlpha) {
				i++;
			}
			int runLength = i - runStart;

			if (runColor != -1) {
				hardlight( runColor );
			} else {
				rm=rm0; gm=gm0; bm=bm0; ra=ra0; ga=ga0; ba=ba0;
			}
			if (runAlpha != -1) {
				alpha( runAlpha );
			} else {
				am=am0; aa=aa0;
			}

			script.lighting( rm, gm, bm, am, ra, ga, ba, aa );
			script.drawQuadSet( buffer, runLength, runStart );
		}

		rm=rm0; gm=gm0; bm=bm0; am=am0;
		ra=ra0; ga=ga0; ba=ba0; aa=aa0;
	}

	private int charOverrideColor( int i ) {
		int effect = charEffects[i];
		if (effect != 0) {
			if ((effect & EFFECT_RAINBOW) != 0) {
				float hue = (animTime * 0.5f + i * 0.06f) % 1f;
				if (hue < 0) hue += 1f;
				return hsvToRgb( hue, 0.9f, 1f );
			} else if ((effect & EFFECT_GLINT) != 0) {
				float diagonal = charX[i];
				float stripeScale = 0.5f; //how many stripes are visible across the text at once

				float layer1 = (float)Math.sin( diagonal * stripeScale        + animTime * 2.2f );
				float layer2 = (float)Math.sin( diagonal * stripeScale * 1.7f - animTime * 1.4f );
				float shimmer = (layer1 + layer2) * 0.5f + 0.5f;
				shimmer = Math.max( 0f, Math.min( 1f, shimmer ) );

				int glintColor = charGlintColors[i] != -1 ? charGlintColors[i] : DEFAULT_GLINT_COLOR;
				int base = charColors[i] != -1 ? charColors[i] : 0xffffff;
				return lerpColor( base, glintColor, 0.25f + shimmer * 0.75f );
			}
		}
		return charColors[i];
	}

	private float charOverrideAlpha( int i ) {
		float result = charAlphas[i] != -1 ? charAlphas[i] / 255f : -1;
		if ((charEffects[i] & EFFECT_FLICKER) != 0) {
			float phase = animTime + i * 0.15f;
			float flicker = 0.6f + 0.4f * (float)Math.sin( phase * 9f + (i * 7 % 13) );
			result = Math.max( 0.3f, Math.min( 1f, flicker ) );
		}
		return result;
	}

	private static int hsvToRgb(float h, float s, float v){
		int i = (int)(h * 6f);
		float f = h * 6f - i;
		float p = v * (1f - s);
		float q = v * (1f - f * s);
		float t = v * (1f - (1f - f) * s);
		float r, g, b;
		switch (i % 6){
			case 0:  r = v; g = t; b = p; break;
			case 1:  r = q; g = v; b = p; break;
			case 2:  r = p; g = v; b = t; break;
			case 3:  r = p; g = q; b = v; break;
			case 4:  r = t; g = p; b = v; break;
			default: r = v; g = p; b = q; break;
		}
		return ((int)(r*255) << 16) | ((int)(g*255) << 8) | (int)(b*255);
	}

	private static int lerpColor(int a, int b, float t){
		int ar = (a >> 16) & 0xff, ag = (a >> 8) & 0xff, ab = a & 0xff;
		int br = (b >> 16) & 0xff, bg = (b >> 8) & 0xff, bb = b & 0xff;
		int r = (int)(ar + (br-ar)*t), g = (int)(ag + (bg-ag)*t), bl = (int)(ab + (bb-ab)*t);
		return (r << 16) | (g << 8) | bl;
	}

	@Override
	public void destroy() {
		super.destroy();
		if (buffer != null)
			buffer.delete();
	}

	protected synchronized void updateVertices() {

		width = 0;
		height = 0;

		if (plainText == null) {
			plainText = "";
		}

		int length = plainText.length();

		quads = Quad.createSet( length );
		realLength = 0;
		charX = new float[length];
		charWidths = new float[length];

		for (int i=0; i < length; i++) {
			RectF rect = font.get( plainText.charAt( i ) );

			float w = font.width( rect );
			float h = font.height( rect );

			charX[i] = width;
			charWidths[i] = w;

			vertices[0]     = width;
			vertices[1]     = 0;

			vertices[2]     = rect.left;
			vertices[3]     = rect.top;

			vertices[4]     = width + w;
			vertices[5]     = 0;

			vertices[6]     = rect.right;
			vertices[7]     = rect.top;

			vertices[8]     = width + w;
			vertices[9]     = h;

			vertices[10]    = rect.right;
			vertices[11]    = rect.bottom;

			vertices[12]    = width;
			vertices[13]    = h;

			vertices[14]    = rect.left;
			vertices[15]    = rect.bottom;

			quads.put( vertices );
			realLength++;

			width += w + font.tracking;
			if (h > height) {
				height = h;
			}
		}

		if (length > 0) {
			width -= font.tracking;
		}

		dirty = false;

	}

	public synchronized void measure() {

		width = 0;
		height = 0;

		if (plainText == null) {
			plainText = "";
		}

		int length = plainText.length();
		for (int i=0; i < length; i++) {
			RectF rect = font.get( plainText.charAt( i ) );

			float w = font.width( rect );
			float h = font.height( rect );

			width += w + font.tracking;
			if (h > height) {
				height = h;
			}
		}

		if (length > 0) {
			width -= font.tracking;
		}
	}

	public float baseLine() {
		return font.baseLine * scale.y;
	}

	public Font font() {
		return font;
	}

	public synchronized void font( Font value ) {
		font = value;
	}

	public String text() {
		return text;
	}

	public synchronized void text( String str ) {
		if (str == null || !str.equals(text)) {
			text = str;
			plainText = parseMarkup( str == null ? "" : str );
			dirty = true;
		}
	}

	private String parseMarkup( String raw ) {
		StringBuilder plain = new StringBuilder( raw.length() );
		ArrayList<Integer> colors = new ArrayList<>( raw.length() );
		ArrayList<Integer> alphas = new ArrayList<>( raw.length() );
		ArrayList<Integer> effects = new ArrayList<>( raw.length() );
		ArrayList<Integer> glints = new ArrayList<>( raw.length() );

		ArrayList<int[]> stack = new ArrayList<>();
		stack.add( new int[]{-1, -1, 0, -1} );
		boolean anyTagRecognized = false;

		int i = 0;
		int len = raw.length();
		while (i < len) {
			char c = raw.charAt(i);
			if (c == '[') {
				if (i+1 < len && raw.charAt(i+1) == '[') {
					int[] cur = stack.get(stack.size()-1);
					plain.append('[');
					colors.add(cur[0]); alphas.add(cur[1]); effects.add(cur[2]); glints.add(cur[3]);
					i += 2;
					continue;
				}

				int close = raw.indexOf(']', i);
				if (close == -1) {
					int[] cur = stack.get(stack.size()-1);
					for (int k = i; k < len; k++) {
						plain.append(raw.charAt(k));
						colors.add(cur[0]); alphas.add(cur[1]); effects.add(cur[2]); glints.add(cur[3]);
					}
					break;
				}

				String tag = raw.substring(i+1, close);
				if (tag.isEmpty()) {
					if (stack.size() > 1) stack.remove(stack.size()-1);
				} else {
					int[] cur = stack.get(stack.size()-1);
					int newColor = cur[0];
					int newAlpha = cur[1];
					int newEffect = cur[2];
					int newGlintColor = cur[3];
					boolean anyRecognized = false;
					boolean glintFlagInTag = false;
					int[] colorInTag = null;

					for (String part : tag.split("\\+")) {
						part = part.trim();
						if (part.isEmpty()) continue;

						Integer effectFlag = EFFECT_FLAGS.get(part.toUpperCase(Locale.ROOT));
						if (effectFlag != null) {
							newEffect |= effectFlag;
							anyRecognized = true;
							if (effectFlag == EFFECT_GLINT) glintFlagInTag = true;
						} else {
							int[] parsed = parseColorTag(part);
							if (parsed != null) {
								colorInTag = parsed;
								anyRecognized = true;
							}
						}
					}

					if (colorInTag != null) {
						if (glintFlagInTag) {
							newGlintColor = colorInTag[0];
						} else {
							newColor = colorInTag[0];
							newAlpha = colorInTag[1];
						}
					}

					if (anyRecognized) {
						anyTagRecognized = true;
						stack.add( new int[]{ newColor, newAlpha, newEffect, newGlintColor } );
					} else {
						//fully unrecognized - literal fallback
						String literal = "[" + tag + "]";
						for (int k = 0; k < literal.length(); k++) {
							plain.append(literal.charAt(k));
							colors.add(cur[0]); alphas.add(cur[1]); effects.add(cur[2]); glints.add(cur[3]);
						}
					}
				}
				i = close + 1;
				continue;
			}

			int[] cur = stack.get(stack.size()-1);
			plain.append(c);
			colors.add(cur[0]); alphas.add(cur[1]); effects.add(cur[2]); glints.add(cur[3]);
			i++;
		}

		int n = colors.size();
		charColors = new int[n];
		charAlphas = new int[n];
		charEffects = new int[n];
		charGlintColors = new int[n];
		boolean effectsFound = false;
		for (int k = 0; k < n; k++) {
			charColors[k] = colors.get(k);
			charAlphas[k] = alphas.get(k);
			charEffects[k] = effects.get(k);
			charGlintColors[k] = glints.get(k);
			if (charEffects[k] != 0) effectsFound = true;
		}
		hasMarkup = anyTagRecognized;
		hasEffects = effectsFound;

		charParticles = new ArrayList<>(n);
		charParticleData = new ArrayList<>(n);
		for (int k = 0; k < n; k++) {
			charParticles.add(new ArrayList<>());
			charParticleData.add(new ArrayList<>());
		}

		return plain.toString();
	}

	private static int[] parseColorTag(String tag){
		if (tag.startsWith("#")){
			String hex = tag.substring(1);
			try {
				if (hex.length() == 6){
					return new int[]{ Integer.parseInt(hex, 16), -1 };
				} else if (hex.length() == 8){
					int rgb = Integer.parseInt(hex.substring(0, 6), 16);
					int a = Integer.parseInt(hex.substring(6, 8), 16);
					return new int[]{ rgb, a };
				}
			} catch (NumberFormatException e){
				return null;
			}
			return null;
		} else {
			Integer rgb = NAMED_COLORS.get(tag.toUpperCase(Locale.ROOT));
			return rgb != null ? new int[]{ rgb, -1 } : null;
		}
	}

	public static class Font extends TextureFilm {

		public static final String LATIN_FULL =
			" !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u007F";
		
		public SmartTexture texture;
		
		public float tracking = 0;
		public float baseLine;
		
		public float lineHeight;
		
		protected Font( SmartTexture tx ) {
			super( tx );
			
			texture = tx;
		}
		
		public Font( SmartTexture tx, int width, String chars ) {
			this( tx, width, tx.height, chars );
		}
		
		public Font( SmartTexture tx, int width, int height, String chars ) {
			super( tx );
			
			texture = tx;
			
			int length = chars.length();
			
			float uw = (float)width / tx.width;
			float vh = (float)height / tx.height;
			
			float left = 0;
			float top = 0;
			float bottom = vh;
			
			for (int i=0; i < length; i++) {
				RectF rect = new RectF( left, top, left += uw, bottom );
				add( chars.charAt( i ), rect );
				if (left >= 1) {
					left = 0;
					top = bottom;
					bottom += vh;
				}
			}
			
			lineHeight = baseLine = height;
		}

		protected void splitBy( Pixmap bitmap, int height, int color, String chars ) {
			
			int length = chars.length();
			
			int width = bitmap.getWidth();
			float vHeight = (float)height / bitmap.getHeight();
			
			int pos;
			int line = 0;
			
		spaceMeasuring:
			for (pos=0; pos <  width; pos++) {
				for (int j=0; j < height; j++) {
					if (bitmap.getPixel( pos, j ) != color) {
						break spaceMeasuring;
					}
				}
			}
			add( ' ', new RectF( 0, 0, (float)pos / width, vHeight-0.01f ) );

			int separator = pos;
			
			for (int i=0; i < length; i++) {
				
				char ch = chars.charAt( i );
				if (ch == ' ') {
					continue;
				} else {

					boolean found;

					do{
						if (separator >= width) {
							line += height;
							separator = 0;
						}
						found = false;
						for (int j=line; j < line + height; j++) {
							if (colorNotMatch( bitmap, separator, j, color)) {
								found = true;
								break;
							}
						}
						if (!found) separator++;
					} while (!found);
					int start = separator;
					
					do {
						if (++separator >= width) {
							line += height;
							separator = start = 0;
							if (line + height >= bitmap.getHeight())
								break;
						}
						found = true;
						for (int j=line; j < line + height; j++) {
							if (colorNotMatch( bitmap, separator, j, color)) {
								found = false;
								break;
							}
						}
					} while (!found);
					
					add( ch, new RectF( (float)start / width, (float)line / bitmap.getHeight(), (float)separator / width, (float)line / bitmap.getHeight() + vHeight) );
					separator++;
				}
			}
			
			lineHeight = baseLine = height( frames.get( chars.charAt( 0 ) ) );
		}
		
		private boolean colorNotMatch(Pixmap pixmap, int x, int y, int color) {
			int pixel = pixmap.getPixel(x, y);
			if ((pixel & 0xFF) == 0) {
				return color != 0;
			}
			return pixel != color;
		}
		
		public static Font colorMarked( SmartTexture tex, int color, String chars ) {
			Font font = new Font( tex );
			font.splitBy( tex.bitmap, tex.height, color, chars );
			return font;
		}
		 
		public static Font colorMarked( SmartTexture tex, int height, int color, String chars ) {
			Font font = new Font( tex );
			font.splitBy( tex.bitmap, height, color, chars );
			return font;
		}
		
		public RectF get( char ch ) {
			if (frames.containsKey( ch )){
				return super.get( ch );
			} else {
				return super.get( '?' );
			}
		}
	}
}
