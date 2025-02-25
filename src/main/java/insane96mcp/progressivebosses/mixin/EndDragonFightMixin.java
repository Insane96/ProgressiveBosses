package insane96mcp.progressivebosses.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import insane96mcp.progressivebosses.module.dragon.data.DragonDefinition;
import insane96mcp.progressivebosses.module.dragon.data.LootComponent;
import insane96mcp.progressivebosses.module.dragon.data.SpikesComponent;
import insane96mcp.progressivebosses.utils.LogHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
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

@Mixin(EndDragonFight.class)
public class EndDragonFightMixin {

	@Shadow @Final private ServerLevel level;

	@Shadow @Nullable
    public BlockPos portalLocation;

	@Shadow @Nullable private List<EndCrystal> respawnCrystals;

	@Shadow private boolean dragonKilled;

	@Shadow @Final private ServerBossEvent dragonEvent;

	@Inject(method = "respawnDragon", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/dimension/end/EndDragonFight;respawnCrystals:Ljava/util/List;", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
	private void onAboutToRespawnDragon(List<EndCrystal> respawnCrystals, CallbackInfo callback) {
		List<EndCrystal> endCrystals = this.level.getEntitiesOfClass(EndCrystal.class, new AABB(this.portalLocation).inflate(48d), EndCrystal::showsBottom);
		for (EndCrystal endCrystal : endCrystals) {
			endCrystal.level().explode(endCrystal, endCrystal.getX(), endCrystal.getY(), endCrystal.getZ(), 6.0F, Level.ExplosionInteraction.NONE);
			endCrystal.discard();
			level.setBlockAndUpdate(endCrystal.blockPosition(), Blocks.AIR.defaultBlockState());
		}

		byte lvl = DragonFeature.getDragonLvl(respawnCrystals);
		DragonDefinition definition = DragonFeature.getDragonDefinition(lvl).orElse(null);
		if (definition == null) {
			LogHelper.warn("Failed to get Dragon Definition for level %s. Summoning a lvl 0", lvl);
			lvl = 0;
			definition = DragonFeature.getDragonDefinition(lvl).orElse(null);
			if (definition == null) {
				LogHelper.warn("Failed to get Dragon Definition for level %s. Dragon will be vanilla", lvl);
				return;
			}
		}
		DragonFeature.dragonLvl = lvl;
		SpikesComponent component = definition.getComponent(SpikesComponent.class).orElse(null);
		if (component == null)
			return;

		//Setup caged pillars
		List<SpikeFeature.EndSpike> spikes = new ArrayList<>(SpikeFeature.getSpikesForLevel(this.level));
		//Order from smaller towers to bigger ones
		spikes.sort(Comparator.comparingInt(SpikeFeature.EndSpike::getRadius));

		//Reset all spikes
		for (SpikeFeature.EndSpike spike : spikes) {
			spike.guarded = false;
		}

		for (int i = 0; i < component.cages; i++) {
			if (i >= spikes.size())
				break;
			spikes.get(i).guarded = true;
		}
	}

	/// Control egg drop via Definition
	@ModifyExpressionValue(method = "setDragonKilled", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/dimension/end/EndDragonFight;previouslyKilled:Z", ordinal = 0))
	public boolean progressivebosses$onTryPlaceEgg(boolean previouslyKilled, EnderDragon dragon) {
		if (!Feature.isEnabled(DragonFeature.class))
			return previouslyKilled;
		boolean shouldDropEgg = DragonFeature.getDragonDefinition(dragon)
				.flatMap(definition -> definition.getComponent(LootComponent.class))
				.map(component -> component.dropsEgg)
				.orElse(previouslyKilled);
		return !shouldDropEgg;
	}

	@Inject(method = "onCrystalDestroyed", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/dimension/end/EndDragonFight;updateCrystalCount()V", shift = At.Shift.AFTER))
	public void progressivebosses$onTryPlaceEgg(EndCrystal pCrystal, DamageSource pDmgSrc, CallbackInfo ci) {
		DragonFeature.onCrystalDestroyed((EndDragonFight) (Object) this, pCrystal, pDmgSrc);
	}
}
