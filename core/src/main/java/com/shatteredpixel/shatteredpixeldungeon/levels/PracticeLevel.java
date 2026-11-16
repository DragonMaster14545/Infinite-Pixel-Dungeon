package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.PracticeDummy;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Shopkeeper;
import com.shatteredpixel.shatteredpixeldungeon.items.Ankh;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.PrismBox;
import com.shatteredpixel.shatteredpixeldungeon.items.ShulkerBox;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.*;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.EmptyRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.StudyRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.SuspiciousChestRoom;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.utils.Point;
import com.watabou.utils.Reflection;

import java.math.BigInteger;
import java.util.ArrayList;

public class PracticeLevel extends Level {

	public static final int REGEN_TURNS = 10000;

	private static final int H = 45;
	private static final int START_X = 5;
	private static final int MAX_X = 100;
	private static final int[] ROW_TOP = {1, 13, 23, 35};
	private static final int ROOM = 9;

	private static final int HAND_DUMMY = 0;
	private static final int HAND_ALCHEMY = 1;
	private static final int HAND_LIBRARY = 2;
	private static final int HAND_SHOP = 3;

	@SuppressWarnings("unchecked")
	private static final Class<? extends Room>[] ROOMS = new Class[]{
			ArmoryRoom.class, ArtifactAtriumRoom.class, CryptRoom.class, CrystalChoiceRoom.class,
			CrystalMazeRoom.class, CrystalPathRoom.class, CrystalVaultRoom.class, GardenRoom.class,
			LaboratoryRoom.class, LibraryRoom.class, MagicWellRoom.class, ArtifactAtriumRoom.class,
			MagicalFireRoom.class, PoisonGardenRoom.class, PoolRoom.class, RunestoneRoom.class,
			SacrificeRoom.class, SentryRoom.class, StatueRoom.class, StorageRoom.class, ToxicGasRoom.class,
			TrapsRoom.class, TreasuryRoom.class, AlchemyChamberRoom.class, StudyRoom.class, SuspiciousChestRoom.class,
			EmptyRoom.class, EmptyRoom.class, EmptyRoom.class, EmptyRoom.class, EmptyRoom.class
	};

	{
		color1 = 0x534f3e;
		color2 = 0xb9d661;
		viewDistance = 40;
	}

	private int W;
	private final ArrayList<Integer> dummyCells = new ArrayList<>();
	private int shopkeeperCell = -1;
	private int[] alchemyRect, libraryRect, shopRect;

	private static class Slot {
		Class<? extends Room> cls;
		int hand = -1;
		int width;
		int roomW = ROOM;
		int roomH = ROOM;
		int l, t, row;
	}

	@Override
	public String tilesTex() {
		return Assets.Environment.TILES_CITY;
	}

	@Override
	public String waterTex() {
		return Assets.Environment.WATER_CITY;
	}

	private ArrayList<Slot> layout() {
		ArrayList<Slot> slots = new ArrayList<>();
		Slot a = new Slot();
		a.hand = HAND_ALCHEMY;
		a.width = 20;
		a.roomW = 19;
		slots.add(a);
		Slot s = new Slot();
		s.hand = HAND_SHOP;
		s.width = 20;
		s.roomW = 19;
		slots.add(s);
		Slot d = new Slot();
		d.hand = HAND_DUMMY;
		d.width = 10;
		slots.add(d);
		Slot lib = new Slot();
		lib.hand = HAND_LIBRARY;
		lib.width = 10;
		slots.add(lib);
		for (Class<? extends Room> c : ROOMS) {
			Slot r = new Slot();
			r.cls = c;
			r.width = 10;
			if (c == CrystalVaultRoom.class) {
				r.roomW = 7;
				r.roomH = 7;
			}
			slots.add(r);
		}

		int row = 0;
		int x = START_X;
		for (Slot sl : slots) {
			if (x + sl.width > MAX_X && row < ROW_TOP.length - 1) {
				row++;
				x = START_X;
			}
			sl.row = row;
			sl.l = x;
			sl.t = ROW_TOP[row];
			x += sl.width;
		}
		return slots;
	}

	private void fill(int x1, int y1, int x2, int y2, int terrain) {
		for (int y = y1; y <= y2; y++) {
			for (int x = x1; x <= x2; x++) {
				map[y * W + x] = terrain;
			}
		}
	}

