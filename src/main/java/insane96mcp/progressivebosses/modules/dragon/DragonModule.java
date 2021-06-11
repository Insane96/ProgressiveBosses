package insane96mcp.progressivebosses.modules.dragon;

import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.progressivebosses.modules.dragon.feature.AttackFeature;
import insane96mcp.progressivebosses.modules.dragon.feature.DifficultyFeature;
import insane96mcp.progressivebosses.modules.dragon.feature.HealthFeature;
import insane96mcp.progressivebosses.setup.Config;

@Label(name = "Dragon")
public class DragonModule extends Module {

	public DifficultyFeature difficultyFeature;
	public HealthFeature healthFeature;
	public AttackFeature attackFeature;

	public DragonModule() {
		super(Config.builder);
		pushConfig(Config.builder);
		difficultyFeature = new DifficultyFeature(this);
		healthFeature = new HealthFeature(this);
		attackFeature = new AttackFeature(this);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		difficultyFeature.loadConfig();
		healthFeature.loadConfig();
		attackFeature.loadConfig();
	}
}
