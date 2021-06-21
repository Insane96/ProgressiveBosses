package insane96mcp.progressivebosses.module.dragon;

import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.progressivebosses.module.dragon.feature.*;
import insane96mcp.progressivebosses.setup.Config;

@Label(name = "Dragon")
public class DragonModule extends Module {

	public DifficultyFeature difficulty;
	public HealthFeature health;
	public AttackFeature attack;
	public RewardFeature reward;
	public MinionFeature minion;
	public LarvaFeature larva;
	public ResistancesFeature resistances;
	public CrystalFeature crystal;

	public DragonModule() {
		super(Config.builder);
		pushConfig(Config.builder);
		difficulty = new DifficultyFeature(this);
		health = new HealthFeature(this);
		attack = new AttackFeature(this);
		reward = new RewardFeature(this);
		minion = new MinionFeature(this);
		larva = new LarvaFeature(this);
		resistances = new ResistancesFeature(this);
		crystal = new CrystalFeature(this);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		difficulty.loadConfig();
		health.loadConfig();
		attack.loadConfig();
		reward.loadConfig();
		minion.loadConfig();
		larva.loadConfig();
		resistances.loadConfig();
		crystal.loadConfig();
	}
}
