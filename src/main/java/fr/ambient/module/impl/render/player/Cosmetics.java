package fr.ambient.module.impl.render.player;

import fr.ambient.component.impl.misc.CosmeticComponent;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.render.RenderCapeLayerEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.property.Property;
import fr.ambient.property.impl.BooleanProperty;
import fr.ambient.property.impl.CompositeProperty;
import fr.ambient.property.impl.ModeProperty;
import fr.ambient.property.impl.NumberProperty;
import fr.ambient.util.CosmeticData;
import fr.ambient.util.packet.RequestUtil;
import fr.ambient.util.player.ChatUtil;
import fr.ambient.util.render.img.ImageObject;
import fr.ambient.util.wings.RenderWings;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.ResourceLocation;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class Cosmetics extends Module {

    public  BooleanProperty waveyCapes = BooleanProperty.newInstance("Wavey Capes", true);
    public final ModeProperty mode = ModeProperty.newInstance("Cape mode", new String[]{"Ambient","Ambient1","Ambient2","2011","2012","2013","2015","2016","CherryBlossom","BadAiim","Kirby","Kirby2", "None", "Custom"}, "Ambient");

    public final ModeProperty haloMode = ModeProperty.newInstance("Halo Mode", new String[]{"None", "Custom"}, "None");
    public final NumberProperty haloScale = NumberProperty.newInstance("Halo Scale", 0.1f, 1f, 2f, 0.1f);
    public final NumberProperty haloHeight = NumberProperty.newInstance("Halo Height", 0.63f, 0.65f, 1f, 0.01f);

    public final CompositeProperty haloGroup = CompositeProperty.newInstance("Halo", new Property[]{haloMode,haloScale,haloHeight});


    public final ModeProperty wingMode = ModeProperty.newInstance("Wing Mode", new String[]{"None", "Dragon"}, "None");
    public NumberProperty gravity = NumberProperty.newInstance("Gravity", 5f, 25f, 50f, 1F, () -> waveyCapes.getValue()) ;
    public NumberProperty numIterations = NumberProperty.newInstance("Num iterations", 5f, 30f, 50f, 1F, () -> waveyCapes.getValue()) ;
    public NumberProperty maxBend = NumberProperty.newInstance("Max Bend", 5f, 6f, 50f, 1F, () -> waveyCapes.getValue()) ;

    private final HashMap<String, ImageObject> capes = new HashMap<>();
    private final HashMap<String, ImageObject> halos = new HashMap<>();
    private final Map<String, CompletableFuture<ImageObject>> pendingRequests = new ConcurrentHashMap<>();

    private ImageObject notFound = new ImageObject(new ResourceLocation("dogclient/icons/notfound.png"));

    public Cosmetics() {
        super(47,"Adds cosmetics to your player model.", ModuleCategory.RENDER);
        this.setEnabled(false);

        for(String name : mode.getValues()) {
            if(!name.equals("None") && !name.equals("Custom")){
                capes.put(name, new ImageObject(new ResourceLocation("dogclient/cape/" + name + ".png")));
            }
        }

        for(ImageObject o : capes.values()){
            o.loadAsync();
        }
        notFound.loadAsync();


        this.registerProperties(mode,haloGroup, wingMode,waveyCapes,gravity,numIterations,maxBend);
    }

    public String lastCapeMode = "";

    public String getHaloMode(){
        if(this.isEnabled()){
            if(haloMode.is("Custom")){
                return CosmeticComponent.customHaloId;
            }
            return haloMode.getValue();
        }
        return "None";
    }

    public String getCapeMode(){
        if(this.isEnabled()){
            if(mode.is("Custom")){
                return CosmeticComponent.customCapeId;
            }
            return mode.getValue();
        }
        return "None";
    }
    @SubscribeEvent
    private void onCapeRender(RenderCapeLayerEvent event){
        if (!CosmeticComponent.cosData.containsKey(event.getEntityPlayer().getGameProfile().getName())) {
            return;
        }
        if(!CosmeticComponent.cosData.get(event.getEntityPlayer().getGameProfile().getName()).getWing().equals("None")){
            drawWings(event);
        }
    }

    public void drawWings(RenderCapeLayerEvent event){
        if(!event.getEntityPlayer().isInvisible()){
            RenderWings renderWings = new RenderWings();
            renderWings.renderWings(event.getEntityPlayer(), mc.timer.renderPartialTicks);
        }
    }
    public String getCurrMode(){
        if(this.isEnabled() && !wingMode.is("None")){
            return "on";
        }else{
            return "none";
        }
    }


}
