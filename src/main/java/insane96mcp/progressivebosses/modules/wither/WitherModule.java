package insane96mcp.progressivebosses.modules.wither;

import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.progressivebosses.modules.wither.feature.*;
import insane96mcp.progressivebosses.setup.Config;

@Label(name = "Wither")
public class WitherModule extends Module {

	public DifficultyFeature difficultyFeature;
	public MiscFeature miscFeature;
	public HealthFeature healthFeature;
	public ResistancesFeature resistancesFeature;
	public RewardFeature rewardFeature;
	public MinionFeature minionFeature;
	public AttackFeature attackFeature;

	public WitherModule() {
		super(Config.builder);
		pushConfig(Config.builder);
		//Must be the first one to be initialized, otherwise the other modules will not get the correct difficulty settings
		difficultyFeature = new DifficultyFeature(this);
		miscFeature = new MiscFeature(this);
		healthFeature = new HealthFeature(this);
		resistancesFeature = new ResistancesFeature(this);
		rewardFeature = new RewardFeature(this);
		minionFeature = new MinionFeature(this);
		attackFeature = new AttackFeature(this);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		difficultyFeature.loadConfig();
		miscFeature.loadConfig();
		healthFeature.loadConfig();
		resistancesFeature.loadConfig();
		rewardFeature.loadConfig();
		minionFeature.loadConfig();
		attackFeature.loadConfig();
	}
}