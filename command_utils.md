/data modify entity @e[type=wither,limit=1] ForgeData.progressivebosses:difficulty set value 72
/data modify entity @e[type=wither,limit=1] Health set value 20
/data modify entity @e[type=minecraft:wither,limit=1] ForgeData.progressivebosses:charge_attack set value 90
/data modify entity @e[type=wither,limit=1] ForgeData.progressivebosses:barrage_attack set value 100

/summon progressivebosses:larva ~ ~ ~ {ForgeData:{"progressivebosses:difficulty":8}}

/data merge entity @e[type=ender_dragon,limit=1] {DragonPhase:3b}