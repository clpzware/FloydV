package fr.ambient.ui.mainmenu.legit;

import fr.ambient.util.input.MouseUtil;
import fr.ambient.util.render.RenderUtil;
import fr.ambient.util.render.font.Fonts;
import lombok.Getter;

import java.awt.*;

@Getter
public class LegitMainMenuButton {
    private int x,y,w,h;
    private String text;
    private Runnable runnable;

    public LegitMainMenuButton(String txt, Runnable runnable){
        this.text = txt;
        this.runnable = runnable;
    }


    public void onDraw(int x, int y, int w, int h,int mouseX, int mouseY){
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        RenderUtil.drawBlur(()->{
            RenderUtil.drawRoundedRect(x,y,w,h,4,new Color(255,255,255, 35));
        });
        Fonts.getOpenSansBold(22).drawString(text, x + 5,y + 5, Color.WHITE.getRGB());
    }
    public void onClick(int mouseX, int mouseY){
        if(MouseUtil.isHovering(mouseX, mouseY, x,y,w,h)){
            runnable.run();
        }
    }

}
