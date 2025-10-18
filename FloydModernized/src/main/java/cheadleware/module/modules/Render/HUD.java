package cheadleware.module.modules.Render;

import cheadleware.Cheadleware;
import cheadleware.enums.BlinkModules;
import cheadleware.event.EventTarget;
import cheadleware.event.types.EventType;
import cheadleware.events.Render2DEvent;
import cheadleware.events.TickEvent;
import cheadleware.module.Module;
import cheadleware.property.properties.*;
import cheadleware.util.ColorUtil;
import cheadleware.util.font.TTFUtils;
import cheadleware.util.font.TrueTypeFontRenderer;
import cheadleware.util.animations.impl.DecelerateAnimation;
import cheadleware.util.animations.Direction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import java.io.InputStream;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class HUD extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public static final Map<Module, String> displayLabelCache = new HashMap<>();
    public static final Map<Module, DecelerateAnimation> moduleAnimations = new HashMap<>();
    public List<Module> activeModules = new ArrayList<>();
    private static ResourceLocation LOGO = null;
    private static boolean logoLoadAttempted = false;
    private static TrueTypeFontRenderer sfFontInfo;
    private static TrueTypeFontRenderer atkinsonFont;
    private static TrueTypeFontRenderer arraylistFont;
    private static TrueTypeFontRenderer sansFont;
    private long lastUpdate = 0;
    private int currentPosition = 0;
    private boolean isWaitingAtStart = true;
    private boolean isFlashing = false;
    private int flashCount = 0;
    private boolean flashState = false;
    private long waitStartTime = System.currentTimeMillis();

    public final ModeProperty colorMode = new ModeProperty("color", 3, new String[]{"RAINBOW", "CHROMA", "ASTOLFO", "CUSTOM1", "CUSTOM12", "CUSTOM123"});
    public final FloatProperty colorSpeed = new FloatProperty("color-speed", 1.0F, 0.5F, 1.5F);
    public final PercentProperty colorSaturation = new PercentProperty("color-saturation", 50);
    public final PercentProperty colorBrightness = new PercentProperty("color-brightness", 100);
    public final ColorProperty custom1 = new ColorProperty("custom-color-1", Color.WHITE.getRGB(), () -> this.colorMode.getValue() == 3 || this.colorMode.getValue() == 4 || this.colorMode.getValue() == 5);
    public final ColorProperty custom2 = new ColorProperty("custom-color-2", Color.WHITE.getRGB(), () -> this.colorMode.getValue() == 4 || this.colorMode.getValue() == 5);
    public final ColorProperty custom3 = new ColorProperty("custom-color-3", Color.WHITE.getRGB(), () -> this.colorMode.getValue() == 5);
    public final ModeProperty posX = new ModeProperty("position-x", 1, new String[]{"LEFT", "RIGHT"});
    public final ModeProperty posY = new ModeProperty("position-y", 0, new String[]{"TOP", "BOTTOM"});
    public final IntProperty offsetX = new IntProperty("offset-x", 2, 0, 255);
    public final IntProperty offsetY = new IntProperty("offset-y", 2, 0, 255);
    public final PercentProperty background = new PercentProperty("background", 25);
    public final BooleanProperty showBar = new BooleanProperty("bar", true);
    public final BooleanProperty shadow = new BooleanProperty("shadow", true);
    public final ModeProperty suffixMode = new ModeProperty("suffix-mode", 1, new String[]{"NONE", "SIMPLE", "BRACKET", "DASH"});
    public final BooleanProperty lowerCase = new BooleanProperty("lower-case", false);
    public final BooleanProperty blinkTimer = new BooleanProperty("blink-timer", true);
    public final BooleanProperty toggleSound = new BooleanProperty("toggle-sounds", true);
    public final BooleanProperty arraylistAnimations = new BooleanProperty("arraylist-animations", true);
    public final BooleanProperty showWatermark = new BooleanProperty("watermark", true);
    public final ModeProperty watermarkMode = new ModeProperty("watermark-mode", 0, new String[]{"ATMOSPHERE", "CHEADLESENSE", "PRIMECHEATS"}, () -> this.showWatermark.getValue());
    public final BooleanProperty straightBar = new BooleanProperty("straight-bar", false, () -> this.showWatermark.getValue());
    public final BooleanProperty showCoords = new BooleanProperty("coords", true);
    public final BooleanProperty showSpeed = new BooleanProperty("speed", true);
    public final BooleanProperty showFPS = new BooleanProperty("fps", true);

    public HUD() {
        super("HUD", true, true);
        initializeFont();
        loadLogo();
    }

    private void loadLogo() {
        if (logoLoadAttempted) return;
        logoLoadAttempted = true;
        try {
            InputStream logoStream = getClass().getClassLoader().getResourceAsStream("assets/textures/cheadleware.png");
            if (logoStream != null) {
                java.awt.image.BufferedImage logoImage = javax.imageio.ImageIO.read(logoStream);
                LOGO = mc.getTextureManager().getDynamicTextureLocation("cheadleware_logo", new net.minecraft.client.renderer.texture.DynamicTexture(logoImage));
                logoStream.close();
                System.out.println("[HUD] Logo loaded successfully!");
            } else {
                System.out.println("[HUD] Logo file not found at assets/textures/cheadleware.png");
            }
        } catch (Exception e) {
            System.out.println("[HUD] Failed to load logo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeFont() {
        try {
            Font sfAwtFontInfo = TTFUtils.getFontFromLocation("SF-Regular.ttf", 20);
            if (sfAwtFontInfo != null) {
                sfFontInfo = new TrueTypeFontRenderer(sfAwtFontInfo, true, true);
                sfFontInfo.generateTextures();
            }
            Font atkinsonAwtFont = TTFUtils.getFontFromLocation("atkinson.ttf", 18);
            if (atkinsonAwtFont != null) {
                atkinsonFont = new TrueTypeFontRenderer(atkinsonAwtFont, true, true);
                atkinsonFont.generateTextures();
            }
            Font sansAwtFont = TTFUtils.getFontFromLocation("sans.ttf", 18);
            if (sansAwtFont != null) {
                sansFont = new TrueTypeFontRenderer(sansAwtFont, true, true);
                sansFont.generateTextures();
            }
            Font arraylistAwtFont = TTFUtils.getFontFromLocation("sans.ttf", 20);
            if (arraylistAwtFont != null) {
                arraylistFont = new TrueTypeFontRenderer(arraylistAwtFont, true, true);
                arraylistFont.generateTextures();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getStringWidth(String text) {
        String strippedText = text.replaceAll("§.", "");
        if (arraylistFont != null) {
            return (int) arraylistFont.getWidth(strippedText);
        }
        return mc.fontRendererObj.getStringWidth(strippedText);
    }

    public int getStringHeight(String text) {
        if (arraylistFont != null) {
            return (int) arraylistFont.getHeight(text);
        }
        return mc.fontRendererObj.FONT_HEIGHT;
    }
    private void drawString(String text, float x, float y, int color, boolean shadow) {
        if (arraylistFont != null) {
            if (shadow) {
                arraylistFont.drawStringWithShadow(text, x, y, color);
            } else {
                arraylistFont.drawString(text, x, y, color);
            }
        } else {
            mc.fontRendererObj.drawString(text, (int) x, (int) y, color, shadow);
        }
    }

    private String getModuleName(Module module) {
        String moduleName = module.getName();
        if (this.lowerCase.getValue()) {
            moduleName = moduleName.toLowerCase(Locale.ROOT);
        }
        return moduleName;
    }

    private String[] getModuleSuffix(Module module) {
        String[] moduleSuffix = module.getSuffix();
        if (this.lowerCase.getValue()) {
            for (int i = 0; i < moduleSuffix.length; i++) {
                moduleSuffix[i] = moduleSuffix[i].toLowerCase();
            }
        }
        return moduleSuffix;
    }

    private String getDisplayLabel(Module module) {
        String label = getModuleName(module);
        String[] suffix = getModuleSuffix(module);
        if (this.suffixMode.getValue() == 0 || suffix.length == 0) {
            return label;
        }
        StringBuilder sb = new StringBuilder(label);
        switch (this.suffixMode.getValue()) {
            case 1:
                sb.append(" §7");
                for (int i = 0; i < suffix.length; i++) {
                    sb.append(suffix[i]);
                    if (i < suffix.length - 1) sb.append(" ");
                }
                break;
            case 2:
                sb.append(" §7[");
                for (int i = 0; i < suffix.length; i++) {
                    sb.append(suffix[i]);
                    if (i < suffix.length - 1) sb.append(" ");
                }
                sb.append("]");
                break;
            case 3:
                sb.append(" §7- ");
                for (int i = 0; i < suffix.length; i++) {
                    sb.append(suffix[i]);
                    if (i < suffix.length - 1) sb.append(" ");
                }
                break;
        }
        return sb.toString();
    }

    private int getModuleWidth(Module module) {
        String displayLabel = displayLabelCache.get(module);
        if (displayLabel == null) {
            displayLabel = getDisplayLabel(module);
        }
        return getStringWidth(displayLabel);
    }

    private float getColorCycle(long long3, long long4) {
        float speedValue = this.colorSpeed.getValue();
        long speed = (long) (3000.0 / Math.pow(Math.min(Math.max(0.5F, speedValue), 1.5F), 3.0));
        return 1.0F - (float) (Math.abs(long3 - long4 * 300L) % speed) / (float) speed;
    }

    public Color getColor(long time) {
        return this.getColor(time, 0L);
    }

    public Color getColor(long time, long offset) {
        Color color = Color.white;
        switch (this.colorMode.getValue()) {
            case 0:
                color = ColorUtil.fromHSB(this.getColorCycle(time, offset), 1.0F, 1.0F);
                break;
            case 1:
                color = ColorUtil.fromHSB(this.getColorCycle(time / 3L, 0L), 1.0F, 1.0F);
                break;
            case 2:
                float cycle = this.getColorCycle(time, offset);
                if (cycle % 1.0F < 0.5F) {
                    cycle = 1.0F - cycle % 1.0F;
                }
                color = ColorUtil.fromHSB(cycle, 1.0F, 1.0F);
                break;
            case 3:
                color = new Color(this.custom1.getValue());
                break;
            case 4:
                double cycle1 = this.getColorCycle(time, offset);
                color = ColorUtil.interpolate((float) (2.0 * Math.abs(cycle1 - Math.floor(cycle1 + 0.5))), new Color(this.custom1.getValue()), new Color(this.custom2.getValue()));
                break;
            case 5:
                double cycle2 = this.getColorCycle(time, offset);
                float floor = (float) (2.0 * Math.abs(cycle2 - Math.floor(cycle2 + 0.5)));
                if (floor <= 0.5F) {
                    color = ColorUtil.interpolate(floor * 2.0F, new Color(this.custom1.getValue()), new Color(this.custom2.getValue()));
                } else {
                    color = ColorUtil.interpolate((floor - 0.5F) * 2.0F, new Color(this.custom2.getValue()), new Color(this.custom3.getValue()));
                }
                break;
        }
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        return Color.getHSBColor(hsb[0], hsb[1] * (this.colorSaturation.getValue().floatValue() / 100.0F), hsb[2] * (this.colorBrightness.getValue().floatValue() / 100.0F));
    }

    private String applyPrimeCheatsEffect(String text) {
        long currentTime = System.currentTimeMillis();
        if (isWaitingAtStart) {
            if (currentTime - waitStartTime > 4000) {
                isWaitingAtStart = false;
                currentPosition = 1;
                lastUpdate = currentTime;
            }
            return "§b" + text;
        }
        if (isFlashing) {
            if (currentTime - lastUpdate > 200) {
                flashState = !flashState;
                flashCount++;
                lastUpdate = currentTime;
                if (flashCount >= 6) {
                    isFlashing = false;
                    flashCount = 0;
                    currentPosition = 0;
                    flashState = false;
                    isWaitingAtStart = true;
                    waitStartTime = currentTime;
                }
            }
            String flashColor = flashState ? "§3" : "§b";
            return flashColor + text;
        }
        if (currentTime - lastUpdate > 200) {
            lastUpdate = currentTime;
            currentPosition++;
            int letterCount = 0;
            for (char c : text.toCharArray()) {
                if (Character.isLetterOrDigit(c)) {
                    letterCount++;
                }
            }
            if (currentPosition > letterCount) {
                isFlashing = true;
                flashCount = 0;
                lastUpdate = currentTime;
                return "§b" + text;
            }
        }
        StringBuilder result = new StringBuilder();
        int charIndex = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                charIndex++;
                if (charIndex < currentPosition) {
                    result.append("§3");
                } else if (charIndex == currentPosition) {
                    result.append("§f");
                } else {
                    result.append("§b");
                }
            }
            result.append(c);
        }
        return result.toString();
    }

    private int getMinecraftColor(String colorCode) {
        if (colorCode.length() < 2) return Color.WHITE.getRGB();
        char code = colorCode.charAt(1);
        switch (code) {
            case '0':
                return new Color(0, 0, 0).getRGB();
            case '1':
                return new Color(0, 0, 170).getRGB();
            case '2':
                return new Color(0, 170, 0).getRGB();
            case '3':
                return new Color(0, 170, 170).getRGB();
            case '4':
                return new Color(170, 0, 0).getRGB();
            case '5':
                return new Color(170, 0, 170).getRGB();
            case '6':
                return new Color(255, 170, 0).getRGB();
            case '7':
                return new Color(170, 170, 170).getRGB();
            case '8':
                return new Color(85, 85, 85).getRGB();
            case '9':
                return new Color(85, 85, 255).getRGB();
            case 'a':
                return new Color(85, 255, 85).getRGB();
            case 'b':
                return new Color(85, 255, 255).getRGB();
            case 'c':
                return new Color(255, 85, 85).getRGB();
            case 'd':
                return new Color(255, 85, 255).getRGB();
            case 'e':
                return new Color(255, 255, 85).getRGB();
            case 'f':
                return Color.WHITE.getRGB();
            default:
                return Color.WHITE.getRGB();
        }
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (this.isEnabled() && event.getType() == EventType.POST) {
            this.activeModules = Cheadleware.moduleManager.modules.values().stream().filter(module -> !module.isHidden()).collect(Collectors.toList());
            for (Module module : activeModules) {
                if (!moduleAnimations.containsKey(module)) {
                    DecelerateAnimation anim = new DecelerateAnimation(200, 1.0);
                    anim.setDirection(module.isEnabled() ? Direction.FORWARDS : Direction.BACKWARDS);
                    moduleAnimations.put(module, anim);
                }
                DecelerateAnimation anim = moduleAnimations.get(module);
                if (arraylistAnimations.getValue()) {
                    anim.setDirection(module.isEnabled() ? Direction.FORWARDS : Direction.BACKWARDS);
                } else {
                    anim.setDirection(module.isEnabled() ? Direction.FORWARDS : Direction.BACKWARDS);
                    anim.setDuration(0);
                }
                displayLabelCache.put(module, getDisplayLabel(module));
            }
            moduleAnimations.keySet().removeIf(module -> !activeModules.contains(module));
            this.activeModules.sort(Comparator.comparingInt(this::getModuleWidth).reversed());
        }
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        ScaledResolution sr = new ScaledResolution(mc);
        renderHUD(sr);
    }

    private void renderWatermarkAtmosphere(long currentMillis) {
        if (sansFont == null) {
            renderWatermarkAtmosphereFallback(currentMillis);
            return;
        }
        String clientName = "cheadleware";
        String versionText = " (Development) ";
        String server = mc.isSingleplayer() ? "singleplayer" : mc.getCurrentServerData() != null ? mc.getCurrentServerData().serverIP : "unknown";
        float logoSize = 12;
        float spacing = 3;
        float clientNameWidth = sansFont.getWidth(clientName);
        float versionWidth = sansFont.getWidth(versionText);
        float serverWidth = sansFont.getWidth(server);
        float totalWidth = logoSize + spacing + clientNameWidth + versionWidth + serverWidth;
        float height = 14;
        float baseX = 3;
        float baseY = 3;
        float barHeight = 1;
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        if (straightBar.getValue()) {
            Color barColor = getColor(currentMillis, 0);
            Gui.drawRect((int) baseX, (int) baseY, (int) (baseX + totalWidth + 6), (int) (baseY + barHeight), barColor.getRGB());
        } else {
            Color leftColor = getColor(currentMillis, 0);
            Color rightColor = getColor(currentMillis, 10);
            cheadleware.util.tenacityshaders.RenderUtil.drawGradientRect(baseX, baseY, baseX + totalWidth + 6, baseY + barHeight, leftColor.getRGB(), rightColor.getRGB());
        }
        Gui.drawRect((int) baseX, (int) (baseY + barHeight), (int) (baseX + totalWidth + 6), (int) (baseY + barHeight + height + 4), new Color(0, 0, 0, 180).getRGB());
        if (LOGO != null) {
            mc.getTextureManager().bindTexture(LOGO);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            Gui.drawModalRectWithCustomSizedTexture((int) (baseX + 3), (int) (baseY + barHeight + 3), 0, 0, (int) logoSize, (int) logoSize, logoSize, logoSize);
        }
        float textX = baseX + 3 + logoSize + spacing;
        float textY = baseY + barHeight + 5;
        sansFont.drawString(clientName, textX, textY, Color.WHITE.getRGB());
        textX += clientNameWidth;
        sansFont.drawString(versionText, textX, textY, new Color(170, 170, 170).getRGB());
        textX += versionWidth;
        sansFont.drawString(server, textX, textY, Color.WHITE.getRGB());
        GlStateManager.disableBlend();
    }

    private void renderWatermarkAtmosphereFallback(long currentMillis) {
        if (sfFontInfo == null) return;
        String clientName = "cheadleware";
        String versionText = " (Development) ";
        String server = mc.isSingleplayer() ? "singleplayer" : mc.getCurrentServerData() != null ? mc.getCurrentServerData().serverIP : "unknown";
        float logoSize = 12;
        float spacing = 3;
        float clientNameWidth = sfFontInfo.getWidth(clientName);
        float versionWidth = sfFontInfo.getWidth(versionText);
        float serverWidth = sfFontInfo.getWidth(server);
        float totalWidth = logoSize + spacing + clientNameWidth + versionWidth + serverWidth;
        float height = 14;
        float baseX = 3;
        float baseY = 3;
        float barHeight = 2;
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        if (straightBar.getValue()) {
            Color barColor = getColor(currentMillis, 0);
            Gui.drawRect((int) baseX, (int) baseY, (int) (baseX + totalWidth + 6), (int) (baseY + barHeight), barColor.getRGB());
        } else {
            Color leftColor = getColor(currentMillis, 0);
            Color rightColor = getColor(currentMillis, 10);
            cheadleware.util.tenacityshaders.RenderUtil.drawGradientRect(baseX, baseY, baseX + totalWidth + 6, baseY + barHeight, leftColor.getRGB(), rightColor.getRGB());
        }
        Gui.drawRect((int) baseX, (int) (baseY + barHeight), (int) (baseX + totalWidth + 6), (int) (baseY + barHeight + height + 4), new Color(0, 0, 0, 180).getRGB());
        if (LOGO != null) {
            mc.getTextureManager().bindTexture(LOGO);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            Gui.drawModalRectWithCustomSizedTexture((int) (baseX + 3), (int) (baseY + barHeight + 3), 0, 0, (int) logoSize, (int) logoSize, logoSize, logoSize);
        }
        float textX = baseX + 3 + logoSize + spacing;
        float textY = baseY + barHeight + 4;
        sfFontInfo.drawString(clientName, textX, textY, Color.WHITE.getRGB());
        textX += clientNameWidth;
        sfFontInfo.drawString(versionText, textX, textY, new Color(170, 170, 170).getRGB());
        textX += versionWidth;
        sfFontInfo.drawString(server, textX, textY, Color.WHITE.getRGB());
        GlStateManager.disableBlend();
    }

    private void renderWatermarkcheadlesense(long currentMillis) {
        if (atkinsonFont == null) {
            renderWatermarkcheadlesenseFallback(currentMillis);
            return;
        }
        String clientName = "cheadle";
        String senseTag = "sense";
        String server = mc.isSingleplayer() ? "singleplayer" : mc.getCurrentServerData() != null ? mc.getCurrentServerData().serverIP : "unknown";
        String remainingHudText = " | " + Minecraft.getDebugFPS() + " fps | " + server;
        float clientNameWidth = atkinsonFont.getWidth(clientName);
        float senseWidth = atkinsonFont.getWidth(senseTag);
        float remainingWidth = atkinsonFont.getWidth(remainingHudText);
        float boxWidth = clientNameWidth + senseWidth + remainingWidth + 4;
        float height = 12;
        float baseX = 5;
        float baseY = 5;
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        Gui.drawRect((int) baseX, (int) baseY, (int) (baseX + boxWidth + 8), (int) (baseY + height + 8), new Color(60, 60, 60).getRGB());
        Gui.drawRect((int) (baseX + 1), (int) (baseY + 1), (int) (baseX + boxWidth + 7), (int) (baseY + height + 7), new Color(40, 40, 40).getRGB());
        Gui.drawRect((int) (baseX + 2), (int) (baseY + 2), (int) (baseX + boxWidth + 6), (int) (baseY + height + 6), new Color(60, 60, 60).getRGB());
        Gui.drawRect((int) (baseX + 3), (int) (baseY + 3), (int) (baseX + boxWidth + 5), (int) (baseY + height + 5), new Color(22, 22, 22).getRGB());
        float textBaseX = baseX + 6;
        float textBaseY = baseY + 6;
        Color senseColor = getColor(currentMillis, 0);
        atkinsonFont.drawString(clientName, textBaseX, textBaseY, Color.WHITE.getRGB());
        atkinsonFont.drawString(senseTag, textBaseX + clientNameWidth, textBaseY, senseColor.getRGB());
        atkinsonFont.drawString(remainingHudText, textBaseX + clientNameWidth + senseWidth, textBaseY, Color.WHITE.getRGB());
        if (straightBar.getValue()) {
            Gui.drawRect((int) (baseX + 3), (int) (baseY + 3), (int) (baseX + boxWidth + 5), (int) (baseY + 4), senseColor.getRGB());
        } else {
            Color leftColor = getColor(currentMillis, 0);
            Color rightColor = getColor(currentMillis, 10);
            cheadleware.util.tenacityshaders.RenderUtil.drawGradientRect(baseX + 3, baseY + 3, baseX + boxWidth + 5, baseY + 4, leftColor.getRGB(), rightColor.getRGB());
        }
        GlStateManager.disableBlend();
    }

    private void renderWatermarkcheadlesenseFallback(long currentMillis) {
        String clientName = "Cheadleware";
        String senseTag = "Sense";
        String server = mc.isSingleplayer() ? "singleplayer" : mc.getCurrentServerData() != null ? mc.getCurrentServerData().serverIP : "unknown";
        String remainingHudText = " | " + Minecraft.getDebugFPS() + " fps | " + server;
        float clientNameWidth = sfFontInfo.getWidth(clientName);
        float senseWidth = sfFontInfo.getWidth(senseTag);
        float remainingWidth = sfFontInfo.getWidth(remainingHudText);
        float boxWidth = clientNameWidth + senseWidth + remainingWidth + 4;
        float height = 11;
        float baseX = 5;
        float baseY = 5;
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        Gui.drawRect((int) baseX, (int) baseY, (int) (baseX + boxWidth + 8), (int) (baseY + height + 8), new Color(60, 60, 60).getRGB());
        Gui.drawRect((int) (baseX + 1), (int) (baseY + 1), (int) (baseX + boxWidth + 7), (int) (baseY + height + 7), new Color(40, 40, 40).getRGB());
        Gui.drawRect((int) (baseX + 2), (int) (baseY + 2), (int) (baseX + boxWidth + 6), (int) (baseY + height + 6), new Color(60, 60, 60).getRGB());
        Gui.drawRect((int) (baseX + 3), (int) (baseY + 3), (int) (baseX + boxWidth + 5), (int) (baseY + height + 5), new Color(22, 22, 22).getRGB());
        float textBaseX = baseX + 5;
        float textBaseY = baseY + 4;
        Color senseColor = getColor(currentMillis, 0);
        sfFontInfo.drawString(clientName, textBaseX, textBaseY, Color.WHITE.getRGB());
        sfFontInfo.drawString(senseTag, textBaseX + clientNameWidth, textBaseY, senseColor.getRGB());
        sfFontInfo.drawString(remainingHudText, textBaseX + clientNameWidth + senseWidth, textBaseY, Color.WHITE.getRGB());
        if (straightBar.getValue()) {
            Gui.drawRect((int) (baseX + 3), (int) (baseY + 3), (int) (baseX + boxWidth + 5), (int) (baseY + 4), senseColor.getRGB());
        } else {
            Color leftColor = getColor(currentMillis, 0);
            Color rightColor = getColor(currentMillis, 10);
            cheadleware.util.tenacityshaders.RenderUtil.drawGradientRect(baseX + 3, baseY + 3, baseX + boxWidth + 5, baseY + 4, leftColor.getRGB(), rightColor.getRGB());
        }
        GlStateManager.disableBlend();
    }

    private void renderWatermarkPrimeCheats(long currentMillis) {
        String baseText = "cheadleware.net";
        String displayText = applyPrimeCheatsEffect(baseText);
        float baseX = 3;
        float baseY = 3;
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        float currentX = baseX;
        String[] colorCodes = displayText.split("(?=§)");
        String currentColor = "";
        for (String segment : colorCodes) {
            if (segment.startsWith("§") && segment.length() >= 2) {
                currentColor = segment.substring(0, 2);
                String chars = segment.substring(2);
                for (char c : chars.toCharArray()) {
                    int color = getMinecraftColor(currentColor);
                    mc.fontRendererObj.drawStringWithShadow(String.valueOf(c), currentX, baseY, color);
                    currentX += mc.fontRendererObj.getCharWidth(c);
                }
            } else if (!segment.isEmpty()) {
                for (char c : segment.toCharArray()) {
                    int color = getMinecraftColor(currentColor);
                    mc.fontRendererObj.drawStringWithShadow(String.valueOf(c), currentX, baseY, color);
                    currentX += mc.fontRendererObj.getCharWidth(c);
                }
            }
        }
        GlStateManager.disableBlend();
    }

    private void renderInfoElements(ScaledResolution sr, long currentMillis) {
        if (sfFontInfo == null) return;
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.enableTexture2D();
        float yOffset = sr.getScaledHeight() - 5;
        float xPos = sr.getScaledWidth() - 5;
        String devText = "Development - 141025 - ";
        String pastaText = "pastaa";
        float devWidth = sfFontInfo.getWidth(devText);
        float pastaWidth = sfFontInfo.getWidth(pastaText);
        float totalWidth = devWidth + pastaWidth;
        float devHeight = sfFontInfo.getHeight(devText);
        yOffset -= devHeight;
        float devX = xPos - totalWidth;
        sfFontInfo.drawStringWithShadow(devText, devX, yOffset, Color.WHITE.getRGB());
        float pastaX = devX + devWidth;
        Color pastaColor = getColor(currentMillis, 0);
        sfFontInfo.drawStringWithShadow(pastaText, pastaX, yOffset, pastaColor.getRGB());
        yOffset -= 2;
        if (showCoords.getValue() && mc.thePlayer != null) {
            String xyzLabel = "XYZ: ";
            String coordsValues = String.format("%.1f, %.1f, %.1f", mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
            float xyzWidth = sfFontInfo.getWidth(xyzLabel);
            float coordsWidth = sfFontInfo.getWidth(coordsValues);
            float coordsTotalWidth = xyzWidth + coordsWidth;
            float coordsHeight = sfFontInfo.getHeight(xyzLabel);
            yOffset -= coordsHeight;
            float coordsX = xPos - coordsTotalWidth;
            Color xyzColor = getColor(currentMillis, 1);
            sfFontInfo.drawStringWithShadow(xyzLabel, coordsX, yOffset, xyzColor.getRGB());
            sfFontInfo.drawStringWithShadow(coordsValues, coordsX + xyzWidth, yOffset, Color.WHITE.getRGB());
            yOffset -= 2;
        }
        yOffset = sr.getScaledHeight() - 5;
        xPos = 5;
        if (showSpeed.getValue() && mc.thePlayer != null) {
            double bps = Math.hypot(mc.thePlayer.posX - mc.thePlayer.prevPosX, mc.thePlayer.posZ - mc.thePlayer.prevPosZ) * 20;
            String speedLabel = "Speed: ";
            String speedValue = String.format("%.2f blocks/sec", bps);
            float speedHeight = sfFontInfo.getHeight(speedLabel);
            float speedLabelWidth = sfFontInfo.getWidth(speedLabel);
            yOffset -= speedHeight;
            Color speedColor = getColor(currentMillis, 2);
            sfFontInfo.drawStringWithShadow(speedLabel, xPos, yOffset, speedColor.getRGB());
            sfFontInfo.drawStringWithShadow(speedValue, xPos + speedLabelWidth, yOffset, Color.WHITE.getRGB());
            yOffset -= 2;
        }
        if (showFPS.getValue()) {
            int fps = Minecraft.getDebugFPS();
            String fpsLabel = "FPS: ";
            String fpsValue = String.valueOf(fps);
            float fpsHeight = sfFontInfo.getHeight(fpsLabel);
            float fpsLabelWidth = sfFontInfo.getWidth(fpsLabel);
            yOffset -= fpsHeight;
            Color fpsLabelColor = getColor(currentMillis, 3);
            sfFontInfo.drawStringWithShadow(fpsLabel, xPos, yOffset, fpsLabelColor.getRGB());
            sfFontInfo.drawStringWithShadow(fpsValue, xPos + fpsLabelWidth, yOffset, Color.WHITE.getRGB());
        }
        GlStateManager.disableBlend();
    }

    private void renderHUD(ScaledResolution sr) {
        if (this.isEnabled() && !mc.gameSettings.showDebugInfo) {
            // IMPORTANT: Render PostProcessing effects FIRST before any HUD text
            PostProcessing postProcessing = (PostProcessing) Cheadleware.moduleManager.getModule(PostProcessing.class);
            if (postProcessing != null && postProcessing.isEnabled()) {
                postProcessing.renderEffects();
            }

            long currentMillis = System.currentTimeMillis();
            if (showWatermark.getValue()) {
                if (watermarkMode.getValue() == 0) {
                    renderWatermarkAtmosphere(currentMillis);
                } else if (watermarkMode.getValue() == 1) {
                    renderWatermarkcheadlesense(currentMillis);
                } else if (watermarkMode.getValue() == 2) {
                    renderWatermarkPrimeCheats(currentMillis);
                }
            }
            renderInfoElements(sr, currentMillis);
            boolean topArrayList = posY.getValue() == 0;
            boolean rightSide = posX.getValue() == 1;
            int screenX = sr.getScaledWidth();
            int screenY = sr.getScaledHeight();
            int textHeight = getStringHeight("A") + 2;
            int textOffset = textHeight - 2;
            int offset = topArrayList ? textHeight : -textHeight;
            int y = topArrayList ? offsetY.getValue() : screenY - textOffset - offsetY.getValue();
            int visibleModuleIndex = 0;
            for (int i = 0; i < activeModules.size(); i++) {
                Module module = activeModules.get(i);
                DecelerateAnimation anim = moduleAnimations.get(module);
                if (anim == null) continue;
                if (!module.isEnabled() && anim.finished(Direction.BACKWARDS)) continue;
                float animProgress = anim.getOutput().floatValue();
                if (animProgress <= 0.01f) continue;
                String name = displayLabelCache.get(module);
                float moduleWidth = getStringWidth(name);
                int aColor = getColor(currentMillis, visibleModuleIndex).getRGB();
                int x;
                int bgStartX;
                int bgEndX;
                int barStartX;
                int barEndX;
                float slideOffset = (1 - animProgress) * (rightSide ? 20 : -20);
                if (rightSide) {
                    x = (int) (screenX - moduleWidth - offsetX.getValue() - (showBar.getValue() ? 2 : 1) + slideOffset);
                    bgStartX = (int) (screenX - moduleWidth - offsetX.getValue() - (showBar.getValue() ? 3 : 2) + slideOffset);
                    bgEndX = (int) (screenX - offsetX.getValue() + slideOffset);
                    barStartX = (int) (screenX - offsetX.getValue() - 1 + slideOffset);
                    barEndX = (int) (screenX - offsetX.getValue() + slideOffset);
                } else {
                    x = (int) (offsetX.getValue() + (showBar.getValue() ? 2 : 1) + slideOffset);
                    bgStartX = (int) (offsetX.getValue() + slideOffset);
                    bgEndX = (int) (offsetX.getValue() + moduleWidth + (showBar.getValue() ? 3 : 2) + slideOffset);
                    barStartX = (int) (offsetX.getValue() + slideOffset);
                    barEndX = (int) (offsetX.getValue() + 1 + slideOffset);
                }
                int top = y - 2;
                int bgAlpha = (int) (background.getValue().floatValue() / 100.0F * animProgress * 255);
                if (background.getValue() > 0) {
                    Gui.drawRect(bgStartX, top, bgEndX, y + textOffset, new Color(0, 0, 0, bgAlpha).getRGB());
                }
                Color textColor = new Color(aColor);
                int animatedColor = new Color(textColor.getRed() / 255f, textColor.getGreen() / 255f, textColor.getBlue() / 255f, animProgress).getRGB();
                drawString(name, x, y - 1, animatedColor, shadow.getValue());
                if (showBar.getValue()) {
                    int barColor = new Color(textColor.getRed() / 255f, textColor.getGreen() / 255f, textColor.getBlue() / 255f, animProgress).getRGB();
                    Gui.drawRect(barStartX, y - 2, barEndX, y + textOffset, barColor);
                }
                y += offset;
                if (module.isEnabled() || animProgress > 0.1f) {
                    visibleModuleIndex++;
                }
            }
            if (this.blinkTimer.getValue()) {
                BlinkModules blinkingModule = Cheadleware.blinkManager.getBlinkingModule();
                if (blinkingModule != BlinkModules.NONE && blinkingModule != BlinkModules.AUTO_BLOCK) {
                    long movementPacketSize = Cheadleware.blinkManager.countMovement();
                    if (movementPacketSize > 0L) {
                        String countStr = String.valueOf(movementPacketSize);
                        GlStateManager.enableBlend();
                        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
                        GlStateManager.disableDepth();
                        drawString(countStr, screenX / 2.0F - (float) getStringWidth(countStr) / 2.0F, screenY / 5.0F * 3.0F, this.getColor(currentMillis, visibleModuleIndex).getRGB() & 16777215 | -1090519040, this.shadow.getValue());
                        GlStateManager.enableDepth();
                        GlStateManager.disableBlend();
                    }
                }
            }
        }
    }
}