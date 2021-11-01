package insane96mcp.progressivebosses.module.elderguardian;

import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.progressivebosses.module.elderguardian.feature.HealthFeature;
import insane96mcp.progressivebosses.setup.Config;

@Label(name = "Elder Guardian")
public class ElderGuardianModule extends Module {

	public HealthFeature health;

	public ElderGuardianModule() {
		super(Config.builder);
		pushConfig(Config.builder);
		health = new HealthFeature(this);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		health.loadConfig();
	}
}
