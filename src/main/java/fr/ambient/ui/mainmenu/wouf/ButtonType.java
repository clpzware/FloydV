package fr.ambient.ui.mainmenu.wouf;

import fr.ambient.util.InstanceAccess;
import fr.ambient.util.render.img.ImageObject;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ButtonType implements InstanceAccess {

    SINGLEPLAYER("Singleplayer", menuImages.get("singleplayer")),
    MULTIPLAYER("Multiplayer", menuImages.get("multiplayer")),
    SETTINGS("Settings", menuImages.get("settings")),
    ALTMANAGER("AltManager", menuImages.get("altmanager")),
    VIAVERSION("ViaVersion", menuImages.get("viaversion")),
    CLOSE("Close", menuImages.get("close"));


    private final String name;
    private final ImageObject image;
}
