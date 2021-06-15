package insane96mcp.progressivebosses.modules;

import insane96mcp.progressivebosses.modules.dragon.DragonModule;
import insane96mcp.progressivebosses.modules.wither.WitherModule;

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
