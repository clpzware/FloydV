package fr.ambient.property;

import lombok.Getter;
import lombok.Setter;

import java.util.function.BooleanSupplier;

public abstract class Property<T> {
    @Getter
    private final String label;
    @Getter
    @Setter
    private T value;
    private final BooleanSupplier dependency;

    protected Property(String label, T value, BooleanSupplier dependency) {
        this.label = label;
        this.value = value;
        this.dependency = dependency;
    }

    public boolean isAvailable() {
        return dependency.getAsBoolean();
    }

    public boolean isVisible() {
        return isAvailable();
    }
}