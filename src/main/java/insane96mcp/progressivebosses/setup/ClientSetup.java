package insane96mcp.progressivebosses.setup;

import insane96mcp.progressivebosses.entity.AreaEffectCloud3DRenderer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientSetup {
	public static void init(final FMLClientSetupEvent event) {
		RenderingRegistry.registerEntityRenderingHandler(ModEntities.AREA_EFFECT_CLOUD_3D.get(), AreaEffectCloud3DRenderer::new);
	}
}
