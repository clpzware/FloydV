package fr.ambient.property.impl;

import fr.ambient.property.Property;
import fr.ambient.util.player.ChatUtil;
import lombok.Getter;

import java.util.Arrays;
import java.util.function.BooleanSupplier;

@Getter
public final class ModeProperty extends Property<String> {
    private final String[] values;

    private ModeProperty(String label, String[] possibilities, String value, BooleanSupplier dependency) {
        super(label, value, dependency);
        this.values = possibilities;
    }

    public static ModeProperty newInstance(String label, String[] possibilities, String value, BooleanSupplier dependency) {
        return new ModeProperty(label, possibilities, value, dependency);
    }

    public static ModeProperty newInstance(String label, String[] possibilities, String value) {
        return new ModeProperty(label, possibilities, value, () -> true);
    }

    public void setIndexValue(int index) {
        super.setValue(this.values[Math.max(0, Math.min(this.values.length - 1, index))]);
    }
    public void setValue(String value){
        boolean doExist = false;

        for(String s : values){
            if(s.equals(value)){
                doExist = true;
                break;
            }
        }
        if(doExist){
            super.setValue(value);
        }else{
            ChatUtil.display("Invalid Value detected. Resetting to default. > " + getLabel());
            super.setValue(this.values[0]);
        }


    }

    public int getIndex() {
        return Arrays.asList(values).indexOf(getValue());
    }
    public int getIndexLast() {
        return Arrays.asList(values).size();
    }

    public boolean is(String query) {
        return this.getValue().equalsIgnoreCase(query);
    }
}