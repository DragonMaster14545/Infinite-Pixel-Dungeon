package com.shatteredpixel.shatteredpixeldungeon.items.modules;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.ExoticPotion;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

public class ItemModule extends Module {

    {
        stackable = true;
        image = ItemSpriteSheet.ITEMED;

        defaultAction = AC_USE;
    }

    @Override
    public void execute( Hero hero, String action ) {
        super.execute(hero, action);
        if (action.equals(AC_USE)) {
            switch (Random.Int(3)) {
                case 0: default:
                    detach(curUser.belongings.backpack);
                    updateQuickslot();
                    Dungeon.level.drop(Generator.random(Generator.Category.POTION).quantity(Random.Int(2, 5)), curUser.pos).sprite.drop();
                    break;
                case 1:
                    detach(curUser.belongings.backpack);
                    updateQuickslot();
                    Dungeon.level.drop(Generator.random(Generator.Category.SCROLL).quantity(Random.Int(2, 5)), curUser.pos).sprite.drop();
                    break;
                case 2:
                    detach(curUser.belongings.backpack);
                    updateQuickslot();
                    Dungeon.level.drop(Generator.random(), curUser.pos).sprite.drop();
                    break;
            }
        }
    }

    @Override
    public long value() {
        return 150 * quantity;
    }
}
