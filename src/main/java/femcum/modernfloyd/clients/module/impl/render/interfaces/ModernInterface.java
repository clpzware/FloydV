package femcum.modernfloyd.clients.module.impl.render.interfaces;

import femcum.modernfloyd.clients.Floyd;
import femcum.modernfloyd.clients.component.impl.render.ParticleComponent;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.other.KillEvent;
import femcum.modernfloyd.clients.event.impl.other.TickEvent;
import femcum.modernfloyd.clients.event.impl.render.Render2DEvent;
import femcum.modernfloyd.clients.font.Fonts;
import femcum.modernfloyd.clients.font.Weight;
import femcum.modernfloyd.clients.module.impl.render.Interface;
import femcum.modernfloyd.clients.module.impl.render.interfaces.api.ModuleComponent;
import femcum.modernfloyd.clients.util.font.Font;
import femcum.modernfloyd.clients.util.render.ColorUtil;
import femcum.modernfloyd.clients.util.render.RenderUtil;
import femcum.modernfloyd.clients.util.render.particle.Particle;
import femcum.modernfloyd.clients.util.vector.Vector2d;
import femcum.modernfloyd.clients.util.vector.Vector2f;
import femcum.modernfloyd.clients.value.Mode;
import femcum.modernfloyd.clients.value.impl.BooleanValue;
import femcum.modernfloyd.clients.value.impl.ModeValue;
import femcum.modernfloyd.clients.value.impl.NumberValue;
import femcum.modernfloyd.clients.value.impl.StringValue;
import femcum.modernfloyd.clients.value.impl.SubMode;
import rip.vantage.commons.util.time.StopWatch;

import java.awt.*;
import java.util.Optional;

import static femcum.modernfloyd.clients.font.Fonts.*;
import static femcum.modernfloyd.clients.layer.Layers.*;

public class ModernInterface extends Mode<Interface> {

    private final Font productSansMedium36 = MAIN.get(36, Weight.MEDIUM);
    private final Font productSansRegular18 = MAIN.get(18, Weight.REGULAR);
    private final Font productSansMedium18 = MAIN.get(18, Weight.MEDIUM);
    private final Font productSansRegular = MAIN.get(18, Weight.REGULAR);
    private Font arrayListFont = productSansRegular;
    private final StopWatch stopWatch = new StopWatch();

    private final ModeValue colorMode = new ModeValue("ArrayList Color Mode", this) {{
        add(new SubMode("Static"));
        add(new SubMode("Fade"));
        add(new SubMode("Breathe"));
        setDefault("Fade");
    }};

    private final ModeValue fontMode = new ModeValue("ArrayList Font", this) {{
        add(new SubMode("Product Sans"));
        add(new SubMode("Minecraft"));
        add(new SubMode("Custom"));
        setDefault("Product Sans");
    }};

    private final StringValue customFont = new StringValue("Custom Installed Font", this, "Arial", () -> !fontMode.getValue().getName().equals("Custom"));

    private final ModeValue shader = new ModeValue("Shader Effect", this) {{
        add(new SubMode("Glow"));
        add(new SubMode("Shadow"));
        add(new SubMode("None"));
        setDefault("Shadow");
    }};

    private final BooleanValue dropShadow = new BooleanValue("Drop Shadow", this, true);
    private final BooleanValue sidebar = new BooleanValue("Sidebar", this, true);
    private final BooleanValue particles = new BooleanValue("Particles on Kill", this, true);
    private final ModeValue background = new ModeValue("BackGround", this) {{
        add(new SubMode("Off"));
        add(new SubMode("Normal"));
        add(new SubMode("Blur"));
        setDefault("Normal");
    }};

    private final StringValue customClientName = new StringValue("Custom Floyd Name", this, "");

