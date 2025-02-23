package insane96mcp.progressivebosses.module.dragon.phase;

import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import insane96mcp.progressivebosses.module.dragon.data.DragonDefinition;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class PBDragonHoldingPatternPhase extends AbstractDragonPhaseInstance {
    private static EnderDragonPhase<PBDragonHoldingPatternPhase> PHASE;

    @Nullable
    public Path currentPath;
    @Nullable
    private Vec3 targetLocation;
    private boolean clockwise;

    public PBDragonHoldingPatternPhase(EnderDragon pDragon) {
        super(pDragon);
    }

    public void doServerTick() {
        double distance = this.targetLocation == null ? 0.0D : this.targetLocation.distanceToSqr(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
        if (distance < 100.0D || distance > 22500.0D || this.dragon.horizontalCollision || this.dragon.verticalCollision)
            this.findNewTarget();
    }

    public void begin() {
        this.currentPath = null;
        this.targetLocation = null;
    }

    @Nullable
    public Vec3 getFlyTargetLocation() {
        return this.targetLocation;
    }

    private void findNewTarget() {
        if (this.currentPath != null && this.currentPath.isDone()) {
            DragonDefinition stats = DragonFeature.getDragonDefinition(this.dragon).orElse(null);
            if (stats == null)
                return;

            this.dragon.getPhaseManager().setPhase(EnderDragonPhase.LANDING_APPROACH);
        }

        if (this.currentPath == null || this.currentPath.isDone()) {
            int j = this.dragon.findClosestNode();
            int k = j;
            if (this.dragon.getRandom().nextInt(8) == 0) {
                this.clockwise = !this.clockwise;
                k = j + 6;
            }

            if (this.clockwise) {
                ++k;
            } else {
                --k;
            }

            if (this.dragon.getDragonFight() != null && this.dragon.getDragonFight().getCrystalsAlive() >= 0) {
                k %= 12;
                if (k < 0) {
                    k += 12;
                }
            } else {
                k -= 12;
                k &= 7;
                k += 12;
            }

            this.currentPath = this.dragon.findPath(j, k, null);
            if (this.currentPath != null) {
                this.currentPath.advance();
            }
        }

        this.navigateToNextPathNode();
    }

    private void navigateToNextPathNode() {
        if (this.currentPath != null && !this.currentPath.isDone()) {
            Vec3i vec3i = this.currentPath.getNextNodePos();
            this.currentPath.advance();
            double d0 = vec3i.getX();
            double d1 = vec3i.getZ();

            double d2;
            do {
                d2 = ((float)vec3i.getY() + this.dragon.getRandom().nextFloat() * 20.0F);
            } while(d2 < (double)vec3i.getY());

            this.targetLocation = new Vec3(d0, d2, d1);
        }

    }

    public EnderDragonPhase<PBDragonHoldingPatternPhase> getPhase() {
        return PHASE;
    }

    public static EnderDragonPhase<PBDragonHoldingPatternPhase> getPhaseType() {
        return PHASE;
    }

    public static void init() {
        PHASE = EnderDragonPhase.create(PBDragonHoldingPatternPhase.class, "PBHoldPattern");
    }
}