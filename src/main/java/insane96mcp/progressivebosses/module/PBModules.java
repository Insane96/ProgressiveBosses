package insane96mcp.progressivebosses.module;

import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.progressivebosses.ProgressiveBosses;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public class PBModules {
	public static final String ENDER_DRAGON = ProgressiveBosses.MOD_ID + ":ender_dragon";
	public static final String WITHER = ProgressiveBosses.MOD_ID + ":wither";
	public static final String ELDER_GUARDIAN = ProgressiveBosses.MOD_ID + ":elder_guardian";

	public static void init(IEventBus eventBus, ModConfigSpec.Builder builder) {
		create(ENDER_DRAGON, "Ender Dragon", eventBus, builder);
		create(WITHER, "Wither", eventBus, builder);
		create(ELDER_GUARDIAN, "Elder Guardian", eventBus, builder);
	}

	public static void create(String id, String name, IEventBus eventBus, ModConfigSpec.Builder builder) {
		Module.Builder.create(ResourceLocation.parse(id), name, ModConfig.Type.COMMON, builder, eventBus).build();
	}
}
