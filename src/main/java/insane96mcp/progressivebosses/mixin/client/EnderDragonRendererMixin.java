package insane96mcp.progressivebosses.mixin.client;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.module.dragon.corruptedendcrystal.CorruptedEndCrystal;
import insane96mcp.progressivebosses.module.dragon.corruptedendcrystal.CorruptedEndCrystalRenderer;
import insane96mcp.progressivebosses.module.dragon.data.AngerComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderDragonRenderer.class)
public class EnderDragonRendererMixin {
    @Unique
    private static final ResourceLocation ANGERED_EYES_LOCATION = new ResourceLocation(ProgressiveBosses.MOD_ID, "textures/entity/enderdragon/angered_dragon_eyes.png");
    @Unique
    private static final RenderType ANGERED_EYES = RenderType.eyes(ANGERED_EYES_LOCATION);

    @Unique
    private static boolean progressiveBosses$isCorrupted;

    @Inject(method = "render(Lnet/minecraft/world/entity/boss/enderdragon/EnderDragon;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", ordinal = 2))
    private void progressivebosses$saveIfCorrupted(EnderDragon pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight, CallbackInfo ci) {
        progressiveBosses$isCorrupted = pEntity.nearestCrystal instanceof CorruptedEndCrystal;
    }

    @ModifyArg(method = "renderCrystalBeams", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
    private static RenderType progressivebosses$saveIfCorrupted(RenderType pRenderType) {
        if (progressiveBosses$isCorrupted) {
            progressiveBosses$isCorrupted = false;
            return CorruptedEndCrystalRenderer.BEAM;
        }
        return pRenderType;
    }

    @Definition(id = "pBuffer", local = @Local(type = MultiBufferSource.class, argsOnly = true))
    @Definition(id = "getBuffer", method = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;")
    @Definition(id = "EYES", field = "Lnet/minecraft/client/renderer/entity/EnderDragonRenderer;EYES:Lnet/minecraft/client/renderer/RenderType;")
    @Expression("pBuffer.getBuffer(EYES)")
    @ModifyExpressionValue(method = "render(Lnet/minecraft/world/entity/boss/enderdragon/EnderDragon;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    private VertexConsumer progressivebosses$changeEyesIfAngered(VertexConsumer original, @Local(argsOnly = true) MultiBufferSource pBuffer, @Local(argsOnly = true) EnderDragon dragon) {
        return AngerComponent.isAngered(dragon) ? pBuffer.getBuffer(ANGERED_EYES) : original;
    }
}
