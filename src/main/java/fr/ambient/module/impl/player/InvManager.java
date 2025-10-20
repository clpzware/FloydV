package fr.ambient.module.impl.player;

import fr.ambient.Ambient;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketReceiveEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.module.impl.combat.KillAura;
import fr.ambient.module.impl.movement.InvMove;
import fr.ambient.property.impl.BooleanProperty;
import fr.ambient.property.impl.NumberProperty;
import fr.ambient.util.inventory.*;
import fr.ambient.util.player.ChatUtil;
import fr.ambient.util.player.MoveUtil;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.Container;
import net.minecraft.item.*;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;

import java.util.Map;


public class InvManager extends Module {

    private final ArmorManager armorManager;
    private final WeaponManager weaponManager;
    private final ToolManager toolManager;
    private final ItemManager itemManager;
    public boolean stop2 = false;
    public boolean stop = false;

    @Getter @Setter
    private ItemStack helmet, chestplate, leggings, boots;
    @Getter @Setter
    private ItemStack weapon, pickaxe, axe, shovel, blocks, gapples;
    private int delay = 0;
    private int lastSlot = -1;

    private final BooleanProperty smartDelay = BooleanProperty.newInstance("Smart Delay", true);
    private final BooleanProperty desyncfix = BooleanProperty.newInstance("Inventory Desync Fix", true);
    private final BooleanProperty notwhilemoving = BooleanProperty.newInstance("Not While Moving", false);
    private final NumberProperty delayMultiplier = NumberProperty.newInstance("Delay Multiplier", 0.5f, 1f, 10F, 0.5F, smartDelay::getValue);
    private final NumberProperty maxdelay = NumberProperty.newInstance("Max Delay", 0.5f, 1f, 20F, 0.5F, smartDelay::getValue);
    private final NumberProperty delaySetting = NumberProperty.newInstance("Delay", 0f, 1f, 20F, 1F, () -> !smartDelay.getValue());

    private final BooleanProperty customSlots = BooleanProperty.newInstance("Custom Slots", false);
    private final NumberProperty sword = NumberProperty.newInstance("Sword Slot", 1f, 1f, 9f, 1f, customSlots::getValue);
    private final NumberProperty pickaxeSlot = NumberProperty.newInstance("Pickaxe Slot", 1f, 2f, 9f, 1f, customSlots::getValue);
    private final NumberProperty axeSlot = NumberProperty.newInstance("Axe Slot", 1f, 3f, 9f, 1f, customSlots::getValue);
    private final NumberProperty shovelSlot = NumberProperty.newInstance("Shovel Slot", 1f, 4f, 9f, 1f, customSlots::getValue);
    private final NumberProperty gappleSlot = NumberProperty.newInstance("Gapple Slot", 1f, 8f, 9f, 1f, customSlots::getValue);
    private final NumberProperty blockSlot = NumberProperty.newInstance("Block Slot", 1f, 9f, 9f, 1f, customSlots::getValue);

    private BooleanProperty checkForContainer = BooleanProperty.newInstance("Check for Container", false);

    public static EntityLivingBase target = null;

    public final BooleanProperty nogui = BooleanProperty.newInstance("Spoof", false);

    public InvManager() {
        super(33, "Automatically organizes and manages your inventory.", ModuleCategory.PLAYER);
        armorManager = new ArmorManager(this);
        weaponManager = new WeaponManager(this);
        toolManager = new ToolManager(this);
        itemManager = new ItemManager(this);
        this.registerProperties(smartDelay,delayMultiplier,maxdelay,delaySetting, customSlots, sword, pickaxeSlot, axeSlot, shovelSlot, gappleSlot, blockSlot, nogui,desyncfix,checkForContainer,notwhilemoving);
    }

    @Override
    protected void onEnable() {
        delay = 0;
        lastSlot = -1;
        super.onEnable();
    }







    @SubscribeEvent
    private void onPacketReceiveEvent(PacketReceiveEvent event) {
        if (event.getPacket() instanceof S32PacketConfirmTransaction wrapper) {
            final Container inventory = mc.thePlayer.inventoryContainer;

            if (wrapper.getWindowId() == inventory.windowId) {
                short action = wrapper.getActionNumber();

                if (action > 0 && action < inventory.transactionID) {
                    inventory.transactionID = (short) (action + 1);
                }
            }
        }
    }



