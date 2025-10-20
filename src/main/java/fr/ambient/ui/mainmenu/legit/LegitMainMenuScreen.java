package fr.ambient.ui.mainmenu.legit;

import fr.ambient.util.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

public class LegitMainMenuScreen  extends GuiScreen {
    public LegitMainMenuScreen(){
        buttons.add(new LegitMainMenuButton("Singleplayer", ()->System.out.println("SINGLEPLAYEH")));
        buttons.add(new LegitMainMenuButton("Multiplayer", ()->System.out.println("MULTIPLAYEH")));
        buttons.add(new LegitMainMenuButton("AltManager", ()->System.out.println("ALTMANAGEH")));
        buttons.add(new LegitMainMenuButton("Exit", ()->System.out.println("HOW DARE YOU LEAVE")));
    }


    private float offsetX = 0;
    private float offsetY = 0;

    private ArrayList<LegitMainMenuButton> buttons = new ArrayList<>();


    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        RenderUtil.drawRect(0,0,sr.getScaledWidth(), sr.getScaledHeight(), Color.BLACK);
        int centerX = sr.getScaledWidth() / 2;
        int centerY = sr.getScaledHeight() / 2;

        offsetX = ((centerX / 2f) - (mouseX / 2f)) / 25;
        offsetY = ((centerY / 2f) - (mouseY / 2f)) / 25;

        offsetX = MathHelper.clamp_float(offsetX, -45,45);
        offsetY = MathHelper.clamp_float(offsetY, -45,45);

        int amount = 5;

        RenderUtil.drawImage(new ResourceLocation("dogclient/background/wallpaper2.png"), 0 + offsetX - 45, 0 + offsetY - 45, width + 90, height + 90);
        RenderUtil.drawBlur(()->{
            RenderUtil.drawImage(new ResourceLocation("dogclient/background/wallpaper2.png"), 0 + offsetX - 45, 0 + offsetY - 45, width + 90, height + 90);
        }, amount);

        int posX = 35;
        int posY = sr.getScaledHeight() / 2 - ((27 * buttons.size()) / 2);

        for(LegitMainMenuButton button : buttons){
            button.onDraw(posX,posY,150,20,mouseX, mouseY);
            posY += 27;
        }


    }

    private void drawBlur(final int mouseX, final int mouseY){

    }
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        buttons.forEach(b->{
            b.onClick(mouseX, mouseY);
        });
    }
    public void mouseReleased(int mouseX, int mouseY, int state) {

    }
    public void keyTyped(char typedChar, int keyCode) throws IOException {

    }
}


