package insane96mcp.progressivebosses.module.elderguardian.feature;

import insane96mcp.insanelib.ai.ILNearestAttackableTargetGoal;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.setup.Strings;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

@Label(name = "Minions", description = "Elder Guardians will spawn Elder Minions.")
@LoadFeature(module = ProgressiveBosses.RESOURCE_PREFIX + "elder_guardian")
public class MinionFeature extends Feature {
	@Config(min = 0)
	@Label(name = "Base Cooldown", description = "Elder Guardians will spawn Elder Minions every this tick value (20 ticks = 1 sec).")
	public static Integer baseCooldown = 200;
	@Config(min = 0)
	@Label(name = "Cooldown Reduction per Missing Elder", description = "The base cooldown is reduced by this value for each missing Elder Guardian.")
	public static Integer cooldownReductionPerMissingElder = 60;

	public MinionFeature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@SubscribeEvent
	public void onElderGuardianSpawn(EntityJoinLevelEvent event) {
		if (event.getLevel().isClientSide
				|| !this.isEnabled()
				|| !(event.getEntity() instanceof ElderGuardian elderGuardian))
			return;

		CompoundTag nbt = elderGuardian.getPersistentData();

		nbt.putInt(Strings.Tags.ELDER_MINION_COOLDOWN, baseCooldown);
	}

	@SubscribeEvent
	public void update(LivingEvent.LivingTickEvent event) {
		if (event.getEntity().level().isClientSide
				|| !this.isEnabled()
				|| !(event.getEntity() instanceof ElderGuardian elderGuardian))
			return;

		Level world = event.getEntity().level();

		CompoundTag elderGuardianTags = elderGuardian.getPersistentData();

		if (elderGuardian.getHealth() <= 0)
			return;
		int cooldown = elderGuardianTags.getInt(Strings.Tags.ELDER_MINION_COOLDOWN);
		if (cooldown > 0) {
			elderGuardianTags.putInt(Strings.Tags.ELDER_MINION_COOLDOWN, cooldown - 1);
			return;
		}
		cooldown = baseCooldown - (cooldownReductionPerMissingElder * elderGuardian.getPersistentData().getInt(Strings.Tags.DIFFICULTY));
		elderGuardianTags.putInt(Strings.Tags.ELDER_MINION_COOLDOWN, cooldown);

		//If there is no player in a radius from the elderGuardian, don't spawn minions
		int radius = 24;
		BlockPos pos1 = elderGuardian.blockPosition().offset(-radius, -radius, -radius);
		BlockPos pos2 = elderGuardian.blockPosition().offset(radius, radius, radius);
		AABB bb = new AABB(pos1, pos2);
		List<ServerPlayer> players = world.getEntitiesOfClass(ServerPlayer.class, bb);

		if (players.isEmpty())
			return;

		List<Guardian> minionsInAABB = world.getEntitiesOfClass(Guardian.class, elderGuardian.getBoundingBox().inflate(12), entity -> entity.getPersistentData().contains(Strings.Tags.ELDER_MINION));
		int minionsCountInAABB = minionsInAABB.size();

		if (minionsCountInAABB >= 5)
			return;

		summonMinion(world, new Vec3(elderGuardian.getX(), elderGuardian.getY(), elderGuardian.getZ()));
	}

	public static void summonMinion(Level world, Vec3 pos) {
		Guardian elderMinion = new Guardian(EntityType.GUARDIAN, world);
		CompoundTag minionTags = elderMinion.getPersistentData();

		minionTags.putBoolean("mobspropertiesrandomness:processed", true);
		//TODO Scaling health

		minionTags.putBoolean(Strings.Tags.ELDER_MINION, true);

		elderMinion.setPos(pos.x, pos.y, pos.z);
		elderMinion.setCustomName(Component.translatable(Strings.Translatable.ELDER_MINION));
		elderMinion.lootTable = BuiltInLootTables.EMPTY;

		MCUtils.applyModifier(elderMinion, ForgeMod.SWIM_SPEED.get(), Strings.AttributeModifiers.SWIM_SPEED_BONUS_UUID, Strings.AttributeModifiers.SWIM_SPEED_BONUS, 2d, AttributeModifier.Operation.MULTIPLY_BASE);

		ArrayList<Goal> goalsToRemove = new ArrayList<>();
		for (WrappedGoal prioritizedGoal : elderMinion.targetSelector.availableGoals) {
			if (!(prioritizedGoal.getGoal() instanceof NearestAttackableTargetGoal))
				continue;

			goalsToRemove.add(prioritizedGoal.getGoal());
		}

		goalsToRemove.forEach(elderMinion.goalSelector::removeGoal);
		elderMinion.targetSelector.addGoal(1, new ILNearestAttackableTargetGoal<>(elderMinion, Player.class, false).setIgnoreLineOfSight());

		world.addFreshEntity(elderMinion);
	}
}
