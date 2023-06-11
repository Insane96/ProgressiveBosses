package insane96mcp.progressivebosses.module.wither.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.utils.DifficultyHelper;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Rewards", description = "Bonus Experience and Drops")
@LoadFeature(module = ProgressiveBosses.RESOURCE_PREFIX + "wither")
public class RewardFeature extends Feature {
	@Config(min = 0d)
	@Label(name = "Bonus Experience", description = "How much more experience (percentage, 60 means +6000%) will Wither drop at max Difficulty.")
	public static Double bonusExperience = 60d;
	@Config
	@Label(name = "Inject Default Loot", description = "If true default mod drops are added to the Wither.\n" +
			"Note that replacing the Wither loot table (e.g. via DataPack) will automatically remove the Injected loot.")
	public static Boolean injectDefaultRewards = true;

	public RewardFeature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@SubscribeEvent
	public void onSpawn(EntityJoinLevelEvent event) {
		if (event.getLevel().isClientSide
				|| !this.isEnabled()
				|| bonusExperience == 0d
				|| !(event.getEntity() instanceof WitherBoss wither))
			return;

		wither.xpReward = 50 + (int) (50 * (bonusExperience * DifficultyHelper.getScalingDifficulty(wither)));
	}

	/*@SubscribeEvent
	public void onLootTableLoad(LootTableLoadEvent event) {
		if (!this.isEnabled() || !injectDefaultRewards)
			return;

		ResourceLocation name = event.getName();
		if (!"minecraft".equals(name.getNamespace()) || !"entities/wither".equals(name.getPath()))
			return;

		LootPool pool = new LootPool.Builder().setRolls(ConstantValue.exactly(1)).add(LootTableReference.lootTableReference(new ResourceLocation(ProgressiveBosses.MOD_ID, "entities/wither"))).build();
		event.getTable().addPool(pool);
	}*/
}
