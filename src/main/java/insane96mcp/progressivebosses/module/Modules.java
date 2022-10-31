package insane96mcp.progressivebosses.module;

import insane96mcp.insanelib.base.Module;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.setup.Config;
import net.minecraftforge.fml.config.ModConfig;

public class Modules {
	public static Module wither;
	public static Module dragon;
	public static Module elderGuardian;

	public static void init() {
		wither = Module.Builder.create(ProgressiveBosses.RESOURCE_PREFIX + "wither", "Wither", ModConfig.Type.COMMON, Config.builder)
				.build();
		dragon = Module.Builder.create(ProgressiveBosses.RESOURCE_PREFIX + "ender_dragon", "Ender Dragon", ModConfig.Type.COMMON, Config.builder)
				.build();
		elderGuardian = Module.Builder.create(ProgressiveBosses.RESOURCE_PREFIX + "elder_guardian", "Elder Guardian", ModConfig.Type.COMMON, Config.builder)
				.build();
	}
}
