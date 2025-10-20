package net.minecraft.network;

import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.viaversion.viabackwards.protocol.v1_11to1_10.Protocol1_11To1_10;
import com.viaversion.viabackwards.protocol.v1_12_1to1_12.Protocol1_12_1To1_12;
import com.viaversion.viarewind.protocol.v1_9to1_8.Protocol1_9To1_8;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.connection.UserConnectionImpl;
import com.viaversion.viaversion.protocol.ProtocolPipelineImpl;
import com.viaversion.viaversion.protocols.v1_11_1to1_12.packet.ServerboundPackets1_12;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import de.florianmichael.vialoadingbase.netty.event.CompressionReorderEvent;
import de.florianmichael.viamcp.MCPVLBPipeline;
import de.florianmichael.viamcp.ViaMCP;
import fr.ambient.event.impl.network.PacketReceiveEvent;
import fr.ambient.event.impl.network.PacketSendEventFinal;
import fr.ambient.event.impl.network.ReceivePacketEventFirst;
import fr.ambient.util.player.ChatUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.local.LocalEventLoopGroup;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.TimeoutException;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.crypto.SecretKey;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import fr.ambient.Ambient;
import fr.ambient.event.impl.network.PacketSendEvent;

public class NetworkManager extends SimpleChannelInboundHandler<Packet> {
    private static final Logger logger = LogManager.getLogger("MinecraftLogger");
    public static final Marker logMarkerNetwork = MarkerManager.getMarker("NETWORK");
    public static final Marker logMarkerPackets = MarkerManager.getMarker("NETWORK_PACKETS", logMarkerNetwork);
    public static final AttributeKey<EnumConnectionState> attrKeyConnectionState = AttributeKey.valueOf("protocol");
    public static final LazyLoadBase<NioEventLoopGroup> CLIENT_NIO_EVENTLOOP = new LazyLoadBase<NioEventLoopGroup>() {
        protected NioEventLoopGroup load() {
            return new NioEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Client IO #%d").setDaemon(true).build());
        }
    };
    public static final LazyLoadBase<EpollEventLoopGroup> CLIENT_EPOLL_EVENTLOOP = new LazyLoadBase<EpollEventLoopGroup>() {
        protected EpollEventLoopGroup load() {
            return new EpollEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Epoll Client IO #%d").setDaemon(true).build());
        }
    };
    public static final LazyLoadBase<LocalEventLoopGroup> CLIENT_LOCAL_EVENTLOOP = new LazyLoadBase<LocalEventLoopGroup>() {
        protected LocalEventLoopGroup load() {
            return new LocalEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Local Client IO #%d").setDaemon(true).build());
        }
    };
    private final EnumPacketDirection direction;
    private final Queue<NetworkManager.InboundHandlerTuplePacketListener> outboundPacketsQueue = Queues.newConcurrentLinkedQueue();
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private Channel channel;
    private SocketAddress socketAddress;
    private INetHandler packetListener;
    private IChatComponent terminationReason;
    private boolean isEncrypted;
    private boolean disconnected;

    public NetworkManager(EnumPacketDirection packetDirection) {
        this.direction = packetDirection;
    }

    public void channelActive(ChannelHandlerContext p_channelActive_1_) throws Exception {
        super.channelActive(p_channelActive_1_);
        this.channel = p_channelActive_1_.channel();
        this.socketAddress = this.channel.remoteAddress();

        try {
            this.setConnectionState(EnumConnectionState.HANDSHAKING);
        } catch (Throwable throwable) {
            logger.fatal(throwable);
        }
    }

    public void setConnectionState(EnumConnectionState newState) {
        this.channel.attr(attrKeyConnectionState).set(newState);
        this.channel.config().setAutoRead(true);
        logger.debug("Enabled auto read");
    }

    public void channelInactive(ChannelHandlerContext p_channelInactive_1_) throws Exception {
        this.closeChannel(new ChatComponentTranslation("disconnect.endOfStream"));
    }

    public void exceptionCaught(ChannelHandlerContext p_exceptionCaught_1_, Throwable p_exceptionCaught_2_) throws Exception {
        ChatComponentTranslation chatcomponenttranslation;

        if (p_exceptionCaught_2_ instanceof TimeoutException) {
            chatcomponenttranslation = new ChatComponentTranslation("disconnect.timeout");
        } else {
            chatcomponenttranslation = new ChatComponentTranslation("disconnect.genericReason", "Internal Exception: " + p_exceptionCaught_2_);
        }

        this.closeChannel(chatcomponenttranslation);
    }

