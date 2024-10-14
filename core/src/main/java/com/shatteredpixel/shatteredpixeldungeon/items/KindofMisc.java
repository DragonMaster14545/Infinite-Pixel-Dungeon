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

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.CheeseCheest;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public abstract class KindofMisc extends EquipableItem {

	public KindofMisc() {
		randomizeRarity();
	}

	@Override
	public boolean doEquip(final Hero hero) {

		if(Arrays.stream(hero.belongings.getMiscsArray()).anyMatch(misc -> misc.getClass() == this.getClass())) {
			return false;
		}
		boolean equipFull = false;
		if ( this instanceof Artifact && hero.belongings.artifacts.size() >= hero.belongings.artifactSlots() && hero.belongings.miscs.size() >= hero.belongings.miscSlots()) {
			//see if we can re-arrange items first
			boolean wasAbleToRearrange = false;
			if (hero.belongings.rings.size() < hero.belongings.ringSlots()) {
				for (KindofMisc misc : hero.belongings.miscs) {
					if(misc instanceof Ring) {
						hero.belongings.rings.add((Ring) misc);
						hero.belongings.miscs.remove(misc);
						wasAbleToRearrange = true;
						break;
					}
				}
			}
			if(!wasAbleToRearrange) {
				equipFull = true;
			}
		} else if (this instanceof Ring && hero.belongings.rings.size() >= hero.belongings.ringSlots() && hero.belongings.miscs.size() >= hero.belongings.miscSlots()) {
			//see if we can re-arrange items first
			boolean wasAbleToRearrange = false;
			if (hero.belongings.artifacts.size() < hero.belongings.artifactSlots()) {
				for (KindofMisc misc : hero.belongings.miscs) {
					if(misc instanceof Artifact) {
						hero.belongings.artifacts.add((Artifact) misc);
						hero.belongings.miscs.remove(misc);
						wasAbleToRearrange = true;
						break;
					}
				}
			}
			if(!wasAbleToRearrange) {
				equipFull = true;
			}
		}
		if (equipFull) {

			final KindofMisc[] miscs = hero.belongings.getMiscsArray();
			Boolean[] enabled = new Boolean[miscs.length];
            Arrays.fill(enabled, true);
			List<KindofMisc> miscList = new ArrayList<>();
			for (KindofMisc misc : miscs) {
				if (misc != null) {
					miscList.add(misc);
				}
			}
            String[] titles = miscList.stream().map(misc -> {
                return Messages.titleCase(misc.title());
            }).toArray(String[]::new);

			GameScene.show(
					new WndOptions(new ItemSprite(this),
							Messages.get(KindofMisc.class, "unequip_title"),
							Messages.get(KindofMisc.class, "unequip_message"),
							titles) {

						@Override
						protected void onSelect(int index) {

							KindofMisc equipped = miscs[index];
							Bag storage = Dungeon.hero.belongings.backpack;
							Item cheese = Dungeon.hero.belongings.getSimilar(new CheeseCheest());
							if (cheese != null){
								storage = (Bag) cheese;
							}
							//we directly remove the item because we want to have inventory capacity
							// to unequip the equipped one, but don't want to trigger any other
							// item detaching logic
							int slot = Dungeon.quickslot.getSlot(KindofMisc.this);
							slotOfUnequipped = -1;
							storage.items.remove(KindofMisc.this);
							if (equipped.doUnequip(hero, true, false)) {
//								//swap out equip in misc slot if needed
//								if (index < hero.belongings.ringSlots && KindofMisc.this instanceof Ring){
//									hero.belongings.artifacts = (Artifact)hero.belongings.miscs;
//									hero.belongings.miscs = null;
//								} else if (index == 2 && KindofMisc.this instanceof Artifact){
//									hero.belongings.rings = (Ring) hero.belongings.miscs;
//									hero.belongings.miscs = null;
//								}
								storage.items.add(KindofMisc.this);
								doEquip(hero);
							} else {
								storage.items.add(KindofMisc.this);
							}
							if (slot != -1) {
								Dungeon.quickslot.setSlot(slot, KindofMisc.this);
							} else if (slotOfUnequipped != -1 && defaultAction() != null){
								Dungeon.quickslot.setSlot(slotOfUnequipped, KindofMisc.this);
							}
							updateQuickslot();
						}

						@Override
						protected boolean enabled(int index) {
							return enabled[index];
						}
					});

			return false;

		} else {

			if (this instanceof Artifact){
				if (hero.belongings.artifacts.size() < hero.belongings.artifactSlots() )   hero.belongings.artifacts.add((Artifact) this);
				else                                    hero.belongings.miscs.add(this);
			} else if (this instanceof Ring){
				if (hero.belongings.rings.size() < hero.belongings.ringSlots() )  hero.belongings.rings.add((Ring) this);
				else                                hero.belongings.miscs.add(this);
			}

			detach( hero.belongings.backpack );

			Talent.onItemEquipped(hero, this);
			activate( hero );

			cursedKnown = true;
			if (cursed) {
				equipCursed( hero );
				GLog.n( Messages.get(this, "equip_cursed", this) );
			}

			hero.spendAndNext( timeToEquip(hero) );
			return true;

		}

	}

	@Override
	public boolean doUnequip(Hero hero, boolean collect, boolean single) {
		if (super.doUnequip(hero, collect, single)){

			if (hero.belongings.artifacts.contains(this)) {
				hero.belongings.artifacts.remove(this);
			} else if (hero.belongings.miscs.contains(this)) {
				hero.belongings.miscs.remove(this);
			} else if (hero.belongings.rings.contains(this)) {
				hero.belongings.rings.remove(this);
			}

			return true;

		} else {

			return false;

		}
	}

	@Override
	public boolean isEquipped( Hero hero ) {
		return hero != null && (hero.belongings.artifacts().contains(this)
				|| hero.belongings.miscs().contains(this)
				|| hero.belongings.rings().contains(this));
	}

}
