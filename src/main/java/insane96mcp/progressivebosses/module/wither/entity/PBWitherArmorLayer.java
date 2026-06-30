package insane96mcp.progressivebosses.module.wither.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EnergySwirlLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class PBWitherArmorLayer extends EnergySwirlLayer<PBWither, PBWitherModel<PBWither>> {
    private static final ResourceLocation WITHER_ARMOR_LOCATION = new ResourceLocation("textures/entity/wither/wither_armor.png");
    private final PBWitherModel<PBWither> model;
    public PBWitherArmorLayer(RenderLayerParent<PBWither, PBWitherModel<PBWither>> pRenderer, EntityModelSet pModelSet) {
        super(pRenderer);
        this.model = new PBWitherModel<>(pModelSet.bakeLayer(ModelLayers.WITHER_ARMOR));
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
