package insane96mcp.progressivebosses.module.wither.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.module.wither.dispenser.WitherSkullDispenseBehavior;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DispenserBlock;

@Label(name = "Misc", description = "Handles various small features, such as the explosion")
@LoadFeature(module = ProgressiveBosses.RESOURCE_PREFIX + "wither")
public class MiscFeature extends Feature {
	public MiscFeature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);

		DispenserBlock.registerBehavior(Items.WITHER_SKELETON_SKULL, new WitherSkullDispenseBehavior());
	}
}
