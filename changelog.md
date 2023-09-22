# Changelog

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
