/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2019 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.levels.traps;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Bones;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CounterBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Overload;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.RageShield;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Stamina;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.MobSpawner;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Shopkeeper;
import com.shatteredpixel.shatteredpixeldungeon.items.Ankh;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Torch;
import com.shatteredpixel.shatteredpixeldungeon.items.emblem.CommonEmblem;
import com.shatteredpixel.shatteredpixeldungeon.items.emblem.EpicEmblem;
import com.shatteredpixel.shatteredpixeldungeon.items.emblem.LaserisedEmblem;
import com.shatteredpixel.shatteredpixeldungeon.items.emblem.RareEmblem;
import com.shatteredpixel.shatteredpixeldungeon.items.emblem.SummonerEmblem;
import com.shatteredpixel.shatteredpixeldungeon.items.emblem.TrihitEmblem;
import com.shatteredpixel.shatteredpixeldungeon.items.emblem.UncommonEmblem;
import com.shatteredpixel.shatteredpixeldungeon.items.food.SmallRation;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfAquaticRejuvenation;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.ExoticPotion;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfIdentify;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfLullaby;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMirrorImage;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfPower;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRage;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRecharging;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRetribution;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTerror;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTransmutation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ExoticScroll;
import com.shatteredpixel.shatteredpixeldungeon.items.treasurebags.BiggerGambleBag;
import com.shatteredpixel.shatteredpixeldungeon.items.treasurebags.GambleBag;
import com.shatteredpixel.shatteredpixeldungeon.items.treasurebags.QualityBag;
import com.shatteredpixel.shatteredpixeldungeon.levels.CityLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Patch;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.ArenaShopLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.PortableShop;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTileSheet;
import com.watabou.noosa.Group;
import com.watabou.noosa.audio.Music;
import com.watabou.utils.Bundle;
import com.watabou.utils.Point;
import com.watabou.utils.Random;
import com.watabou.utils.Rect;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

public class PortableShopLevel extends Level {

    // this whole level is practically hardcoded, improving it the next updates.
	{
		color1 = 0x534f3e;
		color2 = 0xb9d661;
	}

	@Override
	public void playLevelMusic() {
		Music.INSTANCE.play(Assets.Music.CITY_1, true);
	}

	private static final int WIDTH = 48;
	private static final int HEIGHT = 48;

	private static final int ROOM_LEFT		= WIDTH / 2 - 12;
	private static final int ROOM_RIGHT		= WIDTH / 2 + 4;
	private static final int ROOM_TOP		= HEIGHT / 2 - 8;
	private static final int ROOM_BOTTOM	= HEIGHT / 2 + 8;
	
	private int arenaDoor;
	private boolean enteredArena = false;
	private boolean keyDropped = false;
	private PortableShop shop;
	
	@Override
	public String tilesTex() {
		return Assets.Environment.TILES_CITY;
	}
	
	@Override
	public String waterTex() {
		return Assets.Environment.WATER_CITY;
	}
	
