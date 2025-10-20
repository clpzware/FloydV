package fr.ambient.util.inventory;

import fr.ambient.module.impl.player.InvManager;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;

public class WeaponManager {

    private final InvManager invManager;

    public WeaponManager(InvManager invManager) {
        this.invManager = invManager;
    }

    public void manageWeapon() {
        Container container = invManager.getPlayerContainer();
        int currentSwordSlot = invManager.getSwordSlot();
        ItemStack currentWeapon = invManager.getWeapon();
        int bestWeaponSlot = -1;

        for (int i = 9; i < 45; i++) {
            ItemStack stack = container.getSlot(i).getStack();
            if (!invManager.isValidItemStack(stack) || !(stack.getItem() instanceof ItemSword)) continue;

            if (i != currentSwordSlot) {
                if (invManager.isBetterWeapon(stack, currentWeapon)) {
                    bestWeaponSlot = i;
                    currentWeapon = stack;
                } else {
                    if (bestWeaponSlot != -1)
                        invManager.drop(i);
                }
            }
        }

        if (bestWeaponSlot != -1) {
            invManager.swapToHotbar(invManager.getSwordSlot(), bestWeaponSlot);
        }
    }
}
