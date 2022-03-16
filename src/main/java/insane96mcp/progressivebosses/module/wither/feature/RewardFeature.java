package insane96mcp.progressivebosses.module.wither.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.progressivebosses.classutils.Drop;
import insane96mcp.progressivebosses.setup.Config;
import insane96mcp.progressivebosses.setup.Strings;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Label(name = "Rewards", description = "Bonus Experience and Drops")
public class RewardFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Double> bonusExperienceConfig;
	private final ForgeConfigSpec.ConfigValue<List<? extends String>> dropsListConfig;

	private static final List<String> dropsListDefault = Arrays.asList("progressivebosses:nether_star_shard,1,1,0.6,PER_DIFFICULTY,FLAT");

	public double bonusExperience = 7.5d;
	public ArrayList<Drop> dropsList;

	public RewardFeature(Module module) {
		super(Config.builder, module);
		this.pushConfig(Config.builder);
		bonusExperienceConfig = Config.builder
				.comment("How much more experience (percentage) will Wither drop per Difficulty. The percentage is additive (e.g. with this set to 200%, 7 withers spawned = 1400% more experience)")
				.defineInRange("Bonus Experience per Difficulty", bonusExperience, 0.0, Double.MAX_VALUE);
		dropsListConfig = Config.builder
				.comment("A list of drops for the Withers. Entry format: item,amount,difficulty_required,chance,difficulty_mode,chance_mode\n" +
						"item: item id\n" +
						"amount: amount\n" +
						"difficulty_required: the amount of difficulty required for the item to drop, works differently based on mode\n" +
						"chance: chance for the drop to happen, between 0 and 1\n" +
						"difficulty_mode:\n" +
						"* MINIMUM: will try to drop the item when the difficulty matches or is higher\n" +
						"* PER_DIFFICULTY: will try to drop the item once per difficulty (e.g. at difficulty 10, difficulty required 3, there is the chance to drop the item, trying 7 times)\n" +
						"chance_mode:\n" +
						"* FLAT: chance is the percentage chance for the item to drop if the difficulty criteria matches\n" +
						"* SCALING: each point of difficulty >= 'difficulty to drop the item' will be multiplied by the chance (e.g. chance 2% and difficulty 10, difficulty required 5, chance to drop the item will be chance * (difficulty - difficulty_required + 1) = 2% * (10 - 5 + 1) = 12%)\n" +
						"By default Withers have 60% chance per difficulty to drop 1 shard (So at difficulty 20, up to 20 shards can be dropped, 60% chance each).")
				.defineList("Drops", dropsListDefault, o -> o instanceof String);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.bonusExperience = this.bonusExperienceConfig.get();
		this.dropsList = Drop.parseDropsList(this.dropsListConfig.get());
	}

	@SubscribeEvent
	public void onSpawn(EntityJoinWorldEvent event) {
		if (event.getWorld().isClientSide)
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
	public void onDeath(LivingDeathEvent event) {
		if (!this.isEnabled())
			return;

		if (this.dropsList.isEmpty())
			return;

		if (!(event.getEntityLiving() instanceof WitherBoss wither))
			return;

		CompoundTag tags = wither.getPersistentData();
		float difficulty = tags.getFloat(Strings.Tags.DIFFICULTY);
		for (Drop drop : this.dropsList) {
			drop.drop(wither.level, wither.position(), difficulty);
		}
	}
}
