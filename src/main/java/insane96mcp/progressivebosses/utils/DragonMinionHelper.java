package insane96mcp.progressivebosses.utils;

import net.minecraft.entity.monster.ShulkerEntity;
import net.minecraft.nbt.CompoundNBT;

public class DragonMinionHelper {
	public static boolean isBlindingMinion(ShulkerEntity shulker) {
		CompoundNBT compound = shulker.serializeNBT();
		return compound.getByte("Color") == 15;
	}

	public static void setMinionColor(ShulkerEntity shulker, boolean blinding) {
		CompoundNBT compound = shulker.serializeNBT();
		if (blinding)
			compound.putByte("Color", (byte) 15);
		else
			compound.putByte("Color", (byte) 10);
		shulker.deserializeNBT(compound);
	}
}
