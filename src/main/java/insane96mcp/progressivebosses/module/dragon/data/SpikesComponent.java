package insane96mcp.progressivebosses.module.dragon.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import insane96mcp.progressivebosses.setup.PBEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@JsonAdapter(SpikesComponent.Serializer.class)
public class SpikesComponent implements DragonComponent {
    public int cages;
    public float corruptedChance;
    public int crystalsInside;
    public float insideCorruptedChance;

    private static final ResourceLocation ENDERGETIC_CRYSTAL_LOCATION = new ResourceLocation("endergetic:crystal_holder");

    public static boolean onCrystalDamagedByExplosion(DamageSource source) {
        if (!Feature.isEnabled(DragonFeature.class)
                || !DragonFeature.explosionImmuneCrystals)
            return false;

        return source.is(DamageTypeTags.IS_EXPLOSION);
    }

    @Override
    public void apply(EnderDragon dragon) {
        if (this.crystalsInside <= 0)
            return;
        List<EndCrystal> crystals = new ArrayList<>();

        List<SpikeFeature.EndSpike> spikes = new ArrayList<>(SpikeFeature.getSpikesForLevel((ServerLevel) dragon.level()));
        Collections.shuffle(spikes);

        for (SpikeFeature.EndSpike spike : spikes) {
            crystals.addAll(dragon.level().getEntitiesOfClass(EndCrystal.class, spike.getTopBoundingBox(), EndCrystal::showsBottom));
        }

        int crystalSpawned = 0;

        for (EndCrystal crystal : crystals) {
            generateCrystalInTower(dragon.level(), crystal.getBlockX(), crystal.getBlockY(), crystal.getBlockZ());

            if (++crystalSpawned >= crystalsInside)
                break;
        }
    }

    public void generateCrystalInTower(Level level, int x, int y, int z) {
        BlockPos centerPodium = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION);
        while (!level.getBlockState(centerPodium).is(Blocks.BEDROCK) && centerPodium.getY() > level.getSeaLevel()) {
            centerPodium = centerPodium.below();
        }

        //TODO Configurable
        int spawnY = y - 16;
        if (spawnY < centerPodium.getY())
            spawnY = centerPodium.getY();
        BlockPos crystalPos = new BlockPos(x, spawnY, z);

        Stream<BlockPos> blocks = BlockPos.betweenClosedStream(crystalPos.offset(-1, -1, -1), crystalPos.offset(1, 1, 1));
        blocks.forEach(pos -> level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState()));

        BlockState baseBlockState = Blocks.BEDROCK.defaultBlockState();
        if (ModList.get().isLoaded("endergetic"))
            baseBlockState = ForgeRegistries.BLOCKS.getValue(ENDERGETIC_CRYSTAL_LOCATION).defaultBlockState();
        level.setBlockAndUpdate(crystalPos.offset(0, -1, 0), baseBlockState);

        EndCrystal crystal = level.random.nextFloat() < this.insideCorruptedChance ? PBEntities.CORRUPTED_END_CRYSTAL.get().create(level) : EntityType.END_CRYSTAL.create(level);
        crystal.setPos(crystalPos.getX() + .5, crystalPos.getY(), crystalPos.getZ() + .5);
        crystal.setShowBottom(true);

        level.addFreshEntity(crystal);
    }

    public static class Serializer implements JsonDeserializer<SpikesComponent> {
        @Override
        public SpikesComponent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            SpikesComponent component = new SpikesComponent();
            JsonObject jObject = json.getAsJsonObject();
            component.cages = GsonHelper.getAsInt(jObject, "cages");
            component.corruptedChance = GsonHelper.getAsFloat(jObject, "corrupted_chance");
            component.crystalsInside = GsonHelper.getAsInt(jObject, "crystals_inside");
            component.insideCorruptedChance = GsonHelper.getAsFloat(jObject, "inside_corrupted_chance");
            return component;
        }
    }
}