    protected void channelRead0(ChannelHandlerContext p_channelRead0_1_, Packet p_channelRead0_2_) throws Exception {


        ReceivePacketEventFirst packetReceiveEventF = new ReceivePacketEventFirst(p_channelRead0_2_);

        Ambient.getInstance().getEventBus().post(packetReceiveEventF);

        if(packetReceiveEventF.isCancelled()){
            return;
        }
        p_channelRead0_2_ = packetReceiveEventF.getPacket();

        PacketReceiveEvent packetReceiveEvent = new PacketReceiveEvent(p_channelRead0_2_);

        Ambient.getInstance().getEventBus().post(packetReceiveEvent);

        if(packetReceiveEvent.isCancelled()){
            return;
        }
        p_channelRead0_2_ = packetReceiveEvent.getPacket();


        if (this.channel.isOpen()) {
            try {
                p_channelRead0_2_.processPacket(this.packetListener);
            } catch (ThreadQuickExitException ignored) {
            }
        }
    }

    public void setNetHandler(INetHandler handler) {
        Validate.notNull(handler, "packetListener");
        logger.debug("Set listener of {} to {}", new Object[]{this, handler});
        this.packetListener = handler;
    }

    public void sendPacket(Packet packetIn) {
        PacketSendEvent packetSend = new PacketSendEvent(packetIn);
        Ambient.getInstance().getEventBus().post(packetSend);
        if (packetSend.isCancelled()) {
            return;
        }
        PacketSendEventFinal packetSendEventFinal = new PacketSendEventFinal(packetIn);
        Ambient.getInstance().getEventBus().post(packetSendEventFinal);
        if(packetSendEventFinal.isCancelled()){
            return;
        }
        packetIn = packetSendEventFinal.getPacket();


        if(shouldCancelPacket(packetIn)){
            return;
        }

        if (this.isChannelOpen()) {
            this.flushOutboundQueue();
            this.dispatchPacket(packetIn, null);
        } else {
            this.readWriteLock.writeLock().lock();

            try {
                this.outboundPacketsQueue.add(new NetworkManager.InboundHandlerTuplePacketListener(packetSend.getPacket(), (GenericFutureListener[]) null));
            } finally {
                this.readWriteLock.writeLock().unlock();
            }
        }
    }


