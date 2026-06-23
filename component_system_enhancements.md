# Component System Enhancement Ideas

## Simple

### 1. Validation on load
The `DragonComponent` interface has no `validate()` hook. After deserialization, components could check their own fields (negative health, zero-chance events, etc.) and log meaningful errors instead of silently misbehaving in-game.

### 2. Formalize component NBT ownership
Components use `dragon.getPersistentData()` with ad-hoc string keys scattered across classes (e.g. `LAST_BLAST_TAG`, `DRAGON_MINION_COOLDOWN`). Adding `saveNBT(CompoundTag)` / `loadNBT(CompoundTag)` to the `DragonComponent` interface would make each component own a sub-tag, preventing key collisions and making state transparent.

### 3. Consistent priority ordering
`PhaseChanger` already has `getPriority()`, but the base `DragonComponent` doesn't. The `tick()`/`apply()` dispatch in `DragonDefinition` runs components in insertion order. Making ordering explicit (and sorting the list on load) would make behavior predictable.

---

## Medium

### 4. Self-registering components via annotation
`ComponentRegistry.init()` is a hand-maintained list. A `@RegisterComponent("progressivebosses:blast_attack")` annotation + classpath scan (or a `ServiceLoader`) would mean adding a new component class auto-registers it, with no manual wiring.

### 5. Richer `DragonValue`
`DragonValue` only has `base` / `angered`. A natural extension is per-level scaling or formula-based expressions (e.g. `"base": 200, "per_level": 50`), so you don't need separate JSON files per level — you define a scaling curve once. This would let the JSON be much more compact.

### 6. Per-component JSON conditions
Borrowing from vanilla loot tables, each component entry in JSON could carry a `"conditions": [...]` block that gates whether the component is even active at runtime (e.g. only when player count > 2, only in a specific biome, only at night). Right now that kind of logic must be baked into the component's `tick()`/`apply()` code.

### 7. Forge registry for component types
Replace the `HashMap<ResourceLocation, Type>` in `ComponentRegistry` with a proper `DeferredRegister` + Forge registry. This opens the system to addon mods that want to add new component types without touching your code — they just `registerComponent()` in their own mod init.

---

## Large / Rewrites

### 8. Unify all three bosses under one component system
Currently the component system is dragon-only. The Wither and Elder Guardian use separate fixed-field stat classes (`WitherStats`, `ElderGuardianStats`). Abstracting a generic `BossComponent<T extends LivingEntity>` base and migrating all three bosses would centralize the data architecture, allow reusing components across bosses (e.g. a generic `HealthComponent` or `MinionComponent`), and make new bosses trivial to add.

### 9. Codec-based serialization
Replacing the patchwork of Gson `@JsonAdapter` inner classes (one per component) with Minecraft's `Codec` system would let you define serialization once and get JSON + NBT for free. It also means component data could be synced to the client in packets using the same codec, rather than maintaining separate packet classes.

### 10. Datapack inheritance / merging
The reload listener currently overwrites on duplicate levels. A proper datapack system would support the vanilla `"replace": false` pattern and field-level merging, so addon packs could add components to an existing level definition without replacing the whole file.
