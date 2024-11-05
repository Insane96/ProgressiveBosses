package insane96mcp.progressivebosses.mixin;

import insane96mcp.progressivebosses.module.dragon.data.DragonStats;
import insane96mcp.progressivebosses.module.dragon.data.DragonStatsReloadListener;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Mixin(EndDragonFight.class)
public class EndDragonFightMixin {

	@Shadow @Final private ServerLevel level;

	@Shadow @Nullable private BlockPos portalLocation;

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/dimension/end/EndDragonFight;spawnExitPortal(Z)V"), method = "respawnDragon")
	private void respawnDragon(List<EndCrystal> p_64092_, CallbackInfo callback) {
		List<EndCrystal> endCrystals = this.level.getEntitiesOfClass(EndCrystal.class, new AABB(this.portalLocation).inflate(48d), EndCrystal::showsBottom);
		for (EndCrystal endCrystal : endCrystals) {
			endCrystal.level().explode(endCrystal, endCrystal.getX(), endCrystal.getY(), endCrystal.getZ(), 6.0F, Level.ExplosionInteraction.NONE);
			endCrystal.discard();
			level.setBlockAndUpdate(endCrystal.blockPosition(), Blocks.AIR.defaultBlockState());
		}
	}

	@Inject(method = "respawnDragon", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/dimension/end/EndDragonFight;spawnExitPortal(Z)V", shift = At.Shift.AFTER))
	public void onAboutToRespawnDragon(List<EndCrystal> pCrystals, CallbackInfo ci) {
		List<SpikeFeature.EndSpike> spikes = new ArrayList<>(SpikeFeature.getSpikesForLevel(this.level));
		//Order from smaller towers to bigger ones
		spikes.sort(Comparator.comparingInt(SpikeFeature.EndSpike::getRadius));
		//TODO change level based on crystal used to respawn
		DragonStats stats = DragonStatsReloadListener.STATS_MAP.get(0);

		int cages = stats.crystal.cages;

		//Reset all spikes
		for (SpikeFeature.EndSpike spike : spikes) {
			spike.guarded = false;
		}

		for (int i = 0; i < cages; i++) {
			if (i >= spikes.size())
				break;
			spikes.get(i).guarded = true;
		}
	}
}
