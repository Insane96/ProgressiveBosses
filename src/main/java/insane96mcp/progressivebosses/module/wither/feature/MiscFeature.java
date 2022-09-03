package insane96mcp.progressivebosses.module.wither.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.progressivebosses.module.wither.dispenser.WitherSkullDispenseBehavior;
import insane96mcp.progressivebosses.setup.Config;
import insane96mcp.progressivebosses.setup.Strings;
import insane96mcp.progressivebosses.utils.DifficultyHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Misc", description = "Handles various small features, such as the explosion")
public class MiscFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Double> explosionPowerBonusConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> explosionCausesFireAtDifficultyConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> fasterBlockBreakingConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> biggerBlockBreakingConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> ignoreWitherProofBlocksConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> witherNetherOnlyConfig;

	public double explosionPowerBonus = 8d;
	public int explosionCausesFireAtDifficulty = 5;
	public boolean fasterBlockBreaking = true;
	public boolean biggerBlockBreaking = true;
	public boolean ignoreWitherProofBlocks = false;
	public boolean witherNetherOnly = false;

	public MiscFeature(Module module) {
		super(Config.builder, module);
		this.pushConfig(Config.builder);
		explosionPowerBonusConfig = Config.builder
				.comment("How much explosion power (after the invulnerability) will the Wither have at max difficulty. Explosion Radius is capped to 13. Base Wither Explosion Power is 7.0. Setting this to 0 will not increase the Wither Explosion Power.")
				.defineInRange("Explosion Power Bonus", explosionPowerBonus, 0d, 4d);
		explosionCausesFireAtDifficultyConfig = Config.builder
				.comment("At this difficulty the Wither Explosion will cause fire. Set to -1 to disable.")
				.defineInRange("Explosion Causes Fire at Difficulty", explosionCausesFireAtDifficulty, -1, Integer.MAX_VALUE);
		fasterBlockBreakingConfig = Config.builder
				.comment("The Wither will no longer wait 1.0 seconds before breaking blocks when he's hit, instead just 0.5s")
				.define("Faster Breaking Blocks", fasterBlockBreaking);
		biggerBlockBreakingConfig = Config.builder
				.comment("The Wither will break even blocks below him when hit.")
				.define("Bigger Breaking Blocks", biggerBlockBreaking);
		ignoreWitherProofBlocksConfig = Config.builder
				.comment("If true the Wither will break even blocks that are witherproof. Unbreakable blocks will still be unbreakable, so it's really useful with other mods as in vanilla Wither Proof Blocks are all the unbreakable blocks.")
				.define("Ignore Witherproof Blocks", ignoreWitherProofBlocks);
		witherNetherOnlyConfig = Config.builder
				.comment("""
						The wither can only be spawned in the Nether.
						Note that this feature completely disables Wither Skulls from begin placed nearby Soul Sand when not in the Nether or when on the Nether Roof.
						Requires Minecraft restart.""")
				.define("Wither Nether Only", witherNetherOnly);
		Config.builder.pop();
	}

	private boolean behaviourRegistered = false;

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.explosionPowerBonus = this.explosionPowerBonusConfig.get();
		this.explosionCausesFireAtDifficulty = this.explosionCausesFireAtDifficultyConfig.get();
		this.fasterBlockBreaking = this.fasterBlockBreakingConfig.get();
		this.biggerBlockBreaking = this.biggerBlockBreakingConfig.get();
		this.ignoreWitherProofBlocks = this.ignoreWitherProofBlocksConfig.get();
		this.witherNetherOnly = this.witherNetherOnlyConfig.get();

		if (this.witherNetherOnly && !behaviourRegistered) {
			DispenserBlock.registerBehavior(Items.WITHER_SKELETON_SKULL, new WitherSkullDispenseBehavior());
			behaviourRegistered = true;
		}
	}

	@SubscribeEvent
	public void onUpdate(LivingEvent.LivingTickEvent event) {
		if (event.getEntity().level.isClientSide)
			return;

		if (!this.isEnabled())
			return;

		if (!this.biggerBlockBreaking)
			return;

		if (!(event.getEntity() instanceof WitherBoss wither))
			return;
		if (!wither.isAlive())
			return;

		//Overrides the block breaking in wither's updateAI since LivingTickEvent is called before
		if (wither.destroyBlocksTick == 1) {
			--wither.destroyBlocksTick;
			if (net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(wither.level, wither)) {
				int i1 = Mth.floor(wither.getY());
				int l1 = Mth.floor(wither.getX());
				int i2 = Mth.floor(wither.getZ());
				boolean flag = false;

				int yOffsetLow = -1;
				if (wither.isPowered())
					yOffsetLow = 0;

				for(int k2 = -1; k2 <= 1; ++k2) {
					for(int l2 = -1; l2 <= 1; ++l2) {
						for(int j = yOffsetLow; j <= 4; ++j) {
							int i3 = l1 + k2;
							int k = i1 + j;
							int l = i2 + l2;
							BlockPos blockpos = new BlockPos(i3, k, l);
							BlockState blockstate = wither.level.getBlockState(blockpos);
							if (canWitherDestroy(wither, blockpos, blockstate) && net.minecraftforge.event.ForgeEventFactory.onEntityDestroyBlock(wither, blockpos, blockstate)) {
								flag = wither.level.destroyBlock(blockpos, true, wither) || flag;
							}
						}
					}
				}

				if (flag) {
					wither.level.levelEvent(null, 1022, wither.blockPosition(), 0);
				}
			}
		}
	}

	private boolean canWitherDestroy(WitherBoss wither, BlockPos pos, BlockState state) {
		if (this.ignoreWitherProofBlocks)
			return !state.isAir() && state.getDestroySpeed(wither.level, pos) >= 0f;
		else
			return state.canEntityDestroy(wither.level, pos, wither);
	}

	@SubscribeEvent
	public void onExplosion(ExplosionEvent.Start event) {
		if (!this.isEnabled())
			return;

		if (this.explosionCausesFireAtDifficulty == -1 && this.explosionPowerBonus == 0d)
			return;

		if (!(event.getExplosion().getExploder() instanceof WitherBoss wither))
			return;

		//Check if the explosion is the one from the wither
		if (event.getExplosion().radius != 7f)
			return;
		CompoundTag tags = wither.getPersistentData();

		float difficulty = tags.getFloat(Strings.Tags.DIFFICULTY);

		if (difficulty <= 0f)
			return;

		float explosionPower = (float) (event.getExplosion().radius + (this.explosionPowerBonus * DifficultyHelper.getScalingDifficulty(wither)));

		if (explosionPower > 13f)
			explosionPower = 13f;

		event.getExplosion().radius = explosionPower;

		event.getExplosion().fire = difficulty >= this.explosionCausesFireAtDifficulty;
	}

	@SubscribeEvent
	public void onWitherDamage(LivingHurtEvent event) {
		if (event.getEntity().level.isClientSide
				|| !this.isEnabled()
				|| !this.fasterBlockBreaking
				|| !event.getEntity().isAlive()
				|| !(event.getEntity() instanceof WitherBoss wither))
			return;

		wither.destroyBlocksTick = 10;
	}

	@SubscribeEvent
	public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		if (!this.isEnabled())
			return;

		if (event.getItemStack().getItem() == Items.WITHER_SKELETON_SKULL && !this.canPlaceSkull(event.getLevel(), event.getPos().offset(event.getFace().getNormal()))) {
			event.setCanceled(true);
		}
	}

	/**
	 * Returns true if at the specified position a Wither Skull can be placed
	 */
	public boolean canPlaceSkull(Level world, BlockPos pos) {
		if (!this.witherNetherOnly)
			return true;

		boolean isNether = world.dimension().equals(Level.NETHER);

		boolean hasSoulSandNearby = false;
		for (Direction dir : Direction.values()) {
			if (world.getBlockState(pos.offset(dir.getNormal())).getBlock().equals(Blocks.SOUL_SAND) || world.getBlockState(pos.offset(dir.getNormal())).getBlock().equals(Blocks.SOUL_SOIL)){
				hasSoulSandNearby = true;
				break;
			}
		}

		//If it's the nether, and it's below the Nether roof or there's no soulsand nearby can place the skull
		return (isNether && pos.getY() <= 127) || !hasSoulSandNearby;
	}
}
