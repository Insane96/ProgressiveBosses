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
	public static Field EntityLiving_deathLootTable;
	public static Field EntityLiving_experienceValue;
	public static Field EntityWither_bossInfo;
	public static Field DragonFightManager_previouslyKilled;
	public static Method BossInfoServer_setPercent;
		
	public static void Init() {
		try {
			EntityLiving_deathLootTable = ReflectionHelper.findField(EntityLiving.class, "deathLootTable", "field_184659_bA", "bC");
			EntityLiving_experienceValue = ReflectionHelper.findField(EntityLiving.class, "experienceValue", "field_70728_aV", "b_");
			EntityWither_bossInfo = ReflectionHelper.findField(EntityWither.class, "bossInfo", "field_184744_bE");
			BossInfoServer_setPercent = ReflectionHelper.findMethod(BossInfoServer.class, "setPercent", "func_186735_a", float.class);
			
			DragonFightManager_previouslyKilled = ReflectionHelper.findField(DragonFightManager.class, "previouslyKilled", "field_186118_l");
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
