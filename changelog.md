# Changelog

## Upcoming
* Wither
  * Attack
    * Removed the 3 skull rare attack
* Added advancements for killing a maximum difficulty Wither and Elder Dragon
* Bosses loot is now defined in loot tables (progressivebosses:entities is injected into vanilla loot table)
  * This means that the drops are no longer defined in the config file, the option is gone

## 3.5.8
* Increased chance to summon a Blinding Dragon Minion (~~4%~~ -> 5% per difficulty)
* Reverted Wither Minion cooldown reduction (~~40%~~ -> 50% cooldown reduction on wither half health)
* Increased Wither Minion cooldown (~~20-35~~ -> 20-40 seconds)
* Fixed missing Enchanted Golden Apple drop for Ender Dragon
* Fixed Wither Minions dropping their equipment

## 3.5.7
* Wither
  * Minion
    * Increased cooldown (~~15-30~~ -> 20-35 seconds)
    * Decreased cooldown reduction when Wither's below half health (~~-50%~~ -> -40% cooldown)
    * Reduced max minions around the Wither (~~20~~ -> 18)
  * Health
    * Bonus Health Regen is disabled when health between 49% and 50%
  * Attack
    * Wither no longer charges creative players
  * Rewards
    * Added a new reward. 75% chance per difficulty to drop 2 Ancient Debris
    * Increased shards dropped (~~1~~ -> 2 shards, ~~60%~~ -> 75% chance per difficulty)
* Ender Dragon
  * Rewards
    * Added a new reward. 10%/20%/30%/40% chance to drop an Enchanted Golden Apple at difficulties 5/6/7/8

## 3.5.6
* Now working with InsaneLib 1.4.6
* Reduced crystals respawned (4 -> 3 at max difficulty)
* Fixed Ender Dragon not respawning crystals until she had health < 25%
* Fixed Ender Dragon Bonus health begin too high (30 -> 25 bonus hp per difficulty)

## 3.5.5
* Port to 1.18.2

## 3.5.4
* This version changes most of the config values, so it's advised to delete the old one if you're using the default one
* Ender Dragon
  * Maximum Difficulty is now 8, so the hardest Ender Dragon is at difficulty 8 and no longer 24. All the values have been scaled accordingly to reach the same value as before at maximum difficulty, except the ones listed below. The values mentioned in the changelog are scaled accordingly to the new max difficulty
    * Bonus fireballs per difficulty, crystal respawn per difficuly, blinding minion chance and damage reduction when in the center slightly decreased
    * More cages, more crystals and larvae spawn now start at earlier difficulties and one more crystal every difficulty spawn earlier (still 3 tower crystals at max difficulty)
    * Reduced Bonus Regen per difficulty (0.15 -> 0.125 HP/s). Still max 1 HP/s.
  * Crystals
    * When respawning crystals the Ender Dragon now takes a flat amount of bonus damage (~~+25%~+125% damage based off current health~~ -> +33% damage)
    * If the crystal to respawn is a decimal value (e.g. 0.6) the dragon now takes the decimal part as a chance to spawn a +1 crystal (so 60% chance to respawn 1 and 40% chance to fail the respawn)
  * Health
    * Bonus health per difficulty reduced (~~103~~ -> 25 Health at max difficulty)
    * Removed bonus health regen from crystals (~~0.15 hp/s per difficulty~~ -> REMOVED) (kept config option)
  * Resistances and Vulnerabilities
    * Removed bonus damage when not in center (~~+1% current health as bonus indirect damage~~ -> REMOVED; ~~+3% current health as bonus direct damage~~ -> REMOVED) (removed config option)
    * Greatly increased damage reduction when in the center (~~-3.5%~~ -> -8% damage taken when in the center; at max difficulty: -28% -> -64%)
  * Fixed Ender Dragon respawning crystals even when crystal respawned should have been 0 (e.g. at difficulty 0)
  * Fixed Ender Dragon shooting more than one fireball the first fight
  * Fixed Dragon dying in place if killed while respawning a crystal
  * Fixed log spam when dragon was attached to crystal
* Wither
  * Maximum Difficulty is now 8, so the hardest Wither is at difficulty 8 and no longer 24. All the values have been scaled accordingly to reach the same value as before at maximum difficulty, except the ones listed below. The values mentioned in the changelog are scaled accordingly to the new max difficulty.
    * Slightly increased barrage chance
