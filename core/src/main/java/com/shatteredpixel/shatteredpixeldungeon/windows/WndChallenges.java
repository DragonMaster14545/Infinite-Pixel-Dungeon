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

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;
import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.*;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WndChallenges extends Window {

	private final int WIDTH = 120;
	private static final int TTL_HEIGHT = 16;
	private static final int BTN_HEIGHT = 16;
	private static final int GAP = 1;

	private boolean editable;
	private ArrayList<IconButton> infos = new ArrayList<>();
	private ArrayList<ConduitBox> boxes;

	public WndChallenges(boolean[] checked, boolean editable) {
		super();

		this.editable = editable;

		int height = Math.min((Challenges.values().length + 1) * (BTN_HEIGHT + GAP),
				(int) (PixelScene.uiCamera.height * 0.9))/4*3;
		resize(WIDTH, height);

		RenderedTextBlock title = PixelScene.renderTextBlock(Messages.get(this, "title"), 12);
		title.hardlight(TITLE_COLOR);
		title.setPos(
				(WIDTH - title.width()) / 2,
				(TTL_HEIGHT - title.height()) / 2);
		PixelScene.align(title);
		add(title);

		boxes = new ArrayList<>();
		float pos = 2;
		int i = 0;

		ScrollPane pane = new ScrollPane(new Component()) {
			@Override
			public void onClick(float x, float y) {
				if (editable) {
					for (ConduitBox box : boxes) {
						if (box.onClick(x, y)) break;
					}
				}
				for (int j = 0; j < infos.size(); j++) {
					if (infos.get(j).inside(x, y)) {
						String challenge = Challenges.values()[j].nameId;
						ShatteredPixelDungeon.scene().add(
								new WndTitledMessage(Icons.get(Icons.CHALLENGE_ON),
										Messages.titleCase(Messages.get(Challenges.class, challenge)),
										Messages.get(Challenges.class, challenge + "_desc"))
						);
						break;
					}
				}
			}
		};
		add(pane);
		pane.setRect(0, title.bottom() + 2, WIDTH, height - title.bottom() - 2);
		Component content = pane.content();

		for (Challenges chal : Challenges.values()) {
			final String challenge = chal.nameId;
			String chaltitle = Messages.titleCase(Messages.get(Challenges.class, challenge));

			ConduitBox cb = new ConduitBox(chaltitle);
			cb.checked(checked[chal.ordinal()]);
			cb.active = editable;

			if (++i > 0) {
				pos += GAP;
			}
			cb.setRect(0, pos, WIDTH - 16, BTN_HEIGHT);

			content.add(cb);
			boxes.add(cb);

			IconButton info = new IconButton(Icons.get(Icons.INFO)) {
				@Override
				protected void layout() {
					super.layout();
					hotArea.y = -5000;
				}
			};
			info.setRect(cb.right(), pos, 16, BTN_HEIGHT);
			content.add(info);
			infos.add(info);

			pos = cb.bottom();
		}

		content.setSize(WIDTH, pos);
	}

	@Override
	public void onBackPressed() {
		if (editable) {
			boolean[] value = new boolean[Challenges.values().length];
			for (int i = 0; i < boxes.size(); i++) {
				value[i] = boxes.get(i).checked();
			}
			SPDSettings.challenges(value);
		}
		super.onBackPressed();
	}

	public class ConduitBox extends CheckBox {
		public ConduitBox(String label) {
			super(label);
		}

		@Override
		protected void onClick() {
			super.onClick();
		}

		protected boolean onClick(float x, float y) {
			if (!inside(x, y)) return false;
			Sample.INSTANCE.play(Assets.Sounds.CLICK);
			onClick();
			return true;
		}

		@Override
		protected void layout() {
			super.layout();
			hotArea.width = hotArea.height = 0;
		}
	}
}
