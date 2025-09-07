package insane96mcp.progressivebosses.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import insane96mcp.progressivebosses.module.dragon.data.*;
import insane96mcp.progressivebosses.module.dragon.phase.DragonBlastAttackPhase;
import insane96mcp.progressivebosses.module.dragon.phase.DragonCrystalRespawnPhase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhaseManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

@Mixin(value = EnderDragon.class, priority = 1001)
public abstract class EnderDragonMixin extends Mob {

	@Shadow public float oFlapTime;

	@Mutable
	@Shadow @Final private EnderDragonPart[] subEntities;

	@Mutable
	@Shadow @Final public EnderDragonPart head;

	@Shadow @Nullable public EndCrystal nearestCrystal;

	@Shadow public float flapTime;

	@Shadow public abstract EnderDragonPhaseManager getPhaseManager();

	@Shadow(remap = false) private @org.jetbrains.annotations.Nullable Player unlimitedLastHurtByPlayer;

	@Shadow @Final private EnderDragonPart tail1;

	protected EnderDragonMixin(EntityType<? extends Mob> type, Level worldIn) {
		super(type, worldIn);
	}

	@ModifyExpressionValue(method = "<init>", at = @At(value = "CONSTANT", args = "floatValue=1.0"))
    private float onHeadSize(float original) {
		if (!DragonFeature.areFixesEnabled())
			return original;
		return 1.5f;
	}

	@WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/boss/enderdragon/EnderDragon;reallyHurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"), method = "hurt(Lnet/minecraft/world/entity/boss/EnderDragonPart;Lnet/minecraft/world/damagesource/DamageSource;F)Z")
	private boolean progressivebosses$preventDyingInPlaceWhenRespawningCrystals(EnderDragon instance, DamageSource pDamageSource, float pAmount, Operation<Boolean> original) {
		if (this.isDeadOrDying() && instance.getPhaseManager().getCurrentPhase().getPhase().equals(DragonCrystalRespawnPhase.getPhaseType())) {
			instance.setHealth(1.0F);
			instance.getPhaseManager().setPhase(EnderDragonPhase.DYING);
		}
		return original.call(instance, pDamageSource, pAmount);
	}

