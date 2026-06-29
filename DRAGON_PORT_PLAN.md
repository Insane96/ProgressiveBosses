# Ender Dragon Module — 1.20.1 → 1.21.1 Porting Plan

Source: `C:\Users\delvi\source\repos\Insane96\ProgressiveBosses_1.20.1`  
Target: this project (1.21.1 NeoForge)
InsaneLib: `C:\Users\delvi\source\repos\Insane96\InsaneLib`

---

## Overview

The dragon module consists of ~48 Java files, 18 mixins, 2 network packets, 2 custom events, 4 custom dragon phases, and a set of JSON data/resource files. The primary migration challenge is the **MinecraftForge → NeoForge** API shift, plus **insanelib API changes** between the two versions.

The 1.21.1 project already has the module entry stub (`PBModules.ENDER_DRAGON`) wired up. The mixin config (`progressivebosses.mixins.json`) is empty and ready to receive entries.

---

## Global API Change Reference

Every file you touch will need these substitutions.

### Event system

| 1.20.1 (Forge)                                         | 1.21.1 (NeoForge)                                            |
|--------------------------------------------------------|--------------------------------------------------------------|
| `net.minecraftforge.event.*`                           | `net.neoforged.neoforge.event.*`                             |
| `net.minecraftforge.eventbus.api.SubscribeEvent`       | `net.neoforged.bus.api.SubscribeEvent`                       |
| `net.minecraftforge.eventbus.api.IEventBus`            | `net.neoforged.bus.api.IEventBus`                            |
| `net.minecraftforge.common.MinecraftForge.EVENT_BUS`   | `net.neoforged.neoforge.common.NeoForge.EVENT_BUS`           |
| `TickEvent.LevelTickEvent` (+ `event.level`)           | `LevelTickEvent` (+ `event.getLevel()`)                      |
| `LivingEvent.LivingTickEvent`                          | `LivingTickEvent`                                            |
| `EntityJoinLevelEvent`                                 | same class, `net.neoforged.neoforge.event.entity`            |
| `EntityLeaveLevelEvent`                                | same class                                                   |
| `LivingHurtEvent`                                      | same class                                                   |
| `LivingExperienceDropEvent`                            | same class                                                   |
| `ItemTooltipEvent`                                     | same class                                                   |
| `DragonPhaseEvent` (custom event, extends `LivingEvent`) | NeoForge still has `LivingEvent` at `net.neoforged.neoforge.event.entity.living.LivingEvent` — **import change only, no base class change needed** |
| `AddReloadListenerEvent`                               | **`AddServerReloadListenersEvent`** (renamed in NeoForge)    |
| `EntityAttributeCreationEvent`                         | `EntityAttributeCreationEvent` (NeoForge)                    |
| `EntityRenderersEvent.RegisterRenderers`               | `EntityRenderersEvent.RegisterRenderers` (NeoForge)          |
| `BuildCreativeModeTabContentsEvent`                    | same in NeoForge                                             |
| `RegisterCommandsEvent`                                | same in NeoForge                                             |

### Distribution annotations

| 1.20.1 (Forge)                                        | 1.21.1 (NeoForge)                                     |
|-------------------------------------------------------|-------------------------------------------------------|
| `@OnlyIn(Dist.CLIENT)` from `net.minecraftforge.api.distmarker` | `@OnlyIn(Dist.CLIENT)` from `net.neoforged.api.distmarker` |

### Registry / DeferredRegister

| 1.20.1 (Forge)                                        | 1.21.1 (NeoForge)                                     |
|-------------------------------------------------------|-------------------------------------------------------|
| `DeferredRegister.create(ForgeRegistries.ITEMS, …)`   | `DeferredRegister.createItems(MOD_ID)` or `DeferredRegister.create(Registries.ITEM, MOD_ID)` |
| `RegistryObject<T>`                                   | `DeferredHolder<T>` (or `DeferredItem`, `DeferredBlock`, etc.) |
| `ForgeRegistries.ENTITY_TYPES`                        | `Registries.ENTITY_TYPE`                              |

### ResourceLocation constructor — removed in 1.21

