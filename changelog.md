# Changelog

## Upcoming
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