package insane96mcp.progressivebosses.module.dragon.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.setup.Config;
import insane96mcp.progressivebosses.setup.Strings;
import insane96mcp.progressivebosses.utils.DifficultyHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Rewards", description = "Bonus Experience and Dragon Egg per player")
public class RewardFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Double> bonusExperienceConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> dragonEggPerPlayerConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> injectDefaultRewardsConfig;

	public double bonusExperience = 36d;
	public boolean dragonEggPerPlayer = true;
	public boolean injectDefaultRewards = true;

	public RewardFeature(Module module) {
		super(Config.builder, module);
		this.pushConfig(Config.builder);
		bonusExperienceConfig = Config.builder
				.comment("How much more experience (percentage, 36 means +3600%) will Dragon drop at max Difficulty.")
				.defineInRange("Bonus Experience", bonusExperience, 0.0, Double.MAX_VALUE);
		dragonEggPerPlayerConfig = Config.builder
				.comment("If true whenever a player, that has never killed the dragon, kills the dragon a Dragon Egg ìì will drop. E.g. If 2 players kill the Dragon for the first time, she will drop 2 Dragon Eggs")
				.define("Dragon Egg per Player", dragonEggPerPlayer);
		injectDefaultRewardsConfig = Config.builder
				.comment("If true default mod drops are added to the Ender Dragon.\n" +
						"Note that replacing the Ender Dragon loot table (e.g. via DataPack) will automatically remove the Injected loot.")
				.define("Inject Default Loot", this.injectDefaultRewards);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.bonusExperience = this.bonusExperienceConfig.get();
		this.dragonEggPerPlayer = this.dragonEggPerPlayerConfig.get();
		this.injectDefaultRewards = this.injectDefaultRewardsConfig.get();
	}

	@SubscribeEvent
	public void onUpdate(LivingEvent.LivingUpdateEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof EnderDragon dragon))
			return;

		dropEgg(dragon);
	}

	@SubscribeEvent
	public void onExpDrop(LivingExperienceDropEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof EnderDragon dragon)
				|| this.bonusExperience == 0d)
			return;

		event.setDroppedExperience((int) (event.getDroppedExperience() * this.bonusExperience * DifficultyHelper.getScalingDifficulty(dragon)));
	}

	private void dropEgg(EnderDragon dragon) {
		if (!this.dragonEggPerPlayer || dragon.dragonDeathTime != 100)
			return;

		CompoundTag tags = dragon.getPersistentData();

		int eggsToDrop = tags.getInt(Strings.Tags.EGGS_TO_DROP);

		if (dragon.getDragonFight() != null && !dragon.getDragonFight().hasPreviouslyKilledDragon()) {
			eggsToDrop--;
		}

		for (int i = 0; i < eggsToDrop; i++) {
			dragon.level.setBlockAndUpdate(new BlockPos(0, 255 - i, 0), Blocks.DRAGON_EGG.defaultBlockState());
		}
	}

	@SubscribeEvent
	public void onLootTableLoad(LootTableLoadEvent event) {
		if (!this.isEnabled() || !this.injectDefaultRewards)
			return;

		ResourceLocation name = event.getName();
		if (!"minecraft".equals(name.getNamespace()) || !"entities/ender_dragon".equals(name.getPath()))
			return;

		LootPool pool = new LootPool.Builder()
				.setRolls(ConstantValue.exactly(1))
				.add(LootTableReference.lootTableReference(new ResourceLocation(ProgressiveBosses.MOD_ID, "entities/ender_dragon")))
				.build();
		event.getTable().addPool(pool);
	}
}
