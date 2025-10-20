package net.minecraft.network.play.client;

import java.io.IOException;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;

public class C0FPacketConfirmTransaction implements Packet<INetHandlerPlayServer> {
    @Getter
    @Setter
    private int windowId;

    @Getter
    @Setter
    private short uid;
    private boolean accepted;

    public C0FPacketConfirmTransaction() {
    }

    public C0FPacketConfirmTransaction(int windowId, short uid, boolean accepted) {
        this.windowId = windowId;
        this.uid = uid;
        this.accepted = accepted;
    }

    public void processPacket(INetHandlerPlayServer handler) {
        handler.processConfirmTransaction(this);
    }

    public void readPacketData(PacketBuffer buf) throws IOException {
        this.windowId = buf.readByte();
        this.uid = buf.readShort();
        this.accepted = buf.readByte() != 0;
    }

    public void writePacketData(PacketBuffer buf) throws IOException {
        if (ViaLoadingBase.getInstance().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_17)) {
            buf.writeInt(this.windowId);
        } else {
            buf.writeByte(this.windowId);
            buf.writeShort(this.uid);
            buf.writeByte(this.accepted ? 1 : 0);
        }
    }

}
