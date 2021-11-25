package insane96mcp.progressivebosses.module.elderguardian;

import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.progressivebosses.module.elderguardian.feature.*;
import insane96mcp.progressivebosses.setup.Config;

@Label(name = "Elder Guardian")
public class ElderGuardianModule extends Module {

	public HealthFeature health;
	public BaseFeature base;
	public ResistancesFeature resistances;
	public AttackFeature attack;
	public MinionFeature minion;
	public RewardFeature reward;

	public ElderGuardianModule() {
		super(Config.builder);
		pushConfig(Config.builder);
		health = new HealthFeature(this);
		base = new BaseFeature(this);
		resistances = new ResistancesFeature(this);
		attack = new AttackFeature(this);
		minion = new MinionFeature(this);
		reward = new RewardFeature(this);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		health.loadConfig();
		base.loadConfig();
		resistances.loadConfig();
		attack.loadConfig();
		minion.loadConfig();
		reward.loadConfig();
	}
}
