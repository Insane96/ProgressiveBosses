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
			this.wither.level.playSound(null, this.wither.blockPosition(), SoundEvents.WITHER_DEATH, SoundCategory.HOSTILE, 5.0f, 2.0f);
		else if (this.wither.getInvulnerableTicks() == AttackFeature.Consts.CHARGE_ATTACK_TICK_CHARGE) {
			this.target = this.wither.level.getNearestPlayer(this.wither, 64d);
			if (target != null) {
				this.targetPos = this.target.position();
				Vector3d forward = this.targetPos.subtract(this.wither.position()).normalize();
				this.targetPos = this.targetPos.add(forward.multiply(4d, 4d, 4d));
				this.lastDistanceFromTarget = this.targetPos.distanceToSqr(this.wither.position());
				this.wither.level.playSound(null, new BlockPos(this.targetPos), SoundEvents.WITHER_SPAWN, SoundCategory.HOSTILE, 4.0f, 2.0f);
			}
			else {
				this.wither.level.explode(this.wither, this.wither.getX(), this.wither.getY() + 1.75d, this.wither.getZ(), 6f, Explosion.Mode.DESTROY);
				this.wither.setInvulnerableTicks(0);
			}
		}
		else if (this.wither.getInvulnerableTicks() < AttackFeature.Consts.CHARGE_ATTACK_TICK_CHARGE) {
			//Done so it goes faster and faster
			double mult = 60d / this.wither.getInvulnerableTicks();
			Vector3d diff = this.targetPos.subtract(this.wither.position()).normalize().multiply(mult, mult, mult);
			this.wither.setDeltaMovement(diff.x, diff.y * 0.5, diff.z);
			this.wither.getLookControl().setLookAt(this.targetPos);
			AxisAlignedBB axisAlignedBB = new AxisAlignedBB(this.wither.getX() - 2, this.wither.getY() - 2, this.wither.getZ() - 2, this.wither.getX() + 2, this.wither.getY() + 6, this.wither.getZ() + 2);
			Stream<BlockPos> blocks = BlockPos.betweenClosedStream(axisAlignedBB);
			AtomicBoolean hasBrokenBlocks = new AtomicBoolean(false);
			blocks.forEach(blockPos -> {
				BlockState state = wither.level.getBlockState(blockPos);
				if (state.canEntityDestroy(wither.level, blockPos, wither) && net.minecraftforge.event.ForgeEventFactory.onEntityDestroyBlock(wither, blockPos, state) && !state.getBlock().equals(Blocks.AIR)) {
					TileEntity tileentity = state.hasTileEntity() ? this.wither.level.getBlockEntity(blockPos) : null;
					LootContext.Builder lootcontext$builder = (new LootContext.Builder((ServerWorld)this.wither.level)).withRandom(this.wither.level.random).withParameter(LootParameters.ORIGIN, Vector3d.atCenterOf(blockPos)).withParameter(LootParameters.TOOL, ItemStack.EMPTY).withOptionalParameter(LootParameters.BLOCK_ENTITY, tileentity);
					state.getDrops(lootcontext$builder).forEach(itemStack -> {
						ItemEntity itemEntity = new ItemEntity(this.wither.level, blockPos.getX() + .5d, blockPos.getY() + .5d, blockPos.getZ() + .5d, itemStack);
						itemEntity.lifespan = 1200;
						this.wither.level.addFreshEntity(itemEntity);
					});
					wither.level.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
					hasBrokenBlocks.set(true);
				}
			});

			if (hasBrokenBlocks.get())
				this.wither.level.playSound(null, new BlockPos(this.targetPos), SoundEvents.WITHER_BREAK_BLOCK, SoundCategory.HOSTILE, 1.0f, 0.75f);

			axisAlignedBB = axisAlignedBB.inflate(1d);
			this.wither.level.getLoadedEntitiesOfClass(LivingEntity.class, axisAlignedBB).forEach(entity -> {
				if (entity == this.wither)
					return;
				entity.hurt(new EntityDamageSource(Strings.Translatable.WITHER_CHARGE_ATTACK, this.wither), 16f);
				double d2 = entity.getX() - this.wither.getX();
				double d3 = entity.getZ() - this.wither.getZ();
				double d4 = Math.max(d2 * d2 + d3 * d3, 0.1D);
				entity.push(d2 / d4 * 25.0D, 1.4d, d3 / d4 * 25.0D);
			});
		}
		//If the wither's charging and is farther from the target point than the last tick OR is about to finish the invulnerability time then prevent the explosion and stop the attack
		if ((this.wither.getInvulnerableTicks() < AttackFeature.Consts.CHARGE_ATTACK_TICK_CHARGE && this.wither.getInvulnerableTicks() > 0 && (this.targetPos.distanceToSqr(this.wither.position()) - this.lastDistanceFromTarget > 16d || this.targetPos.distanceToSqr(this.wither.position()) < 4d)) || this.wither.getInvulnerableTicks() == 1) {
			this.wither.setInvulnerableTicks(0);
		}
		if (this.targetPos != null)
			this.lastDistanceFromTarget = this.targetPos.distanceToSqr(this.wither.position());
	}
}