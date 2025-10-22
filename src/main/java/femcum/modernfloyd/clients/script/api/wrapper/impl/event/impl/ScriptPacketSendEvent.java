package femcum.modernfloyd.clients.script.api.wrapper.impl.event.impl;

import femcum.modernfloyd.clients.event.impl.packet.PacketSendEvent;
import femcum.modernfloyd.clients.script.api.wrapper.impl.event.CancellableScriptEvent;
public class ScriptPacketSendEvent extends CancellableScriptEvent<PacketSendEvent> {

    public ScriptPacketSendEvent(final PacketSendEvent wrappedEvent) {
        super(wrappedEvent);
    }

    @Override
    public String getHandlerName() {
        return "onPacketSend";
    }
}
