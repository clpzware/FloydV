package fr.ambient.module.impl.player;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import fr.ambient.Ambient;
import fr.ambient.component.impl.misc.BreakerWhitelistComponent;
import fr.ambient.event.EventPriority;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.event.impl.render.Render2DEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.module.impl.combat.KillAura;
import fr.ambient.property.impl.BooleanProperty;
import fr.ambient.property.impl.ModeProperty;
import fr.ambient.property.impl.NumberProperty;
import fr.ambient.util.BlockUtil;
import fr.ambient.util.PosFace;
import fr.ambient.util.packet.PacketUtil;
import fr.ambient.util.player.movecorrect.MoveCorrect;
import fr.ambient.util.render.RenderUtil;
import fr.ambient.util.render.font.Fonts;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockBed;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

public class Breaker extends Module {
    private final ModeProperty breakType = ModeProperty.newInstance("Break Mode", new String[]{"Direct", "Hypixel", "Legit"}, "Hypixel");
    private final NumberProperty breakrange = NumberProperty.newInstance("Breaker Range", 3f, 6f, 10f, 0.5f);
    public final BooleanProperty swing = BooleanProperty.newInstance("Swing ( clientside )", false);
    public final BooleanProperty whitelist = BooleanProperty.newInstance("Whitelist", true);
    private final ModeProperty hudMode = ModeProperty.newInstance("HUD Mode", new String[]{"None", "Display"}, "None");
    private final ModeProperty moveFix = ModeProperty.newInstance("Movement Fix", new String[]{"None", "Silent", "Strict", "NoSprint"}, "None");

    public BlockPos breakPos;
    public BlockPos bedPos;

    public float blockDamage;
    public float lastBlockDamage;
    public int blockDamageCD;
    public  int spoofTick = 0;



    public Breaker() {
        super(29,"Automatically breaks enemy beds through walls.", ModuleCategory.PLAYER);
        this.registerProperties(breakType,hudMode, moveFix,
                breakrange,
                swing,
                whitelist);
        this.setDraggable(true);
        this.setX(200);
        this.setY(320);
        this.setWidth(100);
        this.setHeight(20);

    }

    public void onEnable() {
        blockDamage = 0;
        lastBlockDamage = blockDamage;
        blockDamageCD = 5;
        bedPos = null;
        breakPos = null;
    }


    public boolean pauseOtherAction = false;


