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

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroAction;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DwarfKing;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.NPC;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Image;
import com.watabou.noosa.Visual;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class KillEvolution extends Buff implements ActionIndicator.Action {
	
	{
		//always acts after other buffs, so invisibility effects can process first
		actPriority = BUFF_PRIO - 1;
		type = buffType.POSITIVE;
	}
	
	public enum AttackLevel{
		LVL_1( 55, 1f, 1),
		LVL_2( 100, 2f, 3),
		LVL_3( 350, 4f, 8),
		LVL_4( 760, 8f, 16);

		final int totalKills;
		final float baseDmgBonus;
		final int damageRolls;
		
		AttackLevel( int kills, float base, int rolls){
            totalKills = kills;
			baseDmgBonus = base;
			damageRolls = rolls;
		}
		
		public long damageRoll( Char attacker ){
			long dmg = attacker.damageRoll();
			for( int i = 1; i < damageRolls; i++){
				long newDmg = attacker.damageRoll();
				if (newDmg > dmg) dmg = newDmg;
			}
			return Math.round(dmg * (1f + baseDmgBonus));
		}
		
		public static AttackLevel getLvl(int turnsInvis){
			List<AttackLevel> values = Arrays.asList(values());
			Collections.reverse(values);
			for ( AttackLevel lvl : values ){
				if (turnsInvis >= lvl.totalKills){
					return lvl;
				}
			}
			return LVL_1;
		}
	}
	
	public int internalKills = 0;
	
	@Override
	public boolean act() {
		if (target == Dungeon.hero){
			ActionIndicator.setAction(this);
			ActionIndicator.refresh();
		}
		spend(TICK);
		return true;
	}

	@Override
	public boolean usable() {
		return target == Dungeon.hero;
	}

    @Override
    public boolean isSelectable() {
        return ActionIndicator.Action.super.isSelectable();
    }

    @Override
	public void detach() {
		super.detach();
		ActionIndicator.clearAction(this);
	}

	public int attackLevel(){
		return AttackLevel.getLvl(internalKills).ordinal()+1;
	}

	public long damageRoll( Char attacker ){
		return AttackLevel.getLvl(internalKills).damageRoll(attacker);
	}
	
	@Override
	public int icon() {
		return BuffIndicator.PREPARATION;
	}
	
	@Override
	public void tintIcon(Image icon) {
		switch (AttackLevel.getLvl(internalKills)){
			case LVL_1:
				icon.hardlight(0f, 1f, 0f);
				break;
			case LVL_2:
				icon.hardlight(1f, 1f, 0f);
				break;
			case LVL_3:
				icon.hardlight(1f, 0.6f, 0f);
				break;
			case LVL_4:
				icon.hardlight(1f, 0f, 0f);
				break;
		}
	}

	@Override
	public String desc() {
		String desc = Messages.get(this, "desc");
		
		AttackLevel lvl = AttackLevel.getLvl(internalKills);

		desc += "\n\n" + Messages.get(this, "desc_dmg",
				(int)(lvl.baseDmgBonus*100));
		
		if (lvl.damageRolls > 1){
			desc += " " + Messages.get(this, "desc_dmg_likely");
		}
		
		desc += "\n\n" + Messages.get(this, "desc_invis_time", internalKills);
		
		if (lvl.ordinal() != AttackLevel.values().length-1){
			AttackLevel next = AttackLevel.values()[lvl.ordinal()+1];
			desc += "\n" + Messages.get(this, "desc_total_kills", next.totalKills);
		}
		
		return desc;
	}
	
	private static final String KILLS = "internalKills";
	
	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
        internalKills = bundle.getInt(KILLS);
		ActionIndicator.setAction(this);
	}
	
	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(KILLS, internalKills);
	}

	@Override
	public String actionName() {
		return Messages.get(this, "action_name");
	}

	@Override
	public int actionIcon() {
		return HeroIcon.PREPARATION;
	}

	@Override
	public Visual primaryVisual() {
		Image actionIco = new HeroIcon(this);
		tintIcon(actionIco);
		return actionIco;
	}

	@Override
	public Visual secondaryVisual() {
		BitmapText txt = new BitmapText(PixelScene.pixelFont);
		txt.text(Integer.toString(internalKills));
		txt.hardlight(CharSprite.POSITIVE);
		txt.measure();
		return txt;
	}

	@Override
	public int indicatorColor() {
		return 0x444444;
	}

    @Override
    public void doAction() {

    }
}
