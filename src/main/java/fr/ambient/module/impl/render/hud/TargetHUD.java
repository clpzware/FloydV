package fr.ambient.module.impl.render.hud;

import fr.ambient.Ambient;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.render.Render2DEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.module.impl.combat.KillAura;
import fr.ambient.property.impl.BooleanProperty;
import fr.ambient.property.impl.ModeProperty;
import fr.ambient.ui.framework.Style;
import fr.ambient.ui.framework.TextAlignment;
import fr.ambient.ui.framework.UIComponent;
import fr.ambient.ui.framework.impl.UIHeadComponent;
import fr.ambient.ui.framework.impl.UIProgressComponent;
import fr.ambient.ui.framework.impl.UITextComponent;
import fr.ambient.util.render.MaterialThemePicker;
import fr.ambient.util.render.RenderUtil;
import fr.ambient.util.render.animation.Animation;
import fr.ambient.util.render.animation.Easing;
import fr.ambient.util.render.font.Fonts;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.entity.player.EntityPlayer;

import java.awt.*;
import java.text.DecimalFormat;

public class TargetHUD extends Module {
    private final ModeProperty mode = ModeProperty.newInstance("Mode", new String[] {"Modern", "Minimal", "Basic", "Minecraft"}, "Modern");
    private final BooleanProperty postProcess = BooleanProperty.newInstance("Post Process", true, () -> !mode.is("Basic") && !mode.is("Minecraft"));
    private final Animation animation = new Animation(Easing.EASE_OUT_SINE, 300);
    private final Animation scaleAnimation = new Animation(Easing.EASE_OUT_BACK, 200);

    public TargetHUD() {
        super(61,"Shows detailed information about the player you're targeting.", ModuleCategory.RENDER);
        this.setDraggable(true);
        this.setX(200);
        this.setY(200);
        this.registerProperties(mode, postProcess);
    }

    private EntityPlayer lastTarget;
    private long lastTargetTime;
    private DecimalFormat df = new DecimalFormat("#.#");

    @SubscribeEvent
    private void onRender(Render2DEvent event) {
        scaleAnimation.run(KillAura.target != null || mc.currentScreen instanceof GuiChat ? 1.0 : 0.0);

       if (KillAura.target instanceof EntityPlayer player) {
            if (lastTarget != player) lastTarget = player;
            lastTargetTime = System.currentTimeMillis();
            drawTargetHUD(this.getX(), this.getY(), player);
        } else if (mc.currentScreen instanceof GuiChat) {
            if (lastTarget != mc.thePlayer) lastTarget = mc.thePlayer;
            lastTargetTime = System.currentTimeMillis();
            drawTargetHUD(this.getX(), this.getY(), mc.thePlayer);
        } else if (lastTarget != null) {
            long elapsed = System.currentTimeMillis() - lastTargetTime;

            if (elapsed < 110) {
                drawTargetHUD(this.getX(), this.getY(), lastTarget);
            } else {
                lastTarget = null;
            }
        }
    }

