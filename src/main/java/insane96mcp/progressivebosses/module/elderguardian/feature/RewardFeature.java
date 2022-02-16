package insane96mcp.progressivebosses.module.elderguardian.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.progressivebosses.classutils.Drop;
import insane96mcp.progressivebosses.setup.Config;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Label(name = "Rewards", description = "Bonus Experience and Dragon Egg per player")
public class RewardFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Integer> baseExperienceConfig;
	private final ForgeConfigSpec.ConfigValue<Double> bonusExperienceConfig;
	private final ForgeConfigSpec.ConfigValue<List<? extends String>> dropsListConfig;

	public int baseExperience = 40;
	public double bonusExperience = 1.0d;
	public ArrayList<Drop> dropsList;

	private static final List<String> dropsListDefault = Arrays.asList("minecraft:wet_sponge,1,0,1,MINIMUM,FLAT", "minecraft:wet_sponge,2,1,1,MINIMUM,FLAT", "minecraft:wet_sponge,2,2,1,MINIMUM,FLAT", "progressivebosses:elder_guardian_spike,1,0,1,MINIMUM,FLAT");

	public RewardFeature(Module module) {
		super(Config.builder, module);
		this.pushConfig(Config.builder);
		baseExperienceConfig = Config.builder
				.comment("How much experience will an Elder Guardian drop.")
				.defineInRange("Base Experience", this.baseExperience, 0, 1024);
		bonusExperienceConfig = Config.builder
				.comment("How much more experience (percentage) will Elder Guardian drop per killed Elder Guardian. The percentage is additive (e.g. with this set to 100%, the last Elder will drop 200% more experience)")
				.defineInRange("Bonus Experience", bonusExperience, 0.0, Double.MAX_VALUE);
		dropsListConfig = Config.builder
				.comment("A list of bonus drops for the Elder Guardian. Entry format: item,amount,missing_guardians,chance,mode,chance_mode\n" +
						"item: item id\n" +
						"amount: amount\n" +
						"missing_guardians: the amount of missing guardians required for the item to drop, works differently based on mode\n" +
						"chance: chance for the drop to happen, between 0 and 1\n" +
						"mode:\n" +
						"* MINIMUM: will try to drop the item when the missing_guardians matches or is higher\n" +
						"* PER_DIFFICULTY: will try to drop the item one more time per missing_guardians\n" +
						"chance_mode:\n" +
						"* FLAT: chance is the percentage chance for the item to drop if the difficulty criteria matches\n" +
						"* SCALING: each point of difficulty >= 'difficulty to drop the item' will be multiplied by the chance (e.g. chance 2% and difficulty 10, difficulty required 5, chance to drop the item will be chance * (difficulty - difficulty_required + 1) = 2% * (10 - 5 + 1) = 12%)\n")
				.defineList("Drops", dropsListDefault, o -> o instanceof String);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.baseExperience = this.baseExperienceConfig.get();
		this.bonusExperience = this.bonusExperienceConfig.get();
		this.dropsList = Drop.parseDropsList(this.dropsListConfig.get());
	}

	@SubscribeEvent
	public void onSpawn(EntityJoinWorldEvent event) {
		if (event.getWorld().isClientSide)
			return;

		if (!this.isEnabled())
			return;

		if (this.baseExperience == 0d)
			return;

		if (!(event.getEntity() instanceof ElderGuardian))
			return;

		ElderGuardian elderGuardian = (ElderGuardian) event.getEntity();

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
	public void onDeath(LivingDeathEvent event) {
		if (!this.isEnabled())
			return;

		if (this.dropsList.isEmpty())
			return;

		if (!(event.getEntityLiving() instanceof ElderGuardian))
			return;

		ElderGuardian elderGuardian = (ElderGuardian) event.getEntityLiving();

		for (Drop drop : this.dropsList) {
			drop.drop(elderGuardian.level, elderGuardian.position(), BaseFeature.getDeadElderGuardians(elderGuardian));
		}
	}

}
