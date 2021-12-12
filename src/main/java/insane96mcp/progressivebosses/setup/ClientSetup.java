package insane96mcp.progressivebosses.setup;

import insane96mcp.progressivebosses.module.wither.entity.WitherMinionRenderer;
import net.minecraftforge.client.event.EntityRenderersEvent;

public class ClientSetup {
	public static void init(final EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(PBEntities.WITHER_MINION.get(), WitherMinionRenderer::new);
	}
}