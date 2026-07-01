package insane96mcp.progressivebosses.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import javax.annotation.Nullable;

@Mixin(EndDragonFight.class)
public abstract class EndDragonFightPreventSpawnMixin {

	@Shadow private boolean dragonKilled;

	@Shadow @Final private ServerLevel level;

	@Shadow @Nullable public BlockPos portalLocation;

	@Definition(id = "dragonKilled", field = "Lnet/minecraft/world/level/dimension/end/EndDragonFight;dragonKilled:Z")
	@Expression("this.dragonKilled = false")
	@WrapOperation(method = "scanState", at = @At(value = "MIXINEXTRAS:EXPRESSION", ordinal = 1))
	private void progressivebosses$spawnGuardianCrystals(EndDragonFight instance, boolean value, Operation<Void> original) {
		this.dragonKilled = true;
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			EndCrystal crystal = EntityType.END_CRYSTAL.create(this.level);
			crystal.setInvulnerable(true);
			crystal.setShowBottom(false);
			crystal.setPos(this.portalLocation.getCenter().relative(direction, 3).add(0, 1, 0));
			this.level.addFreshEntity(crystal);
		}
		DragonFeature.spawnDragon = true;
		//this.tryRespawn();
	}

	@Definition(id = "enderdragon", local = @Local(type = EnderDragon.class))
	@Expression("enderdragon != null")
	@WrapOperation(method = "setRespawnStage", at = @At("MIXINEXTRAS:EXPRESSION"))
	public boolean progressivebosses$preventDragonAdvancement(Object left, Object right, Operation<Boolean> original) {
		if (DragonFeature.preventAdvancement) {
			DragonFeature.preventAdvancement = false;
			return false;
		}
		return original.call(left, right);
	}
}