    private int[] drawTargetHUD(float x, float y, EntityPlayer target) {
        double scale = scaleAnimation.getValue();
        float hudWidth;

        switch (mode.getValue()) {
            case "Modern" -> {
                hudWidth = Fonts.getOpenSansBold(15).getWidth(target.getName()) + 80;
                changeSize(hudWidth, 36);

                double healthRatio = (target.getHealth() + target.getAbsorptionAmount()) / (target.getMaxHealth() + target.getAbsorptionAmount());

                animation.run(healthRatio);

                Color color1 = Ambient.getInstance().getHud().getCurrentTheme().color1;
                Color color2 = Ambient.getInstance().getHud().getCurrentTheme().color2;
                Color textColor = MaterialThemePicker.findClosestTheme(Ambient.getInstance().getHud().getCurrentTheme().color2).getShade(200);

                modern.position(x, y);
                modern.size(hudWidth, 36);

                modern.findChild(UIHeadComponent.class, "targetHead").player(target);
                modern.findChild(UITextComponent.class, "targetName").text(target.getName());
                modern.findChild(UITextComponent.class, "targetHealth").text(String.format("%.1f", target.getHealth() + target.getAbsorptionAmount()) + "hp").color(Style.SOLID, textColor);
                modern.findChild(UITextComponent.class, "targetDistance").text(String.format("%.1f", mc.thePlayer.getDistanceToEntity(target)) + "m").color(Style.SOLID, textColor);
                modern.findChild(UIProgressComponent.class, "healthBar").progress((float) animation.getValue()).color(Style.HORIZONTAL, color1, color2);

                modern.render(scale);

                return new int[]{(int) hudWidth, 36};
            }
            case "Minimal" -> {
                hudWidth = 44 + Fonts.getNunito(18).getWidth(target.getName() + String.format("%.1f", target.getHealth()));
                float testHeight = 35;
                changeSize(hudWidth, testHeight);

                double healthRatio = (target.getHealth() + target.getAbsorptionAmount()) / (target.getMaxHealth() + target.getAbsorptionAmount());

                animation.run(healthRatio);

                minimal.position(x, y);
                minimal.size(hudWidth, testHeight);

                minimal.findChild(UIHeadComponent.class, "targetHead").player(target);
                minimal.findChild(UITextComponent.class, "targetName").text(target.getName());

                minimal.findChild(UITextComponent.class, "targetHealth").text(String.format("%.1f", target.getHealth() + target.getAbsorptionAmount()));
                minimal.findChild(UIProgressComponent.class, "healthBar").progress((float) animation.getValue());
                minimal.findChild(UIProgressComponent.class, "armorBar").progress(target.getTotalArmorValue() / 20f);

                minimal.render(scale);

                return new int[] {(int) hudWidth, (int) testHeight};
            }
            case "Basic" -> {
                hudWidth = 60 + Fonts.getSanFrancisco(18).getWidth(target.getName());

                changeSize(Math.max(hudWidth, 100), 29);

                float healthPercentage = (target.getHealth() + target.getAbsorptionAmount()) / (target.getMaxHealth() + target.getAbsorptionAmount());
                animation.run(healthPercentage);

                Color color1 = Ambient.getInstance().getHud().getCurrentTheme().color1;
                Color color2 = Ambient.getInstance().getHud().getCurrentTheme().color2;

                basic.position(x, y);
                basic.size(Math.max(hudWidth, 100), 29);

                basic.findChild(UIHeadComponent.class, "targetHead").player(target);
                basic.findChild(UITextComponent.class, "targetName").text(target.getName());

                basic.findChild(UITextComponent.class, "targetHealth").text(String.format("%.1f", target.getHealth() + target.getAbsorptionAmount()));
                basic.findChild(UIProgressComponent.class, "healthBar").progress((float) animation.getValue());
                basic.findChild(UIProgressComponent.class, "healthBar").color(Style.HORIZONTAL, color1, color2);

                basic.render(scale);

                return new int[]{(int) Math.max(hudWidth, 100), 29};
            }
            case "Minecraft" -> {
                String warning;
                float healthDiff = (target.getHealth() + target.getAbsorptionAmount()) - (mc.thePlayer.getHealth() + mc.thePlayer.getAbsorptionAmount());
                if (healthDiff >= 4)
                    warning = "§c§l⚠ §r";
                else {
                    warning = "";
                }

                hudWidth = 45 + mc.fontRendererObj.getStringWidth(warning + target.getName() + " " + df.format(target.getHealth()));
                changeSize(Math.max(hudWidth, 60), 28);

                float targetHealth = target.getHealth() + target.getAbsorptionAmount();
                String formattedHealth = df.format(targetHealth);

                float healthPercentage = (target.getHealth() + target.getAbsorptionAmount()) /
                        (target.getMaxHealth() + target.getAbsorptionAmount());
                animation.run(healthPercentage);

                float armorPercentage = target.getTotalArmorValue() / 20f;

                int healthColor;
                if (targetHealth > 12) {
                    healthColor = 0xFF009E60;
                } else if (targetHealth > 7) {
                    healthColor = 0xFFFF5F15;
                } else {
                    healthColor = 0xFFB70000;
                }

                RenderUtil.scale(() -> {
                    RenderUtil.drawRect(x, y, getWidth(), getHeight(), new Color(0x70000000, true));
                    RenderUtil.drawHead(target, x + 2, y + 2, getHeight() - 4);

                    mc.fontRendererObj.drawStringWithShadow(warning + target.getDisplayName().getFormattedText(), x + 30, y + 4, -1);
                    mc.fontRendererObj.drawStringWithShadow(formattedHealth,
                            x + getWidth() - mc.fontRendererObj.getStringWidth(formattedHealth) - 2,
                            y + 4,
                            healthColor);

                    RenderUtil.drawRect(x + 30, y + getHeight() - 13, getWidth() - 32, 4, new Color(0x60000000, true));
                    RenderUtil.horizontalGradient(x + 30,
                            y + getHeight() - 13,
                            (getWidth() - 32) * animation.getFloatValue(),
                            4,
                            new Color(0x009E60),
                            new Color(0x00683E));

                    RenderUtil.drawRect(x + 30, y + getHeight() - 6, getWidth() - 32, 4, new Color(0x60000000, true));
                    RenderUtil.horizontalGradient(x + 30,
                            y + getHeight() - 6,
                            (getWidth() - 32) * armorPercentage,
                            4,
                            new Color(0x6495ED),
                            new Color(0x4566A0));
                }, getX() + getWidth() / 2f, getY() + getHeight() / 2f, scale);

                return new int[]{(int) Math.max(hudWidth, 100), 30};
            }
        }

        this.setWidth(0);
        this.setHeight(0);
        return new int[]{0, 0};
    }

