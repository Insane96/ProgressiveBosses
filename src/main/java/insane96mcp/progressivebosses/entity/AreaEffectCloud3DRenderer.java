package insane96mcp.progressivebosses.entity;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.util.ResourceLocation;

public class AreaEffectCloud3DRenderer extends EntityRenderer<AreaEffectCloud3DEntity> {
	public AreaEffectCloud3DRenderer(EntityRendererManager manager) {
		super(manager);
	}

	@Override
	public ResourceLocation getEntityTexture(AreaEffectCloud3DEntity entity) {
		return AtlasTexture.LOCATION_BLOCKS_TEXTURE;
	}
}
