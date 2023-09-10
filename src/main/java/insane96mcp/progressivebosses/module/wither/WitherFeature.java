package insane96mcp.progressivebosses.module.wither;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.module.wither.data.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

@Label(name = "Wither Feature")
@LoadFeature(module = ProgressiveBosses.RESOURCE_PREFIX + "wither", canBeDisabled = false)
public class WitherFeature extends Feature {
	@Config
	@Label(name = "Enable Charge")
	public static Boolean enableCharge = true;
	@Config
	@Label(name = "Enable Barrage")
	public static Boolean enableBarrage = true;
	/*@Config
	@Label(name = "Enable Resistances and Weaknesses")
	public static Boolean enableResistancesAndWeaknesses = true;*/

	public static final List<WitherStats> DEFAULT_WITHER_STATS = new ArrayList<>(List.of(
			new WitherStats(0,
					new WitherAttack(8f, 2f, 0.01f, 45, 70, 0.05f, 12f, 70, 0.05f, 20, 40),
					new WitherHealth(300f, 1f, 1f, 30),
					new WitherResistancesWeaknesses(new PoweredValue(6f, 13f), new PoweredValue(3f, 4f), 225f),
					new WitherMinionStats(new PoweredValue(1), new PoweredValue(2), new PoweredValue(300, 150), new PoweredValue(450, 225), new PoweredValue(0.1f), 3f, true, new PoweredValue(0.7f, 0.3f), 0.3f, 0.3f, 0.4f, 0.2f),
					new WitherMiscStats(7f, false, false, false),
					250, new ResourceLocation("progressivebosses:entities/wither_0")),
			new WitherStats(1,
					new WitherAttack(10f, 2.2f, 0.02f, 40, 60, 0.05f, 16f, 60, 0.05f, 30, 60),
					new WitherHealth(400f, 1.25f, 1f, 30),
					new WitherResistancesWeaknesses(new PoweredValue(9f, 16f), new PoweredValue(6f, 8f), 250f),
					new WitherMinionStats(new PoweredValue(2), new PoweredValue(3), new PoweredValue(350, 175), new PoweredValue(525, 260), new PoweredValue(0.15f), 3f, true, new PoweredValue(0.7f, 0.3f), 0.6f, 0.6f, 0.8f, 0.4f),
					new WitherMiscStats(8.5f, false, true, false),
					600, new ResourceLocation("progressivebosses:entities/wither_1")),
			new WitherStats(2,
					new WitherAttack(13f, 2.4f, 0.03f, 35, 50, 0.05f, 20f, 50, 0.05f, 40, 80),
					new WitherHealth(500f, 1.5f, 0.8f, 30),
					new WitherResistancesWeaknesses(new PoweredValue(12f, 18f), new PoweredValue(9f, 12f), 275f),
					new WitherMinionStats(new PoweredValue(3), new PoweredValue(4), new PoweredValue(400, 200), new PoweredValue(600, 300), new PoweredValue(0.20f), 3f, true, new PoweredValue(0.7f, 0.3f), 0.9f, 0.9f, 1.2f, 0.6f),
					new WitherMiscStats(10f, false, true, false),
					950, new ResourceLocation("progressivebosses:entities/wither_2")),
			new WitherStats(3,
					new WitherAttack(16f, 2.6f, 0.05f, 30, 40, 0.05f, 24f, 40, 0.05f, 50, 100),
					new WitherHealth(600f, 2f, 0.6f, 30),
					new WitherResistancesWeaknesses(new PoweredValue(15f, 20f), new PoweredValue(12f, 16f), 300f),
					new WitherMinionStats(new PoweredValue(4), new PoweredValue(5), new PoweredValue(400, 200), new PoweredValue(600, 300), new PoweredValue(0.25f), 3f, true, new PoweredValue(0.7f, 0.3f), 1.2f, 1.2f, 1.6f, 0.8f),
					new WitherMiscStats(11.5f, false, true, true),
					1300, new ResourceLocation("progressivebosses:entities/wither_3"))
	));

	public WitherFeature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@SubscribeEvent
	public void onSkullPlaced(BlockEvent.EntityPlaceEvent event) {
		SummonHelper.checkSpawnFromSkullPlacement(event.getState(), event.getPos(), (Level) event.getLevel(), event.getEntity());
    }
}