package femcum.modernfloyd.clients.module.impl.ghost;

import femcum.modernfloyd.clients.component.impl.player.BadPacketsComponent;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.value.impl.NumberValue;

@ModuleInfo(aliases = {"module.ghost.clickassist.name"}, description = "module.ghost.clickassist.description", category = Category.GHOST)
public class ClickAssist extends Module {

    public final NumberValue extraLeftClicks = new NumberValue("Extra Left Clicks", this, 1, 0, 3, 1);
    public final NumberValue extraRightClicks = new NumberValue("Extra Right Clicks", this, 1, 0, 3, 1);

    public int leftClicks, rightClicks;
    private boolean leftClick, rightClick;

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (mc.gameSettings.keyBindAttack.isKeyDown()) {
            if (!leftClick) {
                leftClicks = extraLeftClicks.getValue().intValue();
            }

            leftClick = true;
        } else {
            leftClick = false;
        }

        if (mc.gameSettings.keyBindUseItem.isKeyDown()) {
            if (!rightClick) {
                rightClicks = extraRightClicks.getValue().intValue();
            }

            rightClick = true;
        } else {
            rightClick = false;
        }

        if (leftClicks > 0 && Math.random() > 0.2) {
            leftClicks--;

            if (!mc.thePlayer.isUsingItem() && !BadPacketsComponent.bad()) {
                mc.clickMouse();
            }
        } else if (rightClicks > 0 && Math.random() > 0.2) {
            rightClicks--;

            if (!mc.thePlayer.isUsingItem() && !BadPacketsComponent.bad()) {
                mc.rightClickMouse();
            }
        }
    };
}