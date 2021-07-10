package insane96mcp.progressivebosses.module.dragon.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.progressivebosses.base.Strings;
import insane96mcp.progressivebosses.classutils.Drop;
import insane96mcp.progressivebosses.setup.Config;
import net.minecraft.block.Blocks;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

@Label(name = "Rewards", description = "Bonus Experience and Dragon Egg per player")
public class RewardFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Double> bonusExperienceConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> dragonEggPerPlayerConfig;
	private final ForgeConfigSpec.ConfigValue<List<? extends String>> dropsListConfig;

	public double bonusExperience = 1.0d;
	public boolean dragonEggPerPlayer = true;
	public ArrayList<Drop> dropsList;

	public RewardFeature(Module module) {
		super(Config.builder, module);
		Config.builder.comment(this.getDescription()).push(this.getName());
		bonusExperienceConfig = Config.builder
				.comment("How much more experience (percentage) will Dragon drop per Difficulty. The percentage is additive (e.g. with this set to 100%, 7 dragons killed = 700% more experience)")
				.defineInRange("Bonus Experience per Difficulty", bonusExperience, 0.0, Double.MAX_VALUE);
		dragonEggPerPlayerConfig = Config.builder
				.comment("If true whenever a player, that has never killed the dragon, kills the dragon a Dragon Egg ìì will drop. E.g. If 2 players kill the Dragon for the first time, she will drop 2 Dragon Eggs")
				.define("Dragon Egg per Player", dragonEggPerPlayer);
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
						"By default Withers have 2% chance per difficulty >= 2 to drop 1 shard + 4% chance per difficulty >= 4 to drop 2 shards + 8% chance per difficulty >= 8 to drop 4 shards.")
				.defineList("Drops", ArrayList::new, o -> o instanceof String);
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

		if (!(event.getEntity() instanceof EnderDragonEntity))
			return;

		EnderDragonEntity dragon = (EnderDragonEntity) event.getEntity();

		dropExperience(dragon);
		dropEgg(dragon);
	}

	private void dropExperience(EnderDragonEntity dragon) {
		if (this.bonusExperience == 0d)
			return;

		if (dragon.deathTicks <= 150 || dragon.deathTicks % 5 != 0)
			return;

		if (dragon.getFightManager() == null)
			return;

		CompoundNBT dragonTags = dragon.getPersistentData();

		float difficulty = dragonTags.getFloat(Strings.Tags.DIFFICULTY);
		if (difficulty == 0d)
			return;

		//Drop 8% of experience each tick (for 10 ticks) + 20% at the last tick
		int bonusXP = (int) (500 * this.bonusExperience * difficulty * 0.08);
		if (dragon.deathTicks == 200)
			bonusXP += (int) (500 * this.bonusExperience * difficulty * 0.2);

		while (bonusXP > 0) {
			int i = ExperienceOrbEntity.getXPSplit(bonusXP);
			bonusXP -= i;
			dragon.world.addEntity(new ExperienceOrbEntity(dragon.world, dragon.getPositionVec().getX(), dragon.getPositionVec().getY(), dragon.getPositionVec().getZ(), i));
		}
	}

	private void dropEgg(EnderDragonEntity dragon) {
		if (!this.dragonEggPerPlayer)
			return;

		if (dragon.deathTicks != 100)
			return;

		CompoundNBT tags = dragon.getPersistentData();

		int eggsToDrop = tags.getInt(Strings.Tags.EGGS_TO_DROP);

		if (dragon.getFightManager() != null && !dragon.getFightManager().hasPreviouslyKilledDragon()) {
			eggsToDrop--;
		}

		for (int i = 0; i < eggsToDrop; i++) {
			dragon.world.setBlockState(new BlockPos(0, 255 - i, 0), Blocks.DRAGON_EGG.getDefaultState());
		}
	}

	@SubscribeEvent
	public void onDeath(LivingDeathEvent event) {
		if (!this.isEnabled())
			return;

		if (this.dropsList.isEmpty())
			return;

		if (!(event.getEntityLiving() instanceof EnderDragonEntity))
			return;

		EnderDragonEntity dragon = (EnderDragonEntity) event.getEntityLiving();

		CompoundNBT tags = dragon.getPersistentData();
		float difficulty = tags.getFloat(Strings.Tags.DIFFICULTY);
		for (Drop drop : this.dropsList) {
			drop.drop(dragon.world, dragon.getPositionVec(), difficulty);
		}
	}

}