    @SubscribeEvent
    private void onPlayerTick(PreMotionEvent event) {
        Container container = mc.thePlayer.inventoryContainer;


        if(checkForContainer.getValue() && mc.thePlayer.openContainer.inventorySlots.size() > 45){
            return;
        }


        helmet = container.getSlot(5).getStack();
        chestplate = container.getSlot(6).getStack();
        leggings = container.getSlot(7).getStack();
        boots = container.getSlot(8).getStack();

        weapon = container.getSlot(sword.getValue().intValue() + 35).getStack();
        pickaxe = container.getSlot(pickaxeSlot.getValue().intValue() + 35).getStack();
        axe = container.getSlot(axeSlot.getValue().intValue() + 35).getStack();
        shovel = container.getSlot(shovelSlot.getValue().intValue() + 35).getStack();

        gapples = container.getSlot(gappleSlot.getValue().intValue() + 35).getStack();
        blocks = container.getSlot(blockSlot.getValue().intValue() + 35).getStack();

        if (nogui.getValue() && (mc.thePlayer.isUsingItem() || Ambient.getInstance().getModuleManager().getModule(Scaffold.class).isEnabled()))
            return;
        if (notwhilemoving.getValue() && MoveUtil.moving()) return;
        if (stop) return;
        if (stop2) return;

        if ((mc.currentScreen instanceof GuiInventory || (nogui.getValue() && mc.currentScreen == null)) && KillAura.target == null && !lobby()) {
            delay++;
            armorManager.manageArmor();
            weaponManager.manageWeapon();
            toolManager.manageTools();
            itemManager.manageItems();
        } else {
            delay = 0;
            lastSlot = -1;
        }
    }

    private boolean shouldExecute(int slotId) {
        if (smartDelay.getValue())
            return (lastSlot == -1 && delay > 2) || (delay > (Math.abs(slotId - lastSlot)) * delayMultiplier.getValue() || delay > maxdelay.getValue());
        else
            return delay >= delaySetting.getValue();
    }

    public void swapToHotbar(int hotbarSlot, int slotId) {
        if (shouldExecute(slotId)) {
            mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slotId, hotbarSlot - 36, 2, mc.thePlayer);
            delay = 0;
            lastSlot = slotId;
        }
    }

    public void shiftClick(int slotId) {
        if (shouldExecute(slotId)) {
            mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slotId, 1, 1, mc.thePlayer);
            lastSlot = slotId;
            delay = 0;
        }
    }

    public void drop(int slotId) {
        if (shouldExecute(slotId)) {
            mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slotId, 1, 4, mc.thePlayer);
            lastSlot = slotId;
            delay = 0;
        }
    }




