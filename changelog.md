# Changelog

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
* Wither
  * Wither now attacks faster (1 skull every ~~2~~ -> 1.75 seconds) (attack speed when near unchanged 1 skull every 0.7 seconds)
* Fixed generating a bak config file due to wrong explosion config option