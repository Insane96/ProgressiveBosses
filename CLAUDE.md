# ProgressiveBosses — 1.21.1

NeoForge mod that allows to summon stronger versions of the Wither and Ender Dragon, plus makes the Elder Guardian fights slightly harder. Three modules: Ender Dragon, Wither, Elder Guardian.

**Status**: Fully ported from 1.20.1 (`C:\Users\delvi\source\repos\Insane96\ProgressiveBosses_1.20.1`); all three modules (Ender Dragon, Wither, Elder Guardian) are implemented. Now in playtesting/bugfixing.

## Key facts

- **Loader**: NeoForge (was MinecraftForge in 1.20.1)
- **Java**: 21
- **InsaneLib**: custom library by the same author; jar on Modrinth — check `gradle.properties` for version, code in `C:\Users\delvi\source\repos\InsaneLib`
- **MixinExtras**: 0.5.3 (jarJar'd)
- **Mixin config**: `src/main/resources/progressivebosses.mixins.json` (NeoForge embeds Mixin — no Gradle plugin needed)

## Project layout

```
src/main/java/insane96mcp/progressivebosses/
  ProgressiveBosses.java          — mod entry point, wires up all registries/events
  module/
    PBModules.java                — registers the three modules with InsaneLib
    dragon/                       — Ender Dragon module: component-based (DragonComponent/DragonDefinition/ComponentRegistry),
                                     JSON-driven per-level definitions, custom phases, corrupted end crystal
    wither/                       — Wither module: custom PBWither entity/renderer, minions, corrupted soul sand,
                                     fixed-field stat classes (WitherStats etc.)
    elderguardian/                — Elder Guardian module: fixed-field stat class (ElderGuardianStats)
  mixin/                          — ~22 core mixins + accessor/ + client/ subpackages (see mixins.json)
  loot/                           — custom loot conditions/functions (LvlCondition, RandomChanceWithLvlCondition, SetCountPerLvl)
  network/                        — CustomPacketPayload records (BeginBlastAttackPhase, SyncDragonAnger)
  setup/                          — DeferredRegister holders (PBBlocks, PBEntities, PBItems, PBLoot, ClientSetup, Strings)
  data/, event/, commands/, duck/, utils/
src/main/resources/
  progressivebosses.mixins.json   — mixin registry (fully populated)
  data/progressivebosses/         — dragon/wither/elder_guardian level-definition datapacks, loot tables, recipes, tags
  integrated_packs/               — compat packs for other mods (e.g. endergetic_integration)
```

Note: the Wither and Elder Guardian modules use fixed-field stat classes, NOT the component system. Only the dragon module uses the JSON component architecture (see `component_system_enhancements.md` in repo root for known limitations/ideas — not yet acted on).

## InsaneLib patterns

### Module/Feature system

The 1.21.1 insanelib does use `@LoadFeature`. Modules are created via `Module.Builder.create(...)` in `PBModules.java`. Features register their own event listeners by receiving the `IEventBus` at construction.

### ModNBTData

**Always use `ModNBTData` for per-entity persistent storage** — never call `entity.getPersistentData().getX(...)` directly. Keys are `ResourceLocation` constants (usually created with `this.createDataKey("name")` on the Feature, or `ProgressiveBosses.id("name")`).

```java
private static ResourceLocation MY_KEY; // set in Feature.init()
MY_KEY = this.createDataKey("my_key");

// Write
ModNBTData.put(entity, MY_KEY, value);
// Read
float v = ModNBTData.get(entity, MY_KEY, Float.class);
// Check
boolean has = ModNBTData.contains(entity, MY_KEY);
```

## NeoForge migration notes (vs 1.20.1)

- **Events**: `net.minecraftforge.event.*` → `net.neoforged.neoforge.event.*`
- **`@SubscribeEvent`**: now from `net.neoforged.bus.api.SubscribeEvent`
- **`@OnlyIn`**: now from `net.neoforged.api.distmarker`
- **`DeferredRegister`**: use `DeferredRegister.createItems(MOD_ID)` etc.; `RegistryObject` → `DeferredHolder` / `DeferredItem`
- **`ForgeRegistries`**: → `BuiltInRegistries` for vanilla lookups
- **`AttributeModifier`**: no longer takes UUID — takes `ResourceLocation` as identifier
- **Network**: `SimpleChannel` is gone; use `CustomPacketPayload` records + `PayloadTypeRegistry`
- **Custom events**: extend `net.neoforged.bus.api.Event` (not Forge's `LivingEvent`)
- **Access Transformer**: uses deobfuscated (Parchment) names, not SRG (`f_xxxxx_`)
- **Mixin `@Shadow` field names**: use Parchment names
- **`@At(INVOKE)` targets in mixins**: method descriptors use deobfuscated names

## 1.20.1 source reference

The full 1.20.1 implementation is at `C:\Users\delvi\source\repos\Insane96\ProgressiveBosses_1.20.1`. When porting a file, open the 1.20.1 version as reference and adapt — don't copy blindly, all Forge-specific imports and patterns must be replaced.

## Running the mod

```
gradlew runClient   # launch game client
gradlew runServer   # launch dedicated server (headless)
```
