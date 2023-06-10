package insane96mcp.progressivebosses.mixin;

import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.setup.PBItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity implements TraceableEntity {

	private static final TagKey<Item> WITHER_INVULNERABLE = ItemTags.create(new ResourceLocation(ProgressiveBosses.MOD_ID, "wither_invulnerable"));

	public ItemEntityMixin(EntityType<?> p_19870_, Level p_19871_) {
		super(p_19870_, p_19871_);
	}

	@Shadow public abstract ItemStack getItem();

	@Inject(at = @At("HEAD"), method = "hurt", cancellable = true)
	public void onItemHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if (!this.getItem().isEmpty() && this.getItem().is(PBItems.NETHER_STAR_SHARD.get()) && source.getEntity() instanceof WitherBoss)
			cir.setReturnValue(false);
		else if (!this.getItem().isEmpty() && this.getItem().is(WITHER_INVULNERABLE) && this.tickCount < 100 && source.getEntity() instanceof WitherBoss)
			cir.setReturnValue(false);
	}
}
