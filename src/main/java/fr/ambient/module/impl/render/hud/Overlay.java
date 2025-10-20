package fr.ambient.module.impl.render.hud;

import fr.ambient.Ambient;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.event.impl.render.Render2DEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.module.impl.misc.NickHider;
import fr.ambient.property.impl.ModeProperty;
import fr.ambient.property.impl.NumberProperty;
import fr.ambient.util.OverlayColors;
import fr.ambient.util.PlayerStats;
import fr.ambient.util.render.RenderUtil;
import fr.ambient.util.render.font.Fonts;
import fr.ambient.util.render.font.TTFFontRenderer;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static net.minecraft.client.gui.GuiPlayerTabOverlay.field_175252_a;

public class Overlay extends Module {
    private final NumberProperty widthProperty;
    private final ModeProperty modeProperty;
    private int backgroundWidth;

    public Overlay() {
        super(57,"Overlay", ModuleCategory.RENDER);
        modeProperty = ModeProperty.newInstance("Mode", new String[] {"Normal", "Ion", "Rinaorc"}, "Normal");
        widthProperty = NumberProperty.newInstance("Width", 500f, 850f, 1200f, 50f);
        this.registerProperties(modeProperty, widthProperty);

        this.setDraggable(true);
        this.setX(30);
        this.setY(30);
        this.setHeight(10);
        this.setWidth(450);
        this.setWidth((float) widthProperty.getValue().intValue());


        this.backgroundWidth = 450;
    }

    public HashMap<UUID, PlayerStats> playerData = new HashMap<>();

    @SubscribeEvent
    private void onUpdate(UpdateEvent event) {
        if(modeProperty.is("Rinaorc")){
            return;
        }
        NetHandlerPlayClient nethandlerplayclient = this.mc.thePlayer.sendQueue;
        List<NetworkPlayerInfo> list = field_175252_a.sortedCopy(nethandlerplayclient.getPlayerInfoMap());
        for (NetworkPlayerInfo networkplayerinfo : list) {
            UUID uuid = networkplayerinfo.getGameProfile().getId();
            String username = networkplayerinfo.getGameProfile().getName();

            if (!playerData.containsKey(uuid)) {
                PlayerStats stats = new PlayerStats(username, uuid);
                stats.reloadStats();
                this.playerData.put(uuid, stats);
            }
        }

        this.playerData.forEach((k, i) -> {
            i.loadTags();
        });

    }