`new ResourceLocation("mod", "path")` no longer exists. Use `ResourceLocation.fromNamespaceAndPath(...)` or the mod's `ProgressiveBosses.id(...)` helper. This appears in every renderer, tag reference, and any inline RL construction.

```java
// 1.20.1
new ResourceLocation(ProgressiveBosses.MOD_ID, "textures/entity/foo.png")
// 1.21.1
ProgressiveBosses.id("textures/entity/foo.png")
// or
ResourceLocation.fromNamespaceAndPath(ProgressiveBosses.MOD_ID, "textures/entity/foo.png")
```

### AttributeModifier — **breaking change in 1.21**

`AttributeModifier` no longer takes a UUID. It now takes a `ResourceLocation` as its identifier:

```java
// 1.20.1
MCUtils.applyModifier(dragon, Attributes.KNOCKBACK_RESISTANCE,
    KNOCKBACK_REDUCTION_UUID, "Dragon no knockback", 1.0f,
    AttributeModifier.Operation.ADDITION, true);

// 1.21.1
dragon.getAttribute(Attributes.KNOCKBACK_RESISTANCE)
    .addOrUpdatePermanentModifier(new AttributeModifier(
        ProgressiveBosses.id("dragon_knockback_resistance"),
        1.0f, AttributeModifier.Operation.ADD_VALUE));
```

Check whether the insanelib `MCUtils.applyModifier` helper has been updated in the new version; if so, keep using it.

### Network — complete API rewrite

The old `SimpleChannel` / `NetworkRegistry` pattern is gone. NeoForge uses `CustomPacketPayload`.

For each packet (`SyncDragonAnger`, `BeginBlastAttackPhase`):

```java
// 1.21.1 pattern
public record SyncDragonAnger(int entityId, boolean isAngry)
        implements CustomPacketPayload {
    public static final Type<SyncDragonAnger> TYPE =
        new Type<>(ProgressiveBosses.id("sync_dragon_anger"));
    public static final StreamCodec<FriendlyByteBuf, SyncDragonAnger> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.INT, SyncDragonAnger::entityId,
            ByteBufCodecs.BOOL, SyncDragonAnger::isAngry,
            SyncDragonAnger::new);
    @Override public Type<SyncDragonAnger> type() { return TYPE; }
}
```

Register in `ProgressiveBosses` constructor via `PayloadTypeRegistry.playS2C().register(...)`.  
Handle on client via `NeoForge.EVENT_BUS.addListener(PayloadEvent.class, ...)` or the `IPayloadHandler` registered at the same time.

There is no `NetworkHandler.java` class needed anymore — registration goes directly in the mod constructor or a `RegisterPayloadsEvent` listener.

### Loot table / entity drops — **field type changed in 1.21**

`Mob.lootTable` is now `Optional<ResourceKey<LootTable>>`, not `ResourceLocation`. The `EnderDragonMixin` loot injection does:
```java
this.lootTable = lootComponent.lootTable; // won't compile
this.dropFromLootTable(killerDamageSource, false); // signature changed
```

The fix:
- `LootComponent.lootTable` field type changes from `ResourceLocation` to `ResourceKey<LootTable>`.
- Assignment in the mixin becomes `this.lootTable = Optional.of(lootComponent.lootTable)`.
- Verify `dropFromLootTable`'s exact signature in 1.21.1 (likely requires a `ServerLevel` parameter now).

### `ForgeRegistries.ENTITY_TYPES.getKey()`

Replace with `BuiltInRegistries.ENTITY_TYPE.getKey()`.

### Access Transformer

NeoForge ATs use **deobfuscated (Parchment) names** instead of SRG names. The 1.20.1 AT (`f_64069_`, etc.) must be translated. Key dragon entries needed:

```
# EndDragonFight
public net.minecraft.world.level.dimension.end.EndDragonFight previouslyKilled
public net.minecraft.world.level.dimension.end.EndDragonFight portalLocation

# EnderDragonPhase
public net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase create(Ljava/lang/Class;Ljava/lang/String;)Lnet/minecraft/world/entity/boss/enderdragon/phases/EnderDragonPhase;
```

