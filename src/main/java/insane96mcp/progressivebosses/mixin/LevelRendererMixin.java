package insane96mcp.progressivebosses.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
	@ModifyExpressionValue(method = "levelEvent", at = @At(value = "CONSTANT", args = "floatValue=64.0"))
    public float onPlayDragonSound(float original) {
        return 128f;
    }
}
