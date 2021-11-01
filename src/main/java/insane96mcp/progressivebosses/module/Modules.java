package insane96mcp.progressivebosses.module;

import insane96mcp.progressivebosses.module.dragon.DragonModule;
import insane96mcp.progressivebosses.module.elderguardian.ElderGuardianModule;
import insane96mcp.progressivebosses.module.wither.WitherModule;

public class Modules {
	public static WitherModule wither;
	public static DragonModule dragon;
	public static ElderGuardianModule elderGuardian;

	public static void init() {
		wither = new WitherModule();
		dragon = new DragonModule();
		elderGuardian = new ElderGuardianModule();
	}

	public static void loadConfig() {
		wither.loadConfig();
		dragon.loadConfig();
		elderGuardian.loadConfig();
	}
}
