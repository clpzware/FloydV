package femcum.modernfloyd.clients.module.impl.render.interfaces.api;

import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.impl.render.Interface;
import femcum.modernfloyd.clients.module.impl.render.TargetInfo;
import femcum.modernfloyd.clients.value.Mode;
import lombok.Getter;

@Getter
public class InterfaceMode<I extends Module> extends Mode<Interface> {

    private final Mode<TargetInfo> targetInfo;

    public InterfaceMode(String name, Interface parent, Mode<TargetInfo> targetInfo) {
        super(name, parent);
        this.targetInfo = targetInfo;
    }
}
