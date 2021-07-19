package insane96mcp.progressivebosses.module.wither.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import insane96mcp.progressivebosses.ProgressiveBosses;
import net.minecraft.client.renderer.entity.BipedRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WitherMinionRenderer extends BipedRenderer<WitherMinionEntity, WitherMinionModel<WitherMinionEntity>> {
	private static final ResourceLocation MINION_TEXTURES = new ResourceLocation(ProgressiveBosses.MOD_ID, "textures/entity/wither_minion.png");

	public WitherMinionRenderer(EntityRendererManager renderManagerIn) {
		super(renderManagerIn, new WitherMinionModel<>(), 0.5F);
		this.addLayer(new BipedArmorLayer<>(this, new WitherMinionModel(0.5F, true), new WitherMinionModel(1.0F, true)));
	}

	/**
	 * Returns the location of an entity's texture.
	 */
	public ResourceLocation getEntityTexture(WitherMinionEntity entity) {
		return MINION_TEXTURES;
	}

	protected void preRenderCallback(WitherMinionEntity entitylivingbaseIn, MatrixStack matrixStackIn, float partialTickTime) {
		matrixStackIn.scale(0.75f, 0.75f, 0.75f);
	}
}
