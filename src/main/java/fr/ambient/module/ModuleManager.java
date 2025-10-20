package fr.ambient.module;

import fr.ambient.property.Property;
import fr.ambient.property.impl.CompositeProperty;
import fr.ambient.structure.Manager;

import java.util.List;
import java.util.Objects;

public class ModuleManager extends Manager <Module> {
    @SuppressWarnings("unchecked")
    public <T extends Module> T getModule(final Class<T> clazz) {
        return (T) this.getBy(module -> Objects.equals(module.getClass(), clazz));
    }

    @SuppressWarnings("unchecked")
    public <T extends Module> T getModule(String name) {
        return (T) getObjects().stream()
                .filter(module -> module.getName().equalsIgnoreCase(name))
                .findAny()
                .orElse(null);
    }

    public List<Module> getModulesFromCategory(ModuleCategory category) {
        return this.getMultipleBy(module -> module.getCategory() == category);
    }

    public int settingCount(){
        int cnt = 0;
        for(Module module : this.getObjects()){
            for(Property property : module.propertyList){
                cnt++;
                if(property instanceof CompositeProperty compositeProperty){
                    cnt += settingInComp(compositeProperty);
                }
            }
        }
        return cnt;
    }

    public int settingInComp(CompositeProperty compositeProperty){
        int cnt = 0;
        for(Property property : compositeProperty.getChildren()){
            cnt++;
            if(property instanceof CompositeProperty compositeProperty1){
                cnt += settingInComp(compositeProperty1);
            }
        }
        return cnt;
    }
}