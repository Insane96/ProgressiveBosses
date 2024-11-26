package insane96mcp.progressivebosses.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.monster.Shulker;

public class DragonMinionHelper {
	public static void setMinionColor(Shulker shulker, boolean blinding) {
		CompoundTag compound = shulker.serializeNBT();
		compound.putByte("Color", (byte) 10);
		shulker.deserializeNBT(compound);
	}
}
