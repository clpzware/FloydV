package fr.ambient.module.impl.render.hud;

import fr.ambient.Ambient;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.render.Render2DEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.property.impl.*;
import fr.ambient.theme.Theme;
import fr.ambient.ui.framework.Style;
import fr.ambient.ui.framework.UIComponent;
import fr.ambient.ui.framework.impl.UITextComponent;
import fr.ambient.util.player.MoveUtil;
import fr.ambient.util.render.ColorUtil;
import fr.ambient.util.render.RenderUtil;
import fr.ambient.util.render.batching.RenderBatch;
import fr.ambient.util.render.batching.impl.RoundedRectangle;
import fr.ambient.util.render.font.Fonts;
import fr.ambient.util.render.font.TTFFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.EnumChatFormatting;

import java.awt.*;
import java.text.DecimalFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HUD extends Module {

    public Color getColor1() {
        return color1.getValue();
    }

    public Color getColor2() {
        return color2.getValue();
    }

    public void setCurrentTheme(Theme theme) {
        color1.setValue(theme.getColor1());
        color2.setValue(theme.getColor2());
    }

    public Theme getCurrentTheme() {
        return new Theme("HUD", EnumChatFormatting.BLUE, getColor1(), getColor2(), false);
    }

    public Color getColor(int time, int offset) {
        return ColorUtil.getColorFromIndex(time, offset, color1.getValue(), color2.getValue(), false);
    }

    private final ColorProperty color1 = ColorProperty.newInstance("Color", new Color(143, 156, 201));
    private final ColorProperty color2 = ColorProperty.newInstance("Secondary Color", new Color(207, 195, 252));

    private final BooleanProperty arraylist = BooleanProperty.newInstance("Arraylist", true);
    private final NumberProperty arraylistOffset = NumberProperty.newInstance("Offset", 0f, 5f, 20f, 1f);
    private final ModeProperty font = ModeProperty.newInstance("Font", new String[]{"Tahoma", "OpenSans Medium", "SanFrancisco", "Roboto Medium", "Minecraft", "Nunito", "Greycliff"}, "OpenSans Medium", arraylist::getValue);
    private final NumberProperty fontSize = NumberProperty.newInstance("Font Size", 5f, 20f, 40f, 1f, () -> arraylist.getValue() && !font.is("Minecraft"));
    private final ModeProperty colorMode = ModeProperty.newInstance("Color Mode", new String[]{"Fade", "Breathe", "Static"}, "Fade");

    private final BooleanProperty background = BooleanProperty.newInstance("Background", true, arraylist::getValue);
    private final NumberProperty backgroundOpacity = NumberProperty.newInstance("Opacity", 0f, 40f, 100f, 0.1f, () -> arraylist.getValue() && background.getValue());

    private final ModeProperty sidebarMode = ModeProperty.newInstance("Sidebar Mode", new String[]{"Right", "Left", "Top", "Outline", "None"}, "Right", arraylist::getValue);
    private final ModeProperty separatorMode = ModeProperty.newInstance("Separator Mode", new String[]{"Space", "-", "!", "[]", "{}", "()", "\"\""}, "Space", arraylist::getValue);
    private final NumberProperty backgroundRounding = NumberProperty.newInstance("Background Rounding", 0f, 2.5f, 5f, 0.5f, () -> !sidebarMode.is("Left"));

    private final BooleanProperty showSuffixes = BooleanProperty.newInstance("Show Suffixes", true, arraylist::getValue);
    private final MultiProperty shownCategories = MultiProperty.newInstance("Hidden Categories", new String[]{"Render", "Player", "Misc"});

    private final ModeProperty textCase = ModeProperty.newInstance("Text Case", new String[]{"Standard", "Lower"}, "Standard");

    private final BooleanProperty showInfo = BooleanProperty.newInstance("Display Info", true);
    private final ModeProperty watermarkMode = ModeProperty.newInstance("Watermark Mode", new String[]{"None", "Modern", "CSGO", "Basic"}, "Modern");
    private final BooleanProperty mcFont = BooleanProperty.newInstance("Use Minecraft Font", false, () -> watermarkMode.is("Basic") || showInfo.getValue());

    private List<Module> arrayListModule = new ArrayList<>();
    private String oldFont = "";
    private float oldFontSize = 0;
    public static int scoreboardOffset = 0;
    private TTFFontRenderer arrayListFont;

    private final RenderBatch backgroundBatch = new RenderBatch(RenderUtil.Shapes.ROUNDED_RECT);

    public HUD() {
        super(54, "Displays info such as enabled modules and FPS.", ModuleCategory.RENDER);

        CompositeProperty arraylistCategory = CompositeProperty.newInstance("Arraylist Options", arraylist::getValue);
        arraylistCategory.addChildren(
                arraylistOffset,
                font,
                fontSize,
                colorMode,
                background,
                backgroundOpacity,
                sidebarMode,
                separatorMode,
                backgroundRounding,
                showSuffixes,
                shownCategories,
                textCase
        );

        this.registerProperties(
                color1,
                color2,
                arraylist,
                arraylistCategory,
                watermarkMode,
                showInfo,
                mcFont
        );
    }

    @Override
    protected void onEnable() {
        super.onEnable();
        setFont();
    }

    public void setArraylist() {
        if (!arraylist.getValue() || arrayListFont == null) return;

        arrayListModule = Ambient.getInstance().getModuleManager().getObjects().stream()
                .filter(this::isModuleVisible)
                .sorted(Comparator.comparingDouble(module -> -getModuleTextWidth(module)))
                .toList();
    }

    private boolean isModuleVisible(Module module) {
        return (module.isEnabled() || !module.animation.isFinished()) &&
                !isHiddenByCategory(module) &&
                module.isShown() &&
                !module.getOnlyOnKeyHold().getValue();
    }

    private boolean isHiddenByCategory(Module module) {
        return (module.getCategory() == ModuleCategory.RENDER && shownCategories.isSelected("Render")) ||
                (module.getCategory() == ModuleCategory.PLAYER && shownCategories.isSelected("Player")) ||
                (module.getCategory() == ModuleCategory.MISC && shownCategories.isSelected("Misc"));
    }

    public void setFont() {
        arrayListFont = switch (font.getValue().toLowerCase()) {
            case "opensans medium" -> Fonts.getOpenSansMedium(fontSize.getValue().intValue());
            case "sanfrancisco" -> Fonts.getSanFrancisco(fontSize.getValue().intValue());
            case "roboto medium" -> Fonts.getRobotoMedium(fontSize.getValue().intValue());
            case "minecraft" -> Fonts.getMinecraft(fontSize.getValue().intValue());
            case "nunito" -> Fonts.getNunito(fontSize.getValue().intValue());
            case "greycliff" -> Fonts.getGreycliff(fontSize.getValue().intValue());
            case "tahoma" -> Fonts.getTahoma(fontSize.getValue().intValue());
            default -> null;
        };
    }

    UIComponent modernWatermark = new UIComponent()
            .blur(12)
            .glow(Color.BLACK)
            .rounding(4)
            .position(5, 5)
            .size(120, 18)
            .color(Style.SOLID, new Color(0x90121214, true))
            .children(
                    new UITextComponent()
                            .font(Fonts.getNunito(17))
                            .text("Ambient")
                            .margin(5, 5, 5, 5)
                            .id("clientname")
                            .color(Style.SOLID),
                    new UITextComponent()
                            .font(Fonts.getNunito(17))
                            .text("user")
                            .id("username")
                            .margin(45.5f, 5, 5, 5),
                    new UITextComponent()
                            .font(Fonts.getNunito(17))
                            .text("time")
                            .id("time")
                            .margin(80, 5, 5, 5)
            );

    @SubscribeEvent
    private void onRender2D(Render2DEvent event) {
        ScaledResolution sr = new ScaledResolution(mc);

        if (Ambient.getInstance().getMsSinceLast().finished(30000)) {
            RenderUtil.drawRect(10, 10, 350, 40, new Color(40, 40, 40, 120));
            Fonts.getOpenSansBold(25).drawString("ALERT", 15, 15, Color.WHITE.getRGB());
            Fonts.getOpenSansBold(16).drawString("You will get disconnected since you are no longer connected to the backend.", 15, 30, Color.WHITE.getRGB());
            RenderUtil.drawRect(10, 48, Math.min(350 * (Ambient.getInstance().getMsSinceLast().getTime() / 60000f), 350), 2, Color.RED);
        }

        if (!Ambient.getInstance().getFlashbang().finished(2000)) {
            RenderUtil.drawRect(0, 0, sr.getScaledWidth(), sr.getScaledHeight(), Color.WHITE);
        }

        if (showInfo.getValue()) {
            DecimalFormat decimalFormat = new DecimalFormat("#.##");
            String bpsText = "BPS §f" + decimalFormat.format(MoveUtil.getSpeed(mc.thePlayer));
            String fpsText = "FPS §f" + Minecraft.getDebugFPS();

            if (mcFont.getValue()) {
                mc.fontRendererObj.drawStringWithShadow(bpsText, 3,
                        sr.getScaledHeight() - 5 - mc.fontRendererObj.FONT_HEIGHT * 2,
                        Ambient.getInstance().getHud()
                                .getCurrentTheme()
                                .getColor(4, 3 * 2)
                                .getRGB()
                );

                mc.fontRendererObj.drawStringWithShadow(fpsText, 3,
                        sr.getScaledHeight() - 3 - mc.fontRendererObj.FONT_HEIGHT,
                        Ambient.getInstance().getHud()
                                .getCurrentTheme()
                                .getColor(4, 3 * 2)
                                .getRGB()
                );
            } else {
                Fonts.getSanFrancisco(19).drawString(bpsText, 3,
                        sr.getScaledHeight() - 5 - Fonts.getSanFrancisco(19).getHeight(fpsText) * 2,
                        Ambient.getInstance().getHud()
                                .getCurrentTheme()
                                .getColor(4, 3 * 2)
                                .getRGB()
                );

                Fonts.getSanFrancisco(19).drawString(fpsText, 3,
                        sr.getScaledHeight() - 3 - Fonts.getSanFrancisco(19).getHeight(fpsText),
                        Ambient.getInstance().getHud()
                                .getCurrentTheme()
                                .getColor(4, 3 * 2)
                                .getRGB()
                );
            }
        }

        LocalTime currentTime = LocalTime.now();
        switch (watermarkMode.getValue().toLowerCase()) {
            case "modern":
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                String formattedTime = currentTime.format(formatter);

                TTFFontRenderer FONT = Fonts.getNunito(17);
                Color textColor = Ambient.getInstance().getHud()
                        .getCurrentTheme()
                        .getColor2();

                String userinfo = Ambient.getInstance().getUsername() + " §7(" + Ambient.getInstance().getUid() + ")";
                float width = FONT.getWidth("Ambient" + userinfo + formattedTime) + 20;

                modernWatermark.size(width, 18);
                modernWatermark.findChild(UITextComponent.class, "clientname").color(Style.SOLID, textColor);
                modernWatermark.findChild(UITextComponent.class, "username").text(userinfo);

                modernWatermark.findChild(UITextComponent.class, "time").margin(50 + FONT.getWidth(userinfo), 5, 5, 5);
                modernWatermark.findChild(UITextComponent.class, "time").text(formattedTime);

                modernWatermark.render();

                RenderUtil.drawLine(52.5f + FONT.getWidth(userinfo), 9f, 0f, 10f, 1, new Color(255, 255, 255, 70));
                RenderUtil.drawLine(47.5f, 9f, 0f, 10f, 1, new Color(255, 255, 255, 70));

                break;
            case "csgo":
                DateTimeFormatter csformatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                String csformattedTime = currentTime.format(csformatter);
                String user = Ambient.getInstance().getUsername();

                Fonts.getTahoma(15).drawStringWithShadow("ambient", 4, 4, -1);
                Fonts.getTahoma(15).drawStringWithShadow("client", 4 + Fonts.getTahoma(15).getWidth("ambient"), 4, Ambient.getInstance().getHud().getCurrentTheme().getColor2().getRGB());
                Fonts.getTahoma(15).drawStringWithShadow(" | " + user + " | " + csformattedTime, 4 + Fonts.getTahoma(15).getWidth("ambientclient"), 4, -1);
                break;
            case "basic":
                if (!mcFont.getValue())
                    Fonts.getSanFrancisco(19).drawStringWithShadow("A§fmbient", 3, 3,
                            Ambient.getInstance().getHud()
                                    .getCurrentTheme()
                                    .getColor(4, 3 * 2)
                                    .getRGB()
                    );
                else
                    mc.fontRendererObj.drawStringWithShadow("A§fmbient", 3, 3,
                            Ambient.getInstance().getHud()
                                    .getCurrentTheme()
                                    .getColor(4, 3 * 2)
                                    .getRGB()
                    );
                break;
        }

        if (!arraylist.getValue()) return;

        if (!oldFont.equals(font.getValue()) || oldFontSize != fontSize.getValue()) {
            setFont();
        }

        setArraylist();
        oldFontSize = fontSize.getValue();
        oldFont = font.getValue();

        renderArrayList(scaledResolution());
    }

    private void renderArrayList(ScaledResolution sr) {
        float lastWidth = 0;
        float offset = arraylistOffset.getValue();

        for (Module module : arrayListModule) {
            module.animation.run(module.isEnabled() ? 1.0F : 0.0F);

            if (shouldSkipModule(module)) continue;

            String text = prepareModuleText(module);
            float animationValue = (float) module.animation.getValue();
            float textWidth = getTextWidth(text);
            float textOffsetX = calculateTextOffsetX(sr.getScaledWidth(), textWidth, animationValue);
            float rectHeight = calculateRectHeight(text);

            Color[] colors = getColors(offset, rectHeight, animationValue);
            Color color = colors[0];
            Color secondColor = colors[1];

            if (background.getValue()) {
                float[] backgroundRadius = calculateBackgroundRadius(module, textWidth);
                renderBackground(textWidth, offset, rectHeight, animationValue, color, secondColor, backgroundRadius);
            }


            backgroundBatch.renderBatch();

            renderSidebar(module, textOffsetX, textWidth, offset, rectHeight, color, secondColor, lastWidth);
            renderText(text, textOffsetX, offset, color);

            offset += rectHeight * animationValue;

            Scoreboard scoreboard = Ambient.getInstance().getModuleManager().getModule(Scoreboard.class);
            scoreboardOffset = (int) Math.max(0, offset - (scoreboard.getY() - 10));

            lastWidth = textWidth;
        }
    }

    private boolean shouldSkipModule(Module module) {
        return isHiddenByCategory(module) ||
                !module.isShown() ||
                (!module.isEnabled() && module.animation.isFinished()) ||
                module.animation.getValue() == 0.0F;
    }

    private String prepareModuleText(Module module) {
        String text = module.getCustomName();
        if (showSuffixes.getValue() && !module.getSuffix().isEmpty()) {
            text += switch (separatorMode.getValue().toLowerCase()) {
                case "space" -> " §7" + module.getSuffix();
                case "-" -> " §7- " + module.getSuffix();
                case "!" -> " §7! " + module.getSuffix();
                case "[]" -> " §7[" + module.getSuffix() + "]";
                case "{}" -> " §7{" + module.getSuffix() + "}";
                case "()" -> " §7(" + module.getSuffix() + ")";
                case "\"\"" -> " §7\"" + module.getSuffix() + "\"";
                default -> "";
            };
        }
        return text;
    }

    private float getModuleTextWidth(Module module) {
        String text = prepareModuleText(module);

        if (textCase.is("Lower")) {
            text = text.toLowerCase();
        }

        return getTextWidth(text);
    }

    private float getTextWidth(String text) {
        if (textCase.is("Lower")) {
            text = text.toLowerCase();
        }

        return font.is("Minecraft")
                ? mc.fontRendererObj.getStringWidth(text)
                : arrayListFont.getWidth(text);
    }

    private float calculateTextOffsetX(float scaledWidth, float textWidth, float animationValue) {
        return scaledWidth - (textWidth + 4 + arraylistOffset.getValue()) * animationValue;
    }

    private float calculateRectHeight(String text) {
        return font.is("Minecraft")
                ? mc.fontRendererObj.FONT_HEIGHT + 2
                : arrayListFont.getHeight(text) + 2;
    }

    private float[] calculateBackgroundRadius(Module module, float textWidth) {
        float rounding = 0;

        if (!sidebarMode.is("Left")) {
            Module nextModule = getNextModule(module);
            if (nextModule == null) {
                rounding = backgroundRounding.getValue();
            } else {
                float nextWidth = getModuleTextWidth(nextModule);
                rounding = Math.min(backgroundRounding.getValue(), textWidth - nextWidth);
                rounding = Math.max(0, rounding);
            }
        }

        boolean isFirstModule = arrayListModule.indexOf(module) == 0;
        boolean isLastModule = arrayListModule.getLast() == module;

        return new float[]{
                isFirstModule && !(sidebarMode.is("Left") || sidebarMode.is("Top")) ? backgroundRounding.getValue() : 0,
                0,
                !(sidebarMode.is("Right") || sidebarMode.is("Outline")) && isLastModule ? rounding : 0,
                !sidebarMode.is("Outline") ? rounding : 0
        };
    }

    private Module getNextModule(Module currentModule) {
        int currentIndex = arrayListModule.indexOf(currentModule);
        return currentIndex + 1 < arrayListModule.size() ? arrayListModule.get(currentIndex + 1) : null;
    }

    private Color[] getColors(float offset, float rectHeight, float animationValue) {
        Color color = color1.getValue();
        Color secondColor = color2.getValue();

        switch (colorMode.getValue()) {
            case "Breathe", "Static" -> {
                color = ColorUtil.getColorFromIndex(4, 0, color1.getValue(), color2.getValue(), false);
                secondColor = colorMode.getValue().equals("Static") ? color : secondColor;
            }
            case "Fade" -> {
                int colorOffset1 = (int) -(offset - (rectHeight * animationValue) / 2) * 2;
                int colorOffset2 = (int) -(offset + (rectHeight * animationValue) / 2) * 2;
                color = ColorUtil.getColorFromIndex(4, colorOffset1, color1.getValue(), color2.getValue(), false);
                secondColor = ColorUtil.getColorFromIndex(4, colorOffset2, color1.getValue(), color2.getValue(), false);
            }
        }
        return new Color[]{color, secondColor};
    }

    private void renderBackground(float textWidth, float offset, float rectHeight, float animationValue, Color color, Color secondColor, float[] rounding) {
        int opacity = (int) (backgroundOpacity.getValue() * 2.55);
        Color bgColor = new Color(0, 0, 0, opacity);

        float textX = calculateTextOffsetX(scaledResolution().getScaledWidth(), textWidth, animationValue);

        PostProcessing postProcessing = Ambient.getInstance().getModuleManager().getModule(PostProcessing.class);
        if (postProcessing.isEnabled()) {
            addGlowEffect(textX, offset, textWidth, rectHeight, rounding, color, secondColor);
            addBlurEffect(textX, offset, textWidth, rectHeight, rounding);
        }

        backgroundBatch.addShape(RoundedRectangle.variableSolid(
                textX,
                offset - 1.25f,
                textWidth + 4.5f,
                rectHeight + 0.5f,
                rounding,
                bgColor));
    }

    private void addGlowEffect(float x, float y, float width, float height, float[] rounding, Color color, Color secondColor) {

    }

    private void addBlurEffect(float x, float y, float width, float height, float[] rounding) {

    }

    private void renderSidebar(Module module, float textOffsetX, float textWidth, float offset, float rectHeight, Color color, Color secondColor, float lastWidth) {
        boolean isFirstLine = (lastWidth == 0);
        boolean isLastLine = (module == arrayListModule.getLast());

        switch (sidebarMode.getValue()) {
            case "Right" ->
                    RenderUtil.drawRect(textOffsetX + textWidth + 4, offset - 1, 1, rectHeight, color);
            case "Left" -> RenderUtil.drawRect(textOffsetX, offset - 1, 1, rectHeight, color);
            case "Top" -> {
                if (isFirstLine)
                    RenderUtil.drawLine(textOffsetX, offset - 1.5f, textWidth + 4.5f, 0, 2, color);
            }
            case "Outline" -> {
                RenderUtil.drawRect(textOffsetX + textWidth + 4, offset - 1, 1, rectHeight, color);
                RenderUtil.drawRect(textOffsetX, offset - 1, 1, rectHeight, color);

                float diff = Math.abs(lastWidth - textWidth);
                float lineOffset = isFirstLine ? textOffsetX : textOffsetX - diff;
                float lineWidth = isFirstLine ? textWidth + 4 : diff;

                RenderUtil.drawLine(lineOffset, offset - 1, lineWidth, 0, 2, color);
                if (isLastLine) {
                    RenderUtil.drawLine(textOffsetX, offset + rectHeight - 1, textWidth + 5, 0, 2, color);
                }
            }
        }
    }

    private void renderText(String text, float textOffsetX, float offset, Color color) {
        if (textCase.is("Lower")) {
            text = text.toLowerCase();
        }

        if (font.is("Minecraft")) {
            mc.fontRendererObj.drawStringWithShadow(text, textOffsetX + 2, offset, color.getRGB());
        } else {
            arrayListFont.drawStringWithShadow(text, textOffsetX + 2, offset, color.getRGB());
        }
    }
}