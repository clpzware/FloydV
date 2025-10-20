package fr.ambient.util.inventory;

import fr.ambient.module.impl.player.InvManager;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

public class ArmorManager {

    private final InvManager invManager;

    public ArmorManager(InvManager invManager) {
        this.invManager = invManager;
    }

    public void manageArmor() {
        for (ArmorPiece piece : ArmorPiece.values()) {
            getBestArmor(piece);
        }
    }

    private void getBestArmor(ArmorPiece piece) {
        Container container = invManager.getPlayerContainer();

        ItemStack oldArmor = getArmorPiece(piece);
        int armorSlot = piece.getSlot();

        int newArmorSlot = -1;
        int dropSlot = -1;
        int type = piece.ordinal();

        for (int i = 5; i < 45; i++) {
            ItemStack stack = container.getSlot(i).getStack();
            if (!invManager.isValidItemStack(stack)) continue;

            if (stack.getItem() instanceof ItemArmor armor && armor.armorType == type) {
                boolean isBetter = invManager.isBetterArmor(stack, oldArmor, piece);
                boolean isWorse = invManager.isWorseArmor(stack, oldArmor, piece);

                if (isBetter && i != armorSlot) {
                    newArmorSlot = i;
                    oldArmor = stack;
                } else if (isWorse || i != armorSlot) {
                    dropSlot = i;
                }
            }
        }

        if (newArmorSlot != -1) {
            if (armorSlot != -1 && invManager.isValidItemStack(getArmorPiece(piece))) {
                invManager.drop(armorSlot);
            } else {
                invManager.shiftClick(newArmorSlot);
            }
        } else if (dropSlot != -1) {
            invManager.drop(dropSlot);
        }
    }

    private ItemStack getArmorPiece(ArmorPiece piece) {
        return invManager.getArmorPiece(piece);
    }
}
