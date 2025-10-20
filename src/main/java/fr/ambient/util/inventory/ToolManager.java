package fr.ambient.util.inventory;

import fr.ambient.module.impl.player.InvManager;
import net.minecraft.inventory.Container;
import net.minecraft.item.*;

public class ToolManager {

    private final InvManager invManager;

    public ToolManager(InvManager invManager) {
        this.invManager = invManager;
    }

    public void manageTools() {
        manageTool(ItemPickaxe.class, invManager.getPickaxe(), "pickaxe", invManager.getPickaxeSlot());
        manageTool(ItemAxe.class, invManager.getAxe(), "axe", invManager.getAxeSlot());
        manageTool(ItemTool.class, invManager.getShovel(), "shovel", invManager.getShovelSlot());
    }

    private void manageTool(Class<? extends Item> toolClass, ItemStack oldTool, String toolNamePart, int toolSlot) {
        Container container = invManager.getPlayerContainer();
        int newToolSlot = -1;
        int dropSlot = -1;

        for (int i = 9; i < 45; i++) {
            ItemStack stack = container.getSlot(i).getStack();
            if (invManager.isValidItemStack(stack) && toolClass.isInstance(stack.getItem())) {

                if (toolNamePart.equals("shovel") && !stack.getItem().getUnlocalizedName().toLowerCase().contains(toolNamePart))
                    continue;

                if (i != toolSlot) {
                    if (invManager.isBetterTool(stack, oldTool)) {
                        newToolSlot = i;
                        oldTool = stack;
                    } else if (dropSlot == -1) {
                        dropSlot = i;
                    }
                }
            }
        }

        if (newToolSlot != -1) {
            invManager.swapToHotbar(toolSlot, newToolSlot);
        } else if (dropSlot != -1) {
            invManager.drop(dropSlot);
        }
    }
}
