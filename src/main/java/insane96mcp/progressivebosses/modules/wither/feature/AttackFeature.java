package insane96mcp.progressivebosses.modules.wither.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.progressivebosses.ai.WitherRangedAttackGoal;
import insane96mcp.progressivebosses.setup.Config;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.RangedAttackGoal;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;

@Label(name = "Attack", description = "Makes the Wither smarter and attack faster below Half Health")
public class AttackFeature extends Feature {

	//private final ForgeConfigSpec.ConfigValue<Double> explosionPowerBonusConfig;

	//public double explosionPowerBonus = 0.3d;

	public AttackFeature(Module module) {
		super(Config.builder, module);
		Config.builder.comment(this.getDescription()).push(this.getName());
		/*explosionPowerBonusConfig = Config.builder
				.comment("How much explosion power (after the invulnerability) will the Wither gain for each difficulty point. Explosion Radius is capped to 13. Base Wither Explosion Power is 7.0. Setting this to 0 will not increase the Wither Explosion Power.")
				.defineInRange("Explosion Power Bonus", explosionPowerBonus, 0d, 4d);*/
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		///explosionPowerBonus = explosionPowerBonusConfig.get();
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

		setWitherAI(wither);
	}

	public static void setWitherAI(WitherEntity wither) {
		ArrayList<Goal> toRemove = new ArrayList<>();
		wither.goalSelector.goals.forEach(goal -> {
			if (goal.getGoal() instanceof RangedAttackGoal)
				toRemove.add(goal.getGoal());
		});

		toRemove.forEach(wither.goalSelector::removeGoal);

		wither.goalSelector.addGoal(2, new WitherRangedAttackGoal(wither, 1.0D, 40, 20.0f));

		//Fixes https://bugs.mojang.com/browse/MC-29274
		wither.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(wither, PlayerEntity.class, 0, false, false, null));
	}
}
