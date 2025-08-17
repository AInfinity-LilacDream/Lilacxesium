package mcevent.lilacxesium.client.musicdodge;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.nio.charset.StandardCharsets;

/**
 * MusicDodge自定义Payload - 1.21.4版本
 */
public record MusicDodgePayload(String data) implements CustomPayload {
    
    public static final Identifier ID = Identifier.of("mce", "musicdodge");
    public static final CustomPayload.Id<MusicDodgePayload> TYPE = new CustomPayload.Id<>(ID);
    
    public static final PacketCodec<PacketByteBuf, MusicDodgePayload> CODEC = PacketCodec.of(
        MusicDodgePayload::write,
        MusicDodgePayload::read
    );
    
    /**
     * 写入数据到缓冲区
     */
    public void write(PacketByteBuf buf) {
        buf.writeString(this.data);
    }
    
    /**
     * 从缓冲区读取数据
     */
    public static MusicDodgePayload read(PacketByteBuf buf) {
        String data = buf.readString();
        return new MusicDodgePayload(data);
    }
    
    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return TYPE;
    }
}