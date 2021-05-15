package insane96mcp.progressivebosses.base;

import insane96mcp.progressivebosses.modules.dragon.DragonModule;
import insane96mcp.progressivebosses.modules.wither.WitherModule;

public class Modules {
	public static WitherModule witherModule;
	public static DragonModule dragonModule;

	public static void init() {
		witherModule = new WitherModule();
		dragonModule = new DragonModule();
	}

	public static void loadConfig() {
		witherModule.loadConfig();
		dragonModule.loadConfig();
	}
}
