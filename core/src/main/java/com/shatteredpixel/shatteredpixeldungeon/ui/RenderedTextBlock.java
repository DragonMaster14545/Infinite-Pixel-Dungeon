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

	private String parseMarkup(String raw){
		StringBuilder plain = new StringBuilder(raw.length());
		ArrayList<Integer> colors = new ArrayList<>(raw.length());
		ArrayList<Integer> alphas = new ArrayList<>(raw.length());

		ArrayList<int[]> stack = new ArrayList<>();
		stack.add(new int[]{-1, -1});

		int i = 0;
		int len = raw.length();
		while (i < len){
			char c = raw.charAt(i);
			if (c == '['){
				if (i+1 < len && raw.charAt(i+1) == '['){
					int[] cur = stack.get(stack.size()-1);
					plain.append('[');
					colors.add(cur[0]);
					alphas.add(cur[1]);
					i += 2;
					continue;
				}

				int close = raw.indexOf(']', i);
				if (close == -1){
					int[] cur = stack.get(stack.size()-1);
					for (int k = i; k < len; k++){
						plain.append(raw.charAt(k));
						colors.add(cur[0]);
						alphas.add(cur[1]);
					}
					break;
				}

				String tag = raw.substring(i+1, close);
				if (tag.isEmpty()){
					if (stack.size() > 1) stack.remove(stack.size()-1);
				} else {
					int[] parsed = parseColorTag(tag);
					if (parsed != null){
						stack.add(parsed);
					} else {
						int[] cur = stack.get(stack.size()-1);
						String literal = "[" + tag + "]";
						for (int k = 0; k < literal.length(); k++){
							plain.append(literal.charAt(k));
							colors.add(cur[0]);
							alphas.add(cur[1]);
						}
					}
				}
				i = close + 1;
				continue;
			}

			int[] cur = stack.get(stack.size()-1);
			plain.append(c);
			colors.add(cur[0]);
			alphas.add(cur[1]);
			i++;
		}

		charColors = new int[colors.size()];
		charAlphas = new int[alphas.size()];
		for (int k = 0; k < colors.size(); k++){
			charColors[k] = colors.get(k);
			charAlphas[k] = alphas.get(k);
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
		boolean highlighting = false;
		int cursor = 0;
		for (String str : tokens){

			if (str.equals("_") && highlightingEnabled){
				highlighting = !highlighting;
				cursor += str.length();
			} else if (str.equals("\n")){
				words.add(NEWLINE);
				wordMarkupColor.add(-1);
				wordMarkupAlpha.add(-1);
				cursor += str.length();
			} else if (str.equals(" ")){
				words.add(SPACE);
				wordMarkupColor.add(-1);
				wordMarkupAlpha.add(-1);
				cursor += str.length();
			} else {
				RenderedText word = new RenderedText(str, size);

				int markupColor = -1;
				int markupAlpha = -1;
				if (charColors != null && cursor < charColors.length){
					markupColor = charColors[cursor];
					markupAlpha = charAlphas[cursor];
				}

				if (markupColor != -1)         word.hardlight(markupColor);
				else if (highlighting)         word.hardlight(hightlightColor);
				else if (color != -1)          word.hardlight(color);

				if (markupAlpha != -1)         word.alpha(markupAlpha/255f);

				word.scale.set(zoom);

				words.add(word);
				add(word);
				wordMarkupColor.add(markupColor);
				wordMarkupAlpha.add(markupAlpha);

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
