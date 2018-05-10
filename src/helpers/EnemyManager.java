package helpers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;
import bwta.Chokepoint;
import models.Building;
import models.ReservedTile;
import models.SpawnLocation;

public class EnemyManager {
	public static void Init() {
	    enemyBuildingMemory = new HashSet<Position>();
	    enemySpawn = null;
	}
	public static HashSet<Position> enemyBuildingMemory;
	public static SpawnLocation enemySpawn;
	public static void storeEnemyBuidlings() {
		//always loop over all currently visible enemy units (even though this set is usually empty)
			for (Unit u : StarCraftInstance.game.enemy().getUnits()) {
				//if this unit is in fact a building
				if (u.getType().isBuilding()) {
					//check if we have it's position in memory and add it if we don't
					if (!enemyBuildingMemory.contains(u.getPosition())) enemyBuildingMemory.add(u.getPosition());
				}
			}
		
			//loop over all the positions that we remember
			for (final Position p : enemyBuildingMemory) {
				// compute the TilePosition corresponding to our remembered Position p
				TilePosition tileCorrespondingToP = new TilePosition(p.getX()/32 , p.getY()/32);
		
				//if that tile is currently visible to us...
				if (StarCraftInstance.game.isVisible(tileCorrespondingToP)) {
		
					//loop over all the visible enemy buildings and find out if at least
					//one of them is still at that remembered position
					boolean buildingStillThere = false;
					for (Unit u : StarCraftInstance.game.enemy().getUnits()) {
						if ((u.getType().isBuilding()) && (u.getPosition().equals(p))) {
							buildingStillThere = true;
							break;
						}
					}
		
					//if there is no more any building, remove that position from our memory
					if (buildingStillThere == false) {
						enemyBuildingMemory.remove(p);
						break;
					}
		//			else {
		//				StarCraftInstance.self.getUnits().stream().filter(new Predicate<Unit>() {
		//					@Override
		//					public boolean test(Unit u) {
		//						return !u.isAttacking();
		//					}
		//				}).forEach(new Consumer<Unit>() {
		//					@Override
		//					public void accept(Unit attackUnit) {
		//						if (attackUnit.getType() == UnitType.Terran_Marine) {
		//							attackUnit.attack(p);
		//						}
		//					}
		//				});
		//			}
				}
			}
		}
	public static void storeEnemySpawn() {
		if (enemySpawn == null) {
			for (SpawnLocation sl : BaseManager.spawnLocations) {
				if (sl.isMySpawn) continue;
				List<Unit> unitsOnSpawn = StarCraftInstance.game.getUnitsOnTile(sl.baseLocation.getTilePosition());
				if (unitsOnSpawn.size() > 0 && unitsOnSpawn.get(0).getType().isBuilding()) {
					sl.isEnemySpawn = true;
					enemySpawn = sl;
					return;
				}
			}
		}
	}
}
