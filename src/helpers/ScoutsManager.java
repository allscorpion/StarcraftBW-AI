package helpers;

import java.util.ArrayList;
import java.util.List;

import bwapi.TilePosition;
import bwapi.Unit;
import bwta.BWTA;
import bwta.BaseLocation;
import models.CustomBaseLocation;
import models.Scout;

public class ScoutsManager {
	
	public static List<Scout> scouts = new ArrayList<Scout>();
	
	public static void ScoutEnemyBase() {
		for (Scout scout : scouts) {
			if (scout.unit.isIdle()) {
				BaseLocation closestSpawn = null;
				double closestSpawnDistance = Integer.MAX_VALUE;
				for (BaseLocation bl : BaseManager.spawnLocations) {
					TilePosition baseLocationTilePosition = bl.getTilePosition();
					double baseLocationDistanceToScout = baseLocationTilePosition.getDistance(scout.unit.getTilePosition());
					if (!scout.scoutedLocations.contains(baseLocationTilePosition) && (closestSpawn == null || baseLocationDistanceToScout < closestSpawnDistance)) {
						closestSpawn = bl;
						closestSpawnDistance = baseLocationDistanceToScout;
					}
				}	
				if (closestSpawn != null) {
					scout.unit.move(closestSpawn.getPosition());	
					scout.scoutedLocations.add(closestSpawn.getTilePosition());
				}
			
			}
		}
	}
}
