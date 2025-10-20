package fr.ambient.property.impl;

import fr.ambient.property.Property;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

@Getter
public final class MultiProperty extends Property<Set<String>> {
    private final String[] values;

    private MultiProperty(String label, String[] possibilities, Set<String> initialValues, BooleanSupplier dependency) {
        super(label, initialValues, dependency);
        this.values = possibilities;
    }

    public static MultiProperty newInstance(String label, String[] possibilities, Set<String> initialValues, BooleanSupplier dependency) {
        Set<String> filteredValues = initialValues.stream()
                .filter(value -> Arrays.asList(possibilities).contains(value))
                .collect(Collectors.toSet());
        return new MultiProperty(label, possibilities, filteredValues, dependency);
    }

    public static MultiProperty newInstance(String label, String[] possibilities, Set<String> initialValues) {
        Set<String> filteredValues = initialValues.stream()
                .filter(value -> Arrays.asList(possibilities).contains(value))
                .collect(Collectors.toSet());
        return new MultiProperty(label, possibilities, filteredValues, () -> true);
    }

    public static MultiProperty newInstance(String label, String[] possibilities) {
        return new MultiProperty(label, possibilities, new HashSet<>(), () -> true);
    }

    public static MultiProperty newInstance(String label, String[] possibilities, BooleanSupplier dependency) {
        return new MultiProperty(label, possibilities, new HashSet<>(), dependency);
    }

    public void toggleValue(String mode) {
        if (Arrays.asList(values).contains(mode)) {
            if (getValue().contains(mode)) {
                getValue().remove(mode);
            } else {
                getValue().add(mode);
            }
        }
    }

    public void clearAll(){
        getValue().clear();
    }

    public void setValueOF(String val, boolean onoff){
        if (Arrays.asList(values).contains(val)) {
            if (!onoff) {
                getValue().remove(val);
            } else {
                getValue().add(val);
            }
        }
    }

    public boolean isSelected(String mode) {
        return getValue().contains(mode);
    }

    public void setModesFromIndices(int[] indices) {
        Set<String> selectedModes = new HashSet<>();
        for (int index : indices) {
            if (index >= 0 && index < values.length) {
                selectedModes.add(values[index]);
            }
        }
        setValue(selectedModes);
    }

    public int[] getSelectedIndices() {
        Set<String> selectedModes = getValue();
        return Arrays.stream(values)
                .filter(selectedModes::contains)
                .mapToInt(Arrays.asList(values)::indexOf)
                .toArray();
    }
}