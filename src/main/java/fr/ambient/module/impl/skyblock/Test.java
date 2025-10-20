package fr.ambient.module.impl.skyblock;

import fr.ambient.Ambient;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.event.impl.render.Render2DEvent;
import fr.ambient.event.impl.render.Render3DEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.module.impl.render.player.ESP;
import fr.ambient.property.impl.ModeProperty;
import fr.ambient.property.impl.NumberProperty;
import fr.ambient.util.packet.PacketUtil;
import fr.ambient.util.pathfinder.LegitPathFinder;
import fr.ambient.util.player.ChatUtil;
import fr.ambient.util.player.PlayerUtil;
import fr.ambient.util.player.RotationUtil;
import fr.ambient.util.player.movecorrect.MoveCorrect;
import fr.ambient.util.render.model.ESPUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Test extends Module {
    public Test() {
        super(10, ModuleCategory.MISC);
    }

    List<Vec3> toRender = new ArrayList<>();

    public void onEnable(){
        toRender = LegitPathFinder.findPath(new BlockPos(mc.thePlayer), new BlockPos(mc.thePlayer.posX + 32, mc.thePlayer.posY, mc.thePlayer.posZ + 32));
    }


    public void onDisable(){
        toRender.clear();
    }

    @SubscribeEvent
    private void onRender3D(Render3DEvent event){
        ESPUtil.drawPathLine(new ArrayList<>(toRender), 1f);
    }
}
