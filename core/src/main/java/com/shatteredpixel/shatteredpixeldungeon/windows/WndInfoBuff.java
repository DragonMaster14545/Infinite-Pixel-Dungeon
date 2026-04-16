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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.ExaminationParchment;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.*;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;

public class WndInfoBuff extends Window {

	private static final float GAP	= 2;

	private static final int WIDTH = 120;

	public WndInfoBuff(Buff buff){
		super();

		IconTitle titlebar = new IconTitle();

		Image buffIcon = new BuffIcon( buff, true );

		titlebar.icon( buffIcon );
		titlebar.label( Messages.titleCase(buff.name()), Window.TITLE_COLOR );
		titlebar.setRect( 0, 0, WIDTH, 0 );
		add( titlebar );

		RenderedTextBlock txtInfo = PixelScene.renderTextBlock(buff.desc(), 6);
		txtInfo.maxWidth(WIDTH);
		txtInfo.setPos(titlebar.left(), titlebar.bottom() + 2*GAP);
		add( txtInfo );

		float bottom = txtInfo.bottom()+2;

		if(buff instanceof ActionIndicator.Action && ((ActionIndicator.Action)buff).isSelectable()) {
			RedButton button = new RedButton("Set Active") {
				@Override
				protected void onClick() {
					hide();
					ActionIndicator.setAction((ActionIndicator.Action)buff);
				}
			};
			button.setRect(0,bottom+2,WIDTH,16);
			bottom = button.bottom()+2;
			add(button);
		}

        final ExaminationParchment.questionnaireEnergy questionnaireEnergy = Dungeon.hero.buff(ExaminationParchment.questionnaireEnergy.class);
        if (questionnaireEnergy != null && !questionnaireEnergy.isCursed() && questionnaireEnergy.chargesToUseInbuff(buff) > 0) {
            final int chargesToUse = questionnaireEnergy.chargesToUseInbuff(buff);
            RedButton btnRemoveBuffquest = new RedButton(Messages.get(this, "quest_remove_buff", chargesToUse, questionnaireEnergy.availableEnergy()), 6) {
                @Override
                protected void onClick() {
                    if (chargesToUse <= questionnaireEnergy.availableEnergy()){
                        questionnaireEnergy.removeBuff(buff);
                        hide();
                    } else {
                        GLog.w(Messages.get(WndInfoBuff.class, "no_charge_quest", chargesToUse));
                    }
                }
            };
            btnRemoveBuffquest.setRect(0, bottom + 2, WIDTH, 16);
            bottom = btnRemoveBuffquest.bottom()+2;
            btnRemoveBuffquest.icon(new ItemSprite(ItemSpriteSheet.EXAM_PARCHMENT));
            add(btnRemoveBuffquest);
        }

		resize( WIDTH, (int)bottom );
	}
}
