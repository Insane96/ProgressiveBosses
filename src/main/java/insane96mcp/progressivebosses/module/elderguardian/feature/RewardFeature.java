package insane96mcp.progressivebosses.module.elderguardian.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.progressivebosses.ProgressiveBosses;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Rewards", description = "Bonus Experience and Dragon Egg per player")
@LoadFeature(module = ProgressiveBosses.RESOURCE_PREFIX + "elder_guardian")
public class RewardFeature extends Feature {
	@Config(min = -1, max = 1024)
	@Label(name = "Base Experience", description = "How much experience will an Elder Guardian drop. -1 will make the Elder Guardian drop vanilla experience.")
	public int baseExperience = 40;
	@Config(min = 0f)
	@Label(name = "Bonus Experience", description = "How much more experience (percentage) will Elder Guardian drop per killed Elder Guardian. The percentage is additive (e.g. with this set to 100%, the last Elder will drop 200% more experience)")
	public double bonusExperience = 1.0d;
	@Config(min = -1, max = 1024)
	@Label(name = "Inject Default Loot", description = "If true default mod drops are added to the Elder Guardian.\n" +
			"Note that replacing the Elder Guardian loot table (e.g. via DataPack) will automatically remove the Injected loot.")
	public boolean injectDefaultRewards = true;

	public RewardFeature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@SubscribeEvent
	public void onSpawn(EntityJoinLevelEvent event) {
		if (event.getLevel().isClientSide
				|| !this.isEnabled()
				|| this.baseExperience == -1d
				|| !(event.getEntity() instanceof ElderGuardian elderGuardian))
			return;

		elderGuardian.xpReward = this.baseExperience;
	}

	@SubscribeEvent
	public void onExperienceDrop(LivingExperienceDropEvent event) {
		if (!this.isEnabled()
				|| this.bonusExperience == 0d
				|| !(event.getEntity() instanceof ElderGuardian))
			return;

		int bonusExperience = (int) (event.getOriginalExperience() * (this.bonusExperience));
		event.setDroppedExperience(event.getOriginalExperience() + bonusExperience);
	}

	@SubscribeEvent
	public void onLootTableLoad(LootTableLoadEvent event) {
		if (!this.isEnabled()
				|| !this.injectDefaultRewards)
			return;

		ResourceLocation name = event.getName();
		if (!"minecraft".equals(name.getNamespace()) || !"entities/elder_guardian".equals(name.getPath()))
			return;

		LootPool pool = new LootPool.Builder().setRolls(ConstantValue.exactly(1)).add(LootTableReference.lootTableReference(new ResourceLocation(ProgressiveBosses.MOD_ID, "entities/elder_guardian"))).build();
		event.getTable().addPool(pool);
	}

}
