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
        if (c > 0)
            return c < 30 || (c > 50 && c % 10 < 5) || (c < 50 && c % 4 >= 2) ? WITHER_CHARGING_LOCATION : WITHER_LOCATION;

        int i = pEntity.getInvulnerableTicks();
        return i > 0 && (i > 80 || i / 5 % 2 != 1) ? WITHER_INVULNERABLE_LOCATION : WITHER_LOCATION;
    }

    protected void scale(PBWither wither, PoseStack poseStack, float partialTick) {
        /*int chargingTicks = wither.getChargingTicks();
        if (chargingTicks > 0) {
            float scale = 1f;
            //TODO Replace with WitherAttackStats.chargeTime
            scale += (wither - ((float)chargingTicks - partialTick)) * 0.003f;
            poseStack.scale(scale, scale, scale);
        }*/
        int barragingChargingTicks = wither.getBarrageChargeUpTicks();
        if (barragingChargingTicks > 0) {
            float scale = 1f;
            scale += (PBWither.BARRAGE_CHARGE_UP_TICKS - ((float)barragingChargingTicks - partialTick)) * 0.01f;
            if (barragingChargingTicks <= 5)
                scale -= (0.01f * PBWither.BARRAGE_CHARGE_UP_TICKS / 5f) * (5 - (barragingChargingTicks - partialTick));
            poseStack.scale(scale, scale, scale);
        }

        float scale = 2.0F;
        int invulnerableTicks = wither.getInvulnerableTicks();
        if (invulnerableTicks > 0) {
            scale -= ((float)invulnerableTicks - partialTick) / 220.0F * 0.5F;
        }

        poseStack.scale(scale, scale, scale);
    }
}
