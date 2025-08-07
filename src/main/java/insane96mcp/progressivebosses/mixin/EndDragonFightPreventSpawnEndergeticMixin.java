package insane96mcp.progressivebosses.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.teamabnormals.endergetic.common.levelgen.EndergeticDragonFightManager;
import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import insane96mcp.progressivebosses.utils.LogHelper;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EndergeticDragonFightManager.class)
public abstract class EndDragonFightPreventSpawnEndergeticMixin extends EndDragonFight {

    public EndDragonFightPreventSpawnEndergeticMixin(ServerLevel pLevel, long pSeed, Data pData) {
        super(pLevel, pSeed, pData);
    }

    @Definition(id = "dragonKilled", field = "Lcom/teamabnormals/endergetic/common/levelgen/EndergeticDragonFightManager;dragonKilled:Z", remap = true)
    @Expression("this.dragonKilled = false")
    @WrapOperation(method = "scanForLegacyFight", at = @At(value = "MIXINEXTRAS:EXPRESSION", ordinal = 1), remap = false)
    private void onAboutToRespawnDragon(EndergeticDragonFightManager instance, boolean value, Operation<Void> original) {
        if (this.portalLocation == null)
            return;
        ((EndDragonFightAccessor)this).setDragonKilled(true);
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			EndCrystal crystal = EntityType.END_CRYSTAL.create(((EndDragonFightAccessor)this).getLevel());
            if (crystal == null) {
                LogHelper.warn("Can't create end crystal for first dragon spawn. This shouldn't happen");
                continue;
            }
			crystal.setInvulnerable(true);
			crystal.setShowBottom(false);
			crystal.setPos(this.portalLocation.getCenter().relative(direction, 3).add(0, 1, 0));
            ((EndDragonFightAccessor)this).getLevel().addFreshEntity(crystal);
		}
		DragonFeature.spawnDragon = true;
		//this.tryRespawn();
	}
}
