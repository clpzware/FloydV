package fr.ambient.util.render.img;

import lombok.Getter;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;

public final class ImageManager {
    public static HashMap<String, ImageObject> menuImages = new HashMap<>(){{

        put("background", new ImageObject(new ResourceLocation("dogclient/images/menu/background.png")));
        put("newclose", new ImageObject(new ResourceLocation("dogclient/images/menu/close.png")));
        put("skinchanger", new ImageObject(new ResourceLocation("dogclient/images/menu/hanger.png")));
        put("cookie", new ImageObject(new ResourceLocation("dogclient/images/menu/cookie.png")));
        put("session", new ImageObject(new ResourceLocation("dogclient/images/menu/cookie.png")));
        put("cracked", new ImageObject(new ResourceLocation("dogclient/images/menu/cracked.png")));
        put("microsoft", new ImageObject(new ResourceLocation("dogclient/images/menu/microsoft.png")));

        put("wallpaper", new ImageObject(new ResourceLocation("dogclient/background/wallpaper.jpg")));
        put("singleplayer", new ImageObject(new ResourceLocation("dogclient/icons/singleplayer.png")));
        put("multiplayer", new ImageObject(new ResourceLocation("dogclient/icons/multiplayer.png")));
        put("settings", new ImageObject(new ResourceLocation("dogclient/icons/settings.png")));
        put("close", new ImageObject(new ResourceLocation("dogclient/icons/close.png")));
        put("altmanager", new ImageObject(new ResourceLocation("dogclient/icons/altmanager.png")));
        put("viaversion", new ImageObject(new ResourceLocation("dogclient/icons/viaversion.png")));
    }};

    public static HashMap<String, ImageObject> inGameImages = new HashMap<>(){{

        put("sessionWin", new ImageObject(new ResourceLocation("dogclient/icons/session/wins.png")));
        put("sessionTime", new ImageObject(new ResourceLocation("dogclient/icons/session/time.png")));
        put("sessionKD", new ImageObject(new ResourceLocation("dogclient/icons/session/swords.png")));
        put("clock", new ImageObject(new ResourceLocation("dogclient/icons/session/clock.png")));
        put("kills", new ImageObject(new ResourceLocation("dogclient/icons/session/skull.png")));
        put("deaths", new ImageObject(new ResourceLocation("dogclient/icons/session/heart.png")));
        put("wins", new ImageObject(new ResourceLocation("dogclient/icons/session/win.png")));
        put("stopwatch", new ImageObject(new ResourceLocation("dogclient/icons/session/stopwatch.png")));

        put("combat", new ImageObject(new ResourceLocation("dogclient/icons/category/combat.png")));
        put("movement", new ImageObject(new ResourceLocation("dogclient/icons/category/movement.png")));
        put("render", new ImageObject(new ResourceLocation("dogclient/icons/category/render.png")));
        put("player", new ImageObject(new ResourceLocation("dogclient/icons/category/player.png")));
        put("misc", new ImageObject(new ResourceLocation("dogclient/icons/category/exploit.png")));
        put("scripts", new ImageObject(new ResourceLocation("dogclient/icons/category/scripts.png")));

        put("keyboard", new ImageObject(new ResourceLocation("dogclient/icons/keyboard.png")));
        put("chevron", new ImageObject(new ResourceLocation("dogclient/icons/chevron.png")));
        put("play", new ImageObject(new ResourceLocation("dogclient/icons/play.png")));
        put("search", new ImageObject(new ResourceLocation("dogclient/icons/search.png")));
        put("more", new ImageObject(new ResourceLocation("dogclient/icons/more.png")));
        put("check", new ImageObject(new ResourceLocation("dogclient/icons/check.png")));
        put("pencil", new ImageObject(new ResourceLocation("dogclient/icons/pencil.png")));
        put("info", new ImageObject(new ResourceLocation("dogclient/icons/info.png")));
    }};

    @Getter
    private static ArrayList<ImageObject> dynamicallyLoadedImages = new ArrayList<>();

    public static void loadMenuImages(){
        menuImages.values().forEach(ImageObject::loadAsync);
    }
    public static void loadInGameImages(){
        inGameImages.values().forEach(ImageObject::loadAsync);
    }
    public static void unloadMenuImages(){
        menuImages.values().forEach(ImageObject::unload);
    }
    public static void unloadInGameImages(){
        inGameImages.values().forEach(ImageObject::unload);
    }

}
