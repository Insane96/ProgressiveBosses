package insane96mcp.progressivebosses.modules.dragon;

import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.progressivebosses.setup.Config;

@Label(name = "Dragon")
public class DragonModule extends Module {

	public DragonModule() {
		super(Config.builder);
		pushConfig(Config.builder);
		//feature = new Feature(this);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		//feature.loadConfig();
	}
}
