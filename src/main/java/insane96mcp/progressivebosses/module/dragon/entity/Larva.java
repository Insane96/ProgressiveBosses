package insane96mcp.progressivebosses.module.dragon.entity;

import insane96mcp.progressivebosses.module.Modules;
import insane96mcp.progressivebosses.module.dragon.ai.PBNearestAttackableTargetGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class Larva extends Monster {
	public Larva(EntityType<? extends Larva> p_32591_, Level p_32592_) {
		super(p_32591_, p_32592_);
		this.xpReward = 3;
	}

	protected void registerGoals() {
		this.goalSelector.addGoal(1, new FloatGoal(this));
		this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));
		this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D));
		this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new PBNearestAttackableTargetGoal(this));
	}

	protected float getStandingEyeHeight(Pose p_32604_, EntityDimensions p_32605_) {
		return 0.13F;
	}

	protected Entity.MovementEmission getMovementEmission() {
		return Entity.MovementEmission.EVENTS;
	}

	protected SoundEvent getAmbientSound() {
		return SoundEvents.ENDERMITE_AMBIENT;
	}

	protected SoundEvent getHurtSound(DamageSource p_32615_) {
		return SoundEvents.ENDERMITE_HURT;
	}

	protected SoundEvent getDeathSound() {
		return SoundEvents.ENDERMITE_DEATH;
	}

	protected void playStepSound(BlockPos p_32607_, BlockState p_32608_) {
		this.playSound(SoundEvents.ENDERMITE_STEP, 0.15F, 1.0F);
	}

	public void tick() {
		this.yBodyRot = this.getYRot();
		super.tick();
	}

	public void setYBodyRot(float p_32621_) {
		this.setYRot(p_32621_);
		super.setYBodyRot(p_32621_);
	}

	public double getMyRidingOffset() {
		return 0.1D;
	}

	@Override
	public boolean hurt(DamageSource damageSource, float amount) {
		if (Modules.dragon.larva.isEnabled() && Modules.dragon.larva.reducedDragonDamage && damageSource.getEntity() instanceof EnderDragon)
			return super.hurt(damageSource, amount * 0.1f);
		return super.hurt(damageSource, amount);
	}

	public @NotNull MobType getMobType() {
		return MobType.ARTHROPOD;
	}

	public static AttributeSupplier.Builder prepareAttributes() {
		return LivingEntity.createLivingAttributes()
				.add(Attributes.ATTACK_DAMAGE, 2.0d)
				.add(Attributes.MAX_HEALTH, 6.0d)
				.add(Attributes.FOLLOW_RANGE, 64.0d)
				.add(Attributes.MOVEMENT_SPEED, 0.44d)
				.add(Attributes.ATTACK_KNOCKBACK, 1.5d);
	}
}
