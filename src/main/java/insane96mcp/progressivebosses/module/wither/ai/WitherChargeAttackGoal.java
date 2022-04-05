package insane96mcp.progressivebosses.module.wither.ai;

import com.mojang.datafixers.util.Pair;
import insane96mcp.progressivebosses.module.wither.feature.AttackFeature;
import insane96mcp.progressivebosses.setup.Strings;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class WitherChargeAttackGoal extends Goal {
	private final WitherBoss wither;
	private LivingEntity target;
	private Vec3 targetPos;
	private double lastDistanceFromTarget = 0d;

	public WitherChargeAttackGoal(WitherBoss wither) {
		this.wither = wither;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP, Goal.Flag.LOOK, Flag.TARGET));
	}

	/**
	 * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
	 * method as well.
	 */
	public boolean canUse() {
		byte chargeTick = this.wither.getPersistentData().getByte(Strings.Tags.CHARGE_ATTACK);
		return chargeTick > 0;
	}

	public void start() {
		this.wither.getNavigation().stop();
		for (int h = 0; h < 3; h++)
			this.wither.setAlternativeTarget(h, 0);

		this.wither.level.playSound(null, this.wither.blockPosition(), SoundEvents.WITHER_DEATH, SoundSource.HOSTILE, 5.0f, 2.0f);
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	public boolean canContinueToUse() {
		return this.wither.getPersistentData().getByte(Strings.Tags.CHARGE_ATTACK) > 0;
	}

	/**
	 * Reset the task's internal state. Called when this task is interrupted by another one
	 */
	public void stop() {
		this.target = null;
		//AttackFeature.setCharging(this.wither, false);
		this.wither.setDeltaMovement(this.wither.getDeltaMovement().multiply(0.02d, 0.02d, 0.02d));
		this.lastDistanceFromTarget = 0d;
		this.targetPos = null;

		for(Pair<ItemStack, BlockPos> pair : blocksToDrop) {
			Block.popResource(this.wither.level, pair.getSecond(), pair.getFirst());
		}
	}

	ObjectArrayList<Pair<ItemStack, BlockPos>> blocksToDrop = new ObjectArrayList<>();

	/**
	 * Keep ticking a continuous task that has already been started
	 */
	public void tick() {
		byte chargeTick = this.wither.getPersistentData().getByte(Strings.Tags.CHARGE_ATTACK);
		//Needed since stop() now gets called every other tick
		if (chargeTick <= 0) {
			this.stop();
			return;
		}

		if (chargeTick > AttackFeature.Consts.CHARGE_ATTACK_TICK_CHARGE)
			this.wither.setDeltaMovement(Vec3.ZERO);

		if (chargeTick == AttackFeature.Consts.CHARGE_ATTACK_TICK_CHARGE) {
			this.target = this.wither.level.getNearestPlayer(this.wither.getX(), this.wither.getY(), this.wither.getZ(), 64d, true);
			if (target != null) {
				this.targetPos = this.target.position().add(0, -1.5d, 0);
				Vec3 forward = this.targetPos.subtract(this.wither.position()).normalize();
				this.targetPos = this.targetPos.add(forward.multiply(4d, 4d, 4d));
				this.lastDistanceFromTarget = this.targetPos.distanceToSqr(this.wither.position());
				this.wither.level.playSound(null, new BlockPos(this.targetPos), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 4.0f, 2.0f);
			}
			else {
				AttackFeature.stopCharging(this.wither);
			}
		}
		else if (chargeTick < AttackFeature.Consts.CHARGE_ATTACK_TICK_CHARGE) {
			if (this.targetPos == null) {
				AttackFeature.stopCharging(this.wither);
				return;
			}
			//So it goes faster and faster
			double mult = 60d / chargeTick;
			Vec3 diff = this.targetPos.subtract(this.wither.position()).normalize().multiply(mult, mult, mult);
			this.wither.setDeltaMovement(diff.x, diff.y * 0.5, diff.z);
			this.wither.getLookControl().setLookAt(this.targetPos);
			AABB axisAlignedBB = new AABB(this.wither.getX() - 2, this.wither.getY() - 2, this.wither.getZ() - 2, this.wither.getX() + 2, this.wither.getY() + 6, this.wither.getZ() + 2);
			Stream<BlockPos> blocks = BlockPos.betweenClosedStream(axisAlignedBB);
			AtomicBoolean hasBrokenBlocks = new AtomicBoolean(false);
			blocks.forEach(blockPos -> {
				BlockState state = wither.level.getBlockState(blockPos);
				if (state.canEntityDestroy(wither.level, blockPos, wither) && net.minecraftforge.event.ForgeEventFactory.onEntityDestroyBlock(wither, blockPos, state) && !state.getBlock().equals(Blocks.AIR)) {
					BlockEntity tileentity = state.hasBlockEntity() ? this.wither.level.getBlockEntity(blockPos) : null;
					LootContext.Builder lootcontext$builder = (new LootContext.Builder((ServerLevel)this.wither.level)).withRandom(this.wither.level.random).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockPos)).withParameter(LootContextParams.TOOL, ItemStack.EMPTY).withOptionalParameter(LootContextParams.BLOCK_ENTITY, tileentity);
					state.getDrops(lootcontext$builder).forEach(itemStack -> {
						addBlockDrops(blocksToDrop, itemStack, blockPos);
					});
					wither.level.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
					hasBrokenBlocks.set(true);
				}
			});

			if (hasBrokenBlocks.get() && this.wither.tickCount % 2 == 0)
				this.wither.level.playSound(null, new BlockPos(this.targetPos), SoundEvents.WITHER_BREAK_BLOCK, SoundSource.HOSTILE, 1.0f, 0.75f);

			axisAlignedBB = axisAlignedBB.inflate(1d);
			this.wither.level.getEntitiesOfClass(LivingEntity.class, axisAlignedBB).forEach(entity -> {
				if (entity == this.wither)
					return;
				entity.hurt(new EntityDamageSource(Strings.Translatable.WITHER_CHARGE_ATTACK, this.wither), 16f);
				double d2 = entity.getX() - this.wither.getX();
				double d3 = entity.getZ() - this.wither.getZ();
				double d4 = Math.max(d2 * d2 + d3 * d3, 0.1D);
				entity.push(d2 / d4 * 20d, 0.7d, d3 / d4 * 20d);
			});
		}
		//If the wither's charging and is farther from the target point than the last tick OR is about to finish the invulnerability time then prevent the explosion and stop the attack
		if ((chargeTick < AttackFeature.Consts.CHARGE_ATTACK_TICK_CHARGE && (this.targetPos.distanceToSqr(this.wither.position()) - this.lastDistanceFromTarget > 16d || this.targetPos.distanceToSqr(this.wither.position()) < 4d)) || chargeTick == 1) {
			AttackFeature.stopCharging(this.wither);
		}
		if (this.targetPos != null)
			this.lastDistanceFromTarget = this.targetPos.distanceToSqr(this.wither.position());
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	private static void addBlockDrops(ObjectArrayList<Pair<ItemStack, BlockPos>> p_46068_, ItemStack p_46069_, BlockPos p_46070_) {
		int i = p_46068_.size();

		for(int j = 0; j < i; ++j) {
			Pair<ItemStack, BlockPos> pair = p_46068_.get(j);
			ItemStack itemstack = pair.getFirst();
			if (ItemEntity.areMergable(itemstack, p_46069_)) {
				ItemStack itemstack1 = ItemEntity.merge(itemstack, p_46069_, 16);
				p_46068_.set(j, Pair.of(itemstack1, pair.getSecond()));
				if (p_46069_.isEmpty()) {
					return;
				}
			}
		}

		p_46068_.add(Pair.of(p_46069_, p_46070_));
	}
}