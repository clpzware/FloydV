package femcum.modernfloyd.clients.module.impl.other;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.input.ClickEvent;
import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.util.sound.SoundUtil;
import femcum.modernfloyd.clients.value.impl.ModeValue;
import femcum.modernfloyd.clients.value.impl.NumberValue;
import femcum.modernfloyd.clients.value.impl.SubMode;
import org.apache.commons.lang3.RandomUtils;

@ModuleInfo(aliases = {"module.other.clicksounds.name"}, description = "module.other.clicksounds.description", category = Category.RENDER)
public final class ClickSounds extends Module {

    private final ModeValue sound = new ModeValue("Sound", this)
            .add(new SubMode("Standard"))
            .add(new SubMode("Double"))
            .add(new SubMode("Alan"))
            .setDefault("Standard");

    private final NumberValue volume = new NumberValue("Volume", this, 0.5, 0.1, 2, 0.1);
    private final NumberValue variation = new NumberValue("Variation", this, 5, 0, 100, 1);

    @EventLink
    public final Listener<ClickEvent> onClick = event -> {
        String soundName = "floyd.click.standard";

        switch (sound.getValue().getName()) {
            case "Double": {
                soundName = "floyd.click.double";
                break;
            }

            case "Alan": {
                soundName = "floyd.click.alan";
                break;
            }
        }

        SoundUtil.playSound(soundName, volume.getValue().floatValue(), RandomUtils.nextFloat(1.0F, 1 + variation.getValue().floatValue() / 100f));
    };
}