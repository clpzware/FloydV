package fr.ambient.module.impl.render.hud;

import fr.ambient.Ambient;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketReceiveEvent;
import fr.ambient.event.impl.render.Render2DEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.property.impl.ModeProperty;
import fr.ambient.util.math.TimeUtil;
import fr.ambient.util.render.RenderUtil;
import fr.ambient.util.render.font.Fonts;
import fr.ambient.util.render.font.TTFFontRenderer;
import lombok.AllArgsConstructor;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S45PacketTitle;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class SessionStats extends Module {
    private final TimeUtil stopwatch = new TimeUtil();
    private final TimeUtil killCountdown = new TimeUtil();
    private final ModeProperty mode = ModeProperty.newInstance("Mode", new String[]{"Modern"}, "Modern");
    private final String[] killString = {"was &+'s final", "by &+"};
    private final Session session = new Session(0, 0, 0, System.currentTimeMillis());
    private String time = "0 seconds";
    private long seconds;

    public SessionStats() {
        super(60,"Tracks and displays stats for your current play session.", ModuleCategory.RENDER);
        this.setDraggable(true);
        this.registerProperties(mode);
        this.setX(5);
        this.setY(27);
        this.setWidth(100);
        this.setHeight(50);
    }

    @SubscribeEvent
    public void onRender2D(Render2DEvent e) {
        if (stopwatch.finished(1000)) {
            long elapsed = System.currentTimeMillis() - this.session.startTime;

            seconds = (elapsed / 1000);
            long minutes = (elapsed / (1000 * 60)) % 60;
            long hours = (elapsed / (1000 * 60 * 60)) % 24;
            time = String.format("%02d:%02d:%02d", hours, minutes, seconds % 60);

            stopwatch.reset();
        }

        String[] toDisplay = new String[] {
                String.format("K/D: %s", (float) Math.round(((float) this.session.kills / (this.session.death + 1))*10)/10),
                String.format("Wins: %s", this.session.wins)
        };

        switch (mode.getValue()){
            case "Modern" -> {
                TTFFontRenderer osb12 = Fonts.getNunito(15);
                TTFFontRenderer osr17 = Fonts.getOpenSansRegular(17);
                TTFFontRenderer osb17 = Fonts.getNunito(15);

                TTFFontRenderer nunito20 = Fonts.getNunito(20);

                float maxWidth = osb17.getWidth("Session Info") + 65;

                for (String text : toDisplay) {
                    float width = osr17.getWidth(text) + 85;
                    if(width > maxWidth)
                        maxWidth = width;
                }

                float maxHeight = 34;
                for (String ignored : toDisplay) {
                    float height = osr17.getHeight("I");
                    maxHeight += height;
                }

                setWidth(maxWidth);
                setHeight(maxHeight);

                RenderUtil.drawRoundedRect(getX(), getY(), getWidth(), getHeight(), 7f, new Color(0x90121214, true));

                RenderUtil.drawCircleLoading(getX()-1,  getY() - 1, 55, 1, new Color(0x65000000, true));
                RenderUtil.drawCircleLoading(getX()-1,  getY() - 1, 55, (seconds % 3600) / 3600f, Ambient.getInstance().getHud().getCurrentTheme().color2, Ambient.getInstance().getHud().getCurrentTheme().color1);

                String[] parts = time.split(":");
                int hours = Integer.parseInt(parts[0]);
                int minutes = Integer.parseInt(parts[1]);

                if (hours != 0) {
                    osb12.drawCenteredString(hours + "hr", getX() + 27, getY() + getHeight() / 2f - 8, -1);
                    osb12.drawCenteredString(minutes + "min", getX() + 27, getY() + getHeight() / 2f + 2, -1);
                } else {
                    nunito20.drawCenteredString(String.valueOf(minutes), getX() + 26.5f, getY() + getHeight() / 2f - 9, -1);
                    osb17.drawCenteredString("min", getX() + 26.5f, getY() + getHeight() / 2f + 2f, -1);
                }

                String[] statLabels = new String[] {
                        "Wins:",
                        "Kills:",
                        "Deaths:"
                };

                String[] statValues = new String[] {
                        String.valueOf(this.session.wins),
                        String.valueOf(this.session.kills),
                        String.valueOf(this.session.death)
                };

                float boxY = getY() + 7;
                for (int i = 0; i < statLabels.length; i++) {
                    String label = statLabels[i];
                    String value = statValues[i];

                    osb17.drawString(label, getX() + getWidth() / 2 + 9, boxY + 2, -1);
                    osb17.drawString(value, getX() + getWidth() - 8 - osb17.getWidth(value), boxY + 2, Ambient.getInstance().getHud().getCurrentTheme().color2.getRGB());

                    RenderUtil.drawRoundedRect(getX() + getWidth() / 2 - 5, boxY, 11, 11, 3, new Color(0x65000000, true));
                    inGameImages.get(statLabels[i].toLowerCase().trim().replace(":", "")).drawImg(getX() + getWidth() / 2 - 3, boxY + 2, 7, 7, new Color(0x60FFFFFF, true));

                    boxY += 14;
                }
            }
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent e) {
        if (e.getPacket() instanceof S45PacketTitle s45) {
            if (s45.getMessage() == null)
                return;

            if (s45.getMessage().getUnformattedText().contains("VICTORY!")) {
                ++this.session.wins;
            }

            if (s45.getMessage().getUnformattedText().contains("DIED") && killCountdown.finished(10000)) {
                ++this.session.death;
                killCountdown.reset();
            }
        }

        if(e.getPacket() instanceof S02PacketChat s02){
            final List<String> modifiedKillStrings = new ArrayList<>();

            for(String element : this.killString){
                modifiedKillStrings.add(element.replace("&+", mc.thePlayer.getName()));
            }

            if(s02.getChatComponent().getUnformattedText().contains("Bed"))
                return;

            if (modifiedKillStrings.stream().anyMatch(s -> s02.getChatComponent().getUnformattedText().contains(s))) {
                ++this.session.kills;
            }
        }
    }

    @AllArgsConstructor
    private static class Session {
        int kills, death, wins;
        long startTime;
    }

    public void resetSession() {
        session.death = 0;
        session.kills = 0;
        session.wins = 0;
        session.startTime = System.currentTimeMillis();
    }
}
