package insane96mcp.progressivebosses.module.elderguardian.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.utils.MCUtils;
import insane96mcp.progressivebosses.base.Strings;
import insane96mcp.progressivebosses.module.elderguardian.ai.ElderMinionNearestAttackableTargetGoal;
import insane96mcp.progressivebosses.setup.Config;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.monster.ElderGuardianEntity;
import net.minecraft.entity.monster.GuardianEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

@Label(name = "Minions", description = "Elder Guardians will spawn Elder Minions.")
public class MinionFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Integer> baseCooldownConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> cooldownReductionPerMissingGuardianConfig;

	public int baseCooldown = 200;
	public int cooldownReductionPerMissingGuardian = 60;

	public MinionFeature(Module module) {
		super(Config.builder, module);
		this.pushConfig(Config.builder);
		baseCooldownConfig = Config.builder
				.comment("Elder Guardians will spawn Elder Minions every this tick value (20 ticks = 1 sec).")
				.defineInRange("Base Cooldown", this.baseCooldown, 0, Integer.MAX_VALUE);
		cooldownReductionPerMissingGuardianConfig = Config.builder
				.comment("The base cooldown is reduced by this value for each missing Elder Guardian.")
				.defineInRange("Cooldown Reduction per Missing Elder", this.cooldownReductionPerMissingGuardian, 0, Integer.MAX_VALUE);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.baseCooldown = this.baseCooldownConfig.get();
		this.cooldownReductionPerMissingGuardian = this.cooldownReductionPerMissingGuardianConfig.get();
	}

	@SubscribeEvent
	public void onElderGuardianSpawn(EntityJoinWorldEvent event) {
		if (event.getWorld().isClientSide)
			return;

		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof ElderGuardianEntity))
			return;

		ElderGuardianEntity elderGuardian = (ElderGuardianEntity) event.getEntity();

		CompoundNBT witherTags = elderGuardian.getPersistentData();

		witherTags.putInt(Strings.Tags.ELDER_MINION_COOLDOWN, this.baseCooldown);
	}

	@SubscribeEvent
	public void update(LivingEvent.LivingUpdateEvent event) {
		if (event.getEntity().level.isClientSide)
			return;

		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof ElderGuardianEntity))
			return;

		World world = event.getEntity().level;

		ElderGuardianEntity elderGuardian = (ElderGuardianEntity) event.getEntity();
		CompoundNBT elderGuardianTags = elderGuardian.getPersistentData();

		if (elderGuardian.getHealth() <= 0)
			return;
		int cooldown = elderGuardianTags.getInt(Strings.Tags.ELDER_MINION_COOLDOWN);
		if (cooldown > 0) {
			elderGuardianTags.putInt(Strings.Tags.ELDER_MINION_COOLDOWN, cooldown - 1);
			return;
		}
		elderGuardianTags.putInt(Strings.Tags.ELDER_MINION_COOLDOWN, this.baseCooldown - (this.cooldownReductionPerMissingGuardian * BaseFeature.getDeadElderGuardians(elderGuardian)));

		//If there is no player in a radius from the elderGuardian, don't spawn minions
		int radius = 24;
		BlockPos pos1 = elderGuardian.blockPosition().offset(-radius, -radius, -radius);
		BlockPos pos2 = elderGuardian.blockPosition().offset(radius, radius, radius);
		AxisAlignedBB bb = new AxisAlignedBB(pos1, pos2);
		List<ServerPlayerEntity> players = world.getLoadedEntitiesOfClass(ServerPlayerEntity.class, bb);

		if (players.isEmpty())
			return;

		List<GuardianEntity> minionsInAABB = world.getLoadedEntitiesOfClass(GuardianEntity.class, elderGuardian.getBoundingBox().inflate(12), entity -> entity.getPersistentData().contains(Strings.Tags.ELDER_MINION));
		int minionsCountInAABB = minionsInAABB.size();

		if (minionsCountInAABB >= 5)
			return;

		summonMinion(world, new Vector3d(elderGuardian.getX(), elderGuardian.getY(), elderGuardian.getZ()));
	}

	public GuardianEntity summonMinion(World world, Vector3d pos) {
		GuardianEntity elderMinion = new GuardianEntity(EntityType.GUARDIAN, world);
		CompoundNBT minionTags = elderMinion.getPersistentData();

		minionTags.putBoolean("mobspropertiesrandomness:processed", true);
		//TODO Scaling health

		minionTags.putBoolean(Strings.Tags.ELDER_MINION, true);

		elderMinion.setPos(pos.x, pos.y, pos.z);
		elderMinion.setCustomName(new TranslationTextComponent(Strings.Translatable.ELDER_MINION));
		elderMinion.lootTable = LootTables.EMPTY;

		MCUtils.applyModifier(elderMinion, ForgeMod.SWIM_SPEED.get(), Strings.AttributeModifiers.SWIM_SPEED_BONUS_UUID, Strings.AttributeModifiers.SWIM_SPEED_BONUS, 2d, AttributeModifier.Operation.MULTIPLY_BASE);

		ArrayList<Goal> goalsToRemove = new ArrayList<>();
		for (PrioritizedGoal prioritizedGoal : elderMinion.targetSelector.availableGoals) {
			if (!(prioritizedGoal.getGoal() instanceof NearestAttackableTargetGoal))
				continue;

			goalsToRemove.add(prioritizedGoal.getGoal());
		}

		goalsToRemove.forEach(elderMinion.goalSelector::removeGoal);
		elderMinion.targetSelector.addGoal(1, new ElderMinionNearestAttackableTargetGoal<>(elderMinion, PlayerEntity.class, true));

		world.addFreshEntity(elderMinion);
		return elderMinion;
	}
}
