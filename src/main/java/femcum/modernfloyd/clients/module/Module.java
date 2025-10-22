package femcum.modernfloyd.clients.module;

import femcum.modernfloyd.clients.Floyd;
import femcum.modernfloyd.clients.bindable.Bindable;
import femcum.modernfloyd.clients.event.impl.other.ModuleToggleEvent;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.util.Accessor;
import femcum.modernfloyd.clients.util.interfaces.ThreadAccess;
import femcum.modernfloyd.clients.util.interfaces.Toggleable;
import femcum.modernfloyd.clients.util.localization.Localization;
import femcum.modernfloyd.clients.value.Value;
import femcum.modernfloyd.clients.value.impl.BooleanValue;
import femcum.modernfloyd.clients.value.impl.ModeValue;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
@Getter
@Setter
public abstract class Module implements Accessor, ThreadAccess, Toggleable, Bindable {

    private String[] aliases;
    private final List<Value<?>> values = new ArrayList<>();
    private ModuleInfo moduleInfo;
    private boolean enabled;
    private int key;

    public Module() {
        if (this.getClass().isAnnotationPresent(ModuleInfo.class)) {
            this.moduleInfo = this.getClass().getAnnotation(ModuleInfo.class);

            this.aliases = Arrays.stream(this.moduleInfo.aliases())
                    .map(Localization::get).toArray(String[]::new);
            this.key = getModuleInfo().keyBind();
        } else {
            throw new RuntimeException("ModuleInfo annotation not found on " + this.getClass().getSimpleName());
        }
    }

    public Module(final ModuleInfo info) {
        this.moduleInfo = info;

        this.aliases = this.moduleInfo.aliases();
        this.key = getModuleInfo().keyBind();
    }

    @Override
    public String getName() {
        return aliases[0];
    }

    public void onKey() {
        this.toggle();
    }

    @Override
    public int getKey() {
        return key;
    }

    public void toggle() {
        this.setEnabled(!enabled);
    }

    public void setEnabled(final boolean enabled) {
        if (this.enabled == enabled || (!this.moduleInfo.allowDisable() && !enabled)) {
            return;
        }

        this.enabled = enabled;

        Floyd.INSTANCE.getEventBus().handle(new ModuleToggleEvent(this));

//        SoundUtil.toggleSound(enabled);

        if (enabled) {
            superEnable();
        } else {
            superDisable();
        }
    }

    /**
     * Called when a module gets enabled
     * -> important: whenever you override this method in a subclass
     * keep the super.onEnable()
     */
    public final void superEnable() {
        Floyd.INSTANCE.getEventBus().register(this);

        this.values.stream()
                .filter(value -> value instanceof ModeValue)
                .forEach(value -> ((ModeValue) value).getValue().register());

        this.values.stream()
                .filter(value -> value instanceof BooleanValue)
                .forEach(value -> {
                    final BooleanValue booleanValue = (BooleanValue) value;
                    if (booleanValue.getMode() != null && booleanValue.getValue()) {
                        booleanValue.getMode().register();
                    }
                });

        this.onEnable();
    }

    /**
     * Called when a module gets disabled
     * -> important: whenever you override this method in a subclass
     * keep the super.onDisable()
     */
    public final void superDisable() {
        Floyd.INSTANCE.getEventBus().unregister(this);

        this.values.stream()
                .filter(value -> value instanceof ModeValue)
                .forEach(value -> ((ModeValue) value).getValue().unregister());

        this.values.stream()
                .filter(value -> value instanceof BooleanValue)
                .forEach(value -> {
                    final BooleanValue booleanValue = (BooleanValue) value;
                    if (booleanValue.getMode() != null) {
                        booleanValue.getMode().unregister();
                    }
                });

        this.onDisable();
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public List<Value<?>> getAllValues() {
        ArrayList<Value<?>> allValues = new ArrayList<>();

        values.forEach(value -> {
            List<Value<?>> subValues = value.getSubValues();

            allValues.add(value);

            if (subValues != null) {
                allValues.addAll(subValues);
            }
        });

        return allValues;
    }
}