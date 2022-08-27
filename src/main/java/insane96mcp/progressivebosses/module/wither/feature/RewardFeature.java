package insane96mcp.progressivebosses.module.wither.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.setup.Config;
import insane96mcp.progressivebosses.setup.Strings;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Rewards", description = "Bonus Experience and Drops")
public class RewardFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Double> bonusExperienceConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> injectDefaultRewardsConfig;

	public double bonusExperience = 7.5d;
	public boolean injectDefaultRewards = true;

	public RewardFeature(Module module) {
		super(Config.builder, module);
		this.pushConfig(Config.builder);
		bonusExperienceConfig = Config.builder
				.comment("How much more experience (percentage) will Wither drop per Difficulty. The percentage is additive (e.g. with this set to 200%, 7 withers spawned = 1400% more experience)")
				.defineInRange("Bonus Experience per Difficulty", bonusExperience, 0.0, Double.MAX_VALUE);
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
	public void onSpawn(EntityJoinLevelEvent event) {
		if (event.getLevel().isClientSide)
			return;

		if (!this.isEnabled())
			return;

		if (this.bonusExperience == 0d)
			return;

		if (!(event.getEntity() instanceof WitherBoss wither))
			return;

		CompoundTag witherTags = wither.getPersistentData();
		float difficulty = witherTags.getFloat(Strings.Tags.DIFFICULTY);

		wither.xpReward = 50 + (int) (50 * (this.bonusExperience * difficulty));
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
