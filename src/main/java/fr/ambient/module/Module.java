package fr.ambient.module;

import fr.ambient.Ambient;
import fr.ambient.module.impl.misc.Freelook;
import fr.ambient.module.impl.render.ClickGui;
import fr.ambient.notification.NotificationManager;
import fr.ambient.notification.impl.NotificationType;
import fr.ambient.property.Property;
import fr.ambient.property.impl.BooleanProperty;
import fr.ambient.property.impl.wrappers.EnumModeProperty;
import fr.ambient.property.impl.wrappers.ItemProperty;
import fr.ambient.property.impl.wrappers.ModuleModeProperty;
import fr.ambient.util.InstanceAccess;
import fr.ambient.util.input.MouseUtil;
import fr.ambient.util.render.animation.Animation;
import fr.ambient.util.render.animation.Easing;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

@Getter
@Setter
public class Module implements InstanceAccess {
    public final Animation animation = new Animation(Easing.EASE_IN_OUT_QUAD, 250);

    private BooleanProperty onlyOnKeyHold = BooleanProperty.newInstance("Only when key hold", false);
    private final int id;
    private final String name;
    private String description = "No description yet.";
    private final ModuleCategory category;
    public List<Property<?>> propertyList = new ArrayList<>();
    public ArrayList<ModuleModeProperty> moduleModeProperties = new ArrayList<>();
    public ArrayList<EnumModeProperty> enumModeProperties = new ArrayList<>();
    public ArrayList<ItemProperty> itemProperties = new ArrayList<>();
    private int keyBind;
    private boolean enabled;
    private boolean draggable = false;
    private int x;
    private int y;
    private float width;
    private float height;
    private boolean isDragged;
    private int oldMouseX;
    private int oldMouseY;
    private Supplier<String> suffix = () -> "";
    private String customName;
    private boolean shown = true;
    private AnchorPoint anchorPoint = AnchorPoint.LEFT;

    public Module(String name, String description){
        this.category = ModuleCategory.MISC;
        this.id = -1;
        this.name = name;
        this.description = description;
        this.customName = this.name;
        propertyList.add(onlyOnKeyHold);
    }


    public Module(int ID,ModuleCategory category) {
        this(ID, category, Integer.MIN_VALUE);
    }

    public Module(int ID,ModuleCategory category, int keyBind) {
        this.category = category;
        this.keyBind = keyBind;
        this.id = ID;
        this.name = Ambient.getInstance().getExternalValueManager().get("names").values.get(this.id);
        this.customName = this.name;
        propertyList.add(onlyOnKeyHold);
    }

    public Module(int ID, String description, ModuleCategory category) {
        this.category = category;
        this.id = ID;
        this.name = Ambient.getInstance().getExternalValueManager().get("names").values.get(this.id);
        this.description = description;
        this.customName = this.name;
        propertyList.add(onlyOnKeyHold);
    }

    public Module(int ID, String description, ModuleCategory category, int keyBind) {
        this.category = category;
        this.id = ID;
        this.keyBind = keyBind;
        this.name = Ambient.getInstance().getExternalValueManager().get("names").values.get(this.id);
        this.description = description;
        this.customName = this.name;
        propertyList.add(onlyOnKeyHold);
    }


    public Property<?> setting(String name) {
        return propertyList.stream().filter(setting -> setting.getLabel().equals(name)).findFirst().orElse(null);
    }

    public void registerProperty(Property<?> property) {
        this.propertyList.add(property);
    }

    public void registerProperties(Property<?>... properties) {
        this.propertyList.addAll(Arrays.asList(properties));
    }

    @SuppressWarnings("unchecked")
    public <T extends Property<?>> T getValueByName(String name) {
        return (T) propertyList.stream()
                .filter(value -> value.getLabel().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    } // one eful day

    public void setEnabled(boolean enabled) {

        if(enabled != this.enabled && !getOnlyOnKeyHold().getValue() && !(this instanceof ClickGui) && !(this instanceof Freelook)){
            NotificationManager.addNotification("Module", this.name + (enabled ? EnumChatFormatting.GREEN + " Enabled" : EnumChatFormatting.RED + " Disabled"), enabled ? NotificationType.INFO : NotificationType.WARNING);
        }

        setEnableNoNotif(enabled);
        animation.restart();

    }

    public void setEnableNoNotif(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            if (enabled) {
                try {
                    for(ModuleModeProperty moduleMode : moduleModeProperties){
                        moduleMode.onEnable();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

                try {
                    this.onEnable();
                } catch (Throwable ignored) {
                    ignored.printStackTrace();
                }
                try {
                    Ambient.getInstance().getEventBus().register(this);
                }catch (Exception e){
                    e.printStackTrace();
                }


            } else {
                try {
                    Ambient.getInstance().getEventBus().unregister(this);
                }catch (Exception e){
                    e.printStackTrace();
                }

                try {
                    this.onDisable();
                } catch (Throwable ignored) {
                    ignored.printStackTrace();
                }
                try {
                    for(ModuleModeProperty moduleMode : moduleModeProperties){
                        moduleMode.onDisable();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    public String getSuffix() {
        return this.suffix.get();
    }

    public void setDragged(boolean isDragged, int i, int j) {
        this.isDragged = isDragged;
        this.oldMouseX = x - i;
        this.oldMouseY = y - j;
    }

    public float getAnchoredX() {
        switch (anchorPoint) {
            default -> {
                return x;
            }
            case RIGHT -> {
                return x - width;
            }
            case CENTER -> {
                return x - width / 2f;
            }
        }
    }

    public void drag() {
        this.x = MouseUtil.getMouseX() + this.oldMouseX;
        this.y = MouseUtil.getMouseY() + this.oldMouseY;

        clamp();
    }

    public boolean isHovered() {
        return MouseUtil.isHovering(MouseUtil.getMouseX(), MouseUtil.getMouseY(), getAnchoredX(), y, width, height);
    }

    private void clamp() {
        ScaledResolution sr = new ScaledResolution(mc);
        int screenWidth = sr.getScaledWidth();
        int screenHeight = sr.getScaledHeight();
        int snapRange = 5;

        this.x = (int) Math.max(5, Math.min(this.x, screenWidth - 5));
        this.y = (int) Math.max(5, Math.min(this.y, screenHeight - 5));

        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        if (Math.abs(this.x - centerX) <= snapRange) {
            this.x = centerX;
        } else if (Math.abs((this.x + this.width / 2f) - centerX) <= snapRange) {
            this.x = (int) (centerX - this.width / 2);
        } else if (Math.abs((this.x + this.width) - centerX) <= snapRange) {
            this.x = (int) (centerX - this.width);
        }

        if (Math.abs(this.y - centerY) <= snapRange) {
            this.y = centerY;
        } else if (Math.abs((this.y + this.height / 2f) - centerY) <= snapRange) {
            this.y = (int) (centerY - this.height / 2);
        } else if (Math.abs((this.y + this.height) - centerY) <= snapRange) {
            this.y = (int) (centerY - this.height);
        }
    }
}
