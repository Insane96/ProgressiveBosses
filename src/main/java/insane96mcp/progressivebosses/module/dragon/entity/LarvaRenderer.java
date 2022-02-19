package insane96mcp.progressivebosses.module.dragon.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import insane96mcp.progressivebosses.ProgressiveBosses;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class LarvaRenderer extends MobRenderer<LarvaEntity, LarvaModel<LarvaEntity>> {
	private static final ResourceLocation LARVA_LOCATION = new ResourceLocation(ProgressiveBosses.MOD_ID, "textures/entity/larva.png");

	public LarvaRenderer(EntityRendererProvider.Context p_173994_) {
		super(p_173994_, new LarvaModel<>(p_173994_.bakeLayer(ModelLayers.ENDERMITE)), 0.4F);
	}

	@Override
	public ResourceLocation getTextureLocation(LarvaEntity p_114482_) {
		return LARVA_LOCATION;
	}

	protected void scale(LarvaEntity entitylivingbaseIn, PoseStack matrixStackIn, float partialTickTime) {
		matrixStackIn.scale(1.5f, 1.5f, 1.5f);
	}
}
