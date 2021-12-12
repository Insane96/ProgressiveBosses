package insane96mcp.progressivebosses.module.wither.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import insane96mcp.progressivebosses.ProgressiveBosses;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WitherMinionRenderer extends HumanoidMobRenderer<WitherMinionEntity, WitherMinionModel<WitherMinionEntity>> {
	private static final ResourceLocation MINION_TEXTURES = new ResourceLocation(ProgressiveBosses.MOD_ID, "textures/entity/wither_minion.png");

	public WitherMinionRenderer(EntityRendererProvider.Context context) {
		this(context, ModelLayers.SKELETON, ModelLayers.SKELETON_INNER_ARMOR, ModelLayers.SKELETON_OUTER_ARMOR);
	}

	public WitherMinionRenderer(EntityRendererProvider.Context p_174382_, ModelLayerLocation p_174383_, ModelLayerLocation p_174384_, ModelLayerLocation p_174385_) {
		super(p_174382_, new WitherMinionModel<>(p_174382_.bakeLayer(p_174383_)), 0.5F);
		this.addLayer(new HumanoidArmorLayer<>(this, new WitherMinionModel(p_174382_.bakeLayer(p_174384_)), new WitherMinionModel(p_174382_.bakeLayer(p_174385_))));
	}

	/**
	 * Returns the location of an entity's texture.
	 */
	public ResourceLocation getTextureLocation(WitherMinionEntity entity) {
		return MINION_TEXTURES;
	}

	protected void scale(WitherMinionEntity entitylivingbaseIn, PoseStack matrixStackIn, float partialTickTime) {
		matrixStackIn.scale(0.75f, 0.75f, 0.75f);
	}
}
