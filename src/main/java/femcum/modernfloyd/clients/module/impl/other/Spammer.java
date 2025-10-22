package femcum.modernfloyd.clients.module.impl.other;

import femcum.modernfloyd.clients.Floyd;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.util.chat.ChatUtil;
import femcum.modernfloyd.clients.util.player.ServerUtil;
import femcum.modernfloyd.clients.value.impl.NumberValue;
import femcum.modernfloyd.clients.value.impl.StringValue;
import rip.vantage.commons.util.time.StopWatch;

@ModuleInfo(aliases = {"module.other.spammer.name"}, description = "module.other.spammer.description", category = Category.PLAYER)
public final class Spammer extends Module {

    private final StringValue message = new StringValue("Message", this, "Buy Rise at riseclient.com!");
    private final NumberValue delay = new NumberValue("Delay", this, 3000, 0, 20000, 1);

    private final StopWatch stopWatch = new StopWatch();

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (ServerUtil.isOnServer("loyisa.cn") && (!Floyd.DEVELOPMENT_SWITCH && message.getValue().startsWith("/"))) {
            ChatUtil.display("Upon a request from Loyisa we have blacklisted Loyisa's Test Server from Spammer.");
            this.toggle();
            return;
        }

        if (this.stopWatch.finished(delay.getValue().longValue())) {
            if (message.getValue().startsWith("#")) {
                ChatUtil.display("Spammer message cannot contain #. You're not spamming IRC Skid.");
                return;
            }

            mc.thePlayer.sendChatMessage(message.getValue());
            this.stopWatch.reset();
        }
    };
}
