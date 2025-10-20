package fr.ambient.module.impl.misc;

import fr.ambient.Ambient;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.module.impl.combat.AntiBot;
import fr.ambient.property.impl.BooleanProperty;
import fr.ambient.util.player.ChatUtil;
import fr.ambient.util.player.PlayerUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import java.util.HashMap;
import java.util.Map;

public class BedwarsUtils extends Module {

    private final Map<Item, String> itemMessages = new HashMap<>();
    private final Map<EntityPlayer, Item> lastHeldItems = new HashMap<>();
    private final Map<EntityPlayer, Item> lastArmor = new HashMap<>();

    private final BooleanProperty items = BooleanProperty.newInstance("Item Checks", true);
    private final BooleanProperty armor = BooleanProperty.newInstance("Armor Checks", true);

    public BedwarsUtils() {
        super(88, "A collection of helpful bedwars utilities.", ModuleCategory.MISC);

        registerProperties(items, armor);

        registerItemMessage(Items.diamond_sword, EnumChatFormatting.AQUA, "Diamond Sword");
        registerItemMessage(Items.ender_pearl, EnumChatFormatting.DARK_PURPLE, "Ender Pearl");
        registerItemMessage(Items.egg, EnumChatFormatting.GREEN, "Bridge Egg");
        registerItemMessage(Items.spawn_egg, EnumChatFormatting.WHITE, "Iron Golem");
        registerItemMessage(Items.bow, EnumChatFormatting.GOLD, "Bow");
        registerItemMessage(Items.fire_charge, EnumChatFormatting.RED, "Fireball");
        registerItemMessage(Item.getItemFromBlock(Blocks.tnt), EnumChatFormatting.RED, "T§fN§cT");
        registerArmorMessage(Items.diamond_leggings, EnumChatFormatting.AQUA, "Diamond Armor");
        registerArmorMessage(Items.iron_leggings, EnumChatFormatting.WHITE, "Iron Armor");
        registerArmorMessage(Items.chainmail_leggings, EnumChatFormatting.DARK_GRAY, "Chainmail Armor");
    }

    @SubscribeEvent
    private void onUpdate(PreMotionEvent e) {
        for (EntityPlayer player : mc.theWorld.playerEntities) {
            AntiBot antiBot = Ambient.getInstance().getModuleManager().getModule(AntiBot.class);
            if ((antiBot.isEnabled() && antiBot.isBot(player)) || mc.thePlayer == player || PlayerUtil.isEntityTeamSameAsPlayer(player)) continue;

            ItemStack currentItem = player.getHeldItem();
            if (currentItem != null && items.getValue()) {
                Item heldItem = currentItem.getItem();
                Item lastItem = lastHeldItems.get(player);

                if (heldItem != null && (lastItem == null || lastItem != heldItem)) {
                    String message = itemMessages.get(heldItem);
                    if (message != null) {
                        sendMessage(player, message);
                        lastHeldItems.put(player, heldItem);
                    }
                }
            }

            ItemStack leggingStack = player.inventory.armorItemInSlot(1);

            if (leggingStack != null && items.getValue()) {
                Item leggings = leggingStack.getItem();

                if (leggings != null) {
                    String message = itemMessages.get(leggings);

                    if (message != null && (lastArmor.get(player) == null || lastArmor.get(player) != leggings)) {
                        sendArmorMessage(player, message);
                        lastArmor.put(player, leggings);
                    }
                }
            }
        }
    }

    private void registerItemMessage(Item item, EnumChatFormatting color, String itemName) {
        String article = isVowel(itemName.charAt(0)) ? "an" : "a";
        String coloredName = color + itemName;
        itemMessages.put(item, article + " " + coloredName);
    }

    private void registerArmorMessage(Item item, EnumChatFormatting color, String itemName) {
        String coloredName = color + itemName;
        itemMessages.put(item, coloredName);
    }

    private boolean isVowel(char letter) {
        return "AEIOUaeiou".indexOf(letter) != -1;
    }

    private void sendMessage(EntityPlayer player, String message) {
        double distance = mc.thePlayer.getDistanceToEntity(player);
        String distanceString = String.valueOf((int) distance);
        String formattedMessage = EnumChatFormatting.RESET + player.getDisplayName().getFormattedText() + EnumChatFormatting.GRAY + " has " + message + EnumChatFormatting.GRAY + " (" + EnumChatFormatting.LIGHT_PURPLE + distanceString + "m" + EnumChatFormatting.GRAY + ")";
        ChatUtil.display(formattedMessage);
    }

    private void sendArmorMessage(EntityPlayer player, String message) {
        double distance = mc.thePlayer.getDistanceToEntity(player);
        String distanceString = String.valueOf((int) distance);
        String formattedMessage = EnumChatFormatting.RESET + player.getDisplayName().getFormattedText() + EnumChatFormatting.GRAY + " has purchased " + message + EnumChatFormatting.GRAY + " (" + EnumChatFormatting.LIGHT_PURPLE + distanceString + "m" + EnumChatFormatting.GRAY + ")";
        ChatUtil.display(formattedMessage);
    }
}
