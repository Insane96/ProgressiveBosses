package insane96mcp.progressivebosses.network;

import insane96mcp.progressivebosses.ProgressiveBosses;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketManager {
	private static final String PROTOCOL_VERSION = Integer.toString(1);
	public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
			.named(new ResourceLocation(ProgressiveBosses.MOD_ID, "wither_sync"))
			.clientAcceptedVersions(s -> true)
			.serverAcceptedVersions(s -> true)
			.networkProtocolVersion(() -> PROTOCOL_VERSION)
			.simpleChannel();

	public static void init() {
		CHANNEL.registerMessage(1, MessageWitherSync.class, MessageWitherSync::encode, MessageWitherSync::decode, MessageWitherSync::handle);
		MinecraftForge.EVENT_BUS.register(new PacketManager());
	}
}
