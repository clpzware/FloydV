package fr.ambient.ui.framework.impl;

import fr.ambient.ui.framework.UIComponent;
import fr.ambient.util.render.RenderUtil;
import net.minecraft.entity.player.EntityPlayer;

import java.util.UUID;

public class UIHeadComponent extends UIComponent {

    private UUID uuid;
    private EntityPlayer player;

    public UIHeadComponent player(EntityPlayer player) {
        this.uuid = player.getUniqueID();
        this.player = player;
        return this;
    }

    public UIHeadComponent uuid(UUID uuid) {
        this.uuid = uuid;
        this.player = null;
        return this;
    }

    @Override
    public void render() {
        if (player != null) {
            RenderUtil.drawRoundSkin(player, x, y, height, rounding);
        } else {
            RenderUtil.drawRoundHead(uuid, x, y, height, rounding);
        }
    }
}