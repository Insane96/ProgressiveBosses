package insane96mcp.progressivebosses.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import insane96mcp.progressivebosses.module.dragon.data.DragonAnger;
import insane96mcp.progressivebosses.module.dragon.data.DragonStats;
import insane96mcp.progressivebosses.module.dragon.phase.CrystalRespawnPhase;
import insane96mcp.progressivebosses.module.dragon.phase.DragonBlastAttackPhase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
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

@Mixin(EnderDragon.class)
public abstract class EnderDragonMixin extends Mob {

	@Shadow public float oFlapTime;

	@Mutable
	@Shadow @Final private EnderDragonPart[] subEntities;

	@Mutable
	@Shadow @Final public EnderDragonPart head;

	@Shadow @Nullable public EndCrystal nearestCrystal;

	@Shadow public float flapTime;

	@Shadow public abstract EnderDragonPhaseManager getPhaseManager();

	@Shadow private @org.jetbrains.annotations.Nullable Player unlimitedLastHurtByPlayer;

	protected EnderDragonMixin(EntityType<? extends Mob> type, Level worldIn) {
		super(type, worldIn);
	}

	@ModifyExpressionValue(method = "<init>", at = @At(value = "CONSTANT", args = "floatValue=1.0"))
    private float onHeadSize(float original) {
		if (!Feature.isEnabled(DragonFeature.class)
				|| !DragonFeature.enableFixes)
			return original;
		return 1.5f;
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/boss/enderdragon/EnderDragon;reallyHurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z", shift = At.Shift.AFTER), method = "hurt(Lnet/minecraft/world/entity/boss/EnderDragonPart;Lnet/minecraft/world/damagesource/DamageSource;F)Z")
	private void onReallyHurt(EnderDragonPart part, DamageSource damageSource, float amount, CallbackInfoReturnable<Boolean> callbackInfo) {
		EnderDragon $this = (EnderDragon) (Object) this;
		if (this.isDeadOrDying() && $this.getPhaseManager().getCurrentPhase().getPhase().equals(CrystalRespawnPhase.getPhaseType())) {
			$this.setHealth(1.0F);
			$this.getPhaseManager().setPhase(EnderDragonPhase.DYING);
		}
	}

	@Inject(method = "knockBack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;push(DDD)V", shift = At.Shift.AFTER))
	private void hurtMarkKnockbackedEntities(List<Entity> pEntities, CallbackInfo ci, @Local Entity entity) {
		if (!Feature.isEnabled(DragonFeature.class)
				|| !DragonFeature.enableFixes)
			return;
		entity.hurtMarked = true;
	}

	@ModifyExpressionValue(method = "checkCrystals", at = @At(value = "CONSTANT", args = "floatValue=1.0"))
	public float onCrystalHeal(float original) {
		Optional<DragonStats> stats = DragonFeature.getDragonStats((EnderDragon) (Object) this);
        return stats.map(dragonStats -> dragonStats.health.getHealingFromCrystal((EnderDragon) (Object) this, this.nearestCrystal))
				.orElse(original);
    }

	@ModifyExpressionValue(method = "hurt(Lnet/minecraft/world/entity/boss/EnderDragonPart;Lnet/minecraft/world/damagesource/DamageSource;F)Z", at = @At(value = "CONSTANT", args = "floatValue=0.25f"))
	public float maxSittingDamageReceived(float original) {
		Optional<DragonStats> stats = DragonFeature.getDragonStats((EnderDragon) (Object) this);
		//Divided by 2 because it's healed twice per second
		return stats.map(dragonStats -> dragonStats.maxSittingDamageReceived).orElse(original);
	}

	@ModifyExpressionValue(method = "onCrystalDestroyed", at = @At(value = "CONSTANT", args = "floatValue=10.0"))
	public float onAttachedCrystalDamage(float original, EndCrystal pCrystal, BlockPos pPos, DamageSource pDamageSource) {
		if (!pCrystal.showsBottom())
			return original;
		return Math.max(this.getHealth() * 0.05f, this.getHealth() * 0.2f);
	}

	@ModifyExpressionValue(method = "aiStep", at = @At(value = "CONSTANT", args = "doubleValue=0.01"))
	public double onYDeltaSpeed(double original) {
		if (!Feature.isEnabled(DragonFeature.class)
				|| !DragonFeature.enableFixes)
			return original;
		return 0.08d;
	}

	@ModifyExpressionValue(method = "aiStep", at = @At(value = "CONSTANT", args = "floatValue=0.06"))
	public float progressivebosses$movementSpeedMultiplier(float original) {
		return original * DragonAnger.flySpeedMultiplier((EnderDragon) (Object) this);
	}

	@Unique
	private DamageSource progressiveBosses$killerDamageSource;

