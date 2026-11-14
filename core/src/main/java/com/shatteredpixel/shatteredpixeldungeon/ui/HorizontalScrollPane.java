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

import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.input.PointerEvent;
import com.watabou.input.ScrollEvent;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.PointF;

public class HorizontalScrollPane extends ScrollPane {

	public HorizontalScrollPane( Component content ) {
		super( content );
	}

	private boolean dragged = false;

	public boolean dragged() {
		return dragged;
	}

	@Override
	protected void createChildren() {
		controller = new PointerController() {
			@Override
			protected void onPointerDown( PointerEvent event ) {
				dragged = false;
			}

			@Override
			protected void onDrag( PointerEvent event ) {
				super.onDrag( event );
				if (PointF.distance( event.current, event.start ) > PixelScene.defaultZoom * 8) {
					dragged = true;
				}
			}

			@Override
			protected void onScroll( ScrollEvent event ) {
				scrollTo( content.camera.scroll.x + event.amount * 12, 0 );
			}
		};
		add( controller );

		thumb = new ColorBlock( 1, 1, THUMB_COLOR );
		thumb.am = THUMB_ALPHA;
		add( thumb );
	}

	@Override
	public void scrollTo( float x, float y ) {
		super.scrollTo( x, 0 );
		updateThumb();
	}

	@Override
	protected void layout() {
		super.layout();
		updateThumb();
	}

	@Override
	public synchronized void update() {
		super.update();
		updateThumb();
	}

	private void updateThumb() {
		if (thumb == null || content == null || content.camera == null) return;

		boolean scrollable = width < content.width();
		thumb.visible = scrollable;
		if (scrollable) {
			thumb.scale.set( width * width / content.width(), 2 );
			thumb.x = x + width * content.camera.scroll.x / content.width();
			thumb.y = bottom() - 2;
		}
	}
}
