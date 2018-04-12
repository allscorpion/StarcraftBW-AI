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
import models.Worker;


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
    public static List<Unit> MilitaryBuildings = new ArrayList<Unit>();
    public static List<Chokepoint> InaccessibleChokepoints = new ArrayList<Chokepoint>();
    public static int BarracksCount = 0;
    public static HashSet<Position> enemyBuildingMemory = new HashSet<Position>();
    public static void buildingCreated(Unit unit) {
    	if (unit.getType().isBuilding()) {
    		if (unit.getType() == UnitType.Terran_Supply_Depot) {
    			//game.printf("Created depo at " + (self.supplyUsed() / 2) + " Supply");	
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
    		
    		if (BuildingsUnderConstruction.size() > 0) {
    			Building constructingBuilding = GetBuildingFromUnit(unit);
        		if (constructingBuilding == null) {
        			StarCraftInstance.game.printf("Unable to find building");
        		}else {
        			constructingBuilding._structure = null;
        		}	
    		}
    		if (unit.getType() == UnitType.Terran_Supply_Depot) {
    			ResourcesManager.PotentialSupply -= UnitType.Terran_Supply_Depot.supplyProvided();
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
        		ResourcesManager.MilitaryMineralUnitCost -= 50;
        		BarracksCount--;
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
    		if (BuildingsUnderConstruction.size() > 0) {
    			Building finishedBuilding = GetBuildingFromUnit(unit);
        		if (finishedBuilding == null) {
        			StarCraftInstance.game.pauseGame();
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
//	 	Position builderRegionCenter = builder.getRegion().getCenter();
//	 	bwta.Region tileRegion = BWTA.getRegion(aroundTile);
//	 	DrawingHelper.writeTextMessage(String.valueOf(tileRegion.getDistance(builderRegionCenter)));
	 	
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
    		building.GetNewBuilderIfRequired();
    		if (StarCraftInstance.game.isExplored(building._buildingReservedPosition)) {
    			// build failed
    			if (building._builder.unit.canBuild() && !building._builder.unit.isConstructing()) {
    				building.RestartBuild();	
    			}
    		}else {
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
    			if (unit.getTilePosition().getDistance(building._buildingReservedPosition) <= Math.max(building._buildingType.tileSize().getX(), building._buildingType.tileSize().getY())) {
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
    
	public static BaseLocation GetClosestEmptyBase(Unit unit) {
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
	
}