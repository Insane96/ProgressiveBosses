# What's New in Progressive Bosses 3

If you're new to the mod, I recommend getting a look at this: https://www.insane96.eu/projects/mods/progressivebosses/ before reading this page.

## Base mod
* Mod now requires InsaneLib. This means that the mod is now split in Modules (Wither and Dragon) and Features that can be disabled separately.
* Reworked the command `/progressivebosses`, you can now summon "mod's entities".

## Wither
* Difficulty Feature
    * **It's the base feature of the mod, with this disabled every Wither feature will not work.**
    * Allows the Wither to get the difficulty on spawn.
    * Also has a new config option to set the starting difficulty.
* Misc Feature
    * It's responsible for some small features.
    * The Wither's spawn explosion power has been increased (+0.08 explosion power per difficulty -> +0.3 explosion power per difficulty).
    * Explosion now generates fire from lower difficulty (at 10 difficulty -> at 8 difficulty).
* Health Feature
    * **Takes care of the Wither's health**
    * Max Bonus Health regeneration has been increased. (1 hp/s -> 2 hp/s)
* Resistances and Weakneses Feature
    * Repalces the Armor feature and adds a new way to deal more damage to the Wither
    * Wither now has 4% damage resistance per difficulty when he drops below half health. Maximum 35% damage resistance.
    * Wither now takes more magic damage based off his missing Health. Every 200 hp missing the magic damage is amplified by 100%.
* Rewards Feature
    * **Makes the Wither drop more experience and Nether Star Shards.**
    * Bonus experience has been doubled! (10% bonus experience per difficulty -> 20% bonus experience per difficulty).
    * 16% chance to drop a shard up to the current difficulty. So at 5 difficulty you have 16% chance to drop a shard + 16% chance to drop a shard + 16% chance to drop a shard + and so on, up to 5 times.
* Minions Feature
    * **Let the Wither get some help**
    * Minions AI's no longer lost when they reload (on death or dimension change). Also, Minions will no longer attack the Wither in any case.
    * Health is no longer randomized, so it's now 20 hp as normal Wither Skellys.
    * Movement Speed bonus no longer replaces the base value and is now configurable. Also reduced the movement speed bonus (+1% speed per difficulty -> +0.4% speed per difficulty).
    * Minions now see the player from farther away. (16 blocks -> 32 blocks)
    * Minions now swim 4 times as fast.
    * Now wield a Stone Sword. Also have 60% chance to replace the sword with a bow when Wither's over half health and 8% chance when below half health. There's a 6.25% chance per difficulty to get a Sharpness / Power enchantment on equipment. Every 100% chance adds one guaranteed level of the enchantment, while the remaining chance dictates if one more level will be added.
    * Cooldown is no longer decreased by difficulty, instead now is decreased by 40% when the Wither drops below half health. Also, the Min and Max cooldown have been increased. (10-20 seconds -> 12-24 seconds)
    * Increased the max number of minions that can be near a Wither before he stops spawning them (16 -> 20). Also, the range to check for max minions around has been halved. (32 blocks -> 16 blocks)
    * Reduced the max amount of Minions spawned at once. (8 -> 6)
* Brand NEW Attack Feature
    * **Overhauls the attacks of the wither**
    * Wither will no longer follow the player unless it's not in range (so the Wither will no longer try to stand on the head of the player ...).
    * Wither attacks twice as fast with the Middle Head when below half health.
    * Brand new Charge Attack: when at 20% health the Wither will enter an invincible status for 8.5 seconds where after 5 seconds will target a player and after 2 seconds will charge to him destroying everything in its path and exploding at arrival.
    * New Barrage attack: when the Wither takes damage there's a 0.2% chance (max 5%, both doubled when the Wither's below half health) per difficulty to trigger the barrage attack. The Wither will start shoting skulls to random targets nearby at the rate of 10 per second for 2.5 seconds (duration doubled when below half health).
    * Wither Skulls now deal 2% more damage per difficulty and travel 2.5 times as fast. Also, there's 1% chance per difficulty for the Skulls to inflict Wither III instead of II.
    * Withers now attack faster 33.3% faster when below half health, and will attack even faster (up to 30% faster) when the target is near him.
    * There's a 10% chance when attacking to shot 3 skulls instead of 1.