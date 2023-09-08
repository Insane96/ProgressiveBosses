package insane96mcp.progressivebosses.module.wither.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import insane96mcp.progressivebosses.ProgressiveBosses;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class PBWitherRenderer extends MobRenderer<PBWither, PBWitherModel<PBWither>> {
    private static final ResourceLocation WITHER_CHARGING_LOCATION = new ResourceLocation(ProgressiveBosses.MOD_ID, "textures/entity/wither/wither_charge.png");
    private static final ResourceLocation WITHER_INVULNERABLE_LOCATION = new ResourceLocation("textures/entity/wither/wither_invulnerable.png");
    private static final ResourceLocation WITHER_LOCATION = new ResourceLocation("textures/entity/wither/wither.png");

    public PBWitherRenderer(EntityRendererProvider.Context context) {
        super(context, new PBWitherModel<>(context.bakeLayer(ModelLayers.WITHER)), 1.0F);
        this.addLayer(new PBWitherArmorLayer(this, context.getModelSet()));
    }

    protected int getBlockLightLevel(PBWither pEntity, BlockPos pPos) {
        return 15;
    }

    /**
     * Returns the location of an entity's texture.
     */
    public ResourceLocation getTextureLocation(PBWither pEntity) {
        int c = pEntity.getChargingTicks();
        //TODO Blink when about to charge
        if (c > 0)
            return WITHER_CHARGING_LOCATION;
        int i = pEntity.getInvulnerableTicks();
        return i > 0 && (i > 80 || i / 5 % 2 != 1) ? WITHER_INVULNERABLE_LOCATION : WITHER_LOCATION;
    }

    protected void scale(PBWither pLivingEntity, PoseStack poseStack, float partialTick) {
        float f = 2.0F;
        /*int chargingTicks = pLivingEntity.getChargingTicks();
        if (chargingTicks > 0) {
            float scale = 1f;
            //TODO Replace with WitherAttackStats.chargeTime
            scale += (pLivingEntity - ((float)chargingTicks - partialTick)) * 0.003f;
            poseStack.scale(scale, scale, scale);
        }*/
        int i = pLivingEntity.getInvulnerableTicks();
        if (i > 0) {
            f -= ((float)i - partialTick) / 220.0F * 0.5F;
        }

        poseStack.scale(f, f, f);
    }
}
