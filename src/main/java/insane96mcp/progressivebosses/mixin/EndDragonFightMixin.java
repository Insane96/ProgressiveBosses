package insane96mcp.progressivebosses.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import insane96mcp.progressivebosses.module.dragon.data.DragonStats;
import insane96mcp.progressivebosses.utils.LogHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import net.minecraft.world.phys.AABB;
import org.objectweb.asm.Opcodes;
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
import java.util.Optional;

@Mixin(EndDragonFight.class)
public class EndDragonFightMixin {

	@Shadow @Final private ServerLevel level;

	@Shadow @Nullable
    public BlockPos portalLocation;

	@Shadow @Nullable private List<EndCrystal> respawnCrystals;

	@Inject(method = "respawnDragon", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/dimension/end/EndDragonFight;respawnCrystals:Ljava/util/List;", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
	private void onAboutToRespawnDragon(List<EndCrystal> respawnCrystals, CallbackInfo callback) {
		List<EndCrystal> endCrystals = this.level.getEntitiesOfClass(EndCrystal.class, new AABB(this.portalLocation).inflate(48d), EndCrystal::showsBottom);
		for (EndCrystal endCrystal : endCrystals) {
			endCrystal.level().explode(endCrystal, endCrystal.getX(), endCrystal.getY(), endCrystal.getZ(), 6.0F, Level.ExplosionInteraction.NONE);
			endCrystal.discard();
			level.setBlockAndUpdate(endCrystal.blockPosition(), Blocks.AIR.defaultBlockState());
		}

		byte lvl = DragonFeature.getDragonLvl(respawnCrystals);
		DragonFeature.dragonLvl = lvl;

		//Setup caged pillars
		List<SpikeFeature.EndSpike> spikes = new ArrayList<>(SpikeFeature.getSpikesForLevel(this.level));
		//Order from smaller towers to bigger ones
		spikes.sort(Comparator.comparingInt(SpikeFeature.EndSpike::getRadius));
		Optional<DragonStats> stats = DragonFeature.getDragonStats(lvl);
		if (stats.isEmpty()) {
			LogHelper.warn("Failed to get Dragon Stats for level %s", lvl);
			return;
		}

		int cages = stats.get().crystal.cages;

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

	@ModifyExpressionValue(method = "setDragonKilled", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/dimension/end/EndDragonFight;previouslyKilled:Z", ordinal = 0))
	public boolean progressivebosses$onTryPlaceEgg(boolean previouslyKilled) {
		if (!Feature.isEnabled(DragonFeature.class)
				|| !DragonFeature.dragonEggPerDragon)
			return previouslyKilled;
		return false;
	}
}
