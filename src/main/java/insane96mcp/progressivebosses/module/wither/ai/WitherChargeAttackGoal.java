package insane96mcp.progressivebosses.module.wither.ai;

import com.mojang.datafixers.util.Pair;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.module.wither.data.WitherAttack;
import insane96mcp.progressivebosses.module.wither.entity.PBWither;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.commands.arguments.EntityAnchorArgument;
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
import net.minecraft.world.entity.ai.attributes.Attributes;
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
import net.minecraftforge.event.ForgeEventFactory;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Stream;

public class WitherChargeAttackGoal extends Goal {

	private static float DEFAULT_DAMAGE = 8f;
	private static int DEFAULT_TIME_TO_CHARGE = 50;

	public static ResourceKey<DamageType> WITHER_CHARGE_DAMAGE_TYPE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(ProgressiveBosses.MOD_ID, "wither_charge"));

	private final PBWither wither;
	private Vec3 targetPos;
	private double lastDistanceFromTarget = 0d;
	private boolean blowUp = false;

	public WitherChargeAttackGoal(PBWither wither) {
		this.wither = wither;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.LOOK, Flag.TARGET));
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
		if (this.wither.chargeType == ChargeType.STUCK) {
			this.targetPos = this.wither.position().add(0, -5, 0);
			return;
		}

		List<Player> playersNearby = this.wither.level().getEntitiesOfClass(Player.class, this.wither.getBoundingBox().inflate(3f));
		if (!playersNearby.isEmpty()) {
            if (this.wither.needsHealing()) {
                this.targetPos = this.wither.position().add((this.wither.getRandom().nextInt(5) - 2) * 10, (this.wither.getRandom().nextInt(5) - 2) * 10, (this.wither.getRandom().nextInt(5) - 2) * 10);
            }
            else {
                this.blowUp = true;
                this.targetPos = this.wither.position();
            }
        }
		else {
			LivingEntity target = this.wither.getTarget();
			if (target == null) {
				playersNearby = this.wither.level().getEntitiesOfClass(Player.class, this.wither.getBoundingBox().inflate(64f));
				if (!playersNearby.isEmpty())
					target = playersNearby.get(this.wither.getRandom().nextInt(playersNearby.size()));
				//target = this.wither.level().getNearestPlayer(this.wither.getX(), this.wither.getY(), this.wither.getZ(), 64d, true);
			}
			else if (target != null) {
				this.wither.lookAt(target, 30f, 30f);
				this.targetPos = target.position().add(0, -1.5d, 0);
				Vec3 forward = this.targetPos.subtract(this.wither.position()).normalize();
				this.targetPos = this.targetPos.add(forward.multiply(4d, 4d, 4d));
				this.lastDistanceFromTarget = this.targetPos.distanceToSqr(this.wither.position());
			}
			else {
				this.wither.stopCharging();
			}
		}
	}

	/**
	 * Reset the task's internal state. Called when this task is interrupted by another one
	 */
	public void stop() {
		this.wither.setDeltaMovement(this.wither.getDeltaMovement().multiply(0.02d, 0.02d, 0.02d));
		this.wither.hurtMarked = true;
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
		if (chargeTicks > PBWither.CHARGE_ATTACK_TICK_CHARGE) {
			this.wither.setDeltaMovement(Vec3.ZERO);
			if (this.targetPos != null)
				this.wither.lookAt(EntityAnchorArgument.Anchor.EYES, this.targetPos);
		}
		else if (chargeTicks == PBWither.CHARGE_ATTACK_TICK_CHARGE) {
			if (this.targetPos == null) {
				this.wither.stopCharging();
				return;
			}
			this.wither.level().playSound(null, BlockPos.containing(this.targetPos), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 4.0f, 2.0f);
            this.lastDistanceFromTarget = this.targetPos.distanceToSqr(this.wither.position());
		}
		else {
			if (this.blowUp) {
				this.wither.level().playSound(null, this.wither.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE);
				((ServerLevel) this.wither.level()).sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.wither.getX(), this.wither.getY(), this.wither.getZ(), 2, 0f, 0f, 0f, 1f);
				AABB axisAlignedBB = this.wither.getBoundingBox().inflate(2f, 1f, 2f);
				Stream<BlockPos> blocks = BlockPos.betweenClosedStream(axisAlignedBB);
				if (ForgeEventFactory.getMobGriefingEvent(wither.level(), wither)) {
					blocks.forEach(blockPos -> {
						BlockState state = wither.level().getBlockState(blockPos);
						if (this.wither.canDestroyBlock(blockPos, state)
								&& ForgeEventFactory.onEntityDestroyBlock(wither, blockPos, state) && !state.getBlock().equals(Blocks.AIR)) {
							BlockEntity tileentity = state.hasBlockEntity() ? this.wither.level().getBlockEntity(blockPos) : null;
							LootParams.Builder lootcontext$builder = (new LootParams.Builder((ServerLevel)this.wither.level())).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockPos)).withParameter(LootContextParams.TOOL, ItemStack.EMPTY).withOptionalParameter(LootContextParams.BLOCK_ENTITY, tileentity);
							state.getDrops(lootcontext$builder).forEach(itemStack -> addBlockDrops(blocksToDrop, itemStack, blockPos));
							wither.level().setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
						}
					});
				}
				this.wither.level().getEntitiesOfClass(LivingEntity.class, this.wither.getBoundingBox().inflate(4f)).forEach(this::damageAndPush);
				this.wither.stopCharging();
				if (this.wither.stats.attack.barrage != null)
					this.wither.initBarrage();
			}
			else if (this.targetPos == null) {
				this.wither.stopCharging();
			}
			else {
				//So it goes faster and faster
				double mult = 60d / chargeTicks;
				Vec3 diff = this.targetPos.subtract(this.wither.position()).normalize().multiply(mult, mult, mult);
				this.wither.setDeltaMovement(diff.x, diff.y * 0.5, diff.z);
				//this.wither.move(MoverType.SELF, new Vec3(diff.x, diff.y * 0.5, diff.z));
				this.wither.getLookControl().setLookAt(this.targetPos);
				AABB axisAlignedBB = this.wither.getBoundingBox().inflate(2f, 1.5f, 2f);
				Stream<BlockPos> blocks = BlockPos.betweenClosedStream(axisAlignedBB);
				AtomicBoolean hasBrokenBlocks = new AtomicBoolean(false);
				if (ForgeEventFactory.getMobGriefingEvent(wither.level(), wither)) {
					blocks.forEach(blockPos -> {
						BlockState state = wither.level().getBlockState(blockPos);
						if (this.wither.canDestroyBlock(blockPos, state)
								&& ForgeEventFactory.onEntityDestroyBlock(wither, blockPos, state) && !state.getBlock().equals(Blocks.AIR)) {
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

				axisAlignedBB = axisAlignedBB.inflate(1.5d);
				this.wither.level()
						.getEntitiesOfClass(LivingEntity.class, axisAlignedBB)
						.forEach(this::damageAndPush);

				double distance = this.targetPos.distanceToSqr(this.wither.position());
				//If the wither's charging and is farther from the target point than the last tick OR is closer than sqrt(6) blocks OR is about to finish the invulnerability time then prevent the explosion and stop the attack
				if ((distance - this.lastDistanceFromTarget >= 0 && chargeTicks < PBWither.CHARGE_ATTACK_TICK_CHARGE - 1) || distance < 10d || chargeTicks == 1)
					this.wither.stopCharging();

				this.lastDistanceFromTarget = distance;
			}
		}
	}

	private void damageAndPush(LivingEntity entity) {
		if (entity == this.wither)
			return;
		entity.hurt(entity.damageSources().source(WITHER_CHARGE_DAMAGE_TYPE, this.wither), this.wither.stats.attack.charge == null ? 12f : WitherAttack.WitherCharge.getDamage(this.wither));
		float d2 = (float) (entity.getX() - this.wither.getX());
		float d3 = (float) (entity.getZ() - this.wither.getZ());
		float d4 = Math.max(d2 * d2 + d3 * d3, 0.1f);
		float horizontalPush = (float) (5f * (1.0D - entity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)));
		float verticalPush = (float) (0.65f * (1.0D - entity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)));
		if (entity instanceof ServerPlayer player && player.getAbilities().instabuild)
			return;
		entity.push(d2 / d4 * horizontalPush, verticalPush, d3 / d4 * horizontalPush);
		if (entity instanceof Player player)
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

	public enum ChargeType {
		ON_HIT(wither -> wither.stats.attack.charge.onHit == null ? DEFAULT_DAMAGE : wither.stats.attack.charge.onHit.damage,
				wither -> wither.stats.attack.charge.onHit == null ? DEFAULT_TIME_TO_CHARGE : wither.stats.attack.charge.onHit.timeToCharge),
		SECOND_PHASE(wither -> wither.stats.attack.charge.secondPhase == null ? DEFAULT_DAMAGE : wither.stats.attack.charge.secondPhase.damage,
				wither -> wither.stats.attack.charge.secondPhase == null ? DEFAULT_TIME_TO_CHARGE : wither.stats.attack.charge.secondPhase.timeToCharge),
		TARGET_UNSEEN(wither -> wither.stats.attack.charge.targetUnseen == null ? DEFAULT_DAMAGE : wither.stats.attack.charge.targetUnseen.damage,
				wither -> wither.stats.attack.charge.targetUnseen == null ? DEFAULT_TIME_TO_CHARGE : wither.stats.attack.charge.targetUnseen.timeToCharge),
		STUCK(wither -> DEFAULT_DAMAGE,
				wither -> DEFAULT_TIME_TO_CHARGE);

		public final Function<PBWither, Float> getDamage;
		public final Function<PBWither, Integer> getTimeToCharge;

		ChargeType(Function<PBWither, Float> damage, Function<PBWither, Integer> timeToCharge) {
			this.getDamage = damage;
			this.getTimeToCharge = timeToCharge;
		}

		public float getDamage(PBWither wither) {
			return this.getDamage.apply(wither);
		}

		public int getTimeToCharge(PBWither wither) {
			return this.getTimeToCharge.apply(wither);
		}
	}
}