package insane96mcp.progressivebosses.utils;

import insane96mcp.progressivebosses.ProgressiveBosses;

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
}