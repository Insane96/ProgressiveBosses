package insane96mcp.progressivebosses.utils;

import insane96mcp.progressivebosses.ProgressiveBosses;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.neoforged.fml.loading.FMLLoader;

public class LogHelper {
	public static void error(String format, Object... args) {
		ProgressiveBosses.LOGGER.error(String.format(format, args));
	}

	public static void warn(String format, Object... args) {
		ProgressiveBosses.LOGGER.warn(String.format(format, args));
	}

	public static void info(String format, Object... args) {
		ProgressiveBosses.LOGGER.info(String.format(format, args));
	}

	public static void chat(Entity entity, String format, Object... args) {
		if (FMLLoader.isProduction() || entity.level().isClientSide)
			return;
		Component message = Component.literal(String.format(format, args));
		((ServerLevel) entity.level()).players().forEach(player -> player.sendSystemMessage(message));
	}
}