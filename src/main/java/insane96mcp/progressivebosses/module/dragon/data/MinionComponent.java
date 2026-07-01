package insane96mcp.progressivebosses.module.dragon.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import insane96mcp.insanelib.ai.ILNearestAttackableTargetGoal;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.mixin.accessor.MobAccessor;
import insane96mcp.progressivebosses.module.dragon.ai.DragonMinionAttackGoal;
import insane96mcp.progressivebosses.utils.LogHelper;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@JsonAdapter(MinionComponent.Serializer.class)
public class MinionComponent implements DragonComponent {
    public static final ResourceLocation FOLLOW_RANGE_BONUS_ID = ProgressiveBosses.id("follow_range_bonus");

    public DragonValue health;
    public DragonValue spawned;
    public DragonValue averageCooldown;
    public DragonValue deltaCooldown;

    public static final String DRAGON_MINION = ProgressiveBosses.RESOURCE_PREFIX + "dragon_minion";
    public static final String DRAGON_MINION_COOLDOWN = ProgressiveBosses.RESOURCE_PREFIX + "dragon_minion_cooldown";

    public static void onMinionHurt(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof Shulker shulker))
            return;

        CompoundTag compoundNBT = shulker.getPersistentData();
        if (!compoundNBT.contains(DRAGON_MINION))
            return;

        if (event.getSource().getEntity() instanceof EnderDragon)
            //TODO More damage hooks
            event.setNewDamage(0f);
    }

    public void summonMinion(EnderDragon dragon, Level world, Vec3 pos) {
        Shulker shulker = EntityType.SHULKER.create(world);
        if (shulker == null) {
            LogHelper.warn("Failed to summon Dragon Minion");
            return;
        }
        CompoundTag minionTags = shulker.getPersistentData();
        minionTags.putBoolean(DRAGON_MINION, true);

        minionTags.putBoolean("mobspropertiesrandomness:processed", true);

        shulker.setPos(pos.x, pos.y, pos.z);
        shulker.setCustomName(Component.translatable(Util.makeDescriptionId("entity", ResourceLocation.parse(DRAGON_MINION))));
        ((MobAccessor) shulker).setLootTable(BuiltInLootTables.EMPTY);
        shulker.setPersistenceRequired();
        shulker.setVariant(Optional.of(DyeColor.PURPLE));

        shulker.getAttribute(Attributes.MAX_HEALTH).setBaseValue(this.health.getValue(dragon));
        MCUtils.applyModifier(shulker, Attributes.FOLLOW_RANGE, FOLLOW_RANGE_BONUS_ID, 96, AttributeModifier.Operation.ADD_VALUE);

        world.addFreshEntity(shulker);
        setMinionAI(shulker);
    }

    public static void setMinionAI(Shulker shulker) {
        ArrayList<Goal> toRemove = new ArrayList<>();
        shulker.goalSelector.getAvailableGoals().forEach(goal -> {
            if (goal.getGoal() instanceof Shulker.ShulkerAttackGoal)
                toRemove.add(goal.getGoal());
        });
        toRemove.forEach(shulker.goalSelector::removeGoal);
        shulker.goalSelector.addGoal(4, new DragonMinionAttackGoal(shulker, 200));

        toRemove.clear();
        shulker.targetSelector.getAvailableGoals().forEach(goal -> {
            if (goal.getGoal() instanceof NearestAttackableTargetGoal)
                toRemove.add(goal.getGoal());
            if (goal.getGoal() instanceof HurtByTargetGoal)
                toRemove.add(goal.getGoal());
        });
        toRemove.forEach(shulker.targetSelector::removeGoal);

        shulker.targetSelector.addGoal(2, new ILNearestAttackableTargetGoal<>(shulker, Player.class, false).setIgnoreLineOfSight());
        shulker.targetSelector.addGoal(1, new HurtByTargetGoal(shulker, Shulker.class, EnderDragon.class));
    }

    @Override
    public void tick(EnderDragon dragon) {
        Level level = dragon.level();

        CompoundTag dragonTags = dragon.getPersistentData();
        int cooldown = dragonTags.getInt(DRAGON_MINION_COOLDOWN);
        if (--cooldown > 0) {
            dragonTags.putInt(DRAGON_MINION_COOLDOWN, cooldown);
            return;
        }

        //If there is no player on the main island don't spawn minions
        BlockPos centerPodium = dragon.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.getLocation(BlockPos.ZERO));
        AABB bb = new AABB(centerPodium).inflate(96d);
        List<ServerPlayer> players = level.getEntitiesOfClass(ServerPlayer.class, bb);

        if (players.isEmpty())
            return;

        cooldown = (int) level.random.triangle(this.averageCooldown.getIntValue(dragon), this.deltaCooldown.getIntValue(dragon));
        dragonTags.putInt(DRAGON_MINION_COOLDOWN, cooldown);
        int spawned = this.spawned.getIntValue(dragon);
        for (int i = 0; i < spawned; i++) {
            float angle = level.random.nextFloat() * (float) Math.PI * 2f;
            float x = (float) (Math.cos(angle) * (Mth.nextFloat(dragon.getRandom(), 1f, 4f)));
            float z = (float) (Math.sin(angle) * (Mth.nextFloat(dragon.getRandom(), 1f, 4f)));
            float y = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, BlockPos.containing(x, 255, z)).getY();
            summonMinion(dragon, level, new Vec3(x, y, z));
        }
    }

    @Override
    public void apply(EnderDragon dragon) {
        int cooldown = (int) dragon.getRandom().triangle(this.averageCooldown.getIntValue(dragon), this.deltaCooldown.getIntValue(dragon));
        dragon.getPersistentData().putInt(DRAGON_MINION_COOLDOWN, cooldown);
    }

    public static class Serializer implements JsonDeserializer<MinionComponent> {
        @Override
        public MinionComponent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            MinionComponent component = new MinionComponent();
            JsonObject jObject = json.getAsJsonObject();
            component.health = GsonHelper.getAsObject(jObject, "health", context, DragonValue.class);
            component.spawned = GsonHelper.getAsObject(jObject, "spawned", context, DragonValue.class);
            component.averageCooldown = GsonHelper.getAsObject(jObject, "average_cooldown", context, DragonValue.class);
            component.deltaCooldown = GsonHelper.getAsObject(jObject, "delta_cooldown", context, DragonValue.class);
            return component;
        }
    }
}
