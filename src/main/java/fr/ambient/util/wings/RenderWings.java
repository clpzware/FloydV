package fr.ambient.util.wings;

import cc.polymorphism.annot.ExcludeConstant;
import cc.polymorphism.annot.ExcludeFlow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

@ExcludeFlow
@ExcludeConstant
public class RenderWings extends ModelBase {
    private Minecraft mc = Minecraft.getMinecraft();
    private ResourceLocation location;
    private ModelRenderer wing;
    private ModelRenderer wingTip;

    /*
    *   Taken from 1.8 Wings Mod
    *   https://github.com/Canelex/DragonWingsMod
    *  */


    public RenderWings() {
        this.location = new ResourceLocation("dogclient/wings/wings.png");
        this.setTextureOffset("wing.bone", 0, 0);
        this.setTextureOffset("wing.skin", -10, 8);
        this.setTextureOffset("wingtip.bone", 0, 5);
        this.setTextureOffset("wingtip.skin", -10, 18);
        this.wing = new ModelRenderer(this, "wing");
        this.wing.setTextureSize(30, 30);
        this.wing.setRotationPoint(-2.0f, 0.0f, 0.0f);
        this.wing.addBox("bone", -10.0f, -1.0f, -1.0f, 10, 2, 2);
        this.wing.addBox("skin", -10.0f, 0.0f, 0.5f, 10, 0, 10);
        this.wingTip = new ModelRenderer(this, "wingtip");
        this.wingTip.setTextureSize(30, 30);
        this.wingTip.setRotationPoint(-10.0f, 0.0f, 0.0f);
        this.wingTip.addBox("bone", -10.0f, -0.5f, -0.5f, 10, 1, 1);
        this.wingTip.addBox("skin", -10.0f, 0.0f, 0.5f, 10, 0, 10);
        this.wing.addChild(this.wingTip);
    }

    public void renderWings(EntityPlayer player, float partialTicks) {
        double scale = (100 / 100.0);
        double rotate = Math.abs(this.interpolate(player.prevRenderYawOffset,player.renderYawOffset,partialTicks));
        GL11.glPushMatrix();

        double x1 = (player.lastTickPosX + (player.posX - player.lastTickPosX) * mc.timer.renderPartialTicks) - RenderManager.renderPosX;
        double y1 = (player.lastTickPosY + (player.posY - player.lastTickPosY) * mc.timer.renderPartialTicks) - RenderManager.renderPosY;
        double z1 = (player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * mc.timer.renderPartialTicks) - RenderManager.renderPosZ;
        double bbs= y1 + 0.3;
        GL11.glTranslated(-x1, bbs, z1);
        GL11.glScaled(-scale, scale, scale);
        GL11.glRotated(1 /  (MathHelper.wrapAngleTo180_float((float) (180 - rotate))), 0.0, 0.0, 0.0);
        GL11.glTranslated(-x1, -bbs, -z1);




        if (player.isSneaking()) {
            GL11.glTranslated(0.0, 0.125, 0.0);
        }
        GL11.glColor3f(0.9f, 0.9f, 0.9f);
        Minecraft.getMinecraft().getTextureManager().bindTexture(location);
        int j = 0;

        GL11.glEnable(2884);
        float f11 = (System.currentTimeMillis() % 1000L) / 1000.0f * 3.1415927f * 2.0f;
        wing.rotateAngleX = (float) (Math.toRadians(-40.0) - Math.cos(f11) * -0.6f);
        wing.rotateAngleY = (float) (Math.toRadians(30.0) + Math.sin(f11) * 0.4f);
        wing.rotateAngleZ = (float) Math.toRadians(30.0);
        wingTip.rotateAngleZ = (float) (-(Math.sin((f11 + 1.2f)) + 1.1) * 0.75f);
        wing.render(0.0625f);
        GL11.glScalef(-1.0f, 1.0f, 1.0f);
        GL11.glCullFace(1028);
        GL11.glEnable(2884);
        f11 = (System.currentTimeMillis() % 1000L) / 1000.0f * 3.1415927f * 2.0f;
        wing.rotateAngleX = (float) (Math.toRadians(-40.0) - Math.cos(f11) * -0.6f);
        wing.rotateAngleY = (float) (Math.toRadians(30.0) + Math.sin(f11) * 0.4f);
        wing.rotateAngleZ = (float) Math.toRadians(30.0);
        wingTip.rotateAngleZ = (float) (-(Math.sin((f11 + 1.2f)) + 1.1) * 0.75f);
        wing.render(0.0625f);
        GL11.glScalef(-1.0f, 1.0f, 1.0f);
        GL11.glCullFace(1028);



        GL11.glCullFace(1029);
        GL11.glDisable(2884);
        GL11.glColor3f(255.0f, 255.0f, 255.0f);
        GL11.glPopMatrix();
    }

    private float interpolate(float yaw1, float yaw2, float percent) {
        float f = (yaw1 + (yaw2 - yaw1) * percent) % 360.0f;
        if (f < 0.0f) {
            f += 360.0f;
        }
        return f;
    }




}