package insane96mcp.progressivebosses.mixin;

import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import insane96mcp.progressivebosses.module.wither.WitherFeature;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity implements TraceableEntity {

	public ItemEntityMixin(EntityType<?> p_19870_, Level p_19871_) {
		super(p_19870_, p_19871_);
	}

	@Shadow public abstract ItemStack getItem();

	@Inject(at = @At("HEAD"), method = "hurt", cancellable = true)
	public void onItemHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if (!this.getItem().isEmpty()
				&& ((this.getItem().is(WitherFeature.WITHER_INVULNERABLE) && source.getEntity() instanceof WitherBoss)
				|| (this.getItem().is(WitherFeature.WORLD_INVULNERABLE) && !source.is(DamageTypeTags.BYPASSES_RESISTANCE))
				|| (this.getItem().is(DragonFeature.DRAGON_INVULNERABLE) && source.getEntity() instanceof EnderDragon)))
			cir.setReturnValue(false);
	}
}
