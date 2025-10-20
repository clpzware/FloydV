package fr.ambient.module.impl.render.hud;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.render.Render2DEvent;
import fr.ambient.module.AnchorPoint;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.property.impl.BooleanProperty;
import fr.ambient.property.impl.NumberProperty;
import fr.ambient.util.render.ColorUtil;
import fr.ambient.util.render.RenderUtil;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class Scoreboard extends Module {

    private final BooleanProperty shadow = BooleanProperty.newInstance("Text Shadow", true);
    private final BooleanProperty background = BooleanProperty.newInstance("Render Background", true);
    private final BooleanProperty blurBackground = BooleanProperty.newInstance("Post Process", false, () -> background.getValue());
    private final NumberProperty radius = NumberProperty.newInstance("Rounding", 0f, 4f, 10f, 0.5f, background::getValue);

    public Scoreboard() {
        super(80, "Customizes or hides the in-game scoreboard.", ModuleCategory.RENDER);
        this.setDraggable(true);
        ScaledResolution sr = new ScaledResolution(mc);

        this.setX(sr.getScaledWidth() - 5);
        this.setY(sr.getScaledHeight() / 2);
        this.setAnchorPoint(AnchorPoint.RIGHT);
        setWidth(70);
        setHeight(100);

        registerProperties(shadow, background, radius, blurBackground);
    }

    @SubscribeEvent
    public void onRender2D(Render2DEvent e) {
        GL11.glPushAttrib(GL11.GL_CURRENT_BIT);
        GlStateManager.resetColor();

        if (this.mc.theWorld == null) {
            GL11.glPopAttrib();
            return;
        }

        net.minecraft.scoreboard.Scoreboard scoreboard = this.mc.theWorld.getScoreboard();

        if (scoreboard == null) {
            GL11.glPopAttrib();
            return;
        }

        ScoreObjective scoreobjective = null;
        ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(this.mc.thePlayer.getName());

        if (scoreplayerteam != null) {
            int j1 = scoreplayerteam.getChatFormat().getColorIndex();

            if (j1 >= 0) {
                scoreobjective = scoreboard.getObjectiveInDisplaySlot(3 + j1);
            }
        }

        ScoreObjective scoreobjective1 = scoreobjective != null ? scoreobjective : scoreboard.getObjectiveInDisplaySlot(1);
        if (scoreobjective1 != null) {
            if (background.getValue()) {
                RenderUtil.drawRoundedRect(getAnchoredX() - 3, getY() - 3, getWidth() + 6, getHeight() + 6, radius.getValue(), ColorUtil.withAlpha(Color.BLACK, 0.32f));
            }
            mc.ingameGUI.renderScoreboard(scoreobjective1, (int) getAnchoredX() + 5, getY() + 1, new Color(0, 0, 0, 0), shadow.getValue());
        }

        GL11.glPopAttrib();
    }
}
