package com.shatteredpixel.shatteredpixeldungeon.ui.changelist;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.effects.BadgeBanner;
import com.shatteredpixel.shatteredpixeldungeon.items.BlackPsycheChest;
import com.shatteredpixel.shatteredpixeldungeon.items.InfoPage;
import com.shatteredpixel.shatteredpixeldungeon.items.LostBackpack;
import com.shatteredpixel.shatteredpixeldungeon.items.PsycheChest;
import com.shatteredpixel.shatteredpixeldungeon.items.TicketToPortableShop;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.SkeletonKey;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.EquipmentBag;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.SackOfHolding;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.UtilityBag;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.ExperienceBomb;
import com.shatteredpixel.shatteredpixeldungeon.items.emblem.CommonEmblem;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfDivineInspiration;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfArcana;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfValor;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfWealth;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfPower;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfInsurgence;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.Barricade;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.DeterminantInfusion;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.GalacticInfusion;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.IdentificationBomb;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.ItemQuantifier;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.PocketAlchemy;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.TierUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.treasurebags.LegendaryTreasureBag;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.FerretTuft;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ShurikenOfShadows;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.watabou.noosa.Image;

import java.util.ArrayList;

public class InfPDChanges {
    public static void addAllChanges( ArrayList<ChangeInfo> changeInfos ) {
        ChangeInfo changes = new ChangeInfo("Future Updates Announcement", true, "");
        changes.hardlight(0xFF2400);
        changeInfos.add(changes);
        changes.addButton( new ChangeButton(Icons.get(Icons.PREFS), "Tentative Changes",
                "This is a tentative changes that maybe added/changed in the future updates: \n\n" +
                        "_-_ Buffs and Nerfs (as always)\n" +
                        "_-_ New Challenges (harder than the previous mods)\n" +
                        "_-_ Event-based runs, just like the daily and weekly runs!\n" +
                        "_-_ New Spells\n" +
                        "_-_ New Scrolls and Potions?\n" +
                        "_-_ New Lores (infinity, and beyond)\n" +
                        "_-_ Lastly, an unexpected nerfs and buffs out of nowhere.\n\n" +
                        "So these things are maybe added in the future updates and are subject to change, maybe if the devs were not lazy, I guess?"
        ));
        changes.addButton( new ChangeButton(Icons.get(Icons.CHALLENGE_COLOR), "Challenge Onboard",
                "I'd like to make new challenges, but I want to make it harder than the current challenges in this game. Stay tuned!"
        ));

        changes.addButton( new ChangeButton(Icons.BUFFS.get(), "Upcoming: v0.1.9",
                "In the next update, we will try to add new items, and possibly adding new mechanics to the game!\n"
        ));

        changes = new ChangeInfo("InfPD-0.1.9", true, "");
        changes.hardlight(0x00FFFF);
        changeInfos.add(changes);

        changes.addButton( new ChangeButton(Icons.get(Icons.PREFS), "Other Changes",
                "_-_ Scroll of Transmutation now caps the new trinket's level to its max upgrade\n"
        ));

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), "Fixes",
                "_-_ Fixed incorrect description on Elixir of Divine Inspiration\n"
        ));

        changes = new ChangeInfo("InfPD-0.1.8", true, "");
        changes.hardlight(0x00FFFF);
        changeInfos.add(changes);

        changes = new ChangeInfo("E", false, "");
        changes.hardlight(0x00FFFF);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.TIER_UPGRADE), new TierUpgrade().trueName(),
                "This new spell enhances your weapon's tier."));

        changes.addButton( new ChangeButton(Icons.get(Icons.PREFS), "Other Changes",
                "_-_ King's Crown no longer erases old armor's rarity\n" +
                        "_-_ Removed scene resetting from custom note entry (ShPD)"
        ));

        changes.addButton( new ChangeButton(Icons.BUFFS.get(), "Buffs and Nerfs",
                "_-_ Buffed Wand of Teleportation's charge per cast, is now fixed to 2 charges\n" +
                        "_-_ Rat King now drops cheese after 35 trades, from 30"
        ));

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), "Fixes",
                "_-_ Fixed crash related to Elixir of Divine Inspiration\n" +
                        "_-_ Fixed conflicts with additional description of emblem use and custom notes on items"
        ));

        changes = new ChangeInfo("D", false, "");
        changes.hardlight(0x00FFFF);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.ELIXIR_DI), new ElixirOfDivineInspiration().trueName(),
                "A substitute for Potion of same name, its effect is permanent, and gives you bonus EXP."));

        changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.PORTABLE_ALCHEMY), new PocketAlchemy().trueName(),
                "A new spell to help you craft things in the early game."));

        changes.addButton( new ChangeButton(Icons.get(Icons.PREFS), "Other Changes",
                "_-_ Scroll of Transmutation can now produce the same artifact when transmuting, and can now transfer rarities on transmuted items\n" +
                        "_-_ Made all Blacksmith weapons' tier to 5\n" +
                        "_-_ Wand of Lifesteal can now damage enemies even if you are in full health\n" +
                        "_-_ Slightly reduced the probability of dropping foods on enemies\n" +
                        "_-_ Changed Ring of Might's HP cap to 20, instead of 10 (2 * 10 = 20)\n" +
                        "_-_ Removed Scroll of Determination in the Catalog"
        ));

        changes.addButton( new ChangeButton(Icons.BUFFS.get(), "Buffs and Nerfs",
                "_-_ Buffed Gambler spell slightly, it will now take 30% of your gold when failed, instead of 50%\n" +
                        "_-_ Buffed Item Quantifier spell, now has 75% chance to add 3 of the selected item's quantity\n" +
                        "_-_ Increased Ring of Valor's per upgrade value to 3.5% up from 1.5%\n" +
                        "_-_ Reduced Ring of Sharpshooting's durability multiplier to 15% compounded per level, down from 20% compounded\n" +
                        "_-_ Wand of Teleportation now consumes 5 charges times rarity multi"
        ));

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), "Fixes",
                "_-_ Fixed crash when transmuting artifacts when equipped\n" +
                        "_-_ Actually fixed NTF on obsidian rings\n"
        ));

        changes = new ChangeInfo("C", false, "");
        changes.hardlight(0x00FFFF);
        changeInfos.add(changes);

        Image ic = Icons.get(Icons.CALENDAR);
        ic.hardlight(1.5f, 1.5f, 0f);
        changes.addButton( new ChangeButton(ic, "Weekly Runs Changes",
                "Enemies in weekly runs now have their health doubled, and gives you a 2.5x score multiplier regardless if you win or not. It's your choice."));

        changes.addButton( new ChangeButton(new ItemSprite(ItemSpriteSheet.LIFESTEAL_WAND), "New Wand: Wand of Lifesteal",
                "A brand new wand that steals health from each enemy affected. It doesn't work if you have already full health and consumes wand charge anyway."
        ));

        changes.addButton( new ChangeButton(new ItemSprite(ItemSpriteSheet.INFO_PAGE), "New Lore: Beyond Reality",
                "New lores are waiting for you.. You can get some of those after reaching cycle 1 :P"
        ));

        changes.addButton( new ChangeButton(new ItemSprite(ItemSpriteSheet.MYSTERY_CAKE), "Mystery Cake?",
                "A special cake exclusive on the month of October. Please be patient to find it out."
        ));

        changes.addButton( new ChangeButton(Icons.get(Icons.CHALLENGE_COLOR), "Challenge Balances",
                "The following challenges are balanced due to difficulty spiking high when other challenges was combined:\n\n" +
                        "_On Diet_: Increased effectivity of satisfying hunger from _1/10_ to _1/7_\n" +
                        "_Pharmacophobia_: Reduced poison duration from _lvl/4_ to _lvl/6_\n" +
                        "_Into Darkness_: Increased torch count per floor _by 2_, _total of 4_ if the floor is large\n" +
                        "_Forbidden Runes_: Lowered requirements of getting SoU by _80%_ (from +150%, to +70%), reset and SoTrans are still affected\n" +
                        "_Hostile Champions_: Reduced enemies with titles spawn rate by _25%_ (from 1/8, to 1/10)\n" +
                        "_For the Worthy_: Reduced max exp required by _50%_, total of _150% exp_ (from +100%, to +50%)\n"
        ));

        changes.addButton( new ChangeButton(Icons.get(Icons.SCROLL_COLOR), "Nameable Heroes!",
                "You can now name your heroes as you wish!\n\n_This is purely cosmetic and doesn't affect gameplay._"));

        changes.addButton( new ChangeButton(Icons.get(Icons.PREFS), "Other Changes",
                "_-_ Added GameID in rankings\n" +
                        "_-_ Moved recipe of WoA to custom recipes and added recipe of Elixir of Might\n" +
                        "_-_ Added a Save Game Button\n" +
                        "_-_ Emblem exclusive enchantments can now be actually obtain from Enchantment Scrolls/Stone"
        ));

        changes.addButton( new ChangeButton(Icons.BUFFS.get(), "Buffs and Nerfs",
                "_-_ Drastically increased the energy required for Elixir of Might to be crafted"
        ));

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), "Fixes",
                "_-_ Fixed NTF in obsidian ring\n" +
                        "_-_ Fixed Tubes' color pre-identified on start\n" +
                        "_-_ Fixed unintended pre-identification of PoH and SoU at the start of the game"
        ));

        changes = new ChangeInfo("B", false, "");
        changes.hardlight(0x00FFFF);
        changeInfos.add(changes);

        changes.addButton( new ChangeButton( new Image(Assets.Environment.TERRAIN_FEATURES, 112, 16, 16, 16), "New Trap!",
                "We added a new trap, the Multi Trap!\n\n" +
                        "Multi trap consists of 5 combined traps, they commonly appear as you descend to new regions, and I don't think grim traps can appear here...."
        ));

        changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.EXP_BOMB), new ExperienceBomb().trueName(),
                "A new bomb that gives a bonus experience when an enemy is affected."));

        changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.INFO_PAGE), new InfoPage().trueName(),
                "This is your statistic companion, do whatever you want."));

        changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.RING_OBSIDIAN), new RingOfValor().trueName(),
                "_A new ring has been added that enhances hero's damage!_\n\n" +
                        "The Ring of Valor lets the player directly enhances their weapons' damage, instead of only being able to enhance the damage up by only upgrading the item itself."));

        changes.addButton( new ChangeButton(Icons.get(Icons.CHALLENGE_COLOR), "New Challenges",
                "In this update, we added a new challenge(s)!\n\n" +
                        "_-_ For the Worthy\n" +
                        "_-_ Conquest"
        ));

        changes.addButton( new ChangeButton(Icons.BUFFS.get(), "Buffs and Nerfs",
                "_-_ Warrior healing after healing increased to _~14.29% max HP_, from 12.5% max HP\n" +
                        "_-_ Warrior blind duration when throwing that isn't a missile to enemy is increased to _5 turns_, from 3 turns\n" +
                        "_-_ Mage will now get _12 turns_ of recharging when eating up from 9 turns\n" +
                        "_-_ Mage's shield when they targeted the wand on themself will now gain _10.5% shielding per charge_ from max HP, up from 6.5%\n" +
                        "_-_ Nature's Bounty berry count is no longer in fixed value, made berries drop within _1/(15 + (5 * berries found))_ chance\n" +
                        "_-_ Cached Ration's supply ration count is no longer in fixed value, made rations drop within _1/(25 + (5 * rations found))_ chance\n"
        ));

        changes.addButton( new ChangeButton(Icons.get(Icons.PREFS), "Other Changes",
                "_-_ Added the new treasure bags into the catalog\n" +
                        "_-_ Sort button no longer sticks to the left side when you are in portrait mode\n" +
                        "_-_ Made challenge tab scrollable in ranking window\n" +
                        "_-_ Made some tubes more common: Pure Immunity, Ultimate Power and Godspeed"
        ));

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), "Fixes",
                "_-_ Fixed Black Mimic infinite loop on venting gas when Badder Bosses enabled\n" +
                        "_-_ Fixed rankings window doesn't load after adding a new challenge\n"
        ));

        changes = new ChangeInfo("A", false, "");
        changes.hardlight(0x00FFFF);
        changeInfos.add(changes);

        changes.addButton( new ChangeButton(new ItemSprite(ItemSpriteSheet.LEGENDARY_BAG), "New Treasure Bags!",
                "These bags can only be found on fishing hooks, and a pity system to get the Legendary Treasure Bag!\n\n" +
                        "_The higher the rarity, the higher the quality of the loots!_"
        ));
        changes.addButton( new ChangeButton(BadgeBanner.image( Badges.Badge.MANY_BUFFS.image ), "Added Badges from ShPD!",
                "These new badges are implemented and from ShPD, and these are:\n\n" +
                        "_-_ Taking the Mick\n" +
                        "_-_ So Many Colors\n" +
                        "_-_ Pacifist Ascent"
                ));

        changes.addButton( new ChangeButton(BadgeBanner.image( Badges.Badge.CYCLE_5.image ), "New Cycle Badges!",
                "These new badges that you'll obtain will be the proof for yourself for hours of grinding and slaining enemies, appreciating your hard work through this game :)"));

        changes.addButton( new ChangeButton(new WandOfTeleportation(),
                "Due to its nature of being an overpowered wand, we have to make a nerf for them:\n\n" +
                        "_-_ No longer appears in the dungeon, it is now a _craftable item_ with 5000 energy.\n" +
                        "_-_ Charge per cast increased to _2 * rarity_, from 1 (capped at 50 charge)\n" +
                        "_-_ No longer upgradable, instead it will depend on its rarity\n"
        ));

        ic = Icons.get(Icons.CALENDAR);
        ic.hardlight(1.5f, 1.5f, 0f);
        changes.addButton( new ChangeButton(ic, "Weekly Runs!",
                "_Every week there is a specific seeded run that's available to all players!_\n\n" +
                        "The weekly run makes it easy to compete again friends or other folks on the internet, without having to coordinate and share a specific seed.\n\n" +
                        "The game does keep track of your previous weekly scores, and there is a separate leaderboard for them.\n\n" +
                        "To avoid confusion in reading the weekly run seed, their format is Year-Month/Week"));

        changes.addButton( new ChangeButton(Icons.get(Icons.PREFS), "Other Changes",
                "_-_ Added shuriken of shadows into the catalog\n" +
                        "_-_ Changed save limit to 10k, allowing you to save more run at once\n" +
                        "_-_ Improved the save select scene\n" +
                        "_-_ Missile's max durability is returned to 100\n" +
                        "_-_ Capped ring, misc, and ring slot upgrade to 18\n" +
                        "_-_ You can now get SoU and PoH per challenges active\n" +
                        "_-_ Implemented aiming in missiles and wands\n" +
                        "_-_ Generalized applying emblem message\n" +
                        "_-_ Added a true elemental strike for Galactic, Laserised, Summonner, and Trihit enchantment"
        ));

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), "Fixes",
                "_-_ Fixed Nature's Bounty perk not working\n" +
                        "_-_ Fixed Ring of Wealth's name identified when picked up\n" +
                        "_-_ Fixed crash on Elemental Strike when using it with enchantments with no missile effects\n" +
                        "_-_ Fixed Summoner enchantment's knight spawn chance is not true 25%\n" +
                        "_-_ Fixed incorrect description display on Ring of Haste\n" +
                        "_-_ Fixed wild energy's sprite"
        ));

        changes = new ChangeInfo("InfPD-0.1.7", true, "");
        changes.hardlight(0x00FFFF);
        changeInfos.add(changes);

        changes = new ChangeInfo("F", false, "");
        changes.hardlight(0x00FFFF);
        changeInfos.add(changes);

        changes.addButton( new ChangeButton(Icons.get(Icons.CHALLENGE_COLOR), "New Champion Enemies",
                "New Champions has been added in the game!\n\n" +
                        "Blunt Champions - they are expert for using their hands or something they use as a blunt weapons, they deal 75% more damage as well as inflicts vertigo when they attack and paralyzes you in rare cases.\n" +
                        "Frozen Champions - deal 25% more melee damage but they have -20% speed, chills enemies they attack and sometimes freezes them, are immune to cold gases or effects, and spread blizzard around them as they die."
        ));
        changes.addButton( new ChangeButton(new ShurikenOfShadows(),
                "The thrown weapon designed to destroy terrains... and also damaging enemies around it. This can only be obtained on special items."
        ));
        changes.addButton( new ChangeButton(Icons.PLUS.get(), "New Curse",
                "The Warmaster curse, which set-ups an arena instantly and calling all mobs in the dungeon. Leaving in arena will result to taking ~30% of your health."
        ));
        changes.addButton( new ChangeButton(Icons.get(Icons.PREFS), "Other Changes",
                "_-_ Changed the behavior of fire ability in Firing snapper\n" +
                        "_-_ Portable Shops now stocks exotic variants of scrolls and potions\n" +
                        "_-_ Added Cached Ration perk\n" +
                        "_-_ Added Nature's Bounty perk\n" +
                        "_-_ Added Utility Bag in the catalog\n" +
                        "_-_ Chalice of Blood should now regen properly with rarity\n" +
                        "_-_ Resized the bag window"
        ));
        changes.addButton( new ChangeButton(Icons.BUFFS.get(), "Buffs and Nerfs",
                "_-_ Gleaming Staff can now materialize Refined Bags with very low chance\n" +
                        "_-_ Changed and capped Ring of Tenacity to ~99.99% HP reduction\n" +
                        "_-_ Doubled the max exp required to level up\n" +
                        "_-_ The max durability of missiles to be considered as infinite is increased to 10k\n" +
                        "_-_ Increased the health of quest mobs by 100% in Ghost quest\n" +
                        "_-_ Each taken damage while Faith in my Armor chal. activated now literally increases damage taken by x1.005"
        ));
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), "Bug Fixes",
                "_-_ Fixed crash on depth 26+ when skeleton key is equipped\n" +
                        "_-_ Fixed SoU's upgrade amount button using only 1 scroll after upgrading"
        ));

        changes = new ChangeInfo("E", false, "");
        changes.hardlight(0x00FFFF);
        changeInfos.add(changes);

        changes.addButton( new ChangeButton(new TicketToPortableShop(),
                "A new peaceful branch where you can buy things and return again if you're done. It can only be crafted at the alchemy."
        ));
        changes.addButton( new ChangeButton(Icons.BUFFS.get(), "Buffs and Nerfs",
                "_-_ Blocking enchantment has now an initial 4 shielding, from 2\n" +
                        "_-_ Extractor output quantity is increased to 3, from 2\n" +
                        "_-_ Determinant Infusion rage shield increased by 1.5x, from max hp/2\n" +
                        "_-_ Increased chance of item quantifier by 25%, total of 50%"
        ));
        changes.addButton( new ChangeButton(Icons.get(Icons.PREFS), "Other Changes",
                "_-_ Actually added Utility Bag in shops\n" +
                        "_-_ Added very high drop flare for fishing hook\n" +
                        "_-_ Added Cheese Chunk on quick recipe\n"
        ));

        changes = new ChangeInfo("D", false, "");
        changes.hardlight(0x00FFFF);
        changeInfos.add(changes);

        changes.addButton( new ChangeButton(new UtilityBag(),
                "The new bag that is useful but expensive... Shopkeeper was so greedy :P"
        ));
        changes.addButton( new ChangeButton(Icons.BUFFS.get(), "Buffs and Nerfs",
                "_-_ Bosses' max HP will now base on hero's level\n" +
                        "_-_ Hunger and Starvation now takes 150 more turns"
        ));
        changes.addButton( new ChangeButton(Icons.get(Icons.PREFS), "Other Changes",
                "_-_ Energy bottle can now be held by potion bandolier\n" +
                        "_-_ Max blacksmith favor cap has been increased by 7k, total of 10k\n" +
                        "_-_ Added small rations into the arena shop\n" +
                        "_-_ Fate Lock cna onw be used in quickslots\n" +
                        "_-_ Gold in mining levels increased to 60-100"
        ));
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), "Bug Fixes",
                "_-_ Fixed emblem use still limits to 3, and to enchant emblems\n" +
                        "_-_ Fixed Scroll of Transmutation doesn't drop at required EXP"
        ));

        changes = new ChangeInfo("C", false, "");
        changes.hardlight(0x00FFFF);
        changeInfos.add(changes);

        changes.addButton( new ChangeButton(Icons.BUFFS.get(), "Buffs and Nerfs",
                "_-_ Wand of Disintegration now stops until the targeted cell\n" +
                        "_-_ Emblem use is now increased to 5, from 3 uses.\n"  +
                        "_-_ Fishing Rod rare drops will now scale to fishing rod's level\n" +
                        "_-_ Increased treasure bags' amount by 100%\n"
        ));
        changes.addButton( new ChangeButton(new ScrollOfInsurgence(),
                "Changed the effect of this scroll:\n" +
                        "_-_ It will now create an arena that gives an additional 150% EXP, and additional items in some cases."
        ));
        changes.addButton( new ChangeButton(Icons.get(Icons.PREFS), "Other Changes",
                "_-_ Emblem exclusive enchantments will now occur with a very low chance\n" +
                        "_-_ Aleph Knights no longer inherits enchantment on weapons\n" +
                        "_-_ Shop in arena now actually sells ankhs and some emblems\n" +
                        "_-_ Made custom notes' limit infinite\n" +
                        "_-_ Added the new spells into the catalog\n"
        ));
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), "Bug Fixes",
                "_-_ Fixed false 45% health penalty in the Fate Lock\n"
        ));

        changes = new ChangeInfo("B", false, "");
        changes.hardlight(0x00FFFF);
        changeInfos.add(changes);

        changes.addButton( new ChangeButton(new Barricade(),
                "A spell that changes your terrain, casting will a build barricade for you."
        ));
        changes.addButton( new ChangeButton(new DeterminantInfusion(),
                "A spell that gives you life stealing shield, perhaps, used to something.."
        ));
        changes.addButton( new ChangeButton(Icons.get(Icons.PREFS), "Other Changes",
                "_-_ Fixed inconsistencies in tubes' descriptions\n" +
                        "_-_ Made Elixir of Might rarer\n" +
                        "_-_ Emblems can now be held by Velvet Pouch\n" +
                        "_-_ Tubes can now be held by Potion Bandolier\n" +
                        "_-_ Reduced rooms by ~33%\n" +
                        "_-_ Arena now sells Ankhs and some emblems\n" +
                        "_-_ Fate Lock will now give scroll of transmutation when reached very high experience"
        ));
        changes.addButton( new ChangeButton(Icons.BUFFS.get(), "Buffs and Nerfs",
                "_-_ Pharmacophobia will give you ~25% turns of your health, from ~40%\n" +
                        "_-_ Fishing Hooks now drop Refined Bags at very low chance\n" +
                        "_-_ Fishing Hooks rare drops has been decreased"
        ));
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), "Bug Fixes",
                "_-_ Fixed Ring Capacity upgrade doesn't work.\n" +
                        "_-_ Fixed crash when resetting floor 26 in some cases\n" +
                        "_-_ Fixed long negative actor spend time when opening treasurebags\n"
        ));

        changes = new ChangeInfo("A", false, "");
        changes.hardlight(0x00FFFF);
        changeInfos.add(changes);

        changes.addButton( new ChangeButton(Icons.get(Icons.PREFS), "Other Changes",
                "_-_ Implemented tubes, balanced form and will now appear in the dungeon\n" +
                        "_-_ Added new three emblems with emblem-exclusive enchants\n" +
                        "_-_ Reduced bag size to 60, from 61 (overlaps)"
        ));
        changes.addButton( new ChangeButton(new WandOfTeleportation(),
                "Teleportation makes you......? Crazy! Right?\n\n" +
                        "Wand of teleportation has been added to the game!"
        ));
        changes.addButton( new ChangeButton(new ItemSprite(ItemSpriteSheet.WEAPON_HOLDER), "Enchantments",
                "Added new three emblem-exclusive enchantments, all of them was got in some special weapon abilities"
        ));
        changes.addButton( new ChangeButton(new ItemSprite(ItemSpriteSheet.STONE_EMPOWER), "Stone of Empowerment",
                "Another runestone that helps you in the early game, I guess? It's rare to appear in the game."
        ));
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), "Bug Fixes",
                "_-_ Fixed emblems doubling the count use in bestiary.\n"
        ));

        changes = new ChangeInfo("InfPD-0.1.6", true, "");
        changes.hardlight(0x00FFFF);
        changeInfos.add(changes);

        changes = new ChangeInfo("D", false, "");
        changes.hardlight(0x00FFFF);
        changeInfos.add(changes);

        changes.addButton( new ChangeButton(Icons.BUFFS.get(), "Buffs and Nerfs",
                "_-_ Massively decreased overload duration buff in arena\n" +
                        "_-_ Delayed some overpowered stat mobs in arena\n"
        ));
        changes.addButton( new ChangeButton(Icons.get(Icons.PREFS), "Other Changes",
                "_-_ Shopkeeper key no longer drops if the previous badge isn't unlocked.\n" +
                        "_-_ Improved Rage Shield's floating text.\n" +
                        "_-_ All bags' capacity is increased to 61\n" +
                        "_-_ Increased the chance of having an emblem in shoprooms\n"
        ));
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), "Bug Fixes",
                "_-_ Fixed shopkeeper key drops in an incorrect condition.\n" +
                        "_-_ Added a temporary fix for the bestiary crashes due to null challenges\n"
        ));

        changes = new ChangeInfo("C", false, "");
        changes.hardlight(0x00FFFF);
        changeInfos.add(changes);

        changes.addButton( new ChangeButton(new ItemSprite(ItemSpriteSheet.SCROLL_ZYCHRON), "New Scrolls",
                "Another scrolls that will help you in the early game, can you get it in the beginning?"
        ));
        changes.addButton( new ChangeButton(Icons.get(Icons.PREFS), "Other Changes",
                "_-_ The max capacity of rings can now be upgraded\n"
        ));
        changes.addButton(new ChangeButton(new SackOfHolding(),
                "Sack of Holding's behavior has been changed:\n" +
                        "_-_ You must click _enable_ and drop the item you selected to put it into sack of holding"
        ));
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), "Bug Fixes",
                "_-_ Fixed shopkeeper key doesn't drop\n" +
                        "_-_ Fixed fireboost spell description\n" +
                        "_-_ Fixed NTF in galactic infusion\n"
        ));

        changes = new ChangeInfo("B", false, "");
        changes.hardlight(0x00FFFF);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new GalacticInfusion(), "Another spell which guarantee Galactic enchantment on your weapon. How nice..."));
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), "Bug Fixes",
                "_-_ Fixed NPE caused by level reset and resurrect + skeleton key\n" +
                        "_-_ Fixed visual bug in Emblems on changelog\n" +
                        "_-_ Fixed a NTR (AGAIN) when using skeleton key on door with no charge\n" +
                        "_-_ Fixed NTF on skeleton key's keywall\n" +
                        "_-_ Fixed crash on opening a journal\n" +
                        "_-_ Fixed skeleton key not recharging within higher level and high rarity"
        ));

        changes.addButton( new ChangeButton(Icons.get(Icons.PREFS), "Other Changes",
                "_-_ Added Potion of Debug\n" +
                        "_-_ Added RoExperience in generation\n" +
                        "_-_ Added Emblems into the catalog"
        ));

        changes = new ChangeInfo("A", false, "");
        changes.hardlight(0x00FFFF);
        changeInfos.add(changes);

        changes.addButton( new ChangeButton(Icons.get(Icons.CHALLENGE_COLOR), "Trinket Changes",
                "Certain trinkets has now increased max levels\n\n" +
                        "_-_ Dimensional Sundial: 3 -> 10\n" +
                        "_-_ Exotic Crystals: 3 -> 5\n" +
                        "_-_ Mimic Tooth: 3 -> 5\n" +
                        "_-_ Rat Skull: 3 -> 15\n" +
                        "_-_ Shard of Oblivion: 3 -> 10\n\n" +
                        "This ensures the chaos and the unpredictability of the trinkets."
        ));
        changes.addButton( new ChangeButton(new ItemSprite(ItemSpriteSheet.RING_LIMESTONE), "New Rings",
                "Another rings that was actually made for chaos, do they help you?"
        ));
        changes.addButton(new ChangeButton(new ItemQuantifier(), "Another gamble item, which adds two or remove one quantity of an item."));
        changes.addButton(new ChangeButton(new SkeletonKey(), "Skeleton Key has been added to the game! Have fun to this stuff.... I guess?"));
        changes.addButton(new ChangeButton(new FerretTuft(), "Ferret Tuft has been added to the game! Also, added the hit and miss icons from vanilla."));
        changes.addButton(new ChangeButton(new CommonEmblem(),
                "Yes, a brand new item, the _Emblem_!\n\nThis is somewhat like the Raritizing spell but the rarity of the emblem is 100% guaranteed! You can only use emblems on items thrice.\n\nThey can also include some enchants or something powerful..."
        ));
        changes.addButton( new ChangeButton(Icons.get(Icons.PREFS), "Other Changes",
                "_-_ OOFThiefs can no longer steals bags\n" +
                        "_-_ Cheese and Magnetic Meal perk no longer collects item out of range\n" +
                        "_-_ Added \"More Loots\" upgrade\n" +
                        "_-_ Fixed wrong indicator in Gambler spell\n" +
                        "_-_ Added emblems in shops appearing within 15% chance\n" +
                        "_-_ Added price on emblems (base 400)"
        ));
        changes.addButton( new ChangeButton(Icons.BUFFS.get(), "Buffs and Nerfs",
                "_-_ Nerfed gold drops by -50% min, and -25% max\n" +
                        "_-_ Nerfed all of the rings, their effects now caps at x10 multiplier per cycle (starting x10 on cycle 0)\n" +
                        "_-_ Slightly buffed Fishing Rods\n" +
                        "_-_ Increased the chance of getting unique items in Refined Bags (from 4.5% to 5%)\n" +
                        "_-_ Artifact, Ring and Mics slots' SoU cost reduced to _20_ from _25_.\n" +
                        "_-_ Increased fire boost damage in fire booster spell by 10%\n" +
                        "_-_ Added a slight damage boost in _Creative Gloves_ but increased their attack delay by 50%"
        ));

        changes = new ChangeInfo("InfPD-0.1.5", true, "");
        changes.hardlight(0x00FFFF);
        changeInfos.add(changes);
        changes.addButton( new ChangeButton(Icons.get(Icons.PREFS), "Other Changes",
                        "_-_ Changed trait of Tanky champions, now takes only 5% damage\n" +
                        "_-_ Changed rarity display to its true name\n" +
                        "_-_ You can now see what rarity was replaced in the item\n" +
                        "_-_ Changed description of Scroll of Magic Mapping\n" +
                        "_-_ Changed description of Galactic enchantment\n"
        ));
        changes.addButton( new ChangeButton(Icons.BUFFS.get(), "Buffs and Nerfs",
                "_-_ Nerfed Ring of Haste's base speed\n"
        ));
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), "Bug Fixes",
                "_-_ Fixed crash on journal related to Kunai when discovered\n" +
                        "_-_ Fixed conditions on Black Fate Lock for ascension\n" +
                        "_-_ Fixed missing description on Round Shield\n" +
                        "_-_ Fixed a minor crash related to identifying item with a rarity\n" +
                        "_-_ Fixed Ring of Haste where the hero is slower at start\n"
        ));

        changes = new ChangeInfo("InfPD-0.1.4", true, "");
        changes.hardlight(0x00FFFF);
        changeInfos.add(changes);
        changes.addButton( new ChangeButton(Icons.get(Icons.PREFS), "Other Changes",
                "_-_ Added 2 new ring sprites\n" +
                        "_-_ Scak of Holding no longer holds: rings, scrolls and wands\n" +
                        "_-_ Increasing cycles now requires levels...\n" +
                        "_-_ Added 2 new champions, Speedy and Tanky\n" +
                        "_-_ Increased spawn duration on enemies\n" +
                        "_-_ Added _Gambling_ spell\n"
        ));
        changes.addButton( new ChangeButton(Icons.BUFFS.get(), "Buffs and Nerfs",
                "_-_ Ghost's reward upgrade level has been reduced to 2, from 5\n" +
                        "_-_ Ring of Tenacity is now capped at 90% damage resistance, and reduced per-upgrade effect\n" +
                        "_-_ Ring of Elements' per=upgrade percentages is massively reduced\n" +
                        "_-_ Nerfed spirit bow's min and max damage\n" +
                        "_-_ EXP required for getting SoU is now scaled with cycles\n"
        ));
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), "Bug Fixes",
                "_-_ Fixed crash related to RoW with Exotic Crystals\n" +
                        "_-_ Fixed crash on journal's catalog\n" +
                        "_-_ Fixed a small bug when the rarity name was shown even unidentified\n"
        ));

        changes = new ChangeInfo("InfPD-0.1.3", true, "");
        changes.hardlight(0x00FFFF);
        changeInfos.add(changes);
        changes.addButton( new ChangeButton(Icons.get(Icons.PREFS), "Other Changes",
                "_-_ Added true names for Rarities\n" +
                        "_-_ Reverted back Ring of Haste\n" +
                        "_-_ Removed additional descriptions for Galactic Enchanment\n" +
                        "_-_ Increased standard rooms by ~50%\n" +
                        "_-_ Challenges, Daily Runs, and Custom Seeds are now unlocked by default"
        ));
        changes.addButton( new ChangeButton(Icons.BUFFS.get(), "Buffs and Nerfs",
                "_-_ Nerfed Ring of Haste at its finest\n" +
                        "_-_ Treasure Bags' price were increased by 10 gold\n" +
                        "_-_ Scroll of Mirror Image now summons 3 images from 2\n" +
                        "_-_ Scroll of Teleportation now paralyzes you for 3 turns\n" +
                        "_-_ Potion of Liquid Flame now lasts 50% longer\n" +
                        "_-_ Potion of Strength now gives additional strength in 1/2147483647"
        ));
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), "Bug Fixes",
                "_-_ Fixed sack of holding TAKES ALL YOUR ITEMS AT ONCE.\n" +
                        "_-_ Fixed stone of clairvoyance crashing the game\n" +
                        "_-_ Adjusted clayball sprite"
        ));

        changes = new ChangeInfo("InfPD-0.1.2", true, "");
        changes.hardlight(0x00FFFF);
        changeInfos.add(changes);
        changes.addButton( new ChangeButton(Icons.get(Icons.PREFS), "Other Changes",
                "_-_ Added item renaming\n" +
                        "_-_ Made challenge window scrollable\n" +
                        "_-_ Aligned the update link to github (you can now get updates in-game)\n"
        ));
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), "Bug Fixes",
                "_-_ Fixed crashes on the perk Raritizing Magic\n"
        ));
        changes.addButton( new ChangeButton(Icons.BUFFS.get(), "Buffs and Nerfs",
                "_-_ Nerfed hero max exp\n" +
                        "_-_ Reduced the entire loot multiplier\n"
        ));


        changes = new ChangeInfo("InfPD-0.1.1", true, "");
        changes.hardlight(0x00FFFF);
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), "Bug Fixes",
                "_-_ Fixed Potion of Strength not being droppped on the dungeon\n"
        ));
        changes.addButton( new ChangeButton(Icons.get(Icons.PREFS), "Other Changes",
                "_-_ Added a sprite for Raritize spell\n" +
                        "_-_ Added _Extractor_ spell\n" +
                        "_-_ Increased custom note size to 30\n" +
                        "_-_ Added new spells on the Catalog\n" +
                        "_-_ Added some useful info on mobs\n"
        ));
        changes.addButton( new ChangeButton(Icons.BUFFS.get(), "Buffs and Nerfs",
                "_-_ Drastically nerfed gold drops\n" +
                        "_-_ Fire Booster now multiplies the fire damage by 1.15x\n" +
                        "_-_ Recharging buffs now gives you 35 turns of duration\n" +
                        "_-_ Stone of Clairvoyance now scans within 23 cells radius\n" +
                        "_-_ Arena now spawns with 2x HP and gains a champion title after power 30\n"
        ));

        changes = new ChangeInfo("InfPD-0.1.0", true, "");
        changes.hardlight(0x00FFFF);
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(new RingOfWealth(), "Added back ring of wealth"));
        changes.addButton(new ChangeButton(new EquipmentBag(), "Moved the ring, artifact and misc slots into their own bag"));
        changes.addButton(new ChangeButton(new SackOfHolding(), "Added the sack of holding, a bag that can hold any item, its sold on the shop at floor 16"));
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), "System changes",
                "Changed the cycle system to allow for basicly infinite cycles, also changed hardcoded values to be calculated exponentially",
                "Allowed the amount of ring, artifact and misc slots to be variable, for use in later Content",
                "Added variables for multiplying the amount of rooms, monsters, loot, traps and the size of rooms in a layer"));
        changes.addButton(new ChangeButton(new LostBackpack(), "Added an optional second row of quickslots"));
        changes.addButton( new ChangeButton(Icons.BUFFS.get(), "Buffs and Nerfs",
                "_-_ Nerfed all rarities' effects\n" +
                        "_-_ Rebalanced chances and multipliers of rarities\n" +
                        "_-_ Buffed Mimic and Cycle Multiplier\n" +
                        "_-_ The chances of getting luck potion is decreased\n" +
                        "_-_ Bosses now gains 2.5% HP bonus per escalating depth\n"
        ));
        changes.addButton( new ChangeButton(Icons.get(Icons.PREFS), "Other Changes",
                "_-_ Added _Galactic_ enchanment\n" +
                        "_-_ Added _Raritize_ spell\n" +
                        "_-_ Extended other bags from 36 to 57\n" +
                        "_-_ Added Raritizing Magic perk\n"
        ));
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), "Bug Fixes",
                "_-_ Fixed fishing rods not working\n"
        ));
    }
}
