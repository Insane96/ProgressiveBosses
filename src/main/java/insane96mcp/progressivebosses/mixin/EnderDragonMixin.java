package insane96mcp.progressivebosses.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import insane96mcp.progressivebosses.module.dragon.data.DragonStats;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(EnderDragon.class)
public class EnderDragonMixin extends Mob {
	protected EnderDragonMixin(EntityType<? extends Mob> type, Level worldIn) {
		super(type, worldIn);
	}

	@ModifyExpressionValue(method = "checkCrystals", at = @At(value = "CONSTANT", args = "floatValue=1.0"))
	public float onCrystalHeal(float original) {
		Optional<DragonStats> stats = DragonFeature.getDragonStats((EnderDragon) (Object) this);
		//Divided by 2 because it's healed twice per second
        return stats.map(dragonStats -> dragonStats.health.crystalRegeneration / 2f).orElse(original);
    }
}
