package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

import java.math.BigInteger;
import java.util.ArrayList;

public class Ruby extends Item {

    {
        image = ItemSpriteSheet.RUBY;
        stackable = true;
    }

    public Ruby() {
        this(BigInteger.ONE);
    }

    public Ruby(BigInteger amount) {
        quantity = amount;
    }

    @Override
    public ArrayList<String> actions(Hero hero) {
        return new ArrayList<>();
    }

    @Override
    public boolean doPickUp(Hero hero, int pos, float time) {
        Catalog.setSeen(getClass());
        RubyBank.add(quantity);
        GameScene.pickUp(this, pos);
        hero.sprite.showStatusWithIcon(0xFF4466, quantity.toString(), FloatingText.RUBY);
        hero.spendAndNext(TIME_TO_PICK_UP);
        Sample.INSTANCE.play(Assets.Sounds.ITEM);
        updateQuickslot();
        return true;
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
    public BigInteger value() {
        return BigInteger.valueOf(120).multiply(quantity);
    }

    public static BigInteger owned(Hero hero) {
        return RubyBank.get();
    }

    public static boolean spend(Hero hero, BigInteger cost) {
        return RubyBank.spend(cost);
    }

    @Override
    public Item random() {
        quantity = BigInteger.valueOf(Random.IntRange(1, 2));
        return this;
    }
}
