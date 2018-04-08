package helpers;

import java.util.ArrayList;
import java.util.List;

import bwapi.Unit;
import bwta.BWTA;
import bwta.BaseLocation;

public class ScoutsManager {
	
	public static void ScoutEnemyBase(Unit scout) {
		if (scout != null) {
			for (BaseLocation b : StarCraftInstance.baseLocations) {
				// If this is a possible start location,
				if (b.isStartLocation() && b.getTilePosition().getDistance(StarCraftInstance.self.getStartLocation()) > 0) {
					// do something. For example send some unit to attack that position:
					if (!scout.isMoving()) {
						scout.move(b.getPosition());	
					}
					break;
				}
			}	
		}
	}
}
