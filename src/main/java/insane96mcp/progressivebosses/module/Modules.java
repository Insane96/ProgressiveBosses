package insane96mcp.progressivebosses.module;

import insane96mcp.insanelib.base.Module;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.setup.Config;

public class Modules {
	public static Module wither;
	public static Module dragon;
	public static Module elderGuardian;

	public static void init() {
		wither = Module.Builder.create(Config.builder, ProgressiveBosses.RESOURCE_PREFIX + "wither", "Wither")
				.build();
		dragon = Module.Builder.create(Config.builder, ProgressiveBosses.RESOURCE_PREFIX + "ender_dragon", "Ender Dragon")
				.build();
		elderGuardian = Module.Builder.create(Config.builder, ProgressiveBosses.RESOURCE_PREFIX + "elder_guardian", "Elder Guardian")
				.build();
	}
}
