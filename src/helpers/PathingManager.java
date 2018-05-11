package helpers;

import java.util.ArrayList;
import java.util.List;

import bwapi.Color;
import bwapi.Position;
import bwta.Region;
import bwta.BaseLocation;
import bwta.Chokepoint;
import models.CustomBaseLocation;
import models.MyUnit;

public class PathingManager {
	public static List<Position> FindPath(Position start, Position target) {
		List<Position> pathsToTake = new ArrayList<Position>();
		BaseLocation startClosestBaseLocation = null;
		BaseLocation targetClosestBaseLocation = null;
		
		for (CustomBaseLocation cbl : BaseManager.baseLocations) {
			if (startClosestBaseLocation == null || cbl.baseLocation.getDistance(start) < startClosestBaseLocation.getDistance(start)) {
				startClosestBaseLocation = cbl.baseLocation;
			}
			if (targetClosestBaseLocation == null || cbl.baseLocation.getDistance(target) < targetClosestBaseLocation.getDistance(target)) {
				targetClosestBaseLocation = cbl.baseLocation;
			}
		}
		
		if (startClosestBaseLocation != null && targetClosestBaseLocation != null) {
			Position currentPosition = start;
			Region currentRegion = startClosestBaseLocation.getRegion();
			Chokepoint closestChokepoint = GetClosestChokepoint(null, target, currentRegion.getChokepoints());
			for (int i = 0; i < 4; i++) {
				DrawingHelper.drawTextAt(currentRegion.getCenter(), "Region at interval " + i);
				closestChokepoint = GetClosestChokepoint(closestChokepoint, target, currentRegion.getChokepoints());
				StarCraftInstance.game.drawLineMap(currentPosition, closestChokepoint.getCenter(), Color.Black);
				currentPosition = closestChokepoint.getCenter();
				currentRegion = GetNextRegion(closestChokepoint, currentRegion, target);
				if (currentRegion != null) {
					for (BaseLocation bl : currentRegion.getBaseLocations()) {
						if (bl.getPosition() != targetClosestBaseLocation.getPosition()) continue;
						currentPosition = bl.getPosition();
						break;
					}	
				}
			}
			
			
//			StarCraftInstance.game.drawLineMap(start, startClosestChokepoint.getCenter(), Color.Black);
//			StarCraftInstance.game.drawLineMap(startClosestChokepoint.getCenter(), targetClosestBaseLocation.getPosition(), Color.Black);	
//			StarCraftInstance.game.drawLineMap(targetClosestBaseLocation.getPosition(), target, Color.Black);
		}
		return pathsToTake;
	}
	
	
	private static Region GetNextRegion(Chokepoint currentChokepoint, Region currentRegion, Position target) {
		for (CustomBaseLocation cbl : BaseManager.baseLocations)  {
			Region r = cbl.baseLocation.getRegion();
			if (r.getCenter().getDistance(currentRegion.getCenter()) == 0) continue;
			for (Chokepoint c : r.getChokepoints())  {
				if (c.getCenter().getDistance(currentChokepoint.getCenter()) == 0) {
					return r;
				}
				
			}
		}
		return null;
	}
	
	public static Chokepoint GetClosestChokepoint(Chokepoint currentCP, Position p, List<Chokepoint> cps) {
		Chokepoint closestChokepoint = null;
		if (cps.size() == 1) {
			closestChokepoint = cps.get(0);
		}
		for (Chokepoint cp : cps) {
			if (currentCP != null && cp.getCenter().getDistance(currentCP.getCenter()) == 0) continue;
			if (closestChokepoint == null || cp.getDistance(p) < closestChokepoint.getDistance(p)) {
				closestChokepoint = cp;
			}
		}
		return closestChokepoint;
	}
	
}
