package insane96mcp.progressivebosses.network;

import insane96mcp.progressivebosses.ProgressiveBosses;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record BeginBlastAttackPhase(int entityId, int timeToBlowUp) implements CustomPacketPayload {
    public static final Type<BeginBlastAttackPhase> TYPE = new Type<>(ProgressiveBosses.id("begin_blast_attack_phase"));
    public static final StreamCodec<FriendlyByteBuf, BeginBlastAttackPhase> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, BeginBlastAttackPhase::entityId,
            ByteBufCodecs.INT, BeginBlastAttackPhase::timeToBlowUp,
            BeginBlastAttackPhase::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(BeginBlastAttackPhase payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientNetworkHandler.beginBlastAttack(payload.entityId(), payload.timeToBlowUp()));
    }

    public static void sync(ServerPlayer player, EnderDragon enderDragon, int timeToBlowUp) {
        PacketDistributor.sendToPlayer(player, new BeginBlastAttackPhase(enderDragon.getId(), timeToBlowUp));
    }
}
