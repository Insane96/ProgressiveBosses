package insane96mcp.progressivebosses.module.wither.ai;

import com.mojang.datafixers.util.Pair;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.module.wither.entity.PBWither;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class WitherChargeAttackGoal extends Goal {
	public static ResourceKey<DamageType> WITHER_CHARGE_DAMAGE_TYPE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(ProgressiveBosses.MOD_ID, "wither_charge"));

	private final PBWither wither;
	private LivingEntity target;
	private Vec3 targetPos;
	private double lastDistanceFromTarget = 0d;
	private boolean blowUp = false;

	public WitherChargeAttackGoal(PBWither wither) {
		this.wither = wither;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP, Goal.Flag.LOOK, Flag.TARGET));
	}

	/**
	 * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
	 * method as well.
	 */
	public boolean canUse() {
		return this.wither.isCharging();
	}

	public void start() {
		this.wither.getNavigation().stop();
		for (int h = 0; h < 3; h++)
			this.wither.setAlternativeTarget(h, 0);

		this.wither.level().playSound(null, this.wither.blockPosition(), SoundEvents.WITHER_DEATH, SoundSource.HOSTILE, 5.0f, 2.0f);
		blocksToDrop.clear();
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
		this.blowUp = false;

		for (Pair<ItemStack, BlockPos> pair : blocksToDrop) {
			Block.popResource(this.wither.level(), pair.getSecond(), pair.getFirst());
		}
	}

	ObjectArrayList<Pair<ItemStack, BlockPos>> blocksToDrop = new ObjectArrayList<>();

	/**
	 * Keep ticking a continuous task that has already been started
	 */
	public void tick() {
		if (!this.wither.isCharging())
			return;

		int chargeTicks = this.wither.getChargingTicks();
		if (chargeTicks > PBWither.CHARGE_ATTACK_TICK_CHARGE)
			this.wither.setDeltaMovement(Vec3.ZERO);

		if (chargeTicks == PBWither.CHARGE_ATTACK_TICK_CHARGE) {
			List<Player> playersNearby = this.wither.level().getEntitiesOfClass(Player.class, this.wither.getBoundingBox().inflate(3.5f));
			if (!playersNearby.isEmpty()) {
				this.blowUp = true;
				this.wither.level().playSound(null, this.wither.blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 4.0f, 2.0f);
			}
			else {
				this.target = this.wither.getTarget();
				if (this.target == null)
					this.target = this.wither.level().getNearestPlayer(this.wither.getX(), this.wither.getY(), this.wither.getZ(), 64d, true);
				if (target != null) {
					this.targetPos = this.target.position().add(0, -1.5d, 0);
					Vec3 forward = this.targetPos.subtract(this.wither.position()).normalize();
					this.targetPos = this.targetPos.add(forward.multiply(4d, 4d, 4d));
					this.lastDistanceFromTarget = this.targetPos.distanceToSqr(this.wither.position());
					this.wither.level().playSound(null, BlockPos.containing(this.targetPos), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 4.0f, 2.0f);
				}
				else if (this.wither.chargeBelow) {
					this.targetPos = this.wither.position().add(0, -3, 0);
					this.wither.chargeBelow = false;
				}
				else {
					this.wither.stopCharging();
				}
			}
		}
		else if (chargeTicks < PBWither.CHARGE_ATTACK_TICK_CHARGE) {
			if (this.blowUp) {
				this.wither.level().playSound(null, this.wither.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE);
				((ServerLevel) this.wither.level()).sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.wither.getX(), this.wither.getY(), this.wither.getZ(), 2, 0f, 0f, 0f, 1f);
				this.wither.level().getEntitiesOfClass(LivingEntity.class, this.wither.getBoundingBox().inflate(3.5f)).forEach(entity -> this.damageAndPush(entity));
				this.wither.stopCharging();
			}
			else if (this.targetPos == null) {
				this.wither.stopCharging();
			}
			else {
				//So it goes faster and faster
				double mult = 60d / chargeTicks;
				Vec3 diff = this.targetPos.subtract(this.wither.position()).normalize().multiply(mult, mult, mult);
				this.wither.setDeltaMovement(diff.x, diff.y * 0.5, diff.z);
				this.wither.getLookControl().setLookAt(this.targetPos);
				AABB axisAlignedBB = new AABB(this.wither.getX() - 2, this.wither.getY() - 2, this.wither.getZ() - 2, this.wither.getX() + 2, this.wither.getY() + 6, this.wither.getZ() + 2);
				Stream<BlockPos> blocks = BlockPos.betweenClosedStream(axisAlignedBB);
				AtomicBoolean hasBrokenBlocks = new AtomicBoolean(false);
				if (net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(wither.level(), wither)) {
					blocks.forEach(blockPos -> {
						BlockState state = wither.level().getBlockState(blockPos);
						if (this.wither.canDestroyBlock(blockPos, state)
								&& net.minecraftforge.event.ForgeEventFactory.onEntityDestroyBlock(wither, blockPos, state) && !state.getBlock().equals(Blocks.AIR)) {
							BlockEntity tileentity = state.hasBlockEntity() ? this.wither.level().getBlockEntity(blockPos) : null;
							LootParams.Builder lootcontext$builder = (new LootParams.Builder((ServerLevel)this.wither.level())).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockPos)).withParameter(LootContextParams.TOOL, ItemStack.EMPTY).withOptionalParameter(LootContextParams.BLOCK_ENTITY, tileentity);
							state.getDrops(lootcontext$builder).forEach(itemStack -> addBlockDrops(blocksToDrop, itemStack, blockPos));
							wither.level().setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
							hasBrokenBlocks.set(true);
						}
					});
				}

				if (hasBrokenBlocks.get() && this.wither.tickCount % 3 == 0)
					this.wither.level().playSound(null, BlockPos.containing(this.targetPos), SoundEvents.WITHER_BREAK_BLOCK, SoundSource.HOSTILE, 1.0f, 0.75f);

				axisAlignedBB = axisAlignedBB.inflate(1d);
				this.wither.level()
						.getEntitiesOfClass(LivingEntity.class, axisAlignedBB)
						.forEach(entity -> this.damageAndPush(entity));
			}
		}
		if (this.targetPos != null) {
			double distance = this.targetPos.distanceToSqr(this.wither.position());
			//If the wither's charging and is farther from the target point than the last tick OR is closer than sqrt(6) blocks OR is about to finish the invulnerability time then prevent the explosion and stop the attack
			if ((chargeTicks < PBWither.CHARGE_ATTACK_TICK_CHARGE && (distance - this.lastDistanceFromTarget > 16d || distance < 6d)) || chargeTicks == 1)
				this.wither.stopCharging();

			this.lastDistanceFromTarget = distance;
		}
	}

	private void damageAndPush(LivingEntity entity) {
		if (entity == this.wither)
			return;
		entity.hurt(entity.damageSources().source(WITHER_CHARGE_DAMAGE_TYPE, this.wither), this.wither.stats.attack.charge == null ? 12f : this.wither.stats.attack.charge.damage);
		float d2 = (float) (entity.getX() - this.wither.getX());
		float d3 = (float) (entity.getZ() - this.wither.getZ());
		float d4 = Math.max(d2 * d2 + d3 * d3, 0.1f);
		entity.push(d2 / d4 * 15f, 0.7f, d3 / d4 * 15f);
		if (entity instanceof ServerPlayer player)
			player.hurtMarked = true;
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