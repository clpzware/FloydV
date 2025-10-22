package femcum.modernfloyd.clients.value.impl;

import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.ui.click.standard.components.value.impl.CurveValueComponent;
import femcum.modernfloyd.clients.value.Mode;
import femcum.modernfloyd.clients.value.Value;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class CurveValue extends Value<Supplier<Double>> {

    public CurveValue(final String name, final Module parent) {
        super(name, parent, null);
    }

    public CurveValue(final String name, final Mode<?> parent) {
        super(name, parent, null);
    }

    public CurveValue(final String name, final Module parent, final BooleanSupplier hideIf) {
        super(name, parent, null, hideIf);
    }

    public CurveValue(final String name, final Mode<?> parent, final BooleanSupplier hideIf) {
        super(name, parent, null, hideIf);
    }

    @Override
    public List<Value<?>> getSubValues() {
        return null;
    }

    @Override
    public CurveValueComponent createUIComponent() {
        return new CurveValueComponent(this);
    }
}