Place in `src/main/resources/META-INF/accesstransformer.cfg` and reference it in `build.gradle` via `neoForge.accessTransformers.add(...)` or in `neoforge.mods.toml`.

### Custom dragon phases (`EnderDragonPhase<T>`)

`EnderDragonPhase.create()` is used to register new phase types. Confirm this API exists in 1.21.1 (it uses reflection on the static field; AT needed). The phases themselves extend vanilla phase instances — check if method signatures changed (specifically `begin()`, `end()`, `doServerTick()`, `getPhase()`).

### Feature/Module system (insanelib)

The 1.20.1 `@LoadFeature` annotation and `Feature` base class are gone in the new insanelib. The 1.21.1 project uses `Module.Builder.create(...)` directly in `PBModules`. Dragon features will need to register their event listeners manually in `DragonFeature` via the `IEventBus` received at module construction, rather than relying on `@SubscribeEvent` auto-scan.

Check the new insanelib source or jar to confirm what `Feature`/`Module` base class provides and how `isEnabled()` works.

### Mixin config file name

| 1.20.1                            | 1.21.1                                |
|-----------------------------------|---------------------------------------|
| `mixins.progressivebosses.json`   | `progressivebosses.mixins.json`       |

The 1.21.1 config already exists and is empty. NeoForge embeds mixin support — no `mixin {}` Gradle block or `refmap` field needed. Remove `"plugin"` if the `ModLoadedPlugin` class import relies on `net.minecraftforge.fml.loading.LoadingModList` (replace with `net.neoforged.fml.loading.LoadingModList`).

---

## File Inventory

### Phase 1 — Shared infrastructure (no dragon-specific logic)

These are needed by all modules, not just dragon.

| File | Notes |
|------|-------|
| `data/ComponentRegistry.java` | Gson type registry; if replacing with self-registering components (see enhancements), rethink this |
| `data/DifficultyValue.java` | Renamed from `Difficulty` — data class; no other change |
| ~~`module/ILvl.java`~~ | **Dropped** — replace all `ILvl` usages with `LvlHelper.getLvl(entity)` / `LvlHelper.setLvl(entity, lvl)` |
| `loot/LvlCondition.java` | Custom loot condition; `LootItemConditionType` registration may differ |
| `loot/RandomChanceWithLvlCondition.java` | Same |
| `loot/SetCountPerLvl.java` | Same |
| ~~`utils/LogHelper.java`~~ | **Dropped** — all callers use `ProgressiveBosses.LOGGER` directly |
| `utils/LvlHelper.java` | **Simplified** — no `ILvl` interface; just reads/writes `ModNBTData` with key `ProgressiveBosses.id("lvl")` (`LvlHelper.LEVEL_KEY`) |
| `commands/PBCommand.java` | Command registration; `RegisterCommandsEvent` package change |

### Phase 2 — Registration stubs (items, entities, blocks)

| File | Notes |
|------|-------|
| `setup/PBItems.java` | Use `DeferredRegister.createItems(MOD_ID)`; `RegistryObject` → `DeferredItem` |
| `setup/PBEntities.java` | Use `DeferredRegister.create(Registries.ENTITY_TYPE, MOD_ID)`; `RegistryObject` → `DeferredHolder` |
| `setup/PBBlocks.java` | Same |
| `setup/PBLoot.java` | Loot condition/function type registry |
| `setup/ClientSetup.java` | `EntityRenderersEvent.RegisterRenderers` — package change only |
| `setup/Config.java` (if kept) | Config builder integration differs between insanelib versions |

### Phase 3 — Events and network

| File | Notes |
|------|-------|
| `event/DragonPhaseEvent.java` | Keep `extends LivingEvent` — NeoForge still has it; import change only |
| `event/PBEventFactory.java` | Update import, keep logic |
| `network/SyncDragonAnger.java` | Rewrite as `record` implementing `CustomPacketPayload`; see pattern above |
| `network/BeginBlastAttackPhase.java` | Same |
| `network/ClientNetworkHandler.java` | Move handler logic to payload handler lambdas |
| `network/NetworkHandler.java` | Delete — registration moves to mod constructor |

