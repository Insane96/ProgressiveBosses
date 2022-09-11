package insane96mcp.progressivebosses.module.elderguardian.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.setup.Config;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Rewards", description = "Bonus Experience and Dragon Egg per player")
public class RewardFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Integer> baseExperienceConfig;
	private final ForgeConfigSpec.ConfigValue<Double> bonusExperienceConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> injectDefaultRewardsConfig;

	public int baseExperience = 40;
	public double bonusExperience = 1.0d;
	public boolean injectDefaultRewards = true;

	public RewardFeature(Module module) {
		super(Config.builder, module);
		this.pushConfig(Config.builder);
		baseExperienceConfig = Config.builder
				.comment("How much experience will an Elder Guardian drop. -1 will make the Elder Guardian drop vanilla experience.")
				.defineInRange("Base Experience", this.baseExperience, -1, 1024);
		bonusExperienceConfig = Config.builder
				.comment("How much more experience (percentage) will Elder Guardian drop per killed Elder Guardian. The percentage is additive (e.g. with this set to 100%, the last Elder will drop 200% more experience)")
				.defineInRange("Bonus Experience", bonusExperience, 0.0, Double.MAX_VALUE);
		injectDefaultRewardsConfig = Config.builder
				.comment("If true default mod drops are added to the Elder Guardian.\n" +
						"Note that replacing the Elder Guardian loot table (e.g. via DataPack) will automatically remove the Injected loot.")
				.define("Inject Default Loot", this.injectDefaultRewards);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.baseExperience = this.baseExperienceConfig.get();
		this.bonusExperience = this.bonusExperienceConfig.get();
		this.injectDefaultRewards = this.injectDefaultRewardsConfig.get();
	}

	@SubscribeEvent
	public void onSpawn(EntityJoinWorldEvent event) {
		if (event.getWorld().isClientSide)
			return;

		if (!this.isEnabled())
			return;

		if (this.baseExperience == -1d)
			return;

		if (!(event.getEntity() instanceof ElderGuardian elderGuardian))
			return;

		elderGuardian.xpReward = this.baseExperience;
	}

	@SubscribeEvent
	public void onExperienceDrop(LivingExperienceDropEvent event) {
		if (!this.isEnabled())
			return;

		if (this.bonusExperience == 0d)
			return;

		if (!(event.getEntity() instanceof ElderGuardian))
			return;

		int bonusExperience = (int) (event.getOriginalExperience() * (this.bonusExperience));
		event.setDroppedExperience(event.getOriginalExperience() + bonusExperience);
	}

	@SubscribeEvent
	public void onLootTableLoad(LootTableLoadEvent event) {
		if (!this.isEnabled() || !this.injectDefaultRewards)
			return;

		ResourceLocation name = event.getName();
		if (!"minecraft".equals(name.getNamespace()) || !"entities/elder_guardian".equals(name.getPath()))
			return;

		LootPool pool = new LootPool.Builder().setRolls(ConstantValue.exactly(1)).add(LootTableReference.lootTableReference(new ResourceLocation(ProgressiveBosses.MOD_ID, "entities/elder_guardian"))).build();
		event.getTable().addPool(pool);
	}

}