* Updated things to modern tecnologies (using LivingExperienceDropEvent and onhurt of dragon phase)

## 3.5.3
* Increased InsaneLib version required (~~1.4.2~~ -> 1.4.3)
  * Update InsaneLib to 1.4.4 if you want fewer particles from 3D Area Effect Clouds
* Dragon
  * Minion
    * Blinding Minions bullets now only blind and no longer give levitation too. The bliding duration can also be now configured
  * Crystals
    * Cages and More Crystals now generate starting from smaller towers and no longer randomly
    * Crystals respawned by Dragon now always start from the bigger towers to the smaller ones instead of begin random. (0.2 ~~tries to respawn a crystal~~ crystals respawned per difficulty, rounded down)
    * Changed crystal respawning health threshold (0% chance when health >= ~~50%~~ -> 80%; 100% chance when health <= 20%) (health threshold reduced by ~~20%~~ -> 35% every time the crystals are respawned)
    * Time to respawn a crystal is now fixed (~~50~44 ticks based off difficulty~~ -> 50 ticks)
  * Attack
    * Dragon will no longer fireball the player if too near
  * Resistances
    * Increased Bonus direct damage when not in the center (~~2%~~ -> 3% of current health)
    * Increased bonus indirect damage when not in the center (~~0.9%~~ -> 1% of current health)
  * Health
    * Bonus crystal regen now scales with dragon missing health (+0.05 hp/s (per difficulty) when attached to a crystal [NEW] **when missing 100% health**)

## 3.5.2
* Dragon
  * Crystals
    * No longer respawn in towers
    * Reduced crystals inside towers (~~1 at difficulty 8, 4 at difficulty 11~~ -> 1 at difficulty 8, 2 at difficulty 16, 3 at difficulty 24)
    * Increased bonus cages (~~4~~ -> 6)
    * Reduced number of tries to respawn a crystal (~~0.6~~ -> 0.2 tries per difficulty; ~~14.4~~ -> 4.8 tries at max difficulty) but the dragon can now trigger the respawn phase multiple times. 
      * The health threshold to trigger the respawn has been changed (0% chance when health >= ~~20%~~ -> 50%; 100% chance when health <= ~~5%~~ -> 20%) and reduces by 20% each time (so after triggering one time, the second time the chance is 0% at >= 30% health and 100% at <= 0% health)
    * Cystals left from previous fights are removed on dragon respawn
    * Fixed dragon always having opposite chance to trigger crystal respawning
  * Attack
    * Reduced bonus damage dealt (~~+12.5~~ -> +10% direct damage per difficulty; ~~+11%~~ -> +10% acid damage per difficulty)
    * Halved range where the Ender Dragon would charge and fireball the player (~~128~~ -> 64 blocks from the center of the island)
    * Chaining fireballing is now slightly slower
    * Ender dragon no longer targets creative or spectator players
    * Reduced Charge (~~50%~~ -> 45% at max difficulty) and Fireball (~~40%~~ -> 35% at max difficulty) chance
  * Minions
    * Halved range where a player is required to let Minions **and Larvae** spawn (~~128~~ -> 64 blocks from the center of the island)
    * Fixed Dragon Minion shooting twice as fast
  * Resistances and Vulnerabilities
    * Increased explosion damage reduction (~~-50%~~ -> -66.7 damage)
    * Increased bonus damage when respawning crystals (~~+20%~120%~~ -> +25%~125% damage)
    * Increased bonus indirect damage (~~0.8%~~ -> 0.9% of current health)
  * Rewards
    * Increased bonus experience (~~+100%~~ -> +150% exp per difficulty)

## Beta 3.5.1
* Wither
  * Attack
    * Charge
      * Wither now becomes purple when is in Charge mode
      * Wither is no longer invulnerable when charging
      * Wither no longer explodes if no players are found to charge
      * Items are now dropped when the charge attack ends and are spawned already stacked (like Explosions do since around 1.14)
      * Knockback on charge attack reduced
      * Wither will no longer continuously Charge if it has not seen the player for a while (basically added a cap for the chance)
    * Fixed Wither AI completely broken
  * Rewards
    * Increased bonus experience (+200% -> +250% exp per difficulty)
    * Increased chance to drop a Nether Star Shard (50% -> 60% per shard per difficulty)
    * Nether Stars are now crafted with 9 shards instead of duplicating a Nether Star
  * Minion
    * Reduced Wither Minions Bonus speed per difficulty (+12% -> +10%)
    * Texture changed
  * Misc
    * Fixed Wither Nether Only preventing the spawn of the wither anywhere
