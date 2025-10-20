package fr.ambient.module.impl.player;

import fr.ambient.Ambient;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.property.impl.BooleanProperty;
import fr.ambient.property.impl.NumberProperty;
import fr.ambient.util.inventory.ItemManager;
import fr.ambient.util.math.TimeUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;

public class ChestStealer extends Module {
    public ChestStealer() {
        super(30,"Automatically takes all items from a chest.", ModuleCategory.PLAYER);
        this.registerProperties(smartDelay, multiplier, preTakeDelay, takeDelay,checkTitle);
    }

    private int lastSlot = -1;

    private ItemManager itemManager;

    private final BooleanProperty smartDelay = BooleanProperty.newInstance("Smart Delay", true);
    private final BooleanProperty checkTitle = BooleanProperty.newInstance("Check Title", false);
    private final NumberProperty multiplier = NumberProperty.newInstance("Delay Multiplier", 25f, 50f, 500f, 5f, smartDelay::getValue);
    private final NumberProperty preTakeDelay = NumberProperty.newInstance("Pre Steal Delay", 0f, 0f, 1000f, 50f);
    private final NumberProperty takeDelay = NumberProperty.newInstance("Take Delay", 0f, 0f, 1000f, 50f, () -> !smartDelay.getValue());

    private final TimeUtil timeUtil = new TimeUtil();
    private final TimeUtil timeUtil2 = new TimeUtil();

    @Override
    protected void onEnable() {
        lastSlot = -1;
        itemManager = new ItemManager(Ambient.getInstance().getModuleManager().getModule(InvManager.class));
        super.onEnable();
    }

    @SubscribeEvent
    private void onTickEvent(UpdateEvent event){
        if ((mc.thePlayer.openContainer instanceof ContainerChest chest)) {
            if (isGui()) {
                return;
            }




            if(checkTitle.getValue() && !chest.getLowerChestInventory().getName().toLowerCase().contains("chest")){
                return;
            }


            if (!timeUtil.finished(preTakeDelay.getValue().intValue())) {
                return;
            }

            for (int i = 0; i < chest.getLowerChestInventory().getSizeInventory(); i++) {
                if ((chest.getLowerChestInventory().getStackInSlot(i) != null)) {
                    ItemStack stack = chest.getLowerChestInventory().getStackInSlot(i);
                    if (!itemManager.isUseless(stack, i) && !itemManager.isGarbage(stack)) {
                        if ((!smartDelay.getValue() && timeUtil2.finished(takeDelay.getValue().intValue())) || (smartDelay.getValue() && (lastSlot == -1 || (timeUtil2.finished((long) (i - lastSlot) * multiplier.getValue().intValue()) || timeUtil2.finished(800))))) {
                            lastSlot = i;
                            mc.playerController.windowClick(chest.windowId, i, 0, 1, mc.thePlayer);
                            timeUtil2.reset();
                        }
                    }
                }
            }

            // REVERT IOF IT BUGS, I CBA TO TEST

            if (isChestEmpty(chest) && timeUtil2.finished(smartDelay.getValue() ? 150L : takeDelay.getValue().longValue())) {
                mc.thePlayer.closeScreen();
                lastSlot = -1;
            }
        } else {
            timeUtil.reset();
        }
    }


    private boolean isChestEmpty(Container container) {
        boolean isEmpty = true;
        int maxSlot = (container.inventorySlots.size() == 90) ? 54 : 27;
        for (int i = 0; i < maxSlot; ++i) {
            if (container.getSlot(i).getHasStack() && !itemManager.isGarbage(container.getInventory().get(i)) && !itemManager.isUseless(container.getInventory().get(i), i)) {
                isEmpty = false;
            }
        }
        return isEmpty;
    }

    private boolean isGui() {
        final int range = 5;

        for (int i = -range; i < range; ++i) {
            for (int j = range; j > -range; --j) {
                for (int k = -range; k < range; ++k) {
                    int n2 = (int) mc.thePlayer.posX + i;
                    int n3 = (int) mc.thePlayer.posY + j;
                    int n4 = (int) mc.thePlayer.posZ + k;
                    BlockPos blockPos = new BlockPos(n2, n3, n4);
                    Block block = mc.theWorld.getBlockState(blockPos).getBlock();
                    if (block instanceof BlockChest || block instanceof BlockEnderChest) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
