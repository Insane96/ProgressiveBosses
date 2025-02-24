package insane96mcp.progressivebosses.module.dragon.phase;

import com.mojang.logging.LogUtils;
import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import insane96mcp.progressivebosses.module.dragon.data.AcidballComponent;
import insane96mcp.progressivebosses.module.dragon.data.DragonDefinition;
import insane96mcp.progressivebosses.module.dragon.data.StrafePlayerComponent;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
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

public class PBDragonStrafePlayerPhase extends AbstractDragonPhaseInstance {
    private static EnderDragonPhase<PBDragonStrafePlayerPhase> PHASE;

    public static final Logger LOGGER = LogUtils.getLogger();
    public int fireballsToShoot = 0;
    public int fireballCharge;
    @Nullable
    public Path currentPath;
    @Nullable
    public Vec3 targetLocation;
    @Nullable
    public LivingEntity attackTarget;
    private boolean holdingPatternClockwise;

    public PBDragonStrafePlayerPhase(EnderDragon pDragon) {
        super(pDragon);
    }

    /**
     * Gives the phase a chance to update its status.
     * Called by dragon's onLivingUpdate. Only used when !worldObj.isRemote.
     */
    public void doServerTick() {
        DragonDefinition definition = DragonFeature.getDragonDefinition(this.dragon).orElse(null);
        if (definition == null)
            return;
        StrafePlayerComponent component = definition.getComponent(StrafePlayerComponent.class).orElse(null);
        if (component == null)
            return;
        if (this.attackTarget == null) {
            LOGGER.warn("Skipping player strafe phase because no player was found");
            this.dragon.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
            return;
        }
        if (this.currentPath != null && this.currentPath.isDone()) {
            double x = this.attackTarget.getX();
            double z = this.attackTarget.getZ();
            double dX = x - this.dragon.getX();
            double dZ = z - this.dragon.getZ();
            double dSqrt = Math.sqrt(dX * dX + dZ * dZ);
            //double yOffset = Math.min(0.4f + dSqrt / 80.0d - 1.0d, 15d);
            this.targetLocation = new Vec3(x, this.attackTarget.getY() + 15, z);
        }

        double d12 = this.targetLocation == null ? 0.0D : this.targetLocation.distanceToSqr(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
        if (d12 < 15 * 15 || d12 > 150 * 150)
            this.findNewTarget();

        if (this.attackTarget.distanceToSqr(this.dragon) >= 9216
                || !this.dragon.hasLineOfSight(this.attackTarget)) {
            if (this.fireballCharge > 0)
                --this.fireballCharge;
            return;
        }
        ++this.fireballCharge;
        Vec3 targetDirection = (new Vec3(this.attackTarget.getX() - this.dragon.getX(), 0.0D, this.attackTarget.getZ() - this.dragon.getZ())).normalize();
        Vec3 dragonViewDirection = (new Vec3(Mth.sin(this.dragon.getYRot() * ((float)Math.PI / 180F)), 0.0D, (-Mth.cos(this.dragon.getYRot() * ((float)Math.PI / 180F))))).normalize();
        float dot = (float) dragonViewDirection.dot(targetDirection);
        float angleToTarget = (float)(Math.acos(dot) * (double)(180F / (float)Math.PI));
        angleToTarget += 0.5F;
        if (this.fireballCharge >= 5 && angleToTarget >= 0.0F && angleToTarget < 12.5F) {
            int fired = component.acidballPerShot.getIntValue(this.dragon);
            AcidballComponent acidballComponent = definition.getComponent(AcidballComponent.class).orElse(null);
            for (int i = 0; i < fired; i++)
                summonAcidball(acidballComponent);
            this.fireballCharge = 5 - component.cooldownBetweenShots.getIntValue(this.dragon);

            if (--this.fireballsToShoot <= 0)
                this.dragon.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
        }
    }

    private void summonAcidball(@Nullable AcidballComponent component) {
        Vec3 vec32 = this.dragon.getViewVector(1.0F);
        double headXOffset = this.dragon.head.getX() - vec32.x;
        double headYOffset = this.dragon.head.getY(0.5D) + 0.5D;
        double headZOffset = this.dragon.head.getZ() - vec32.z;
        double targetXOffset = this.attackTarget.getX() + Mth.randomBetween(this.dragon.getRandom(), -3f, 3f) - headXOffset;
        double targetYOffset = this.attackTarget.getY() - headYOffset;
        double targetZOffset = this.attackTarget.getZ() + Mth.randomBetween(this.dragon.getRandom(), -3f, 3f) - headZOffset;
        if (!this.dragon.isSilent())
            this.dragon.level().levelEvent(null, 1017, this.dragon.blockPosition(), 0);

        DragonFireball dragonfireball = new DragonFireball(this.dragon.level(), this.dragon, targetXOffset, targetYOffset, targetZOffset);
        if (component != null && component.speedMultiplier != null) {
            float speedMultiplier = component.speedMultiplier.getValue(this.dragon);
            dragonfireball.xPower *= speedMultiplier;
            dragonfireball.yPower *= speedMultiplier;
            dragonfireball.zPower *= speedMultiplier;
        }
        dragonfireball.moveTo(headXOffset, headYOffset, headZOffset, 0.0F, 0.0F);
        this.dragon.level().addFreshEntity(dragonfireball);
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

            Node andThen = this.attackTarget != null ? new Node(this.attackTarget.getBlockX(), this.attackTarget.getBlockY(), this.attackTarget.getBlockZ()) : null;
            this.currentPath = this.dragon.findPath(closestNode, newNode, andThen);
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
        this.fireballCharge = 0;
        this.targetLocation = null;
        this.currentPath = null;
        this.attackTarget = null;

        DragonDefinition definition = DragonFeature.getDragonDefinition(this.dragon).orElse(null);
        if (definition == null)
            return;
        StrafePlayerComponent component = definition.getComponent(StrafePlayerComponent.class).orElse(null);
        if (component == null)
            return;
        this.fireballsToShoot = component.getAcidballShot(this.dragon, this.dragon.getRandom());

        Player player = DragonFeature.getRandomPlayer(dragon, dragon.level(), 96);
        if (player == null)
            return;
        this.setTarget(player);
    }

    public void setTarget(@NotNull LivingEntity pAttackTarget) {
        this.attackTarget = pAttackTarget;
        int closestNode = this.dragon.findClosestNode();
        int closestTargetNode = this.dragon.findClosestNode(this.attackTarget.getX(), this.attackTarget.getY(), this.attackTarget.getZ());
        int x = this.attackTarget.getBlockX();
        int z = this.attackTarget.getBlockZ();
        double dX = (double)x - this.dragon.getX();
        double dZ = (double)z - this.dragon.getZ();
        double dSqr = Math.sqrt(dX * dX + dZ * dZ);
        double offsetY = Math.min(0.4f + dSqr / 80.0d - 1.0d, 10.5d);
        int y = Mth.floor(this.attackTarget.getY() + offsetY);
        Node node = new Node(x, y, z);
        this.currentPath = this.dragon.findPath(closestNode, closestTargetNode, node);
        if (this.currentPath != null) {
            this.currentPath.advance();
            this.navigateToNextPathNode();
        }
    }

    /**
     * Returns the location the dragon is flying toward
     */
    @Nullable
    public Vec3 getFlyTargetLocation() {
        return this.targetLocation;
    }

    public EnderDragonPhase<PBDragonStrafePlayerPhase> getPhase() {
        return PHASE;
    }

    public static EnderDragonPhase<PBDragonStrafePlayerPhase> getPhaseType() {
        return PHASE;
    }

    public static void init() {
        PHASE = EnderDragonPhase.create(PBDragonStrafePlayerPhase.class, "PBStrafePlayer");
    }
}