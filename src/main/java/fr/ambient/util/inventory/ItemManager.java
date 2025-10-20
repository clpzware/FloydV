package fr.ambient.util.inventory;

import fr.ambient.module.impl.player.InvManager;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import java.util.Set;

public class ItemManager {

    private final InvManager invManager;

    private static final Set<Item> GARBAGE_ITEMS = Set.of(
            Items.snowball, Items.egg, Items.fishing_rod, Items.experience_bottle,
            Items.skull, Items.flint, Items.lava_bucket, Items.flint_and_steel,
            Items.string, Items.gunpowder, Items.bucket,
            Items.enchanted_book, Items.spawn_egg, Items.stick
    );

    private static final Set<String> GARBAGE_ITEM_NAMES = Set.of(
            "anvil", "tnt", "seed", "table", "string", "eye", "mushroom",
            "chest", "pressure_plate", "bow", "arrow", "gunpowder", "bucket",
            "enchanted_book", "cactus", "jukebox", "sand", "stick"
    );


    private static final Set<Integer> NEGATIVE_POTION_EFFECT_IDS = Set.of(
            Potion.moveSlowdown.getId(), Potion.blindness.getId(), Potion.poison.getId(),
            Potion.digSlowdown.getId(), Potion.weakness.getId(), Potion.harm.getId()
    );

    public ItemManager(InvManager manager) {
        this.invManager = manager;
    }

    public void manageItems() {
        getBlockStack();
        getGoldenApples();

        dropUselessItems();

        manageBlockCount();
    }

    private int countBlocks(Container container) {
        int blockCount = 0;
        for (int i = 9; i < 45; i++) {
            ItemStack stack = container.getSlot(i).getStack();
            if (stack != null && shouldChooseBlock(stack)) {
                blockCount += stack.stackSize;
            }
        }
        return blockCount;
    }

    private void manageBlockCount() {
        Container container = invManager.getPlayerContainer();
        int blockCount = countBlocks(container);

        if (blockCount > 256) {
            int excess = blockCount - 256;
            dropExcessBlocks(container, excess);
        }
    }

    private void dropExcessBlocks(Container container, int excess) {
        for (int i = 9; i < 45; i++) {
            ItemStack stack = container.getSlot(i).getStack();
            if (stack != null && shouldChooseBlock(stack)) {
                int stackSize = stack.stackSize;

                if (stackSize <= excess) {
                    invManager.drop(i);
                    excess -= stackSize;
                }

                if (excess <= 0) {
                    break;
                }
            }
        }
    }

    private void getBlockStack() {
        Container container = invManager.getPlayerContainer();

        if (invManager.getBlocks() == null || !shouldChooseBlock(invManager.getBlocks())) {
            int bestSlot = findBestBlockStack(container);

            if (bestSlot != -1) {
                invManager.swapToHotbar(invManager.getBlockSlot(), bestSlot);
            }
        }
    }

    private boolean shouldChooseBlock(ItemStack stack) {
        return stack.getItem() instanceof ItemBlock;
    }

    private int findBestBlockStack(Container container) {
        ItemStack bestStack = null;
        int bestSlot = -1;

        for (int i = 9; i < 45; i++) {
            ItemStack stack = container.getSlot(i).getStack();
            if (stack != null && shouldChooseBlock(stack)) {
                if (bestStack == null || stack.stackSize >= bestStack.stackSize) {
                    bestStack = stack;
                    bestSlot = i;
                }
            }
        }
        return bestSlot;
    }

    private void getGoldenApples() {
        Container container = invManager.getPlayerContainer();

        ItemStack currentGapples = invManager.getGapples();

        int slot = findItemSlot(container, ItemAppleGold.class);

        if (slot != -1) {
            ItemStack foundGapples = container.getSlot(slot).getStack();

            if (currentGapples == null || !(currentGapples.getItem() instanceof ItemAppleGold) || (foundGapples.stackSize > currentGapples.stackSize)) {
                if (currentGapples != null && !(currentGapples.getItem() instanceof ItemAppleGold)) {
                    invManager.shiftClick(invManager.getGappleSlot());
                }
                invManager.swapToHotbar(invManager.getGappleSlot(), slot);
            }
        }
    }

    private int findItemSlot(Container container, Class<? extends Item> itemClass) {
        for (int i = 9; i < 45; i++) {
            ItemStack stack = container.getSlot(i).getStack();
            if (stack != null && itemClass.isInstance(stack.getItem())) {
                return i;
            }
        }
        return -1;
    }

    private void dropUselessItems() {
        Container container = invManager.getPlayerContainer();

        for (int i = 9; i < 45; i++) {
            ItemStack stack = container.getSlot(i).getStack();
            if (stack != null && isUseless(stack, i)) {
                invManager.drop(i);
                break;
            }
        }
    }

    public boolean isGarbage(ItemStack stack) {
        Item item = stack.getItem();
        String itemName = item.getUnlocalizedName().toLowerCase();

        if (GARBAGE_ITEMS.contains(item)) {
            return true;
        }

        for (String garbageName : GARBAGE_ITEM_NAMES) {
            if (itemName.contains(garbageName)) {
                if (garbageName.equals("chest") && itemName.contains("plate")) {
                    continue;
                }
                return true;
            }
        }

        return item instanceof ItemHoe || isNegativePotionEffect(stack);
    }

    private boolean isNegativePotionEffect(ItemStack stack) {
        if (stack.getItem() instanceof ItemPotion potion) {
            for (PotionEffect effect : potion.getEffects(stack)) {
                if (NEGATIVE_POTION_EFFECT_IDS.contains(effect.getPotionID())) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isUseless(ItemStack stack, int slot) {
        if (isGarbage(stack)) {
            return true;
        }

        Item item = stack.getItem();
        if (item instanceof ItemArmor armor) {
            return !invManager.isBetterArmor(stack, getEquippedArmor(armor.armorType), ArmorPiece.fromInt(armor.armorType));
        }

        return isToolUseless(stack, slot);
    }

    private boolean isToolUseless(ItemStack item, int slot) {
        if (item.getItem() instanceof ItemSword && slot != invManager.getSwordSlot()) {
            return !invManager.isBetterWeapon(item, invManager.getWeapon());
        }
        if (item.getItem() instanceof ItemAxe && slot != invManager.getAxeSlot()) {
            return !invManager.isBetterTool(item, invManager.getAxe());
        }
        if (item.getItem() instanceof ItemPickaxe && slot != invManager.getPickaxeSlot()) {
            return !invManager.isBetterTool(item, invManager.getPickaxe());
        }
        if (item.getItem().getUnlocalizedName().toLowerCase().contains("shovel") && slot != invManager.getShovelSlot()) {
            return !invManager.isBetterTool(item, invManager.getShovel());
        }
        return false;
    }

    private ItemStack getEquippedArmor(int armorType) {
        return switch (armorType) {
            case 0 -> invManager.getHelmet();
            case 1 -> invManager.getChestplate();
            case 2 -> invManager.getLeggings();
            case 3 -> invManager.getBoots();
            default -> null;
        };
    }
}