### Phase 4 — Core dragon data classes

Port in this order (least to most dependencies):

1. `module/dragon/data/DragonValue.java` — utility; no event imports. **Enhancement opportunity: add per-level scaling** (see enhancements section)
2. `module/dragon/data/DragonMinion.java` — minimal
3. `module/dragon/data/DragonComponent.java` — interface; swap Forge event imports to NeoForge. **Enhancement opportunity: add `validate()`, `saveNBT()`/`loadNBT()` hooks** (see enhancements)
4. `module/dragon/data/DragonDefinition.java` — swap Forge event imports
5. `module/dragon/data/DragonDefinitionReloadListener.java` — `PreparableReloadListener` (vanilla API, unchanged)
6. `module/dragon/data/HealthComponent.java`
7. `module/dragon/data/VulnerabilitiesComponent.java`
8. `module/dragon/data/FlySpeedComponent.java`
9. `module/dragon/data/AngerComponent.java` — sends `SyncDragonAnger`; port after network; convert raw `getPersistentData()` → `ModNBTData`
10. `module/dragon/data/MinionComponent.java` — spawns shulkers; check shulker API; convert raw NBT
11. `module/dragon/data/LandComponent.java`
12. `module/dragon/data/SittingAttackComponent.java`
13. `module/dragon/data/BlastAttackComponent.java` — sends `BeginBlastAttackPhase`; convert raw NBT (`LAST_BLAST_TAG`)
14. `module/dragon/data/StrafePlayerComponent.java`
15. `module/dragon/data/AcidballComponent.java` — uses custom damage type; `DamageSources` API may have changed
16. `module/dragon/data/MeleeDamageComponent.java`
17. `module/dragon/data/ChargePlayerComponent.java`
18. `module/dragon/data/SpikesComponent.java`
19. `module/dragon/data/CrystalRespawnComponent.java`
20. `module/dragon/data/LootComponent.java` — field type: `ResourceLocation` → `ResourceKey<LootTable>`
21. `module/dragon/data/PhaseChanger.java`

### Phase 5 — Custom dragon phases

These are the trickiest: they subclass vanilla phase classes and are registered via `EnderDragonPhase.create()` which needs an AT.

1. `module/dragon/phase/DragonBlastAttackPhase.java` — also calls `dragon.getPersistentData().getLong/putLong(LAST_BLAST_TAG)` in `isInCooldown()` and `doServerTick()`: convert to `ModNBTData`
2. `module/dragon/phase/DragonCrystalRespawnPhase.java`
3. `module/dragon/phase/PBDragonHoldingPatternPhase.java`
4. `module/dragon/phase/PBDragonStrafePlayerPhase.java`

Check each for: `DragonPhaseInstance` method signature changes, `doServerTick()` parameter changes, `getTarget()` return type changes.

### Phase 6 — Corrupted End Crystal entity

1. `module/dragon/corruptedendcrystal/CorruptedEndCrystal.java` — extends `EndCrystal`; check constructor signature
2. `module/dragon/corruptedendcrystal/CorruptedEndCrystalItem.java` — item; check `Item.Properties` changes
3. `module/dragon/corruptedendcrystal/CorruptedEndCrystalRenderer.java` — fix `new ResourceLocation(...)` → `ProgressiveBosses.id(...)`; verify `VertexConsumer.endVertex()` still exists in 1.21.1 (removed in 1.21.2+; confirm exact target version)

### Phase 7 — AI goal

`module/dragon/ai/DragonMinionAttackGoal.java` — goal AI; `Goal` API is unchanged.

### Phase 8 — Main feature class

`module/dragon/DragonFeature.java`:
- Remove `@LoadFeature` — register with module event bus manually
- All `@SubscribeEvent` methods: keep annotation, register `this` on the event bus
- `TickEvent.LevelTickEvent` → `LevelTickEvent`
- `LivingEvent.LivingTickEvent` → `LivingTickEvent`
- `ForgeRegistries.ENTITY_TYPES.getKey(…)` → `BuiltInRegistries.ENTITY_TYPE.getKey(…)`
- `AttributeModifier` UUID → `ResourceLocation` (see above)
- `addClientPack(…)` — check new insanelib API

