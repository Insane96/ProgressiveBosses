package insane96mcp.progressivebosses.modules.wither.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.progressivebosses.ai.WitherRangedAttackGoal;
import insane96mcp.progressivebosses.base.Strings;
import insane96mcp.progressivebosses.setup.Config;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.RangedAttackGoal;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;

@Label(name = "Attack", description = "Makes the Wither smarter (will no longer try to stand on the player's head ...) and attack faster below Half Health")
public class AttackFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Boolean> applyToVanillaWitherConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> twiceAttackSpeedOnHalfHealthConfig;

	public boolean applyToVanillaWither = true;
	public boolean twiceAttackSpeedOnHalfHealth = true;

	public AttackFeature(Module module) {
		super(Config.builder, module);
		Config.builder.comment(this.getDescription()).push(this.getName());
		applyToVanillaWitherConfig = Config.builder
				.comment("If the AI changes should be applied to the first wither spawned too.")
				.define("Apply to Vanilla Wither", applyToVanillaWither);
		twiceAttackSpeedOnHalfHealthConfig = Config.builder
				.comment("The middle head will attack twice as fast when the Wither drops below half health.")
				.define("Twice Attack Speed on Half Health", twiceAttackSpeedOnHalfHealth);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.applyToVanillaWither = this.applyToVanillaWitherConfig.get();
		this.twiceAttackSpeedOnHalfHealth = this.twiceAttackSpeedOnHalfHealthConfig.get();
	}

	@SubscribeEvent
	public void onSpawn(EntityJoinWorldEvent event) {
		if (event.getWorld().isRemote)
			return;

		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof WitherEntity))
			return;

		WitherEntity wither = (WitherEntity) event.getEntity();
		CompoundNBT compoundNBT = wither.getPersistentData();
		if ((!compoundNBT.contains(Strings.Tags.DIFFICULTY) || compoundNBT.getFloat(Strings.Tags.DIFFICULTY) == 0f) && !this.applyToVanillaWither)
			return;

		setWitherAI(wither);
	}

	public void setWitherAI(WitherEntity wither) {
		ArrayList<Goal> toRemove = new ArrayList<>();
		wither.goalSelector.goals.forEach(goal -> {
			if (goal.getGoal() instanceof RangedAttackGoal)
				toRemove.add(goal.getGoal());
		});

		toRemove.forEach(wither.goalSelector::removeGoal);

		wither.goalSelector.addGoal(2, new WitherRangedAttackGoal(wither,  40, 20.0f, this.twiceAttackSpeedOnHalfHealth));

		//Fixes https://bugs.mojang.com/browse/MC-29274
		wither.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(wither, PlayerEntity.class, 0, false, false, null));
	}
}
