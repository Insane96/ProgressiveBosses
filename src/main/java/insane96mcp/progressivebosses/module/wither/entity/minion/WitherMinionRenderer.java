package insane96mcp.progressivebosses.module.wither.entity.minion;

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
public class WitherMinionRenderer extends HumanoidMobRenderer<WitherMinion, WitherMinionModel<WitherMinion>> {
	private static final ResourceLocation MINION_TEXTURES = new ResourceLocation(ProgressiveBosses.MOD_ID, "textures/entity/wither_minion.png");

	public WitherMinionRenderer(EntityRendererProvider.Context context) {
		this(context, ModelLayers.SKELETON, ModelLayers.SKELETON_INNER_ARMOR, ModelLayers.SKELETON_OUTER_ARMOR);
	}

	public WitherMinionRenderer(EntityRendererProvider.Context context, ModelLayerLocation model, ModelLayerLocation innerModel, ModelLayerLocation outerModel) {
		super(context, new WitherMinionModel<>(context.bakeLayer(model)), 0.5F);
		this.addLayer(new HumanoidArmorLayer<>(this, new WitherMinionModel<>(context.bakeLayer(innerModel)), new WitherMinionModel<>(context.bakeLayer(outerModel)), context.getModelManager()));
	}

	/**
	 * Returns the location of an entity's texture.
	 */
	public ResourceLocation getTextureLocation(WitherMinion entity) {
		return MINION_TEXTURES;
	}

	protected void scale(WitherMinion entitylivingbaseIn, PoseStack matrixStackIn, float partialTickTime) {
		matrixStackIn.scale(0.75f, 0.75f, 0.75f);
	}
}
