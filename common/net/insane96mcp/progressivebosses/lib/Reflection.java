package net.insane96mcp.progressivebosses.lib;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.end.DragonFightManager;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class Reflection {
	public static Field livingDeathLootTable;
	public static Field livingExperienceValue;
	public static Field witherBossInfo;
	public static Field dragonFightManager_previouslyKilled;
	public static Method bossInfoServerSetPercent;
		
	public static void Init() {
		try {
			livingDeathLootTable = ReflectionHelper.findField(EntityLiving.class, "deathLootTable", "field_184659_bA", "bC");
			livingExperienceValue = ReflectionHelper.findField(EntityLiving.class, "experienceValue", "field_70728_aV", "b_");
			witherBossInfo = ReflectionHelper.findField(EntityWither.class, "bossInfo", "field_184744_bE");
			bossInfoServerSetPercent = ReflectionHelper.findMethod(BossInfoServer.class, "setPercent", "func_186735_a", float.class);
			
			dragonFightManager_previouslyKilled = ReflectionHelper.findField(DragonFightManager.class, "previouslyKilled", "field_186118_l");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void Set(Field field, Object object, Object value) {
		try {
			field.set(object, value);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	public static Object Get(Field field, Object object) {
		try {
			return field.get(object);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Object Invoke(Method method, Object object, Object... params) {
		try {
			return method.invoke(object, params);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}
}
