// File: ModernInterface.java

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

    // ArrayList settings - SIMPLIFIED
    private final BooleanValue arrayListBackground = new BooleanValue("ArrayList Background", this, true);
    private final NumberValue arrayListBackgroundAlpha = new NumberValue("ArrayList BG Alpha", this, 0.65, 0.0, 1.0, 0.05);
    private final NumberValue arrayListPaddingX = new NumberValue("ArrayList Padding X", this, 6.0, 2.0, 15.0, 0.5);
    private final NumberValue arrayListPaddingY = new NumberValue("ArrayList Padding Y", this, 3.0, 1.0, 10.0, 0.5);
    private final NumberValue arrayListCornerRadius = new NumberValue("ArrayList Radius", this, 4.0, 0.0, 12.0, 0.5);
    private final BooleanValue arrayListAccentBar = new BooleanValue("ArrayList Accent Bar", this, true);

    // Watermark settings
    private final BooleanValue watermarkBackground = new BooleanValue("Watermark Background", this, true);
    private final NumberValue watermarkBackgroundAlpha = new NumberValue("Watermark BG Alpha", this, 0.7, 0.0, 1.0, 0.05);
    private final NumberValue watermarkPaddingX = new NumberValue("Watermark Padding X", this, 10.0, 4.0, 20.0, 0.5);
    private final NumberValue watermarkPaddingY = new NumberValue("Watermark Padding Y", this, 7.0, 3.0, 15.0, 0.5);
    private final NumberValue watermarkCornerRadius = new NumberValue("Watermark Radius", this, 8.0, 0.0, 15.0, 0.5);
    private final NumberValue watermarkShadowSize = new NumberValue("Watermark Shadow", this, 8.0, 0.0, 15.0, 1.0);

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

        // Dynamic spacing based on padding and radius
        float dynamicSpacing = arrayListBackground.getValue() ?
                (arrayListPaddingY.getValue().floatValue() * 2 + arrayListCornerRadius.getValue().floatValue() * 0.5f) :
                (minecraft ? 1.5F : 0.0F);

        this.getParent().setModuleSpacing(arrayListFont.height() + dynamicSpacing);
        this.getParent().setWidthComparator(arrayListFont);
        this.getParent().setEdgeOffset(10);

        float sy = event.getScaledResolution().getScaledHeight() - arrayListFont.height() - 1;
        final double widthOffset = minecraft ? 3.5 : 2;

        final float paddingX = arrayListPaddingX.getValue().floatValue();
        final float paddingY = arrayListPaddingY.getValue().floatValue();
        final float radius = arrayListCornerRadius.getValue().floatValue();
        final Color darkBackground = new Color(18, 18, 18, (int)(arrayListBackgroundAlpha.getValue().floatValue() * 255));

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

            // ArrayList background rendering
            if (arrayListBackground.getValue()) {
                final float totalWidth = (float)(moduleComponent.nameWidth + moduleComponent.tagWidth) + paddingX * 2 + (hasTag ? 3 : 0);
                final float totalHeight = arrayListFont.height() + paddingY * 2;

                // Background with subtle shadow
                getLayer(REGULAR, 1).add(() -> {
                    // Subtle outer glow/shadow
                    if (glow || shadow) {
                        RenderUtil.roundedRectangle(
                                (float)x - paddingX - 1.5f,
                                (float)y - paddingY - 1.5f,
                                totalWidth + 3f,
                                totalHeight + 3f,
                                radius + 1f,
                                new Color(0, 0, 0, 40)
                        );
                    }

                    // Main background
                    RenderUtil.roundedRectangle(
                            (float)x - paddingX,
                            (float)y - paddingY,
                            totalWidth,
                            totalHeight,
                            radius,
                            darkBackground
                    );
                });

                // Blur layer
                if (blurBackGround) {
                    getLayer(BLUR).add(() -> RenderUtil.roundedRectangle(
                            (float)x - paddingX,
                            (float)y - paddingY,
                            totalWidth,
                            totalHeight,
                            radius,
                            Color.BLACK
                    ));
                }

                // Accent bar on the left
                if (arrayListAccentBar.getValue()) {
                    final float accentWidth = 2f;
                    final float accentHeight = totalHeight - (radius * 1.5f);
                    final float accentY = (float)y - paddingY + (radius * 0.75f);

                    getLayer(REGULAR, 1).add(() -> {
                        RenderUtil.rectangle(
                                (float)x - paddingX + 1.5f,
                                accentY,
                                accentWidth,
                                accentHeight,
                                finalColor
                        );
                    });

                    // Bloom glow for accent bar
                    getLayer(BLOOM).add(() -> {
                        RenderUtil.rectangle(
                                (float)x - paddingX + 1.5f,
                                accentY,
                                accentWidth,
                                accentHeight,
                                ColorUtil.withAlpha(finalColor, 180)
                        );

                        // Extra glow
                        RenderUtil.rectangle(
                                (float)x - paddingX + 0.5f,
                                accentY - 1f,
                                accentWidth + 2f,
                                accentHeight + 2f,
                                ColorUtil.withAlpha(finalColor, 100)
                        );
                    });
                }

                // Background bloom
                getLayer(BLOOM).add(() -> {
                    if (glow) {
                        RenderUtil.roundedRectangle(
                                (float)x - paddingX,
                                (float)y - paddingY,
                                totalWidth,
                                totalHeight,
                                radius,
                                ColorUtil.withAlpha(finalColor, 40)
                        );
                    } else if (shadow) {
                        RenderUtil.roundedRectangle(
                                (float)x - paddingX,
                                (float)y - paddingY,
                                totalWidth,
                                totalHeight,
                                radius,
                                getTheme().getDropShadow()
                        );
                    }
                });
            } else {
                // Original background rendering when ArrayList background is off
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
            }

            // Draw text
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

            // Old sidebar (only if ArrayList background is off)
            if (this.sidebar.getValue() && !arrayListBackground.getValue()) {
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

        // WATERMARK RENDERING
        final float wPaddingX = watermarkPaddingX.getValue().floatValue();
        final float wPaddingY = watermarkPaddingY.getValue().floatValue();
        final float wRadius = watermarkCornerRadius.getValue().floatValue();

        final String clientNameText = customClientName.getValue().isEmpty() ? Floyd.NAME : customClientName.getValue();
        final String versionText = Floyd.VERSION;
        final String fpsText = mc.getDebugFPS() + " fps";
        final String userText = username;
        final String uidText = "UID: " + (Floyd.UID != null ? Floyd.UID : "0");

        final float nameWidth = this.productSansMedium36.width(clientNameText);
        final float versionWidth = this.productSansMedium36.width(versionText);
        final float fpsWidth = this.productSansRegular18.width(fpsText);
        final float userWidth = this.productSansRegular18.width(userText);
        final float uidWidth = this.productSansRegular18.width(uidText);
        final float separatorWidth = this.productSansRegular18.width(" | ");

        final float totalLineWidth = nameWidth + 2 + versionWidth + separatorWidth + fpsWidth + separatorWidth + userWidth + separatorWidth + uidWidth;
        final float textHeight = this.productSansMedium36.height();

        final float finalTotalWidth = totalLineWidth + 2.0f * wPaddingX;
        final float finalTotalHeight = textHeight + 2.0f * wPaddingY;

        final float posX = 4.0f;
        final float posY = 4.5f;

        final Color redAccent = new Color(255, 50, 50);
        final Color darkRed = new Color(180, 30, 30);
        final Color backgroundColor = new Color(18, 18, 18, (int)(watermarkBackgroundAlpha.getValue().floatValue() * 255));
        final Color textSecondary = new Color(200, 200, 200);

        getLayer(REGULAR, 1).add(() -> {
            if (watermarkBackground.getValue()) {
                int shadowSize = watermarkShadowSize.getValue().intValue();
                if (shadowSize > 0) {
                    for (int i = shadowSize; i > 0; i--) {
                        float alpha = (float)i / (float)shadowSize * 0.15f;
                        Color shadow2 = new Color(0, 0, 0, (int)(alpha * 255.0f));
                        RenderUtil.roundedRectangle(
                                posX - i * 0.5f, posY - i * 0.5f,
                                finalTotalWidth + i, finalTotalHeight + i,
                                wRadius + i * 0.3f, shadow2
                        );
                    }
                }

                RenderUtil.roundedRectangle(posX, posY, finalTotalWidth, finalTotalHeight, wRadius, backgroundColor);
                RenderUtil.rectangle(posX + wRadius, posY + finalTotalHeight - 2.5f, finalTotalWidth - wRadius * 2, 2.5f, redAccent);
            }

            final float textY = posY + wPaddingY;
            float currentX = posX + wPaddingX;

            for (int i = 0; i < clientNameText.length(); i++) {
                char c = clientNameText.charAt(i);
                String str = String.valueOf(c);
                float charWidth = this.productSansMedium36.width(str);

                float progress = (float)i / (float)clientNameText.length();
                Color charColor = new Color(
                        (int)(redAccent.getRed() + (darkRed.getRed() - redAccent.getRed()) * progress),
                        (int)(redAccent.getGreen() + (darkRed.getGreen() - redAccent.getGreen()) * progress),
                        (int)(redAccent.getBlue() + (darkRed.getBlue() - redAccent.getBlue()) * progress)
                );

                this.productSansMedium36.drawWithShadow(str, currentX, textY, charColor.getRGB());
                currentX += charWidth;
            }

            currentX += 2;
            this.productSansMedium36.drawWithShadow(versionText, currentX, textY, new Color(255, 120, 120).getRGB());
            currentX += versionWidth;

            final float smallFontOffset = (this.productSansMedium36.height() - this.productSansRegular18.height()) / 2.0f;
            final float smallTextY = textY + smallFontOffset;

            this.productSansRegular18.drawWithShadow(" | ", currentX, smallTextY, textSecondary.getRGB());
            currentX += separatorWidth;

            this.productSansRegular18.drawWithShadow(fpsText, currentX, smallTextY, redAccent.getRGB());
            currentX += fpsWidth;

            this.productSansRegular18.drawWithShadow(" | ", currentX, smallTextY, textSecondary.getRGB());
            currentX += separatorWidth;

            this.productSansRegular18.drawWithShadow(userText, currentX, smallTextY, Color.WHITE.getRGB());
            currentX += userWidth;

            this.productSansRegular18.drawWithShadow(" | ", currentX, smallTextY, textSecondary.getRGB());
            currentX += separatorWidth;

            this.productSansRegular18.drawWithShadow(uidText, currentX, smallTextY, textSecondary.getRGB());

            productSansRegular.drawWithShadow("XYZ:", 5, sy, textSecondary.getRGB());
            productSansMedium18.drawWithShadow(coordinates, 5 + xyzWidth, sy, Color.WHITE.getRGB());
        });

        getLayer(BLOOM).add(() -> {
            if (watermarkBackground.getValue()) {
                RenderUtil.rectangle(posX + wRadius, posY + finalTotalHeight - 2.5f, finalTotalWidth - wRadius * 2, 2.5f,
                        ColorUtil.withAlpha(redAccent, 200));

                RenderUtil.rectangle(posX + wRadius - 1, posY + finalTotalHeight - 3.5f, finalTotalWidth - wRadius * 2 + 2, 4.5f,
                        ColorUtil.withAlpha(redAccent, 120));
                RenderUtil.rectangle(posX + wRadius - 2, posY + finalTotalHeight - 4.5f, finalTotalWidth - wRadius * 2 + 4, 6.5f,
                        ColorUtil.withAlpha(redAccent, 60));

                if (glow) {
                    RenderUtil.roundedRectangle(posX, posY, finalTotalWidth, finalTotalHeight, wRadius,
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