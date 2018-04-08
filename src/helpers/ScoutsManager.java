package helpers;

import java.util.ArrayList;
import java.util.List;

import bwapi.Unit;
import bwta.BWTA;
import bwta.BaseLocation;

public class ScoutsManager {
	public static List<Unit> Scouts = new ArrayList<Unit>();
	public static void ScoutEnemyBase() {
		if (Scouts.size() > 0) {
			for (BaseLocation b : StarCraftInstance.baseLocations) {
				// If this is a possible start location,
				if (b.isStartLocation() && b.getTilePosition().getDistance(StarCraftInstance.self.getStartLocation()) > 0) {
					// do something. For example send some unit to attack that position:
					for (Unit scout : Scouts) {
						if (!scout.isMoving()) {
							scout.move(b.getPosition());	
						}
					}
					break;
				}
			}	
		}
	}
}
