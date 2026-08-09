package com.shatteredpixel.shatteredpixeldungeon.items.modules;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.ExoticPotion;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

public class ScrollModule extends Module {

    {
        stackable = true;
        image = ItemSpriteSheet.SCROLL_GIVED;

        defaultAction = AC_USE;
    }

    @Override
    public void execute( Hero hero, String action ) {
        super.execute(hero, action);
        if (action.equals(AC_USE)) {
            Item base = Generator.random(Generator.Category.SCROLL);
            if (Random.Int(2) == 0) {
                detach(curUser.belongings.backpack);
                updateQuickslot();
                Dungeon.level.drop(Generator.random(Generator.Category.SCROLL), curUser.pos).sprite.drop();
            } else {
                Class<?> exoClass = ExoticPotion.regToExo.get(base.getClass());
                detach( curUser.belongings.backpack );
                updateQuickslot();
                Dungeon.level.drop((Item) Reflection.newInstance(exoClass), curUser.pos).sprite.drop();
            }
        }
    }

    @Override
    public long value() {
        return 75 * quantity;
    }
}