	@Inject(method = "tickDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ExperienceOrb;award(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;I)V", shift = At.Shift.AFTER, ordinal = 1))
	public void progressivebosses$dropDeathLoot(CallbackInfo ci) {
		this.dropFromLootTable(progressiveBosses$killerDamageSource, false);
		/*ResourceLocation resourcelocation = this.getLootTable();
        //noinspection DataFlowIssue - Calling this inside !isClientSide check
        LootTable loottable = this.level().getServer().getLootData().getLootTable(resourcelocation);
		LootParams.Builder lootparams$builder = (new LootParams.Builder((ServerLevel)this.level())).withParameter(LootContextParams.THIS_ENTITY, this).withParameter(LootContextParams.ORIGIN, this.position()).withParameter(LootContextParams.DAMAGE_SOURCE, progressiveBosses$killerDamageSource).withOptionalParameter(LootContextParams.KILLER_ENTITY, progressiveBosses$killerDamageSource.getEntity()).withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, progressiveBosses$killerDamageSource.getDirectEntity());
		if (this.unlimitedLastHurtByPlayer != null)
			lootparams$builder = lootparams$builder.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, this.unlimitedLastHurtByPlayer).withLuck(this.unlimitedLastHurtByPlayer.getLuck());

		LootParams lootparams = lootparams$builder.create(LootContextParamSets.ENTITY);
		loottable.getRandomItems(lootparams, this.getLootTableSeed(), stack -> {

		});*/
	}

	@Inject(method = "hurt(Lnet/minecraft/world/entity/boss/EnderDragonPart;Lnet/minecraft/world/damagesource/DamageSource;F)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/boss/enderdragon/phases/EnderDragonPhaseManager;setPhase(Lnet/minecraft/world/entity/boss/enderdragon/phases/EnderDragonPhase;)V", shift = At.Shift.AFTER, ordinal = 0))
	public void progressivebosses$storeKillerDamageSource(EnderDragonPart pPart, DamageSource pSource, float pDamage, CallbackInfoReturnable<Boolean> cir) {
		this.progressiveBosses$killerDamageSource = pSource;
	}

	@ModifyVariable(method = "hurt(Lnet/minecraft/world/entity/boss/EnderDragonPart;Lnet/minecraft/world/damagesource/DamageSource;F)Z", at = @At(value = "STORE", ordinal = 0), argsOnly = true)
	public float onDamageAmount(float original, EnderDragonPart part, DamageSource source, float amount) {
		if (!part.name.equals("wing") && !part.name.equals("neck") && !part.name.equals("tail"))
			return original;
		return original * 1.5f;
	}

	@ModifyExpressionValue(method = "aiStep", at = @At(value = "CONSTANT", args = "floatValue=5.5", ordinal = 0))
	public float neckOffsetX(float original) {
		if (!Feature.isEnabled(DragonFeature.class)
				|| !DragonFeature.enableFixes)
			return original;
		return DragonFeature.neckOffsetXZ();
	}

	@ModifyExpressionValue(method = "aiStep", at = @At(value = "CONSTANT", args = "floatValue=5.5", ordinal = 2))
	public float neckOffsetZ(float original) {
		if (!Feature.isEnabled(DragonFeature.class)
				|| !DragonFeature.enableFixes)
			return original;
		return DragonFeature.neckOffsetXZ();
	}

	@ModifyExpressionValue(method = "aiStep", at = @At(value = "CONSTANT", args = "floatValue=1.5", ordinal = 3))
	public float tailOffsetY(float original) {
		if (!Feature.isEnabled(DragonFeature.class)
				|| !DragonFeature.enableFixes)
			return original;
		return DragonFeature.tailOffsetY();
	}

	@ModifyExpressionValue(method = "aiStep", at = @At(value = "CONSTANT", args = "floatValue=6.5", ordinal = 0))
	public float headOffsetX(float original) {
		if (!Feature.isEnabled(DragonFeature.class)
				|| !DragonFeature.enableFixes)
			return original;
		return DragonFeature.headOffsetXZ();
	}

	@ModifyReturnValue(method = "getHeadYOffset", at = @At(value = "RETURN", ordinal = 1))
	public float headOffsetY(float original) {
		if (!Feature.isEnabled(DragonFeature.class)
				|| !DragonFeature.enableFixes)
			return original;
		return DragonFeature.headOffsetY(original);
	}

	@ModifyExpressionValue(method = "aiStep", at = @At(value = "CONSTANT", args = "floatValue=6.5", ordinal = 2))
	public float headOffsetZ(float original) {
		if (!Feature.isEnabled(DragonFeature.class)
				|| !DragonFeature.enableFixes)
			return original;
		return DragonFeature.headOffsetXZ();
	}

	@ModifyReturnValue(method = "getHeadYOffset", at = @At(value = "RETURN", ordinal = 0))
	public float headOffsetSittingY(float original) {
		if (!Feature.isEnabled(DragonFeature.class)
				|| !DragonFeature.enableFixes)
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
