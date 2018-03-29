package net.insane96mcp.progressivebosses.lib;

import java.lang.reflect.Field;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class Reflection {
	public static Field livingDeathLootTable;
	public static Field livingExperienceValue;
		
	public static void Init() {
		try {
			livingDeathLootTable = ReflectionHelper.findField(EntityLiving.class, "deathLootTable", "field_184659_bA", "bC");
			livingExperienceValue = ReflectionHelper.findField(EntityLiving.class, "experienceValue", "field_70728_aV", "b_");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
