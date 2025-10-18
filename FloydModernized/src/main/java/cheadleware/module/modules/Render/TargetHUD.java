package cheadleware.module.modules.Render;

import cheadleware.Cheadleware;
import cheadleware.enums.ChatColors;
import cheadleware.event.EventTarget;
import cheadleware.event.types.EventType;
import cheadleware.events.PacketEvent;
import cheadleware.events.Render2DEvent;
import cheadleware.module.Module;
import cheadleware.module.modules.Combat.KillAura;
import cheadleware.util.ColorUtil;
import cheadleware.util.RenderUtil;
import cheadleware.util.TeamUtil;
import cheadleware.util.TimerUtil;
import cheadleware.util.animations.Animation;
import cheadleware.util.animations.ContinualAnimation;
import cheadleware.util.animations.Direction;
import cheadleware.util.animations.impl.DecelerateAnimation;
import cheadleware.util.tenacityshaders.MathUtils;
import cheadleware.property.properties.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C02PacketUseEntity.Action;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.input.Mouse;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class TargetHUD extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final DecimalFormat healthFormat = new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.US));
    private static final DecimalFormat diffFormat = new DecimalFormat("+0.0;-0.0", new DecimalFormatSymbols(Locale.US));
    private final TimerUtil lastAttackTimer = new TimerUtil();
    private final TimerUtil animTimer = new TimerUtil();
    private final TimerUtil hitTimer = new TimerUtil();
    private EntityLivingBase lastTarget = null;
    private EntityLivingBase target = null;
    private ResourceLocation headTexture = null;
    private float oldHealth = 0.0F;
    private float newHealth = 0.0F;
    private float maxHealth = 0.0F;
    private boolean dragging = false;
    private float dragX = 0;
    private float dragY = 0;
    private ContinualAnimation healthBarAnimation;
    private ContinualAnimation trailAnimation;
    private Animation openAnimation;
    private Animation slideAnimation;
    public final ModeProperty mode = new ModeProperty("mode", 0, new String[]{"MYAU", "ATMOSPHERE"});
    public final ModeProperty color = new ModeProperty("color", 0, new String[]{"DEFAULT", "HUD"});
    public final ModeProperty posX = new ModeProperty("position-x", 1, new String[]{"LEFT", "MIDDLE", "RIGHT"});
    public final ModeProperty posY = new ModeProperty("position-y", 1, new String[]{"TOP", "MIDDLE", "BOTTOM"});
    public final FloatProperty scale = new FloatProperty("scale", 1.0F, 0.5F, 1.5F);
    public final IntProperty offX = new IntProperty("offset-x", 0, -255, 255);
    public final IntProperty offY = new IntProperty("offset-y", 40, -255, 255);
    public final PercentProperty background = new PercentProperty("background", 25);
    public final BooleanProperty head = new BooleanProperty("head", true);
    public final BooleanProperty indicator = new BooleanProperty("indicator", true, () -> this.mode.getValue() == 0);
    public final BooleanProperty outline = new BooleanProperty("outline", false, () -> this.mode.getValue() == 0);
    public final BooleanProperty animations = new BooleanProperty("animations", true);
    public final BooleanProperty shadow = new BooleanProperty("shadow", true);
    public final BooleanProperty kaOnly = new BooleanProperty("ka-only", true);
    public final BooleanProperty chatPreview = new BooleanProperty("chat-preview", false);

    public TargetHUD() {
        super("TargetHUD", false, true);
        openAnimation = new DecelerateAnimation(250, 1.0);
        slideAnimation = new DecelerateAnimation(250, 1.0);
        healthBarAnimation = new ContinualAnimation();
        trailAnimation = new ContinualAnimation();
    }

    private EntityLivingBase resolveTarget() {
        KillAura killAura = (KillAura) Cheadleware.moduleManager.modules.get(KillAura.class);
        if (killAura.isEnabled() && killAura.isAttackAllowed() && TeamUtil.isEntityLoaded(killAura.getTarget())) {
            return killAura.getTarget();
        } else if (!this.kaOnly.getValue() && !this.lastAttackTimer.hasTimeElapsed(1500L) && TeamUtil.isEntityLoaded(this.lastTarget)) {
            return this.lastTarget;
        } else {
            return this.chatPreview.getValue() && mc.currentScreen instanceof GuiChat ? mc.thePlayer : null;
        }
    }

    private ResourceLocation getSkin(EntityLivingBase entityLivingBase) {
        if (entityLivingBase instanceof EntityPlayer) {
            NetworkPlayerInfo playerInfo = mc.getNetHandler().getPlayerInfo(entityLivingBase.getName());
            if (playerInfo != null) return playerInfo.getLocationSkin();
        }
        return null;
    }

    private Color getTargetColor(EntityLivingBase entityLivingBase) {
        if (entityLivingBase instanceof EntityPlayer) {
            if (TeamUtil.isFriend((EntityPlayer) entityLivingBase)) return Cheadleware.friendManager.getColor();
            if (TeamUtil.isTarget((EntityPlayer) entityLivingBase)) return Cheadleware.targetManager.getColor();
        }
        switch (this.color.getValue()) {
            case 0: if (!(entityLivingBase instanceof EntityPlayer)) return new Color(-1);
                return TeamUtil.getTeamColor((EntityPlayer) entityLivingBase, 1.0F);
            case 1: int rgb = ((HUD) Cheadleware.moduleManager.modules.get(HUD.class)).getColor(System.currentTimeMillis()).getRGB();
                return new Color(rgb);
            default: return new Color(-1);
        }
    }

    private float calculateMyauWidth(EntityLivingBase renderTarget) {
        if (renderTarget == null) return 120;
        String targetNameText = ChatColors.formatColor(String.format("&r%s&r", TeamUtil.stripName(renderTarget)));
        int targetNameWidth = mc.fontRendererObj.getStringWidth(targetNameText);
        float headIconOffset = this.head.getValue() && this.headTexture != null ? 25.0F : 0.0F;
        return Math.max(headIconOffset + 70.0F, headIconOffset + 4.0F + targetNameWidth);
    }

    private float calculatePosX(ScaledResolution sr, float width) {
        float posX = this.offX.getValue().floatValue() / this.scale.getValue();
        switch (this.posX.getValue()) {
            case 1: posX += sr.getScaledWidth() / this.scale.getValue() / 2.0F - width / 2.0F; break;
            case 2: posX = sr.getScaledWidth() / this.scale.getValue() - width - Math.abs(posX); break;
        }
        return posX;
    }

    private float calculatePosY(ScaledResolution sr, float height) {
        float posY = this.offY.getValue().floatValue() / this.scale.getValue();
        switch (this.posY.getValue()) {
            case 1: posY += sr.getScaledHeight() / this.scale.getValue() / 2.0F - height / 2.0F; break;
            case 2: posY = sr.getScaledHeight() / this.scale.getValue() - height - Math.abs(posY); break;
        }
        return posY;
    }

    private void updateOffsets(ScaledResolution sr, float newX, float newY, float width, float height) {
        switch (this.posX.getValue()) {
            case 0: this.offX.setValue((int)(newX * this.scale.getValue())); break;
            case 1: this.offX.setValue((int)((newX - sr.getScaledWidth() / this.scale.getValue() / 2.0F + width / 2.0F) * this.scale.getValue())); break;
            case 2: this.offX.setValue((int)((sr.getScaledWidth() / this.scale.getValue() - newX - width) * -this.scale.getValue())); break;
        }
        switch (this.posY.getValue()) {
            case 0: this.offY.setValue((int)(newY * this.scale.getValue())); break;
            case 1: this.offY.setValue((int)((newY - sr.getScaledHeight() / this.scale.getValue() / 2.0F + height / 2.0F) * this.scale.getValue())); break;
            case 2: this.offY.setValue((int)((sr.getScaledHeight() / this.scale.getValue() - newY - height) * -this.scale.getValue())); break;
        }
    }

    @EventTarget
    public void onRender(Render2DEvent event) {
        if (!this.isEnabled() || mc.thePlayer == null) return;
        EntityLivingBase prevTarget = this.target;
        this.target = this.resolveTarget();
        if (this.animations.getValue()) {
            openAnimation.setDuration(250);
            slideAnimation.setDuration(250);
            if (this.target != null) {
                openAnimation.setDirection(Direction.FORWARDS);
                slideAnimation.setDirection(Direction.FORWARDS);
            } else {
                openAnimation.setDirection(Direction.BACKWARDS);
                slideAnimation.setDirection(Direction.BACKWARDS);
            }
        } else {
            openAnimation.jumpToEnd();
            slideAnimation.jumpToEnd();
        }
        if (this.target == null && openAnimation.finished(Direction.BACKWARDS)) return;
        EntityLivingBase renderTarget = this.target != null ? this.target : prevTarget;
        if (renderTarget == null) return;
        if (mc.currentScreen instanceof GuiChat) {
            ScaledResolution sr = new ScaledResolution(mc);
            int mouseX = Mouse.getX() * sr.getScaledWidth() / mc.displayWidth;
            int mouseY = sr.getScaledHeight() - Mouse.getY() * sr.getScaledHeight() / mc.displayHeight - 1;
            float width = this.mode.getValue() == 0 ? calculateMyauWidth(renderTarget) : 120;
            float height = this.mode.getValue() == 0 ? 27 : 32;
            float posX = calculatePosX(sr, width);
            float posY = calculatePosY(sr, height);
            if (Mouse.isButtonDown(0)) {
                if (!dragging) {
                    if (mouseX >= posX * this.scale.getValue() && mouseX <= (posX + width) * this.scale.getValue() &&
                            mouseY >= posY * this.scale.getValue() && mouseY <= (posY + height) * this.scale.getValue()) {
                        dragging = true;
                        dragX = mouseX - posX * this.scale.getValue();
                        dragY = mouseY - posY * this.scale.getValue();
                    }
                } else {
                    float newX = (mouseX - dragX) / this.scale.getValue();
                    float newY = (mouseY - dragY) / this.scale.getValue();
                    updateOffsets(sr, newX, newY, width, height);
                }
            } else {
                dragging = false;
            }
        }
        if (this.mode.getValue() == 0) {
            renderMyau(prevTarget, renderTarget);
        } else {
            renderAtmosphere(prevTarget, renderTarget);
        }
    }

    private void renderMyau(EntityLivingBase prevTarget, EntityLivingBase renderTarget) {
        float health = (mc.thePlayer.getHealth() + mc.thePlayer.getAbsorptionAmount()) / 2.0F;
        float abs = renderTarget.getAbsorptionAmount() / 2.0F;
        float heal = renderTarget.getHealth() / 2.0F + abs;
        if (renderTarget != prevTarget) {
            this.headTexture = null;
            this.animTimer.reset();
            this.oldHealth = heal;
            this.newHealth = heal;
            float percent = renderTarget.getMaxHealth() == 0.0f ? 1.0f : heal / (renderTarget.getMaxHealth() / 2.0f);
            healthBarAnimation.setOutput(percent);
            trailAnimation.setOutput(percent);
        }
        if (Math.abs(this.newHealth - heal) > 0.01f) {
            this.oldHealth = healthBarAnimation.getOutput() * (renderTarget.getMaxHealth() / 2.0f);
            this.newHealth = heal;
            this.maxHealth = renderTarget.getMaxHealth() / 2.0F;
            this.animTimer.reset();
        } else if (!this.animations.getValue() || this.animTimer.hasTimeElapsed(150L)) {
            this.oldHealth = this.newHealth;
            this.maxHealth = renderTarget.getMaxHealth() / 2.0F;
        }
        ResourceLocation resourceLocation = this.getSkin(renderTarget);
        if (resourceLocation != null) this.headTexture = resourceLocation;
        float elapsedTime = (float)Math.min(Math.max(this.animTimer.getElapsedTime(), 0L), 150L);
        float healthRatio = this.maxHealth != 0.0F ? Math.min(Math.max(RenderUtil.lerpFloat(this.newHealth, this.oldHealth, elapsedTime / 150.0F) / this.maxHealth, 0.0F), 1.0F) : 1.0F;
        Color targetColor = this.getTargetColor(renderTarget);
        Color healthBarColor = this.color.getValue() == 0 ? ColorUtil.getHealthBlend(healthRatio) : targetColor;
        float healthDeltaRatio = Math.min(Math.max((health - heal + 1.0F) / 2.0F, 0.0F), 1.0F);
        Color healthDeltaColor = ColorUtil.getHealthBlend(healthDeltaRatio);
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        String targetNameText = ChatColors.formatColor(String.format("&r%s&r", TeamUtil.stripName(renderTarget)));
        int targetNameWidth = mc.fontRendererObj.getStringWidth(targetNameText);
        String healthText = ChatColors.formatColor(String.format("&r&f%s%s❤&r", healthFormat.format(heal), abs > 0.0F ? "&6" : "&c"));
        int healthTextWidth = mc.fontRendererObj.getStringWidth(healthText);
        String statusText = ChatColors.formatColor(String.format("&r&l%s&r", heal == health ? "D" : (heal < health ? "W" : "L")));
        int statusTextWidth = mc.fontRendererObj.getStringWidth(statusText);
        String healthDiffText = ChatColors.formatColor(String.format("&r%s&r", heal == health ? "0.0" : diffFormat.format(health - heal)));
        int healthDiffWidth = mc.fontRendererObj.getStringWidth(healthDiffText);
        float barContentWidth = Math.max((float)targetNameWidth + (this.indicator.getValue() ? 2.0F + (float)statusTextWidth + 2.0F : 0.0F), (float)healthTextWidth + (this.indicator.getValue() ? 2.0F + (float)healthDiffWidth + 2.0F : 0.0F));
        float headIconOffset = this.head.getValue() && this.headTexture != null ? 25.0F : 0.0F;
        float barTotalWidth = Math.max(headIconOffset + 70.0F, headIconOffset + 2.0F + barContentWidth + 2.0F);
        float posX = this.offX.getValue().floatValue() / this.scale.getValue();
        switch (this.posX.getValue()) {
            case 1: posX += (float)scaledResolution.getScaledWidth() / this.scale.getValue() / 2.0F - barTotalWidth / 2.0F; break;
            case 2: posX *= -1.0F; posX += (float)scaledResolution.getScaledWidth() / this.scale.getValue() - barTotalWidth; break;
        }
        float posY = this.offY.getValue().floatValue() / this.scale.getValue();
        switch (this.posY.getValue()) {
            case 1: posY += (float)scaledResolution.getScaledHeight() / this.scale.getValue() / 2.0F - 13.5F; break;
            case 2: posY *= -1.0F; posY += (float)scaledResolution.getScaledHeight() / this.scale.getValue() - 27.0F; break;
        }
        float slideOffset = 0;
        if (this.animations.getValue()) {
            slideOffset = (1 - slideAnimation.getOutput().floatValue()) * 20;
            if (this.posX.getValue() == 0) posX -= slideOffset;
            else if (this.posX.getValue() == 2) posX += slideOffset;
        }
        GlStateManager.pushMatrix();
        float scaleAnim = Math.max(openAnimation.getOutput().floatValue(), 0.001f);
        float finalScale = this.scale.getValue() * scaleAnim;
        GlStateManager.translate(posX * this.scale.getValue(), posY * this.scale.getValue(), -450.0F);
        GlStateManager.scale(finalScale, finalScale, 0.0F);
        GlStateManager.translate(-posX, -posY, 0);
        GlStateManager.translate(posX, posY, 0);
        RenderUtil.enableRenderState();
        int backgroundColor = new Color(0.0F, 0.0F, 0.0F, (float)this.background.getValue()/100.0F * scaleAnim).getRGB();
        int outlineColor = this.outline.getValue() ? new Color(targetColor.getRed(), targetColor.getGreen(), targetColor.getBlue(), (int)(255*scaleAnim)).getRGB() : new Color(0, 0, 0, 0).getRGB();
        RenderUtil.drawOutlineRect(0.0F, 0.0F, barTotalWidth, 27.0F, 1.5F, backgroundColor, outlineColor);
        RenderUtil.drawRect(headIconOffset + 2.0F, 22.0F, barTotalWidth - 2.0F, 25.0F, ColorUtil.darker(healthBarColor, 0.2F).getRGB());
        RenderUtil.drawRect(headIconOffset + 2.0F, 22.0F, headIconOffset+2.0F+healthRatio*(barTotalWidth-2.0F-headIconOffset-2.0F), 25.0F, healthBarColor.getRGB());
        RenderUtil.disableRenderState();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0F, 1.0F, 1.0F, scaleAnim);
        mc.fontRendererObj.drawString(targetNameText, headIconOffset+2.0F, 2.0F, new Color(1,1,1,scaleAnim).getRGB(), this.shadow.getValue());
        mc.fontRendererObj.drawString(healthText, headIconOffset+2.0F, 12.0F, new Color(1,1,1,scaleAnim).getRGB(), this.shadow.getValue());
        if (this.indicator.getValue()) {
            mc.fontRendererObj.drawString(statusText, barTotalWidth-2.0F-statusTextWidth, 2.0F, new Color(healthDeltaColor.getRed()/255f, healthDeltaColor.getGreen()/255f, healthDeltaColor.getBlue()/255f, scaleAnim).getRGB(), this.shadow.getValue());
            mc.fontRendererObj.drawString(healthDiffText, barTotalWidth-2.0F-healthDiffWidth, 12.0F, ColorUtil.darker(healthDeltaColor, 0.8F).getRGB(), this.shadow.getValue());
        }
        if (this.head.getValue() && this.headTexture != null) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, scaleAnim);
            mc.getTextureManager().bindTexture(this.headTexture);
            Gui.drawScaledCustomSizeModalRect(2, 2, 8.0F, 8.0F, 8, 8, 23, 23, 64.0F, 64.0F);
            Gui.drawScaledCustomSizeModalRect(2, 2, 40.0F, 8.0F, 8, 8, 23, 23, 64.0F, 64.0F);
        }
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    private void renderAtmosphere(EntityLivingBase prevTarget, EntityLivingBase renderTarget) {
        float heal = renderTarget.getHealth() + renderTarget.getAbsorptionAmount();
        if (renderTarget != prevTarget) {
            this.headTexture = null;
            this.oldHealth = heal;
            this.newHealth = heal;
            this.animTimer.reset();
            float percent = renderTarget.getMaxHealth() + renderTarget.getAbsorptionAmount() == 0 ? 1.0f : heal/(renderTarget.getMaxHealth() + renderTarget.getAbsorptionAmount());
            healthBarAnimation.setOutput(percent);
            trailAnimation.setOutput(percent);
        }
        if (Math.abs(this.newHealth - heal) > 0.01f) {
            this.oldHealth = healthBarAnimation.getOutput() * (renderTarget.getMaxHealth() + renderTarget.getAbsorptionAmount());
            this.newHealth = heal;
            this.maxHealth = renderTarget.getMaxHealth() + renderTarget.getAbsorptionAmount();
            this.animTimer.reset();
        } else if (!this.animations.getValue() || this.animTimer.hasTimeElapsed(150L)) {
            this.oldHealth = this.newHealth;
            this.maxHealth = renderTarget.getMaxHealth() + renderTarget.getAbsorptionAmount();
        }
        ResourceLocation resourceLocation = this.getSkin(renderTarget);
        if (resourceLocation != null) this.headTexture = resourceLocation;
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        cheadleware.util.font.TrueTypeFontRenderer nameFont = cheadleware.util.font.FontManager.SANS;
        String targetName = TeamUtil.stripName(renderTarget);
        float nameWidth = nameFont != null ? nameFont.getWidth(targetName) : mc.fontRendererObj.getStringWidth(targetName);
        float width = 120, height = 32;
        float posX = this.offX.getValue().floatValue() / this.scale.getValue();
        switch (this.posX.getValue()) {
            case 1: posX += (float)scaledResolution.getScaledWidth() / this.scale.getValue() / 2.0F - width / 2.0F; break;
            case 2: posX *= -1.0F; posX += (float)scaledResolution.getScaledWidth() / this.scale.getValue() - width; break;
        }
        float posY = this.offY.getValue().floatValue() / this.scale.getValue();
        switch (this.posY.getValue()) {
            case 1: posY += (float)scaledResolution.getScaledHeight() / this.scale.getValue() / 2.0F - height / 2.0F; break;
            case 2: posY *= -1.0F; posY += (float)scaledResolution.getScaledHeight() / this.scale.getValue() - height; break;
        }
        if (this.animations.getValue()) {
            float slideOffset = (1-slideAnimation.getOutput().floatValue())*30;
            if (this.posX.getValue() == 0) posX -= slideOffset;
            else if (this.posX.getValue() == 2) posX += slideOffset;
        }
        GlStateManager.pushMatrix();
        float scaleAnim = Math.max(openAnimation.getOutput().floatValue(), 0.001f);
        float finalScale = this.scale.getValue() * scaleAnim;
        GlStateManager.translate((posX+width/2)*this.scale.getValue(), (posY+height/2)*this.scale.getValue(), -450.0F);
        GlStateManager.scale(finalScale, finalScale, 0.0F);
        GlStateManager.translate(-(posX+width/2), -(posY+height/2), 0);
        float bgAlpha = (this.background.getValue()/100.0F)*scaleAnim;
        Color backgroundColor = new Color(0, 0, 0, (int)(165*bgAlpha));
        int textColor = new Color(1, 1, 1, scaleAnim).getRGB();
        Gui.drawRect((int)posX, (int)posY, (int)(posX+width), (int)(posY+height), backgroundColor.getRGB());
        if (this.head.getValue() && renderTarget instanceof AbstractClientPlayer && this.headTexture != null) {
            float hitAlpha = 0;
            if (!this.hitTimer.hasTimeElapsed(500L)) hitAlpha = 1.0f - (this.hitTimer.getElapsedTime()/500.0f);
            GlStateManager.color(1.0F, 1.0F, 1.0F, scaleAnim);
            mc.getTextureManager().bindTexture(this.headTexture);
            Gui.drawScaledCustomSizeModalRect((int)(posX+3), (int)(posY+3), 8.0F, 8.0F, 8, 8, 26, 26, 64.0F, 64.0F);
            Gui.drawScaledCustomSizeModalRect((int)(posX+3), (int)(posY+3), 40.0F, 8.0F, 8, 8, 26, 26, 64.0F, 64.0F);
            if (hitAlpha > 0) {
                Color redFlash = new Color(1.0f, 0.0f, 0.0f, hitAlpha*scaleAnim);
                Gui.drawRect((int)(posX+3), (int)(posY+3), (int)(posX+29), (int)(posY+29), redFlash.getRGB());
            }
        }
        if (nameFont != null) { nameFont.drawStringWithShadow(targetName, posX+32, posY+5, textColor); }
        else { mc.fontRendererObj.drawString(targetName, (int)(posX+32), (int)(posY+5), textColor, this.shadow.getValue()); }
        boolean hasArmor = false;
        if (renderTarget instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)renderTarget;
            for (int i=0;i<4;i++) if (player.getCurrentArmor(i)!=null) { hasArmor=true; break; }
        }
        float healthPercent = net.minecraft.util.MathHelper.clamp_float(heal/this.maxHealth, 0, 1);
        String healthText = (int)MathUtils.round(healthPercent*100,0.01) + "%";
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.75F, 0.75F, 0.75F);
        float percentY = hasArmor ? (posY+16)/0.75F : (posY+7)/0.75F;
        if (nameFont != null) {
            float percentWidth = nameFont.getWidth(healthText)*0.75F;
            float scaledX = (posX+width-percentWidth-3)/0.75F;
            nameFont.drawStringWithShadow(healthText, scaledX, percentY, textColor);
        } else {
            float scaledX = (posX+width-mc.fontRendererObj.getStringWidth(healthText)*0.75F-3)/0.75F;
            mc.fontRendererObj.drawString(healthText, (int)scaledX, (int)percentY, textColor, this.shadow.getValue());
        }
        GlStateManager.popMatrix();
        if (renderTarget instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)renderTarget;
            int itemX = (int)(posX+32), itemY = (int)(posY+14);
            GlStateManager.pushMatrix();
            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.color(1.0F, 1.0F, 1.0F, scaleAnim);
            GlStateManager.translate(itemX, itemY, 0);
            GlStateManager.scale(0.6F, 0.6F, 0.6F);
            int offset = 0;
            ItemStack heldItem = player.getHeldItem();
            if (heldItem != null) { mc.getRenderItem().renderItemAndEffectIntoGUI(heldItem, offset, 0); offset += 18; }
            for (int i=3;i>=0;i--) {
                ItemStack armorPiece = player.getCurrentArmor(i);
                if (armorPiece != null) { mc.getRenderItem().renderItemAndEffectIntoGUI(armorPiece, offset, 0); offset +=18; }
            }
            RenderHelper.disableStandardItemLighting();
            GlStateManager.popMatrix();
        }
        float barWidth = 85, barHeight = 3, barX = posX+32, barY = posY+26;
        float targetHealthPercent = net.minecraft.util.MathHelper.clamp_float(heal/this.maxHealth, 0, 1);
        int animDuration = this.animations.getValue() ? 300 : 1;
        healthBarAnimation.animate(targetHealthPercent, animDuration);
        float currentHealthPercent = healthBarAnimation.getOutput();
        int trailDuration = this.animations.getValue() ? 600 : 1;
        trailAnimation.animate(targetHealthPercent, trailDuration);
        float currentTrailPercent = trailAnimation.getOutput();
        float currentHealthWidth = barWidth * currentHealthPercent;
        float trailWidth = barWidth * currentTrailPercent;
        Color healthBarColor = this.getTargetColor(renderTarget);
        Color trailColor = ColorUtil.applyOpacity(healthBarColor, 0.35f);
        Gui.drawRect((int)barX, (int)barY, (int)(barX+barWidth), (int)(barY+barHeight), new Color(0, 0, 0, (int)(180*bgAlpha)).getRGB());
        if (trailWidth > currentHealthWidth) {
            Color adjustedTrailColor = new Color(trailColor.getRed()/255f, trailColor.getGreen()/255f, trailColor.getBlue()/255f, trailColor.getAlpha()/255f*scaleAnim);
            Gui.drawRect((int)barX, (int)barY, (int)(barX+trailWidth), (int)(barY+barHeight), adjustedTrailColor.getRGB());
        }
        Color adjustedHealthColor = new Color(healthBarColor.getRed()/255f, healthBarColor.getGreen()/255f, healthBarColor.getBlue()/255f, scaleAnim);
        Gui.drawRect((int)barX, (int)barY, (int)(barX+currentHealthWidth), (int)(barY+barHeight), adjustedHealthColor.getRGB());
        GlStateManager.color(1.0F,1.0F,1.0F,1.0F);
        GlStateManager.popMatrix();
    }
}