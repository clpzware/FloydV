package fr.ambient.util.waveycapes.renderlayers;

import cc.polymorphism.annot.ExcludeConstant;
import cc.polymorphism.annot.ExcludeFlow;
import fr.ambient.Ambient;
import fr.ambient.module.impl.render.player.Cosmetics;
import fr.ambient.protection.ProtectedLaunch;
import fr.ambient.util.render.img.ImageObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EnumPlayerModelParts;

@ExcludeFlow
@ExcludeConstant
public class CustomCapeRenderLayer implements LayerRenderer<AbstractClientPlayer> {
    static final int partCount = 16;
    private ModelRenderer[] customCape = new ModelRenderer[partCount];
    private final RenderPlayer playerRenderer;
    private final SmoothCapeRenderer smoothCapeRenderer = new SmoothCapeRenderer();

    public CustomCapeRenderLayer(RenderPlayer playerRenderer, ModelBase model) {
        this.playerRenderer = playerRenderer;
        buildMesh(model);
    }

    private void buildMesh(ModelBase model) {
        customCape = new ModelRenderer[partCount];
        for (int i = 0; i < partCount; i++) {
            ModelRenderer base = new ModelRenderer(model, 0, i);
            base.setTextureSize(64, 32);
            this.customCape[i] = base.addBox(-5.0F, (float)i, -1.0F, 10, 1, 1);
        }
    }

    @Override
    public void doRenderLayer(AbstractClientPlayer abstractClientPlayer, float paramFloat1, float paramFloat2, float deltaTick,
                              float animationTick, float paramFloat5, float paramFloat6, float paramFloat7) {
        Cosmetics cosmetics = Ambient.getInstance().getModuleManager().getModule(Cosmetics.class);
        if (!cosmetics.waveyCapes.getValue() || !cosmetics.isEnabled() || abstractClientPlayer != Minecraft.getMinecraft().thePlayer || cosmetics.getCapeMode().equalsIgnoreCase("None") || cosmetics.getCustomName().equalsIgnoreCase("None")) return;

        if (!abstractClientPlayer.isInvisible() && (abstractClientPlayer.hasPlayerInfo() && abstractClientPlayer.isWearing(EnumPlayerModelParts.CAPE) && abstractClientPlayer.getLocationCape() != null || abstractClientPlayer.getName().equals(Minecraft.getMinecraft().getSession().getUsername()))) {
            abstractClientPlayer.updateSimulation(abstractClientPlayer, partCount);

            smoothCapeRenderer.renderSmoothCape(this, abstractClientPlayer, deltaTick);
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}