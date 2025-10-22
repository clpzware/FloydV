package femcum.modernfloyd.clients.value.impl;

import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.ui.click.standard.components.value.impl.ColorValueComponent;
import femcum.modernfloyd.clients.value.Mode;
import femcum.modernfloyd.clients.value.Value;

import java.awt.*;
import java.util.List;
import java.util.function.BooleanSupplier;
public class ColorValue extends Value<Color> {

    public ColorValue(final String name, final Module parent, final Color defaultValue) {
        super(name, parent, defaultValue);
    }

    public ColorValue(final String name, final Mode<?> parent, final Color defaultValue) {
        super(name, parent, defaultValue);
    }

    public ColorValue(final String name, final Module parent, final Color defaultValue, final BooleanSupplier hideIf) {
        super(name, parent, defaultValue, hideIf);
    }

    public ColorValue(final String name, final Mode<?> parent, final Color defaultValue, final BooleanSupplier hideIf) {
        super(name, parent, defaultValue, hideIf);
    }

    @Override
    public List<Value<?>> getSubValues() {
        return null;
    }

    @Override
    public ColorValueComponent createUIComponent() {
        return new ColorValueComponent(this);
    }
}