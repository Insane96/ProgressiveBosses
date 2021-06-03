package insane96mcp.progressivebosses.modules.wither.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.progressivebosses.base.Strings;
import insane96mcp.progressivebosses.modules.wither.dispenser.WitherSkullDispenseBehavior;
import insane96mcp.progressivebosses.setup.Config;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

@Label(name = "Misc", description = "Handles various small features, such as the explosion")
public class MiscFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Double> explosionPowerBonusConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> explosionCausesFireAtDifficultyConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> preventGettingStuckConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> witherNetherOnlyConfig;

	public double explosionPowerBonus = 0.3d;
	public int explosionCausesFireAtDifficulty = 8;
	public boolean preventGettingStuck = true;
	public boolean witherNetherOnly = false;

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
		witherNetherOnlyConfig = Config.builder
				.comment("The wither can only be spawned in the Nether.\n" +
						"Note that this feature completely disables Wither Skulls from begin placed nearby Soul Sand when not in the Nether or when on the Nether Roof.\n" +
						"Requires Minecraft restart.")
				.define("Wither Nether Only", witherNetherOnly);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.explosionPowerBonus = this.explosionPowerBonusConfig.get();
		this.explosionCausesFireAtDifficulty = this.explosionCausesFireAtDifficultyConfig.get();
		this.preventGettingStuck = this.preventGettingStuckConfig.get();
		this.witherNetherOnly = this.witherNetherOnlyConfig.get();

		if (this.witherNetherOnly)
			DispenserBlock.registerDispenseBehavior(Items.WITHER_SKELETON_SKULL, new WitherSkullDispenseBehavior());
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

		if (wither.getHealth() <= 0)
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

			//Moves the Wither up to 2 blocks down only if possible
			if (!tags.contains(Strings.Tags.CHARGE_ATTACK)) {
				for (int i = 0; i < 2; i++) {
					if (!world.getBlockState(wither.getPosition().down()).isSolid())
						wither.setPosition(wither.getPosX(), wither.getPosY() - 1, wither.getPosZ());
				}
			}
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

	@SubscribeEvent
	public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		if (!this.isEnabled())
			return;

		if (!this.witherNetherOnly)
			return;


		if (event.getItemStack().getItem() == Items.WITHER_SKELETON_SKULL && !canPlaceSkull(event.getWorld(), event.getPos().add(event.getFace().getDirectionVec()))) {
			event.setCanceled(true);
		}
	}

	/**
	 * Returns true if at the specified position a Wither Skull can be placed
	 */
	public static boolean canPlaceSkull(World world, BlockPos pos) {
		boolean isNether = world.getDimensionKey().getLocation().equals(DimensionType.THE_NETHER_ID);

		boolean hasSoulSandNearby = false;
		for (Direction dir : Direction.values()) {
			if (world.getBlockState(pos.add(dir.getDirectionVec())).getBlock().equals(Blocks.SOUL_SAND)){
				hasSoulSandNearby = true;
				break;
			}
		}

		//If it's not the nether or if it is but it's on the Nether roof and there's soulsand nearby
		if ((!isNether || pos.getY() > 127) && hasSoulSandNearby)
			return false;

		return true;
	}
}
