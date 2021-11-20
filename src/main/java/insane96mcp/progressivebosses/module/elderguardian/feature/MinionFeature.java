package insane96mcp.progressivebosses.module.elderguardian.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.utils.RandomHelper;
import insane96mcp.progressivebosses.base.Strings;
import insane96mcp.progressivebosses.setup.Config;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.monster.ElderGuardianEntity;
import net.minecraft.entity.monster.GuardianEntity;
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

@Label(name = "Minions", description = "Elder Guardians got some help")
public class MinionFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Integer> minionPerMissingGuardianConfig;

	public int minionPerMissingGuardian = 1;

	public MinionFeature(Module module) {
		super(Config.builder, module);
		this.pushConfig(Config.builder);
		minionPerMissingGuardianConfig = Config.builder
				.comment("Elder Guardians will spawn this_value * missing_elder_guardians amount of minions.")
				.defineInRange("Minion per Missing Elder Guardian", minionPerMissingGuardian, 0, Integer.MAX_VALUE);

		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.minionPerMissingGuardian = this.minionPerMissingGuardianConfig.get();
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

		//TODO move to config, 10 seconds
		witherTags.putInt(Strings.Tags.ELDER_GUARDIAN_MINION_COOLDOWN, 200);
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

		int cooldown = elderGuardianTags.getInt(Strings.Tags.ELDER_GUARDIAN_MINION_COOLDOWN);
		if (cooldown > 0) {
			elderGuardianTags.putInt(Strings.Tags.ELDER_GUARDIAN_MINION_COOLDOWN, cooldown - 1);
			return;
		}

		//If there is no player in a radius from the elderGuardian, don't spawn minions
		int radius = 24;
		BlockPos pos1 = elderGuardian.blockPosition().offset(-radius, -radius, -radius);
		BlockPos pos2 = elderGuardian.blockPosition().offset(radius, radius, radius);
		AxisAlignedBB bb = new AxisAlignedBB(pos1, pos2);
		List<ServerPlayerEntity> players = world.getLoadedEntitiesOfClass(ServerPlayerEntity.class, bb);

		if (players.isEmpty())
			return;

		List<GuardianEntity> minionsInAABB = world.getLoadedEntitiesOfClass(GuardianEntity.class, elderGuardian.getBoundingBox().inflate(16), entity -> entity.getPersistentData().contains(Strings.Tags.ELDER_GUARDIAN_MINION));
		int minionsCountInAABB = minionsInAABB.size();

		if (minionsCountInAABB >= 5)
			return;

		elderGuardianTags.putInt(Strings.Tags.WITHER_MINION_COOLDOWN, 200);

		int x = 0, y = 0, z = 0;
		//Tries to spawn the Minion up to 5 times
		for (int t = 0; t < 5; t++) {
			x = (int) (elderGuardian.getX() + (RandomHelper.getInt(world.random, -3, 3)));
			y = (int) (elderGuardian.getY() + 3);
			z = (int) (elderGuardian.getZ() + (RandomHelper.getInt(world.random, -3, 3)));

			y = getYSpawn(EntityType.GUARDIAN, new BlockPos(x, y, z), world, 8);
			if (y != -1)
				break;
		}
		if (y <= 0)
			return;

		GuardianEntity guardianEntity = summonMinion(world, new Vector3d(x + 0.5, y + 0.5, z + 0.5));
	}

	/**
	 * Returns -1 when no spawn spots are found, otherwise the Y coord
	 * @param pos
	 * @param world
	 * @param minRelativeY
	 * @return
	 */
	private static int getYSpawn(EntityType entityType, BlockPos pos, World world, int minRelativeY) {
		int height = (int) Math.ceil(entityType.getHeight());
		int fittingYPos = -1;
		for (int y = pos.getY(); y > pos.getY() - minRelativeY; y--) {
			boolean viable = true;
			BlockPos p = new BlockPos(pos.getX(), y, pos.getZ());
			for (int i = 0; i < height; i++) {
				if (world.getBlockState(p.above(i)).getMaterial().blocksMotion()) {
					viable = false;
					break;
				}
			}
			if (!viable)
				continue;
			fittingYPos = y;
			if (!world.getBlockState(p.below()).getMaterial().blocksMotion())
				continue;
			return y;
		}
		return fittingYPos;
	}

	public GuardianEntity summonMinion(World world, Vector3d pos) {
		GuardianEntity elderMinion = new GuardianEntity(EntityType.GUARDIAN, world);
		CompoundNBT minionTags = elderMinion.getPersistentData();

		minionTags.putBoolean("mobspropertiesrandomness:processed", true);
		//TODO Scaling health

		elderMinion.setPos(pos.x, pos.y, pos.z);
		elderMinion.setCustomName(new TranslationTextComponent(Strings.Translatable.ELDER_MINION));
		elderMinion.lootTable = LootTables.EMPTY;
		elderMinion.setPersistenceRequired();

		ModifiableAttributeInstance swimSpeed = elderMinion.getAttribute(ForgeMod.SWIM_SPEED.get());
		if (swimSpeed != null) {
			AttributeModifier swimSpeedBonus = new AttributeModifier(Strings.AttributeModifiers.SWIM_SPEED_BONUS_UUID, Strings.AttributeModifiers.SWIM_SPEED_BONUS, 2d, AttributeModifier.Operation.MULTIPLY_BASE);
			swimSpeed.addPermanentModifier(swimSpeedBonus);
		}

		ArrayList<Goal> goalsToRemove = new ArrayList<>();
		for (PrioritizedGoal prioritizedGoal : elderMinion.targetSelector.availableGoals) {
			if (!(prioritizedGoal.getGoal() instanceof NearestAttackableTargetGoal))
				continue;

			NearestAttackableTargetGoal<?> goal = (NearestAttackableTargetGoal<?>) prioritizedGoal.getGoal();

			goalsToRemove.add(prioritizedGoal.getGoal());
		}

		goalsToRemove.forEach(elderMinion.goalSelector::removeGoal);

		world.addFreshEntity(elderMinion);
		return elderMinion;
	}
}
