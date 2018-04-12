package helpers;

import java.util.ArrayList;
import java.util.List;

import bwapi.Unit;
import bwta.BWTA;
import bwta.BaseLocation;
import models.CustomBaseLocation;

public class ScoutsManager {
	
	public static void ScoutEnemyBase(Unit scout) {
		if (scout != null) {
			for (CustomBaseLocation cbl : BaseManager.baseLocations) {
				// If this is a possible start location,
				if (cbl.baseLocation.isStartLocation() && cbl.baseLocation.getTilePosition().getDistance(StarCraftInstance.self.getStartLocation()) > 0) {
					// do something. For example send some unit to attack that position:
					
						scout.move(cbl.baseLocation.getPosition());	
					
					break;
				}
			}	
		}
	}
}
