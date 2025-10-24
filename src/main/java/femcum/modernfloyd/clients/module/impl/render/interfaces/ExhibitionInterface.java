package femcum.modernfloyd.clients.module.impl.render.interfaces;

import femcum.modernfloyd.clients.component.impl.player.PingComponent;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.other.TickEvent;
import femcum.modernfloyd.clients.event.impl.render.Render2DEvent;
import femcum.modernfloyd.clients.module.impl.render.Interface;
import femcum.modernfloyd.clients.module.impl.render.interfaces.api.ModuleComponent;
import femcum.modernfloyd.clients.util.font.Font;
import femcum.modernfloyd.clients.util.render.ColorUtil;
import femcum.modernfloyd.clients.util.render.RenderUtil;
import femcum.modernfloyd.clients.util.vector.Vector2d;
import femcum.modernfloyd.clients.value.Mode;
import femcum.modernfloyd.clients.value.impl.BooleanValue;
import femcum.modernfloyd.clients.value.impl.ModeValue;
import femcum.modernfloyd.clients.value.impl.StringValue;
import femcum.modernfloyd.clients.value.impl.SubMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExhibitionInterface extends Mode<Interface> {

    private final ModeValue colorMode = new ModeValue("ArrayList Color Mode", this) {{
        add(new SubMode("Static"));
        add(new SubMode("Rainbow"));
        add(new SubMode("Fade"));
        setDefault("Rainbow");
    }};

    private final ModeValue alignment = new ModeValue("ArrayList Alignment", this) {{
        add(new SubMode("Right"));
        add(new SubMode("Left"));
        setDefault("Right");
    }};

    private final BooleanValue sideBar = new BooleanValue("Arraylist Side Bar", this, false);
    private final BooleanValue background = new BooleanValue("Arraylist Background", this, false);
    private final BooleanValue showFpsOption = new BooleanValue("Show Fps", this, false);
    private final BooleanValue showTimeOption = new BooleanValue("Show Time", this, false);
    private final BooleanValue showPingOption = new BooleanValue("Show Ping", this, false);
    private final BooleanValue showCoordinates = new BooleanValue("Show Coordinates", this, true);
    private final BooleanValue showVersion = new BooleanValue("Show Version", this, true);
    private final StringValue customClientName = new StringValue("Custom Floyd Name", this, "FloydV");

    public ExhibitionInterface(String name, Interface parent) {
        super(name, parent);
    }

    private static int rainbow(int delay) {
        double rainbowState = Math.ceil((System.currentTimeMillis() + delay) / 10.0);
        rainbowState %= 360;
        return Color.getHSBColor((float) (rainbowState / 360.0f), 0.6f, 1f).getRGB();
    }

    private Color darkerColor(Color color, float factor) {
        return new Color(
                Math.max((int)(color.getRed() * factor), 0),
                Math.max((int)(color.getGreen() * factor), 0),
                Math.max((int)(color.getBlue() * factor), 0),
                color.getAlpha()
        );
    }

    @EventLink
    public final Listener<Render2DEvent> onRender2D = event -> {
        if (mc == null || mc.gameSettings.showDebugInfo || mc.theWorld == null || mc.thePlayer == null) {
            return;
        }

        Font interfaceFont = mc.fontRendererObj;

        int moduleCount = 0;
        Color[] colors = {new Color(91, 206, 250), new Color(245, 169, 184), Color.WHITE, new Color(245, 169, 184)};
        int colorIndex = 0;

        this.getParent().setModuleSpacing(this.mc.fontRendererObj.height() + 2F);
        this.getParent().setWidthComparator(this.mc.fontRendererObj);
        this.getParent().setEdgeOffset(3);

        double y = mc.scaledResolution.getScaledHeight() - 10;
        String posX2 = String.valueOf(Math.round(mc.thePlayer.posX));
        String posY2 = String.valueOf(Math.round(mc.thePlayer.posY));
        String posZ2 = String.valueOf(Math.round(mc.thePlayer.posZ));

        // ArrayList rendering
        for (final ModuleComponent moduleComponent : this.getParent().getActiveModuleComponents()) {
            if (moduleComponent.animationTime == 0) {
                continue;
            }
            final boolean hasTag = !moduleComponent.getTag().isEmpty() && this.getParent().suffix.getValue();

            double posX = moduleComponent.getPosition().getX();
            double posY = moduleComponent.getPosition().getY();

            if (alignment.getValue().getName().equals("Left")) {
                posY = moduleComponent.getPosition().getY() + interfaceFont.height() + 2;
                if (sideBar.getValue()) {
                    posX = 3 + moduleComponent.animationTime - 8;
                } else {
                    posX = 3 + moduleComponent.animationTime - 10;
                }
            }

            Color finalColor = moduleComponent.getColor();
            Color color = this.getTheme().getFirstColor();

            switch (colorMode.getValue().getName()) {
                case "Fade":
                    color = this.getTheme().getAccentColor(new Vector2d(0, moduleComponent.getPosition().getY()));
                    break;

                case "Rainbow":
                    color = new Color(rainbow(500 * moduleCount / 6));
                    break;

                case "Trans":
                    finalColor = colors[colorIndex];
                    break;
            }

            if (background.getValue()) {
                RenderUtil.rectangle(posX - 2, posY - 2, (moduleComponent.nameWidth + moduleComponent.tagWidth) + 4, this.getParent().moduleSpacing, getTheme().getBackgroundShade());
            }

            if (sideBar.getValue()) {
                if (this.alignment.getValue().getName().equals("Left")) {
                    RenderUtil.rectangle(posX - (this.background.getValue() ? 2 : 3), posY - 2, 1, this.getParent().moduleSpacing, color);
                } else {
                    RenderUtil.rectangle(posX + (moduleComponent.nameWidth + moduleComponent.tagWidth) + 2, posY - 2, 1, this.getParent().moduleSpacing, color);
                }
            }

            interfaceFont.drawWithShadow(moduleComponent.getDisplayName(), posX, posY, finalColor.getRGB());
            moduleCount++;
            colorIndex = (colorIndex + 1) % colors.length;

            if (hasTag) {
                interfaceFont.drawWithShadow(moduleComponent.getDisplayTag(), posX + moduleComponent.getNameWidth() + 3, posY, 0xFFCCCCCC);
            }

            moduleComponent.setColor(color);
        }

        // Floyd v4 style watermark
        Color primaryColor = colorMode.getValue().getName().equals("Rainbow") ?
                new Color(rainbow(1000)) : getTheme().getFirstColor();
        Color secondaryColor = colorMode.getValue().getName().equals("Rainbow") ?
                new Color(rainbow(1500)) : getTheme().getSecondColor();

        drawFloydStyleWatermark(interfaceFont, primaryColor, secondaryColor);

        // Coordinates
        int colour = colorMode.getValue().getName().equals("Rainbow") ? rainbow(1000) : getTheme().getFirstColor().getRGB();
        if (showCoordinates.getValue()) {
            interfaceFont.drawWithShadow("X:§7 " + posX2, 3, y - mc.fontRendererObj.height() * 2, colour);
            interfaceFont.drawWithShadow("Y:§7 " + posY2, 3, y - mc.fontRendererObj.height(), colour);
            interfaceFont.drawWithShadow("Z:§7 " + posZ2, 3, y, colour);
        }
    };

    private void drawFloydStyleWatermark(Font font, Color clientColor1, Color clientColor2) {
        float xVal = 5;
        float yVal = 5;
        float spacing = 1;

        String clientName = customClientName.getValue().isEmpty() ? "FloydV" : customClientName.getValue();

        float nameWidth = font.width(clientName);
        float versionWidth = showVersion.getValue() ? font.width("v1.0") : 0;
        float versionX = xVal + nameWidth;
        float totalWidth = showVersion.getValue() ? (versionX + versionWidth) - xVal : nameWidth;

        // Create darker colors for shadow effect (exactly like dev.floyd)
        Color darkerColor1 = darkerColor(clientColor1, 0.6f);
        Color darkerColor2 = darkerColor(clientColor2, 0.6f);

        // Draw darker shadow layer first (offset by spacing) - EXACTLY like Floyd v4
        GlStateManager.pushMatrix();
        RenderUtil.start();
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glBegin(GL11.GL_QUADS);

        // Shadow layer gradient
        ColorUtil.glColor(darkerColor1);
        GL11.glVertex2d(xVal + spacing, yVal + spacing);
        GL11.glVertex2d(xVal + spacing, yVal + spacing + 20);

        ColorUtil.glColor(darkerColor2);
        GL11.glVertex2d(xVal + spacing + totalWidth, yVal + spacing + 20);
        GL11.glVertex2d(xVal + spacing + totalWidth, yVal + spacing);

        GL11.glEnd();
        GL11.glShadeModel(GL11.GL_FLAT);
        RenderUtil.stop();

        // Draw text on shadow layer with alpha 0 (mask)
        font.draw(clientName, xVal + spacing, yVal + spacing, 0);
        if (showVersion.getValue()) {
            font.draw("v1.0", versionX + (spacing / 2f), yVal + (spacing / 2f), 0);
        }

        // Draw main gradient layer on top
        RenderUtil.start();
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glBegin(GL11.GL_QUADS);

        // Main layer gradient
        ColorUtil.glColor(clientColor1);
        GL11.glVertex2d(xVal, yVal);
        GL11.glVertex2d(xVal, yVal + 20);

        ColorUtil.glColor(clientColor2);
        GL11.glVertex2d(xVal + totalWidth, yVal + 20);
        GL11.glVertex2d(xVal + totalWidth, yVal);

        GL11.glEnd();
        GL11.glShadeModel(GL11.GL_FLAT);
        RenderUtil.stop();

        // Draw text on main layer with alpha 0 (mask)
        font.draw(clientName, xVal, yVal, 0);
        if (showVersion.getValue()) {
            font.draw("v1.0", versionX, yVal, 0);
        }

        GlStateManager.popMatrix();
    }

    @EventLink
    public final Listener<TickEvent> onTick = event -> threadPool.execute(() -> {
        for (final ModuleComponent moduleComponent : this.getParent().getActiveModuleComponents()) {
            if (moduleComponent.animationTime == 0) {
                continue;
            }
            Font interfaceFont = mc.fontRendererObj;

            moduleComponent.setHasTag(!moduleComponent.getTag().isEmpty() && this.getParent().suffix.getValue());
            String name = (this.getParent().lowercase.getValue() ? moduleComponent.getTranslatedName().toLowerCase() : moduleComponent.getTranslatedName())
                    .replace(getParent().getRemoveSpaces().getValue() ? " " : "", "");

            String tag = (this.getParent().lowercase.getValue() ? moduleComponent.getTag().toLowerCase() : moduleComponent.getTag())
                    .replace(getParent().getRemoveSpaces().getValue() ? " " : "", "");

            moduleComponent.setNameWidth(interfaceFont.width(name));
            moduleComponent.setTagWidth(moduleComponent.isHasTag() ? (interfaceFont.width(tag) + 3) : 0);
            moduleComponent.setDisplayName(name);
            moduleComponent.setDisplayTag(tag);
        }
    });
}