	@WrapOperation(method = "knockBack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;push(DDD)V"))
	private void progressivebosses$hurtMarkKnockbackedEntities(Entity instance, double pX, double pY, double pZ, Operation<Void> original) {
		original.call(instance, pX, pY, pZ);
		if (DragonFeature.areFixesEnabled())
			instance.hurtMarked = true;
	}

	@WrapOperation(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/boss/enderdragon/EnderDragon;knockBack(Ljava/util/List;)V"))
	public void progressivebosses$preventKnockbackWhenBlasting(EnderDragon instance, List<Entity> entities, Operation<Void> original) {
		if (this.getPhaseManager().getCurrentPhase().getPhase() == DragonBlastAttackPhase.getPhaseType())
			return;
		original.call(instance, entities);
	}

	@Definition(id = "entity", local = @Local(type = Entity.class))
	@Definition(id = "LivingEntity", type = LivingEntity.class)
	@Expression("entity instanceof LivingEntity")
	@WrapOperation(method = "hurt(Ljava/util/List;)V", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
	public boolean progressivebosses$headOnTryHurtEntity(Object object, Operation<Boolean> original, @Local Entity entity) {
		if (!DragonFeature.areFixesEnabled())
			return original.call(object);
		boolean isLiving = original.call(object);
		if (!isLiving)
			return false;
		LivingEntity livingEntity = (LivingEntity) entity;
		return livingEntity.getLastHurtByMobTimestamp() < livingEntity.tickCount - 10 || livingEntity.getLastHurtByMob() != this;
	}

	@Definition(id = "LivingEntity", type = LivingEntity.class)
	@Definition(id = "entity", local = @Local(type = Entity.class))
	@Definition(id = "getLastHurtByMobTimestamp", method = "Lnet/minecraft/world/entity/LivingEntity;getLastHurtByMobTimestamp()I")
	@Definition(id = "tickCount", field = "Lnet/minecraft/world/entity/Entity;tickCount:I")
	@Expression("((LivingEntity)entity).getLastHurtByMobTimestamp() < entity.tickCount - 2")
	@ModifyExpressionValue(method = "knockBack", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
	public boolean progressivebosses$changeHurtCooldown(boolean original, @Local Entity entity) {
		if (!DragonFeature.areFixesEnabled())
			return original;

        return ((LivingEntity)entity).getLastHurtByMobTimestamp() < entity.tickCount - 10 || ((LivingEntity) entity).getLastHurtByMob() != this;
	}

	@ModifyExpressionValue(method = "checkCrystals", at = @At(value = "CONSTANT", args = "floatValue=1.0"))
	public float onCrystalHeal(float original) {
		return DragonFeature.getDragonDefinition((EnderDragon) (Object) this)
				.flatMap(stats -> stats.getComponent(HealthComponent.class))
				.map(healthComponent -> healthComponent.getHealingFromCrystal((EnderDragon) (Object) this, this.nearestCrystal, original))
				.orElse(original);
	}

	@ModifyExpressionValue(method = "hurt(Lnet/minecraft/world/entity/boss/EnderDragonPart;Lnet/minecraft/world/damagesource/DamageSource;F)Z", at = @At(value = "CONSTANT", args = "floatValue=0.25f"))
	public float progressivebosses$maxSittingDamageReceived(float original) {
		return DragonFeature.getDragonDefinition((EnderDragon) (Object) this)
				.flatMap(stats -> stats.getComponent(SittingAttackComponent.class))
				.flatMap(component -> Optional.ofNullable(component.damageBeforeTakeOff))
				.map(damageBeforeTakeOff -> damageBeforeTakeOff.getValue((EnderDragon) (Object) this))
				.orElse(original);
	}

	@ModifyExpressionValue(method = "onCrystalDestroyed", at = @At(value = "CONSTANT", args = "floatValue=10.0"))
	public float onAttachedCrystalDamage(float original, EndCrystal pCrystal, BlockPos pPos, DamageSource pDamageSource) {
		if (!pCrystal.showsBottom())
			return original;
		return DragonFeature.getDragonDefinition((EnderDragon) (Object) this)
				.flatMap(stats -> stats.getComponent(VulnerabilitiesComponent.class))
				.map(vulnerabilitiesComponent -> vulnerabilitiesComponent.getAttachedCrystalDamage(pCrystal, (EnderDragon) (Object) this))
				.orElse(original);
	}

	@ModifyExpressionValue(method = "aiStep", at = @At(value = "CONSTANT", args = "doubleValue=0.01"))
	public double onYDeltaSpeed(double original) {
		if (!DragonFeature.areFixesEnabled())
			return original;
		return 0.075d;
	}

	@ModifyExpressionValue(method = "aiStep", at = @At(value = "CONSTANT", args = "floatValue=0.06"))
	public float progressivebosses$movementSpeedMultiplier(float original) {
		return original * DragonFeature.getDragonDefinition((EnderDragon) (Object) this)
				.flatMap(definition -> definition.getComponent(FlySpeedComponent.class))
				.map(flySpeedComponent -> flySpeedComponent.getFlySpeedMultiplier((EnderDragon) (Object) this))
				.orElse(1f);
	}

	@Unique
	private DamageSource progressiveBosses$killerDamageSource;

	@Inject(method = "hurt(Lnet/minecraft/world/entity/boss/EnderDragonPart;Lnet/minecraft/world/damagesource/DamageSource;F)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/boss/enderdragon/phases/EnderDragonPhaseManager;setPhase(Lnet/minecraft/world/entity/boss/enderdragon/phases/EnderDragonPhase;)V", shift = At.Shift.AFTER, ordinal = 0))
	public void progressivebosses$storeKillerDamageSource(EnderDragonPart pPart, DamageSource pSource, float pDamage, CallbackInfoReturnable<Boolean> cir) {
		this.progressiveBosses$killerDamageSource = pSource;
	}

	@Inject(method = "tickDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ExperienceOrb;award(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;I)V", shift = At.Shift.AFTER, ordinal = 1))
	public void progressivebosses$dropDeathLoot(CallbackInfo ci) {
		if (progressiveBosses$killerDamageSource == null)
			return;
		this.lootTable = DragonFeature.getDragonDefinition((EnderDragon) (Object) this)
				.flatMap(definition -> definition.getComponent(LootComponent.class))
				.map(lootComponent -> lootComponent.lootTable)
				.orElse(LootComponent.VANILLA_LOOT_TABLE);
		this.dropFromLootTable(progressiveBosses$killerDamageSource, false);
	}

	@ModifyVariable(method = "hurt(Lnet/minecraft/world/entity/boss/EnderDragonPart;Lnet/minecraft/world/damagesource/DamageSource;F)Z", at = @At(value = "STORE", ordinal = 0), argsOnly = true)
	public float onDamageAmount(float original, EnderDragonPart part, DamageSource source, float amount) {
		if (!part.name.equals("wing") && !part.name.equals("neck") && !part.name.equals("tail"))
			return original;
		return original * 1.5f;
	}

	@ModifyExpressionValue(method = "hurt(Ljava/util/List;)V", at = @At(value = "CONSTANT", args = "floatValue=10.0"))
	public float progressiveBosses$headDamage(float original) {
		return DragonFeature.getDragonDefinition((EnderDragon) (Object) this)
				.flatMap(stats -> stats.getComponent(MeleeDamageComponent.class))
				.flatMap(component -> Optional.ofNullable(component.headDamage))
				.map(headDamage -> headDamage.getValue((EnderDragon) (Object) this))
				.orElse(original);
	}

	@ModifyExpressionValue(method = "knockBack(Ljava/util/List;)V", at = @At(value = "CONSTANT", args = "floatValue=5.0"))
	public float progressiveBosses$bodyDamage(float original) {
		return DragonFeature.getDragonDefinition((EnderDragon) (Object) this)
				.flatMap(stats -> stats.getComponent(MeleeDamageComponent.class))
				.flatMap(component -> Optional.ofNullable(component.wingDamage))
				.map(wingDamage -> wingDamage.getValue((EnderDragon) (Object) this))
				.orElse(original);
	}

	@ModifyExpressionValue(method = "aiStep", at = @At(value = "CONSTANT", args = "floatValue=5.5", ordinal = 0))
	public float neckOffsetX(float original) {
		if (!DragonFeature.areFixesEnabled())
			return original;
		return DragonFeature.neckOffsetXZ();
	}

	@ModifyExpressionValue(method = "aiStep", at = @At(value = "CONSTANT", args = "floatValue=5.5", ordinal = 2))
	public float neckOffsetZ(float original) {
		if (!DragonFeature.areFixesEnabled())
			return original;
		return DragonFeature.neckOffsetXZ();
	}

	@ModifyExpressionValue(method = "aiStep", at = @At(value = "CONSTANT", args = "floatValue=1.5", ordinal = 3))
	public float tailOffsetY(float original) {
		if (!DragonFeature.areFixesEnabled())
			return original;
		return DragonFeature.tailOffsetY();
	}

	@ModifyExpressionValue(method = "aiStep", at = @At(value = "CONSTANT", args = "floatValue=6.5", ordinal = 0))
	public float headOffsetX(float original) {
		if (!DragonFeature.areFixesEnabled())
			return original;
		return DragonFeature.headOffsetXZ();
	}

	@ModifyReturnValue(method = "getHeadYOffset", at = @At(value = "RETURN", ordinal = 1))
	public float headOffsetY(float original) {
		if (!DragonFeature.areFixesEnabled())
			return original;
		return DragonFeature.headOffsetY(original);
	}

	@ModifyExpressionValue(method = "aiStep", at = @At(value = "CONSTANT", args = "floatValue=6.5", ordinal = 2))
	public float headOffsetZ(float original) {
		if (!DragonFeature.areFixesEnabled())
			return original;
		return DragonFeature.headOffsetXZ();
	}

	@ModifyReturnValue(method = "getHeadYOffset", at = @At(value = "RETURN", ordinal = 0))
	public float headOffsetSittingY(float original) {
		if (!DragonFeature.areFixesEnabled())
			return original;
		return DragonFeature.headOffsetSittingY();
	}

	@ModifyExpressionValue(method = "getHeadYOffset", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/boss/enderdragon/phases/DragonPhaseInstance;isSitting()Z"))
	public boolean progressivebosses$headYOffsetExplosion(boolean original) {
		return original && this.getPhaseManager().getCurrentPhase().getPhase() != DragonBlastAttackPhase.getPhaseType();
	}

	@ModifyExpressionValue(method = "getHeadLookVector", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/boss/enderdragon/phases/DragonPhaseInstance;isSitting()Z"))
	public boolean progressivebosses$headLookVectorExplosion(boolean original) {
		return original && this.getPhaseManager().getCurrentPhase().getPhase() != DragonBlastAttackPhase.getPhaseType();
	}

	@ModifyExpressionValue(method = "getHeadPartYOffset", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/boss/enderdragon/phases/DragonPhaseInstance;isSitting()Z"))
	public boolean progressivebosses$headPartYOffsetExplosion(boolean original) {
		return original && this.getPhaseManager().getCurrentPhase().getPhase() != DragonBlastAttackPhase.getPhaseType();
	}
}
