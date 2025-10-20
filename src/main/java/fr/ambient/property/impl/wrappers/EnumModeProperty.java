package fr.ambient.property.impl.wrappers;

import fr.ambient.property.impl.ModeProperty;
import lombok.Getter;

import java.util.ArrayList;
import java.util.function.BooleanSupplier;

public class EnumModeProperty<E extends Enum<E>> {
    @Getter
    private ModeProperty modeProperty;

    private Class<E> eClass;

    public EnumModeProperty(String name, Class<E> enumType, E defVal){
        this(name, enumType, defVal, ()->true);
    }


    public EnumModeProperty(String name, Class<E> enumType, E defVal, BooleanSupplier supplier){

        if (!enumType.isEnum()) {
            throw new IllegalArgumentException("Not Enum. bad.");
        }
        eClass = enumType;


        ArrayList<String> choices = new ArrayList<>();

        for(E e : eClass.getEnumConstants()){
            choices.add(e.toString());
        }

        String[] possibilities = choices.toArray(new String[0]);

        modeProperty = ModeProperty.newInstance(name, possibilities, defVal.toString(),supplier);

    }

    public E getValue(){
        for(E e : eClass.getEnumConstants()){
            if(e.toString().equals(modeProperty.getValue())){
                return e;
            }
        }
        return null;
    }

}
