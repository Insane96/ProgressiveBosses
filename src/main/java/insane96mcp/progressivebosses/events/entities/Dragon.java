package insane96mcp.progressivebosses.events.entities;

import net.minecraftforge.event.entity.EntityJoinWorldEvent;

public class Dragon {
	public static void setStats(EntityJoinWorldEvent event) {
/*
		crystalCages(dragon, killedCount);
		moreCrystals(dragon, killedCount);
*/
	}
/*
	private static void crystalCages(EnderDragonEntity dragon, float killedCount) {
		int moreCagesAtDifficulty = Config.COMMON.dragon.crystal.moreCagesAtDifficulty.get();

		if (moreCagesAtDifficulty == -1)
			return;

		if (killedCount < moreCagesAtDifficulty)
			return;

		Vector3d centerPodium = Vector3d.copyCenteredHorizontally(dragon.world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION));

		AxisAlignedBB bbCrystals = new AxisAlignedBB(centerPodium.add(-64, -16, -64), centerPodium.add(64, 64, 64));

		List<EnderCrystalEntity> crystals = dragon.world.getEntitiesWithinAABB(EnderCrystalEntity.class, bbCrystals);
		//Remove the 4 crystals at the center
		crystals.removeIf(c -> Math.sqrt(c.getDistanceSq(centerPodium)) <= 10d);
		//Remove all the crystals that aren't on bedrock (so any player placed crystal or leftovers from previous fight will not be counted)
		crystals.removeIf(c -> c.world.getBlockState(c.getPosition().down()).getBlock() != Blocks.BEDROCK);
		//Remove all the crystals that already have cages around
		crystals.removeIf(c -> c.world.getBlockState(c.getPosition().up(2)).getBlock() == Blocks.IRON_BARS);
		//Order by the lowest crystal
		crystals.sort(Comparator.comparingDouble(Entity::getPosY));

		int crystalsInvolved = Math.round(killedCount - moreCagesAtDifficulty + 1);
		int cagesGenerated = 0;

		for (EnderCrystalEntity crystal : crystals) {

			//Shamelessly copied from MC Code
			BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();
			for(int k = -2; k <= 2; ++k) {
				for(int l = -2; l <= 2; ++l) {
					for(int i1 = 0; i1 <= 3; ++i1) {
						boolean flag = MathHelper.abs(k) == 2;
						boolean flag1 = MathHelper.abs(l) == 2;
						boolean flag2 = i1 == 3;
						if (flag || flag1 || flag2) {
							boolean flag3 = k == -2 || k == 2 || flag2;
							boolean flag4 = l == -2 || l == 2 || flag2;
							BlockState blockstate = Blocks.IRON_BARS.getDefaultState().with(PaneBlock.NORTH, flag3 && l != -2).with(PaneBlock.SOUTH, flag3 && l != 2).with(PaneBlock.WEST, flag4 && k != -2).with(PaneBlock.EAST, flag4 && k != 2);
							crystal.world.setBlockState(blockpos$mutable.setPos(crystal.getPosX() + k, crystal.getPosY() - 1 + i1, crystal.getPosZ() + l), blockstate);
						}
					}
				}
			}

			cagesGenerated++;
			if (cagesGenerated == crystalsInvolved)
				break;
		}
	}

	private static void moreCrystals(EnderDragonEntity dragon, float killedCount) {
		int moreCrystalsAtDifficulty = Config.COMMON.dragon.crystal.moreCrystalsAtDifficulty.get();

		if (moreCrystalsAtDifficulty == -1)
			return;

		if (killedCount < moreCrystalsAtDifficulty)
			return;

		Vector3d centerPodium = Vector3d.copyCenteredHorizontally(dragon.world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION));

		AxisAlignedBB bbCrystals = new AxisAlignedBB(centerPodium.add(-64, -16, -64), centerPodium.add(64, 64, 64));

		List<EnderCrystalEntity> crystals = dragon.world.getEntitiesWithinAABB(EnderCrystalEntity.class, bbCrystals);
		//Remove the 4 crystals at the center
		crystals.removeIf(c -> Math.sqrt(c.getDistanceSq(centerPodium)) <= 10d);
		//Remove all the crystals that aren't on bedrock (so any player placed crystal or leftovers from previous fight will not be counted)
		crystals.removeIf(c -> c.world.getBlockState(c.getPosition().down()).getBlock() != Blocks.BEDROCK);
		//Order by the lowest crystal
		crystals.sort(Comparator.comparingDouble(Entity::getPosY));

		int crystalsInvolved = Math.round(killedCount - moreCrystalsAtDifficulty + 1);
		int crystalSpawned = 0;

		for (EnderCrystalEntity crystal : crystals) {
			BlockPos crystalPos = new BlockPos(crystal.getPosX(), crystal.getPosY() - 16, crystal.getPosZ());
			if (crystalPos.getY() < centerPodium.getY())
				crystalPos = new BlockPos(crystalPos.getX(), centerPodium.getY(), crystalPos.getZ());

			Stream<BlockPos> blocks = BlockPos.getAllInBox(crystalPos.add(-1, -1, -1), crystalPos.add(1, 1, 1));

			blocks.forEach(pos -> dragon.world.setBlockState(pos, Blocks.AIR.getDefaultState()));
			dragon.world.setBlockState(crystalPos.add(0, -1, 0), Blocks.BEDROCK.getDefaultState());

			dragon.world.createExplosion(dragon, crystalPos.getX() + .5f, crystalPos.getY(), crystalPos.getZ() + .5, 5f, Explosion.Mode.DESTROY);

			EnderCrystalEntity newCrystal = new EnderCrystalEntity(dragon.world, crystalPos.getX() + .5, crystalPos.getY(), crystalPos.getZ() + .5);
			//newCrystal.setShowBottom(false);
			dragon.world.addEntity(newCrystal);

			crystalSpawned++;
			if (crystalSpawned == crystalsInvolved)
				break;
		}
	}

*/
}
