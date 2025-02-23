package insane96mcp.progressivebosses.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class BeginBlastAttackPhase {
    int entityId;
    int timeToBlowUp;

    public BeginBlastAttackPhase(int entityId, int timeToBlowUp) {
        this.entityId = entityId;
        this.timeToBlowUp = timeToBlowUp;
    }

    public static void encode(BeginBlastAttackPhase pkt, FriendlyByteBuf buf) {
        buf.writeInt(pkt.entityId);
        buf.writeInt(pkt.timeToBlowUp);
    }

    public static BeginBlastAttackPhase decode(FriendlyByteBuf buf) {
        return new BeginBlastAttackPhase(buf.readInt(), buf.readInt());
    }

    public static void handle(final BeginBlastAttackPhase message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientNetworkHandler.beginBlastAttack(message.entityId, message.timeToBlowUp);
        });
        ctx.get().setPacketHandled(true);
    }

    public static void sync(ServerPlayer player, EnderDragon enderDragon, int timeToBlowUp) {
        Object msg = new BeginBlastAttackPhase(enderDragon.getId(), timeToBlowUp);
        NetworkHandler.CHANNEL.sendTo(msg, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }
}
