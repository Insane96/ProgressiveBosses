package insane96mcp.progressivebosses.module.dragon.phase;

import com.mojang.logging.LogUtils;
import insane96mcp.progressivebosses.event.DragonPhaseEvent;
import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import insane96mcp.progressivebosses.module.dragon.data.DragonAttack;
import insane96mcp.progressivebosses.module.dragon.data.DragonStats;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.Optional;

public class DragonStrafePillarPhase extends AbstractDragonPhaseInstance {
    private static EnderDragonPhase<DragonStrafePillarPhase> STRAFE_PILLAR;

    public static final Logger LOGGER = LogUtils.getLogger();
    @Nullable
    public Path currentPath;
    @Nullable
    public Vec3 targetLocation;
    @Nullable
    public Entity pillarTarget;
    private boolean holdingPatternClockwise;

    public DragonStrafePillarPhase(EnderDragon pDragon) {
        super(pDragon);
    }

    /**
     * Gives the phase a chance to update its status.
     * Called by dragon's onLivingUpdate. Only used when !worldObj.isRemote.
     */
    public void doServerTick() {
        if (this.pillarTarget == null || !this.pillarTarget.isAlive()) {
            LOGGER.warn("Skipping pillar strafe phase because no crystal was found or was dead");
            this.dragon.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
            return;
        }
        if (this.currentPath != null && this.currentPath.isDone()) {
            this.targetLocation = new Vec3(this.pillarTarget.getX(), this.pillarTarget.getY() + 15, this.pillarTarget.getZ());
        }

        double distanceToTarget = this.targetLocation == null ? 0.0D : this.targetLocation.distanceToSqr(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
        if (distanceToTarget > 80 * 80)
            this.findNewTarget();

        Vec3 targetDirection = (new Vec3(this.pillarTarget.getX() - this.dragon.getX(), 0.0D, this.pillarTarget.getZ() - this.dragon.getZ())).normalize();
        Vec3 dragonViewDirection = (new Vec3(Mth.sin(this.dragon.getYRot() * ((float)Math.PI / 180F)), 0.0D, (-Mth.cos(this.dragon.getYRot() * ((float)Math.PI / 180F))))).normalize();
        float dot = (float)dragonViewDirection.dot(targetDirection);
        float angleToTarget = (float)(Math.acos(dot) * (double)(180F / (float)Math.PI));
        angleToTarget += 0.5F;
        double dX = this.pillarTarget.getX() - this.dragon.getX();
        double dZ = this.pillarTarget.getZ() - this.dragon.getZ();
        double distanceXZ = Math.sqrt(dX * dX + dZ * dZ);
        double distanceY = Math.abs(this.pillarTarget.getY() - this.dragon.getY());
        if (angleToTarget >= 0.0F && angleToTarget < 10.0F && distanceXZ > 15 & distanceY > 5) {
            Vec3 vec32 = this.dragon.getViewVector(1.0F);
            double headXOffset = this.dragon.head.getX() - vec32.x;
            double headYOffset = this.dragon.head.getY(0.5D) + 0.5D;
            double headZOffset = this.dragon.head.getZ() - vec32.z;
            if (!this.dragon.isSilent())
                this.dragon.level().levelEvent(null, 1017, this.dragon.blockPosition(), 0);

            for (int i = 0; i < 2; i++) {
                double targetXOffset = this.pillarTarget.getX() + Mth.randomBetween(this.dragon.getRandom(), -4f, 4f) - headXOffset;
                double targetYOffset = this.pillarTarget.getY() - headYOffset;
                double targetZOffset = this.pillarTarget.getZ() + Mth.randomBetween(this.dragon.getRandom(), -4f, 4f) - headZOffset;
                DragonFireball dragonfireball = new DragonFireball(this.dragon.level(), this.dragon, targetXOffset, targetYOffset, targetZOffset);
                DragonAttack.setAcidBallSpeedMultiplier(dragonfireball);
                dragonfireball.moveTo(headXOffset, headYOffset, headZOffset, 0.0F, 0.0F);
                this.dragon.level().addFreshEntity(dragonfireball);
            }

            this.dragon.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
        }
        else
            this.findNewTarget();
    }

    public void findNewTarget() {
        if (this.currentPath == null || this.currentPath.isDone()) {
            int closestNode = this.dragon.findClosestNode();
            int newNode = closestNode;
            if (this.dragon.getRandom().nextInt(8) == 0) {
                this.holdingPatternClockwise = !this.holdingPatternClockwise;
                newNode = closestNode + 6;
            }

            if (this.holdingPatternClockwise) {
                ++newNode;
            } else {
                --newNode;
            }

            if (this.dragon.getDragonFight() != null && this.dragon.getDragonFight().getCrystalsAlive() > 0) {
                newNode %= 12;
                if (newNode < 0) {
                    newNode += 12;
                }
            }
            else {
                newNode -= 12;
                newNode &= 7;
                newNode += 12;
            }

            this.currentPath = this.dragon.findPath(closestNode, newNode, null);
            if (this.currentPath != null) {
                this.currentPath.advance();
            }
        }

        this.navigateToNextPathNode();
    }

    private void navigateToNextPathNode() {
        if (this.currentPath != null && !this.currentPath.isDone()) {
            Vec3i nextNodePos = this.currentPath.getNextNodePos();
            this.currentPath.advance();
            double x = nextNodePos.getX();
            double z = nextNodePos.getZ();

            double y;
            do {
                y = ((float)nextNodePos.getY() + this.dragon.getRandom().nextFloat() * 20.0F);
            } while (y < (double)nextNodePos.getY());

            this.targetLocation = new Vec3(x, y, z);
        }

    }

    /**
     * Called when this phase is set to active
     */
    public void begin() {
        this.targetLocation = null;
        this.currentPath = null;
        this.pillarTarget = null;

        Optional<DragonStats> stats = DragonFeature.getDragonStats(this.dragon);
        if (stats.isEmpty())
            return;
        //TODO
    }

    public void setTargetFromPlayer(@NotNull Player player) {
        this.pillarTarget = getNearestCrystal(player);
        if (this.pillarTarget == null)
            return;
        int closestNode = this.dragon.findClosestNode();
        int closestTargetNode = this.dragon.findClosestNode(this.pillarTarget.getX(), this.pillarTarget.getY(), this.pillarTarget.getZ());
        int x = this.pillarTarget.getBlockX();
        int z = this.pillarTarget.getBlockZ();
        double dX = (double)x - this.dragon.getX();
        double dZ = (double)z - this.dragon.getZ();
        double dSqr = Math.sqrt(dX * dX + dZ * dZ);
        double offsetY = Math.min(0.4f + dSqr / 80.0d - 1.0d, 10.5d);
        int y = Mth.floor(this.pillarTarget.getY() + offsetY);
        Node node = new Node(x, y, z);
        this.currentPath = this.dragon.findPath(closestNode, closestTargetNode, node);
        if (this.currentPath != null) {
            this.currentPath.advance();
            this.navigateToNextPathNode();
        }
    }

    public EndCrystal getNearestCrystal(Entity entity) {
        double distance = Double.MAX_VALUE;
        EndCrystal nearestCrystal = null;
        for (EndCrystal crystal : entity.level().getEntitiesOfClass(EndCrystal.class, entity.getBoundingBox().inflate(64), EndCrystal::showsBottom)) {
            double newDistance = entity.distanceTo(crystal);
            if (newDistance < distance) {
                nearestCrystal = crystal;
                distance = newDistance;
            }
        }
        return nearestCrystal;
    }

    /**
     * Returns the location the dragon is flying toward
     */
    @Nullable
    public Vec3 getFlyTargetLocation() {
        return this.targetLocation;
    }

    public EnderDragonPhase<DragonStrafePillarPhase> getPhase() {
        return STRAFE_PILLAR;
    }

    public static EnderDragonPhase<DragonStrafePillarPhase> getPhaseType() {
        return STRAFE_PILLAR;
    }

    public static void init() {
        STRAFE_PILLAR = EnderDragonPhase.create(DragonStrafePillarPhase.class, "PBStrafePillar");
    }

    public static void convertToPBStrafe(DragonPhaseEvent event) {

    }
}