* Ender Dragon
  * Resistances and Vulnerabilities
    * Dragon takes more damage (up to +120% based off current health, min +20%) when she's respawning crystals
  * Crystals
    * Dragon no longer sacrifices health when respawns a crystal
  * Larva
    * Is now an actual entity and no longer an Endermite in disguise. It's bigger and black-purplish
    * Increased health (4 -> 6)
    * Are no longer immune to Dragon damage. Instead, they take 10% damage from her.
  * Minion
    * Are no longer immune to Dragon damage. Instead, they take 10% damage from her.
* Fixed crash with InsaneLib 1.4.3

## Alpha 3.5.0
* Updated to Minecraft 1.18.1
* Requires InsaneLib 1.4.2
* Removed bonus damage per missing Elder Guardian (+10% -> +0% attack damage per missing guardian)

## 3.4.1
* Now requires InsaneLib 1.4.0
* Elder Guardian
  * Added Elder Minions
    * Every 10/7/4 seconds (for 0/1/2 missing Elder Guardians), Elder Guardians will spawn Elder Minions (Guardians)
    * Elder Minions swim faster and drop nothing but normal xp
  * Added Rewards
    * Elder Guardians now drop 40 xp instead of 10, increased by 100% per missing Elder Guardian
    * Elder Guardians now drop 2 sponges + 2 sponges per missing Elder Guardian
    * Elder Guardians drop an Elder Guardian Shard. 3 Elder Guardian Shards + 2 prismarine = Trident!
  * Explosions are no longer canceled when near an Elder Guardian. Instead, are set to break no blocks
  * Reduced bonus damage from Elder Guardians (+20% -> +10% per missing guardian)
  * Renamed Adventure feature to Base feature
* Nether Star Shards no longer stack to 32
* Fixed Wither Minions not spawning in spaces smaller than 3 blocks high

## Alpha 3.4.0
* Added Elder Guardian Module!
  * Health Feature 
    * 0.5 health regen per second, 50% more health and 40 absorption hearts
  * Adventure Feature
    * Players approaching Elder Guardians (and thus a Monument) will be set to adventure mode unless they leave the Monument or until they defeat all the Guardians
    * When a Guardian is killed a sound is played to alert the players that the other Elder Guardians will be harder
  * Resistance Feature
    * Elder Guardians gain 30% damage reduction per Elder Guardian defeated
  * Attack feature
    * Elder Guardians deal 20% more damage per Elder Guardian Defeated
    * They're time to charge an attack is reduced by 1.25 seconds per Elder Guardian defeated. In vanilla and the First elder guardian will take 3 seconds to charge the attack and deal damage, followed by 1.75s the second Elder Guardian and 0.5s the last one.
* Wither and Dragon Difficulty now increases by 25% per player past the first one (e.g. with 2 players at 4 difficulty you would get a Dragon / Wither with difficulty 5)
* Dragon
  * Added bonus crystal health regen. 0.05 health/sec per difficulty when the Ender Dragon is attached to a Crystal
  * Chance for the Ender Dragon to trigger a Crystal respawn phase is now given by health left (5% chance when below 20% health -> 0% chance when health >= 20% up to 100% chance when health <= 5%)
  * Can now take damage from more sources (e.g. fireworks)
* Wither
  * Attack
    * Wither now stops charging when moving too far away for the targeting position 
    * Wither will no longer charge if he can't see a target that's not a player 
    * Wither no longer starts a charging attack while already performing one
* Added a config option to disable the message for the first Wither Summoned / Dragon killed

## 3.3.3
* Wither will now try to charge the target when hidden for too long
* Added a message when the first dragon is killed or the first wither is summoned mentioning that the next ones will be harder to fight
* Added a config option to Ignore Witherproof Blocks (disabled by default)  
  When enabled the wither will destroy wither proof blocks too (still will not destroy unbreakable blocks)
