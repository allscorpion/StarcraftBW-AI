package helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import bwapi.*;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;
import helpers.ResourcesManager;
import models.Building;


public class BuildingsManager {

	// Resources to build structure
	// Tile to build structure
	// Move to tile if unexplored
	// Build on tile if explored
	// Check if building is constructing
	// Check if worker is alive and constructing building
	// Get new worker if old worker is dead
	// Continue constructing building
	
    public static HashSet<TilePosition> ReservedTiles = new HashSet<TilePosition>();
    public static List<Building> BuildingsUnderConstruction = new ArrayList<Building>();
    public static List<Unit> CommandCenters = new ArrayList<Unit>();
    public static List<Unit> MilitaryBuildings = new ArrayList<Unit>();
    public static List<Chokepoint> InaccessibleChokepoints = new ArrayList<Chokepoint>();
    public static int BarracksCount = 0;
    public static void buildingCreated(Unit unit) {
    	if (unit.getType().isBuilding()) {
    		if (unit.getType() == UnitType.Terran_Supply_Depot) {
    			//game.printf("Created depo at " + (self.supplyUsed() / 2) + " Supply");	
    		}
    		else if (unit.getType() == UnitType.Terran_Command_Center) {
        		CommandCenters.add(unit);
        	}
        	else if (unit.getType() == UnitType.Terran_Barracks) {
        		MilitaryBuildings.add(unit);
    		}
    		if (BuildingsUnderConstruction.size() > 0) {
    			Building constructingBuidling = GetBuildingFromUnit(unit);
        		if (constructingBuidling == null) {
        			StarCraftInstance.game.printf("Unable to find building");
        		}else {
        			constructingBuidling._structure = unit;
        			BuildingStartedConstruction(constructingBuidling);
        		}	
    		}
    	}
    }

    public static void buildingDestroyed(Unit unit) {
    	if (unit.getType().isBuilding()) {
    		if (unit.getType() == UnitType.Terran_Command_Center) {
        		CommandCenters.remove(unit);
        	}
        	else if (unit.getType() == UnitType.Terran_Barracks) {
        		MilitaryBuildings.remove(unit);
        		ResourcesManager.MilitaryMineralUnitCost -= 50;
        		BarracksCount--;
    		}
    	}
    }
    
    public static void buildingComplete(Unit unit) {
    	if (unit.getType().isBuilding()) {
    		if (BuildingsUnderConstruction.size() > 0) {
    			Building finishedBuilding = GetBuildingFromUnit(unit);
        		if (finishedBuilding == null) {
        			StarCraftInstance.game.printf("Unable to find building");
        		}else {
        			BuildingFinishedConstruction(finishedBuilding);
        		}	
    		}
    	}
    }
    
 // Returns a suitable TilePosition to build a given building type near
 // specified TilePosition aroundTile, or null if not found. (builder parameter is our worker)
    public static TilePosition getBuildTile(Unit builder, UnitType buildingType, TilePosition aroundTile) {
	 	TilePosition ret = null;
	 	int maxDist = 3;
	 	int stopDist = 40;
	
	 	// Refinery, Assimilator, Extractor
	 	if (buildingType.isRefinery()) {
	 		for (Unit n : StarCraftInstance.game.neutral().getUnits()) {
	 			if ((n.getType() == UnitType.Resource_Vespene_Geyser) &&
	 					( Math.abs(n.getTilePosition().getX() - aroundTile.getX()) < stopDist ) &&
	 					( Math.abs(n.getTilePosition().getY() - aroundTile.getY()) < stopDist )
	 					) return n.getTilePosition();
	 		}
	 	}
	
	 	while ((maxDist < stopDist) && (ret == null)) {
	 		for (int i=aroundTile.getX()-maxDist; i<=aroundTile.getX()+maxDist; i++) {
	 			for (int j=aroundTile.getY()-maxDist; j<=aroundTile.getY()+maxDist; j++) {
	 				TilePosition availablePosition = new TilePosition(i, j);
	 				if (!isTileReserved(availablePosition, buildingType) && StarCraftInstance.game.canBuildHere(availablePosition, buildingType, builder, false)) {
	 					// units that are blocking the tile
	 					boolean unitsInWay = false;
	 					for (Unit u : StarCraftInstance.game.getAllUnits()) {
	 						if (u.getID() == builder.getID()) continue;
	 						if ((Math.abs(u.getTilePosition().getX()-i) < 4) && (Math.abs(u.getTilePosition().getY()-j) < 4)) unitsInWay = true;
	 					}
	 					if (!unitsInWay) {
	 						// game.sendText("i: " + i + " j: " + j);
	 						return availablePosition;
	 					}
	 					// creep for Zerg
//	 					if (buildingType.requiresCreep()) {
//	 						boolean creepMissing = false;
//	 						for (int k=i; k<=i+buildingType.tileWidth(); k++) {
//	 							for (int l=j; l<=j+buildingType.tileHeight(); l++) {
//	 								if (!game.hasCreep(k, l)) creepMissing = true;
//	 								break;
//	 							}
//	 						}
//	 						if (creepMissing) continue;
//	 					}
	 				}
	 			}
	 		}
	 		maxDist += 2;
	 	}
	
	 	if (ret == null) {
	 		// refund building 
	 		StarCraftInstance.game.printf("Unable to find suitable build position for "+ buildingType.toString());	
	 	};
	 	return ret;
	 }
    
