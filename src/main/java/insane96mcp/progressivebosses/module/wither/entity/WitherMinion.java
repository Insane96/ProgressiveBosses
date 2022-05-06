package insane96mcp.progressivebosses.module.wither.entity;

import insane96mcp.progressivebosses.module.wither.ai.minion.MinionNearestAttackableTargetGoal;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class WitherMinion extends AbstractSkeleton {

	private static final Predicate<LivingEntity> NOT_UNDEAD = livingEntity -> livingEntity != null && livingEntity.getMobType() != MobType.UNDEAD && livingEntity.attackable();

	public WitherMinion(EntityType<? extends AbstractSkeleton> type, Level worldIn) {
		super(type, worldIn);
	}

	@Override
	protected SoundEvent getStepSound() {
		return SoundEvents.WITHER_SKELETON_STEP;
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(1, new FloatGoal(this));
		this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

		this.targetSelector.addGoal(1, new HurtByTargetGoal(this, WitherBoss.class, WitherMinion.class));
		this.targetSelector.addGoal(2, new MinionNearestAttackableTargetGoal(this, Player.class, 0, false, false, null));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Mob.class, 0, false, false, NOT_UNDEAD));
	}

	protected SoundEvent getAmbientSound() {
		return SoundEvents.WITHER_SKELETON_AMBIENT;
	}

	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundEvents.WITHER_SKELETON_HURT;
	}

	protected SoundEvent getDeathSound() {
		return SoundEvents.WITHER_SKELETON_DEATH;
	}

	protected float getStandingEyeHeight(Pose poseIn, EntityDimensions sizeIn) {
		return 1.3F;
	}

	/**
	 * Gets the pitch of living sounds in living entities.
	 */
	public float getVoicePitch() {
		return (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.33F;
	}

	/**
	 * Gives armor or weapon for entity based on given DifficultyInstance
	 */
	protected void populateDefaultEquipmentSlots(DifficultyInstance difficulty) {
		this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
	}

	@Nullable
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn, MobSpawnType reason, @Nullable SpawnGroupData spawnDataIn, @Nullable CompoundTag dataTag) {
		SpawnGroupData ilivingentitydata = super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
		this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(4.0D);
		this.reassessWeaponGoal();
		return ilivingentitydata;
	}

	public boolean doHurtTarget(Entity entityIn) {
		if (!super.doHurtTarget(entityIn)) {
			return false;
		} else {
			if (entityIn instanceof LivingEntity) {
				((LivingEntity)entityIn).addEffect(new MobEffectInstance(MobEffects.WITHER, 200));
			}

			return true;
		}
	}

	public boolean hurt(DamageSource source, float amount) {
		if (source.getEntity() instanceof WitherMinion || source.getEntity() instanceof WitherBoss)
			amount *= 0.2f;
		return !this.isInvulnerableTo(source) && super.hurt(source, amount);
	}

	public boolean canBeAffected(MobEffectInstance potioneffectIn) {
		return potioneffectIn.getEffect() != MobEffects.WITHER && super.canBeAffected(potioneffectIn);
	}

	//Do not generate Wither Roses
	protected void createWitherRose(@Nullable LivingEntity entitySource) {
	}

	public static AttributeSupplier.Builder prepareAttributes() {
		return LivingEntity.createLivingAttributes()
				.add(Attributes.ATTACK_DAMAGE, 3.0d)
				.add(Attributes.MAX_HEALTH, 20.0d)
				.add(Attributes.FOLLOW_RANGE, 40.0d)
				.add(Attributes.MOVEMENT_SPEED, 0.25d)
				.add(Attributes.ATTACK_KNOCKBACK, 1.5d);
	}
}