* Wither no longer dies when starting a charge attack if he has less than 10 health
* Fixed crash when min cooldown was higher than max cooldown

## 3.3.2
* Dragon fireball impact damage now counts as both Magic and projectile damage (so Projectile protection works against it)
* Reduced Larva bonus attack damage (+0.4 -> +0.35 per difficulty)
* Hopefully fixed (again) compatilibty with ArcLight

## 3.3.1
* Wither
    * Added Blacklist for Wither's Difficulty Feature. Defaults to Botania's Pink Wither
    * Charge and Barrage attack chances are now based off damage taken and wither health, 10 damage is 100% (of chance), increasing / decreasing accordingly (5 damage = 50%, 15 damage = 150%). Also increased max Charge chance (5% -> 6%)
* Dragon
    * Larvae Attack Damage now scales with dragon difficulty (+0.4 damage per difficulty) (+9.6 at max difficulty)
    * Decreased Direct bonus damage (13.5% -> 12.5% more direct damage per difficulty) and Acid bonus damage (11.3% -> 11% more acid damage per difficulty)
    * Increased Crystal Respawn Multiplier (0.5 -> 0.6 tries per difficulty, 12 -> 14 tries at max difficulty)
    * Increased Minion Attack Speed (1 bullet every 4-22 secs -> 1 bullet every 3.5-19.25 secs)
    * Decreased Blinding Minion chance (1.7% -> 1.5% chance per difficulty) (at max difficulty: 40.8% -> 36% chance)
    * The respawn phase is now an actual phase! This means that the Hover Phase works again like vanilla and the code is slightly cleaner.

## Alpha 3.3.0
* Fixed possible crash on chunk load
* Wither
    * Attack
        * Reduced Wither bonus damage (+5% -> +4% damage per difficulty). This should reduce the disintegration of armor by a few minutes.
        * Charged attack chance is now based off Wither's health. Less health = higher chance to charge. Also increased max chance (2% (4% when below half health) -> up to 5% (based off Wither's health) chance to charge the player)
        * Barrage attack chance is now based off Wither's health. Less health = higher chance to activate barrage. (0.175% chance per difficulty, up to 4% (doubled when below half health) -> up to 0.35% (based off Wither's health) chance per difficulty to start a barrage attack)
        * Barrage attack duration is now based off Wither's health. Less health = higher barrage duration. (3 (6 if below half health) -> 1 to 7.5 (based off Wither's health) seconds duration)
          Also no longer stacks.
    * Minion
        * Cooldown reduction on half health increased (-40% -> -50% cooldown)
        * Can now sense the player through blocks
    * Misc
        * Wither no longer breaks block below him when below half health

## 3.2.0
* Updated InsaneLib
    * With this, difficulty features can no longer be disabled
* Dragon
    * Resistances & Weaknesses
        * Fixed Ender dragon taking bonus damage from crystal explosions
    * Attack 
        * Fireball and Charge chance are now calculated based of difficulty and max difficulty and no longer accounts for end crystals.  
          Basically higher difficulty = higher chance, capped at max difficulty. Max chance still 50% for charging and 40% for fireballing
        * When searching for a player to charge the Ender Dragon now checks for players near crystals, prioritizing them.  
          Also if a player is near a crystal the chance to charge is doubled (ignoring max chance).
        * Dragon can now fireball the player from farther away (64 -> 96 blocks)
        * Chaining Fireballing and Charging is now slightly slower.
* Wither
    * Minion
        * Completely revamped! Now are an actual entity instead of using Wither skeletons!
          They are small (1.5 blocks high) blueish Skeletons, no longer generate Wither Roses and no longer shoot arrows on fire (instead shot Withered Arrows).
          Drop nothing but 6-8 experience.
        * Now take greatly reduced damage from Withers (-80% damage)
        * Now take 3 times more magic damage
        * Increased Minion Cooldown (12-24 -> 15-30 secs)
        * Fix punch/knockback chance still begin 4% instead of 15%, but actually reduce (15% -> 10% per difficulty) so reduced max enchant level (III-60% to be IV -> II-40% to be III).
    * Resistances and Weaknesses
        * Damage reduction now only applies to melee damage. Incrased Max Damage reduction (15% -> 24% damage reduction when above half health, 36% -> 48% damage reduction when below half health)
        * Decreased bonus magic damage (+100% magic damage every 200 -> 250 missing health)
    * Attack
        * Reduced bonus damage per difficulty (+6% -> +5% damage per difficulty)
        * Increased charge attack chance (1.5% -> 2%)
        * Decreased Barrage attack chance and max barrage attack chance (0.2% -> 0.175% per difficulty, up to 5% -> 4%)
        * Slowed down barrage attack (10 -> 6.66 skulls per second) but increased duration (2.5 -> 3 secs)
        * Increased Charge Damage (10 -> 16)
        * Items now correctly spawn on charge attack and no longer spam the logs
    