    public static boolean isTileReserved(TilePosition position, UnitType buildingType) {
    	boolean isReserved = false;
    	for (TilePosition reservedPosition : ReservedTiles) {
    		if (reservedPosition.getDistance(position) <= Math.max(buildingType.tileSize().getX(), buildingType.tileSize().getY())) {
    			isReserved = true;
    			break;
    		}
		}
    	return isReserved;
    }
    
    public static void CheckBuildingProgress() {
    	for (Building building : BuildingsUnderConstruction) {
    		if (building._builder.canBuild() && !building._isConstructing) {
    			// build failed
    			building.RestartBuild();
    		}
		}
    }
    
    public static Building GetBuildingFromUnit(Unit unit) {
    	for (Building building : BuildingsUnderConstruction) {
    		if (unit.getTilePosition().getDistance(building._buildingReservedPosition) == 0) {
    			return building;	
    		}
		}	
    	return null;		
    }
    
    public static void BuildingStartedConstruction(Building building) {
    	building._isConstructing = true;
    	ResourcesManager.MineralsInReserve -= building._buildingType.mineralPrice();
		RemoveBuildingReservedPosition(building);
    }
    
    public static void RemoveBuildingReservedPosition(Building building) {
    	for (TilePosition position : ReservedTiles) {
			if (position.getDistance(building._buildingReservedPosition) == 0)  {
				ReservedTiles.remove(position);
				break;
			}
		}
    }
    
    public static void BuildingFinishedConstruction(Building building) {
    	building._builder.stop();
		BuildingsUnderConstruction.remove(building);
    }
    
	public static BaseLocation GetClosestEmptyBase(Unit unit) {
    	BaseLocation mySpawn = BWTA.getStartLocation(StarCraftInstance.self);
        BaseLocation closestBase = null;
        List<Position> baseLocationsTaken = new ArrayList<Position>();
        for (Unit commandCenter : CommandCenters) {
        	baseLocationsTaken.add(commandCenter.getPosition());
        }
        for (final BaseLocation baseLocation : BWTA.getBaseLocations()) {
        	boolean isInaccessible = false;
        	for (Chokepoint cp : baseLocation.getRegion().getChokepoints()) {
        		if (InaccessibleChokepoints.contains(cp)) {
        			isInaccessible = true;
        			break;
        		}
        	}
			if (!baseLocationsTaken.contains(baseLocation.getPosition()) && !isInaccessible && mySpawn.getRegion().isReachable(baseLocation.getRegion()) && !baseLocation.isIsland()) 
			{
				if (closestBase == null) {
					closestBase = baseLocation;
				}
				else if (baseLocation.getGroundDistance(mySpawn) < closestBase.getGroundDistance(mySpawn)) {
					closestBase = baseLocation;	
				}
			}
		}
        //game.drawTextMap(closestBase.getPosition(), "My next base");
        return closestBase;
    }
	
	public static Chokepoint GetClosestChokepoint(final BaseLocation baseLocation) {
		List<Chokepoint> baseChokepoints = baseLocation.getRegion().getChokepoints();
		Collections.sort(baseChokepoints, new Comparator<Chokepoint>() {
            @Override
            public int compare(Chokepoint u1, Chokepoint u2) {
				return baseLocation.getPosition().getDistance(u1.getCenter()) < baseLocation.getPosition().getDistance(u2.getCenter())
                        ? -1 : 1;
            }
        });
		return baseChokepoints.get(0);
	}
	
}