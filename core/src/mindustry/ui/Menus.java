package mindustry.ui;

import mindustry.annotations.Annotations.*;
import mindustry.gen.*;


import static mindustry.Vars.*;

/** Class for handling menus and notifications across the network. Unstable API! */
public class Menus implements MenusInterface {
    private final MenusSuper menusSuper = new MenusSuper();

    //do not invoke any of the methods below directly, use Call

    @Remote(variants = Variant.both)
    public static void menu(int menuId, String title, String message, String[][] options){
        if(title == null) title = "";
        if(options == null) options = new String[0][0];

        ui.showMenu(title, message, options, (option) -> Call.menuChoose(player, menuId, option));
    }

    @Remote(variants = Variant.both)
    public static void followUpMenu(int menuId, String title, String message, String[][] options){
        if(title == null) title = "";
        if(options == null) options = new String[0][0];

        ui.showFollowUpMenu(menuId, title, message, options, (option) -> Call.menuChoose(player, menuId, option));
    }

    @Remote(variants = Variant.both)
    public static void hideFollowUpMenu(int menuId) {
        ui.hideFollowUpMenu(menuId);
    }


    @Remote(variants = Variant.both)
    public static void textInput(int textInputId, String title, String message, int textLength, String def, boolean numeric){
        if(title == null) title = "";

        ui.showTextInput(title, message, textLength, def, numeric, (text) -> {
            Call.textInputResult(player, textInputId, text);
        }, () -> {
            Call.textInputResult(player, textInputId, null);
        });
    }

    @Remote(variants = Variant.both, unreliable = true)
    public static void setHudText(String message){
        if(message == null) return;

        ui.hudfrag.setHudText(message);
    }

    @Remote(variants = Variant.both)
    public static void hideHudText(){
        ui.hudfrag.toggleHudText(false);
    }

    /** TCP version */
    @Remote(variants = Variant.both)
    public static void setHudTextReliable(String message){
        setHudText(message);
    }

    @Remote(variants = Variant.both)
    public static void announce(String message){
        if(message == null) return;

        ui.announce(message);
    }

    @Remote(variants = Variant.both)
    public static void infoMessage(String message){
        if(message == null) return;

        ui.showText("", message);
    }

    @Remote(variants = Variant.both, unreliable = true)
    public static void infoPopup(String message, float duration, int align, int top, int left, int bottom, int right){
        if(message == null) return;

        ui.showInfoPopup(message, duration, align, top, left, bottom, right);
    }

    @Remote(variants = Variant.both, unreliable = true)
    public static void label(String message, float duration, float worldx, float worldy){
        if(message == null) return;

        ui.showLabel(message, duration, worldx, worldy);
    }

    @Remote(variants = Variant.both)
    public static void infoPopupReliable(String message, float duration, int align, int top, int left, int bottom, int right){
        if(message == null) return;

        ui.showInfoPopup(message, duration, align, top, left, bottom, right);
    }

    @Remote(variants = Variant.both)
    public static void labelReliable(String message, float duration, float worldx, float worldy){
        label(message, duration, worldx, worldy);
    }

    @Remote(variants = Variant.both)
    public static void infoToast(String message, float duration){
        if(message == null) return;

        ui.showInfoToast(message, duration);
    }

    @Remote(variants = Variant.both)
    public static void warningToast(int unicode, String text){
        if(text == null || Fonts.icon.getData().getGlyph((char)unicode) == null) return;

        ui.hudfrag.showToast(Fonts.getGlyph(Fonts.icon, (char)unicode), text);
    }
}
