package insane96mcp.progressivebosses.module.wither.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.module.wither.dispenser.WitherSkullDispenseBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Label(name = "Misc", description = "Handles various small features, such as the explosion")
@LoadFeature(module = ProgressiveBosses.RESOURCE_PREFIX + "wither")
public class MiscFeature extends Feature {
	@Config
	@Label(name = "Wither Nether Only", description = """
						The wither can only be spawned in the Nether.
						Note that this feature completely disables Wither Skulls from begin placed nearby Soul Sand when not in the Nether or when on the Nether Roof.
						Requires Minecraft restart.""")
	public static Boolean witherNetherOnly = false;

	public MiscFeature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);	}

	private boolean behaviourRegistered = false;

	@Override
	public void readConfig(final ModConfigEvent event) {
		super.readConfig(event);
		if (witherNetherOnly && !behaviourRegistered) {
			behaviourRegistered = true;
			DispenserBlock.registerBehavior(Items.WITHER_SKELETON_SKULL, new WitherSkullDispenseBehavior());
		}
	}

	@SubscribeEvent
	public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		if (!this.isEnabled())
			return;

		//noinspection ConstantConditions
		if (event.getItemStack().getItem() == Items.WITHER_SKELETON_SKULL && !canPlaceSkull(event.getLevel(), event.getPos().offset(event.getFace().getNormal()))) {
			event.setCanceled(true);
		}
	}

	/**
	 * Returns true if at the specified position a Wither Skull can be placed
	 */
	public static boolean canPlaceSkull(Level world, BlockPos pos) {
		if (!witherNetherOnly)
			return true;

		boolean isNether = world.dimension().equals(Level.NETHER);

		boolean hasSoulSandNearby = false;
		for (Direction dir : Direction.values()) {
			if (world.getBlockState(pos.offset(dir.getNormal())).getBlock().equals(Blocks.SOUL_SAND) || world.getBlockState(pos.offset(dir.getNormal())).getBlock().equals(Blocks.SOUL_SOIL)){
				hasSoulSandNearby = true;
				break;
			}
		}

		//If it's the nether, and it's below the Nether roof or there's no soulsand nearby can place the skull
		return (isNether && pos.getY() <= 127) || !hasSoulSandNearby;
	}
}
