package com.shatteredpixel.shatteredpixeldungeon.items;

import java.math.BigInteger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Base64Coder;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.VaultLink;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTransmutation;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.FileUtils;
import com.watabou.utils.PlatformSupport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.UUID;

public class GiftBox extends ShulkerBox {

	public static final String AC_SHARE = "SHARE";

	private static final String MAGIC = "InfPD";
	private static final String CLIP = "InfPD:";
	private static final String FILE = "gift_redeemed.dat";
	private static final String KEY = "ids";
	private static final int NONCE = 8;
	private static final int MAC = 32;

	private String giftId = newGiftId();

	private static String newGiftId() {
		return Long.toHexString( UUID.randomUUID().getMostSignificantBits() ) + Long.toHexString( UUID.randomUUID().getLeastSignificantBits() );
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if (hero.belongings.backpack.contains( this )) actions.add( AC_SHARE );
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {
		if (action.equals( AC_SHARE )) {
			share( hero );
			return;
		}
		super.execute( hero, action );
	}

	@Override
	public BigInteger capacity() {
		return BigInteger.valueOf( BASE_CAPACITY );
	}

	@Override
	public BigInteger maxQuantity() {
		return BigInteger.TEN.pow( 1000 );
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return new ItemSprite.Glowing( color );
	}

	@Override
	public BigInteger maxItemLevel() {
		return BigInteger.TEN.pow( 1000 );
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public Item upgrade() {
		return this;
	}

	@Override
	public Item upgrade( BigInteger n ) {
		return this;
	}

	@Override
	public BigInteger value() {
		return BigInteger.ZERO;
	}

    @Override
	public Item duplicate() {
		GiftBox dupe = (GiftBox) super.duplicate();
		if (dupe != null) dupe.giftId = newGiftId();
		return dupe;
	}

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( "gift_id", giftId );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		if (bundle.contains( "gift_id" )) giftId = bundle.getString( "gift_id" );
	}

	public static boolean isEquipment( Item item ) {
		return item instanceof Weapon || item instanceof Armor || item instanceof Wand
				|| item instanceof Ring || item instanceof Artifact;
	}

	private static byte[] digest( byte[]... parts ) {
		try {
			MessageDigest md = MessageDigest.getInstance( "SHA-256" );
			for (byte[] p : parts) md.update( p );
			return md.digest();
		} catch (java.security.NoSuchAlgorithmException e) {
			throw new RuntimeException( e );
		}
	}

	private static byte[] key( String label ) {
		return ( "infinitepd:" + label ).getBytes( StandardCharsets.UTF_8 );
	}

	private static byte[] xor( byte[] data, byte[] nonce ) {
		byte[] out = new byte[data.length];
		byte[] k = key( "enc" );
		int pos = 0;
		int counter = 0;
		while (pos < data.length) {
			byte[] block = digest( k, nonce, new byte[]{ (byte) ( counter >>> 24 ), (byte) ( counter >>> 16 ), (byte) ( counter >>> 8 ), (byte) counter } );
			counter++;
			for (int i = 0; i < block.length && pos < data.length; i++, pos++) {
				out[pos] = (byte) ( data[pos] ^ block[i] );
			}
		}
		return out;
	}

	private static byte[] mac( byte[] nonce, byte[] cipher ) {
		byte[] k = key( "mac" );
		return digest( k, nonce, cipher, k );
	}

	private static String seal( String plain ) {
		byte[] nonce = new byte[NONCE];
		new SecureRandom().nextBytes( nonce );
		byte[] cipher = xor( plain.getBytes( StandardCharsets.UTF_8 ), nonce );
		byte[] tag = mac( nonce, cipher );
		byte[] out = new byte[NONCE + cipher.length + MAC];
		System.arraycopy( nonce, 0, out, 0, NONCE );
		System.arraycopy( cipher, 0, out, NONCE, cipher.length );
		System.arraycopy( tag, 0, out, NONCE + cipher.length, MAC );
		return CLIP + new String( Base64Coder.encode( out ) );
	}

	private static String open( String text ) throws IOException {
		if (text == null) throw new IOException();
		String t = text.trim();
		if (!t.startsWith( CLIP )) throw new IOException();
		byte[] raw;
		try {
			raw = Base64Coder.decode( t.substring( CLIP.length() ) );
		} catch (RuntimeException e) {
			throw new IOException( e );
		}
		if (raw.length < NONCE + MAC + 1) throw new IOException();
		byte[] nonce = new byte[NONCE];
		byte[] tag = new byte[MAC];
		byte[] cipher = new byte[raw.length - NONCE - MAC];
		System.arraycopy( raw, 0, nonce, 0, NONCE );
		System.arraycopy( raw, NONCE, cipher, 0, cipher.length );
		System.arraycopy( raw, NONCE + cipher.length, tag, 0, MAC );
		if (!MessageDigest.isEqual( tag, mac( nonce, cipher ) )) throw new IOException();
		return new String( xor( cipher, nonce ), StandardCharsets.UTF_8 );
	}

	private String encode() {
		for (Item i : contents) {
			if (isEquipment( i )) i.gifted = true;
		}
		Bundle b = new Bundle();
		b.put( "custom_name", customName );
		b.put( "magic", MAGIC );
		b.put( "id", giftId );
		b.put( "items", contents );
		b.put( "boxid", boxId );
		b.put( "custom_color", customColor );
		b.put( "color", color );
		ByteArrayOutputStream out = new ByteArrayOutputStream();
        Bundle.write( b, out, false );
        return seal( new String( out.toByteArray(), StandardCharsets.UTF_8 ) );
	}

	private void destroy( Hero hero ) {
		for (Item i : new ArrayList<>( contents )) Dungeon.quickslot.clearItem( i );
		contents.clear();
		pinned = 0;
		Bag owner = ownerOf( hero.belongings.backpack );
		if (owner != null) detachAll( owner );
		updateQuickslot();
	}

	private Bag ownerOf( Bag bag ) {
		for (Item i : bag.items) {
			if (i == this) return bag;
			if (i instanceof Bag) {
				Bag found = ownerOf( (Bag) i );
				if (found != null) return found;
			}
		}
		return null;
	}

	private void share( final Hero hero ) {
		if (DeviceCompat.isDebug()) {
			GLog.i( Messages.get( GiftBox.class, "debug_build" ) );
			return;
		}
		if (contents.isEmpty()) {
			GLog.i( Messages.get( GiftBox.class, "share_empty" ) );
			return;
		}
		final boolean picker = Game.platform.supportsFilePicker();
		ArrayList<String> opts = new ArrayList<>();
		if (picker) opts.add( Messages.get( GiftBox.class, "share_file" ) );
		opts.add( Messages.get( GiftBox.class, "share_clip" ) );
		opts.add( Messages.get( GiftBox.class, "share_cancel" ) );
		final GiftBox self = this;
		GameScene.show( new WndOptions( Messages.get( GiftBox.class, "share_title" ),
				Messages.get( GiftBox.class, "share_body" ),
				opts.toArray( new String[0] ) ) {
			@Override
			protected void onSelect( int index ) {
				int fileIdx = picker ? 0 : -1;
				int clipIdx = picker ? 1 : 0;
				if (index != fileIdx && index != clipIdx) return;
				final String data = self.encode();
				if (data == null) {
					GLog.n( Messages.get( GiftBox.class, "share_failed" ) );
					return;
				}
				if (index == fileIdx) {
					Game.platform.saveFileWithPicker( "gift_" + System.currentTimeMillis() + ".ipdgift",
							data.getBytes( StandardCharsets.UTF_8 ),
							new PlatformSupport.FilePickerCallback() {
								@Override
								public void onComplete( boolean success ) {
									if (success) {
										self.destroy( hero );
										GLog.p( Messages.get( GiftBox.class, "shared_file" ) );
									} else {
										GLog.w( Messages.get( GiftBox.class, "share_kept" ) );
									}
								}
							} );
				} else {
					try {
						Gdx.app.getClipboard().setContents( data );
					} catch (Throwable t) {
						GLog.n( Messages.get( GiftBox.class, "share_failed" ) );
						return;
					}
					self.destroy( hero );
					GLog.p( Messages.get( GiftBox.class, "shared_clip" ) );
				}
			}
		} );
	}

	private static synchronized boolean used( String id ) {
		try {
			Bundle b = FileUtils.bundleFromFile( FILE );
			String all = b.getString( KEY );
			for (String s : all.split( "," )) if (s.equals( id )) return true;
		} catch (IOException | RuntimeException ignored) {
		}
		return false;
	}

	private static synchronized void markUsed( String id ) {
		String all = "";
		try {
			all = FileUtils.bundleFromFile( FILE ).getString( KEY );
		} catch (IOException | RuntimeException ignored) {
		}
		Bundle b = new Bundle();
		b.put( KEY, all.isEmpty() ? id : all + "," + id );
		try {
			FileUtils.bundleToFile( FILE, b );
		} catch (IOException e) {
			ShatteredPixelDungeon.reportException( e );
		}
	}

	public static void redeem( final VaultLink link, final Runnable after ) {
		final boolean picker = Game.platform.supportsFilePicker();
		ArrayList<String> opts = new ArrayList<>();
		if (picker) opts.add( Messages.get( GiftBox.class, "redeem_file" ) );
		opts.add( Messages.get( GiftBox.class, "redeem_clip" ) );
		opts.add( Messages.get( GiftBox.class, "redeem_cancel" ) );
		GameScene.show( new WndOptions( Messages.get( GiftBox.class, "redeem_title" ),
				Messages.get( GiftBox.class, "redeem_body" ),
				opts.toArray( new String[0] ) ) {
			@Override
			protected void onSelect( int index ) {
				int fileIdx = picker ? 0 : -1;
				int clipIdx = picker ? 1 : 0;
				if (index == fileIdx) {
					Game.platform.openFileWithPicker( new PlatformSupport.FileOpenCallback() {
						@Override
						public void onFileSelected( byte[] data ) {
							if (data == null) return;
							redeemText( link, new String( data, StandardCharsets.UTF_8 ), after );
						}
					} );
				} else if (index == clipIdx) {
					String text = null;
					try {
						text = Gdx.app.getClipboard().getContents();
					} catch (Throwable ignored) {
					}
					if (text == null || !text.trim().startsWith( CLIP )) {
						GLog.w( Messages.get( GiftBox.class, "redeem_bad" ) );
						return;
					}
					redeemText( link, text, after );
				}
			}
		} );
	}

	private static void redeemText( VaultLink link, String text, Runnable after ) {
		GiftBox box = new GiftBox();
		String id;
		try {
			Bundle b = Bundle.read( new ByteArrayInputStream( open( text ).getBytes( StandardCharsets.UTF_8 ) ) );
			if (!MAGIC.equals( b.getString( "magic" ) )) throw new IOException();
			id = b.getString( "id" );
			box.giftId = b.getString( "id" );
			box.customName = b.getString("custom_name");
			box.boxId = b.getLong("boxid");
			box.customColor = b.getBoolean("custom_color");
			box.color = b.getInt("color");
			if (id == null || id.isEmpty()) throw new IOException();
			for (Bundlable x : b.getCollection( "items" ) ) {
				if (x instanceof Item && !( x instanceof Bag ) && !( x instanceof ShulkerBox )) {
					if (box.contents.size() >= BASE_CAPACITY) break;
					if (isEquipment( (Item) x )) ( (Item) x ).gifted = true;
					box.add( (Item) x );
				}
			}
		} catch (IOException | RuntimeException e) {
			GLog.w( Messages.get( GiftBox.class, "redeem_bad" ) );
			return;
		}
		if (box.contents.isEmpty()) {
			GLog.w( Messages.get( GiftBox.class, "redeem_bad" ) );
			return;
		}
		if (used( id )) {
			GLog.w( Messages.get( GiftBox.class, "redeem_used" ) );
			return;
		}
		if (BigInteger.valueOf( VaultStore.boxes().size() ).compareTo( link.slots() ) >= 0) {
			GLog.w( Messages.get( GiftBox.class, "redeem_no_room" ) );
			return;
		}
		VaultStore.boxes().add( box );
		if (!VaultStore.save()) {
			VaultStore.boxes().remove( box );
			GLog.n( Messages.get( GiftBox.class, "redeem_failed" ) );
			return;
		}
		markUsed( id );
		VaultStore.log( VaultStore.LOG_TRANSFER_IN, box.name() );
		VaultStore.sync();
		GLog.p( Messages.get( GiftBox.class, "redeem_done", box.contents.size() ) );
		if (after != null) after.run();
	}

	private void add( Item item ) {
		if (item.stackable) {
			for (Item i : contents) {
				if (item.isSimilar( i )) {
					i.merge( item );
					return;
				}
			}
		}
		contents.add( item );
	}

	public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe.SimpleRecipe {

		{
			inputs =  new Class[]{ ShulkerBox.class, ScrollOfTransmutation.class };
			inQuantity = new int[]{ 1, 1 };

			cost = 15;

			output = GiftBox.class;
			outQuantity = 1;
		}

	}
}
