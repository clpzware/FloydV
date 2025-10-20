package fr.ambient.module.impl.movement.invmove;

import fr.ambient.Ambient;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketSendEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.combat.KillAura;
import fr.ambient.module.impl.player.InvManager;
import fr.ambient.module.impl.player.Scaffold;
import fr.ambient.util.packet.PacketUtil;
import fr.ambient.util.player.ChatUtil;
import net.minecraft.block.BlockContainer;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockPos;
import org.lwjglx.input.Keyboard;

public class Watchdoginvmove extends ModuleMode {

    public boolean using = false;
    private int ticksSinceUsed = 0;
    public int ticksSinceChestClick = 0;

    public Watchdoginvmove(String modeName, Module module) {
        super(modeName, module);
    }

    @SubscribeEvent
    public void onUpdate(UpdateEvent event) {
        ticksSinceUsed++;
        if (mc.currentScreen instanceof GuiChat) return;
        InvManager manager = Ambient.getInstance().getModuleManager().getModule(InvManager.class);



        if (mc.thePlayer.isUsingItem()) {
            ticksSinceUsed = 0;
            manager.stop = true;
        } else {
            if (ticksSinceUsed == 4) {
                manager.stop = false;
            }
        }


        ticksSinceChestClick++;
        updateKeyBindings();

        if (mc.currentScreen instanceof GuiChest) {
            PotionEffect speedEffect = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed);
            float slowMultiplier = (speedEffect != null) ? 0.22f : 0.65f;

            mc.thePlayer.motionX *= slowMultiplier;
            mc.thePlayer.motionZ *= slowMultiplier;
        }

        if (mc.currentScreen instanceof GuiInventory || (manager.isEnabled() && manager.nogui.getValue() && mc.currentScreen == null && ticksSinceChestClick > 6) && sendConditions()) {

            switch (mc.thePlayer.ticksExisted % 4) {
                case 0 -> PacketUtil.sendPacket(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
                case 1 -> PacketUtil.sendPacket(new C0DPacketCloseWindow());
            }
        }
    }

    @SubscribeEvent
    private void onPacketSend(PacketSendEvent event) {
        InvManager manager = Ambient.getInstance().getModuleManager().getModule(InvManager.class);

        if (event.getPacket() instanceof C0DPacketCloseWindow) {
            manager.stop2 = true;
        } else if (event.getPacket() instanceof C16PacketClientStatus) {
            manager.stop2 = false;
        }

        if (event.getPacket() instanceof C08PacketPlayerBlockPlacement c08PacketPlayerBlockPlacement) {
            BlockPos pos = c08PacketPlayerBlockPlacement.getPosition();
            if (mc.theWorld.getBlockState(pos).getBlock() instanceof BlockContainer) {
                ticksSinceChestClick = 0;
            }
        }
    }

    private boolean sendConditions() {
        return !Ambient.getInstance().getModuleManager().getModule(Scaffold.class).isEnabled() && !mc.thePlayer.isUsingItem() && KillAura.target == null;
    }

    private void updateKeyBindings() {
        mc.gameSettings.keyBindForward.pressed = Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode());
        mc.gameSettings.keyBindBack.pressed = Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode());
        mc.gameSettings.keyBindLeft.pressed = Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode());
        mc.gameSettings.keyBindRight.pressed = Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode());

        if (mc.currentScreen instanceof GuiChest) {
            mc.gameSettings.keyBindJump.pressed = false;
        }
    }
}
