package insane96mcp.progressivebosses.module.dragon.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.progressivebosses.classutils.Drop;
import insane96mcp.progressivebosses.setup.Config;
import insane96mcp.progressivebosses.setup.Strings;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Label(name = "Rewards", description = "Bonus Experience and Dragon Egg per player")
public class RewardFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Double> bonusExperienceConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> dragonEggPerPlayerConfig;
	private final ForgeConfigSpec.ConfigValue<List<? extends String>> dropsListConfig;

	private static final List<String> dropsListDefault = Arrays.asList("minecraft:enchanted_golden_apple,1,5,0.10,MINIMUM,SCALING");

	public double bonusExperience = 4.5d;
	public boolean dragonEggPerPlayer = true;
	public ArrayList<Drop> dropsList;

	public RewardFeature(Module module) {
		super(Config.builder, module);
		this.pushConfig(Config.builder);
		bonusExperienceConfig = Config.builder
				.comment("How much more experience (percentage) will Dragon drop per Difficulty. The percentage is additive (e.g. with this set to 100%, 7 dragons killed = 700% more experience)")
				.defineInRange("Bonus Experience per Difficulty", bonusExperience, 0.0, Double.MAX_VALUE);
		dragonEggPerPlayerConfig = Config.builder
				.comment("If true whenever a player, that has never killed the dragon, kills the dragon a Dragon Egg ìì will drop. E.g. If 2 players kill the Dragon for the first time, she will drop 2 Dragon Eggs")
				.define("Dragon Egg per Player", dragonEggPerPlayer);
		dropsListConfig = Config.builder
				.comment("""
						A list of drops for the Dragons. Entry format: item,amount,difficulty_required,chance,difficulty_mode,chance_mode
						item: item id
						amount: amount
						difficulty_required: the amount of difficulty required for the item to drop, works differently based on mode
						chance: chance for the drop to happen, between 0 and 1
						difficulty_mode:
						* MINIMUM: will try to drop the item when the difficulty matches or is higher
						* PER_DIFFICULTY: will try to drop the item once per difficulty (e.g. at difficulty 10, difficulty required 3, the chance to drop the item is tried 7 times)
						chance_mode:
						* FLAT: chance is the percentage chance for the item to drop if the difficulty criteria matches
						* SCALING: each point of difficulty >= 'difficulty to drop the item' will be multiplied by the chance (e.g. difficulty 10, chance 2% and difficulty required 5, chance to drop the item will be chance * (difficulty - difficulty_required + 1) = 2% * (10 - 5 + 1) = 12%)""")
				.defineList("Drops", dropsListDefault, o -> o instanceof String);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.bonusExperience = this.bonusExperienceConfig.get();
		this.dragonEggPerPlayer = this.dragonEggPerPlayerConfig.get();
		this.dropsList = Drop.parseDropsList(this.dropsListConfig.get());
	}

	@SubscribeEvent
	public void onUpdate(LivingEvent.LivingUpdateEvent event) {
		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof EnderDragon dragon))
			return;

		dropEgg(dragon);
	}

	@SubscribeEvent
	public void onExpDrop(LivingExperienceDropEvent event) {
		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof EnderDragon dragon))
			return;

		if (this.bonusExperience == 0d)
			return;

		CompoundTag dragonTags = dragon.getPersistentData();
		float difficulty = dragonTags.getFloat(Strings.Tags.DIFFICULTY);
		if (difficulty == 0d)
			return;

		event.setDroppedExperience((int) (event.getDroppedExperience() * this.bonusExperience * difficulty));
	}

	private void dropEgg(EnderDragon dragon) {
		if (!this.dragonEggPerPlayer)
			return;

		if (dragon.dragonDeathTime != 100)
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
	public void onDeath(LivingDeathEvent event) {
		if (!this.isEnabled())
			return;

		if (this.dropsList.isEmpty())
			return;

		if (!(event.getEntityLiving() instanceof EnderDragon dragon))
			return;

		CompoundTag tags = dragon.getPersistentData();
		float difficulty = tags.getFloat(Strings.Tags.DIFFICULTY);
		for (Drop drop : this.dropsList) {
			drop.drop(dragon.level, dragon.position(), difficulty);
		}
	}

}
