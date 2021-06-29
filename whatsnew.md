# What's New in Progressive Bosses 3

If you're new to the mod, I recommend getting a look at this: https://www.insane96.eu/projects/mods/progressivebosses/ before reading this page.

## Base mod
* Mod now requires InsaneLib. This means that the mod is now split in Modules (Wither and Dragon) and Features that can be disabled separately.
* Reworked the command `/progressivebosses`, you can now summon "mod's entities".

## Wither
* Difficulty Feature
    * **It's the base feature of the mod, with this disabled almost every Wither feature will not work.**
    * Allows the Wither to get the difficulty on spawn.
    * Also has a new config option to set the starting difficulty.
* Misc Feature
    * **It's responsible for some small features.**
    * The Wither's spawn explosion power has been increased (+0.08 explosion power per difficulty -> +0.3 explosion power per difficulty).
    * The Wither will now break blocks faster and even below and above him. This removes the "prevent getting stuck" feature since now he always breaks blocks below him.
    * Explosion now generates fire from lower difficulty (at 10 difficulty -> at 8 difficulty).
* Health Feature
    * **Takes care of the Wither's health**
    * Max Bonus Health regeneration has been increased. (1 hp/s -> 2 hp/s)
* Resistances and Weakneses Feature
    * **Repalces the Armor feature and adds a new way to deal more damage to the Wither.**
    * Wither now has 1% damage resistance per difficulty when he's above half health (up to 15%), increased to 2% (up to 40%) when he drops below half health.
    * Wither now takes more magic damage based off his missing Health. Every 200 hp missing the magic damage is amplified by 100%.
* Rewards Feature
    * **Makes the Wither drop more experience and Nether Star Shards.**
    * Bonus experience has been increased massively! (+10% experience per difficulty -> +50% experience per difficulty).
    * 16% chance to drop a shard up to the current difficulty, minimum 16% of difficulty. So at 5 difficulty you have 16% chance to drop a shard + 16% chance to drop a shard + 16% chance to drop a shard + and so on, up to 5 times.
* Minions Feature
    * **Let the Wither get some help**
    * Minions AI's no longer lost when they reload (on death or dimension change). Also, Minions will no longer attack the Wither in any case.
    * Health is no longer randomized, so it's now 20 hp as normal Wither Skellys.
    * Movement Speed bonus no longer replaces the base value and is now configurable. Also reduced the movement speed bonus (+1% speed per difficulty -> +0.4% speed per difficulty).
    * Minions now see the player from farther away. (16 blocks -> 32 blocks)
    * Minions now can swim and do it 4 times as fast.
    * Now wield a Stone Sword. Also have 60% chance to replace the sword with a bow when Wither's over half health and 8% chance when below half health. There's a 6.25% chance per difficulty to get a Sharpness / Power enchantment on equipment. There's also a 4% chance per difficulty to get Knockback / Punch on equipment. Every 100% chance adds one guaranteed level of the enchantment, while the remaining chance dictates if one more level will be added.
    * Now drop normal Wither Skeletons experience instead of 1 xp
    * Cooldown is no longer decreased by difficulty, instead now is decreased by 40% when the Wither drops below half health. Also, the Min and Max cooldown have been increased. (10-20 seconds -> 12-24 seconds)
    * Increased the max number of minions that can be near a Wither before he stops spawning them (16 -> 20). Also, the range to check for max minions around has been halved. (32 blocks -> 16 blocks)
    * Reduced the max amount of Minions spawned at once. (8 -> 6)
* Brand NEW Attack Feature
    * **Overhauls the attacks of the Wither**
    * Wither will no longer follow the player unless it's not in range (so the Wither will no longer try to stand on the head of the player ...).
    * Wither's middle head will now fire skulls up to 75% faster when the target is near him.
    * Brand new Charge Attack: When the Wither takes damage there's 1.5% chance (doubled when the Wither's below half health) to start the charge attack. The Wither will enter an invincible status where after 3 seconds will target the nearest player and charges to him dealing massive damage and knocking back everyone in its path. During the whole charge phase the Wither regens 1% of missing health per second.
    * New Barrage attack: when the Wither takes damage there's a 0.2% chance (max 5%, both doubled when the Wither's below half health) per difficulty to trigger the barrage attack. The Wither will start shoting skulls to random targets nearby at the rate of 10 per second for 2.5 seconds (duration doubled when below half health).
    * Wither Skulls now deal 2% more damage per difficulty and travel 2.5 times as fast.
    * There's a 10% chance when attacking to shot 3 low precision skulls instead of 1.
  
