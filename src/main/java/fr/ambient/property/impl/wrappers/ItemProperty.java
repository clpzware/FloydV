package fr.ambient.property.impl.wrappers;

import fr.ambient.property.impl.ModeProperty;
import lombok.Getter;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BooleanSupplier;

public class ItemProperty{
    @Getter
    private ModeProperty modeProperty;

    private HashMap<String, Item> nameToItem = new HashMap<>();

    public ItemProperty(String name, Item defVal){
        this(name, defVal, ()->true);
    }


    public ItemProperty(String name, Item defVal, BooleanSupplier supplier){
        ArrayList<String> choices = new ArrayList<>();

        for(Item e : Item.itemRegistry.valueItems){
            choices.add(e.getUnlocalizedName());
            nameToItem.put(e.getUnlocalizedName(), e);
        }

        String[] possibilities = choices.toArray(new String[0]);

        modeProperty = ModeProperty.newInstance(name, possibilities, defVal.toString(),supplier);

    }

    public Item getValue(){
        return nameToItem.get(modeProperty.getValue());
    }

}
