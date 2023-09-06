package insane96mcp.progressivebosses.module.wither.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.WitherBossModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EnergySwirlLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class PBWitherArmorLayer extends EnergySwirlLayer<PBWither, WitherBossModel<PBWither>> {
    private static final ResourceLocation WITHER_ARMOR_LOCATION = new ResourceLocation("textures/entity/wither/wither_armor.png");
    private final WitherBossModel<PBWither> model;
    public PBWitherArmorLayer(RenderLayerParent<PBWither, WitherBossModel<PBWither>> pRenderer, EntityModelSet pModelSet) {
        super(pRenderer);
        this.model = new WitherBossModel<>(pModelSet.bakeLayer(ModelLayers.WITHER_ARMOR));
    }

    @Override
    protected float xOffset(float pTickCount) {
        return Mth.cos(pTickCount * 0.02F) * 3.0F;
    }

    @Override
    protected ResourceLocation getTextureLocation() {
        return WITHER_ARMOR_LOCATION;
    }

    @Override
    protected EntityModel<PBWither> model() {
        return this.model;
    }
}
