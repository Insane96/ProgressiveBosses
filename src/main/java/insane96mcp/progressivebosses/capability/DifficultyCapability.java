package insane96mcp.progressivebosses.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DifficultyCapability implements ICapabilitySerializable<CompoundNBT> {

	@CapabilityInject(IDifficulty.class)
	public static final Capability<IDifficulty> DIFFICULTY = null;
	private final LazyOptional<IDifficulty> instance = LazyOptional.of(DIFFICULTY::getDefaultInstance);

	public static void register() {
		CapabilityManager.INSTANCE.register(IDifficulty.class, new DifficultyStorage(), Difficulty::new);
	}

	public void invalidate() {
		instance.invalidate();
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		return DIFFICULTY.orEmpty(cap, instance);
	}

	@Override
	public CompoundNBT serializeNBT() {
		return (CompoundNBT) DIFFICULTY.getStorage().writeNBT(DIFFICULTY, instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!")), null);
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		DIFFICULTY.getStorage().readNBT(DIFFICULTY, instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!")), null, nbt);
	}
}
