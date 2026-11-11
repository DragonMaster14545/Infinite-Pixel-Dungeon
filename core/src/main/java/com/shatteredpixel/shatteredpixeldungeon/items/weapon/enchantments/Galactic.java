package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Galactic extends Weapon.Enchantment {
    private static final ItemSprite.Glowing BLACK = new ItemSprite.Glowing( 0x000000 );

    private final List<Weapon.Enchantment> additional = new ArrayList<>();
    private int capacity = 3;
    private boolean unrolled = true;

    public Galactic() {
    }

    private List<Weapon.Enchantment> list() {
        if ( unrolled ) {
            unrolled = false;
            int fill = Random.chances( new float[]{ 0, 70, 30 } );
            for (int i = 0; i < fill; i++) {
                Weapon.Enchantment e = Weapon.Enchantment.random( Galactic.class );
                if ( canInsert( e ) ) additional.add( e );
            }
        }
        return additional;
    }

    public static Galactic empty() {
        Galactic g = new Galactic();
        g.unrolled = false;
        return g;
    }

    public int capacity() {
        return Math.max( capacity, list().size() );
    }

    public int used() {
        return list().size();
    }

    public int free() {
        return capacity() - used();
    }

    public List<Weapon.Enchantment> sockets() {
        return Collections.unmodifiableList( list() );
    }

    public boolean canAddSocket() {
        return capacity() < 12;
    }

    public boolean addSocket() {
        if ( !canAddSocket() ) return false;
        capacity = capacity() + 1;
        return true;
    }

    public static boolean socketable( Weapon.Enchantment e ) {
        return e != null && !e.curse() && !( e instanceof Galactic );
    }

    public boolean canInsert( Weapon.Enchantment e ) {
        if ( !socketable( e ) ) return false;
        if ( free() <= 0 ) return false;
        for (Weapon.Enchantment have : list()) {
            if ( have.getClass() == e.getClass() ) return false; //no duplicates
        }
        return true;
    }

    public boolean insert( Weapon.Enchantment e ) {
        if ( !canInsert( e ) ) return false;
        list().add( e );
        return true;
    }

    public Weapon.Enchantment remove( int index ) {
        if ( index < 0 || index >= list().size() ) return null;
        return list().remove( index );
    }

    public Galactic set( Weapon.Enchantment... enchants ) {
        unrolled = false;
        additional.clear();
        for (Weapon.Enchantment e : enchants) {
            if ( socketable( e ) ) list().add( e );
        }
        capacity = Math.max( 3, list().size() );
        return this;
    }


    public boolean hasEnchant( Class<? extends Weapon.Enchantment> type, Char owner ) {
        for (Weapon.Enchantment enchantment : list()) {
            if ( enchantment.getClass() == type ) return owner.buff( MagicImmune.class ) == null;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public <T extends Weapon.Enchantment> T getEnchant( Class<? extends T> type ) {
        for (Weapon.Enchantment enchantment : list()) {
            if ( enchantment.getClass() == type ) return (T) enchantment;
        }
        return null;
    }

    @Override
    public long proc( Weapon weapon, Char attacker, Char defender, long damage ) {
        for (Weapon.Enchantment enchantment : list()) {
            damage = enchantment.proc( weapon, attacker, defender, damage );
        }
        return damage;
    }

    @Override
    public String name( String weaponName ) {
        return super.name( weaponName );
    }

    public String trueName( String weaponName ) {
        return super.name( weaponName );
    }

    @Override
    public String desc() {
        StringBuilder desc = new StringBuilder( Messages.get( this, "desc" ) );
        desc.append( "\n\n" ).append( Messages.get( Galactic.class, "sockets", used(), capacity() ) );
        for (Weapon.Enchantment e : list()) {
            desc.append( "\n_" ).append( Messages.titleCase( e.name( Messages.get( Galactic.class, "enchantment" ) ) ) )
                    .append( "_" );
        }
        if ( free() > 0 ) {
            desc.append( "\n" ).append( Messages.get( Galactic.class, "empty", free() ) );
        }
        return desc.toString();
    }

    @Override
    public ItemSprite.Glowing glowing() {
        return BLACK;
    }

    public static final String ADDITIONAL = "additional";
    public static final String CAPACITY = "capacity";

    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle( bundle );
        bundle.put( ADDITIONAL, list() );
        bundle.put( CAPACITY, capacity() );
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle( bundle );
        unrolled = false;
        additional.clear();
        for (Bundlable bundlable : bundle.getCollection( ADDITIONAL )) {
            additional.add( (Weapon.Enchantment) bundlable );
        }
        //old saves have no capacity key, they keep everything they already hold
        capacity = Math.max( bundle.contains( CAPACITY ) ? bundle.getInt( CAPACITY ) : 3, list().size() );
    }
}
