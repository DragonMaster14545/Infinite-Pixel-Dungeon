/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;



import com.shatteredpixel.shatteredpixeldungeon.utils.Big;
import java.math.BigInteger;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class WarScythe extends MeleeWeapon {

    {
        image = ItemSpriteSheet.WAR_SCYTHE;
        hitSound = Assets.Sounds.HIT_SLASH;
        hitSoundPitch = 0.9f;

        internalTier = tier = 5;
        ACC = 0.8f; //20% penalty to accuracy
    }

    @Override
    public BigInteger max(BigInteger lvl) {
        return  BigInteger.valueOf(Math.round(6.67d*(tier()+1))).add(lvl.multiply(BigInteger.valueOf((tier()+1))));                   //scaling unchanged
    }

    @Override
    public String targetingPrompt() {
        return Messages.get(this, "prompt");
    }

	@Override
	protected void duelistAbility(Hero hero, Integer target) {
		//replaces damage with 30+4.5*lvl bleed, roughly 133% avg base dmg, 129% avg scaling
		BigInteger bleedAmt = augment.damageFactor(Big.round(Big.dec(30f).add(Big.dec(4.5f).multiply(Big.dec(buffedLvl())))));
		Sickle.harvestAbility(hero, target, 0f, bleedAmt, this);
	}

	@Override
	public String abilityInfo() {
		BigInteger bleedAmt = levelKnown ? Big.round(Big.dec(30f).add(Big.dec(4.5f).multiply(Big.dec(buffedLvl())))) : BigInteger.valueOf(30);
		if (levelKnown){
			return Messages.get(this, "ability_desc", augment.damageFactor(bleedAmt));
		} else {
			return Messages.get(this, "typical_ability_desc", bleedAmt);
		}
    }

	@Override
	public String upgradeAbilityStat(BigInteger level) {
		return augment.damageFactor(Big.round(Big.dec(30f).add(Big.dec(4.5f).multiply(Big.dec(level))))).toString();
	}

}