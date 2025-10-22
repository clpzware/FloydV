package femcum.modernfloyd.clients.util;

import femcum.modernfloyd.clients.Floyd;
import femcum.modernfloyd.clients.component.Component;
import femcum.modernfloyd.clients.layer.Layer;
import femcum.modernfloyd.clients.layer.LayerManager;
import femcum.modernfloyd.clients.layer.Layers;
import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.ui.click.standard.RiseClickGUI;
import femcum.modernfloyd.clients.ui.theme.Themes;
import com.google.gson.Gson;
import net.minecraft.client.Minecraft;

public interface Accessor {
    Minecraft mc = Minecraft.getMinecraft();

    default Floyd getInstance() {
        return Floyd.INSTANCE;
    }

    default LayerManager getLayerManager() {
        return getInstance().getLayerManager();
    }

    default RiseClickGUI getClickGUI() {
        return getInstance().getClickGUI();
    }

    default Layer getLayer(Layers layer) {
        return getLayerManager().get(layer);
    }

    default Layer getLayer(Layers layer, int group) {
        return getLayerManager().get(layer, group);
    }

    default <T extends Component> T getComponent(Class<T> component) {
        return getInstance().getComponentManager().get(component);
    }

    default Themes getTheme() {
        return getInstance().getThemeManager().getTheme();
    }

    default <T extends Module> T getModule(final Class<T> clazz) {
        return getInstance().getModuleManager().get(clazz);
    }

    default Gson getGSON() {
        return getInstance().getGSON();
    }

    default Minecraft getClient() {
        return Minecraft.getMinecraft();
    }
}
