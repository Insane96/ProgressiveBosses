package insane96mcp.progressivebosses.modules.wither;

import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.progressivebosses.modules.wither.feature.DifficultyFeature;
import insane96mcp.progressivebosses.setup.Config;

@Label(name = "Wither")
public class WitherModule extends Module {

	public DifficultyFeature difficultyFeature;

	public WitherModule() {
		super(Config.builder);
		pushConfig(Config.builder);
		//Must be the first one to be initialized, otherwise the other modules will not get the correct difficulty settings
		difficultyFeature = new DifficultyFeature(this);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		difficultyFeature.loadConfig();
	}
}