### Phase 9 — Mixins

Port the 18 server mixins + 2 client mixins. For each, check:
- Target class still exists in 1.21.1 with same name
- `@At(value = "INVOKE", target = "…")` method descriptors (use deobfuscated names in NeoForge)
- `@At(value = "CONSTANT", args = "floatValue=…")` constants (vanilla logic may have changed)
- `@Expression` patterns in MixinExtras (still works; MixinExtras 0.5.3 is included)
- `@Shadow` field names (use Parchment-deobfuscated names)

**Mixin files and specific risks:**

| Mixin | Target | Risk |
|-------|--------|------|
| `EnderDragonMixin` | `EnderDragon` | High — loot table field type change; many constant injection targets |
| `EnderDragonPhaseManagerMixin` | `EnderDragonPhaseManager` | Low |
| `AbstractDragonPhaseInstanceMixin` | `DragonPhaseInstance` | Medium — method names |
| `AbstractDragonSittingPhaseMixin` | `AbstractDragonSittingPhase` | Medium |
| `DragonTakeoffPhaseMixin` | `DragonTakeoffPhase` | Low |
| `DragonSittingScanningPhaseMixin` | `DragonSittingScanningPhase` | Low |
| `DragonSittingFlamingPhaseMixin` | `DragonSittingFlamingPhase` | Low |
| `DragonSittingAttackingPhaseMixin` | `DragonSittingAttackingPhase` | Low |
| `DragonLandingPhaseMixin` | `DragonLandingPhase` | Low |
| `DragonChargePlayerPhaseMixin` | `DragonChargePlayerPhase` | Low |
| `DragonFireballMixin` | `DragonFireball` | Medium — damage source API |
| `EndDragonFightMixin` | `EndDragonFight` | High — portalLocation, respawn logic |
| `EndDragonFightAccessor` | `EndDragonFight` | Medium — accessor field names need AT or Parchment names |
| `EndDragonFightPreventSpawnMixin` | `EndDragonFight` | Low — conditional on `betterendisland` absent |
| `EndDragonFightPreventSpawnEndergeticMixin` | `EndDragonFight` | Low — conditional on `endergetic` present; **check if mod has a 1.21.1 build before porting** |
| `DragonRespawnAnimationSummoningDragonMixin` | `DragonRespawnAnimation` | Low |
| `DragonRespawnAnimationSummoningPillarsMixin` | `DragonRespawnAnimation` | Low |
| `EndCrystalMixin` | `EndCrystal` | Low |
| `ItemEntityMixin` | `ItemEntity` | Low |
| `SpikeFeatureMixin` | `SpikeFeature` | Low |
| `ShulkerMixin` | `Shulker` | Low |
| `ShulkerBulletMixin` | `ShulkerBullet` | Low |
| `ProjectileInvoker` | `Projectile` | Low |
| `LevelRendererMixin` (client) | `LevelRenderer` | Medium |
| `EnderDragonRendererMixin` (client) | `EnderDragonRenderer` | Medium |

For the conditional mixin plugin (`ModLoadedPlugin`):
- Replace `net.minecraftforge.fml.loading.LoadingModList` → `net.neoforged.fml.loading.LoadingModList`
- Same interface `IMixinConfigPlugin` from Mixin (unchanged)
- Add `"plugin": "insane96mcp.progressivebosses.mixin.plugin.ModLoadedPlugin"` back to `progressivebosses.mixins.json`
- If `endergetic` has no 1.21.1 build, remove `EndDragonFightPreventSpawnEndergeticMixin` from the config and simplify `ModLoadedPlugin` accordingly

**Registering mixins** — add each mixin's simple class name to the `"mixins"` (or `"client"`) array in `progressivebosses.mixins.json`.

### Phase 10 — ProgressiveBosses.java wiring

