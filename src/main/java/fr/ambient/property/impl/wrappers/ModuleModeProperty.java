package fr.ambient.property.impl.wrappers;

import com.viaversion.viaversion.util.ChatColorUtil;
import fr.ambient.Ambient;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.world.TickEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.property.impl.ModeProperty;
import fr.ambient.util.player.ChatUtil;
import lombok.Getter;

import java.util.HashMap;
import java.util.Objects;

public class ModuleModeProperty {
    @Getter
    private final ModeProperty modeProperty;
    @Getter
    private final HashMap<String,ModuleMode> moduleModes = new HashMap<>();

    @Getter
    private Module m;
    private String lastMode = "";

    public ModuleModeProperty(Module module, String name, String defaultName, ModuleMode... moduleModes){
        String[] modes = new String[moduleModes.length];
        int a = 0;
        for(ModuleMode moduleMode : moduleModes){
            modes[a] = moduleMode.getModeName();
            this.moduleModes.put(moduleMode.getModeName(), moduleMode);
            a++;
        }
        this.modeProperty = ModeProperty.newInstance(name, modes, defaultName);
        m = module;

        lastMode = modeProperty.getValue();
        Ambient.getInstance().getEventBus().register(this);
    }

    public void onEnable(){
        lastMode = modeProperty.getValue();
        this.moduleModes.get(modeProperty.getValue()).onEnable();
        Ambient.getInstance().getEventBus().register(this.moduleModes.get(modeProperty.getValue()));
    }

    public void onDisable(){
        lastMode = modeProperty.getValue();
        this.moduleModes.get(modeProperty.getValue()).onDisable();
        for(ModuleMode moduleMode : moduleModes.values()){
            Ambient.getInstance().getEventBus().unregister(moduleMode);
        }
    }

    @SubscribeEvent
    public void onRe(TickEvent event){
        if(m.isEnabled()){
            if(!Objects.equals(lastMode, this.modeProperty.getValue())){
                ModuleMode last = moduleModes.get(lastMode);
                if (last != null) {
                    Ambient.getInstance().getEventBus().unregister(last);
                    last.onDisable();
                }
                ModuleMode current = moduleModes.get(this.modeProperty.getValue());
                if (current != null) {
                    Ambient.getInstance().getEventBus().register(current);
                    current.onEnable();
                }
                lastMode = this.modeProperty.getValue();
            }
        }
    }
}
