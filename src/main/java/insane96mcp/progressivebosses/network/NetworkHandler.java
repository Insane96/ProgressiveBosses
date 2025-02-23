package insane96mcp.progressivebosses.network;

import insane96mcp.progressivebosses.ProgressiveBosses;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
	private static final String PROTOCOL_VERSION = Integer.toString(1);
	public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
			.named(new ResourceLocation(ProgressiveBosses.MOD_ID, "network_channel"))
			.clientAcceptedVersions(s -> true)
			.serverAcceptedVersions(s -> true)
			.networkProtocolVersion(() -> PROTOCOL_VERSION)
			.simpleChannel();

	private static int index = 0;

	public static void init() {
		CHANNEL.registerMessage(++index, SyncDragonAnger.class, SyncDragonAnger::encode, SyncDragonAnger::decode, SyncDragonAnger::handle);
		CHANNEL.registerMessage(++index, BeginBlastAttackPhase.class, BeginBlastAttackPhase::encode, BeginBlastAttackPhase::decode, BeginBlastAttackPhase::handle);
	}
}
