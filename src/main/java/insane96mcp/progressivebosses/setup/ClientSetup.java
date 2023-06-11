package insane96mcp.progressivebosses.setup;

import insane96mcp.progressivebosses.module.dragon.entity.LarvaRenderer;
import insane96mcp.progressivebosses.module.wither.entity.WitherMinionRenderer;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;

public class ClientSetup {
	public static void registerEntityRenderers(final EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(PBEntities.WITHER_MINION.get(), WitherMinionRenderer::new);
		event.registerEntityRenderer(PBEntities.LARVA.get(), LarvaRenderer::new);
	}

	public static void creativeTabsBuildContents(final BuildCreativeModeTabContentsEvent event)
	{
		if (event.getTabKey() == CreativeModeTabs.INGREDIENTS)
		{
			event.accept(PBItems.NETHER_STAR_SHARD.get());
			event.accept(PBItems.ELDER_GUARDIAN_SPIKE.get());
		}
	}
}