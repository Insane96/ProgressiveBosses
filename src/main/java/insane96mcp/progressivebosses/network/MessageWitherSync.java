package insane96mcp.progressivebosses.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageWitherSync {
	int entityId;
	byte charging;

	public MessageWitherSync(int entityId, byte charging) {
		this.entityId = entityId;
		this.charging = charging;
	}

	public static void encode(MessageWitherSync pkt, FriendlyByteBuf buf) {
		buf.writeInt(pkt.entityId);
		buf.writeByte(pkt.charging);
	}

	public static MessageWitherSync decode(FriendlyByteBuf buf) {
		return new MessageWitherSync(buf.readInt(), buf.readByte());
	}

	public static void handle(final MessageWitherSync message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			MessageHandler.handleWitherSyncMessage(message.entityId, message.charging);
		});
		ctx.get().setPacketHandled(true);
	}
}
