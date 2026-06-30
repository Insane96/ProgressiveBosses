package insane96mcp.progressivebosses.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkHandler {
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(SyncDragonAnger.TYPE, SyncDragonAnger.STREAM_CODEC, SyncDragonAnger::handle);
        registrar.playToClient(BeginBlastAttackPhase.TYPE, BeginBlastAttackPhase.STREAM_CODEC, BeginBlastAttackPhase::handle);
    }
}
