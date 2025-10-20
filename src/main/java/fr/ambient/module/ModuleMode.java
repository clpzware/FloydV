package fr.ambient.module;


import lombok.Getter;
import net.minecraft.client.Minecraft;

public class ModuleMode{

    public Minecraft mc = Minecraft.getMinecraft();

    @Getter
    private String modeName;
    @Getter
    private Module superModule;

    public ModuleMode(String modeName, Module module){
        this.modeName = modeName;
        this.superModule = module;
    }
    public void onEnable(){

    }
    public void onDisable(){

    }



}
