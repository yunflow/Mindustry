package mindustry.ui;

import arc.util.Nullable;
import mindustry.gen.Player;

/**
 * @author Zhaojie Wang
 */
public interface MenusInterface {
    interface MenuListener {
        void get(Player player, int option);
    }

    interface TextInputListener {
        void get(Player player, @Nullable String text);
    }
}
