package insane96mcp.progressivebosses.setup;

import insane96mcp.progressivebosses.ProgressiveBosses;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

//TODO Dafuq is this
public class Reflection {
	static MethodHandles.Lookup lookup = MethodHandles.lookup();

	static Method onEntityHitMethod;
	public static MethodHandle onEntityHitMH;
	public static void ProjectileEntity_onEntityHit(Projectile projectileEntity, EntityHitResult p_213868_1_) {
		try {
			onEntityHitMH.invoke(projectileEntity, p_213868_1_);
		}
		catch (Throwable throwable) {
			throwable.printStackTrace();
		}
	}

	static Method onBlockHitMethod;
	public static MethodHandle onBlockHitMH;
	public static void ProjectileEntity_onBlockHit(Projectile projectileEntity, BlockHitResult p_213868_1_) {
		try {
			onBlockHitMH.invoke(projectileEntity, p_213868_1_);
		}
		catch (Throwable throwable) {
			throwable.printStackTrace();
		}
	}

	public static void init() {
		try {
			onEntityHitMethod = ObfuscationReflectionHelper.findMethod(Projectile.class, "onHitEntity", EntityHitResult.class);
			onEntityHitMH = lookup.unreflect(onEntityHitMethod);

			onBlockHitMethod = ObfuscationReflectionHelper.findMethod(Projectile.class, "onHitBlock", BlockHitResult.class);
			onBlockHitMH = lookup.unreflect(onBlockHitMethod);
		} catch (IllegalAccessException e) {
			ProgressiveBosses.LOGGER.error(e.toString());
		}
	}
}
