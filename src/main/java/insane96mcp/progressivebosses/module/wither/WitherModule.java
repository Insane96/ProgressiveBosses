package insane96mcp.progressivebosses.module.wither;

import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.progressivebosses.module.wither.feature.*;
import insane96mcp.progressivebosses.setup.Config;

@Label(name = "Wither")
public class WitherModule extends Module {

	public DifficultyFeature difficulty;
	public MiscFeature misc;
	public HealthFeature health;
	public ResistancesFeature resistances;
	public RewardFeature reward;
	public MinionFeature minion;
	public AttackFeature attack;

	public WitherModule() {
		super(Config.builder);
		pushConfig(Config.builder);
		//Must be the first one to be initialized, otherwise the other modules will not get the correct difficulty settings
		difficulty = new DifficultyFeature(this);
		misc = new MiscFeature(this);
		health = new HealthFeature(this);
		resistances = new ResistancesFeature(this);
		reward = new RewardFeature(this);
		minion = new MinionFeature(this);
		attack = new AttackFeature(this);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		difficulty.loadConfig();
		misc.loadConfig();
		health.loadConfig();
		resistances.loadConfig();
		reward.loadConfig();
		minion.loadConfig();
		attack.loadConfig();
	}
}