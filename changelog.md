# Changelog

## Beta 3.1.1
* Dragon
    * Attack
        * The dragon can now charge / fireball the player again when she's just finished charging / fireballing. COMBO BREAKER
    * Larva
        * Increased Cooldown (30-50 secs -> 30-60 seconds)
        * Increased max Larva Spawned (6 -> 8)
    * Minion
        * Increased Blindness duration (5 secs -> 7.5 secs)
        * Increased Attack Speed (5-27.5 secs -> 4-22 secs)
    * Resistances & Vulnerabilities
        * Changed Bonus Damage (+75% damage -> +80% direct damage & +55% indirect damage)
    * Crystals
        * Now have 0.5% chance per difficulty to be respawned inside the tower instead of on top
        * Respawn can now trigger when Dragon's health below 20% instead of 15%

## Alpha 3.1.0
* Dragon port!
    * Health
        * Increased Max Health regeneration (0.5 -> 1.0)
    * Attack
        * Reduced Dragon bonus attack damage per difficulty (+10% damage per difficulty -> +4% damage per difficulty)
        * Reduced Dragon bonus damage from acid (+10% damage per difficulty -> +3.3% damage per difficulty)
        * The Ender Dragon should no longer miss the player when charging. Also dives faster, instead of turning continuously
        * Fireballing has a chance to trigger when the Ender Dragon's ending her Holding Pattern instead of every tick. Chance increased (1.5% -> 45%)
        * Dragon can also fire up to 1 more fireball every 5 difficulty
        * Charging a player has a chance to trigger when the Ender Dragon's ending her Holding Pattern instead of every tick. Chance increased (1.5% -> 45%)
        * Ender Dragon's fireballs now deal magic damage on impact in a spherical area of 4.5 blocks and spawn (faster than vanilla) a 3D Area of Effect Cloud. The cloud has halved duration. Fireballs are also much faster
    * Minions
        * Will no longer spawn if there are no players in the main island
        * Changed cooldown reduction (-10 ticks per difficulty -> -0.5% per difficulty)
        * Shoot slower (1 bullet every 2-11 seconds -> 1 bullet every 5-27.5 seconds)
        * No longer take damage from the Ender Dragon
        * 0.3% chance per difficulty to spawn as a Bliding Minion. Blinding Minions's bullets will give blindness in addition to levitation
        * Can now spawn on towers
    * Larvae
        * Cooldown reduced (52.5-60 secs -> 30-50 secs)
        * Removed Cooldown Reduction
        * No longer spawn inside blocks
        * No longer take damage from the Ender Dragon
    * Rewards
        * Increased Bonus Experience (+10% per difficulty -> +30% per difficulty)
        * Removed experience per player, the ender dragon will now drop, like vanilla, 12k the first time, otherwise 500 + the bonus experience
        * Can now have custom rewards difficulty based (like the Wither)
    * Vulnerabilities and Resistances
        * +75% damage from players when not in the center podium. This is done to let the player deal significant damage to the Dragon when she charges him, and to be able to deal some damage with arrows since its almost impossible to hit her head while she's flying
        * -0.3% melee damage per difficulty when in the center podium
        * -50% explosion damage
    * Crystals
        * Below 15% health the Ender Dragon has a small chance each time she's hit to trigger a crystal respawning phase. During this phase the Ender Dragon will fly over towers and sacrifice 10 health (5 due to Vulnerabilities and Resistances module) to respawn crystals. Higher difficulties mean more crystals respawned and faster respawning. This phase can only happen once per Ender Dragon
        * Maximum 4 bonus cages can now generate and maximum 5 bonus crystals instead of all crystals with cages and all towers with crystals inside.
        * Crystals no longer always spawn 16 blocks below the top of the tower, instead between 12 and 24 blocks below
        * Crystals are now immune to explosions
        * Leaving and reentering The End no longer respawns the cages / more crystals again  
* Wither
    * Hugely increased Bonus Experience (+20% per difficulty -> +50% per difficulty)
    * Cleaned up some things

