package insane96mcp.progressivebosses.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(EndDragonFight.class)
public class EndDragonFightMixin {

	@Shadow @Final private ServerLevel level;

	@Shadow @Nullable private BlockPos portalLocation;

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/dimension/end/EndDragonFight;spawnExitPortal(Z)V"), method = "respawnDragon")
	private void respawnDragon(List<EndCrystal> p_64092_, CallbackInfo callback) {
		List<EndCrystal> endCrystals = this.level.getEntitiesOfClass(EndCrystal.class, new AABB(this.portalLocation).inflate(48d), EndCrystal::showsBottom);
		for (EndCrystal endCrystal : endCrystals) {
			endCrystal.hurt(DamageSource.GENERIC, 1f);
		}
	}
}
