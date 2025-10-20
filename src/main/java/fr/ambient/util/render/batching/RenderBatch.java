package fr.ambient.util.render.batching;

import cc.polymorphism.annot.ExcludeConstant;
import cc.polymorphism.annot.ExcludeFlow;
import fr.ambient.util.InstanceAccess;
import fr.ambient.util.render.RenderUtil;
import fr.ambient.util.render.shader.Shader;
import net.minecraft.client.gui.ScaledResolution;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.client.renderer.GlStateManager.*;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

@ExcludeFlow
@ExcludeConstant
public class RenderBatch implements InstanceAccess {
    private final List<RenderableShape> batch = new ArrayList<>();
    private final Shader shader;

    public RenderBatch(RenderUtil.Shapes shape) {
        this.shader = shape.getShader();
    }

    public void addShape(RenderableShape shape) {
        batch.add(shape);
    }

    public void renderBatch() {
        if (batch.isEmpty()) return;

        ScaledResolution sr = new ScaledResolution(mc);

        disableTexture2D();
        enableBlend();
        blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        shader.start();

        for (RenderableShape shape : batch) {
            shape.render(shader, sr);
        }

        shader.stop();
        enableTexture2D();
        disableBlend();
        resetColor();

        batch.clear();
    }
}

