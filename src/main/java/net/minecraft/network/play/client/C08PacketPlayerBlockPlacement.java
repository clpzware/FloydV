package net.minecraft.network.play.client;

import java.io.IOException;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.util.BlockPos;

public class C08PacketPlayerBlockPlacement implements Packet<INetHandlerPlayServer> {
    private static final BlockPos field_179726_a = new BlockPos(-1, -1, -1);
    @Getter
    private BlockPos position;
    @Getter
    private int placedBlockDirection;
    @Getter
    private ItemStack stack;
    private float facingX;
    private float facingY;
    private float facingZ;

    @Getter
    @Setter
    private int hand = 0;

    public C08PacketPlayerBlockPlacement() {
    }

    public C08PacketPlayerBlockPlacement(ItemStack stackIn) {
        this(field_179726_a, 255, stackIn, 0.0F, 0.0F, 0.0F);
    }

    public C08PacketPlayerBlockPlacement(BlockPos positionIn, int placedBlockDirectionIn, ItemStack stackIn, float facingXIn, float facingYIn, float facingZIn) {
        this.position = positionIn;
        this.placedBlockDirection = placedBlockDirectionIn;
        this.stack = stackIn != null ? stackIn.copy() : null;
        this.facingX = facingXIn;
        this.facingY = facingYIn;
        this.facingZ = facingZIn;
    }

    public void readPacketData(PacketBuffer buf) throws IOException {
        this.position = buf.readBlockPos();
        this.placedBlockDirection = buf.readUnsignedByte();
        this.stack = buf.readItemStackFromBuffer();
        this.facingX = (float) buf.readUnsignedByte() / 16.0F;
        this.facingY = (float) buf.readUnsignedByte() / 16.0F;
        this.facingZ = (float) buf.readUnsignedByte() / 16.0F;
    }

    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeBlockPos(this.position);
        buf.writeByte(this.placedBlockDirection);
        buf.writeItemStackToBuffer(this.stack);
        if(ViaLoadingBase.getInstance().getTargetVersion().newerThan(ProtocolVersion.v1_8)){
            /*buf.writeFloat(this.facingX);
            buf.writeFloat(this.facingY);
            buf.writeFloat(this.facingZ);*/
            buf.writeByte((int) (this.facingX * 16.0F));
            buf.writeByte((int) (this.facingY * 16.0F));
            buf.writeByte((int) (this.facingZ * 16.0F));
        }else {
            buf.writeByte((int) (this.facingX * 16.0F));
            buf.writeByte((int) (this.facingY * 16.0F));
            buf.writeByte((int) (this.facingZ * 16.0F));
        }
    }

    public void processPacket(INetHandlerPlayServer handler) {
        handler.processPlayerBlockPlacement(this);
    }

    public float getPlacedBlockOffsetX() {
        return this.facingX;
    }

    public float getPlacedBlockOffsetY() {
        return this.facingY;
    }

    public float getPlacedBlockOffsetZ() {
        return this.facingZ;
    }
}
