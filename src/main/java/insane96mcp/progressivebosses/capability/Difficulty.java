package insane96mcp.progressivebosses.capability;

import net.minecraft.util.math.MathHelper;

public class Difficulty implements IDifficulty {

	private int spawnedWithers;
	private int killedDragons;
	// 0 = just spawned, 1 = first dragon, 2 = first dragon spawned
	private byte firstDragon;

	@Override
	public int getSpawnedWithers() {
		return this.spawnedWithers;
	}

	@Override
	public void setSpawnedWithers(int spawnedWithers) {
		this.spawnedWithers = MathHelper.clamp(spawnedWithers, 0, Integer.MAX_VALUE);
	}

	@Override
	public int getKilledDragons() {
		return this.killedDragons;
	}

	@Override
	public void setKilledDragons(int killedDragons) {
		this.killedDragons = MathHelper.clamp(killedDragons, 0, Integer.MAX_VALUE);
	}

	@Override
	public byte getFirstDragon() {
		return this.firstDragon;
	}

	@Override
	public void setFirstDragon(byte firstDragon) {
		this.firstDragon = firstDragon;
	}


	@Override
	public void addSpawnedWithers(int amount) {
		this.setSpawnedWithers(this.getSpawnedWithers() + amount);
	}

	@Override
	public void addKilledDragons(int amount) {
		this.setKilledDragons(this.getKilledDragons() + amount);
	}


}
