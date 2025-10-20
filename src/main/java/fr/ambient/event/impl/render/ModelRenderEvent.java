package fr.ambient.event.impl.render;

import fr.ambient.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.EntityLivingBase;

@AllArgsConstructor
public class ModelRenderEvent extends Event {

    @Getter
    private final EntityLivingBase entity;
    private final Runnable model, layers;

    public void drawEntityModel() {
        this.model.run();
    }

    public void drawEntityLayers() {
        this.layers.run();
    }
}
