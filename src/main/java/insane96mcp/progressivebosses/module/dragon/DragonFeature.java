package insane96mcp.progressivebosses.module.dragon;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.event.DragonPhaseEvent;
import insane96mcp.progressivebosses.module.dragon.corruptedendcrystal.CorruptedEndCrystal;
import insane96mcp.progressivebosses.module.dragon.data.*;
import insane96mcp.progressivebosses.module.dragon.phase.PBDragonHoldingPatternPhase;
import insane96mcp.progressivebosses.network.SyncDragonAnger;
import insane96mcp.progressivebosses.utils.LogHelper;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Label(name = "Ender Dragon Feature")
@LoadFeature(module = ProgressiveBosses.RESOURCE_PREFIX + "ender_dragon", canBeDisabled = false)
public class DragonFeature extends Feature {
    public static final TagKey<Item> DRAGON_INVULNERABLE = ItemTags.create(new ResourceLocation(ProgressiveBosses.MOD_ID, "dragon_invulnerable"));
    public static final String LEVEL = ProgressiveBosses.RESOURCE_PREFIX + "level";

    public static final UUID KNOCKBACK_REDUCTION_UUID = UUID.fromString("db8b06d6-791d-4f3b-867d-35e384af9eab");

    @Config
    @Label(name = "Explosion Immune Crystals", description = "Crystals can no longer be destroyed by other explosions.")
    public static Boolean explosionImmuneCrystals = true;

    @Config
    @Label(name = "Enable Fixes", description = """
            Enable some fixes for the Ender Dragon:
             - Dragon Head and Neck have been repositioned/resized correctly.
             - Dragon will now play the growl sound only 4 times/second when respawning and when sitting instead of 20/second (so your ears shouldn't blow up anymore)
             - When the crystals that respawn the dragon in the center are destroyed, the fire is extinguished
             - Ender Dragon can now rise and fall faster (somewhere around 1.14 the multiplier for the y speed was reduced to 0.01 instead of 0.1 https://bugs.mojang.com/browse/MC-272431)
             - Dragon is moved exactly at the center of the well when landed
             - Fixes players accumulating knockback when hit by the dragon and then launching like a rocket
             - Sets a portal cooldown to 4 years so she no longer goes through end gates""")
    public static Boolean enableFixes = true;

    /// Temp variable to keep track of the level of the dragon between placing the End Crystal and summoning the dragon
    public static byte dragonLvl = 0;

