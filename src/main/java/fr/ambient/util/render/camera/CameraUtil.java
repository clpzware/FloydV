package fr.ambient.util.render.camera;

import fr.ambient.util.InstanceAccess;
import fr.ambient.util.math.structure.Vec3d;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;

public final class CameraUtil implements InstanceAccess {

    public static void draw2DElementTo3D(Runnable runnable, float x, float y, float z) {
        RenderManager renderManager = mc.getRenderManager();
        float viewerYaw = renderManager.playerViewY;
        float viewerPitch = renderManager.playerViewX;
        boolean isThirdPersonFrontal = renderManager.options.thirdPersonView == 2;

        Vec3d pos = new Vec3d(x, y, z);
        pos = pos.subtract(RenderManager.viewerPosX, RenderManager.viewerPosY, RenderManager.viewerPosZ);

        GlStateManager.pushMatrix();
        GlStateManager.translate((float)pos.getX(), (float)pos.getY() + 0.25F, (float)pos.getZ());
        GlStateManager.rotate(-viewerYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((isThirdPersonFrontal ? -1 : 1) * viewerPitch, 1.0F, 0.0F, 0.0F);

        final float scale = 0.025F;
        GlStateManager.scale(-scale, -scale, scale);
        GlStateManager.disableDepth();

        runnable.run();

        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }
}
