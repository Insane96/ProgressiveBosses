package insane96mcp.progressivebosses.setup;

import insane96mcp.progressivebosses.module.wither.entity.WitherMinionRenderer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientSetup {
	public static void init(final FMLClientSetupEvent event) {
		RenderingRegistry.registerEntityRenderingHandler(PBEntities.WITHER_MINION.get(), WitherMinionRenderer::new);
	}
}