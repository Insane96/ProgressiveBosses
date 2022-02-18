package insane96mcp.progressivebosses.mixin.client;

import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.base.Strings;
import net.minecraft.client.renderer.entity.WitherBossRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WitherBossRenderer.class)
public class WitherBossRendererMixin {

	private static final ResourceLocation WITHER_CHARGE_LOCATION = new ResourceLocation(ProgressiveBosses.MOD_ID, "textures/entity/wither/wither_charge.png");

	@Inject(at = @At("RETURN"), method = "getTextureLocation(Lnet/minecraft/world/entity/boss/wither/WitherBoss;)Lnet/minecraft/resources/ResourceLocation;", cancellable = true)
	public void getTextureLocation(WitherBoss wither, CallbackInfoReturnable<ResourceLocation> cir) {
		if (wither.getPersistentData().getBoolean(Strings.Tags.CHARGE_ATTACK))
			cir.setReturnValue(WITHER_CHARGE_LOCATION);
	}
}
