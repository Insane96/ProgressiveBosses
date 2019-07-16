package insane96mcp.progressivebosses.setup;

import insane96mcp.progressivebosses.utils.ModReflection;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.monster.ShulkerEntity;
import net.minecraft.world.ServerBossInfo;
import net.minecraft.world.end.DragonFightManager;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Reflection {
    public static Field MobEntity_deathLootTable;
    public static Field MobEntity_experienceValue;
    public static Field WitherEntity_bossInfo;
    public static Field DragonFightManager_previouslyKilled;
    public static Method BossInfoServer_setPercent;
    public static Class<?> ShulkerEntity_AttackGoal;

    public static void init() {
        try {
            MobEntity_deathLootTable = ObfuscationReflectionHelper.findField(MobEntity.class, "field_184659_bA");
            MobEntity_experienceValue = ObfuscationReflectionHelper.findField(MobEntity.class, "field_70728_aV");
            WitherEntity_bossInfo = ObfuscationReflectionHelper.findField(WitherEntity.class, "field_184744_bE");
            BossInfoServer_setPercent = ObfuscationReflectionHelper.findMethod(ServerBossInfo.class, "func_186735_a", float.class);

            DragonFightManager_previouslyKilled = ObfuscationReflectionHelper.findField(DragonFightManager.class, "field_186118_l");

            ShulkerEntity_AttackGoal = ModReflection.getClass(ShulkerEntity.class.getClassLoader(), "net.minecraft.entity.monster.ShulkerEntity$AttackGoal");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void set(Field field, Object object, Object value) {
        try {
            field.set(object, value);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static Object get(Field field, Object object) {
        try {
            return field.get(object);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object invoke(Method method, Object object, Object... params) {
        try {
            return method.invoke(object, params);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
