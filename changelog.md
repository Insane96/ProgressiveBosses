# Changelog
## Alpha 3.0.7
* Wither
    * Resistances & Vulnerabilities
        * Added damage reduction before half health: 1% per difficulty up to 20%.
        * Decreased damage reduction on half health (4% -> 2%)
        * Increased max damage reduction on half health (35% -> 40%)
    * Misc
        * Added a config option to prevent the spawning of the Wither outside The Nether (or on the Nether roof)
    * Attack
        * Removed chance for Wither III. Wither 3 almost completely prevented knockback since dealt damage continuously
        * Wither will try to not get stuck on the ceiling of caves
    * Rewards
        * Nether Star Shards drop amount are now guaranteed to be at least 16% of Wither's Difficulty
    * Minions
        * Added 4% chance per difficulty for Wither Minions to have Punch / knockback enchanted equipment
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