package fr.ambient.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.ambient.component.impl.player.HypixelComponent;
import fr.ambient.util.packet.RequestUtil;
import lombok.Getter;
import net.minecraft.util.EnumChatFormatting;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;


@Getter
public class PlayerStats {
    private UUID playerUUID;
    private float fkdr, kdr, wlr, index;
    private int fk, fd, k, d, w, l, stars;
    private String tags = "N";
    private String username;
    private boolean loaded = false;
    private boolean invalid = false;
    public PlayerStats(String username, UUID playerUUID){
        this.playerUUID = playerUUID;
        this.username = username;
    }
    public void reloadStats(){
        if(invalid){
            return;
        }
        this.tags = "";
        Runnable tsk = ()->{


        };




        CompletableFuture.runAsync(tsk)
                .thenRun(this::loadTags)
                .exceptionally(ex -> {
                    System.out.println("Task failed: " + ex.getMessage());
                    return null;
                });

    }

    public void loadTags(){
        this.tags = "";

        if(loaded){
            if(fkdr > 10 && stars < 8){
                this.tags += EnumChatFormatting.DARK_GREEN + "A"; // uhh certified alt
            }
            if(fkdr > 200){
                this.tags += EnumChatFormatting.DARK_RED + "S"; // Worth to snipe
            }
            if(stars > 600 && fkdr < 1){
                this.tags += EnumChatFormatting.LIGHT_PURPLE + "N"; // Nosteria found ;(
            }
            if(fkdr > 0.8 && fkdr < 2 && wlr < 0.1){
                this.tags += EnumChatFormatting.RED + "P"; // Probable Sniper, normal fkdr but low ahh wlr
            }
            if(kdr > 1.75){
                this.tags += EnumChatFormatting.RED + "K"; // high kdr, sus
            }
        }
        if(HypixelComponent.known_cheaters_or_alts.contains(this.playerUUID)){
            this.tags += EnumChatFormatting.GOLD + "C"; // Known Cheater, alt, or just a target. worth to stay
        }
        if(HypixelComponent.beamed_accounts.contains(this.playerUUID)){
            this.tags += EnumChatFormatting.BLUE + "B"; // Beamed Account. prob cheater
        }
        if(HypixelComponent.joinTimes.containsKey(this.playerUUID)){
            int bbs = HypixelComponent.joinTimes.get(this.playerUUID);
            if(bbs > 0){
                switch (bbs){
                    case 1 -> this.tags += EnumChatFormatting.GREEN;
                    case 2 -> this.tags += EnumChatFormatting.DARK_GREEN;
                    case 3 -> this.tags += EnumChatFormatting.YELLOW;
                    case 4 -> this.tags += EnumChatFormatting.GOLD;
                    case 5 -> this.tags += EnumChatFormatting.RED;
                    default -> this.tags += EnumChatFormatting.DARK_RED;
                }

                this.tags += bbs + "";
            }
        }
    }




}
