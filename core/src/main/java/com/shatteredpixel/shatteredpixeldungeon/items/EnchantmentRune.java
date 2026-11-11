package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfEnchantment;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Galactic;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class EnchantmentRune extends Item {

	public static final String AC_INSERT = "INSERT";

	{
		image = ItemSpriteSheet.STONE_ENCHANT;
		stackable = false;
		defaultAction = AC_INSERT;
	}

	private Weapon.Enchantment enchantment;
	private boolean rolled = false;

	public EnchantmentRune() {
	}

	public static EnchantmentRune of( Weapon.Enchantment e ) {
		EnchantmentRune rune = new EnchantmentRune();
		rune.enchantment = e;
		rune.rolled = true;
		return rune;
	}

	public Weapon.Enchantment enchantment() {
		if ( !rolled ) {
			rolled = true;
			enchantment = Weapon.Enchantment.random( Galactic.class );
		}
		return enchantment;
	}

	private String enchantName() {
		Weapon.Enchantment e = enchantment();
		return e == null ? "" : Messages.titleCase( e.name( Messages.get( Galactic.class, "enchantment" ) ) );
	}

	@Override
	public String name() {
		return Messages.get( this, "name", enchantName() );
	}

	@Override
	public String desc() {
		Weapon.Enchantment e = enchantment();
		return Messages.get( this, "desc" ) + ( e == null ? "" : "\n\n" + e.desc() );
	}

	@Override
	public ItemSprite.Glowing glowing() {
		Weapon.Enchantment e = enchantment();
		return e == null ? null : e.glowing();
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public long value() {
		return 120 * quantity;
	}

	@Override
	public long energyVal() {
		return 8 * quantity;
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_INSERT );
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {
		super.execute( hero, action );
		if ( action.equals( AC_INSERT ) ) {
			curUser = hero;
			curItem = this;
			GameScene.selectItem( selector );
		}
	}

	private boolean fits( Item item ) {
		if ( !( item instanceof Weapon ) ) return false;
		Weapon.Enchantment e = ( (Weapon) item ).enchantment;
		return e instanceof Galactic && ( (Galactic) e ).canInsert( enchantment() );
	}

	public boolean insertInto( Weapon weapon, Hero hero ) {
		if ( !fits( weapon ) ) return false;
		Galactic galactic = (Galactic) weapon.enchantment;
		Weapon.Enchantment e = enchantment();
		if ( !galactic.insert( e ) ) return false;
		weapon.enchant( galactic ); //refreshes the quickslot, journal catalog and sprite glow
		return true;
	}

	private final WndBag.ItemSelector selector = new WndBag.ItemSelector() {

		@Override
		public String textPrompt() {
			return Messages.get( EnchantmentRune.class, "prompt" );
		}

		@Override
		public Class<? extends Bag> preferredBag() {
			return null;
		}

		@Override
		public boolean itemSelectable( Item item ) {
			return fits( item );
		}

		@Override
		public void onSelect( Item item ) {
			if ( item == null || !( curItem instanceof EnchantmentRune ) ) return;
			EnchantmentRune rune = (EnchantmentRune) curItem;
			if ( !( item instanceof Weapon ) || !rune.insertInto( (Weapon) item, curUser ) ) {
				GLog.w( Messages.get( EnchantmentRune.class, "no_fit" ) );
				return;
			}

			rune.detach( curUser.belongings.backpack );
			GLog.p( Messages.get( EnchantmentRune.class, "inserted", rune.enchantName(), item.name() ) );
			Sample.INSTANCE.play( Assets.Sounds.READ );
			Invisibility.dispel();
			curUser.sprite.operate( curUser.pos );
			curUser.spend( 1f );
			curUser.busy();
		}
	};

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		if ( rolled ) bundle.put( ENCHANTMENT, enchantment );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		if ( bundle.contains( ENCHANTMENT ) ) {
			enchantment = (Weapon.Enchantment) bundle.get( ENCHANTMENT );
			rolled = true;
		}
	}

	private static final String ENCHANTMENT = "enchantment";

	public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe.SimpleRecipe {

		{
			inputs = new Class[]{ ScrollOfEnchantment.class };
			inQuantity = new int[]{ 1 };

			cost = 10;

			output = EnchantmentRune.class;
			outQuantity = 1;
		}
	}
}