## Dragon
* Difficulty Feature
    * **It's the base feature of the mod, with this disabled almost every Dragon feature will not work.**
    * Allows the Dragon to get the difficulty on spawn.
    * Also has a new config option to set the starting difficulty.
* Health Feature
    * **Takes care of the Dragon's health**
    * Increased Max Health regeneration. (0.5 -> 1.0)
* Brand NEW Attack Feature
    * **Overhauls the attacks of the Dragon**
    * Reduced Dragon bonus attack damage per difficulty. (+10% damage per difficulty -> +4% damage per difficulty)
    * Reduced Dragon bonus damage from acid. (+10% damage per difficulty -> +3.3% damage per difficulty)
    * The Ender Dragon should no longer miss the player when charging. Also dives faster, instead of turning continuously.
    * Fireballing and charging now have a chance to trigger when the Ender Dragon's ending her Holding Pattern, Charging Phase or Fireballing Phase instead of every tick. Chance increased. (1.5% -> 45%)
    * Dragon can also fire up to 1 more fireball every 5 difficulty. SHOTGUN
    * Ender Dragon's fireballs now deal magic damage on impact in a spherical area of 4.5 blocks and spawn a 3D Area of Effect Cloud. The cloud has halved duration. Fireballs are also much faster.
* Minions Feature
    * **Let the Dragon get some help**
    * Will no longer spawn if there are no players on the main island.
    * Changed cooldown reduction. (-10 ticks per difficulty -> -0.5% per difficulty)
    * Shoot slower. (1 bullet every 2-11 seconds -> 1 bullet every 4-22 seconds)
    * No longer take damage from the Ender Dragon.
    * 0.3% chance per difficulty to spawn as a Bliding Minion. Blinding Minions's bullets will give blindness in addition to levitation.
    * Can now spawn on towers.
* Larvae Feature
    * **Small annoying pests**
    * Cooldown reduced. (52.5-60 secs -> 30-60 secs)
    * Increased Max Larva spawned. (6 -> 8)
    * Removed Cooldown Reduction.
    * No longer spawn inside blocks.
    * No longer take damage from the Ender Dragon.
* Rewards Feature
    * **Makes the Dragon drop more experience.**
    * Increased Bonus Experience. (+10% per difficulty -> +30% per difficulty)
    * Removed experience per player, the ender dragon will now drop, like vanilla, 12k the first time, otherwise 500 + the bonus experience.
    * Can now have custom rewards difficulty based (like the Wither).
* Vulnerabilities and Resistances Feature
    * **Repalces the Armor feature and adds a new way to deal more damage to the Dragon.**
    * +80% melee damage and +55% indirect damage from players when not in the center podium. This is done to let the player deal significant damage to the Dragon when she charges him, and to be able to deal some damage with arrows since its almost impossible to hit her head while she's flying.
    * -0.3% melee damage per difficulty when in the center podium.
    * -50% explosion damage.
* Crystals Feature
    * **More Cages, More Crystals and respawning Crystals**
    * Below 20% health the Ender Dragon has a small chance each time she's hit to trigger a crystal respawning phase. During this phase the Ender Dragon will fly over towers and sacrifice 10 health (5 due to Vulnerabilities and Resistances module) to respawn crystals. Higher difficulties mean more crystals respawned, higher chance for the crystals to be spawned inside towers and faster respawning. This phase can only happen once per Ender Dragon.
    * Maximum 4 bonus cages can now generate and maximum 5 bonus crystals instead of all crystals with cages and all towers with crystals inside.
    * Crystals no longer always spawn 16 blocks below the top of the tower, instead between 12 and 24 blocks below.
    * Crystals are now immune to explosions.
    * Leaving and reentering The End no longer respawns the cages / more crystals again.