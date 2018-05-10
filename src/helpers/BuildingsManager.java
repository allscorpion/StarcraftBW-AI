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
import models.CommandCenter;
import models.CustomBaseLocation;
import models.ReservedTile;
import models.Worker;


public class BuildingsManager {

	public static void Init() {
		ReservedTiles = new HashSet<ReservedTile>();
		CommandCenterReservedTiles = new HashSet<ReservedTile>();
	    BuildingsUnderConstruction = new ArrayList<Building>();
	    Academy = null;
	    MilitaryBuildings = new ArrayList<Unit>();
	    InaccessibleChokepoints = new ArrayList<Chokepoint>();
	    BarracksCount = 0;
	    paddingAroundBuildings = 1;
	}
	
	public static int paddingAroundBuildings;
    public static HashSet<ReservedTile> ReservedTiles;
    public static HashSet<ReservedTile> CommandCenterReservedTiles;
    public static List<Building> BuildingsUnderConstruction;
    public static Unit Academy;
    public static List<Unit> MilitaryBuildings;
    public static List<Chokepoint> InaccessibleChokepoints;
    public static int BarracksCount;
    
    public static void buildingCreated(Unit unit) {
    	if (unit.getType().isBuilding()) {
    		if (unit.getType() == UnitType.Terran_Supply_Depot) {
    			//game.printf("Created depo at " + (self.supplyUsed() / 2) + " Supply");	
    		}
    		else if (unit.getType() == UnitType.Terran_Academy) {
    			BuildingsManager.Academy = unit;
    		}
    		else if (unit.getType() == UnitType.Terran_Command_Center) {
    			CustomBaseLocation cbl = BaseManager.GetCustomBaseLocationFromPosition(unit.getPosition());
        		if (cbl == null) {
        			StarCraftInstance.game.printf("Unable to find custom base location");
        		}else {
        			cbl.commandCenter = new CommandCenter(unit);
        			BaseManager.AllowOversaturationForAllCommandCenters();
        		}
        	}
        	else if (unit.getType() == UnitType.Terran_Barracks) {
        		BarracksCount++;
        		MilitaryBuildings.add(unit);
    		}
    		if (!unit.getType().isAddon()) {
    			if (BuildingsUnderConstruction.size() > 0) {
        			Building constructingBuidling = GetBuildingFromUnit(unit);
            		if (constructingBuidling == null) {
            			StarCraftInstance.game.printf("Unable to find building");
            		}else {
            			constructingBuidling._buildingReservedPosition.isTemp = false;
            			constructingBuidling._structure = unit;
            			BuildingStartedConstruction(constructingBuidling);
            		}	
        		}
    		}
    	
    	}
    }

    public static void buildingDestroyed(Unit unit) {
    	if (unit.getType().isBuilding()) {
    		ReservedTile rt = getTilePosition(unit.getTilePosition(), unit.getType());
    		if (rt != null) {
    			ReservedTiles.remove(rt);	
    		}
    		if (!unit.getType().isAddon()) {
	    		if (BuildingsUnderConstruction.size() > 0) {
	    			Building constructingBuilding = GetBuildingFromUnit(unit);
	        		if (constructingBuilding == null) {
	        			StarCraftInstance.game.printf("Unable to find building");	 
	        		}else {
	        			constructingBuilding._structure = null;
	        		}
	    		}
    		}
    		if (unit.getType().supplyProvided() > 0) {
    			ResourcesManager.PotentialSupply -= unit.getType().supplyProvided();
    		}
    		if (unit.getType() == UnitType.Terran_Academy) {
    			Academy = null;
    		}
    		else if (unit.getType() == UnitType.Terran_Command_Center) {
    			CustomBaseLocation cbl = BaseManager.GetCustomBaseLocationFromPosition(unit.getPosition());
        		if (cbl != null) {
        			cbl.commandCenter = null;
        			BaseManager.DisallowOversaturationForAllCommandCenters();
        			BaseManager.TransferAdditionalWorkersToFreeBase();
        		}	
        	}
        	else if (unit.getType() == UnitType.Terran_Barracks) {
        		MilitaryBuildings.remove(unit);
        		BarracksCount--;
    		}else if (unit.getType() == UnitType.Terran_Refinery) {
    			BaseManager.RemoveGasFromCommandCenter(unit);
        	}
    	}
    }
    
