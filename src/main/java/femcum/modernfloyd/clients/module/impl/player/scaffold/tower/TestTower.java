package femcum.modernfloyd.clients.module.impl.player.scaffold.tower;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.event.impl.motion.StrafeEvent;
import femcum.modernfloyd.clients.module.impl.player.Scaffold;
import femcum.modernfloyd.clients.util.player.PlayerUtil;
import femcum.modernfloyd.clients.value.Mode;

public class TestTower  extends Mode<Scaffold> {
    public TestTower(String name, Scaffold parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {


    };

    @EventLink
    public final Listener<StrafeEvent> onStrafe = event -> {
        if(mc.gameSettings.keyBindJump.isKeyDown() && mc.thePlayer.onGround && PlayerUtil.blockNear(1)){
       mc.thePlayer.motionY = .81;
       mc.thePlayer.jump();

        }

        if(mc.thePlayer.offGroundTicks < 4 && mc.gameSettings.keyBindJump.isKeyDown() && PlayerUtil.blockNear(1)){
            mc.thePlayer.motionY = .78;
        } else if(mc.gameSettings.keyBindJump.isKeyDown() && mc.thePlayer.offGroundTicks > 4){

        }
   };

    }