    // Watermark settings
    private final BooleanValue watermarkBackground = new BooleanValue("Watermark Background", this, true);
    private final NumberValue watermarkBackgroundAlpha = new NumberValue("Watermark BG Alpha", this, 0.7, 1.0, 0.0, 0.05);
    private final NumberValue watermarkPaddingX = new NumberValue("Watermark Padding X", this, 10.0, 20.0, 4.0, 0.5);
    private final NumberValue watermarkPaddingY = new NumberValue("Watermark Padding Y", this, 7.0, 15.0, 3.0, 0.5);
    private final NumberValue watermarkCornerRadius = new NumberValue("Watermark Radius", this, 8.0, 15.0, 0.0, 0.5);
    private final NumberValue watermarkShadowSize = new NumberValue("Watermark Shadow", this, 8.0, 15.0, 0.0, 1.0);

    private boolean glow, shadow;
    private boolean normalBackGround, blurBackGround;
    private String username, coordinates;
    private Color logoColor;
    private float xyzWidth;

    public ModernInterface(String name, Interface parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<Render2DEvent> onRender2D = event -> {
        if (mc == null || mc.gameSettings.showDebugInfo || mc.theWorld == null || mc.thePlayer == null) {
            return;
        }

        boolean minecraft = arrayListFont == MINECRAFT.get();
        this.getParent().setModuleSpacing((minecraft ? 1.5F : 0.0F) + arrayListFont.height());
        this.getParent().setWidthComparator(arrayListFont);
        this.getParent().setEdgeOffset(10);

        float sy = event.getScaledResolution().getScaledHeight() - arrayListFont.height() - 1;
        final double widthOffset = minecraft ? 3.5 : 2;

        // Render modules in the top right corner
        for (final ModuleComponent moduleComponent : this.getParent().getActiveModuleComponents()) {
            if (moduleComponent.animationTime == 0) {
                continue;
            }

            String name = (this.getParent().lowercase.getValue() ? moduleComponent.getTranslatedName().toLowerCase() : moduleComponent.getTranslatedName())
                    .replace(getParent().getRemoveSpaces().getValue() ? " " : "", "");
            String tag = (this.getParent().lowercase.getValue() ? moduleComponent.getTag().toLowerCase() : moduleComponent.getTag())
                    .replace(getParent().getRemoveSpaces().getValue() ? " " : "", "");
            final double x = moduleComponent.getPosition().getX();
            final double y = moduleComponent.getPosition().getY();
            final Color finalColor = moduleComponent.getColor();
            final boolean hasTag = !moduleComponent.getTag().isEmpty() && this.getParent().suffix.getValue();

            if (this.normalBackGround || this.blurBackGround) {
                Runnable backgroundRunnable = () -> RenderUtil.rectangle(
                        x + 0.5 - widthOffset, y - 2.5,
                        (moduleComponent.nameWidth + moduleComponent.tagWidth) + 2 + widthOffset,
                        this.getParent().moduleSpacing, getTheme().getBackgroundShade());

                if (this.normalBackGround) {
                    getLayer(REGULAR, 1).add(backgroundRunnable);
                }

                if (this.blurBackGround) {
                    getLayer(BLUR).add(() -> RenderUtil.rectangle(
                            x + 0.5 - widthOffset, y - 2.5,
                            (moduleComponent.nameWidth + moduleComponent.tagWidth) + 2 + widthOffset,
                            this.getParent().moduleSpacing, Color.BLACK));
                }

                getLayer(BLOOM).add(() -> {
                    if (glow || shadow) {
                        RenderUtil.rectangle(
                                x + 0.5 - widthOffset, y - 2.5,
                                (moduleComponent.nameWidth + moduleComponent.tagWidth) + 2 + widthOffset,
                                this.getParent().moduleSpacing,
                                glow ? ColorUtil.withAlpha(finalColor, 164) : getTheme().getDropShadow());
                    }
                });
            }

            Runnable textRunnable = () -> {
                if (dropShadow.getValue()) {
                    arrayListFont.drawWithShadow(name, x, y, finalColor.getRGB());
                    if (hasTag) {
                        arrayListFont.drawWithShadow(tag, x + moduleComponent.getNameWidth() + 3, y, 0xFFCCCCCC);
                    }
                } else {
                    arrayListFont.draw(name, x, y, finalColor.getRGB());
                    if (hasTag) {
                        arrayListFont.draw(tag, x + moduleComponent.getNameWidth() + 3, y, 0xFFCCCCCC);
                    }
                }
            };

            Runnable shadowRunnable = () -> {
                arrayListFont.draw(name, x, y, Color.BLACK.getRGB());
                if (hasTag) {
                    arrayListFont.draw(tag, x + moduleComponent.getNameWidth() + 3, y, Color.BLACK.getRGB());
                }
            };

            getLayer(REGULAR, 1).add(textRunnable);

            if (glow) {
                getLayer(BLOOM).add(textRunnable);
            } else if (shadow) {
                getLayer(BLOOM).add(shadowRunnable);
            }

            if (this.sidebar.getValue()) {
                getLayer(REGULAR, 1).add(() -> RenderUtil.roundedRectangle(
                        x + moduleComponent.getNameWidth() + moduleComponent.tagWidth + 2, y - 1.5f, 2, 9, 1, finalColor));
            }
        }

        if (coordinates == null || username == null) return;

        getLayer(BLOOM).add(() -> {
            if (!stopWatch.finished(2000)) {
                ParticleComponent.render();
            }
        });

        // Enhanced watermark rendering with proper centering
        final float paddingX = watermarkPaddingX.getValue().floatValue();
        final float paddingY = watermarkPaddingY.getValue().floatValue();
        final float radius = watermarkCornerRadius.getValue().floatValue();

        final String clientNameText = customClientName.getValue().isEmpty() ? Floyd.NAME : customClientName.getValue();
        final String versionText = Floyd.VERSION;
        final String fpsText = mc.getDebugFPS() + " fps";
        final String userText = username;
        final String uidText = "UID: " + (Floyd.UID != null ? Floyd.UID : "0");

        // Calculate widths for proper layout
        final float nameWidth = this.productSansMedium36.width(clientNameText);
        final float versionWidth = this.productSansMedium36.width(versionText);
        final float fpsWidth = this.productSansRegular18.width(fpsText);
        final float userWidth = this.productSansRegular18.width(userText);
        final float uidWidth = this.productSansRegular18.width(uidText);
        final float separatorWidth = this.productSansRegular18.width(" | ");

        // Calculate total dimensions
        final float lineSpacing = 2.0f;
        final float titleHeight = this.productSansMedium36.height();
        final float infoHeight = this.productSansRegular18.height();
        final float totalTextHeight = titleHeight + lineSpacing + infoHeight;

        final float line1Width = nameWidth + 2 + versionWidth;
        final float line2Width = fpsWidth + separatorWidth + userWidth + separatorWidth + uidWidth;
        final float maxTextWidth = Math.max(line1Width, line2Width);

        final float finalTotalWidth = maxTextWidth + 2.0f * paddingX;
        final float finalTotalHeight = totalTextHeight + 2.0f * paddingY;

        final float posX = 4.0f;
        final float posY = 4.5f;

        // Define colors - Red and dark theme
        final Color redAccent = new Color(255, 50, 50);
        final Color darkRed = new Color(180, 30, 30);
        final Color backgroundColor = new Color(18, 18, 18, (int)(watermarkBackgroundAlpha.getValue().floatValue() * 255));
        final Color textSecondary = new Color(200, 200, 200);

        // Draw watermark background and shadow
        getLayer(REGULAR, 1).add(() -> {
            if (watermarkBackground.getValue()) {
                // Draw smooth shadow
                int shadowSize = watermarkShadowSize.getValue().intValue();
                if (shadowSize > 0) {
                    for (int i = shadowSize; i > 0; i--) {
                        float alpha = (float)i / (float)shadowSize * 0.15f;
                        Color shadow = new Color(0, 0, 0, (int)(alpha * 255.0f));
                        RenderUtil.roundedRectangle(
                                posX - i * 0.5f, posY - i * 0.5f,
                                finalTotalWidth + i, finalTotalHeight + i,
                                radius + i * 0.3f, shadow
                        );
                    }
                }

                // Draw background
                RenderUtil.roundedRectangle(posX, posY, finalTotalWidth, finalTotalHeight, radius, backgroundColor);

                // Draw bottom accent bar
                RenderUtil.rectangle(posX + radius, posY + finalTotalHeight - 2.5f, finalTotalWidth - radius * 2, 2.5f, redAccent);
            }

            // Calculate centered positions
            final float contentStartY = posY + paddingY;

            // Line 1: Client name and version (centered)
            float line1X = posX + (finalTotalWidth - line1Width) / 2.0f;
            float titleY = contentStartY;

            // Draw client name in red gradient
            float currentX = line1X;
            for (int i = 0; i < clientNameText.length(); i++) {
                char c = clientNameText.charAt(i);
                String str = String.valueOf(c);
                float charWidth = this.productSansMedium36.width(str);

                // Gradient from red to dark red
                float progress = (float)i / (float)clientNameText.length();
                Color charColor = new Color(
                        (int)(redAccent.getRed() + (darkRed.getRed() - redAccent.getRed()) * progress),
                        (int)(redAccent.getGreen() + (darkRed.getGreen() - redAccent.getGreen()) * progress),
                        (int)(redAccent.getBlue() + (darkRed.getBlue() - redAccent.getBlue()) * progress)
                );

                this.productSansMedium36.drawWithShadow(str, currentX, titleY, charColor.getRGB());
                currentX += charWidth;
            }

            // Draw version in lighter red
            currentX += 2;
            this.productSansMedium36.drawWithShadow(versionText, currentX, titleY, new Color(255, 120, 120).getRGB());

            // Line 2: FPS | Username | UID (centered)
            float line2X = posX + (finalTotalWidth - line2Width) / 2.0f;
            float infoY = contentStartY + titleHeight + lineSpacing;

            currentX = line2X;

            // FPS with red accent
            this.productSansRegular18.drawWithShadow(fpsText, currentX, infoY, redAccent.getRGB());
            currentX += fpsWidth;

            // Separator
            this.productSansRegular18.drawWithShadow(" | ", currentX, infoY, textSecondary.getRGB());
            currentX += separatorWidth;

            // Username
            this.productSansRegular18.drawWithShadow(userText, currentX, infoY, Color.WHITE.getRGB());
            currentX += userWidth;

            // Separator
            this.productSansRegular18.drawWithShadow(" | ", currentX, infoY, textSecondary.getRGB());
            currentX += separatorWidth;

            // UID
            this.productSansRegular18.drawWithShadow(uidText, currentX, infoY, textSecondary.getRGB());

            // Draw coordinates at bottom
            productSansRegular.drawWithShadow("XYZ:", 5, sy, textSecondary.getRGB());
            productSansMedium18.drawWithShadow(coordinates, 5 + xyzWidth, sy, Color.WHITE.getRGB());
        });

        // Add bloom/glow effect to watermark
        getLayer(BLOOM).add(() -> {
            if (watermarkBackground.getValue()) {
                // Always glow the accent bar for that sick effect!
                RenderUtil.rectangle(posX + radius, posY + finalTotalHeight - 2.5f, finalTotalWidth - radius * 2, 2.5f,
                        ColorUtil.withAlpha(redAccent, 200));

                // Extra intense glow layers for the bar
                RenderUtil.rectangle(posX + radius - 1, posY + finalTotalHeight - 3.5f, finalTotalWidth - radius * 2 + 2, 4.5f,
                        ColorUtil.withAlpha(redAccent, 120));
                RenderUtil.rectangle(posX + radius - 2, posY + finalTotalHeight - 4.5f, finalTotalWidth - radius * 2 + 4, 6.5f,
                        ColorUtil.withAlpha(redAccent, 60));

                if (glow) {
                    // Glow the entire watermark box when glow mode is enabled
                    RenderUtil.roundedRectangle(posX, posY, finalTotalWidth, finalTotalHeight, radius,
                            ColorUtil.withAlpha(redAccent, 80));
                }
            }

            if (!stopWatch.finished(2000)) {
                ParticleComponent.render();
            }
        });

        if (mc.thePlayer.ticksExisted % 150 == 0) {
            stopWatch.reset();
        }
    };

    @EventLink
    public final Listener<KillEvent> onKill = event -> {
        if (!stopWatch.finished(2000) && this.particles.getValue()) {
            for (int i = 0; i <= 10; i++) {
                ParticleComponent.add(new Particle(new Vector2f(0, 0),
                        new Vector2f((float) Math.random(), (float) Math.random())));
            }
        }
        stopWatch.reset();
    };

    @EventLink
    public final Listener<TickEvent> onTick = event -> {
        if (mc.thePlayer == null || !mc.getNetHandler().doneLoadingTerrain) {
            return;
        }

        threadPool.execute(() -> {
            glow = this.shader.getValue().getName().equals("Glow");
            shadow = this.shader.getValue().getName().equals("Shadow");
            normalBackGround = background.getValue().getName().equals("Normal");
            blurBackGround = normalBackGround || background.getValue().getName().equals("Blur");

            username = Floyd.DISCUSER != null && !Floyd.DISCUSER.equals("Processing") && !Floyd.DISCUSER.equals("Unknown")
                    ? Floyd.DISCUSER
                    : (mc.getSession() == null || mc.getSession().getUsername() == null ? "null" : mc.getSession().getUsername());
            coordinates = (int) mc.thePlayer.posX + ", " + (int) mc.thePlayer.posY + ", " + (int) mc.thePlayer.posZ;
            xyzWidth = this.productSansMedium18.width("XYZ:") + 2;
            logoColor = this.getTheme().getFirstColor();

            // Update font based on mode
            switch (fontMode.getValue().getName()) {
                case "Product Sans":
                    Font productSans = MAIN.get(18, Weight.REGULAR);
                    if (!arrayListFont.equals(productSans)) arrayListFont = productSans;
                    break;
                case "Minecraft":
                    Font minecraftFont = MINECRAFT.get();
                    if (!arrayListFont.equals(minecraftFont)) arrayListFont = minecraftFont;
                    break;
                case "Custom":
                    String name = customFont.getValue();
                    if (Math.random() > 0.95) {
                        Optional<String> location = Fonts.getFontPaths().stream()
                                .filter(font -> removeNonAlphabetCharacters(font).toLowerCase()
                                        .contains(removeNonAlphabetCharacters(name).toLowerCase()))
                                .findFirst();
                        if (location.isPresent() && !CUSTOM.getName().equals(location.get())) {
                            CUSTOM.setName(location.get());
                            CUSTOM.getSizes().clear();
                        }
                    }
                    Font custom = CUSTOM.get(18);
                    if (!arrayListFont.equals(custom)) arrayListFont = custom;
                    break;
            }

            // Update module components
            for (final ModuleComponent moduleComponent : this.getParent().getActiveModuleComponents()) {
                if (moduleComponent.animationTime == 0) {
                    continue;
                }

                final boolean hasTag = !moduleComponent.getTag().isEmpty() && this.getParent().suffix.getValue();
                String name = (this.getParent().lowercase.getValue() ? moduleComponent.getTranslatedName().toLowerCase() : moduleComponent.getTranslatedName())
                        .replace(getParent().getRemoveSpaces().getValue() ? " " : "", "");
                String tag = (this.getParent().lowercase.getValue() ? moduleComponent.getTag().toLowerCase() : moduleComponent.getTag())
                        .replace(getParent().getRemoveSpaces().getValue() ? " " : "", "");

                Color color = this.getTheme().getFirstColor();
                switch (this.colorMode.getValue().getName()) {
                    case "Breathe":
                        double factor = this.getTheme().getBlendFactor(new Vector2d(0, 0));
                        color = ColorUtil.mixColors(this.getTheme().getFirstColor(), this.getTheme().getSecondColor(), factor);
                        break;
                    case "Fade":
                        color = this.getTheme().getAccentColor(new Vector2d(0, moduleComponent.getPosition().getY()));
                        break;
                }

                moduleComponent.setColor(color);
                moduleComponent.setNameWidth(arrayListFont.width(name));
                moduleComponent.setTagWidth(hasTag ? (arrayListFont.width(tag) + 4) : 0);
                moduleComponent.setHasTag(hasTag);
                moduleComponent.setDisplayName(name);
                moduleComponent.setDisplayTag(tag);
            }
        });
    };

    public static String removeNonAlphabetCharacters(String input) {
        return input.replaceAll("[^a-zA-Z]", "");
    }
}