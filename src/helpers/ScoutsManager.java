package helpers;

import java.util.ArrayList;
import java.util.List;

import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;
import bwta.BWTA;
import bwta.BaseLocation;
import models.CustomBaseLocation;
import models.Scout;
import models.SpawnLocation;

public class ScoutsManager {
	
	public static void Init() {
		 scouts = new ArrayList<Scout>();
		 scoutedLocations = new ArrayList<TilePosition>();
		 enemyPerimeterScouted = new ArrayList<Position>();
	}
	
	public static List<Scout> scouts;
	public static List<TilePosition> scoutedLocations;
	public static List<Position> enemyPerimeterScouted;
	
	public static void ScoutKilled(Unit u) {
		Scout deadScout = ConvertUnitToScout(u);
		if (deadScout != null) {
			scouts.remove(deadScout);
		}
	}
	
	public static Scout ConvertUnitToScout(Unit u) {
		for (Scout scout : scouts) {
			if (scout.unit.getID() == u.getID()) {
				return scout;
			}
		}
		return null;
	}
	
	public static void ScoutEnemyBase() {
		EnemyManager.storeEnemySpawn();
		if (EnemyManager.enemySpawn != null) {
			ScoutEnemyBasePerimeter();
			return;
		}
		for (Scout scout : scouts) {
			if (scout.unit.isIdle()) {
				if (scout.currentTarget != null && scout.unit.getTilePosition().getDistance(scout.currentTarget) <= 10) {
					scoutedLocations.add(scout.currentTarget);
				}
				SpawnLocation closestSpawn = null;
				double closestSpawnDistance = Integer.MAX_VALUE;
				for (SpawnLocation sl : BaseManager.spawnLocations) {
					if (sl.isMySpawn) continue;
					TilePosition baseLocationTilePosition = sl.baseLocation.getTilePosition();
					double baseLocationDistanceToScout = baseLocationTilePosition.getDistance(scout.unit.getTilePosition());
					if (!scoutedLocations.contains(baseLocationTilePosition) && (closestSpawn == null || baseLocationDistanceToScout < closestSpawnDistance)) {
						closestSpawn = sl;
						closestSpawnDistance = baseLocationDistanceToScout;
					}
				}	
				if (closestSpawn != null) {
					Position ct = closestSpawn.baseLocation.getPosition();
					scout.currentTarget = closestSpawn.baseLocation.getTilePosition();
					scout.unit.move(ct);
				}
			}
		}
	}
	private static void ScoutEnemyBasePerimeter() {
		if (EnemyManager.enemySpawn == null) return;
		List<Position> enemySpawnPerimeter = EnemyManager.enemySpawn.baseLocation.getRegion().getPolygon().getPoints();
		if (enemySpawnPerimeter.size() == enemyPerimeterScouted.size()) {
			// we have scouted the entire perimeter of the base
			// start again
			enemyPerimeterScouted = new ArrayList<Position>();
		}
			
		for (Scout scout : scouts) {
			if (scout.unit.isIdle() || (scout.currentTarget != null && scout.currentTarget.getDistance(scout.unit.getTilePosition()) <= 10)) {
				for (Position p : enemySpawnPerimeter) {
					if (enemyPerimeterScouted.contains(p)) continue;
					scout.currentTarget = p.toTilePosition();
					scout.unit.move(p);
					enemyPerimeterScouted.add(p);
					break;
				}
			}
		}
	}
}