## Alpha 3.0.7
* Swapped `<get/set/add>` and `<targetPlayer>` in che command (`/progressivebosses difficulty <get/set/add> <targetPlayer>` -> `/progressivebosses difficulty <targetPlayer> <get/set/add>`)
* Wither
    * Resistances & Vulnerabilities
        * Added damage reduction before half health: 1% per difficulty up to 15%.
        * Decreased damage reduction per difficulty on half health (4% -> 2%)
        * Increased max damage reduction on half health (35% -> 36%)
    * Misc
        * Block breaking when hit will now happen faster and will break blocks below Wither's too, this feature makes prevent getting stuck useles, so has been removed
        * Added a config option to prevent the spawning of the Wither outside The Nether (or on the Nether roof). Disabled by default
    * Attack
        * Removed chance for Wither III. Wither 3 almost completely prevented knockback since dealt damage continuously
        * Removed bonus attack speed when on half health
        * Wither's Attack speed is now 0.5 skulls/second
        * Attack Speed when near increased (up to 0.71 skulls/sec -> up to 2 skulls/sec)
        * Charge attack reworked. When the Wither takes damage there's 1.5% chance (doubled when the Wither's below half health) to start the charge attack. The Wither enters an invincible state where after 3 seconds will target the nearest player and charges to him dealing massive damage (scaling with Increased Damage feature) and knocking back everyone in its path. If he can't find a target, will make a small explosion in place. 
        * Reduced health regeneration when charging (2% max health/second -> 1% missing health/second)
        * Items dropped by the charge attack will now despawn after a minute to reduce lag
        * Wither will try to not get stuck on the ceiling of caves
    * Rewards
        * Nether Star Shards drop amount are now guaranteed to be at least 16% of Wither's Difficulty
    * Minions
        * Now drop normal Wither Skeletons experience instead of 1 xp
        * Added 4% chance per difficulty for Wither Minions to have Punch / knockback enchanted equipment
        * Can now swim
        * Wither Minions will no longer despawn

## Alpha 3.0.6
* Fixed charged attack no longer working
* Changed command to use `/progressivebosses difficulty <set/add/get>` instead of `/progressivebosses <set/add/get>`

## Alpha 3.0.5
* Reworked the command `/progressivebosses`, you can now summon "mod's entities"
* Fixed italian translation
* Removed Russian Translation, needs to be remade
* Reduced the wither Invincibility time during charging phase. (10 -> 8.5)

## Alpha 3.0.4
* Added Catalogue and Configured icon and background
* Adjusted configs
* Wither
    * Attack
        * Wither Skulls now have 1% chance per difficulty to inflict Wither III instead of II
        * Increased max barrage chance (8% -> 10%), but halved it when Wither's above half health
        * Barrage attack now is more precise
        * Barrage attack duration is now 2.5 seconds, doubled when the wither's below half health
        * Barrage attack no longer ticks down if the wither doesn't see the target
    * Minions
        * Increased max Minion Around (16 -> 20)

## Alpha 3.0.3
* Wither
    * Attack
        * Added the Barrage attack: everytime the Wither takes damage there's a 0.4% chance per difficulty (max 8%) to trigger a barrage attack, the Wither starts shooting skulls to the target, at the rate of 10 per second for 4 seconds.
        * Wither skulls now deal 2% more damage per difficulty and travel 2.5 times as fast
        * The wither will now attack faster the nearer the target is (up to 30% faster over base attack interval)
        * Attack interval reduction when below half health reduced (50% -> 33.3%)
        * There's now a 10% chance the all the 3 heads will fire a skull when the middle skull attacks
        * The explosion power when landing the charge attack is now 9 instead of scaling with the Stronger Explosion Feature in the Misc Module
        * Charge attack time before charge reduced (7.5 seconds -> 10 seconds)
        * Charge attack target selection before charge increased (1.5 secs -> 2 secs)
    * Rewards
        * Shards drop changed:  
        16% chance to drop a shard, trying as many times as the difficulty. So at 5 difficulty you have 16% chance to drop a shard + 16% chance to drop a shard + 16% chance to drop a shard + and so on, up to 5 times.
    * Minions
        * Minions now have 6.25% chance per difficulty to get a Sharpness / Power enchantment on equipment. Every 100% chance adds one guaranteed level of the enchantment, while the remaining chance dictates if one more level will be added.
        * Minions now have a higher follow range (16 -> 32)
        * Minions now are 4 times as fast at swimming
        * Reduced max spawned (8 -> 6)
        * Reduced Cooldown reduction on half health (50% -> 40%)
        * Reduced Speed Bonus per difficulty (1% -> 0.4%)
        * Wither now checks in a 16 blocks radius for Minions instead of 32 to stop spawning them
        * Fixed bonus movement speed not begin applied