    @SubscribeEvent
    private void onRender2D(Render2DEvent event) {
        switch (modeProperty.getValue()) {
            case "Normal" -> {
                this.setWidth((float) widthProperty.getValue().intValue());
                this.backgroundWidth = (int) (this.getWidth() * 0.5);

                int baseX = this.getX();
                int nameX = baseX + 2;
                int starsX = baseX + (int) (this.getWidth() * 0.1);
                int fkdrX = baseX + (int) (this.getWidth() * 0.14);
                int kdrX = baseX + (int) (this.getWidth() * 0.18);
                int wlrX = baseX + (int) (this.getWidth() * 0.21);
                int fkX = baseX + (int) (this.getWidth() * 0.24);
                int fdX = baseX + (int) (this.getWidth() * 0.27);
                int kX = baseX + (int) (this.getWidth() * 0.30);
                int dX = baseX + (int) (this.getWidth() * 0.33);
                int wX = baseX + (int) (this.getWidth() * 0.36);
                int lX = baseX + (int) (this.getWidth() * 0.39);
                int indexX = baseX + (int) (this.getWidth() * 0.42);
                int tagX = baseX + (int) (this.getWidth() * 0.46);

                NetHandlerPlayClient nethandlerplayclient = this.mc.thePlayer.sendQueue;
                List<NetworkPlayerInfo> list = field_175252_a.sortedCopy(nethandlerplayclient.getPlayerInfoMap());

                TTFFontRenderer fr = Fonts.getRobotoMedium(18);
                RenderUtil.drawRect(this.getX(), this.getY(), backgroundWidth, 13, new Color(0, 0, 0, 150));
                RenderUtil.drawRect(this.getX(), this.getY(), backgroundWidth, this.getHeight(), new Color(0, 0, 0, 180));
                RenderUtil.drawRect(this.getX() - 1, this.getY(), 1, this.getHeight(), Ambient.getInstance().getHud().getCurrentTheme().getColor1());
                RenderUtil.drawRect(this.getX() + backgroundWidth - 1, this.getY(), 1, this.getHeight(), Ambient.getInstance().getHud().getCurrentTheme().getColor1());
                RenderUtil.drawRect(this.getX(), this.getY(), backgroundWidth, 1, Ambient.getInstance().getHud().getCurrentTheme().getColor1());
                RenderUtil.drawRect(this.getX(), this.getY() + this.getHeight(), backgroundWidth, 1, Ambient.getInstance().getHud().getCurrentTheme().getColor1());
                int yOffset = 4;
                var MainColor = Ambient.getInstance().getHud().getCurrentTheme().color1.getRGB();
                fr.drawString("Name", nameX, this.getY() + yOffset, MainColor);
                fr.drawString("Stars", starsX, this.getY() + yOffset, MainColor);
                fr.drawString("FKDR", fkdrX, this.getY() + yOffset, MainColor);
                fr.drawString("KDR", kdrX, this.getY() + yOffset, MainColor);
                fr.drawString("WLR", wlrX, this.getY() + yOffset, MainColor);
                fr.drawString("FK", fkX, this.getY() + yOffset, MainColor);
                fr.drawString("FD", fdX, this.getY() + yOffset, MainColor);
                fr.drawString("K", kX, this.getY() + yOffset, MainColor);
                fr.drawString("D", dX, this.getY() + yOffset, MainColor);
                fr.drawString("W", wX, this.getY() + yOffset, MainColor);
                fr.drawString("L", lX, this.getY() + yOffset, MainColor);
                fr.drawString("Index", indexX, this.getY() + yOffset, MainColor);
                fr.drawString("Tag", tagX, this.getY() + yOffset, MainColor);

                int off_y = this.getY() + 15;
                for (NetworkPlayerInfo networkplayerinfo : list) {
                    if (this.playerData.containsKey(networkplayerinfo.getGameProfile().getId())) {
                        PlayerStats stats = this.playerData.get(networkplayerinfo.getGameProfile().getId());
                        DecimalFormat format = new DecimalFormat("#.##");
                        if (Objects.equals(stats.getUsername(), mc.thePlayer.getName())) {
                            if (Ambient.getInstance().getModuleManager().getModule(NickHider.class).isEnabled()) {
                                fr.drawString(Ambient.getInstance().getUsername(), nameX, off_y, Color.orange.getRGB());
                            } else {
                                fr.drawString(stats.getUsername(), nameX, off_y, Color.orange.getRGB());
                            }
                        } else {
                            fr.drawString(stats.getUsername(), nameX, off_y, Color.WHITE.getRGB());
                        }
                        if (stats.getStars() != 0)
                            fr.drawString(stats.getStars() + "", starsX, off_y, Color.gray.getRGB());
                        if (stats.getFkdr() != 0)
                            fr.drawString(format.format(stats.getFkdr()), fkdrX, off_y, OverlayColors.getColorFKDR(stats.getFkdr()).getRGB());
                        if (stats.getKdr() != 0)
                            fr.drawString(format.format(stats.getKdr()), kdrX, off_y, OverlayColors.getColorKDR(stats.getKdr()).getRGB());
                        if (stats.getWlr() != 0)
                            fr.drawString(format.format(stats.getWlr()), wlrX, off_y, OverlayColors.getColorWLR(stats.getWlr()).getRGB());
                        if (stats.getFk() != 0)
                            fr.drawString(format.format(stats.getFk()), fkX, off_y, OverlayColors.getKills(stats.getFk()).getRGB());
                        if (stats.getFd() != 0)
                            fr.drawString(format.format(stats.getFd()), fdX, off_y, OverlayColors.getKills(stats.getFd()).getRGB());
                        if (stats.getK() != 0)
                            fr.drawString(format.format(stats.getK()), kX, off_y, OverlayColors.getKills(stats.getK()).getRGB());
                        if (stats.getD() != 0)
                            fr.drawString(format.format(stats.getD()), dX, off_y, OverlayColors.getKills(stats.getD()).getRGB());
                        if (stats.getW() != 0)
                            fr.drawString(format.format(stats.getW()), wX, off_y, OverlayColors.getKills(stats.getW()).getRGB());
                        if (stats.getL() != 0)
                            fr.drawString(format.format(stats.getL()), lX, off_y, OverlayColors.getWins(stats.getL()).getRGB());
                        if (stats.getIndex() != 0)
                            fr.drawString(format.format(stats.getIndex()), indexX, off_y, OverlayColors.getWins(stats.getL()).getRGB());
                        fr.drawString(stats.getTags(), tagX, off_y, Color.WHITE.getRGB());

                        off_y += 11;
                    }
                }

                this.setHeight(off_y - (this.getY()));
            }
            case "Ion" -> {
                this.setWidth((float) widthProperty.getValue().intValue());
                this.backgroundWidth = (int) (this.getWidth() * 0.32);

                int baseX = this.getX();
                int nameX = baseX + 2;
                int starsX = baseX + (int) (this.getWidth() * 0.12);
                int fkdrX = baseX + (int) (this.getWidth() * 0.16);
                int kdrX = baseX + (int) (this.getWidth() * 0.225);
                int wlrX = baseX + (int) (this.getWidth() * 0.26);

                NetHandlerPlayClient nethandlerplayclient = this.mc.thePlayer.sendQueue;
                List<NetworkPlayerInfo> list = field_175252_a.sortedCopy(nethandlerplayclient.getPlayerInfoMap());

                TTFFontRenderer fr = Fonts.getRobotoMedium(18);
                RenderUtil.drawRect(this.getX(), this.getY(), backgroundWidth, 13, new Color(0, 0, 0, 150));
                RenderUtil.drawRect(this.getX(), this.getY(), backgroundWidth, this.getHeight(), new Color(0, 0, 0, 180));
                int yOffset = 3;
                var MainColor = Ambient.getInstance().getHud().getCurrentTheme().color1.getRGB();
                fr.drawString("Name", nameX, this.getY() + yOffset, MainColor);
                fr.drawString("Stars", starsX, this.getY() + yOffset, MainColor);
                fr.drawString("Final KDR", fkdrX, this.getY() + yOffset, MainColor);
                fr.drawString("KDR", kdrX, this.getY() + yOffset, MainColor);
                fr.drawString("W/L Ratio", wlrX, this.getY() + yOffset, MainColor);

                int off_y = this.getY() + 15;
                for (NetworkPlayerInfo networkplayerinfo : list) {
                    if (this.playerData.containsKey(networkplayerinfo.getGameProfile().getId())) {
                        PlayerStats stats = this.playerData.get(networkplayerinfo.getGameProfile().getId());
                        DecimalFormat format = new DecimalFormat("#.##");
                        if (Objects.equals(stats.getUsername(), mc.thePlayer.getName())) {
                            if (Ambient.getInstance().getModuleManager().getModule(NickHider.class).isEnabled()) {
                                fr.drawString(Ambient.getInstance().getUsername(), nameX, off_y, Color.orange.getRGB());
                            } else {
                                fr.drawString(stats.getUsername(), nameX, off_y, Color.orange.getRGB());
                            }
                        } else {
                            fr.drawString(stats.getUsername(), nameX, off_y, Color.WHITE.getRGB());
                        }
                        if (stats.getStars() != 0)
                            fr.drawString(stats.getStars() + "", starsX, off_y, Color.gray.getRGB());
                        if (stats.getFkdr() != 0)
                            fr.drawString(format.format(stats.getFkdr()), fkdrX, off_y, OverlayColors.getColorFKDR(stats.getFkdr()).getRGB());
                        if (stats.getKdr() != 0)
                            fr.drawString(format.format(stats.getKdr()), kdrX, off_y, OverlayColors.getColorKDR(stats.getKdr()).getRGB());
                        if (stats.getWlr() != 0)
                            fr.drawString(format.format(stats.getWlr()), wlrX, off_y, OverlayColors.getColorWLR(stats.getWlr()).getRGB());

                        off_y += 11;
                    }
                }

                this.setHeight(off_y - (this.getY()));
            }
            case "Rinaorc" -> {
                int off_y = this.getY();
                TTFFontRenderer fr = Fonts.getRobotoMedium(18);
                RenderUtil.drawRoundedRect(this.getX(), this.getY(), this.getHeight(), this.getWidth(), 5, new Color(40,40,40,40));
                for(EntityPlayer player : mc.theWorld.playerEntities){
                    if(player.getName().isEmpty() ||player.getName().isBlank()){
                        continue;
                    }
                    fr.drawString(player.getName(), this.getX(), off_y, Color.WHITE.getRGB());
                    off_y += 18;
                }
                this.setHeight(off_y - (this.getY()));
                this.setWidth(200);
            }
        }

    }
}
