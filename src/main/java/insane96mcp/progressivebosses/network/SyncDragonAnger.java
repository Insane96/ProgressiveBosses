package insane96mcp.progressivebosses.network;

import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import insane96mcp.progressivebosses.module.dragon.data.AngerComponent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncDragonAnger {
    int entityId;
    boolean isAngry;

    public SyncDragonAnger(int entityId, boolean isAngry) {
        this.entityId = entityId;
        this.isAngry = isAngry;
    }

    public static void encode(SyncDragonAnger pkt, FriendlyByteBuf buf) {
        buf.writeInt(pkt.entityId);
        buf.writeBoolean(pkt.isAngry);
    }

    public static SyncDragonAnger decode(FriendlyByteBuf buf) {
        return new SyncDragonAnger(buf.readInt(), buf.readBoolean());
    }

    public static void handle(final SyncDragonAnger message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientNetworkHandler.applyDragonAnger(message.entityId, message.isAngry);
        });
        ctx.get().setPacketHandled(true);
    }

    public static void sync(ServerPlayer player, EnderDragon enderDragon) {
        boolean isAngry = DragonFeature.getDragonDefinition(enderDragon)
                .flatMap(definition -> definition.getComponent(AngerComponent.class))
                .map(angerComponent -> angerComponent.isAngered(enderDragon))
                .orElse(false);
        Object msg = new SyncDragonAnger(enderDragon.getId(), isAngry);
        NetworkHandler.CHANNEL.sendTo(msg, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }
}
