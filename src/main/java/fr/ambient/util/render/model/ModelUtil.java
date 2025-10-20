package fr.ambient.util.render.model;

import cc.polymorphism.annot.ExcludeConstant;
import cc.polymorphism.annot.ExcludeFlow;
import net.minecraft.util.Vec3;

import java.awt.*;

import static net.minecraft.client.renderer.GlStateManager.disableCull;
import static net.minecraft.client.renderer.GlStateManager.enableCull;
import static org.lwjgl.opengl.GL11.*;

@ExcludeFlow
@ExcludeConstant
public final class ModelUtil {


    public static void draw3DSphere(Vec3 pos, final float size, final Color c) {
        pos = VecUtil.fixVec3(pos);

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        disableCull();
        glDepthMask(false);

        glColor4f(c.getRed() / 255.0f, c.getGreen() / 255.0f, c.getBlue() / 255.0f, c.getAlpha() / 255.0f);

        final int numSegments = 10;
        final int numStacks = 10;

        final double segmentStep = 2 * Math.PI / numSegments;
        final double stackStep = Math.PI / numStacks;

        for (int i = 0; i < numStacks; i++) {
            final double lat0 = -0.5 * Math.PI + i * stackStep;
            final double z0 = Math.sin(lat0);
            final double zr0 = Math.cos(lat0);

            final double lat1 = -0.5 * Math.PI + (i + 1) * stackStep;
            final double z1 = Math.sin(lat1);
            final double zr1 = Math.cos(lat1);

            glBegin(GL_TRIANGLE_STRIP);

            for (int j = 0; j <= numSegments; j++) {
                final double lng = j * segmentStep;
                final double x = Math.cos(lng);
                final double y = Math.sin(lng);

                glVertex3d(pos.xCoord + size * x * zr0, pos.yCoord + size * y * zr0, pos.zCoord + size * z0);
                glVertex3d(pos.xCoord + size * x * zr1, pos.yCoord + size * y * zr1, pos.zCoord + size * z1);
            }
            glEnd();
        }

        glEnable(GL_TEXTURE_2D);
        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        enableCull();
        glDisable(GL_BLEND);
    }


}