## 3.1.4
* Wither 
    * Increased Fire Explosion at Difficulty (8 -> 16)
    * Fixed Botania's Pink wither spawning minions

## 3.1.3
* The mod now works properly with Endergetic Expansion
* Dragon
    * Dragon now reaches max difficulty (and also max health, chances, etc.) at 24 dragons killed instead of 82. (check [here](https://github.com/Insane96/ProgressiveBosses/commit/c102cb4e9316ae40b41aefb9b510c7a2430da913) to see the full changes)
    * Health
        * Increased bonus health regeneration per difficulty (0.025 -> 0.05 hp/s) (max bonus regen reached at 40 -> 20 difficulty)
    * Attack
        * Decreased max Fireball chance (45% -> 40%)
        * Increased max Charge chance (45% -> 50%)
        * The max chance for the Dragon to charge/fireball the player is now reached at a higher difficulty (16 -> 20)
        * Fixed multiple charges or strafes in a row not working properly
    * Minion
        * Increased Cooldown (70-90 seconds -> 80-100 seconds)
        * Increased Blinding Minion Chance (0.3% -> 1.7% chance per difficulty) (at max difficulty: 24.6% -> 40.8% chance)
        * Slightly reduced spawning radius (should no longer spawn behind towers)
    * Larva
        * Increased Larva Cooldown (30-60 -> 40-70 secs)
    * Resistances and Vulnerabilities
        * Changed bonus direct and indirect damage to bonus current health damage.  
          (+80% direct damage dealt -> +2% of current health direct damage)  
          (+55% indirect damage dealt -> +0.8% of current health indirect damage)
    * Crystals
        * More cages around the crystals will now spawn at difficulty 2 instead of 1
* Wither
    * Wither now reaches max difficulty (and also max health, chances, etc.) at 24 wither spawned instead of 72.
    * Health
        * Increased bonus health regeneration per difficulty (0.05 -> 0.1 hp/s) (max bonus regen reached at 40 -> 20 difficulty)
    * Attack
        * Increased Skull velocity multiplier (wither skulls 2.5 -> 2.75 times faster)
    * Minions
        * Increased power/sharpness chance (6.25% -> 20% chance per difficulty) (chance at max difficulty: 450% -> 480%)
        * Increased punch/knockback chance (4% -> 15% chance per difficulty) (chance at max difficulty: 288% -> 360%, so increased max punch/knockback level II-III -> III-IV)
    * Misc
        * Increased explosion power bonus (0.3 -> 0.5) (capped explosion (13 power) at difficulty: 20 -> 12)
    * Reward
        * Increased bonus experience per difficulty (+50% -> +200% bonus experience per difficulty)
        * Increased nether star shard drop chance (16% -> 50% chance to drop a shard, trying as many times as the difficulty)

## Beta 3.1.2
* The mod should now work on Arclight (Bukkit/Forge server) (seems not tho :c)
* Wither
    * Charge attack now deals proper knockback (about like the ender dragon)

## Beta 3.1.1
* Moved Player Data to Capabilities. This means that you'll need to transfer player's difficulty to the new system with `/progressivebosses difficulty @p set <wither/dragon> <amount>`. You can get your old difficulty with `/progressivebosses legacy_difficulty @p get`.
* Dragon
    * Attack
        * The dragon can now charge / fireball the player again when she's just finished charging / fireballing. COMBO BREAKER
    * Larva
        * Increased Cooldown (30-50 secs -> 30-60 seconds)
        * Increased max Larva Spawned (6 -> 8)
    * Minion
        * Increased Blindness duration (5 secs -> 7.5 secs)
        * Increased Attack Speed (1 bullet every 5-27.5 secs -> 1 bullet every 4-22 secs)
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