    public void onDisable() {
        if (blockDamage > 0 && blockDamage < 1 && breakPos != null) {
            PacketUtil.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, breakPos, EnumFacing.UP));
        }

        blockDamage = 0;
        lastBlockDamage = blockDamage;
        blockDamageCD = 0;
        bedPos = null;
        breakPos = null;
        Ambient.getInstance().getRotationComponent().setActive(false, this.getClass());
    }

    public ItemStack st = null;

    @SubscribeEvent
    private void render2D(Render2DEvent event){
        if(this.breakPos != null){
            float smoothedOut = lastBlockDamage + (blockDamage - lastBlockDamage) * mc.timer.renderPartialTicks;
            switch (hudMode.getValue()){
                case "Bar" -> {
                    this.setWidth(100);
                    this.setHeight(10);
                    RenderUtil.drawRect(this.getX(), this.getY(), this.getWidth(), this.getHeight(), new Color(20,20,20,70));
                    RenderUtil.drawRect(this.getX(), this.getY(), this.getWidth() * MathHelper.clamp_float(smoothedOut, 0, 1), this.getHeight(), Ambient.getInstance().getHud().getCurrentTheme().getColor1());
                }
                case "Percentage" -> {
                    this.setWidth(100);
                    this.setHeight(15);
                    Fonts.getOpenSansMedium(13).drawString(new DecimalFormat("#.##").format(MathHelper.clamp_float(smoothedOut, 0, 1) * 100) + " %", this.getX() + 1, this.getY() + 1, Color.WHITE.getRGB());
                    RenderUtil.drawRect(this.getX(), this.getY(), this.getWidth(), this.getHeight(), new Color(20,20,20,70));
                    RenderUtil.drawRect(this.getX(), this.getY() + 12, this.getWidth() * MathHelper.clamp_float(smoothedOut, 0, 1), 3, Ambient.getInstance().getHud().getCurrentTheme().getColor1());
                }
                case "Display" -> {
                    this.setWidth(120);
                    this.setHeight(35);

                    Color color1 = Ambient.getInstance().getHud().getCurrentTheme().color1;
                    Color color2 = Ambient.getInstance().getHud().getCurrentTheme().color2;

                    Block block = mc.theWorld.getBlockState(breakPos).getBlock();
                    String blockName = block.getLocalizedName();
                    ItemStack blockStack = new ItemStack(Item.getItemFromBlock(block));
                    if(block == Blocks.air){
                        blockName = "Nothing ( CD )";
                        blockStack = new ItemStack(Blocks.barrier);
                    } else if (block == Blocks.bed) {
                        blockStack = new ItemStack(Items.bed);
                    }



                    RenderUtil.drawRoundedRect(this.getX() - 1, this.getY() - 1, this.getWidth() + 2, this.getHeight() + 2, 7, new Color(0x90121214, true));
                    RenderUtil.drawRoundedRect(this.getX() + 4, this.getY() + 4, 17, 17, 2.5f, new Color(0x65000000, true));

                    ItemStack finalBlockStack = blockStack;
                    RenderUtil.scale(() -> RenderUtil.drawItemStack(finalBlockStack, this.getX() + 4.5f, this.getY() + 4.5f), this.getX() + 12.5f, this.getY() + 12.5f, 0.8f);


                    float rectStartX = this.getX() + 4;
                    float rectWidth = 112 * MathHelper.clamp_float(smoothedOut, 0, 1);
                    float rectEndX = rectStartX + rectWidth;
                    String percent = new DecimalFormat("#").format(MathHelper.clamp_float(smoothedOut, 0, 1) * 100) + "%";

                    Fonts.getNunito(16).drawString(blockName, this.getX() + 25, this.getY() + 5, Color.WHITE.getRGB());
                    Fonts.getNunito(13).drawString(percent, MathHelper.clamp_float(rectEndX - Fonts.getNunito(13).getWidth(percent) / 2f, this.getX() + 25, this.getX() + this.getWidth() - 4 - Fonts.getNunito(13).getWidth(percent)), this.getY() + 16, color2.getRGB());

                    RenderUtil.drawRoundedRect(this.getX() + 4, this.getY() + this.getHeight() - 10, 112, 6, 2.5f, new Color(0x65000000, true));
                    RenderUtil.drawRoundedRect(this.getX() + 4, this.getY() + this.getHeight() - 10, 112 * MathHelper.clamp_float(smoothedOut, 0, 1), 6, 2.5f, color2, color2, color1, color1);
                }
            }
        }
    }

    public void sendBlockBreak(BlockPos pos, EnumFacing face) {
        lastBlockDamage = blockDamage;
        if(pos == null){
            if(blockDamageCD > 0){
                blockDamageCD--;
                return;
            }
        }

        Block block = mc.theWorld.getBlockState(pos).getBlock();
        if (block instanceof BlockAir) {
            this.blockDamage = 0;
            this.blockDamageCD = 6;
            return;
        }
        if(blockDamageCD > 0){
            blockDamageCD--;
            return;
        }
        spoofTick = getSlotFromBlock(block);
        ItemStack stack = mc.thePlayer.getHeldItem();
        if (spoofTick == -1) {
            spoofTick = mc.thePlayer.inventory.currentItem;
        } else {
            stack = mc.thePlayer.inventory.getStackInSlot(spoofTick);
        }
        st = stack;



        KillAura killAura = Ambient.getInstance().getModuleManager().getModule(KillAura.class);
        if(blockDamage == 0f){
            if(killAura.isEnabled() && KillAura.target != null && breakPos != null){
                 return;
            }else{
                startBreak();
            }
        }

        addDamage(breakPos);


        if(blockDamage >= 1f){
            if (!killAura.isEnabled() || KillAura.target == null) {
                stopBreak();
            }
        }
    }


    public void sendAnimReqPcket(Packet packet){
        if(ViaLoadingBase.getInstance().getTargetVersion().newerThan(ProtocolVersion.v1_8)){
            PacketUtil.sendPacket(packet);
            if(swing.getValue()){
                mc.thePlayer.swingItem();
            }else{
                PacketUtil.sendPacket(new C0APacketAnimation());
            }
        }else{
            if(swing.getValue()){
                mc.thePlayer.swingItem();
            }else{
                PacketUtil.sendPacket(new C0APacketAnimation());
            }
            PacketUtil.sendPacket(packet);
        }
    }


    public void startBreak(){
        int oldSlot = mc.thePlayer.inventory.currentItem;
        mc.thePlayer.inventory.currentItem = spoofTick;

        mc.playerController.syncCurrentPlayItem();
        sendAnimReqPcket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, breakPos, EnumFacing.UP));
        addDamage(breakPos);

        mc.thePlayer.inventory.currentItem = oldSlot;

    }

    public void addDamage(BlockPos pos){
        Block block = mc.theWorld.getBlockState(pos).getBlock();
        float addyDMG = mc.thePlayer.getToolDigEfficiency(block, st) / block.getBlockHardness(mc.theWorld, pos) / 30.0F;
        if(!mc.thePlayer.onGround){
            addyDMG /= 5;
        }
        blockDamage += addyDMG;
        mc.theWorld.sendBlockBreakProgress(this.mc.thePlayer.getEntityId(), pos, (int) (this.blockDamage * 10.0F) - 1);
    }


    public void stopBreak(){
        int oldSl = mc.thePlayer.inventory.currentItem;
        mc.thePlayer.inventory.currentItem = spoofTick;
        mc.playerController.syncCurrentPlayItem();
        sendAnimReqPcket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, breakPos, EnumFacing.UP));
        this.blockDamage = 0;
        this.blockDamageCD = 5;
        mc.theWorld.setBlockToAir(breakPos);
        mc.thePlayer.inventory.currentItem = oldSl;
    }

    @SubscribeEvent(value = EventPriority.VERY_HIGH)
    private void onPlayerTick(UpdateEvent event) {
        if (Ambient.getInstance().getModuleManager().getModule(NoFall.class).blinking) {
            return;
        }

        BlockPos closestBedPos = findBed(breakrange.getValue());

        if(closestBedPos != null){
            if(!checkPosValidity(closestBedPos)){
                return;
            }
            if(blockDamageCD != 0){
                if(breakType.is("Hypixel")){
                    if (isBedOpen(closestBedPos)) {
                        breakPos = closestBedPos;
                    } else {
                        if (breakPos == null) {
                            breakPos = getNearestBlock(closestBedPos);
                        } else if (!(mc.theWorld.getBlockState(breakPos).getBlock() instanceof BlockAir) && !(mc.theWorld.getBlockState(breakPos).getBlock().isFullBlock())) {
                            breakPos = getNearestBlock(closestBedPos);
                        }
                    }
                } else {
                    breakPos = closestBedPos;
                }
            }
            sendBlockBreak(breakPos, EnumFacing.UP);
        }else{
            blockDamage = 0;
            blockDamageCD = 1;
            bedPos = null;
            breakPos = null;
            Ambient.getInstance().getRotationComponent().setActive(false, this.getClass());
        }

    }

    @SubscribeEvent
    private void onNetworkTick(PreMotionEvent event) {
        if (breakPos != null && !(mc.theWorld.getBlockState(breakPos).getBlock() instanceof BlockAir) && KillAura.target == null) {
            if ((blockDamage == 0 || (blockDamage + mc.theWorld.getBlockState(breakPos).getBlock().getPlayerRelativeBlockHardness(this.mc.thePlayer, this.mc.thePlayer.worldObj, breakPos)) >= 1)) {
                float[] rotations = BlockUtil.getRotationToBlockDirect(new PosFace(breakPos, EnumFacing.UP));
                Ambient.getInstance().getRotationComponent().setActive(true, this.getClass());
                Ambient.getInstance().getRotationComponent().setRotations(rotations[0], rotations[1], MoveCorrect.getMoveCorrect(moveFix.getValue()));
            }else{
                if(blockDamage != 0 && !((blockDamage + mc.theWorld.getBlockState(breakPos).getBlock().getPlayerRelativeBlockHardness(this.mc.thePlayer, this.mc.thePlayer.worldObj, breakPos)) >= 1) && Ambient.getInstance().getRotationComponent().active){
                    Ambient.getInstance().getRotationComponent().setActive(false, this.getClass());
                }
            }
        }
    }

    private boolean checkPosValidity(BlockPos pos) {
        if (pos == null) {
            return false;
        }
        return !(mc.thePlayer.getDistance(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > 6);
    }

    private BlockPos findBed(float distance) {
        float bedDist = 69420;
        BlockPos bedPos = null;

        for (float x = -distance; x < distance; x++) {
            for (float z = -distance; z < distance; z++) {
                for (float y = -distance; y < distance; y++) {
                    BlockPos cPos = new BlockPos(x + mc.thePlayer.posX, y + mc.thePlayer.posY, z + mc.thePlayer.posZ);
                    if (mc.theWorld.getBlockState(cPos).getBlock() instanceof BlockBed) {

                        if(whitelist.getValue()){
                            if(BreakerWhitelistComponent.isWhitelisted(cPos)){
                                continue;
                            }
                        }


                        double bcd = mc.thePlayer.getDistance(cPos.getX() + 0.5, cPos.getY() + 0.5, cPos.getZ() + 0.5);
                        if (bcd < bedDist) {
                            bedDist = (float) bcd;
                            bedPos = cPos;
                        }
                    }
                }
            }
        }

        return bedPos;
    }

    private boolean isBedOpen(BlockPos pos) {
        BlockPos bed_pos2 = null;
        for (BlockPos adjacentPos : new BlockPos[]{pos.north(), pos.south(), pos.east(), pos.west()}) {
            if (mc.theWorld.getBlockState(adjacentPos).getBlock() instanceof BlockBed) {
                bed_pos2 = adjacentPos;
                break;
            }
        }
        if (bed_pos2 == null) return false;
        return isNearbyAir(pos) || isNearbyAir(bed_pos2);
    }

    public boolean isNearbyAir(BlockPos pos) {
        for (BlockPos adjacentPos : new BlockPos[]{pos.up(), pos.south(), pos.east(), pos.west(), pos.north()}) {
            if (mc.theWorld.getBlockState(adjacentPos).getBlock() instanceof BlockAir) {
                return true;

            }
        }
        return false;
    }

    public BlockPos getNearestBlock(BlockPos pos) {
        double distance = 69;
        BlockPos lasb = null;

        for (BlockPos p : new ArrayList<>(Arrays.asList(pos.up(), pos.west(), pos.south(), pos.east(), pos.north()))) {
            if (!(mc.theWorld.getBlockState(p).getBlock() instanceof BlockBed) && mc.thePlayer.getDistance(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5) < distance) {
                lasb = p;
                distance = mc.thePlayer.getDistance(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5);
            }
        }
        return lasb;
    }

    public int getSlotFromBlock(Block block) {
        int slot = -1;
        float breakspeed = 0;

        for (int i = 0; i < 9; i++) {
            if (mc.thePlayer.inventory.getStackInSlot(i) != null && mc.thePlayer.inventory.getStackInSlot(i).getStrVsBlock(block) > breakspeed) {
                breakspeed = mc.thePlayer.inventory.getStackInSlot(i).getStrVsBlock(block);
                slot = i;
            }
        }
        return slot;
    }
}