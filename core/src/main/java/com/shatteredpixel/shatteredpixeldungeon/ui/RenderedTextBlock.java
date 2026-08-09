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

package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.messages.Languages;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.Game;
import com.watabou.noosa.RenderedText;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class RenderedTextBlock extends Component {

	private int maxWidth = Integer.MAX_VALUE;
	public int nLines;

	private static final RenderedText SPACE = new RenderedText();
	private static final RenderedText NEWLINE = new RenderedText();

	protected String text;
	protected String[] tokens = null;
	protected ArrayList<RenderedText> words = new ArrayList<>();
	protected boolean multiline = false;

	private int size;
	private float zoom;
	private int color = -1;

	private int hightlightColor = Window.TITLE_COLOR;
	private boolean highlightingEnabled = true;

	private ArrayList<Integer> wordMarkupColor = new ArrayList<>();
	private ArrayList<Integer> wordMarkupAlpha = new ArrayList<>();
	private ArrayList<Integer> wordEffect = new ArrayList<>();
	private ArrayList<Integer> wordBaseColor = new ArrayList<>();
	private ArrayList<Integer> wordEffectSeed = new ArrayList<>();
	private ArrayList<Integer> wordGlintColor = new ArrayList<>();
	private float animTime = 0f;

	public static final int LEFT_ALIGN = 1;
	public static final int CENTER_ALIGN = 2;
	public static final int RIGHT_ALIGN = 3;
	private int alignment = LEFT_ALIGN;

	public RenderedTextBlock(int size){
		this.size = size;
	}

	public RenderedTextBlock(String text, int size){
		this.size = size;
		text(text);
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

	private int[] charColors = null;
	private int[] charAlphas = null;
	private int[] charEffects = null;
	private int[] charGlintColors = null;

	private static final int DEFAULT_GLINT_COLOR = 0x9d7bd8;

	private static final int EFFECT_RAINBOW = 1;
	private static final int EFFECT_GLINT   = 1 << 1;
	private static final int EFFECT_FLICKER = 1 << 2;

	private static final HashMap<String, Integer> EFFECT_FLAGS = new HashMap<>();
	static {
		EFFECT_FLAGS.put("RAINBOW", EFFECT_RAINBOW);
		EFFECT_FLAGS.put("GLINT",   EFFECT_GLINT);
		EFFECT_FLAGS.put("FLICKER", EFFECT_FLICKER);
	}

	private String parseMarkup(String raw){
		StringBuilder plain = new StringBuilder(raw.length());
		ArrayList<Integer> colors = new ArrayList<>(raw.length());
		ArrayList<Integer> alphas = new ArrayList<>(raw.length());
		ArrayList<Integer> effects = new ArrayList<>(raw.length());
		ArrayList<Integer> glintColors = new ArrayList<>(raw.length());

		ArrayList<int[]> stack = new ArrayList<>();
		stack.add(new int[]{-1, -1, 0, -1});

		int i = 0;
		int len = raw.length();
		while (i < len){
			char c = raw.charAt(i);
			char open = (c == '[') ? '[' : (c == '{') ? '{' : 0;

			if (open != 0){
				char closeChar = (open == '[') ? ']' : '}';

				if (i+1 < len && raw.charAt(i+1) == open){
					appendLiteral(open, plain, colors, alphas, effects, glintColors, stack);
					i += 2;
					continue;
				}

				int scan = i + 1;
				int closeIdx = -1;
				boolean hitOtherOpen = false;
				while (scan < len){
					char sc = raw.charAt(scan);
					if (sc == closeChar){
						closeIdx = scan;
						break;
					}
					if (sc == '[' || sc == '{'){
						hitOtherOpen = true;
						break;
					}
					scan++;
				}

				if (closeIdx == -1 || hitOtherOpen){
					appendLiteral(open, plain, colors, alphas, effects, glintColors, stack);
					i++;
					continue;
				}

				String tag = raw.substring(i+1, closeIdx);
				if (tag.isEmpty()){
					if (stack.size() > 1) stack.remove(stack.size()-1);
				} else {
					int[] cur = stack.get(stack.size()-1);
					int newColor = cur[0];
					int newAlpha = cur[1];
					int newEffect = cur.length > 2 ? cur[2] : 0;
					int newGlintColor = cur.length > 3 ? cur[3] : -1;
					boolean anyRecognized = false;
					boolean glintFlagInTag = false;
					int[] colorInTag = null;

					for (String part : tag.split("\\+")){
						part = part.trim();
						if (part.isEmpty()) continue;

						Integer effectFlag = EFFECT_FLAGS.get(part.toUpperCase(Locale.ROOT));
						if (effectFlag != null){
							newEffect |= effectFlag;
							anyRecognized = true;
							if (effectFlag == EFFECT_GLINT) glintFlagInTag = true;
						} else {
							int[] parsed = parseColorTag(part);
							if (parsed != null){
								colorInTag = parsed;
								anyRecognized = true;
							}
						}
					}

					if (colorInTag != null){
						if (glintFlagInTag){
							newGlintColor = colorInTag[0];
						} else {
							newColor = colorInTag[0];
							newAlpha = colorInTag[1];
						}
					}

					if (anyRecognized){
						stack.add(new int[]{ newColor, newAlpha, newEffect, newGlintColor });
					} else {
						//fully unrecognized - literal fallback, preserving original bracket style
						String literal = open + tag + closeChar;
						for (int k = 0; k < literal.length(); k++){
							plain.append(literal.charAt(k));
							colors.add(cur[0]);
							alphas.add(cur[1]);
							effects.add(cur.length > 2 ? cur[2] : 0);
							glintColors.add(cur.length > 3 ? cur[3] : -1);
						}
					}
				}
				i = closeIdx + 1;
				continue;
			}

			int[] cur = stack.get(stack.size()-1);
			plain.append(c);
			colors.add(cur[0]);
			alphas.add(cur[1]);
			effects.add(cur.length > 2 ? cur[2] : 0);
			glintColors.add(cur.length > 3 ? cur[3] : -1);
			i++;
		}

		charColors = new int[colors.size()];
		charAlphas = new int[alphas.size()];
		charEffects = new int[effects.size()];
		charGlintColors = new int[glintColors.size()];
		for (int k = 0; k < colors.size(); k++){
			charColors[k] = colors.get(k);
			charAlphas[k] = alphas.get(k);
			charEffects[k] = effects.get(k);
			charGlintColors[k] = glintColors.get(k);
		}

		return plain.toString();
	}

	private static void appendLiteral(char ch, StringBuilder plain, ArrayList<Integer> colors,
	                                  ArrayList<Integer> alphas, ArrayList<Integer> effects,
	                                  ArrayList<Integer> glintColors, ArrayList<int[]> stack){
		int[] cur = stack.get(stack.size()-1);
		plain.append(ch);
		colors.add(cur[0]);
		alphas.add(cur[1]);
		effects.add(cur.length > 2 ? cur[2] : 0);
		glintColors.add(cur.length > 3 ? cur[3] : -1);
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

	public void text(String text){
		this.text = text;

		if (text != null && !text.equals("")) {

			String stripped = parseMarkup(text);
			tokens = Game.platform.splitforTextBlock(stripped, multiline);

			build();
		}
	}

	//for manual text block splitting, a space between each word is assumed
	public void tokens(String... words){
		StringBuilder fullText = new StringBuilder();
		for (String word : words) {
			fullText.append(word);
		}
		text = fullText.toString();

		charColors = null;
		charAlphas = null;
		charEffects = null;
		charGlintColors = null;

		tokens = words;
		build();
	}

	public void text(String text, int maxWidth){
		this.maxWidth = maxWidth;
		multiline = true;
		text(text);
	}

	public String text(){
		return text;
	}

	public void maxWidth(int maxWidth){
		if (this.maxWidth != maxWidth){
			this.maxWidth = maxWidth;
			multiline = true;
			text(text);
		}
	}

	public int maxWidth(){
		return maxWidth;
	}

	private synchronized void build(){
		if (tokens == null) return;

		clear();
		words = new ArrayList<>();
		wordMarkupColor = new ArrayList<>();
		wordMarkupAlpha = new ArrayList<>();
		wordEffect = new ArrayList<>();
		wordBaseColor = new ArrayList<>();
		wordEffectSeed = new ArrayList<>();
		wordGlintColor = new ArrayList<>();
		boolean highlighting = false;
		int cursor = 0;
		int lastWordEffect = -1;
		int currentGroupSeed = 0;
		for (String str : tokens){

			if (str.equals("_") && highlightingEnabled){
				highlighting = !highlighting;
				cursor += str.length();
			} else if (str.equals("\n")){
				words.add(NEWLINE);
				wordMarkupColor.add(-1);
				wordMarkupAlpha.add(-1);
				wordEffect.add(0);
				wordBaseColor.add(-1);
				wordEffectSeed.add(0);
				wordGlintColor.add(-1);
				cursor += str.length();
			} else if (str.equals(" ")){
				words.add(SPACE);
				wordMarkupColor.add(-1);
				wordMarkupAlpha.add(-1);
				wordEffect.add(0);
				wordBaseColor.add(-1);
				wordEffectSeed.add(0);
				wordGlintColor.add(-1);
				cursor += str.length();
			} else {
				RenderedText word = new RenderedText(str, size);

				int markupColor = -1;
				int markupAlpha = -1;
				int effect = 0;
				int markupGlintColor = -1;
				if (charColors != null && cursor < charColors.length){
					markupColor = charColors[cursor];
					markupAlpha = charAlphas[cursor];
					effect = charEffects[cursor];
					markupGlintColor = charGlintColors[cursor];
				}

				if (effect != lastWordEffect){
					currentGroupSeed = words.size();
					lastWordEffect = effect;
				}

				int baseColor = markupColor != -1 ? markupColor : (color != -1 ? color : 0xffffff);

				if (effect == 0 && markupColor != -1)   word.hardlight(markupColor);
				else if (effect == 0 && highlighting)   word.hardlight(hightlightColor);
				else if (effect == 0 && color != -1)    word.hardlight(color);
				else if (effect != 0)                   word.hardlight(baseColor);

				if (markupAlpha != -1) word.alpha(markupAlpha/255f);

				word.scale.set(zoom);

				words.add(word);
				add(word);
				wordMarkupColor.add(markupColor);
				wordMarkupAlpha.add(markupAlpha);
				wordEffect.add(effect);
				wordBaseColor.add(baseColor);
				wordEffectSeed.add(currentGroupSeed);
				wordGlintColor.add(markupGlintColor);

				if (height < word.height()) height = word.height();

				cursor += str.length();
			}
		}
		layout();
	}

	public synchronized void zoom(float zoom){
		this.zoom = zoom;
		for (RenderedText word : words) {
			if (word != null) word.scale.set(zoom);
		}
		layout();
	}

	public synchronized void hardlight(int color){
		this.color = color;
		for (int i = 0; i < words.size(); i++) {
			RenderedText word = words.get(i);
			if (word != null && (wordMarkupColor.isEmpty() || wordMarkupColor.get(i) == -1)) {
				word.hardlight( color );
			}
		}
	}

	public synchronized void resetColor(){
		this.color = -1;
		for (int i = 0; i < words.size(); i++) {
			RenderedText word = words.get(i);
			if (word != null && (wordMarkupColor.isEmpty() || wordMarkupColor.get(i) == -1)) {
				word.resetColor();
			}
		}
	}

	public synchronized void alpha(float value){
		for (RenderedText word : words) {
			if (word != null) word.alpha( value );
		}
	}

	public synchronized void setHightlighting(boolean enabled){
		setHightlighting(enabled, Window.TITLE_COLOR);
	}

	public synchronized void setHightlighting(boolean enabled, int color){
		if (enabled != highlightingEnabled || color != hightlightColor) {
			hightlightColor = color;
			highlightingEnabled = enabled;
			build();
		}
	}

	public synchronized void invert(){
		if (words != null) {
			for (RenderedText word : words) {
				if (word != null) {
					word.ra = 0.77f;
					word.ga = 0.73f;
					word.ba = 0.62f;
					word.rm = -0.77f;
					word.gm = -0.73f;
					word.bm = -0.62f;
				}
			}
		}
	}

	public synchronized void align(int align){
		alignment = align;
		layout();
	}

	@Override
	public void update(){
		super.update();
		if (wordEffect.isEmpty()) return;

		animTime += Game.elapsed;

		for (int i = 0; i < words.size(); i++){
			int effect = wordEffect.get(i);
			if (effect == 0) continue;

			RenderedText word = words.get(i);
			if (word == null || word == SPACE || word == NEWLINE) continue;

			int seed = wordEffectSeed.get(i);
			float phase = animTime + seed * 0.15f;
			int base = wordBaseColor.get(i);

			if ((effect & EFFECT_RAINBOW) != 0){
				float hue = (phase * 0.5f) % 1f;
				word.hardlight( hsvToRgb(hue, 0.9f, 1f) );
			} else if ((effect & EFFECT_GLINT) != 0){
				RenderedText anchor = words.get(seed);
				float diagonal = (anchor.x - this.x) + (anchor.y - this.y) * 1.5f; //~45-degree axis

				float stripeScale = 0.5f; //how many stripes are visible across the text at once
				float layer1 = (float)Math.sin(diagonal * stripeScale        + animTime * 2.2f);
				float layer2 = (float)Math.sin(diagonal * stripeScale * 1.7f - animTime * 1.4f);
				float shimmer = (layer1 + layer2) * 0.5f + 0.5f;
				shimmer = Math.max(0f, Math.min(1f, shimmer));

				int glintColor = wordGlintColor.get(i) != -1 ? wordGlintColor.get(i) : DEFAULT_GLINT_COLOR;
				word.hardlight( lerpColor(base, glintColor, 0.25f + shimmer * 0.75f) );
			}

			if ((effect & EFFECT_FLICKER) != 0){
				float flicker = 0.6f + 0.4f * (float)Math.sin(phase * 9f + (seed*7 % 13));
				word.alpha( Math.max(0.3f, Math.min(1f, flicker)) );
			}
		}
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
	protected synchronized void layout() {
		super.layout();
		float x = this.x;
		float y = this.y;
		float height = 0;
		nLines = 1;

		ArrayList<ArrayList<RenderedText>> lines = new ArrayList<>();
		ArrayList<RenderedText> curLine = new ArrayList<>();
		lines.add(curLine);

		width = 0;
		for (int i = 0; i < words.size(); i++){
			RenderedText word = words.get(i);
			if (word == SPACE){
				x += 1.667f;
			} else if (word == NEWLINE) {
				//newline
				y += height+2f;
				x = this.x;
				nLines++;
				curLine = new ArrayList<>();
				lines.add(curLine);
			} else {
				if (word.height() > height) height = word.height();

				float fullWidth = word.width();
				int j = i+1;

				//this is so that words split only by highlighting are still grouped in layout
				//Chinese/Japanese always render every character separately without spaces however
				while (Messages.lang() != Languages.CHINESE
						&& j < words.size() && words.get(j) != SPACE && words.get(j) != NEWLINE){
					fullWidth += words.get(j).width() - 0.667f;
					j++;
				}

				if ((x - this.x) + fullWidth - 0.001f > maxWidth && !curLine.isEmpty()){
					y += height+2f;
					x = this.x;
					nLines++;
					curLine = new ArrayList<>();
					lines.add(curLine);
				}

				word.x = x;
				word.y = y;
				PixelScene.align(word);
				x += word.width();
				curLine.add(word);

				if ((x - this.x) > width) width = (x - this.x);

				//Note that spacing currently doesn't factor in halfwidth and fullwidth characters
				//(e.g. Ideographic full stop)
				x -= 0.667f;

			}
		}
		this.height = (y - this.y) + height;

		if (alignment != LEFT_ALIGN){
			for (ArrayList<RenderedText> line : lines){
				if (line.size() == 0) continue;
				float lineWidth = line.get(line.size()-1).width() + line.get(line.size()-1).x - this.x;
				if (alignment == CENTER_ALIGN){
					for (RenderedText text : line){
						text.x += (width() - lineWidth)/2f;
						PixelScene.align(text);
					}
				} else if (alignment == RIGHT_ALIGN) {
					for (RenderedText text : line){
						text.x += width() - lineWidth;
						PixelScene.align(text);
					}
				}
			}
		}
	}
}