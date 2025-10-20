package fr.ambient.component.impl.misc;

import fr.ambient.component.Component;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BlackDetectorComponent extends Component {

    public static HashMap<UUID, Float> blackPercentages = new HashMap<>();

    public static CompletableFuture<Float> getBlackAndBrownPercentageAsync(EntityPlayer player) {
        return CompletableFuture.supplyAsync(() -> {
            NetworkPlayerInfo playerInfo = mc.getNetHandler().getPlayerInfo(player.getUniqueID());

            if (playerInfo == null) {
                return 0f;
            }
            if (blackPercentages.containsKey(player.getUniqueID())) {
                return blackPercentages.get(player.getUniqueID());
            }

            try {
                URL skinURL = new URL("https://mc-heads.net/avatar/" + player.getUniqueID());
                BufferedImage skinImage = ImageIO.read(skinURL);
                if (skinImage == null) {
                    return 0f; // Handle case where skinImage is null
                }

                int width = skinImage.getWidth();
                int height = skinImage.getHeight();
                int blackPixelCount = 0;
                int totalPixels = 0;

                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        int color = skinImage.getRGB(x, y);
                        int alpha = (color >> 24) & 0xFF;
                        if (alpha == 0) {
                            continue; // Ignore fully transparent pixels
                        }

                        int red = (color >> 16) & 0xFF;
                        int green = (color >> 8) & 0xFF;
                        int blue = color & 0xFF;

                        //black
                        if (red <= 60 && green <= 60 && blue <= 60) {
                            blackPixelCount++;
                        }else if (red >= 101 && red <= 150 && green >= 51 && green <= 100 && blue <= 50) {
                            blackPixelCount++;
                        }

                        //brown


                        totalPixels++;
                    }
                }

                if (totalPixels == 0) {
                    return 0f;
                }

                float blackPercentage = (blackPixelCount / (float) totalPixels) * 100;
                blackPercentages.put(player.getUniqueID(), blackPercentage);
                return blackPercentage;

            } catch (IOException e) {

            }

            return 0f;
        });
    }

}
