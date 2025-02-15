package insane96mcp.progressivebosses.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import insane96mcp.progressivebosses.module.dragon.corruptedendcrystal.CorruptedEndCrystal;
import insane96mcp.progressivebosses.module.dragon.corruptedendcrystal.CorruptedEndCrystalRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
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
}