Update the main mod class to:
- Register `DragonDefinitionReloadListener` on **`AddServerReloadListenersEvent`**
- Register network payloads (`PayloadTypeRegistry.playS2C()`) for `SyncDragonAnger` and `BeginBlastAttackPhase`
- Register `ComponentRegistry.init()` in common setup
- Initialize phase types (`DragonCrystalRespawnPhase.init()`, etc.)
- `DragonFeature` event listener registration (replace auto-`@LoadFeature` scan)

### Phase 11 — Resources

Copy verbatim from 1.20.1 (no format changes between versions):

| Source path | Notes |
|-------------|-------|
| `data/progressivebosses/progressivebosses/ender_dragon/base.json` | Dragon definition |
| `data/progressivebosses/progressivebosses/ender_dragon/corrupted.json` | Dragon definition |
| `data/progressivebosses/loot_tables/entities/ender_dragon/base.json` | Loot tables |
| `data/progressivebosses/loot_tables/entities/ender_dragon/corrupted.json` | Loot tables |
| `data/progressivebosses/advancements/max_lvl_dragon.json` | Advancement |
| `data/progressivebosses/damage_type/dragon_fireball.json` | Damage type |
| `data/minecraft/tags/damage_type/always_hurts_ender_dragons.json` | Damage tag |
| `data/progressivebosses/functions/summon_dragon.mcfunction` | MC function |
| `assets/progressivebosses/textures/entity/enderdragon/angered_dragon_eyes.png` | Texture |
| `assets/progressivebosses/textures/entity/corrupted_end_crystal/` | Textures |
| `assets/progressivebosses/models/item/corrupted_end_crystal.json` | Item model |
| Lang keys in `assets/progressivebosses/lang/en_us.json` | Add dragon keys |

---

## ModNBTData — migrate raw getPersistentData() calls

The new insanelib introduces `ModNBTData` as the canonical way to store per-entity data. In 1.20.1 some components still call `entity.getPersistentData()` directly. These must all be converted to `ModNBTData` in the port.

Pattern:
```java
// Old (raw)
dragon.getPersistentData().putFloat(ProgressiveBosses.RESOURCE_PREFIX + "anger", value);
float v = dragon.getPersistentData().getFloat(ProgressiveBosses.RESOURCE_PREFIX + "anger");

// New (ModNBTData)
private static final ResourceLocation ANGER_KEY = ProgressiveBosses.id("anger");
ModNBTData.put(dragon, ANGER_KEY, value);
float v = ModNBTData.get(dragon, ANGER_KEY, Float.class);
```

Files in 1.20.1 that use raw `getPersistentData()` and need conversion:
- `AngerComponent` — `ANGER_TAG`, `ANGERED_TAG` string keys
- `BlastAttackComponent` — forced blast flag
- `MinionComponent` — `DRAGON_MINION` tag for shulker ownership tracking
- `DragonBlastAttackPhase` — `LAST_BLAST_TAG` cooldown tracking in `isInCooldown()` and `doServerTick()`
- Any other component that uses `dragon.getPersistentData().contains(...)`, `getBoolean(...)`, `getFloat(...)`, `putX(...)`

Key `ResourceLocation` fields should be declared as static constants on `DragonFeature` (using `this.createDataKey("name")`) or on each component class, following the pattern already established for `DragonFeature.LEVEL` and `DragonFeature.PROCESSED`.

---

## Component System Enhancements

These improvements should be applied during the port, not deferred. They are grouped by effort.

### Simple

**1. Validation on load**  
Add a `default void validate() {}` hook to `DragonComponent`. After `DragonDefinitionReloadListener` finishes deserializing, call `validate()` on every component. Each component can then check its own fields (negative health, zero-duration timers, etc.) and log meaningful errors rather than silently misbehaving at runtime.

**2. Formalize component NBT ownership**  
Components currently scatter ad-hoc `getPersistentData()` keys across classes. Add `default void saveNBT(CompoundTag tag) {}` / `default void loadNBT(CompoundTag tag) {}` to `DragonComponent`, where each component receives a sub-tag named after its type. This prevents key collisions, makes component state transparent, and aligns with the `ModNBTData` migration.