    public static void buildingComplete(Unit unit) {
    	if (unit.getType().isBuilding()) {
    		if (unit.getType() == UnitType.Terran_Command_Center) {
    			CustomBaseLocation cbl = BaseManager.GetCustomBaseLocationFromPosition(unit.getPosition());
        		if (cbl == null) {
        			StarCraftInstance.game.printf("Unable to find custom base location");
        		}else {
        			BaseManager.DisallowOversaturationForAllCommandCenters();
        			BaseManager.TransferAdditionalWorkersToFreeBase();
        		}
        	}
    		else if (unit.getType() == UnitType.Terran_Refinery) {
    			BaseManager.AssignGasToCommandCenter(unit);
    			BaseManager.TransferWorkersToRefinery();
        	}
    		if (!unit.getType().isAddon()) {
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
    }
    
    
    public static boolean areAllMilitaryBuildingsProducing() {
    	for (Unit militaryBuilding : MilitaryBuildings) {
    		if (militaryBuilding.isBeingConstructed() || !militaryBuilding.isTraining()) return false;
    	}
    	return true;
    }
    
    
 // Returns a suitable TilePosition to build a given building type near
 // specified TilePosition aroundTile, or null if not found. (builder parameter is our worker)
    public static TilePosition getBuildTile(Unit builder, UnitType buildingType, TilePosition aroundTile) {
	 	TilePosition ret = null;
	 	int maxDist = 3;
	 	int stopDist = 40;
	 	int padding = 1;
	 	// Refinery, Assimilator, Extractor
	 	if (buildingType.isRefinery()) {
	 		for (Unit n : StarCraftInstance.game.neutral().getUnits()) {
	 			if ((n.getType() == UnitType.Resource_Vespene_Geyser) &&
	 					( Math.abs(n.getTilePosition().getX() - aroundTile.getX()) < stopDist ) &&
	 					( Math.abs(n.getTilePosition().getY() - aroundTile.getY()) < stopDist ) && 
	 					!isTileReserved(n.getTilePosition(), buildingType) &&
	 					StarCraftInstance.game.canBuildHere(n.getTilePosition(), buildingType, builder, false)
	 					) return n.getTilePosition();
	 		}
	 	}
//	 	Position builderRegionCenter = builder.getRegion().getCenter();
//	 	bwta.Region tileRegion = BWTA.getRegion(aroundTile);
//	 	DrawingHelper.writeTextMessage(String.valueOf(tileRegion.getDistance(builderRegionCenter)));
	 	
	 	while ((maxDist < stopDist) && (ret == null)) {
	 		for (int i=aroundTile.getX()-maxDist; i<=aroundTile.getX()+maxDist; i++) {
	 			for (int j=aroundTile.getY()-maxDist; j<=aroundTile.getY()+maxDist; j++) {
	 				TilePosition availablePosition = new TilePosition(i, j);
	 				if (buildingType != UnitType.Terran_Command_Center && isCommandCenterTileReserved(availablePosition, buildingType)) continue;
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
    
    public static boolean isCommandCenterTileReserved(TilePosition position, UnitType buildingType) {
    	ReservedTile testPosition = new ReservedTile(position, buildingType);
    	for (ReservedTile reservedPosition : CommandCenterReservedTiles) {
    		if (testPosition.isOverlappingTile(reservedPosition)) {
    			return true;
    		}
		}
    	return false;
    }
    
    public static ReservedTile getTilePosition(TilePosition position, UnitType buildingType) {
    	ReservedTile testPosition = new ReservedTile(position, buildingType);
    	for (ReservedTile reservedPosition : ReservedTiles) {
    		if (testPosition.isOverlappingTile(reservedPosition)) {
    			return reservedPosition;
    		}
		}
    	return null;
    }
    
    public static boolean isTileReserved(TilePosition position, UnitType buildingType) {
    	ReservedTile testPosition = new ReservedTile(position, buildingType);
    	for (ReservedTile reservedPosition : ReservedTiles) {
    		if (testPosition.isOverlappingTile(reservedPosition)) {
    			return true;
    		}
		}
    	return false;
    }
    
    public static int amountOfBuildingTypeReserved(UnitType buildingType) {
    	int totalReserved = 0;
    	for (ReservedTile reservedPosition : ReservedTiles) {
    		if (reservedPosition.isTemp && reservedPosition.buildingType == buildingType) {
    			totalReserved++;
    		}
		}
    	return totalReserved;
    }
    
    public static void CheckBuildingProgress() {
    	for (Building building : BuildingsUnderConstruction) {
    		if (building._buildingReservedPosition == null) {
    			BuildingFailedConstruction(building);
    			break;
    		}
    		building.GetNewBuilderIfRequired();
    		if (StarCraftInstance.game.isExplored(building._buildingReservedPosition.tilePositionTopLeft)) {
    			// build failed
    			if (building._builder.unit.canBuild() && !building._builder.unit.isConstructing()) {
    				building.RestartBuild();	
    			}
    		}else {
    			building.RestartBuild();
    			// see if worker is getting closer otherwise delete building
//    			if (building._buildingReservedPosition.getDistance(building._lastBuilderPosition) > 
//    					building._buildingReservedPosition.getDistance(building._builder.getTilePosition())) {
//    				building._lastBuilderPosition = building._builder.getTilePosition();
//    			}else {
//    				BuildingFailedConstruction(building);	
//    			}
    		}
		}
    }
    
    public static Building GetBuildingFromUnit(Unit unit) {
    	for (Building building : BuildingsUnderConstruction) {
    		if (building._buildingReservedPosition != null && building._buildingType != null) {
    			ReservedTile unitRT = new ReservedTile(unit.getTilePosition(), building._buildingType); 
    			if (unitRT.equals((building._buildingReservedPosition))) {
        			return building;	
        		}	
    		}
		}	
    	return null;		
    }
    
    public static Building GetBuildingFromWorker(Unit worker) {
    	for (Building building : BuildingsUnderConstruction) {
    		if (building._builder != null) {
    			if (worker.getID() == building._builder.unit.getID()) {
        			return building;	
        		}	
    		}
		}	
    	return null;		
    }
    
    public static void BuildingStartedConstruction(Building building) {
    	ResourcesManager.MineralsInReserve -= building._buildingType.mineralPrice();
		//RemoveBuildingReservedPosition(building);
    }
    
    public static void RemoveBuildingReservedPosition(Building building) {
    	for (ReservedTile position : ReservedTiles) {
    		if (building._buildingReservedPosition != null && position.equals(building._buildingReservedPosition)) {
				ReservedTiles.remove(position);
				break;
    		}
		}
    }
    
    public static void BuildingFinishedConstruction(Building building) {
    	building._builder.unit.stop();
    	building._builder.isBuilding = false;
    	if (building._buildingType == UnitType.Terran_Command_Center) {
			ResourcesManager.PotentialSupply += building._buildingType.supplyProvided();	
			// split workers between bases
		}
		BuildingsUnderConstruction.remove(building);
    }
    
    public static void BuildingFailedConstruction(Building building) {
    	BuildingStartedConstruction(building);
    	ResourcesManager.PotentialSupply -= building._buildingType.supplyProvided();
    	BuildingFinishedConstruction(building);
    }
    
	public static BaseLocation GetClosestEmptyBase() {
    	BaseLocation mySpawn = BaseManager.mySpawn;
        BaseLocation closestBase = null;
        List<Position> baseLocationsTaken = new ArrayList<Position>();
        for (CustomBaseLocation cbl : BaseManager.baseLocations) {
        	if (cbl.commandCenter != null) {
        		baseLocationsTaken.add(cbl.baseLocation.getPosition());	
        	}
        }
        for (final CustomBaseLocation cbl : BaseManager.baseLocations) {
        	boolean isInaccessible = false;
        	for (Chokepoint cp : cbl.baseLocation.getRegion().getChokepoints()) {
        		if (InaccessibleChokepoints.contains(cp)) {
        			isInaccessible = true;
        			break;
        		}
        	}
			if (!baseLocationsTaken.contains(cbl.baseLocation.getPosition()) && !isTileReserved(cbl.baseLocation.getTilePosition(), UnitType.Terran_Command_Center) && !isInaccessible && mySpawn.getRegion().isReachable(cbl.baseLocation.getRegion()) && !cbl.baseLocation.isIsland()) 
			{
				if (closestBase == null) {
					closestBase = cbl.baseLocation;
				}
				else if (cbl.baseLocation.getGroundDistance(mySpawn) < closestBase.getGroundDistance(mySpawn)) {
					closestBase = cbl.baseLocation;	
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