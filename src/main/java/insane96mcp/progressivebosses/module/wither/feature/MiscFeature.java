package insane96mcp.progressivebosses.module.wither.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.progressivebosses.base.Strings;
import insane96mcp.progressivebosses.module.wither.dispenser.WitherSkullDispenseBehavior;
import insane96mcp.progressivebosses.setup.Config;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Misc", description = "Handles various small features, such as the explosion")
public class MiscFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Double> explosionPowerBonusConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> explosionCausesFireAtDifficultyConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> fasterBlockBreakingConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> biggerBlockBreakingConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> witherNetherOnlyConfig;

	public double explosionPowerBonus = 0.5d;
	public int explosionCausesFireAtDifficulty = 16;
	public boolean fasterBlockBreaking = true;
	public boolean biggerBlockBreaking = true;
	public boolean witherNetherOnly = false;

	public MiscFeature(Module module) {
		super(Config.builder, module);
		this.pushConfig(Config.builder);
		explosionPowerBonusConfig = Config.builder
				.comment("How much explosion power (after the invulnerability) will the Wither gain for each difficulty point. Explosion Radius is capped to 13. Base Wither Explosion Power is 7.0. Setting this to 0 will not increase the Wither Explosion Power.")
				.defineInRange("Explosion Power Bonus", explosionPowerBonus, 0d, 4d);
		explosionCausesFireAtDifficultyConfig = Config.builder
				.comment("At this difficulty the Wither Explosion will cause fire. Set to -1 to disable.")
				.defineInRange("Explosion Causes Fire at Difficulty", explosionCausesFireAtDifficulty, -1, Integer.MAX_VALUE);
		fasterBlockBreakingConfig = Config.builder
				.comment("The Wither will no longer wait 1 full second before breaking blocks when he's hit, instead just 0.5s")
				.define("Faster Breaking Blocks", fasterBlockBreaking);
		biggerBlockBreakingConfig = Config.builder
				.comment("The Wither will break even blocks below him when hit.")
				.define("Bigger Breaking Blocks", biggerBlockBreaking);
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
		this.fasterBlockBreaking = this.fasterBlockBreakingConfig.get();
		this.biggerBlockBreaking = this.biggerBlockBreakingConfig.get();
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

		if (!this.biggerBlockBreaking)
			return;

		if (!(event.getEntity() instanceof WitherEntity))
			return;

		WitherEntity wither = (WitherEntity) event.getEntity();

		if (!wither.isAlive())
			return;

		//Overrides the block breaking in wither's updateAI since LivingUpdateEvent is called before
		if (wither.blockBreakCounter == 1) {
			--wither.blockBreakCounter;
			if (net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(wither.world, wither)) {
				int i1 = MathHelper.floor(wither.getPosY());
				int l1 = MathHelper.floor(wither.getPosX());
				int i2 = MathHelper.floor(wither.getPosZ());
				boolean flag = false;

				for(int k2 = -1; k2 <= 1; ++k2) {
					for(int l2 = -1; l2 <= 1; ++l2) {
						for(int j = -1; j <= 4; ++j) {
							int i3 = l1 + k2;
							int k = i1 + j;
							int l = i2 + l2;
							BlockPos blockpos = new BlockPos(i3, k, l);
							BlockState blockstate = wither.world.getBlockState(blockpos);
							if (blockstate.canEntityDestroy(wither.world, blockpos, wither) && net.minecraftforge.event.ForgeEventFactory.onEntityDestroyBlock(wither, blockpos, blockstate)) {
								flag = wither.world.destroyBlock(blockpos, true, wither) || flag;
							}
						}
					}
				}

				if (flag) {
					wither.world.playEvent(null, 1022, wither.getPosition(), 0);
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
	public void onWitherDamage(LivingHurtEvent event) {
		if (event.getEntity().getEntityWorld().isRemote)
			return;

		if (!this.isEnabled())
			return;

		if (!this.fasterBlockBreaking)
			return;

		if (!event.getEntity().isAlive())
			return;

		if (!(event.getEntityLiving() instanceof WitherEntity))
			return;

		WitherEntity wither = (WitherEntity) event.getEntityLiving();
		wither.blockBreakCounter = 10;
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
			if (world.getBlockState(pos.add(dir.getDirectionVec())).getBlock().equals(Blocks.SOUL_SAND) || world.getBlockState(pos.add(dir.getDirectionVec())).getBlock().equals(Blocks.SOUL_SOIL)){
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
