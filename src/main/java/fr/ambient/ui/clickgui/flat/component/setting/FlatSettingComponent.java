package fr.ambient.ui.clickgui.flat.component.setting;

import fr.ambient.property.Property;
import fr.ambient.ui.framework.Component;
import lombok.Getter;

public class FlatSettingComponent<T extends Property<?>> extends Component {
    @Getter
    protected final T property;

    public FlatSettingComponent(T property) {
        this.property = property;
    }
}
