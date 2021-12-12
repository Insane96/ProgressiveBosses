package insane96mcp.progressivebosses.setup;

import insane96mcp.progressivebosses.ProgressiveBosses;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

public class Reflection {
	static MethodHandles.Lookup lookup = MethodHandles.lookup();

	static Method onEntityHitMethod;
	public static MethodHandle onEntityHitMH;
	public static void ProjectileEntity_onEntityHit(ProjectileEntity projectileEntity, EntityRayTraceResult p_213868_1_) {
		try {
			onEntityHitMH.invoke(projectileEntity, p_213868_1_);
		}
		catch (Throwable throwable) {
			throwable.printStackTrace();
		}
	}

	static Method onBlockHitMethod;
	public static MethodHandle onBlockHitMH;
	public static void ProjectileEntity_onBlockHit(ProjectileEntity projectileEntity, BlockRayTraceResult p_213868_1_) {
		try {
			onBlockHitMH.invoke(projectileEntity, p_213868_1_);
		}
		catch (Throwable throwable) {
			throwable.printStackTrace();
		}
	}

	public static void init() {
		try {
			onEntityHitMethod = ObfuscationReflectionHelper.findMethod(ProjectileEntity.class, "func_213868_a", EntityRayTraceResult.class);
			onEntityHitMH = lookup.unreflect(onEntityHitMethod);

			onBlockHitMethod = ObfuscationReflectionHelper.findMethod(ProjectileEntity.class, "func_230299_a_", BlockRayTraceResult.class);
			onBlockHitMH = lookup.unreflect(onBlockHitMethod);
		} catch (IllegalAccessException e) {
			ProgressiveBosses.LOGGER.error(e.toString());
		}
	}
}