    public DragonFeature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    @Nullable
    public static Player getRandomPlayer(EnderDragon dragon, Level level, int range) {
        List<Player> players = level.getEntitiesOfClass(Player.class, dragon.getBoundingBox().inflate(range));
        if (players.isEmpty())
            return null;

        return players.get(Mth.nextInt(level.random, 0, players.size() - 1));
    }

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || event.getLevel().isClientSide)
            return;
        onDragonJoinLevel(event);
        DragonMinion.onShulkerSpawn(event);
        if (event.getEntity() instanceof EnderDragon dragon)
            ((ServerLevel) dragon.level()).players().forEach(player -> SyncDragonAnger.sync(player, dragon));
        else if (event.getEntity() instanceof ServerPlayer player)
            ((ServerLevel) player.level()).getDragons().forEach(dragon -> SyncDragonAnger.sync(player, dragon));
    }

    public void onDragonJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof EnderDragon dragon)
                || dragon.getPersistentData().contains(ProgressiveBosses.RESOURCE_PREFIX + "processed"))
            return;

        if (enableFixes) {
            dragon.setPortalCooldown(Integer.MAX_VALUE);
            MCUtils.applyModifier(dragon, Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_REDUCTION_UUID, "Dragon no knockback", 1.0f, AttributeModifier.Operation.ADDITION, true);
        }

        if (!dragon.getPersistentData().contains(LEVEL))
            dragon.getPersistentData().putByte(LEVEL, dragonLvl);

        DragonDefinition stats = getDragonDefinition(dragon).orElse(null);
        if (stats == null) {
            LogHelper.warn("Failed to get Dragon Stats for level %s", dragon.getPersistentData().getByte(LEVEL));
            return;
        }
        stats.apply(dragon);
        dragon.setCustomName(Component.translatable(Util.makeDescriptionId("entity", ForgeRegistries.ENTITY_TYPES.getKey(dragon.getType())) + "." + dragon.getPersistentData().getByte(LEVEL)));
        dragon.getPersistentData().putBoolean(ProgressiveBosses.RESOURCE_PREFIX + "processed", true);
    }

    @SubscribeEvent
    public void onExpDrop(LivingExperienceDropEvent event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof EnderDragon dragon)
                || event.getDroppedExperience() == 0)
            return;

        getDragonDefinition(dragon)
                .flatMap(definition -> definition.getComponent(LootComponent.class))
                .map(lootComponent -> lootComponent.xpDropped)
                .ifPresent(xpDropped -> {
                    //This will 100% break if any other mod changes experience dropped
                    if (event.getDroppedExperience() == Mth.floor(12000 * 0.08F)
                            || event.getDroppedExperience() == Mth.floor(500 * 0.08F))
                        event.setDroppedExperience(Mth.floor(xpDropped * 0.08f));
                    else if (event.getDroppedExperience() == Mth.floor(12000 * 0.2F)
                            || event.getDroppedExperience() == Mth.floor(500 * 0.2F))
                        event.setDroppedExperience(Mth.floor(xpDropped * 0.2f));
                });
    }

    @SubscribeEvent
    public void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof EnderDragon dragon)
                || !dragon.isDeadOrDying())
            return;

        List<ShulkerBullet> bullets = dragon.level().getEntitiesOfClass(ShulkerBullet.class, dragon.getBoundingBox().inflate(128));
        bullets.forEach(Entity::discard);
        List<Shulker> minions = dragon.level().getEntitiesOfClass(Shulker.class, dragon.getBoundingBox().inflate(128), shulker -> shulker.getPersistentData().contains(DragonMinion.DRAGON_MINION));
        minions.forEach(Entity::discard);
    }

    @SubscribeEvent
    public void onUpdate(LivingEvent.LivingTickEvent event) {
        CrystalRespawnComponent.tickCrystalPhantom(event);
        if (!this.isEnabled()
                || !(event.getEntity() instanceof EnderDragon dragon))
            return;

        DragonDefinition definition = getDragonDefinition(dragon).orElse(null);
        if (definition == null)
            return;
        definition.tick(dragon);
        if (dragon.level().isClientSide)
            AngerComponent.tickClient(dragon);
        DragonMinion.tick(dragon);
    }

    @SubscribeEvent
    public void onSetPhase(DragonPhaseEvent.Change event) {
        if (!this.isEnabled())
            return;

        EnderDragon dragon = event.getDragon();
        DragonDefinition definition = getDragonDefinition(dragon).orElse(null);
        if (definition == null)
            return;

        definition.components.forEach(component -> component.onPhaseChange(event, dragon));
        if (event.getNewPhase() == EnderDragonPhase.DYING)
            return;
        if (event.getNewPhase() == EnderDragonPhase.HOLDING_PATTERN)
            event.setNewPhase(PBDragonHoldingPatternPhase.getPhaseType());
    }

    @SubscribeEvent
    public void onPhaseBegin(DragonPhaseEvent.Begin event) {
        if (!this.isEnabled())
            return;

        DragonDefinition definition = getDragonDefinition(event.getDragon()).orElse(null);
        if (definition == null)
            return;

        definition.components.forEach(component -> {
            if (component instanceof PhaseChanger phaseChanger)
                phaseChanger.onPhaseBegin(event, event.getDragon());
        });
    }

    public static void onCrystalDestroyed(EndDragonFight fight, EndCrystal crystal, DamageSource damageSource) {
        if (!(crystal.level() instanceof ServerLevel serverLevel)
                || fight.getDragonUUID() == null)
            return;

        EnderDragon dragon = (EnderDragon) serverLevel.getEntity(fight.getDragonUUID());
        if (dragon == null)
            return;
        getDragonDefinition(dragon).ifPresent(
            dragonDefinition -> {
                dragonDefinition.components.forEach(component -> component.onCrystalDestroyed(dragon, crystal, fight.getCrystalsAlive()));
            }
        );
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (!this.isEnabled())
            return;

        DragonMinion.onMinionHurt(event);
        CrystalRespawnComponent.onPhantomHurt(event);

        if (!(event.getEntity() instanceof EnderDragon dragon))
            return;
        getDragonDefinition(dragon).ifPresent(
                dragonDefinition -> {
                    dragonDefinition.components.forEach(component -> component.onLivingHurt(event, dragon));
                }
        );
    }

    public void onDragonHurt(LivingHurtEvent event) {
    }

    public static Optional<DragonDefinition> getDragonDefinition(EnderDragon dragon) {
        byte lvl = dragon.getPersistentData().getByte(LEVEL);
        return getDragonDefinition(lvl);
    }

    public static Optional<DragonDefinition> getDragonDefinition(byte lvl) {
        return Optional.ofNullable(DragonDefinitionReloadListener.STATS_MAP.get(lvl));
    }

    public static byte getDragonLvl(List<EndCrystal> respawningCrystals) {
        byte corrupted = 0;
        for (EndCrystal crystal : respawningCrystals) {
            if (crystal instanceof CorruptedEndCrystal)
                corrupted++;
        }
        return corrupted;
    }

    public static float neckOffsetXZ() {
        return 3.8f;
    }

    public static float tailOffsetY() {
        return 0f;
    }

    public static float headOffsetXZ() {
        return 6f;
    }

    public static float headOffsetSittingY() {
        return 0f;
    }

    public static float headOffsetY(float original) {
        return original + 1.5f;
    }
}
