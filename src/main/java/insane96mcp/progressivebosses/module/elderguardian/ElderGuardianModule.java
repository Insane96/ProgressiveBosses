package insane96mcp.progressivebosses.module.elderguardian;

import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.progressivebosses.module.elderguardian.feature.AdventureFeature;
import insane96mcp.progressivebosses.module.elderguardian.feature.AttackFeature;
import insane96mcp.progressivebosses.module.elderguardian.feature.HealthFeature;
import insane96mcp.progressivebosses.module.elderguardian.feature.ResistancesFeature;
import insane96mcp.progressivebosses.setup.Config;

@Label(name = "Elder Guardian")
public class ElderGuardianModule extends Module {

	public HealthFeature health;
	public AdventureFeature adventure;
	public ResistancesFeature resistances;
	public AttackFeature attack;

	public ElderGuardianModule() {
		super(Config.builder);
		pushConfig(Config.builder);
		health = new HealthFeature(this);
		adventure = new AdventureFeature(this);
		resistances = new ResistancesFeature(this);
		attack = new AttackFeature(this);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		health.loadConfig();
		adventure.loadConfig();
		resistances.loadConfig();
		attack.loadConfig();
	}
}
