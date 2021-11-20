package insane96mcp.progressivebosses.module.wither.ai;

import insane96mcp.progressivebosses.base.Strings;
import insane96mcp.progressivebosses.module.wither.feature.AttackFeature;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.server.ServerWorld;

import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class WitherChargeAttackGoal extends Goal {
	private final WitherEntity wither;
	private LivingEntity target;
	private Vector3d targetPos;
	private double lastDistanceFromTarget = 0d;

	public WitherChargeAttackGoal(WitherEntity wither) {
		this.wither = wither;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP, Goal.Flag.LOOK, Flag.TARGET));
	}

	/**
	 * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
	 * method as well.
	 */
	public boolean canUse() {
		if (this.wither.getInvulnerableTicks() != AttackFeature.Consts.CHARGE_ATTACK_TICK_START)
			return false;
		CompoundNBT witherTags = wither.getPersistentData();
		return witherTags.contains(Strings.Tags.CHARGE_ATTACK);
	}

	public void start() {
		this.wither.getNavigation().stop();
		for (int h = 0; h < 3; h++)
			this.wither.setAlternativeTarget(h, 0);
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	public boolean canContinueToUse() {
		return this.wither.getInvulnerableTicks() > 0;
	}

	/**
	 * Reset the task's internal state. Called when this task is interrupted by another one
	 */
	public void stop() {
		this.target = null;
		CompoundNBT witherTags = wither.getPersistentData();
		witherTags.remove(Strings.Tags.CHARGE_ATTACK);
		this.wither.setDeltaMovement(this.wither.getDeltaMovement().multiply(0.02d, 0.02d, 0.02d));
		this.lastDistanceFromTarget = 0d;
		this.targetPos = null;
	}

	/**
	 * Keep ticking a continuous task that has already been started
	 */
	public void tick() {
		if (this.wither.getInvulnerableTicks() > AttackFeature.Consts.CHARGE_ATTACK_TICK_CHARGE)
			this.wither.setDeltaMovement(Vector3d.ZERO);

		if (this.wither.getInvulnerableTicks() == AttackFeature.Consts.CHARGE_ATTACK_TICK_START)
			this.wither.level.playSound(null, this.wither.getPosition(), SoundEvents.WITHER_DEATH, SoundCategory.HOSTILE, 5.0f, 2.0f);
		else if (this.wither.getInvulnerableTicks() == AttackFeature.Consts.CHARGE_ATTACK_TICK_CHARGE) {
			this.target = this.wither.level.getNearestPlayer(this.wither, 64d);
			if (target != null) {
				this.targetPos = this.target.position();
				Vector3d forward = this.targetPos.subtract(this.wither.position()).normalize();
				this.targetPos = this.targetPos.add(forward.multiply(4d, 4d, 4d));
				this.lastDistanceFromTarget = this.targetPos.distanceToSqr(this.wither.position());
				this.wither.world.playSound(null, new BlockPos(this.targetPos), SoundEvents.WITHER_SPAWN, SoundCategory.HOSTILE, 4.0f, 2.0f);
			}
			else {
				this.wither.world.createExplosion(this.wither, this.wither.getPosX(), this.wither.getPosY() + 1.75d, this.wither.getPosZ(), 6f, Explosion.Mode.DESTROY);
				this.wither.setInvulTime(0);
			}
		}
		else if (this.wither.getInvulnerableTicks() < AttackFeature.Consts.CHARGE_ATTACK_TICK_CHARGE) {
			//Done so it goes faster and faster
			double mult = 60d / this.wither.getInvulnerableTicks();
			Vector3d diff = this.targetPos.subtract(this.wither.getPositionVec()).normalize().mul(mult, mult, mult);
			this.wither.setMotion(diff.x, diff.y * 0.5, diff.z);
			this.wither.getLookController().setLookPosition(this.targetPos);
			AxisAlignedBB axisAlignedBB = new AxisAlignedBB(this.wither.getPosX() - 2, this.wither.getPosY() - 2, this.wither.getPosZ() - 2, this.wither.getPosX() + 2, this.wither.getPosY() + 6, this.wither.getPosZ() + 2);
			Stream<BlockPos> blocks = BlockPos.getAllInBox(axisAlignedBB);
			AtomicBoolean hasBrokenBlocks = new AtomicBoolean(false);
			blocks.forEach(blockPos -> {
				BlockState state = wither.world.getBlockState(blockPos);
				if (state.canEntityDestroy(wither.world, blockPos, wither) && net.minecraftforge.event.ForgeEventFactory.onEntityDestroyBlock(wither, blockPos, state) && !state.getBlock().equals(Blocks.AIR)) {
					TileEntity tileentity = state.hasTileEntity() ? this.wither.world.getTileEntity(blockPos) : null;
					LootContext.Builder lootcontext$builder = (new LootContext.Builder((ServerWorld)this.wither.world)).withRandom(this.wither.world.rand).withParameter(LootParameters.ORIGIN, Vector3d.copyCentered(blockPos)).withParameter(LootParameters.TOOL, ItemStack.EMPTY).withNullableParameter(LootParameters.BLOCK_ENTITY, tileentity);
					state.getDrops(lootcontext$builder).forEach(itemStack -> {
						ItemEntity itemEntity = new ItemEntity(this.wither.world, blockPos.getX() + .5d, blockPos.getY() + .5d, blockPos.getZ() + .5d, itemStack);
						itemEntity.lifespan = 1200;
						this.wither.world.addEntity(itemEntity);
					});
					wither.world.setBlockState(blockPos, Blocks.AIR.getDefaultState());
					hasBrokenBlocks.set(true);
				}
			});

			if (hasBrokenBlocks.get())
				this.wither.world.playSound(null, new BlockPos(this.targetPos), SoundEvents.ENTITY_WITHER_BREAK_BLOCK, SoundCategory.HOSTILE, 1.0f, 0.75f);

			axisAlignedBB = axisAlignedBB.inflate(1d);
			this.wither.world.getLoadedEntitiesWithinAABB(LivingEntity.class, axisAlignedBB).forEach(entity -> {
				if (entity == this.wither)
					return;
				entity.attackEntityFrom(new EntityDamageSource(Strings.Translatable.WITHER_CHARGE_ATTACK, this.wither), 16f);
				double d2 = entity.getPosX() - this.wither.getPosX();
				double d3 = entity.getPosZ() - this.wither.getPosZ();
				double d4 = Math.max(d2 * d2 + d3 * d3, 0.1D);
				entity.addVelocity(d2 / d4 * 25.0D, 1.4d, d3 / d4 * 25.0D);
			});
		}
		//If the wither's charging and is farther from the target point than the last tick OR is about to finish the invulnerability time then prevent the explosion and stop the attack
		if ((this.wither.getInvulnerableTicks() < AttackFeature.Consts.CHARGE_ATTACK_TICK_CHARGE && this.wither.getInvulnerableTicks() > 0 && (this.targetPos.squareDistanceTo(this.wither.getPositionVec()) - this.lastDistanceFromTarget > 16d || this.targetPos.squareDistanceTo(this.wither.getPositionVec()) < 4d)) || this.wither.getInvulTime() == 1) {
			this.wither.setInvulTime(0);
		}
		if (this.targetPos != null)
			this.lastDistanceFromTarget = this.targetPos.squareDistanceTo(this.wither.getPositionVec());
	}
}