	@Override
	protected boolean build() {
		dummyCells.clear();
		shopkeeperCell = -1;
		itemsToSpawn.clear();

		ArrayList<Slot> slots = layout();
		int maxRight = 40;
		for (Slot s : slots) maxRight = Math.max(maxRight, s.l + s.width);
		W = maxRight + 2;

		setSize(W, H);

		fill(1, 10, W - 2, 12, Terrain.EMPTY_SP);
		fill(1, 32, W - 2, 34, Terrain.EMPTY_SP);
		fill(1, 10, 3, 34, Terrain.EMPTY_SP);

		int entrance = 9 * W + 2;
		map[entrance] = Terrain.ENTRANCE;
		transitions.add(new LevelTransition(this, entrance, LevelTransition.Type.REGULAR_ENTRANCE));

		for (Slot s : slots) {
			boolean topDoor = (s.row % 2 == 1);
			int doorX = s.l + s.roomW / 2;
			int doorY = topDoor ? s.t : s.t + s.roomH - 1;
			if (s.hand >= 0) {
				paintHand(s, doorX, doorY);
			} else {
				paintRooms(s, doorX, doorY, topDoor);
			}
		}
		return true;
	}

	private void paintHand(Slot s, int doorX, int doorY) {
		int l = s.l, t = s.t, r = s.l + s.roomW - 1, b = s.t + s.roomH - 1;
		fill(l + 1, t + 1, r - 1, b - 1, Terrain.EMPTY);
		map[doorY * W + doorX] = Terrain.DOOR;
		int[] rect = {l + 1, t + 1, r - 1, b - 1};
		switch (s.hand) {
			case HAND_DUMMY:
				for (int x = l + 2; x <= r - 2; x += 2) {
					for (int y = t + 2; y <= b - 2; y += 2) {
						if (y == doorY - 1 || y == doorY + 1) continue;
						dummyCells.add(y * W + x);
					}
				}
				break;
			case HAND_ALCHEMY:
				alchemyRect = rect;
				map[(t + 2) * W + l + 1] = Terrain.ALCHEMY;
				map[(t + 4) * W + l + 1] = Terrain.ALCHEMY;
				map[(t + 6) * W + l + 1] = Terrain.ALCHEMY;
				break;
			case HAND_LIBRARY:
				libraryRect = rect;
				for (int x = l + 1; x <= r - 1; x++) map[(t + 1) * W + x] = Terrain.BOOKSHELF;
				break;
			case HAND_SHOP:
				shopRect = rect;
				fill(l + 1, t + 1, r - 1, b - 1, Terrain.EMPTY_SP);
				shopkeeperCell = (t + s.roomH / 2) * W + l + s.roomW / 2;
				break;
		}
	}

	private void paintRooms(Slot s, int doorXIn, int doorY, boolean topDoor) {
		int doorX = doorXIn;
		int l = s.l, t = s.t, r = s.l + s.roomW - 1, b = s.t + s.roomH - 1;
		try {
			Room room = Reflection.newInstance(s.cls);
			room.set(l, t, r, b);
			int chosenX = doorX;
			for (int off = 0; off <= 4; off++) {
				boolean found = false;
				for (int sg = -1; sg <= 1; sg += 2) {
					int cx = doorX + off * sg;
					if (cx <= l || cx >= r) continue;
					if (room.canConnect(new Point(cx, doorY))) {
						chosenX = cx;
						found = true;
						break;
					}
				}
				if (found) break;
			}
			doorX = chosenX;
			Room.Door door = new Room.Door(doorX, doorY);
			room.connected.put(new EmptyRoom(), door);
			room.paint(this);
			Painter.set(this, new Point(doorX, doorY), Terrain.DOOR);

			for (Item item : new ArrayList<>(itemsToSpawn)) {
				dropNear(item, doorX, doorY, topDoor);
			}
			itemsToSpawn.clear();
		} catch (Throwable e) {
			fill(l, t, r, b, Terrain.WALL);
			itemsToSpawn.clear();
		}
	}

	private void dropNear(Item item, int doorX, int doorY, boolean topDoor) {
		int dir = topDoor ? -1 : 1;
		for (int dy = 1; dy <= 3; dy++) {
			for (int dx = 0; dx <= 4; dx++) {
				for (int sign = -1; sign <= 1; sign += 2) {
					int x = doorX + dx * sign;
					int y = doorY + dir * dy;
					if (x < 1 || x >= W - 1 || y < 0 || y >= H) continue;
					int cell = y * W + x;
					if (map[cell] == Terrain.EMPTY_SP && heaps.get(cell) == null) {
						drop(item, cell);
						return;
					}
				}
			}
		}
	}

	@Override
	public Mob createMob() {
		return null;
	}

