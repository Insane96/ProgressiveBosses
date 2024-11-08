package insane96mcp.progressivebosses.module.dragon.corruptedendcrystal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import insane96mcp.progressivebosses.ProgressiveBosses;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

@OnlyIn(Dist.CLIENT)
public class CorruptedEndCrystalRenderer extends EntityRenderer<CorruptedEndCrystal> {
    private static final ResourceLocation END_CRYSTAL_LOCATION = new ResourceLocation(ProgressiveBosses.MOD_ID, "textures/entity/corrupted_end_crystal/corrupted_end_crystal.png");
    private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(END_CRYSTAL_LOCATION);
    public static final ResourceLocation CRYSTAL_BEAM_LOCATION = new ResourceLocation(ProgressiveBosses.MOD_ID, "textures/entity/corrupted_end_crystal/corrupted_end_crystal_beam.png");
    private static final RenderType BEAM = RenderType.entitySmoothCutout(CRYSTAL_BEAM_LOCATION);
    private static final float SIN_45 = (float)Math.sin((Math.PI / 4D));
    private static final String GLASS = "glass";
    private static final String BASE = "base";
    private final ModelPart cube;
    private final ModelPart glass;
    private final ModelPart base;

    public CorruptedEndCrystalRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
        this.shadowRadius = 0.5F;
        ModelPart modelpart = pContext.bakeLayer(ModelLayers.END_CRYSTAL);
        this.glass = modelpart.getChild("glass");
        this.cube = modelpart.getChild("cube");
        this.base = modelpart.getChild("base");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("glass", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
        partdefinition.addOrReplaceChild("cube", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
        partdefinition.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 16).addBox(-6.0F, 0.0F, -6.0F, 12.0F, 4.0F, 12.0F), PartPose.ZERO);
        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    public void render(CorruptedEndCrystal pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight) {
        pMatrixStack.pushPose();
        float f = getY(pEntity, pPartialTicks);
        float f1 = ((float)pEntity.time + pPartialTicks) * 3.0F;
        VertexConsumer vertexconsumer = pBuffer.getBuffer(RENDER_TYPE);
        pMatrixStack.pushPose();
        pMatrixStack.scale(2.0F, 2.0F, 2.0F);
        pMatrixStack.translate(0.0F, -0.5F, 0.0F);
        int i = OverlayTexture.NO_OVERLAY;
        if (pEntity.showsBottom()) {
            this.base.render(pMatrixStack, vertexconsumer, pPackedLight, i);
        }

        pMatrixStack.mulPose(Axis.YP.rotationDegrees(f1));
        pMatrixStack.translate(0.0F, 1.5F + f / 2.0F, 0.0F);
        pMatrixStack.mulPose((new Quaternionf()).setAngleAxis(((float)Math.PI / 3F), SIN_45, 0.0F, SIN_45));
        this.glass.render(pMatrixStack, vertexconsumer, pPackedLight, i);
        float f2 = 0.875F;
        pMatrixStack.scale(0.875F, 0.875F, 0.875F);
        pMatrixStack.mulPose((new Quaternionf()).setAngleAxis(((float)Math.PI / 3F), SIN_45, 0.0F, SIN_45));
        pMatrixStack.mulPose(Axis.YP.rotationDegrees(f1));
        this.glass.render(pMatrixStack, vertexconsumer, pPackedLight, i);
        pMatrixStack.scale(0.875F, 0.875F, 0.875F);
        pMatrixStack.mulPose((new Quaternionf()).setAngleAxis(((float)Math.PI / 3F), SIN_45, 0.0F, SIN_45));
        pMatrixStack.mulPose(Axis.YP.rotationDegrees(f1));
        this.cube.render(pMatrixStack, vertexconsumer, pPackedLight, i);
        pMatrixStack.popPose();
        pMatrixStack.popPose();
        BlockPos blockpos = pEntity.getBeamTarget();
        if (blockpos != null) {
            float f3 = (float)blockpos.getX() + 0.5F;
            float f4 = (float)blockpos.getY() + 0.5F;
            float f5 = (float)blockpos.getZ() + 0.5F;
            float f6 = (float)((double)f3 - pEntity.getX());
            float f7 = (float)((double)f4 - pEntity.getY());
            float f8 = (float)((double)f5 - pEntity.getZ());
            pMatrixStack.translate(f6, f7, f8);
            renderCrystalBeams(-f6, -f7 + f, -f8, pPartialTicks, pEntity.time, pMatrixStack, pBuffer, pPackedLight);
        }

        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }

