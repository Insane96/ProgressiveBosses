package insane96mcp.progressivebosses.network;

import insane96mcp.progressivebosses.setup.Strings;
import net.minecraft.client.Minecraft;
import net.minecraft.server.TickTask;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class MessageHandler {
	public static Player getSidedPlayer(NetworkEvent.Context ctx) {
		return ctx.getDirection() == NetworkDirection.PLAY_TO_SERVER ? ctx.getSender() : Minecraft.getInstance().player;
	}

	public static void handleWitherSyncMessage(int id, byte charging) {
		BlockableEventLoop<? super TickTask> executor = LogicalSidedProvider.WORKQUEUE.get(LogicalSide.CLIENT);
		executor.tell(new TickTask(0, () -> {
			Level level = Minecraft.getInstance().level;
			Entity entity = level.getEntity(id);
			if (entity instanceof WitherBoss wither) {
				wither.getPersistentData().putByte(Strings.Tags.CHARGE_ATTACK, charging);
			}
		}));
	}
}
