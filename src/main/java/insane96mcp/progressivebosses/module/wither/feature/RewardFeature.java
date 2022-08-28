package insane96mcp.progressivebosses.module.wither.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.setup.Config;
import insane96mcp.progressivebosses.utils.DifficultyHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Rewards", description = "Bonus Experience and Drops")
public class RewardFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Double> bonusExperienceConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> injectDefaultRewardsConfig;

	public double bonusExperience = 60d;
	public boolean injectDefaultRewards = true;

	public RewardFeature(Module module) {
		super(Config.builder, module);
		this.pushConfig(Config.builder);
		bonusExperienceConfig = Config.builder
				.comment("How much more experience (percentage, 60 means +6000%) will Wither drop at max Difficulty.")
				.defineInRange("Bonus Experience ", bonusExperience, 0.0, Double.MAX_VALUE);
		injectDefaultRewardsConfig = Config.builder
				.comment("If true default mod drops are added to the Wither.\n" +
						"Note that replacing the Wither loot table (e.g. via DataPack) will automatically remove the Injected loot.")
				.define("Injecet Default Loot", this.injectDefaultRewards);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.bonusExperience = this.bonusExperienceConfig.get();
		this.injectDefaultRewards = this.injectDefaultRewardsConfig.get();
	}

	@SubscribeEvent
	public void onSpawn(EntityJoinWorldEvent event) {
		if (event.getWorld().isClientSide
				|| !this.isEnabled()
				|| this.bonusExperience == 0d
				|| !(event.getEntity() instanceof WitherBoss wither))
			return;

		wither.xpReward = 50 + (int) (50 * (this.bonusExperience * DifficultyHelper.getScalingDifficulty(wither)));
	}

	@SubscribeEvent
	public void onLootTableLoad(LootTableLoadEvent event) {
		if (!this.isEnabled() || !this.injectDefaultRewards)
			return;

		ResourceLocation name = event.getName();
		if (!"minecraft".equals(name.getNamespace()) || !"entities/wither".equals(name.getPath()))
			return;

		LootPool pool = new LootPool.Builder().setRolls(ConstantValue.exactly(1)).add(LootTableReference.lootTableReference(new ResourceLocation(ProgressiveBosses.MOD_ID, "entities/wither"))).build();
		event.getTable().addPool(pool);
	}
}