    public static void renderCrystalBeams(float pX, float pY, float pZ, float pPartialTick, int pTickCount, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        float f = Mth.sqrt(pX * pX + pZ * pZ);
        float triang = pX * pX + pY * pY + pZ * pZ;
        float f1 = Mth.sqrt(triang);
        pPoseStack.pushPose();
        pPoseStack.translate(0.0F, 2.0F, 0.0F);
        pPoseStack.mulPose(Axis.YP.rotation((float)(-Math.atan2((double)pZ, (double)pX)) - ((float)Math.PI / 2F)));
        pPoseStack.mulPose(Axis.XP.rotation((float)(-Math.atan2((double)f, (double)pY)) - ((float)Math.PI / 2F)));
        VertexConsumer vertexconsumer = pBuffer.getBuffer(BEAM);
        float f2 = 0.0F - ((float)pTickCount + pPartialTick) * 0.01F;
        float f3 = Mth.sqrt(triang) / 32.0F - ((float)pTickCount + pPartialTick) * 0.01F;
        int i = 8;
        float f4 = 0.0F;
        float f5 = 0.75F;
        float f6 = 0.0F;
        PoseStack.Pose posestack$pose = pPoseStack.last();
        Matrix4f matrix4f = posestack$pose.pose();
        Matrix3f matrix3f = posestack$pose.normal();

        for(int j = 1; j <= 8; ++j) {
            float f7 = Mth.sin((float)j * ((float)Math.PI * 2F) / 8.0F) * 0.75F;
            float f8 = Mth.cos((float)j * ((float)Math.PI * 2F) / 8.0F) * 0.75F;
            float f9 = (float)j / 8.0F;
            vertexconsumer.vertex(matrix4f, f4 * 0.2F, f5 * 0.2F, 0.0F).color(0, 0, 0, 255).uv(f6, f2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(pPackedLight).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
            vertexconsumer.vertex(matrix4f, f4, f5, f1).color(255, 255, 255, 255).uv(f6, f3).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(pPackedLight).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
            vertexconsumer.vertex(matrix4f, f7, f8, f1).color(255, 255, 255, 255).uv(f9, f3).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(pPackedLight).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
            vertexconsumer.vertex(matrix4f, f7 * 0.2F, f8 * 0.2F, 0.0F).color(0, 0, 0, 255).uv(f9, f2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(pPackedLight).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
            f4 = f7;
            f5 = f8;
            f6 = f9;
        }

        pPoseStack.popPose();
    }

    public static float getY(CorruptedEndCrystal pEndCrystal, float pPartialTick) {
        float f = (float)pEndCrystal.time + pPartialTick;
        float f1 = Mth.sin(f * 0.2F) / 2.0F + 0.5F;
        f1 = (f1 * f1 + f1) * 0.4F;
        return f1 - 1.4F;
    }

    /**
     * Returns the location of an entity's texture.
     */
    public ResourceLocation getTextureLocation(CorruptedEndCrystal pEntity) {
        return END_CRYSTAL_LOCATION;
    }

    public boolean shouldRender(CorruptedEndCrystal pLivingEntity, Frustum pCamera, double pCamX, double pCamY, double pCamZ) {
        return super.shouldRender(pLivingEntity, pCamera, pCamX, pCamY, pCamZ) || pLivingEntity.getBeamTarget() != null;
    }
}