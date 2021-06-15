package insane96mcp.progressivebosses.module;

import insane96mcp.progressivebosses.module.dragon.DragonModule;
import insane96mcp.progressivebosses.module.wither.WitherModule;

public class Modules {
	public static WitherModule wither;
	public static DragonModule dragon;

	public static void init() {
		wither = new WitherModule();
		dragon = new DragonModule();
	}

	public static void loadConfig() {
		wither.loadConfig();
		dragon.loadConfig();
	}
}
