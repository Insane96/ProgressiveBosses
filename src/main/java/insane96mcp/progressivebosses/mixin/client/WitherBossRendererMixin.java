package insane96mcp.progressivebosses.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.base.Strings;
import insane96mcp.progressivebosses.module.wither.feature.AttackFeature;
import net.minecraft.client.renderer.entity.WitherBossRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WitherBossRenderer.class)
public class WitherBossRendererMixin {

	private static final ResourceLocation WITHER_CHARGE_LOCATION = new ResourceLocation(ProgressiveBosses.MOD_ID, "textures/entity/wither/wither_charge.png");

	@Inject(at = @At("RETURN"), method = "getTextureLocation(Lnet/minecraft/world/entity/boss/wither/WitherBoss;)Lnet/minecraft/resources/ResourceLocation;", cancellable = true)
	public void getTextureLocation(WitherBoss wither, CallbackInfoReturnable<ResourceLocation> cir) {
		if (wither.getPersistentData().getByte(Strings.Tags.CHARGE_ATTACK) > 0)
			cir.setReturnValue(WITHER_CHARGE_LOCATION);
	}

	@Inject(at = @At("TAIL"), method = "scale(Lnet/minecraft/world/entity/boss/wither/WitherBoss;Lcom/mojang/blaze3d/vertex/PoseStack;F)V")
	public void scale(WitherBoss wither, PoseStack poseStack, float partialTick, CallbackInfo ci) {
		byte chargeTick = wither.getPersistentData().getByte(Strings.Tags.CHARGE_ATTACK);
		if (chargeTick > 0)
		{
			float scale = 1f;
			scale += (AttackFeature.Consts.CHARGE_ATTACK_TICK_START - ((float)chargeTick - partialTick)) * 0.004f;
			poseStack.scale(scale, scale, scale);
		}
	}
}
