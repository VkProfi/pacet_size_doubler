package ru.vkprofi.packetdoubler.mixin;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.io.IOException;
import java.util.List;

@Mixin(PacketDecoder.class)
public abstract class PacketDecoderMixin extends ByteToMessageDecoder {
    @Unique
    private static final Logger LOGGER = LogUtils.getLogger();

    @Final @Shadow
    private PacketFlow flow;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int i = in.readableBytes();
        if (i != 0) {
            FriendlyByteBuf friendlybytebuf = new FriendlyByteBuf(in);
            int j = friendlybytebuf.readVarInt();
            Packet<?> packet = ctx.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get().createPacket(this.flow, j, friendlybytebuf);
            if (packet == null) {
                throw new IOException("Bad packet id " + j);
            } else {
                int k = ctx.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get().getId();
                JvmProfiler.INSTANCE.onPacketReceived(k, j, ctx.channel().remoteAddress(), i);
                if (friendlybytebuf.readableBytes() > 0) {
                    // Разрешаем пакеты с большим количеством читаемых байтов
                    if (friendlybytebuf.readableBytes() > 83886080) {
                        throw new IOException("Packet " + ctx.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get().getId() + "/" + j + " (" + packet.getClass().getSimpleName() + ") was larger than I expected, found " + friendlybytebuf.readableBytes() + " bytes extra whilst reading packet " + j);
                    }
                }
                out.add(packet);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(Connection.PACKET_RECEIVED_MARKER, " IN: [{}:{}] {}", ctx.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get(), j, packet.getClass().getName());
                }
            }
        }
    }
}