package fr.ambient.module.impl.render.world;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.AttackEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumParticleTypes;

public class KillEffects extends Module {

    private EntityPlayer target;

    public KillEffects() {
        super(117, ModuleCategory.RENDER);
    }

    @Override
    protected void onDisable() {
        target = null;
        super.onDisable();
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent e) {
        check();
    }

    public void check() {
        if (target != null && (!mc.theWorld.loadedEntityList.contains(target) || target.isDead || target.getHealth() <= 0)) {
            final double startY = target.posY;
            final double endY = target.posY + target.height + 0.4;
            final double step = 0.2;
            final int particleCount = 2;
            final Block blockType = Blocks.redstone_block;

            for (int i = 0; i < 20; i++) {
                for (double y = startY; y <= endY; y += step) {
                    double offsetX = (Math.random() - 0.5) * 0.5;
                    double offsetZ = (Math.random() - 0.5) * 0.5;

                    for (int j = 0; j < particleCount; j++) {
                        mc.theWorld.spawnParticle(
                                EnumParticleTypes.BLOCK_CRACK,
                                target.posX + offsetX,
                                y,
                                target.posZ + offsetZ,
                                (Math.random() - 0.5) * 0.02,
                                Math.random() * 0.02,
                                (Math.random() - 0.5) * 0.02,
                                Block.getStateId(blockType.getDefaultState())
                        );
                    }
                }
            }

            mc.theWorld.playSound(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, "dig.stone", 1, 1, false);

            target = null;
        }
    }

    @SubscribeEvent
    public void onAttack(AttackEvent e) {
        if (!e.getEntity().isDead && e.getEntity() instanceof EntityPlayer p) {
            target = p;
        }
    }
}