	@Override
	protected void createMobs() {
		for (int c : dummyCells) {
			Mob d = new PracticeDummy();
			d.pos = c;
			mobs.add(d);
		}
		if (shopkeeperCell >= 0) {
			Shopkeeper keeper = new Shopkeeper();
			keeper.pos = shopkeeperCell;
			mobs.add(keeper);
		}
	}

	private Actor regen;

	@Override
	public Actor addRespawner() {
		if (regen == null) {
			regen = new Regenerator();
			Actor.addDelayed(regen, 1f);
		} else {
			Actor.add(regen);
		}
		return regen;
	}

	private static class Regenerator extends Actor {

		{
			actPriority = BUFF_PRIO;
		}

		@Override
		protected boolean act() {
			spend(TICK);
			if (!Dungeon.practice || Dungeon.hero == null || !Dungeon.hero.isAlive()) return true;
			Dungeon.practiceTurns++;
			if (Dungeon.practiceTurns == 9500) {
				GLog.p("The level will reset in around 500 turns, please reset immediately or prepare to it.");
			}
			if (Dungeon.practiceTurns >= REGEN_TURNS) {
				Dungeon.practiceTurns = 0;
				Dungeon.practiceGen++;
				GLog.w(Messages.get(PracticeLevel.class, "regen"));
				InterlevelScene.mode = InterlevelScene.Mode.RESET;
				Game.switchScene(InterlevelScene.class);
			}
			return true;
		}
	}

	private ArrayList<Integer> cells(int[] rect, int skipRows) {
		ArrayList<Integer> list = new ArrayList<>();
		if (rect == null) return list;
		for (int y = rect[1] + skipRows; y <= rect[3]; y++) {
			for (int x = rect[0]; x <= rect[2]; x++) {
				list.add(y * W + x);
			}
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	private void place(Class<?>[] classes, ArrayList<Integer> spots, int qty) {
		for (Class<?> c : classes) {
			if (spots.isEmpty()) return;
			Item item;
			try {
				item = (Item) Reflection.newInstance((Class<? extends Item>) c);
			} catch (Exception e) {
				continue;
			}
			if (item == null) continue;
			item.identify();
			if (item.stackable) item.quantity(BigInteger.valueOf(qty));
			drop(item, spots.remove(0)).type = Heap.Type.FOR_ARENA_SALE;
		}
	}

	private void placeRandom(Generator.Category cat, ArrayList<Integer> spots, int count) {
		for (int i = 0; i < count && !spots.isEmpty(); i++) {
			Item item = Generator.random(cat);
			if (item == null) continue;
			Heap h = drop(item, spots.remove(0));
			h.type = Heap.Type.FOR_SALE;
		}
	}

	private void placeItem(Item item, ArrayList<Integer> spots, int count) {
		for (int i = 0; i < count && !spots.isEmpty(); i++) {
			if (item == null) continue;
			Heap h = drop(item, spots.remove(0));
			h.type = Heap.Type.FOR_SALE;
		}
	}

	@Override
	protected void createItems() {
		if (alchemyRect != null) {
			ArrayList<Integer> alch = new ArrayList<>();
			for (int y = alchemyRect[1]; y <= alchemyRect[3]; y++) {
				for (int x = alchemyRect[0] + 2; x <= alchemyRect[2]; x++) {
					alch.add(y * W + x);
				}
			}
			place(Generator.Category.POTION.classes, alch, 5);
			place(Generator.Category.SEED.classes, alch, 5);
			place(Generator.Category.STONE.classes, alch, 5);
		}

		ArrayList<Integer> lib = cells(libraryRect, 1);
		place(Generator.Category.SCROLL.classes, lib, 5);

		ArrayList<Integer> shop = cells(shopRect, 0);
		shop.remove((Integer) shopkeeperCell);
		placeRandom(Generator.Category.WEAPON, shop, 5);
		placeRandom(Generator.Category.ARMOR, shop, 5);
		placeRandom(Generator.Category.RING, shop, 5);
		placeRandom(Generator.Category.WAND, shop, 5);
		placeRandom(Generator.Category.ARTIFACT, shop, 5);
		placeRandom(Generator.Category.FOOD, shop, 5);
		placeRandom(Generator.Category.MODULE, shop, 5);
		placeRandom(Generator.Category.TUBES, shop, 5);
		placeItem(new Ankh(), shop, 5);
		if (Badges.isUnlocked( Badges.Badge.KIT_UNLOCKED )) placeItem(new ShulkerBox(), shop, 5);
		placeItem(new PrismBox(), shop, 5);
	}

	@Override
	public int randomRespawnCell(Char ch) {
		return 11 * W + 2;
	}
}
