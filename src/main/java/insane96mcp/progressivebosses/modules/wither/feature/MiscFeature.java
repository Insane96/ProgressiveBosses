package insane96mcp.progressivebosses.modules.wither.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.progressivebosses.base.Strings;
import insane96mcp.progressivebosses.setup.Config;
import net.minecraft.block.BlockState;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

@Label(name = "Misc", description = "Handles various small features, such as the explosion")
public class MiscFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Double> explosionPowerBonusConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> explosionCausesFireAtDifficultyConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> preventGettingStuckConfig;

	public double explosionPowerBonus = 0.3d;
	public int explosionCausesFireAtDifficulty = 8;
	public boolean preventGettingStuck = true;

	public MiscFeature(Module module) {
		super(Config.builder, module);
		Config.builder.comment(this.getDescription()).push(this.getName());
		explosionPowerBonusConfig = Config.builder
				.comment("How much explosion power (after the invulnerability) will the Wither gain for each difficulty point. Explosion Radius is capped to 13. Base Wither Explosion Power is 7.0. Setting this to 0 will not increase the Wither Explosion Power.")
				.defineInRange("Explosion Power Bonus", explosionPowerBonus, 0d, 4d);
		explosionCausesFireAtDifficultyConfig = Config.builder
				.comment("At this difficulty the Wither Explosion will cause fire. Set to -1 to disable.")
				.defineInRange("Explosion Causes Fire at Difficulty", explosionCausesFireAtDifficulty, -1, Integer.MAX_VALUE);
		preventGettingStuckConfig = Config.builder
				.comment("The Wither will try to not get stuck in bedrock for easy killing.")
				.define("Prevent Getting Stuck", preventGettingStuck);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		explosionPowerBonus = explosionPowerBonusConfig.get();
		explosionCausesFireAtDifficulty = explosionCausesFireAtDifficultyConfig.get();
		preventGettingStuck = preventGettingStuckConfig.get();
	}

	@SubscribeEvent
	public void onUpdate(LivingEvent.LivingUpdateEvent event) {
		if (event.getEntity().world.isRemote)
			return;

		if (!this.isEnabled())
			return;

		if (!this.preventGettingStuck)
			return;

		if (!(event.getEntity() instanceof WitherEntity))
			return;

		World world = event.getEntity().world;

		WitherEntity wither = (WitherEntity) event.getEntity();
		CompoundNBT tags = wither.getPersistentData();

		if (wither.getHealth() < 0)
			return;

		if (wither.getInvulTime() == 1) {
			if (!net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(wither.world, wither))
				return;

			Stream<BlockPos> blocks = BlockPos.getAllInBox(wither.getPosition().add(-1, -1, -1), wither.getPosition().add(1, 4, 1));
			AtomicBoolean flag = new AtomicBoolean(false);
			blocks.forEach(pos -> {
				BlockState state = wither.world.getBlockState(pos);
				if (state.canEntityDestroy(wither.world, pos, wither) && net.minecraftforge.event.ForgeEventFactory.onEntityDestroyBlock(wither, pos, state)) {
					flag.set(wither.world.destroyBlock(pos, true, wither) || flag.get());
				}
			});

			if (flag.get())
				wither.world.playEvent((PlayerEntity)null, 1022, wither.getPosition(), 0);

			//TODO: Ugly, but moves the wither up to 2 blocks down
			if (!world.getBlockState(wither.getPosition().down()).isSolid())
				wither.setPosition(wither.getPosX(), wither.getPosY() - 1, wither.getPosZ());
			if (!world.getBlockState(wither.getPosition().down()).isSolid())
				wither.setPosition(wither.getPosX(), wither.getPosY() - 1, wither.getPosZ());
		}
	}

	@SubscribeEvent
	public void onExplosion(ExplosionEvent.Start event) {
		if (!this.isEnabled())
			return;

		if (this.explosionCausesFireAtDifficulty == -1 && this.explosionPowerBonus == 0d)
			return;

		if (!(event.getExplosion().getExploder() instanceof WitherEntity))
			return;

		//Check if the explosion is the one from the wither
		if (event.getExplosion().size != 7f)
			return;

		WitherEntity wither = (WitherEntity) event.getExplosion().getExploder();
		CompoundNBT tags = wither.getPersistentData();

		float difficulty = tags.getFloat(Strings.Tags.DIFFICULTY);

		if (difficulty <= 0f)
			return;

		float explosionPower = (float) (event.getExplosion().size + (this.explosionPowerBonus * difficulty));

		if (explosionPower > 13f)
			explosionPower = 13f;

		event.getExplosion().size = explosionPower;

		event.getExplosion().causesFire = difficulty >= this.explosionCausesFireAtDifficulty;
	}
}