    UIComponent modern = new UIComponent()
            .position(500, 275)
            .size(120, 35)
            .rounding(7)
            .color(Style.SOLID, new Color(0x90121214, true))
            .glow(Color.BLACK)
            .blur(12)
            .children(
                    new UIHeadComponent()
                            .margin(4, 4, 0, 4)
                            .maxWidth(27)
                            .rounding(3)
                            .id("targetHead"),
                    new UITextComponent()
                            .font(Fonts.getNunito(16))
                            .color(Style.SOLID, Color.WHITE)
                            .margin(36, 5, 0, 0)
                            .id("targetName"),
                    new UITextComponent()
                            .alignment(TextAlignment.RIGHT)
                            .font(Fonts.getNunito(14))
                            .color(Style.SOLID, new Color(0xFF38ef7d))
                            .margin(36, 16, 4, 4)
                            .id("targetHealth"),
                    new UITextComponent()
                            .font(Fonts.getNunito(14))
                            .color(Style.SOLID, new Color(0xFF38ef7d))
                            .margin(36, 16, 4, 4)
                            .id("targetDistance"),
                    new UIProgressComponent()
                            .background(0, 0, 0, 100)
                            .progress(0.9f)
                            .margin(36, 26, 4, 4)
                            .rounding(2.5f)
                            .color(Style.HORIZONTAL, new Color(0xFF11998e), new Color(0xFF38ef7d))
                            .id("healthBar")
            );

    UIComponent minimal = new UIComponent()
            .position(500, 275)
            .size(120, 35)
            .rounding(7)
            .color(Style.SOLID, new Color(0x80000000, true))
            .glow(new Color(0xAA000000, true))
            .blur(12)
            .children(
                    new UIHeadComponent()
                            .id("targetHead")
                            .margin(4, 4, 0, 4)
                            .maxWidth(27)
                            .rounding(3),
                    new UITextComponent()
                            .font(Fonts.getNunito(18))
                            .color(Style.SOLID, Color.WHITE)
                            .margin(35, 5, 0, 0)
                            .id("targetName"),
                    new UITextComponent()
                            .alignment(TextAlignment.RIGHT)
                            .font(Fonts.getNunito(18))
                            .color(Style.SOLID, new Color(0xFF38ef7d))
                            .margin(35, 5, 4, 4)
                            .id("targetHealth"),
                    new UIProgressComponent()
                            .background(0, 0, 0, 100)
                            .progress(0.9f)
                            .margin(35, 17, 4, 13)
                            .rounding(2)
                            .color(Style.HORIZONTAL, new Color(0xFF11998e), new Color(0xFF38ef7d))
                            .id("healthBar"),
                    new UIProgressComponent()
                            .background(0, 0, 0, 100)
                            .progress(0.8f)
                            .margin(35, 25, 4, 5)
                            .rounding(2)
                            .color(Style.HORIZONTAL, new Color(0xFF5B86E5), new Color(0xFF36D1DC))
                            .id("armorBar")
            );

    UIComponent basic = new UIComponent()
            .position(500, 275)
            .size(100, 29)
            .rounding(0)
            .color(Style.SOLID, new Color(0x70000000, true))
            .children(
                    new UIHeadComponent()
                            .margin(2, 2, 0, 2)
                            .maxWidth(25)
                            .id("targetHead"),
                    new UITextComponent()
                            .font(Fonts.getSanFrancisco(18))
                            .color(Style.SOLID, Color.WHITE)
                            .margin(29, 3, 0, 0)
                            .id("targetName"),
                    new UITextComponent()
                            .font(Fonts.getSanFrancisco(16))
                            .alignment(TextAlignment.RIGHT)
                            .color(Style.SOLID, Color.GREEN)
                            .margin(0, 4, 3, 0)
                            .id("targetHealth"),
                    new UIProgressComponent()
                            .background(new Color(0, 0, 0, 100))
                            .progress(0.8f)
                            .rounding(0)
                            .margin(29, 23, 2, 2)
                            .color(Style.HORIZONTAL, Color.WHITE, Color.WHITE)
                            .id("healthBar")
            );

    private void changeSize(float width, float height){
        this.setHeight(height);
        this.setWidth(width);
    }
}
