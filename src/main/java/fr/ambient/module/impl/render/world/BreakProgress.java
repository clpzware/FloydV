package fr.ambient.module.impl.render.world;

import fr.ambient.Ambient;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.render.Render3DEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.module.impl.player.Breaker;
import fr.ambient.property.impl.BooleanProperty;
import fr.ambient.util.render.animation.Animation;
import fr.ambient.util.render.animation.Easing;
import fr.ambient.util.render.model.ESPUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;


public class BreakProgress extends Module {

    private final BooleanProperty box = BooleanProperty.newInstance("Show Box", true);
    private final BooleanProperty text = BooleanProperty.newInstance("Show Text", false);
    private final BooleanProperty breaker = BooleanProperty.newInstance("Only With Breaker", false);
    private final BooleanProperty animationSetting = BooleanProperty.newInstance("Animation", true, text::getValue);
    private final Animation popIn = new Animation(Easing.EASE_IN_OUT_QUAD, 250);

    public BreakProgress() {
        super(42,"Shows the progress of breaking blocks", ModuleCategory.RENDER);
        this.registerProperties(box, text, animationSetting,breaker);
    }

    @SubscribeEvent
    private void onRender3D(Render3DEvent event) {
        if (breaker.getValue() && !Ambient.getInstance().getModuleManager().getModule(Breaker.class).isEnabled()) {
            return;
        }
        final double progress = animationSetting.getValue() ? popIn.getValue() : 1;

        Breaker module = Ambient.getInstance().getModuleManager().getModule(Breaker.class);
        BlockPos blockPos = module.isEnabled() ? module.breakPos : mc.objectMouseOver.getBlockPos();

        boolean isBreaking = mc.playerController.curBlockDamageMP != 0 || (module.isEnabled() && module.blockDamage != 0) && blockPos != null;

        Block block = mc.theWorld.getBlockState(blockPos).getBlock();
        if (block instanceof BlockAir) isBreaking = false;

        popIn.run(isBreaking ? 1.0F : 0.0F);

        if (blockPos == null) return;

        AxisAlignedBB bb = block.getSelectedBoundingBox(mc.theWorld, blockPos);
        final double x = blockPos.getX() + 0.5 - RenderManager.viewerPosX;
        final double y = blockPos.getY() + (bb.maxY - bb.minY) / 2f - RenderManager.viewerPosY;
        final double z = blockPos.getZ() + 0.5 - RenderManager.viewerPosZ;

        int damagePercentage = MathHelper.clamp_int((int) ((mc.playerController.curBlockDamageMP != 0 ? mc.playerController.curBlockDamageMP : module.blockDamage) * 100), 0, 100);

        if (text.getValue()) {
            GlStateManager.pushMatrix();
            GlStateManager.translate((float) x, (float) y, (float) z);
            GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f);
            GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0f, 0.0f, 0.0f);
            GlStateManager.scale(-0.022f * progress, -0.022f * progress, -0.022f * progress);
            GlStateManager.depthMask(false);
            GlStateManager.disableDepth();
            mc.fontRendererObj.drawString(damagePercentage + "%", (float) (-mc.fontRendererObj.getStringWidth(damagePercentage + "%") / 2), -3.0f, -1, true);
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.popMatrix();
        }

        if (damagePercentage != 0 && isBreaking && box.getValue()) {
            ESPUtil.breakProgress(blockPos, Ambient.getInstance().getHud().getCurrentTheme().color2, damagePercentage / 100f, 0.4f);
        }
    }
}