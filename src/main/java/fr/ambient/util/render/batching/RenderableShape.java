package fr.ambient.util.render.batching;

import fr.ambient.util.render.shader.Shader;
import net.minecraft.client.gui.ScaledResolution;

public interface RenderableShape {
    void render(Shader shader, ScaledResolution sr);
}
