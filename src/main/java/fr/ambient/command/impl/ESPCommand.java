package fr.ambient.command.impl;

import fr.ambient.command.Command;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import org.lwjglx.Sys;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ESPCommand extends Command {

    // THIS IS PURELY A JOKE COMMAND

    public static int timer = -1;

    public ESPCommand(){
        super("esp");
    }


    @Override
    public void execute(String[] args, String message) {

        timer = 30;

        /*ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        Runnable task = () -> {

            scheduler.shutdown();
        };

        scheduler.schedule(task, 5, TimeUnit.SECONDS);*/
    }
}
