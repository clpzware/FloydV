package fr.ambient.module.impl.skyblock;

import fr.ambient.Ambient;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.event.impl.render.Render2DEvent;
import fr.ambient.event.impl.render.Render3DEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.property.impl.ModeProperty;
import fr.ambient.property.impl.NumberProperty;
import fr.ambient.util.packet.PacketUtil;
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
import net.minecraft.util.Vec3;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class AutoGift extends Module {
    public AutoGift() {
        super(99, ModuleCategory.MISC);
        registerProperties(mode,giftInterval);
    }

    private EntityArmorStand targetArmorStand = null;
    private EntityArmorStand lastTarget = null;

    private EntityLivingBase target = null;

    private ArrayList<EntityArmorStand> alreadyClicked = new ArrayList<>();

    private ModeProperty mode = ModeProperty.newInstance("Mode", new String[]{"Gifter", "Gifted"}, "Gifter");
    private NumberProperty giftInterval = NumberProperty.newInstance("Gift Interval", 1f, 5f, 20f, 1f);

    public void onEnable(){
        if(mode.is("Gifter")){
            double distance = 69f;
            EntityPlayer giftto = null;
            for(EntityPlayer player : mc.theWorld.playerEntities){
                double ds = PlayerUtil.getBiblicallyAccurateDistanceToEntity(player);
                if(ds < distance && player != mc.thePlayer && ds < 6){
                    giftto = player;
                    distance = ds;
                }
            }
            if(giftto != null){
                target = giftto;
            }
            ChatUtil.display(target.getName() + " is now your gifting target !");
        }
    }


    public void onDisable(){
        lastTarget = null;
        Ambient.getInstance().getRotationComponent().setActive(false, this.getClass());
    }

    @SubscribeEvent
    private void onTick(UpdateEvent event){
        if(mode.is("Gifted")){
            if(targetArmorStand != null){
                PacketUtil.sendPacket(new C02PacketUseEntity(targetArmorStand, PlayerUtil.getClosestPointToEntity(targetArmorStand).subtract(new Vec3(targetArmorStand.posX, targetArmorStand.posY, targetArmorStand.posZ))));
                PacketUtil.sendPacket(new C02PacketUseEntity(targetArmorStand, C02PacketUseEntity.Action.INTERACT));
                alreadyClicked.add(targetArmorStand);
            }

            targetArmorStand = null;


            if(mc.thePlayer.ticksExisted % giftInterval.getValue().intValue() == 0){
                for(Entity e : mc.theWorld.loadedEntityList){
                    if(e instanceof EntityArmorStand entityArmorStand){
                        if(entityArmorStand.getName().toLowerCase().contains("click to open") && PlayerUtil.getBiblicallyAccurateDistanceToEntity(entityArmorStand) < 3 && !alreadyClicked.contains(entityArmorStand)){
                            targetArmorStand = entityArmorStand;
                            lastTarget = targetArmorStand;
                            return;
                        }
                    }
                }
                if(targetArmorStand == null){
                    lastTarget = null;
                }
            }
        }
        if(mode.is("Gifter") && mc.thePlayer.ticksExisted % giftInterval.getValue().intValue() == 0){
            ItemStack itemStack = mc.thePlayer.inventory.getCurrentItem();

            boolean flag = itemStack == null || !itemStack.getDisplayName().toLowerCase().contains("gift") || !(itemStack.getItem() instanceof ItemSkull) ||  itemStack.getDisplayName().toLowerCase().contains("talisman");

            if(mc.currentScreen instanceof GuiChest || mc.currentScreen instanceof GuiInventory){
                mc.thePlayer.closeScreen();
                flag = true;
            }



            if(flag){

                ChatUtil.display("Somethings wrong ! Checking...");

                for(int i = 0; i < 8; i++){
                    ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
                    if (stack != null && stack.getItem() instanceof ItemSkull && stack.getDisplayName().toLowerCase().contains("gift") && !stack.getDisplayName().toLowerCase().contains("talisman")) {
                        mc.thePlayer.inventory.currentItem = i;
                        ChatUtil.display("Found " + stack.getDisplayName() + " at slot " + i);
                        return;
                    }
                }

                ChatUtil.display("You dont have gifts in your hotbar... looking into the inventory");

                Container container = mc.thePlayer.inventoryContainer;

                for (int i = 9; i < 35; i++) {
                    ItemStack stack = container.getSlot(i).getStack();
                    if (stack != null && stack.getItem() instanceof ItemSkull && stack.getDisplayName().toLowerCase().contains("gift") && !stack.getDisplayName().toLowerCase().contains("talisman")) {
                        swapToHotbar(i, mc.thePlayer.inventory.currentItem);
                        ChatUtil.display("Found " + stack.getDisplayName() + " at slot " + i);
                        return;
                    }
                }

                ChatUtil.display("You dont have gifts ! Disabling... ");
                this.setEnabled(false);
                return;
            }else{
                if(PlayerUtil.getBiblicallyAccurateDistanceToEntity(target) < 3){
                    ChatUtil.display("Gifting...");
                    try {
                        PacketUtil.sendPacket(new C02PacketUseEntity(target, PlayerUtil.getClosestPointToEntity(target).subtract(new Vec3(target.posX, target.posY, target.posZ))));
                        PacketUtil.sendPacket(new C02PacketUseEntity(target, C02PacketUseEntity.Action.INTERACT));
                        PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }else{
                    ChatUtil.display("Get closer to target player >.<");
                }
            }
        }
    }



    private void swapToHotbar(int slotid, int invSlot){
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slotid, invSlot, 2, mc.thePlayer);
    }


    @SubscribeEvent
    private void onRotate(PreMotionEvent event){
        if(mode.is("Gifted")){
            if(lastTarget != null){
                float[] rotations = RotationUtil.getRotationDifference(new Vec3(mc.thePlayer), new Vec3(lastTarget), mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
                Ambient.getInstance().getRotationComponent().setActive(true, this.getClass());
                Ambient.getInstance().getRotationComponent().setRotations(mc.thePlayer.rotationYaw - rotations[0],mc.thePlayer.rotationPitch - rotations[1], MoveCorrect.SILENT);
            }else{
                Ambient.getInstance().getRotationComponent().setActive(false, this.getClass());
            }
        }
        if(mode.is("Gifter")){
            if(PlayerUtil.getBiblicallyAccurateDistanceToEntity(target) < 6){
                float[] rotations = RotationUtil.getRotationDifference(new Vec3(mc.thePlayer), new Vec3(target), mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
                Ambient.getInstance().getRotationComponent().setActive(true, this.getClass());
                Ambient.getInstance().getRotationComponent().setRotations(mc.thePlayer.rotationYaw - rotations[0],mc.thePlayer.rotationPitch - rotations[1], MoveCorrect.SILENT);
            }else{
                Ambient.getInstance().getRotationComponent().setActive(false, this.getClass());
            }
        }
    }

    @SubscribeEvent
    private void onRender3D(Render3DEvent event){
        if(mode.is("Gifted")) {
            if (lastTarget != null) {
                ESPUtil.drawPathLine(new ArrayList<Vec3>(Arrays.asList(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ), new Vec3(lastTarget.posX, mc.thePlayer.posY, lastTarget.posZ))), 1f, Color.RED);
            }
            for (Entity e : mc.theWorld.loadedEntityList) {
                if (e instanceof EntityArmorStand entityArmorStand) {
                    if (entityArmorStand.getName().toLowerCase().contains("click to open") && PlayerUtil.getBiblicallyAccurateDistanceToEntity(entityArmorStand) < 3 && !alreadyClicked.contains(entityArmorStand)) {
                        ESPUtil.drawBoundingBox(ESPUtil.getESPFromVec3(new Vec3(entityArmorStand), .1f));
                    }

                }
            }
        }
        if(mode.is("Gifter") && target != null){
            ESPUtil.drawBoundingBox(ESPUtil.getDaFuckingRenderPosAxisAlignedWithMargin(target, 0.1f));
        }
    }

    @SubscribeEvent
    private void onRender2D(Render2DEvent event){
        if(mode.is("Gifted")){
            ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
            mc.fontRendererObj.drawString(alreadyClicked.size() + " gifts", sr.getScaledWidth() / 2, sr.getScaledHeight() / 2, Color.WHITE.getRGB());
        }

    }
}
