package insane96mcp.progressivebosses.module.wither.entity;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.monster.AbstractSkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class WitherMinionEntity extends AbstractSkeletonEntity {

	private static final Predicate<LivingEntity> NOT_UNDEAD = livingEntity -> livingEntity != null && livingEntity.getCreatureAttribute() != CreatureAttribute.UNDEAD && livingEntity.attackable();

	public WitherMinionEntity(EntityType<? extends AbstractSkeletonEntity> type, World worldIn) {
		super(type, worldIn);
	}

	@Override
	protected SoundEvent getStepSound() {
		return SoundEvents.ENTITY_WITHER_SKELETON_STEP;
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(1, new SwimGoal(this));
		this.goalSelector.addGoal(5, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
		this.goalSelector.addGoal(6, new LookAtGoal(this, PlayerEntity.class, 8.0F));
		this.goalSelector.addGoal(6, new LookRandomlyGoal(this));

		//TODO Give them xray
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this, WitherEntity.class, WitherMinionEntity.class));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, MobEntity.class, 0, false, false, NOT_UNDEAD));
	}

	protected SoundEvent getAmbientSound() {
		return SoundEvents.ENTITY_WITHER_SKELETON_AMBIENT;
	}

	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundEvents.ENTITY_WITHER_SKELETON_HURT;
	}

	protected SoundEvent getDeathSound() {
		return SoundEvents.ENTITY_WITHER_SKELETON_DEATH;
	}

	protected float getStandingEyeHeight(Pose poseIn, EntitySize sizeIn) {
		return 1.3F;
	}

	/**
	 * Gets the pitch of living sounds in living entities.
	 */
	protected float getSoundPitch() {
		return (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.33F;
	}

	/**
	 * Gives armor or weapon for entity based on given DifficultyInstance
	 */
	protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty) {
		this.setItemStackToSlot(EquipmentSlotType.MAINHAND, new ItemStack(Items.BOW));
	}

	@Nullable
	public ILivingEntityData onInitialSpawn(IServerWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason, @Nullable ILivingEntityData spawnDataIn, @Nullable CompoundNBT dataTag) {
		ILivingEntityData ilivingentitydata = super.onInitialSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
		this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(4.0D);
		this.setCombatTask();
		return ilivingentitydata;
	}

	public boolean attackEntityAsMob(Entity entityIn) {
		if (!super.attackEntityAsMob(entityIn)) {
			return false;
		} else {
			if (entityIn instanceof LivingEntity) {
				((LivingEntity)entityIn).addPotionEffect(new EffectInstance(Effects.WITHER, 200));
			}

			return true;
		}
	}

	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (source.getTrueSource() instanceof WitherMinionEntity)
			amount *= 0.2f;
		return !this.isInvulnerableTo(source) && super.attackEntityFrom(source, amount);
	}

	public boolean isPotionApplicable(EffectInstance potioneffectIn) {
		return potioneffectIn.getPotion() != Effects.WITHER && super.isPotionApplicable(potioneffectIn);
	}

	private static final List<EffectInstance> ARROW_EFFECTS = Arrays.asList(new EffectInstance(Effects.WITHER, 200));

	/**
	 * Fires an arrow
	 */
	protected AbstractArrowEntity fireArrow(ItemStack arrowStack, float distanceFactor) {
		AbstractArrowEntity abstractarrowentity = super.fireArrow(arrowStack, distanceFactor);
		if (abstractarrowentity instanceof ArrowEntity) {
			ItemStack witherArrow = new ItemStack(Items.TIPPED_ARROW, 1);
			PotionUtils.appendEffects(witherArrow, ARROW_EFFECTS);
			((ArrowEntity)abstractarrowentity).setPotionEffect(witherArrow);
		}
		return abstractarrowentity;
	}

	//Do not generate Wither Roses
	protected void createWitherRose(@Nullable LivingEntity entitySource) {
	}

	public static AttributeModifierMap.MutableAttribute prepareAttributes() {
		return LivingEntity.registerAttributes()
				.createMutableAttribute(Attributes.ATTACK_DAMAGE, 3.0d)
				.createMutableAttribute(Attributes.MAX_HEALTH, 20.0d)
				.createMutableAttribute(Attributes.FOLLOW_RANGE, 40.0d)
				.createMutableAttribute(Attributes.MOVEMENT_SPEED, 0.25d)
				.createMutableAttribute(Attributes.ATTACK_KNOCKBACK, 1.5d);
	}
}
