package insane96mcp.progressivebosses.module.dragon.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.setup.Strings;
import insane96mcp.progressivebosses.utils.DifficultyHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Rewards", description = "Bonus Experience and Dragon Egg per player")
@LoadFeature(module = ProgressiveBosses.RESOURCE_PREFIX + "ender_dragon")
public class RewardFeature extends Feature {

	@Config(min = 0d)
	@Label(name = "Bonus Experience", description = "How much more experience (percentage, 36 means +3600%) will Dragon drop at max Difficulty.")
	public static Double bonusExperience = 36d;
	@Config
	@Label(name = "Dragon Egg per Player", description = "If true whenever a player, that has never killed the dragon, kills the dragon a Dragon Egg will drop. E.g. If 2 players kill the Dragon for the first time, she will drop 2 Dragon Eggs")
	public static Boolean dragonEggPerPlayer = true;

	public RewardFeature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@SubscribeEvent
	public void onUpdate(LivingEvent.LivingTickEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof EnderDragon dragon))
			return;

		dropEgg(dragon);
	}

	@SubscribeEvent
	public void onExpDrop(LivingExperienceDropEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof EnderDragon dragon)
				|| bonusExperience == 0d)
			return;
		float scalingDifficulty = DifficultyHelper.getScalingDifficulty(dragon);
		if (scalingDifficulty == 0)
			return;

		event.setDroppedExperience((int) (event.getDroppedExperience() * bonusExperience * scalingDifficulty));
	}

	private static void dropEgg(EnderDragon dragon) {
		if (!dragonEggPerPlayer
				|| dragon.dragonDeathTime != 100)
			return;

		CompoundTag tags = dragon.getPersistentData();

		int eggsToDrop = tags.getInt(Strings.Tags.EGGS_TO_DROP);

		if (dragon.getDragonFight() != null && !dragon.getDragonFight().hasPreviouslyKilledDragon()) {
			eggsToDrop--;
		}

		for (int i = 0; i < eggsToDrop; i++) {
			dragon.level().setBlockAndUpdate(new BlockPos(0, 255 - i, 0), Blocks.DRAGON_EGG.defaultBlockState());
		}
	}
}
