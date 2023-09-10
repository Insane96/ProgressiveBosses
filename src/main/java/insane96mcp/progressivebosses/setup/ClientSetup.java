package insane96mcp.progressivebosses.setup;

import insane96mcp.progressivebosses.module.dragon.entity.LarvaRenderer;
import insane96mcp.progressivebosses.module.wither.data.WitherStatsReloadListener;
import insane96mcp.progressivebosses.module.wither.entity.PBWitherRenderer;
import insane96mcp.progressivebosses.module.wither.entity.minion.WitherMinionRenderer;
import insane96mcp.progressivebosses.module.wither.entity.skull.PBWitherSkullRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;

public class ClientSetup {
	public static void registerEntityRenderers(final EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(PBEntities.WITHER.get(), PBWitherRenderer::new);
		event.registerEntityRenderer(PBEntities.WITHER_SKULL.get(), PBWitherSkullRenderer::new);
		event.registerEntityRenderer(PBEntities.WITHER_MINION.get(), WitherMinionRenderer::new);
		event.registerEntityRenderer(PBEntities.LARVA.get(), LarvaRenderer::new);
	}

	public static void creativeTabsBuildContents(final BuildCreativeModeTabContentsEvent event)
	{
		if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS)
		{
			//No need for the lvl 0 corruption
			for (int i = 1; i < WitherStatsReloadListener.STATS_MAP.size(); i++) {
				ItemStack stack = new ItemStack(PBItems.CORRUPTED_SOUL_SAND.get(), 1);
				CompoundTag tag = new CompoundTag();
				tag.putInt("lvl", i);
				BlockItem.setBlockEntityData(stack, PBBlocks.CORRUPTED_SOUL_SAND_BLOCK_ENTITY.get(), tag);
				event.accept(stack);
			}
		}
		else if (event.getTabKey() == CreativeModeTabs.INGREDIENTS)
		{
			event.accept(PBItems.NETHER_STAR_SHARD.get());
			event.accept(PBItems.ELDER_GUARDIAN_SPIKE.get());
		}
	}
}