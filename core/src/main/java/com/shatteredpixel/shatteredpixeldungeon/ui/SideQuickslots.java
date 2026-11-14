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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.QuickSlot;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.ui.Component;

public class SideQuickslots extends Component {

	public static final String KEY_LEFT  = "side_quickslots_left";
	public static final String KEY_RIGHT = "side_quickslots_right";

	private static final int SLOT_W = 20;
	private static final int SLOT_H = 24;
	private static final int GAP = 1;
	private static final int TOP_MARGIN = 44;

	private static SideQuickslots instance;

	private Toolbar.QuickslotTool[] left;
	private Toolbar.QuickslotTool[] right;

	private Toolbar toolbar;
	private boolean lastEnabled = true;

	public static int leftCount() {
		return SPDSettings.getInt(KEY_LEFT, 0, 0, QuickSlot.SIDE_MAX);
	}

	public static void leftCount(int value) {
		SPDSettings.put(KEY_LEFT, Math.max(0, Math.min(QuickSlot.SIDE_MAX, value)));
		updateLayout();
	}

	public static int rightCount() {
		return SPDSettings.getInt(KEY_RIGHT, 0, 0, QuickSlot.SIDE_MAX);
	}

	public static void rightCount(int value) {
		SPDSettings.put(KEY_RIGHT, Math.max(0, Math.min(QuickSlot.SIDE_MAX, value)));
		updateLayout();
	}

	public static void updateLayout() {
		if (instance != null) instance.layoutSlots();
	}

	public SideQuickslots() {
		super();
		instance = this;
	}

	public void toolbar(Toolbar toolbar) {
		this.toolbar = toolbar;
		layoutSlots();
	}

	@Override
	protected void createChildren() {
		left = new Toolbar.QuickslotTool[QuickSlot.SIDE_MAX];
		right = new Toolbar.QuickslotTool[QuickSlot.SIDE_MAX];
		for (int i = 0; i < QuickSlot.SIDE_MAX; i++) {
			left[i] = new Toolbar.QuickslotTool(86, 0, SLOT_W, SLOT_H, QuickSlot.TOOLBAR_SIZE + i);
			left[i].border(2, 1);
			add(left[i]);
			right[i] = new Toolbar.QuickslotTool(86, 0, SLOT_W, SLOT_H, QuickSlot.TOOLBAR_SIZE + QuickSlot.SIDE_MAX + i);
			right[i].border(2, 1);
			add(right[i]);
		}
	}

	@Override
	public synchronized void destroy() {
		super.destroy();
		if (instance == this) instance = null;
	}

	public void layoutSlots() {
		if (toolbar == null || PixelScene.uiCamera == null) return;

		float camW = PixelScene.uiCamera.width;
		float camH = PixelScene.uiCamera.height;

		float bottom = toolbar.top() - 2 - (toolbar.hasExtraQuickslotRow() ? SLOT_H : 0);
		float avail = bottom - TOP_MARGIN;
		int fit = Math.max(0, (int) (avail / (SLOT_H + GAP)));

		placeColumn(left, Math.min(leftCount(), fit), 0, TOP_MARGIN, avail, camH);
		placeColumn(right, Math.min(rightCount(), fit), camW - SLOT_W, TOP_MARGIN, avail, camH);
	}

	private void placeColumn(Toolbar.QuickslotTool[] column, int shown, float x, float top, float avail, float camH) {
		float used = shown * (SLOT_H + GAP) - GAP;
		float y = top;
		for (int i = 0; i < column.length; i++) {
			Toolbar.QuickslotTool tool = column[i];
			boolean show = i < shown;
			tool.visible = show;
			if (show) {
				tool.setPos(x, y);
				y += SLOT_H + GAP;
			} else {
				tool.setPos(x, camH);
			}
			tool.enable(show && lastEnabled);
		}
	}

	@Override
	public void update() {
		super.update();

		if (toolbar != null) {
			boolean show = toolbar.visible;
			if (visible != show) visible = active = show;
		}

		if (Dungeon.hero == null) return;
		boolean enabled = Dungeon.hero.ready && Dungeon.hero.isAlive();
		if (enabled != lastEnabled) {
			lastEnabled = enabled;
			layoutSlots();
		}
	}

	public void alpha(float value) {
		for (int i = 0; i < left.length; i++) {
			left[i].alpha(value);
			right[i].alpha(value);
		}
	}
}