	private static final String DOOR	= "door";
	private static final String ENTERED	= "entered";
	private static final String DROPPED	= "droppped";
	private static final String SHOP = "shop";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( DOOR, arenaDoor );
		bundle.put( ENTERED, enteredArena );
		bundle.put( DROPPED, keyDropped );
		bundle.put( SHOP, shop);
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		arenaDoor = bundle.getInt( DOOR );
		enteredArena = bundle.getBoolean( ENTERED );
		keyDropped = bundle.getBoolean( DROPPED );
		shop = (PortableShop) bundle.get(SHOP);
	}
	
	@Override
	protected boolean build() {
		
		setSize(WIDTH, HEIGHT);

		Rect space = new Rect();

		space.set(
				Random.IntRange(2, 6),
				Random.IntRange(2, 6),
				Random.IntRange(width-6, width-2),
				Random.IntRange(height-6, height-2)
		);

		Painter.fill( this, space, Terrain.EMPTY );

		exit = space.left + space.width()/2 + (space.top - 1) * width();
		
		map[exit] = Terrain.WALL_DECO;

		Painter.fill( this, ROOM_LEFT - 1, ROOM_TOP - 1,
			ROOM_RIGHT - ROOM_LEFT + 3, ROOM_BOTTOM - ROOM_TOP + 3, Terrain.WALL );
		Painter.fill( this, ROOM_LEFT, ROOM_TOP + 1,
			ROOM_RIGHT - ROOM_LEFT + 1, ROOM_BOTTOM - ROOM_TOP, Terrain.EMPTY );

		Painter.fill( this, ROOM_LEFT, ROOM_TOP,
			ROOM_RIGHT - ROOM_LEFT + 1, 1, Terrain.EMPTY_DECO );
		shop = new PortableShop();
		shop.set(ROOM_LEFT-1, ROOM_TOP-1, ROOM_RIGHT+1, ROOM_BOTTOM+1);
		
		arenaDoor = Random.Int( ROOM_LEFT, ROOM_RIGHT ) + (ROOM_BOTTOM + 1) * width();
		map[arenaDoor] = Terrain.DOOR;
		
		int entrance = Random.Int( ROOM_LEFT + 1, ROOM_RIGHT - 1 ) +
			Random.Int( ROOM_TOP + 1, ROOM_BOTTOM - 1 ) * width();
		transitions.add(new LevelTransition(this, entrance, LevelTransition.Type.REGULAR_ENTRANCE));
		map[entrance] = Terrain.PEDESTAL;

		{
			itemsToSpawn = new ArrayList<>();
            for (int i = 0; i < 2; i++) itemsToSpawn.add(new GambleBag());
            for (int i = 0; i < 2; i++) itemsToSpawn.add(new BiggerGambleBag());
            for (int i = 0; i < 2; i++) itemsToSpawn.add(new QualityBag());
			for (int i = 0; i < 2; i++) itemsToSpawn.add(new Ankh());
			for (int i = 0; i < 2; i++) itemsToSpawn.add(new Torch().quantity(Random.IntRange(2, 5)));
            itemsToSpawn.add(new CommonEmblem().quantity(Random.IntRange(2, 4)));
            itemsToSpawn.add(new UncommonEmblem().quantity(Random.IntRange(2, 4)));
            itemsToSpawn.add(new RareEmblem().quantity(Random.IntRange(2, 4)));
            itemsToSpawn.add(new EpicEmblem().quantity(Random.IntRange(2, 4)));

			// TODO make this an optimized code.
			for (int i = 0; i < 48; i++) {
				if (Random.Int(2) == 0) {
					itemsToSpawn.add( Generator.random(Generator.Category.SCROLL).quantity(Random.IntRange(2, 10)) );
				} else {
					itemsToSpawn.add( Reflection.newInstance(ExoticScroll.regToExo.get(Generator.random(Generator.Category.SCROLL).getClass())).quantity(Random.IntRange(2, 10)) );
				}
			}

			for (int i = 0; i < 48; i++) {
				if (Random.Int(2) == 0) {
					itemsToSpawn.add( Generator.random(Generator.Category.POTION).quantity(Random.IntRange(2, 10)) );
				} else {
					itemsToSpawn.add( Reflection.newInstance(ExoticPotion.regToExo.get(Generator.random(Generator.Category.POTION).getClass())).quantity(Random.IntRange(2, 10)) );
				}
			}

			for (int i = 0; i < 24; i++) {
				Item item = Generator.randomWeapon();
				itemsToSpawn.add( item.upgrade(Random.IntRange(2, 10)) );
			}

			for (int i = 0; i < 14; i++) {
				Item item = Generator.random(Generator.Category.TUBES);
				itemsToSpawn.add( item.quantity(Random.IntRange(2, 10)) );
			}

			for (int i = 0; i < 5; i++) {
				Item item = Generator.random(Generator.Category.FOOD);
				itemsToSpawn.add( item.quantity(Random.IntRange(2, 5)) );
			}

			for (int i = 0; i < 26; i++) {
				Item item = Generator.random(Generator.Category.STONE);
				itemsToSpawn.add( item.quantity(Random.IntRange(2, 5)) );
			}

			for (int i = 0; i < 28; i++) {
				Item item = Generator.random(Generator.Category.WAND);
				itemsToSpawn.add( item.upgrade(Random.IntRange(2, 10)) );
			}

			for (int i = 0; i < 26; i++) {
				Item item = Generator.random(Generator.Category.RING);
				itemsToSpawn.add( item.upgrade(Random.IntRange(2, 10)) );
			}

			Point itemPlacement = new Point(cellToPoint(arenaDoor));
			if (itemPlacement.y == ROOM_TOP-1){
				itemPlacement.y++;
			} else if (itemPlacement.y == ROOM_BOTTOM+1) {
				itemPlacement.y--;
			} else if (itemPlacement.x == ROOM_LEFT-1){
				itemPlacement.x++;
			} else {
				itemPlacement.x--;
			}

			for (Item item : itemsToSpawn) {

				if (itemPlacement.x == ROOM_LEFT && itemPlacement.y != ROOM_TOP){
					itemPlacement.y--;
				} else if (itemPlacement.y == ROOM_TOP && itemPlacement.x != ROOM_RIGHT){
					itemPlacement.x++;
				} else if (itemPlacement.x == ROOM_RIGHT && itemPlacement.y != ROOM_BOTTOM){
					itemPlacement.y++;
				} else {
					itemPlacement.x--;
				}

				int cell = pointToCell(itemPlacement);



				if (heaps.get( cell ) != null) {
					do {
						cell = pointToCell(new Point( Random.IntRange( ROOM_TOP, ROOM_RIGHT ),
								Random.IntRange( ROOM_TOP, ROOM_BOTTOM )));
					} while (heaps.get( cell ) != null || findMob( cell ) != null);
				}

				drop( item, cell ).type = Heap.Type.FOR_SALE;
			}
		}
		
		boolean[] patch = Patch.generate( width, height, 0.30f, 6, true );
		for (int i=0; i < length(); i++) {
			if (map[i] == Terrain.EMPTY && patch[i]) {
				map[i] = Terrain.WATER;
			}
		}

		for (int i=0; i < length(); i++) {
			if (map[i] == Terrain.EMPTY && Random.Int( 6 ) == 0) {
				map[i] = Terrain.INACTIVE_TRAP;
				Trap t = new SummoningTrap().reveal();
				t.active = false;
				setTrap(t, i);
			}
		}
		
		for (int i=width() + 1; i < length() - width(); i++) {
			if (map[i] == Terrain.EMPTY) {
				int n = 0;
				if (map[i+1] == Terrain.WALL) {
					n++;
				}
				if (map[i-1] == Terrain.WALL) {
					n++;
				}
				if (map[i+width()] == Terrain.WALL) {
					n++;
				}
				if (map[i-width()] == Terrain.WALL) {
					n++;
				}
				if (Random.Int( 8 ) <= n) {
					map[i] = Terrain.EMPTY_DECO;
				}
			}
		}
		
		for (int i=0; i < length() - width(); i++) {
			if (map[i] == Terrain.WALL
					&& DungeonTileSheet.floorTile(map[i + width()])
					&& Random.Int( 3 ) == 0) {
				map[i] = Terrain.WALL_DECO;
			}
		}

		
		return true;
	}

    @Override
    public Mob createMob() {
        return null;
    }

    @Override
    protected void createMobs() {
    }

    public Actor addRespawner() {
        return null;
    }
	
	@Override
	protected void createItems() {
		Random.pushGenerator(Random.Long());
		ArrayList<Item> bonesItems = Bones.get();
		if (bonesItems != null) {
			int pos;
			do {
				pos = randomRespawnCell(null);
			} while (pos == entrance());
			for (Item i : bonesItems) {
				drop(i, pos).setHauntedIfCursed().type = Heap.Type.REMAINS;
			}
		}
		Random.popGenerator();
	}
	
	@Override
	public String tileName( int tile ) {
		switch (tile) {
			case Terrain.GRASS:
				return Messages.get(CityLevel.class, "grass_name");
			case Terrain.HIGH_GRASS:
				return Messages.get(CityLevel.class, "high_grass_name");
			case Terrain.WATER:
				return Messages.get(CityLevel.class, "water_name");
			default:
				return super.tileName( tile );
		}
	}
	
	@Override
	public String tileDesc( int tile ) {
		switch (tile) {
			case Terrain.ENTRANCE:
				return Messages.get(CityLevel.class, "entrance_desc");
			case Terrain.EXIT:
				return Messages.get(CityLevel.class, "exit_desc");
			case Terrain.HIGH_GRASS:
				return Messages.get(CityLevel.class, "high_grass_desc");
			case Terrain.WALL_DECO:
				return Messages.get(CityLevel.class, "wall_deco_desc");
			case Terrain.BOOKSHELF:
				return Messages.get(CityLevel.class, "bookshelf_desc");
			default:
				return super.tileDesc( tile );
		}
	}

	@Override
	public Group addVisuals() {
		super.addVisuals();
		CityLevel.addCityVisuals(this, visuals);
		return visuals;
	}
}
