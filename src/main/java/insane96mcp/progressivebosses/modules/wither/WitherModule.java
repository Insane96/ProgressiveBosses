package insane96mcp.progressivebosses.modules.wither;

import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.progressivebosses.modules.wither.feature.DifficultyFeature;
import insane96mcp.progressivebosses.modules.wither.feature.HealthFeature;
import insane96mcp.progressivebosses.modules.wither.feature.MiscFeature;
import insane96mcp.progressivebosses.modules.wither.feature.ResistancesFeature;
import insane96mcp.progressivebosses.setup.Config;

@Label(name = "Wither")
public class WitherModule extends Module {

	public DifficultyFeature difficultyFeature;
	public MiscFeature miscFeature;
	public HealthFeature healthFeature;
	public ResistancesFeature resistancesFeature;

	public WitherModule() {
		super(Config.builder);
		pushConfig(Config.builder);
		//Must be the first one to be initialized, otherwise the other modules will not get the correct difficulty settings
		difficultyFeature = new DifficultyFeature(this);
		miscFeature = new MiscFeature(this);
		healthFeature = new HealthFeature(this);
		resistancesFeature = new ResistancesFeature(this);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		difficultyFeature.loadConfig();
		miscFeature.loadConfig();
		healthFeature.loadConfig();
		resistancesFeature.loadConfig();
	}
}