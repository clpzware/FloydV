package fr.ambient.property.impl;

import fr.ambient.property.Property;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;

@Getter
public class CompositeProperty extends Property<Void> {
    private final List<Property<?>> children;

    private CompositeProperty(String label, BooleanSupplier dependency) {
        super(label, null, dependency);
        this.children = new ArrayList<>();
    }



    public static CompositeProperty newInstance(String label, Property<?>[] props, BooleanSupplier dependency) {
        CompositeProperty c = new CompositeProperty(label, dependency);
        for(Property p : props){
            c.addChild(p);
        }
        return c;
    }

    public static CompositeProperty newInstance(String label, Property<?>[] props) {
        CompositeProperty c = new CompositeProperty(label, ()->true);
        for(Property p : props){
            c.addChild(p);
        }
        return c;
    }

    public static CompositeProperty newInstance(String label, BooleanSupplier dependency) {
        return new CompositeProperty(label, dependency);
    }

    public static CompositeProperty newInstance(String label) {
        return new CompositeProperty(label, () -> true);
    }

    public void addChild(Property<?> property) {
        children.add(property);
    }

    public final void addChildren(Property<?>... properties) {
        children.addAll(Arrays.asList(properties));
    }

    public void removeChild(Property<?> property) {
        children.remove(property);
    }

    public Property<?> getChild(String label) {
        return children.stream()
                .filter(property -> property.getLabel().equals(label))
                .findFirst()
                .orElse(null);
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }
}