    public boolean shouldCancelPacket(Packet packetIn){

        if(Minecraft.getMinecraft().isSingleplayer()){
            return false;
        }


        if(packetIn instanceof C0APacketAnimation && ViaLoadingBase.getInstance().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_11)){
            final PacketWrapper blockPlace = PacketWrapper.create(ServerboundPackets1_12.SWING, Ambient.getInstance().getUserConnection());

            blockPlace.write(Types.VAR_INT, 0);

            try {
                blockPlace.sendToServer(Protocol1_12_1To1_12.class);
                return true;

            } catch (Exception e) {
                ChatUtil.display("Error while translating C0A - Relog Please.");
                e.printStackTrace();
            }
        }
        if(packetIn instanceof C08PacketPlayerBlockPlacement c08PacketPlayerBlockPlacement && ViaLoadingBase.getInstance().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_11) && c08PacketPlayerBlockPlacement.getPlacedBlockDirection() == 255){
            final PacketWrapper blockPlace = PacketWrapper.create(ServerboundPackets1_12.USE_ITEM, Ambient.getInstance().getUserConnection());

            blockPlace.write(Types.VAR_INT, c08PacketPlayerBlockPlacement.getHand()); // handowo

            try {
                blockPlace.sendToServer(Protocol1_12_1To1_12.class);
                return true;
            } catch (Exception e) {
                ChatUtil.display("Error while translating c08 255 packet - Relog Please.");
                e.printStackTrace();
            }

        }
        if(packetIn instanceof C08PacketPlayerBlockPlacement c08PacketPlayerBlockPlacement && ViaLoadingBase.getInstance().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_11) && c08PacketPlayerBlockPlacement.getPlacedBlockDirection() != 255){
            final PacketWrapper blockPlace = PacketWrapper.create(ServerboundPackets1_12.USE_ITEM_ON, Ambient.getInstance().getUserConnection());
            blockPlace.write(Types.BLOCK_POSITION1_8, new BlockPosition(
                    c08PacketPlayerBlockPlacement.getPosition().getX(),
                    c08PacketPlayerBlockPlacement.getPosition().getY(),
                    c08PacketPlayerBlockPlacement.getPosition().getZ()
            ));
            blockPlace.write(Types.VAR_INT, c08PacketPlayerBlockPlacement.getPlacedBlockDirection()); // dir
            blockPlace.write(Types.VAR_INT, 0); // owo

            float f0 = c08PacketPlayerBlockPlacement.getPlacedBlockOffsetX() - (c08PacketPlayerBlockPlacement.getPlacedBlockOffsetX() % 0.0625f);
            float f1 = c08PacketPlayerBlockPlacement.getPlacedBlockOffsetY() - (c08PacketPlayerBlockPlacement.getPlacedBlockOffsetY() % 0.0625f);
            float f2 = c08PacketPlayerBlockPlacement.getPlacedBlockOffsetZ() - (c08PacketPlayerBlockPlacement.getPlacedBlockOffsetZ() % 0.0625f);

            blockPlace.write(Types.FLOAT, f0);
            blockPlace.write(Types.FLOAT, f1);
            blockPlace.write(Types.FLOAT, f2);

            try {
                blockPlace.sendToServer(Protocol1_12_1To1_12.class);
                return true;
            } catch (Exception e) {
                ChatUtil.display("Error while translating c08 " + c08PacketPlayerBlockPlacement.getPlacedBlockDirection() + " packet Relog Please.");
                e.printStackTrace();
            }
        }

        if(packetIn instanceof C02PacketUseEntity c02PacketUseEntity && c02PacketUseEntity.getAction() == C02PacketUseEntity.Action.INTERACT_AT && ViaLoadingBase.getInstance().getTargetVersion().newerThan(ProtocolVersion.v1_8)){
            Vec3 hv = c02PacketUseEntity.getHitVec();
            Entity entity = c02PacketUseEntity.getEntityFromWorld(Minecraft.getMinecraft().theWorld);
            if(hv == null || entity == null ||  entity instanceof EntityItemFrame || entity instanceof EntityFireball){
                return false;
            }
            float w = entity.width;
            float h = entity.height;
            c02PacketUseEntity.setHitVec(new Vec3(
                    Math.clamp(hv.xCoord, -w/2, w/2),
                    Math.clamp(hv.yCoord, 0, h),
                    Math.clamp(hv.zCoord, -w/2, w/2)
            ));
            return false;
        }
        return false;
    }


    public void sendPacketNoEvent(Packet packetIn) {
        PacketSendEventFinal packetSendEventFinal = new PacketSendEventFinal(packetIn);
        Ambient.getInstance().getEventBus().post(packetSendEventFinal);
        if(packetSendEventFinal.isCancelled()){
            return;
        }
        packetIn = packetSendEventFinal.getPacket();

        if(shouldCancelPacket(packetIn)){
            return;
        }

        if (this.isChannelOpen()) {
            this.flushOutboundQueue();
            this.dispatchPacket(packetIn, null);
        } else {
            this.readWriteLock.writeLock().lock();

            try {
                this.outboundPacketsQueue.add(new NetworkManager.InboundHandlerTuplePacketListener(packetIn, (GenericFutureListener[]) null));
            } finally {
                this.readWriteLock.writeLock().unlock();
            }
        }
    }
    public void sendPacketNoEventAbsolute(Packet packetIn) {

        if(shouldCancelPacket(packetIn)){
            return;
        }

        if (this.isChannelOpen()) {
            this.flushOutboundQueue();
            this.dispatchPacket(packetIn, null);
        } else {




            this.readWriteLock.writeLock().lock();

            try {
                this.outboundPacketsQueue.add(new NetworkManager.InboundHandlerTuplePacketListener(packetIn, (GenericFutureListener[]) null));
            } finally {
                this.readWriteLock.writeLock().unlock();
            }
        }
    }


    public void sendPacket(Packet packetIn, GenericFutureListener<? extends Future<? super Void>> listener, GenericFutureListener<? extends Future<? super Void>>... listeners) {
        if (this.isChannelOpen()) {
            this.flushOutboundQueue();
            this.dispatchPacket(packetIn, ArrayUtils.add(listeners, 0, listener));
        } else {
            this.readWriteLock.writeLock().lock();

            try {
                PacketSendEvent packetSend = new PacketSendEvent(packetIn);
                Ambient.getInstance().getEventBus().post(packetSend);
                if (packetSend.isCancelled()) {
                    return;
                }
                this.outboundPacketsQueue.add(new NetworkManager.InboundHandlerTuplePacketListener(packetSend.getPacket(), ArrayUtils.add(listeners, 0, listener)));
            } finally {
                this.readWriteLock.writeLock().unlock();
            }
        }
    }

    private void dispatchPacket(final Packet inPacket, final GenericFutureListener<? extends Future<? super Void>>[] futureListeners) {
        final EnumConnectionState enumconnectionstate = EnumConnectionState.getFromPacket(inPacket);
        final EnumConnectionState enumconnectionstate1 = this.channel.attr(attrKeyConnectionState).get();

        if (enumconnectionstate1 != enumconnectionstate) {
            logger.debug("Disabled auto read");
            this.channel.config().setAutoRead(false);
        }

        if (this.channel.eventLoop().inEventLoop()) {
            if (enumconnectionstate != enumconnectionstate1) {
                this.setConnectionState(enumconnectionstate);
            }

            ChannelFuture channelfuture = this.channel.writeAndFlush(inPacket);

            if (futureListeners != null) {
                channelfuture.addListeners(futureListeners);
            }

            channelfuture.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        } else {
            this.channel.eventLoop().execute(() -> {
                if (enumconnectionstate != enumconnectionstate1) {
                    NetworkManager.this.setConnectionState(enumconnectionstate);
                }

                ChannelFuture channelfuture1 = NetworkManager.this.channel.writeAndFlush(inPacket);

                if (futureListeners != null) {
                    channelfuture1.addListeners(futureListeners);
                }

                channelfuture1.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
            });
        }
    }

    private void flushOutboundQueue() {
        if (this.channel != null && this.channel.isOpen()) {
            this.readWriteLock.readLock().lock();

            try {
                while (!this.outboundPacketsQueue.isEmpty()) {
                    NetworkManager.InboundHandlerTuplePacketListener networkmanager$inboundhandlertuplepacketlistener = this.outboundPacketsQueue.poll();
                    this.dispatchPacket(networkmanager$inboundhandlertuplepacketlistener.packet, networkmanager$inboundhandlertuplepacketlistener.futureListeners);
                }
            } finally {
                this.readWriteLock.readLock().unlock();
            }
        }
    }

    public void processReceivedPackets() {
        this.flushOutboundQueue();

        if (this.packetListener instanceof ITickable) {
            ((ITickable) this.packetListener).update();
        }

        this.channel.flush();
    }

    public SocketAddress getRemoteAddress() {
        return this.socketAddress;
    }

    public void closeChannel(IChatComponent message) {
        if (this.channel.isOpen()) {
            this.channel.close().awaitUninterruptibly();
            this.terminationReason = message;
        }
    }

    public boolean isLocalChannel() {
        return this.channel instanceof LocalChannel || this.channel instanceof LocalServerChannel;
    }

    public static NetworkManager createNetworkManagerAndConnect(InetAddress address, int serverPort, boolean useNativeTransport) {
        final NetworkManager networkmanager = new NetworkManager(EnumPacketDirection.CLIENTBOUND);
        Class<? extends SocketChannel> oclass;
        LazyLoadBase<? extends EventLoopGroup> lazyloadbase;

        if (Epoll.isAvailable() && useNativeTransport) {
            oclass = EpollSocketChannel.class;
            lazyloadbase = CLIENT_EPOLL_EVENTLOOP;
        } else {
            oclass = NioSocketChannel.class;
            lazyloadbase = CLIENT_NIO_EVENTLOOP;
        }

        (new Bootstrap()).group(lazyloadbase.getValue()).handler(new ChannelInitializer<Channel>() {
            protected void initChannel(Channel p_initChannel_1_) throws Exception {
                try {
                    p_initChannel_1_.config().setOption(ChannelOption.TCP_NODELAY, Boolean.TRUE);
                } catch (ChannelException ignored) {
                }

                p_initChannel_1_.pipeline().addLast((String)"timeout", (ChannelHandler)(new ReadTimeoutHandler(30))).addLast((String)"splitter", (ChannelHandler)(new MessageDeserializer2())).addLast((String)"decoder", (ChannelHandler)(new MessageDeserializer(EnumPacketDirection.CLIENTBOUND))).addLast((String)"prepender", (ChannelHandler)(new MessageSerializer2())).addLast((String)"encoder", (ChannelHandler)(new MessageSerializer(EnumPacketDirection.SERVERBOUND))).addLast((String)"packet_handler", (ChannelHandler)networkmanager);

                if (p_initChannel_1_ instanceof SocketChannel && ViaLoadingBase.getInstance().getTargetVersion().getVersion() != ViaMCP.NATIVE_VERSION) {
                    Ambient.getInstance().setUserConnection(new UserConnectionImpl(p_initChannel_1_, true));
                    new ProtocolPipelineImpl(Ambient.getInstance().getUserConnection());

                    p_initChannel_1_.pipeline().addLast(new MCPVLBPipeline(Ambient.getInstance().getUserConnection()));
                }            }
        }).channel(oclass).connect(address, serverPort).syncUninterruptibly();
        return networkmanager;
    }

    public static NetworkManager provideLocalClient(SocketAddress address) {
        final NetworkManager networkmanager = new NetworkManager(EnumPacketDirection.CLIENTBOUND);
        (new Bootstrap()).group(CLIENT_LOCAL_EVENTLOOP.getValue()).handler(new ChannelInitializer<Channel>() {
            protected void initChannel(Channel p_initChannel_1_) throws Exception {
                p_initChannel_1_.pipeline().addLast("packet_handler", networkmanager);
            }
        }).channel(LocalChannel.class).connect(address).syncUninterruptibly();
        return networkmanager;
    }

    public void enableEncryption(SecretKey key) {
        this.isEncrypted = true;
        this.channel.pipeline().addBefore("splitter", "decrypt", new NettyEncryptingDecoder(CryptManager.createNetCipherInstance(2, key)));
        this.channel.pipeline().addBefore("prepender", "encrypt", new NettyEncryptingEncoder(CryptManager.createNetCipherInstance(1, key)));
    }

    public boolean getIsencrypted() {
        return this.isEncrypted;
    }

    public boolean isChannelOpen() {
        return this.channel != null && this.channel.isOpen();
    }

    public boolean hasNoChannel() {
        return this.channel == null;
    }

    public INetHandler getNetHandler() {
        return this.packetListener;
    }

    public IChatComponent getExitMessage() {
        return this.terminationReason;
    }

    public void disableAutoRead() {
        this.channel.config().setAutoRead(false);
    }

    public void setCompressionTreshold(int treshold) {
        if (treshold >= 0) {
            if (this.channel.pipeline().get("decompress") instanceof NettyCompressionDecoder) {
                ((NettyCompressionDecoder) this.channel.pipeline().get("decompress")).setCompressionTreshold(treshold);
            } else {
                this.channel.pipeline().addBefore("decoder", "decompress", new NettyCompressionDecoder(treshold));
            }

            if (this.channel.pipeline().get("compress") instanceof NettyCompressionEncoder) {
                ((NettyCompressionEncoder) this.channel.pipeline().get("decompress")).setCompressionTreshold(treshold);
            } else {
                this.channel.pipeline().addBefore("encoder", "compress", new NettyCompressionEncoder(treshold));
            }
        } else {
            if (this.channel.pipeline().get("decompress") instanceof NettyCompressionDecoder) {
                this.channel.pipeline().remove("decompress");
            }

            if (this.channel.pipeline().get("compress") instanceof NettyCompressionEncoder) {
                this.channel.pipeline().remove("compress");
            }
        }
        this.channel.pipeline().fireUserEventTriggered(new CompressionReorderEvent());

    }

    public void checkDisconnected() {
        if (this.channel != null && !this.channel.isOpen()) {
            if (!this.disconnected) {
                this.disconnected = true;

                if (this.getExitMessage() != null) {
                    this.getNetHandler().onDisconnect(this.getExitMessage());
                } else if (this.getNetHandler() != null) {
                    this.getNetHandler().onDisconnect(new ChatComponentText("Disconnected"));
                }
            } else {
                logger.warn("handleDisconnection() called twice");
            }
        }
    }

    static class InboundHandlerTuplePacketListener {
        private final Packet packet;
        private final GenericFutureListener<? extends Future<? super Void>>[] futureListeners;

        public InboundHandlerTuplePacketListener(Packet inPacket, GenericFutureListener<? extends Future<? super Void>>... inFutureListeners) {
            this.packet = inPacket;
            this.futureListeners = inFutureListeners;
        }
    }
}
