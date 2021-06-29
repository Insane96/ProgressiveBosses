package insane96mcp.progressivebosses.capability;

public interface IDifficulty {
	int getSpawnedWithers();
	void setSpawnedWithers(int spawnedWithers);
	int getKilledDragons();
	void setKilledDragons(int killedDragons);
	byte getFirstDragon();
	void setFirstDragon(byte firstDragon);

	void addSpawnedWithers(int amount);
	void addKilledDragons(int amount);
}