public Container getPlayerContainer() {
        return mc.thePlayer.inventoryContainer;
    }

    public boolean isValidItemStack(ItemStack stack) {
        return stack != null;
    }

    public int getSwordSlot() {
        return sword.getValue().intValue() + 35;
    }

    public int getPickaxeSlot() {
        return pickaxeSlot.getValue().intValue() + 35;
    }

    public int getAxeSlot() {
        return axeSlot.getValue().intValue() + 35;
    }

    public int getShovelSlot() {
        return shovelSlot.getValue().intValue() + 35;
    }

    public int getBlockSlot() {
        return blockSlot.getValue().intValue() + 35;
    }

    public int getGappleSlot() {
        return gappleSlot.getValue().intValue() + 35;
    }

    public ItemStack getArmorPiece(ArmorPiece piece) {
        return switch (piece) {
            case HELMET -> this.helmet;
            case CHESTPLATE -> this.chestplate;
            case LEGGINGS -> this.leggings;
            case BOOTS -> this.boots;
        };
    }

    public boolean isBetterArmor(ItemStack newArmor, ItemStack oldArmor, ArmorPiece piece) {
        if(oldArmor == null) return true;

        Item oldItem = oldArmor.getItem();

        if(oldItem instanceof ItemArmor oldItemArmor) {
            if(oldItemArmor.armorType == piece.ordinal()) {
                return getArmorProtection(newArmor) > getArmorProtection(oldArmor);
            } else {
                return true;
            }
        }

        return false;
    }

    public boolean isWorseArmor(ItemStack newArmor, ItemStack oldArmor, ArmorPiece piece) {
        if(oldArmor == null) return false;

        Item oldItem = oldArmor.getItem();

        if(oldItem instanceof ItemArmor oldItemArmor) {
            if(oldItemArmor.armorType == piece.ordinal()) {
                return getArmorProtection(newArmor) < getArmorProtection(oldArmor);
            } else {
                return false;
            }
        }

        return true;
    }

    private float getArmorProtection(ItemStack stack) {
        if(stack == null) return 0F;

        Item item = stack.getItem();

        float baseProtection = 0F;

        if (item instanceof ItemArmor armor) {
            baseProtection += armor.damageReduceAmount;
        }

        float enchantsProtection = EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, stack) * 1.25F
                + EnchantmentHelper.getEnchantmentLevel(Enchantment.blastProtection.effectId, stack) * 0.15F
                + EnchantmentHelper.getEnchantmentLevel(Enchantment.fireProtection.effectId, stack) * 0.15F
                + EnchantmentHelper.getEnchantmentLevel(Enchantment.projectileProtection.effectId, stack) * 0.15F
                + EnchantmentHelper.getEnchantmentLevel(Enchantment.thorns.effectId, stack) * 0.1F
                + EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack) * 0.1F;

        return baseProtection + enchantsProtection;
    }

    public boolean isBetterWeapon(ItemStack newWeapon, ItemStack currentWeapon) {
        if (currentWeapon == null) return true;
        return getAttackDamage(newWeapon) > getAttackDamage(currentWeapon);
    }

    private float getAttackDamage(ItemStack stack) {
        if (stack == null) return 0F;

        Item item = stack.getItem();
        float baseDamage = (item instanceof ItemSword swordItem) ? swordItem.getAttackDamage()
                : (item instanceof ItemTool toolItem) ? toolItem.getAttackDamage() : 0F;

        Map<Enchantment, Float> enchantmentModifiers = Map.of(
                Enchantment.sharpness, 1.25F,
                Enchantment.fireAspect, 0.3F,
                Enchantment.knockback, 0.15F,
                Enchantment.unbreaking, 0.1F
        );

        float enchantDamage = 0F;
        for (Map.Entry<Enchantment, Float> entry : enchantmentModifiers.entrySet()) {
            int level = EnchantmentHelper.getEnchantmentLevel(entry.getKey().effectId, stack);
            enchantDamage += level * entry.getValue();
        }

        return baseDamage + enchantDamage;
    }

    public boolean isBetterTool(ItemStack newTool, ItemStack oldTool) {
        Item item = newTool.getItem();

        if(item instanceof ItemTool) {
            if(oldTool != null) {
                return getToolEfficiency(newTool) > getToolEfficiency(oldTool);
            } else {
                return true;
            }
        }

        return false;
    }


    public boolean lobby() {
        Item compass = Item.getByNameOrId("minecraft:compass");
        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = mc.thePlayer.inventory.getStackInSlot(i);
            if (itemStack != null) {
                if (itemStack.getItem() == compass) return true;
                if (itemStack.getItem() instanceof ItemBow && itemStack.hasDisplayName() && itemStack.getDisplayName().contains("Kit Selector")) return true;
            }
        }
        return false;
    }





private float getToolEfficiency(ItemStack stack) {
        if (stack == null) return 0F;

        Item item = stack.getItem();
        float baseEfficiency = 0F;

        if (item instanceof ItemTool tool) {
            baseEfficiency = switch (tool.getToolMaterial()) {
                case WOOD -> 1F;
                case GOLD -> 1.9F;
                case STONE -> 2F;
                case IRON -> 3F;
                case EMERALD -> 4F;
            };
        }

        float enchantEfficiency = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, stack) * 1.25F +
                EnchantmentHelper.getEnchantmentLevel(Enchantment.fortune.effectId, stack) * 1.25F +
                EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack) * 0.3F;

        return baseEfficiency + enchantEfficiency;
    }
}