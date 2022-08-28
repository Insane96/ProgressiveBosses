package insane96mcp.progressivebosses.setup;

import insane96mcp.progressivebosses.ProgressiveBosses;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

public class Reflection {
	static MethodHandles.Lookup lookup = MethodHandles.lookup();

	static Method onHitEntityMethod;
	static MethodHandle onHitEntityHandler;
	public static void Projectile_onHitEntity(Projectile projectileEntity, EntityHitResult p_213868_1_) {
		try {
			onHitEntityHandler.invoke(projectileEntity, p_213868_1_);
		}
		catch (Throwable throwable) {
			throwable.printStackTrace();
		}
	}

	static Method onHitBlockMethod;
	static MethodHandle onHitBlockHandler;
	public static void Projectile_onHitBlock(Projectile projectileEntity, BlockHitResult p_213868_1_) {
		try {
			onHitBlockHandler.invoke(projectileEntity, p_213868_1_);
		}
		catch (Throwable throwable) {
			throwable.printStackTrace();
		}
	}

	public static void init() {
		try {
			onHitEntityMethod = ObfuscationReflectionHelper.findMethod(Projectile.class, "m_5790_", EntityHitResult.class);
			onHitEntityHandler = lookup.unreflect(onHitEntityMethod);

			onHitBlockMethod = ObfuscationReflectionHelper.findMethod(Projectile.class, "m_8060_", BlockHitResult.class);
			onHitBlockHandler = lookup.unreflect(onHitBlockMethod);
		} catch (IllegalAccessException e) {
			ProgressiveBosses.LOGGER.error(e.toString());
		}
	}
}
