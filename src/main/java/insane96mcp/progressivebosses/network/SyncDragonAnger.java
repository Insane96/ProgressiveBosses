package insane96mcp.progressivebosses.network;

import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import insane96mcp.progressivebosses.module.dragon.data.AngerComponent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncDragonAnger(int entityId, boolean isAngry) implements CustomPacketPayload {
    public static final Type<SyncDragonAnger> TYPE = new Type<>(ProgressiveBosses.id("sync_dragon_anger"));
    public static final StreamCodec<FriendlyByteBuf, SyncDragonAnger> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, SyncDragonAnger::entityId,
            ByteBufCodecs.BOOL, SyncDragonAnger::isAngry,
            SyncDragonAnger::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncDragonAnger payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientNetworkHandler.applyDragonAnger(payload.entityId(), payload.isAngry()));
    }

    public static void sync(ServerPlayer player, EnderDragon enderDragon) {
        boolean isAngry = DragonFeature.getDragonDefinition(enderDragon)
                .flatMap(definition -> definition.getComponent(AngerComponent.class))
                .map(angerComponent -> angerComponent.isAngered(enderDragon))
                .orElse(false);
        PacketDistributor.sendToPlayer(player, new SyncDragonAnger(enderDragon.getId(), isAngry));
    }
}
