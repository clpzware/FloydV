package fr.ambient.module.impl.render.hud;


import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.property.impl.BooleanProperty;


public class Chat extends Module {
    private final BooleanProperty transparentChat = BooleanProperty.newInstance("Transparent Chat", false);
    private final BooleanProperty compactChat = BooleanProperty.newInstance("Compact Chat", false);

    public Chat() {
        super(44,"Modifies how chat is rendered.", ModuleCategory.RENDER);
        this.registerProperties(transparentChat, compactChat);
    }

    public boolean isTransparent() {
        return transparentChat.getValue();
    }

    public boolean isCompact() { return compactChat.getValue(); }
}