## Alpha 3.0.2
* Wither
    * Rewards
        * Inverted the chances for Shards to drop:  
          `8% chance * (Difficulty - 1)` from Difficulty = 2 to drop 1 Nether Star Shard  
          `6% chance * (Difficulty - 3)` from Difficulty = 4 to drop 2 Nether Star Shard  
          `4% chance * (Difficulty - 5)` from Difficulty = 6 to drop 3 Nether Star Shard  
          `2% chance * (Difficulty - 7)` from Difficulty = 8 to drop 4 Nether Star Shard  
          E.g. At difficulty = 10 you have 72% chance to drop 1 shard + 42% chance to drop 2 shards + 20% chance to drop 3 shards + 6% chance to drop 4 shards.
    * Attack
        * Added a charge attack. At 20% health the wither will enter a still status where after 4.5 secs will target a player and after 1.5 seconds will charge at him, destroying everything in its path and exploding at arrival (or after 1.5 secs of charging if for some reasons can't get to the target point).

## Alpha 3.0.1
* Fixed possible freeze on Wither / Dragon load
* Wither
    * New Attack Feature
        * Wither will no longer follow the player unless it's not in range (so the Wither will no longer try to stand on the head of the player ...)
        * Wither attacks twice as fast with the Middle Head when below half health
        * Fixes https://bugs.mojang.com/browse/MC-29274
    * Minions
        * Now wield a Stone Sword. Have 60% chance to replace the sword with a bow when Wither's over half health and 8% chance when below half health.
        * Min and Max Cooldown decreased (15-30 secs -> 12-24 secs). Cooldown below half health increased (35% -> 50%) (5-10.5 secs -> 6-12 secs)
        * Reduced Max Wither Minions Around (16)
    * Rewards
        * Nether Star Shards now stack up to 32 and are "indestructible" (have 16k hearts)
    * Resistances & Weakneses
        * Changed Bonus Magic Damage: every 200 missing health the damage will be amplified by 100%. `(Magic Damage + 5% of Missing Health -> Magic Damage * (Missing Health / 200 + 1))`
* Dragon
    * Fixed having wrong Difficulty Tag

## Alpha 3.0.0
* Rewritten the mod to be modular. Now requires InsaneLib
    * The mod now works with Modules and Features, both can be easily disabled
* Wither
    * Difficulty
        * Added config to set the starting difficulty
        * Increased Wither Spawn Radius Check (96 -> 128)
            * If no players are found in this radius the nearest player is taken into account instead
    * Misc
        * Increased Explosion Size bonus per difficulty (0.08 -> 0.3). This means that explosion will cap at 20 difficulty (vanilla explosions don't look that good when size > 10). Also explosion is no longer replaced, instead it's just modified.
        * Reduced Explosion Causes Fire at Difficulty (10 -> 8)
        * Added config option to disable the "Anti-stuck" feature.
    * Health
        * Increased Max Bonus Health regeneration (1 -> 2)
    * Replaced "Armor" with "Resistances & Weakneses"
        * Wither now has % damage resistance when drops below half health. 4% per difficulty up to 35%
        * Wither now takes increased Magic Damage, 5% of missing health. E.g. A difficulty = 0 Wither (with 300 max health) is at 1/3 of health (so it's missing 200hp), if he were to take magic damage he will receive 200 * 5% = 10 more damage.
    * Rewards
        * Completely changed the way Nether Star Shards drop.  
          Wall of text warning:  
          `2% chance * (Difficulty - 1)` from Difficulty = 2 to drop 1 Nether Star Shard  
          `4% chance * (Difficulty - 3)` from Difficulty = 4 to drop 2 Nether Star Shard  
          `6% chance * (Difficulty - 5)` from Difficulty = 6 to drop 3 Nether Star Shard  
          `8% chance * (Difficulty - 7)` from Difficulty = 8 to drop 4 Nether Star Shard  
          E.g. At difficulty = 10 you have 18% chance to drop 1 shard + 28% chance to drop 2 shards + 30% chance to drop 3 shards + 24% chance to drop 4 shards.
        * Increased Bonus Experience per difficulty (10% -> 20%)
    * Minions
        * Minions AI's no longer lost when they reload. Also Minions should no longer attack the Wither in any case.
        * Health is no longer randomizzed
        * Movement Speed bonus is now configurable. Still +1% per difficulty. Also now applies as Attribute Modifier and doesn't replace the base Attribute.
        * Increased max minions around (16 -> 20)
        * Increased Min (10 secs -> 15 secs) and Max (20 secs -> 30 secs) Cooldowns.
        * Cooldown is no longer decreased per difficulty, instead now is decreased by 65% when the Wither drops below half health
        * Added a config option to disable the killing of minions on Wither's death
* Dragon
    * Added config to set the starting difficulty
    * Not yet ported