**3. Consistent component ordering**  
`PhaseChanger` already has `getPriority()`, but the base `DragonComponent` doesn't. The `tick()` / `apply()` dispatch in `DragonDefinition` currently runs components in insertion order. Add a default `getPriority()` returning 0 to the interface and sort the list in `DragonDefinition` after deserialization so behavior is predictable.

---

### Medium

**4. Self-registering components via `ServiceLoader`**  
`ComponentRegistry.init()` is a hand-maintained list. Replace it with `ServiceLoader` (or a `@RegisterComponent("progressivebosses:blast_attack")` annotation + compile-time processor): each component class declares itself, and the registry discovers them automatically. Adding a new component never requires touching `ComponentRegistry`.

**5. Richer `DragonValue` with per-level scaling**  
`DragonValue` currently only has `base` / `angered`. Add an optional `per_level` field so values scale automatically with dragon level, removing the need for separate JSON files per level. Syntax:
```json
{ "base": 200, "per_level": 50 }
```
Value at level N = `base + per_level * N`. This would eventually allow collapsing `base.json` + `corrupted.json` into a single definition file.

**6. Per-component JSON conditions**  
Borrowing from vanilla loot tables, each component entry in JSON could carry an optional `"conditions": [...]` block that gates whether the component activates at runtime (e.g. only above a certain player count, only at night). Logic that is currently baked into `tick()` / `apply()` can be moved to data.

---

### Large / Rewrites

**7. Unify all three bosses under one component system**  
Currently only the dragon uses components; the Wither and Elder Guardian use fixed-field stat classes (`WitherStats`, `ElderGuardianStats`). Abstracting a `BossComponent<T extends LivingEntity>` base and migrating all three bosses would centralize the data architecture, allow reusing components across bosses (e.g. a generic `HealthComponent`), and make future bosses trivial to add. This is the biggest architectural win and sets up everything else.

**8. Codec-based serialization**  
Replace the Gson `@JsonAdapter` inner class per component with Minecraft's `Codec` system. One codec definition covers JSON loading and NBT serialization. It also means component state could be synced to the client via the same codec, removing the need for separate packet classes (`SyncDragonAnger`, `BeginBlastAttackPhase`).

---

## Recommended Work Order

1. Set up AT file with deobfuscated field/method names
2. Port shared infrastructure (Phase 1) — apply enhancement #4 (self-registering) if desired
3. Port registration stubs (Phase 2)
4. Port events and network (Phase 3)
5. Port data classes (Phase 4) — apply enhancements #1, #2, #3 here
6. Port custom phases (Phase 5)
7. Port Corrupted End Crystal (Phase 6)
8. Port AI goal (Phase 7)
9. Port main feature class (Phase 8)
10. Port mixins one by one (Phase 9) — compile-test after each batch
11. Wire up `ProgressiveBosses.java` (Phase 10)
12. Copy resources (Phase 11)
13. Run game and test: crystal respawn, dragon levels 0–4, anger mechanic, blast attack, acid balls, minions

Enhancements #5, #6, #7, #8 are best tackled after the port is stable.

---

## Known Risks / Things to Verify

- **`EnderDragon.lootTable` field type** — changed to `Optional<ResourceKey<LootTable>>`; affects `LootComponent` field type and the mixin assignment; verify `dropFromLootTable` signature
- **`EnderDragonPhase.create()` still exists** — verify via Parchment mappings
- **`EndDragonFightAccessor` fields** — `previouslyKilled` and `portalLocation` AT names must match Parchment
- **`Explosion` fields** (`radius`, `fire`) — AT entries needed; verify Parchment names
- **Shulker `setRawPeekAmount`** — method name may have changed
- **`unlimitedLastHurtByPlayer`** in `EnderDragonMixin` — NeoForge-added field (`@Shadow(remap=false)`); verify NeoForge 1.21.1 still adds it
- **`CorruptedEndCrystalRenderer`** — `VertexConsumer.endVertex()` removed in 1.21.2+; confirm target is exactly 1.21.1
- **`endergetic` mod** — check if it has a 1.21.1 build before porting `EndDragonFightPreventSpawnEndergeticMixin`
- **`growlTime` field** in `AngerComponent.tickClient()` — private field; may need AT entry
