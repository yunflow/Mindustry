package mindustry.ui;

import arc.Core;
import arc.Events;
import arc.struct.Seq;
import arc.util.Nullable;
import mindustry.annotations.Annotations;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import mindustry.gen.Player;

import java.util.ArrayList;
import java.util.List;

import static mindustry.Vars.ui;

/**
 * @author Zhaojie Wang
 */
public class MenusSuper implements MenusInterface {
    private static final Seq<Menus.MenuListener> menuListeners = new Seq<>();
    private static final Seq<Menus.TextInputListener> textInputListeners = new Seq<>();
    private final List<Menus> menus = new ArrayList<>();

    /**
     * Register a *global* menu listener. If no option is chosen, the option is returned as -1.
     */
    public static int registerMenu(Menus.MenuListener listener) {
        menuListeners.add(listener);
        return menuListeners.size - 1;
    }

    /**
     * Register a *global* text input listener. If no text is provided, the text is returned as null.
     */
    public static int registerTextInput(Menus.TextInputListener listener) {
        textInputListeners.add(listener);
        return textInputListeners.size - 1;
    }

    @Annotations.Remote(targets = Annotations.Loc.both, called = Annotations.Loc.both)
    public static void menuChoose(@Nullable Player player, int menuId, int option) {
        if (player != null) {
            Events.fire(new EventType.MenuOptionChooseEvent(player, menuId, option));
            if (menuId >= 0 && menuId < menuListeners.size) {
                menuListeners.get(menuId).get(player, option);
            }
        }
    }

    @Annotations.Remote(targets = Annotations.Loc.both, called = Annotations.Loc.both)
    public static void textInputResult(@Nullable Player player, int textInputId, @Nullable String text) {
        if (player != null) {
            Events.fire(new EventType.TextInputEvent(player, textInputId, text));
            if (textInputId >= 0 && textInputId < textInputListeners.size) {
                textInputListeners.get(textInputId).get(player, text);
            }
        }
    }

    @Annotations.Remote(variants = Annotations.Variant.both)
    public static void openURI(String uri){
        if(uri == null) return;

        ui.showConfirm(Core.bundle.format("linkopen", uri), () -> Core.app.openURI(uri));
    }

    //internal use only
    @Annotations.Remote
    public static void removeWorldLabel(int id){
        var label = Groups.label.getByID(id);
        if(label != null){
            label.remove();
        }
    }

    public List<Menus> getMenuListeners() {
        return menus;
    }
}
