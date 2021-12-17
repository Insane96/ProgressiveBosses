package insane96mcp.progressivebosses.capability;

import insane96mcp.progressivebosses.base.Strings;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DifficultyProvider implements ICapabilityProvider, ICapabilitySerializable<CompoundTag> {

	public static final ResourceLocation IDENTIFIER = new ResourceLocation(Strings.Tags.DIFFICULTY);

	private final IDifficulty backend = new DifficultyImpl();
	private final LazyOptional<IDifficulty> optionalData = LazyOptional.of(() -> backend);

	@NotNull
	@Override
	public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		return Difficulty.INSTANCE.orEmpty(cap, this.optionalData);
	}

	public void invalidate() {
		this.optionalData.invalidate();
	}

	@Override
	public CompoundTag serializeNBT() {
		CompoundTag nbt = new CompoundTag();
		nbt.putInt(Strings.Tags.SPAWNED_WITHERS, backend.getSpawnedWithers());
		nbt.putInt(Strings.Tags.KILLED_DRAGONS, backend.getKilledDragons());
		nbt.putByte(Strings.Tags.FIRST_DRAGON, backend.getFirstDragon());
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		backend.setSpawnedWithers(nbt.getInt(Strings.Tags.SPAWNED_WITHERS));
		backend.setKilledDragons(nbt.getInt(Strings.Tags.KILLED_DRAGONS));
		backend.setFirstDragon(nbt.getByte(Strings.Tags.FIRST_DRAGON));
	}
}
