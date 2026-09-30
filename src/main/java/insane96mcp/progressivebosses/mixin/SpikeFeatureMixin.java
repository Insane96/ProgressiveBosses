package insane96mcp.progressivebosses.mixin;

import com.mojang.serialization.Codec;
import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.SpikeConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpikeFeature.class)
public abstract class SpikeFeatureMixin extends Feature<SpikeConfiguration> {

	public SpikeFeatureMixin(Codec<SpikeConfiguration> pCodec) {
		super(pCodec);
	}

	// Placed at the 4 lower corners of the guard cage (k,l in {-2,2}, i1 == 0 in vanilla's loop), computed
	// directly from `spike` instead of capturing vanilla's loop locals: those locals proved fragile to
	// capture by name/ordinal across NeoForge/compiler toolchain differences (see mixin_local_lvt_gotcha memory).
	@Inject(method = "placeSpike", at = @At("TAIL"))
	public void progressivebosses$placeSpikeBaseObsidian(ServerLevelAccessor level, RandomSource random, SpikeConfiguration config, SpikeFeature.EndSpike spike, CallbackInfo ci) {
		if (!DragonFeature.areFixesEnabled() || !spike.isGuarded())
			return;
		for (int x : new int[]{-2, 2}) {
			for (int z : new int[]{-2, 2}) {
				this.setBlock(level, new BlockPos(spike.getCenterX() + x, spike.getHeight() - 1, spike.getCenterZ() + z), Blocks.OBSIDIAN.defaultBlockState());
			}
		}
	}
}
