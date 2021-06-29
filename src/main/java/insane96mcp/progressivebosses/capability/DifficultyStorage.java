package insane96mcp.progressivebosses.capability;

import insane96mcp.progressivebosses.base.Strings;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class DifficultyStorage implements Capability.IStorage<IDifficulty> {

	@Nullable
	@Override
	public INBT writeNBT(Capability<IDifficulty> capability, IDifficulty instance, Direction side) {
		CompoundNBT nbt = new CompoundNBT();
		nbt.putInt(Strings.Tags.SPAWNED_WITHERS, instance.getSpawnedWithers());
		nbt.putInt(Strings.Tags.KILLED_DRAGONS, instance.getKilledDragons());
		nbt.putBoolean(Strings.Tags.FIRST_DRAGON, instance.isFirstDragon());
		return nbt;
	}

	@Override
	public void readNBT(Capability<IDifficulty> capability, IDifficulty instance, Direction side, INBT nbt) {
		if (!(instance instanceof Difficulty))
			throw new IllegalArgumentException("Can not deserialize to an instance that isn't the default implementation");

		CompoundNBT compoundNBT = (CompoundNBT) nbt;
		instance.setSpawnedWithers(compoundNBT.getInt(Strings.Tags.SPAWNED_WITHERS));
		instance.setKilledDragons(compoundNBT.getInt(Strings.Tags.KILLED_DRAGONS));
		instance.setFirstDragon(compoundNBT.getBoolean(Strings.Tags.FIRST_DRAGON));
	}
}
