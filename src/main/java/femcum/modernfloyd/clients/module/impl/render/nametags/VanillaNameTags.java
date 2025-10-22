package femcum.modernfloyd.clients.module.impl.render.nametags;

import femcum.modernfloyd.clients.component.impl.player.TargetComponent;
import femcum.modernfloyd.clients.component.impl.render.ProjectionComponent;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.render.Render2DEvent;
import femcum.modernfloyd.clients.module.impl.render.NameTags;
import femcum.modernfloyd.clients.util.font.Font;
import femcum.modernfloyd.clients.util.player.PlayerUtil;
import femcum.modernfloyd.clients.util.render.RenderUtil;
import femcum.modernfloyd.clients.value.Mode;
import femcum.modernfloyd.clients.value.impl.BooleanValue;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;

import javax.vecmath.Vector4d;
import java.awt.*;

import static femcum.modernfloyd.clients.layer.Layers.*;

public class VanillaNameTags extends Mode<NameTags> {

    public VanillaNameTags(String name, NameTags parent) {
        super(name, parent);
    }
    private final BooleanValue showTeam = new BooleanValue("Show Team Tag", this, false);

    @EventLink()
    public final Listener<Render2DEvent> onRender2D = event -> {
        Font font = mc.fontRendererObj;

        GlStateManager.pushMatrix();

        for (EntityLivingBase entity : TargetComponent.getTargets(this.getClass(), getParent().player.getValue(), getParent().invisibles.getValue(), getParent().animals.getValue(), getParent().mobs.getValue(), getParent().teams.getValue())) {
            if (entity == mc.thePlayer) {
                continue;
            }

            String text = entity.getDisplayName().getUnformattedText();

            if (showTeam.getValue() && PlayerUtil.sameTeam(entity)) {
                text += " §a[TEAM]";
            }

            entity.hideNameTag();

            Vector4d position = ProjectionComponent.get(entity);

            if (position == null) {
                continue;
            }

            float padding = 2;
            int height = 8;
            float width = getParent().getWidth(text, font);

            float posX = (float) (position.x + (position.z - position.x) / 2);
            float posY = (float) position.y - height;

            getLayer(BLOOM).add(() -> RenderUtil.rectangle(posX - width / 2 - padding, posY - padding - 3, width + padding * 2,
                    height + padding * 2, getTheme().getDropShadow()));

            getLayer(BLUR).add(() -> RenderUtil.rectangle(posX - width / 2 - padding, posY - padding - 3, width + padding * 2,
                    height + padding * 2, Color.BLACK));
            String finalText = text;
            getLayer(REGULAR).add(() -> {
                RenderUtil.rectangle(posX - width / 2 - padding, posY - padding - 3, width + padding * 2,
                        height + padding * 2, getTheme().getBackgroundShade());

                font.drawCentered(finalText, posX + 0.5f, posY - 2, Color.WHITE.getRGB());
            });
        }

        GlStateManager.popMatrix();
    };
}
