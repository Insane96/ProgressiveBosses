# Changelog
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