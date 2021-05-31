package insane96mcp.progressivebosses.modules.wither.classutils;

import insane96mcp.insanelib.utils.LogHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.math.NumberUtils;

import javax.annotation.Nullable;

public class Drop {

	public ResourceLocation itemId;
	public int amount;
	public int difficultyRequired;
	public double chance;
	public DifficultyMode difficultyMode;
	public ChanceMode chanceMode;

	public Drop(ResourceLocation itemId, int amount, int difficultyRequired, double chance, DifficultyMode difficultyMode, ChanceMode chanceMode) {
		this.itemId = itemId;
		this.amount = amount;
		this.difficultyRequired = difficultyRequired;
		this.chance = chance;
		this.difficultyMode = difficultyMode;
		this.chanceMode = chanceMode;
	}

	@Nullable
	public static Drop parseLine(String line) {
		String[] split = line.split(",");
		if (split.length != 6) {
			LogHelper.warn("Invalid line \"%s\" for Drop", line);
			return null;
		}

		//Item
		ResourceLocation item = ResourceLocation.tryCreate(split[0]);
		if (item == null) {
			LogHelper.warn("%s item for Hoe Cooldown is not a valid Resource Location", split[0]);
			return null;
		}
		if (!ForgeRegistries.ITEMS.containsKey(item)) {
			LogHelper.warn("%s item for Drop seems to not exist", split[0]);
			return null;
		}

		//Amount
		if (!NumberUtils.isParsable(split[1])) {
			LogHelper.warn(String.format("Invalid amount \"%s\" for Drop", line));
			return null;
		}
		int amount = Integer.parseInt(split[1]);

		//Difficulty required
		if (!NumberUtils.isParsable(split[2])) {
			LogHelper.warn(String.format("Invalid difficulty_required \"%s\" for Drop", line));
			return null;
		}
		int difficultyRequired = Integer.parseInt(split[2]);

		//Chance
		if (!NumberUtils.isParsable(split[3])) {
			LogHelper.warn(String.format("Invalid chance \"%s\" for Drop", line));
			return null;
		}
		double chance = Double.parseDouble(split[3]);

		//Difficulty Mode
		DifficultyMode difficultyMode;
		try {
			difficultyMode = DifficultyMode.valueOf(split[4]);
		}
		catch (IllegalArgumentException e) {
			LogHelper.warn(String.format("Invalid difficulty_mode \"%s\" for Drop", line));
			return null;
		}

		//Chance Mode
		ChanceMode chanceMode;
		try {
			chanceMode = ChanceMode.valueOf(split[5]);
		}
		catch (IllegalArgumentException e) {
			LogHelper.warn(String.format("Invalid chance_mode \"%s\" for Drop", line));
			return null;
		}

		return new Drop(item, amount, difficultyRequired, chance, difficultyMode, chanceMode);
	}

	public enum DifficultyMode {
		MINIMUM,
		PER_DIFFICULTY
	}

	public enum ChanceMode {
		FLAT,
		SCALING
	}
}
