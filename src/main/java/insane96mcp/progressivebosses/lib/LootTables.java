package net.insane96mcp.progressivebosses.lib;

import net.insane96mcp.progressivebosses.ProgressiveBosses;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTableList;

public class LootTables {
	public static ResourceLocation dragonMinion;
	
	public static void Init() {
		dragonMinion = LootTableList.register(new ResourceLocation(ProgressiveBosses.MOD_ID, "dragon_minion"));
	}
}
