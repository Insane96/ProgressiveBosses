package insane96mcp.progressivebosses.module.wither.entity.minion;

import insane96mcp.insanelib.ai.ILNearestAttackableTargetGoal;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.insanelib.util.MathHelper;
import insane96mcp.progressivebosses.module.ILvl;
import insane96mcp.progressivebosses.module.wither.ai.RangedMinionAttackGoal;
import insane96mcp.progressivebosses.module.wither.data.WitherMinionStats;
import insane96mcp.progressivebosses.module.wither.data.WitherStats;
import insane96mcp.progressivebosses.module.wither.data.WitherStatsReloadListener;
import insane96mcp.progressivebosses.module.wither.entity.PBWither;
import insane96mcp.progressivebosses.setup.PBEntities;
import insane96mcp.progressivebosses.setup.Strings;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
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
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class WitherMinion extends AbstractSkeleton implements ILvl {

	protected final RangedMinionAttackGoal minionBowGoal = new RangedMinionAttackGoal(this, 1.0D, 20, 15.0F);

	private static final Predicate<LivingEntity> NOT_UNDEAD = livingEntity -> livingEntity != null && livingEntity.getMobType() != MobType.UNDEAD && livingEntity.attackable();

	WitherMinionStats stats;
	int lvl;
	boolean summonedByPoweredWither;

	public WitherMinion(EntityType<? extends AbstractSkeleton> type, Level worldIn) {
		super(type, worldIn);
		this.reassesMinionWeapon();
	}

	@Nullable
	public static WitherMinion create(Vec3 pos, PBWither wither) {
		return create(wither.level(), pos, wither.getLvl(), wither.isPowered());
	}

	@Nullable
	public static WitherMinion create(Level level, Vec3 pos, int lvl, boolean isPowered) {
		WitherMinion minion = PBEntities.WITHER_MINION.get().create(level);
		if (minion == null)
			return null;
		CompoundTag minionTags = minion.getPersistentData();
		minionTags.putBoolean("mobspropertiesrandomness:processed", true);

		minion.setPos(pos);
		minion.setLvl(lvl);
		minion.summonedByPoweredWither = isPowered;
		minion.setDropChance(EquipmentSlot.MAINHAND, -2f);
		minion.setCanPickUpLoot(false);
		minion.setPersistenceRequired();
		minion.setEquipment();

		MCUtils.applyModifier(minion, Attributes.MOVEMENT_SPEED, Strings.AttributeModifiers.MOVEMENT_SPEED_BONUS_UUID, Strings.AttributeModifiers.MOVEMENT_SPEED_BONUS, minion.stats.bonusMovementSpeed.getValue(isPowered), AttributeModifier.Operation.MULTIPLY_BASE);

		level.addFreshEntity(minion);
		return minion;
	}

	private void setEquipment() {
		this.setDropChance(EquipmentSlot.MAINHAND, Float.MIN_VALUE);

		float bowChance = this.stats.bowChance.getValue(this.summonedByPoweredWither);
		ItemStack item;

		if (this.random.nextFloat() < bowChance) {
			item = new ItemStack(Items.BOW);
			int powerLevel = MathHelper.getAmountWithDecimalChance(this.getRandom(), this.stats.powerChance);
			if (powerLevel > 0)
				item.enchant(Enchantments.POWER_ARROWS, powerLevel);
			int punchLevel = MathHelper.getAmountWithDecimalChance(this.getRandom(), this.stats.punchChance);
			if (punchLevel > 0)
				item.enchant(Enchantments.PUNCH_ARROWS, punchLevel);
		}
		else {
			item = new ItemStack(Items.STONE_SWORD);
			int sharpnessLevel = MathHelper.getAmountWithDecimalChance(this.getRandom(), this.stats.sharpnessChance);
			if (sharpnessLevel > 0)
				item.enchant(Enchantments.SHARPNESS, sharpnessLevel);
			int knockbackLevel = MathHelper.getAmountWithDecimalChance(this.getRandom(), this.stats.knockbackChance);
			if (knockbackLevel > 0)
				item.enchant(Enchantments.KNOCKBACK, knockbackLevel);
		}
		this.setItemSlot(EquipmentSlot.MAINHAND, item);
	}

	@Override
	public int getLvl() {
		return this.lvl;
	}

	@Override
	public void setLvl(int lvl) {
		this.lvl = lvl;
		if (WitherStatsReloadListener.STATS_MAP.containsKey(lvl)) {
			this.stats = WitherStatsReloadListener.STATS_MAP.get(lvl).minion;
		}
		else {
			this.stats = WitherStats.getDefaultStats().minion;
		}
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

		this.targetSelector.addGoal(1, new HurtByTargetGoal(this, PBWither.class, WitherBoss.class, WitherMinion.class));
		this.targetSelector.addGoal(2, new ILNearestAttackableTargetGoal<>(this, Player.class, false).setIgnoreLineOfSight());
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

	@Nullable
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn, MobSpawnType reason, @Nullable SpawnGroupData spawnDataIn, @Nullable CompoundTag dataTag) {
		SpawnGroupData spawnGroupData = super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
		this.reassessWeaponGoal();
		return spawnGroupData;
	}

	@Override
	public void reassessWeaponGoal() {
		super.reassessWeaponGoal();
		if (this.minionBowGoal != null)
			this.reassesMinionWeapon();
	}

	private void reassesMinionWeapon() {
		if (!this.level().isClientSide) {
			this.goalSelector.removeGoal(this.meleeGoal);
			this.goalSelector.removeGoal(this.bowGoal);
			this.goalSelector.removeGoal(this.minionBowGoal);
			ItemStack itemstack = this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, item -> item instanceof net.minecraft.world.item.BowItem));
			if (itemstack.is(Items.BOW)) {
				this.minionBowGoal.setMinAttackInterval(30);
				this.goalSelector.addGoal(4, this.minionBowGoal);
			} else {
				this.goalSelector.addGoal(4, this.meleeGoal);
			}

		}
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

	public void actuallyHurt(DamageSource source, float amount) {
		if (source.getEntity() instanceof WitherMinion || source.getEntity() instanceof PBWither)
			amount *= 0.2f;
		if (source.is(DamageTypes.MAGIC))
			amount *= this.stats.magicDamageMultiplier;
		super.actuallyHurt(source, amount);
	}

	public boolean canBeAffected(MobEffectInstance potioneffectIn) {
		return potioneffectIn.getEffect() != MobEffects.WITHER && super.canBeAffected(potioneffectIn);
	}

	//Do not generate Wither Roses
	protected void createWitherRose(@Nullable LivingEntity entitySource) {
	}

	public static AttributeSupplier.Builder prepareAttributes() {
		return LivingEntity.createLivingAttributes()
				.add(Attributes.ATTACK_DAMAGE, 1.0d)
				.add(Attributes.MAX_HEALTH, 16.0d)
				.add(Attributes.FOLLOW_RANGE, 64.0d)
				.add(Attributes.MOVEMENT_SPEED, 0.25d)
				.add(Attributes.ATTACK_KNOCKBACK, 1d)
				.add(ForgeMod.SWIM_SPEED.get(), 3d);
	}
}
