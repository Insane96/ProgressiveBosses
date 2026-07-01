package insane96mcp.progressivebosses.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import insane96mcp.progressivebosses.module.dragon.corruptedendcrystal.CorruptedEndCrystal;
import insane96mcp.progressivebosses.module.dragon.data.SpikesComponent;
import insane96mcp.progressivebosses.setup.PBEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(targets = "net/minecraft/world/level/dimension/end/DragonRespawnAnimation$3")
public class DragonRespawnAnimationSummoningPillarsMixin {
    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/feature/Feature;place(Lnet/minecraft/world/level/levelgen/feature/configurations/FeatureConfiguration;Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Z", shift = At.Shift.AFTER))
    public void progressivebosses$corruptSpikeCrystal(ServerLevel serverLevel, EndDragonFight endDragonFight, List<EndCrystal> crystals, int respawningTicks, BlockPos pos, CallbackInfo ci, @Local SpikeFeature.EndSpike endSpike) {
        float chance = DragonFeature.getDragonDefinition(DragonFeature.dragonLvl)
                .flatMap(dragonDefinition -> dragonDefinition.getComponent(SpikesComponent.class))
                .map(spikesComponent -> spikesComponent.corruptedChance)
                .orElse(0f);
        if (chance <= 0 || serverLevel.getRandom().nextFloat() >= chance)
            return;

        EndCrystal crystal = serverLevel.getEntitiesOfClass(EndCrystal.class, endSpike.getTopBoundingBox())
                .stream().findFirst().orElse(null);
        if (crystal == null)
            return;
        CorruptedEndCrystal corruptedEndCrystal = PBEntities.CORRUPTED_END_CRYSTAL.get().create(serverLevel);
        corruptedEndCrystal.setShowBottom(crystal.showsBottom());
        corruptedEndCrystal.setPos(crystal.position());
        corruptedEndCrystal.setBeamTarget(crystal.getBeamTarget());
        crystal.discard();
        serverLevel.addFreshEntity(corruptedEndCrystal);
    }
}
