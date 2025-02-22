# Changelog

* Sync dragon anger with players joining

## Alpha 4.3.1
* Reduced acid balls shot and slowed down the series of shots
* Dragon now takes off after blasting
* Fixed Blast attack damaging the ender dragon

## Alpha 4.3.0
Please note that this is an alpha because customization of the dragon is not yet final, many fields will be changed and some will be removed and many added.  
Also the dragon difficulty must be fine-tuned.

* Data packable Ender Dragon
  * Reworked the whole fight
    * Dragon now has two levels: base and corrupted
      * The base ender dragon is much stronger, requiring full diamond/netherite armor. Drops an enchanted golden apple
      * Corrupted instead requires full netherite armor + beacon (and shield/totem of undying). Drops 4 enchanted golden apple
      * Corrupted can be summoned with 4 corrupted end crystals, crafted with an end crystal and a dragon egg
    * Dragon now always drops an egg
  * Added a new item tag `progressivebosses:dragon_invulnerable` making items in this tag invulnerable to dragon damage
  * Fixed Crystals destroyed on dragon respawning leaving behind fire
  * Fixed Vanilla bugs
    * Fixed [MC-272431](https://bugs.mojang.com/browse/MC-272431)
      * This makes the ender dragon fly up and down faster, like she used to do back in 1.12
    * Fixed [MC-86836](https://bugs.mojang.com/browse/MC-86836)
      * This makes the ender dragon no longer teleport when inside portal blocks
    * Fixed [MC-105770](https://bugs.mojang.com/browse/MC-105770)
      * Fixes players accumulating knockback when hit by the dragon and then launching like a rocket
    * Fixed [MC-169167](https://bugs.mojang.com/browse/MC-169167)
      * Dragon attack on an entity has a 10 ticks cooldown
    * Fixed Ender Dragon growl sound playing 20 times / sec (making it really loud) when about to respawn and when at the center breathing (it's now 4 times / sec)
    * Fixed Ender Dragon growl sound being played in a too small radius making it inaudible from ground level
    * Fixed head and neck placements and head size
* Wither
  * Charge Attack now ignores armor
  * Ultimate wither charge attack damage slightly increased (12 -> 13)
  * Non-Ultimate wither's charge damage has been reduced (8 -> 6.5)
* Enhanced a bit the wither's death explosion
* You can now disable one of the default levels by setting the corresponding JSON to `{}`
* Fixed wither not dropping items from default loot table

## Beta 4.2.1
* Requires InsaneLib 1.15.0
* Wither and Wither Minions are now affected by IguanaTweaks' Smite and Water Coolant enchantments
* Minor rebalance to charge when unseen
* Wither charge no longer pushes players in creative not flying

## Alpha 4.2.0
* [Wither now has a death animation when dying](https://youtube.com/shorts/J8IGIIoF9O4)
  * Blows up before disappearing
  * Configurable with a new `death` object in the wither json
* Wither no longer summons minions while charging
* Netherite blocks are now Wither Immune
* Nether Stars (and shards) can no longer be destroyed in-world (e.g. lava, cactus, etc)
  * Added a new item tag `progressivebosses:world_immune`
* Buffed wither drops
  * Base wither drops 200 -> 500 xp
  * Armored wither drops ~~2 stars + 2 shards~~ -> 3 stars and 600 -> 1,500 xp
  * Summoner wither drops ~~3 stars + 3 shards~~ -> 5 stars and 1,000 -> 3,000 xp
  * Ultimate wither drops ~~5 stars~~ -> 8 stars and 2,000 -> 8,000 xp
* Ultimate Wither barrage is now slightly less inaccurate
  * Also made barrage `inaccuracy` configurable
* Goodbye `resistances` welcome `attribute_modifiers`
  * Contains two objects `above_half_health` and `below_half_health`, being a list of attribute modifiers
* Fixed charging to get unstuck not working properly if there are players nearby
* Wither charge no longer pushes players in creative

## Alpha 4.1.14
* Wither attacks now go on cooldown when barrage finishes (`attack_cooldown_on_end`)
* Wither charge knockback is now reduced by knockback resistance attribute
* Barrage attack no longer stops when can't see the target
* Renamed barrage `chance` to `chance_on_hit`
* Reworked charge object
  * Now contains `on_hit`, `second_phase` and `target_unseen`. All contain `damage` and `time_to_charge`.
  * `on_hit` contains `chance`. If present the wither has a chance to charge when attacked equal to `chance` multiplied by 10% of the damage received.
  * `second_phase` contains `times`, `tick_reduction`, `max_reduction`, `barrage` and `minion`. If present the Wither will charge `times` number of times in succession. `tick_reduction` reduces the time between each charge. `max_reduction` is the maximum reduction between each charge. If true, `barrage` will make the wither start a barrage attack as soon as the charges end. `minion` will make the wither summon minions as soon as the charges end.
  * `target_unseen` contains `seconds_unseen`, `chance_per_second`, `max_chance`. `seconds_unseen` is how much time until the wither has a chance to charge. `chance_per_second` is how much chance multiplied by seconds passed has the wither to charge each second.

## Alpha 4.1.13
* Increased damage taken by wither from blue skulls
  * Damage from blue skulls is multiplied by 0.8% -> 1% of its max health
* Renamed default wither stats names in data pack
* Base Wither now charges once when entering second phase, 3 times for the armored wither and 4 for the ultimate wither 
* Charge Object
  * Added `second_phase` in Wither's charge object. If true, the wither will charge 3 times in rapid succession
  * Added `second_phase_times`, `second_phase_tick_reduction` and `second_phase_max_reduction` to configure the second phase charging times, tick reduction and max reduction
  * Renamed `max_chance` to `chance_on_hit`
  * Renamed `time` to `base_time`

## Alpha 4.1.12
* Fixed wither minions dropping wither roses
  * Wither skeletons also no longer drop wither roses
* Fixed Wither Minions dropping no xp (now drop 2)

## Alpha 4.1.11
* Rebalanced Summoner Wither
  * Side heads now attack faster
  * Health is now the same as other withers (1000 -> 500)
  * Heals much more on kill and passively
  * Barrage attack speed is faster
  * Minions are summoned more often
* Increased Wither Minions base damage (1 -> 3)
* Wither now summons minions (if can) when finishes the 3 charges when brought below half health 
* Reduced bow chance for wither minions
* Fixed Wither roses not dropping
* Fixed Wither immune to instant health / damage arrows
* Fixed Wither Minions not spawning with correct equipment

## Alpha 4.1.10
* Wither charging chance is no longer based off missing health, instead the charge-up time is lower when lower health 
  * Increased time to charge from 1.5s to 2s. At 0 hp he can charge in 1s
* When brought to half health Wither now charges 3 times in rapid succession
* Minor rebalance to some withers
* Increased Ultimate Wither experience
* Wither now charges random players in range instead of the closest one
* Wither's blue skull chance is now lower when below half health
* Slight buff to wither drops
* Fixed Wither not healing above half health
* More fixes

## Alpha 4.1.9
* Fixed wither bobbing up and down when below half health
* Fixed targeting, again

## Alpha 4.1.8
* Wither
  * Fixed wither skull not being tagged as a projectile thus ignoring projectile protection
  * Fixed ignoring charging cooldown
  * Fixed side heads ignoring the target if too far away
  * Fixed not being in the `forge:bosses` tag

## Alpha 4.1.7
* Wither level changes (changed their names, no longer I, II, III, IV)
  * All Withers: Withers now attack slower when player's close and faster when player's away
  * Wither: Increased Skull damage and skull speed. Heavily increased barrage chance, duration and attack speed. Increased health
  * Armored Wither: Now searches for targets to kill when below 40% health to heal. Increased Health
  * Summoner Wither effect duration reduced, increased health threshold when searching for targets to heal. Increased minions spawned and their health.
  * Ultimate Wither: Increased skull damage and health threshold when searching for targets to heal. Heavily increased barrage chance. Increased health. Reduced healing and healing on kill
* Increased charge cooldown (0.5 -> 1.5 seconds)
* Wither is now tagged in the `forge:bosses` entity type tag
* Fixed withers not barraging when no players nearby
* Fixed side heads searching for targets too far away

## Alpha 4.1.6
* Minor rebalancements to wither levels
* Added info to corrupted soul sand
* Renamed `side_heads_attack_speed_divider` to `side_heads_attack_speed_multiplier`
* Fixed Wither always charging away

## Alpha 4.1.5
* Reworked Levels, they are now different one from another, instead of being just stronger
  * All the withers' skull and charge damage is now the same for all the withers (8 and 16)
  * Wither I: no charging minions. Also has no resistances. Reduced xp drop from 250 to 200
  * Wither II: no barrages or minions. Charge happens more often. Wither effect is II but lasts less
  * Wither III: no resistances, no charging. Minions spawn often and are stronger
  * Wither IV: a beast of boss, only 300HP but with high resistance and charges continuously. Let's not talk about the minions
* Rewards
  * Wither IV now drops 6 nether stars instead of 5, 16 ancient debris instead of 8
  * Wither III now drops 8 ancient debris instead of 4
* Blue skulls damage against the wither now is multiplied by 0.5% of wither's max health
* When wither's below `attack_to_heal_threshold` he now charges away from players
* Barrage attacks are now slightly more precise
* Wither minions are now immune to fall damage
* Barrage attack now has higher chance to happen when wither's above half health

## Alpha 4.1.4
* Wither no longer barrages if there are players nearby
* Increased chance to charge below half health
* Wither now tries to barrage after blowing up, with double the chance
* Halved charge knockback
* Changed wither loot
  * First wither only drops a nether star
  * Wither II drops 2 nether star, 2 nether star shards and a Netherite Smithing upgrade template
  * Wither III drops 3 nether star, 3 nether star shards, 4 Ancient Debris and 2 Netherite Smithing upgrade template
  * Wither IV drops 5 nether star, 8 Ancient Debris and 4 Netherite Smithing upgrade template
* Fixed wither no longer blowing up on charge when players nearby

## Alpha 4.1.3
* Now requires InsaneLib 1.13.4
* Ported stuff from 3.9.3 to 3.9.7
  * Fixed first dragon dropping no experience
  * Items in the `progressivebosses:wither_invulnerable` item tag are now invulnerable to wither damage (not only for 5 seconds from spawning)
    * Nether Star Shards have been added to above tag, instead of being hardcoded
  * Fixed possible game freeze with Elder Guardians (most notably with Bumblezone)
  * Fixed Elder Guardian Health Feature not working
  * Better Compatibility with YUNG's better end island
    * Cages now use the vanilla method to generate so YUNG's one now generate correctly
    * Fixed Crystals spawning in towers too close to normal crystals

## Alpha 4.1.2
* Wither
  * Slight rework to charge attack
    * The target is now decided as soon as the charge attack starts, instead of when the wither charges
    * Now takes half the time to charge
    * No longer takes half the time to blow up if there are players nearby
    * Charge bounding box slightly increased (both when blowing up and when charging)
    * The wither should no longer freak out when close the point where he's charging
  * Added `heal_on_skull_kill` that defines how much health the wither regenerates when a skull kills an entity
    * 5/7/9/10 healing at I to IV. Was 10
  * Minion health is now configurable
    * 10/12/14/15 health at I to IV. Was 15
  * Wither effect duration is now configurable
    * Also reduced in hard difficulty from 40s to 20s and increased in easy from 0s to 10s
  * Reduced the explosion radius of skulls (1 -> 0.75 and 2 -> 1.5 for blue skulls)
  * Sped up barrage at Wither II
  * Increased middle head attack range (24 -> 32)
  * Slowed down wither minion firing speed
  * Renamed `resistances_weaknesses` to `resistances` in Wither's json

## Alpha 4.1.1
* Wither
  * When he reaches half health and charges, after charging minions will spawn
  * Can no longer heal back past half health, even if kills someone
* Adventure mode range is now doubled when YUNG's Better Ocean Monuments is installed
* Elder Guardian no longer spawn minions if any 6 guardians are nearby, not only minions
* Removed Mining Fatigue from Ocean Monuments if Adventure mode is enabled

## Alpha 4.1.0
* Elder Guardian
  * Now data packable!
  * Now heal by 5 hp when the laser hits
    * Removed passive regen
  * Changed attack speed (3/1.75/0.5 -> 2.5/1.75/0.75 seconds to laser)
  * Reduced damage reduction
  * Reduced minion cooldown
  * Elder Minions now have 50% less health
* Wither
  * Levels are now loaded through data packs instead of the config folder
  * Now targets any creature when below a certain health threshold trying to heal back
  * Heads attack cooldown is now reduced whenever the wither is hit
  * Blue skulls chance has been increased and scales with how many normal skulls have been shot since last blue skulls
  * Now weak to magic damage and always takes 1.5x damage from it
    * This also applies to minions
  * Now heals 10 hp instead of 5 when killing someone with a skull
  * Minions now get near the wither to get sacrificed and let the wither heal
  * Can now drop either Ancient Debris or Netherite Upgrade Templates

## Alpha 4.0.1
* Wither has now a really high chance to charge if there are players nearby when above half health
* Loot
  * Withers no longer drops Corrupted Soul Sand, instead can be crafted with soul sand/soil and nether stars
  * Wither drops 1 nether star per level, plus 2/3/4 shards at levels II/III/IV
  * Wither drops 4 debris per level, plus 2/3/4 at levels II/III/IV

## Alpha 4.0.0 - Wither
* Stats have been heavily adjusted to the new system and features
* Wither now has 4 levels (from 8 difficulties)
  * Each level stat can be configured separately in a `wither.json` file in the config folder
  * Killing a Wither has a chance to drop a corrupted Soul Sand, which can be used in the center of the Wither summoning pattern to summon a higher level wither
* Attacking
  * Blue skulls can be sent back (like a ghast fireball) to hit the Wither
  * Wither side heads now try to target another entity if it's the same as the middle head (so the wither should be able to attack two players at once)
  * Wither side heads now attack twice as slower than the middle head
  * Skulls speed is now slower at lower Levels
  * The first Wither applies Wither I instead of II
  * Charging now makes the wither blow up instead if there are players nearby
  * Barraging now has a charge up animation before starting
* Health
  * Reduced at higher levels
  * When brought to half health no longer goes back up over it with passive healing
* Resistances and Weaknesses
  * Replaced damage reduction with armor and armor toughness
* All the wither past lvl II now ignore wither proof blocks
* Heavily increased xp dropped and loot

## 3.9.7
* Fixed first dragon dropping no experience

## 3.9.6
* Items in the `progressivebosses:wither_invulnerable` item tag are now invulnerable to wither damage (not only for 5 seconds from spawning)
  * Nether Star Shards have been added to above tag, instead of being hardcoded

## 3.9.5
* Fixed possible game freeze with Elder Guardians (most notably with Bumblezone)

## 3.9.4
* Fixed Elder Guardian Health Feature not working

## 3.9.3
* Better Compatibility with YUNG's better end island
  * Cages now use the vanilla method to generate so YUNG's one now generate correctly
  * Fixed Crystals spawning in towers too close to normal crystals

## 3.9.2
* Now requires InsaneLib 1.11.1

## 3.9.1
* Fixed Elder Guardians being able to be invincible if mods spawned more than 4 elder guardians

## 3.9.0
* Updated to MC 1.20.1

## 3.8.3
* Slightly lowered Wither charge chance and barrage duration
* Increased ender dragon healing reduction when hit duration

## 3.8.2
* Nether Star is now crafted from 4 shards instead of 9
* Wither loot
  * Shards changed from (75% to drop 2 shards * difficulty) to (2 shards * difficulty)
  * Ancient debris changed from (75% to drop 2 debris * difficulty) to (2 debris * difficulty)
* Ender Dragon loot
  * Enchanted Golden Apples changed from (10/20/30/40/50% chance to drop on difficulties 4/5/6/7/8) to (12.5% chance per difficulty, up to a guaranteed drop at maximum difficulty)
* Wither loot is now invulnerable to wither damage (Nether Star Shards and Ancient Debris). An item tag `progressivebosses:wither_immune` has been added to add items that for 5 seconds from spawning are immune to wither damage
* Fixed wither causing fire at any difficulty

## 3.8.1
* MC 1.19.4
* Reduced Wither charge attack damage (~~16~~ -> 12)

## 3.8.0
* Updated to 1.19.3

## 3.7.5
* Wither
  * Bonus health regen is now reduced by 40% if Wither took damage in the last 3 seconds
* Dragon
  * Bonus health regen is now reduced by 60% if Dragon took damage in the last 3 seconds

## 3.7.4
* Fixed Mixins overriding some stuff
* Fixed minions enchantment not going over 127 (the config is now capped to that value)

## 3.7.3
* Updated to InsaneLib 1.7.1

## 3.7.2
* Ported 3.6.4 changes
* Updated to InsaneLib 1.7.0
* Ender Dragon
  * Reduced melee damage reduction when at center (~~-45%~~ -> -24% damage)
  * Added bonus melee damage when not at center: +24%
  * Dragon Minions will now no longer spawn on towers
* Increased Elder Guardian adventure mode range and made it configurable (~~32~~ -> 48)

## 3.7.1
* Wither Minions
  * Equipment chances can now be configured separately
    * Reduced Power level (Power ~~IV~~ -> III at max difficulty, ~~80%~~ -> 20% chance to be Power ~~V~~ -> IV)
    * Reduced Punch level (Punch ~~II~~ -> I at max difficulty, ~~40%~~ -> 50% chance to be Punch ~~III~~ -> II)
    * Reduced Sharpness Chance (Sharpness ~~IV~~ -> II at max difficulty, ~~80%~~ -> 40% chance to be Sharpness ~~V~~ -> III)
  * Reduced base damage (~~3~~ -> 1)
  * Reduced base attack knockback
  * Fixed Wither minions not generating with Punch (but with 2 power enchantments)
* Internally changed how Wither drops work

## Beta 3.7.0
* Port to 1.19.2
* Ender Dragon
  * Reduced bonus damage (~~+240%~~ -> +225% at max difficulty)
  * Reduced damage reduction when sitting (~~-60%~~ -> -45% at max difficulty)
  * Dragon now takes more time to respawn a crystal (~~5~~ -> 10 seconds)
  * Takes more bonus damage when respawning crystals (~~+33%~~ -> +50% damage)
* Wither
  * Wither now attacks faster (1 skull every ~~2~~ -> 1.75 seconds) (attack speed when near unchanged 1 skull every 0.7 seconds)
* Fixed generating a bak config file due to wrong explosion config option
 