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
import femcum.modernfloyd.clients.value.impl.StringValue;
import femcum.modernfloyd.clients.value.impl.SubMode;
import rip.vantage.commons.util.time.StopWatch;

import java.awt.*;
import java.util.Optional;

import static femcum.modernfloyd.clients.font.Fonts.*;
import static femcum.modernfloyd.clients.layer.Layers.*;

public class ModernInterface extends Mode<Interface> {

    private final Font productSansMedium36 = MAIN.get(36, Weight.MEDIUM);
    private final Font watermarkFont = MAIN.get(23, Weight.REGULAR);
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

    private boolean glow, shadow;
    private boolean normalBackGround, blurBackGround;
    private String username, coordinates;
    private Color logoColor;
    private float userWidth, xyzWidth;

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

        float sx = event.getScaledResolution().getScaledWidth();
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

                // Draw the glow/shadow around the module
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

        // Draw logo and player head
        getLayer(REGULAR, 1).add(() -> {
            RenderUtil.rectangle(
                    7, 7,
                    this.productSansMedium36.width(Floyd.NAME + " " + Floyd.VERSION) + 20,
                    this.productSansMedium36.height() + 2.5,
                    getTheme().getBackgroundShade()
            );

            RenderUtil.renderPlayerHead(mc.thePlayer, 9, 9, 0.5);

            float charX = 24.0F;
            for (char i : (Floyd.NAME + " " + Floyd.VERSION).toCharArray()) {
                String string = String.valueOf(i);
                this.productSansMedium36.draw(
                        string,
                        charX, 11.25,
                        this.getTheme().getAccentColor(new Vector2d(charX * 32, 11.25F)).getRGB()
                );
                charX += this.productSansMedium36.width(string) + 0.25F;
            }

            if (!customClientName.getValue().isEmpty()) {
                this.productSansMedium18.draw(customClientName.getValue(),
                        6 + productSansMedium36.width(Floyd.NAME) + 2, 6,
                        getTheme().getSecondColor().getRGB());
            }

            // Draw coordinates
            productSansRegular.drawWithShadow("XYZ:", 5, sy, 0xFFCCCCCC);
            productSansMedium18.drawWithShadow(coordinates, 5 + xyzWidth, sy, 0xFFCCCCCC);
        });

        getLayer(BLOOM).add(() -> {
            RenderUtil.rectangle(
                    7, 7,
                    this.productSansMedium36.width(Floyd.NAME + " " + Floyd.VERSION) + 20,
                    this.productSansMedium36.height() + 2.5,
                    getTheme().getDropShadow()
            );
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

            username = mc.getSession() == null || mc.getSession().getUsername() == null ? "null" : mc.getSession().getUsername();
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