import java.util.ArrayList;
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
import helpers.StarCraftInstance;
import helpers.BuildingsManager;
import helpers.DrawingHelper;
import helpers.ResourcesManager;
import helpers.ProcessHelper;
import helpers.WorkersManager;
import helpers.UnitsManager;
import models.Building;

public class start extends DefaultBWListener {
	
    private HashSet<Position> enemyBuildingMemory = new HashSet<Position>();
   
    private static Mirror mirror = new Mirror();
    public void run() {
        mirror.getModule().setEventListener(this);
        mirror.startGame();
    }

    @Override
    public void onStart() {
    	StarCraftInstance.Start(mirror);
        StarCraftInstance.game.setLocalSpeed(15);
        //Use BWTA to analyze map
        //This may take a few minutes if the map is processed first time!
        //System.out.println("Analyzing map...");
        BWTA.readMap();
        BWTA.analyze();
        //System.out.println("Map data ready");
        //int i = 0;
        
        // reveal entire map
//      StarCraftInstance.game.sendText("black sheep wall");
        // 10,000 Minerals and Gas
        //StarCraftInstance.game.sendText("show me the money");
        
        BaseLocation mySpawn = BWTA.getStartLocation(StarCraftInstance.self);
        for(BaseLocation baseLocation : BWTA.getBaseLocations()){
        	StringBuilder baseText = new StringBuilder(String.valueOf(baseLocation.getGroundDistance(mySpawn)) + "\n");
        	baseText.append(String.valueOf(baseLocation.getAirDistance(mySpawn)));
        	//game.drawTextMap(baseLocation.getPosition().getX(), baseLocation.getPosition().getY(), baseText.toString()); 
        	for(Chokepoint cp: baseLocation.getRegion().getChokepoints()) {
        		//game.drawTextMap(cp.getCenter().getX(), cp.getCenter().getY(), String.valueOf(cp.getWidth())); 
        		//find the closest mineral
        		if (cp.getWidth() < 150) {
                    for (Unit neutralUnit : StarCraftInstance.game.getUnitsInRadius(cp.getPoint(), (int)cp.getWidth())) {
                        if (neutralUnit.getType().isMineralField()) {
                        	BuildingsManager.InaccessibleChokepoints.add(cp);
                        	//game.drawCircleMap(cp.getPoint().getX(), cp.getPoint().getY(), (int)cp.getWidth(), Color.Green);
                        	break;
                        }
                    }        			
        		}
        	}
//        	System.out.println("Base location #" + (++i) + ". Printing location's region polygon:");
//        	for(Position position : baseLocation.getRegion().getPolygon().getPoints()){
//        		System.out.print(position + ", ");
//        	}
//        	System.out.println();
        }
        
//        BaseLocation mySpawn = BWTA.getStartLocation(self);
//        for(BaseLocation baseLocation : BWTA.getBaseLocations()){
//        	game.drawTextMap(baseLocation.getPosition().getX(), baseLocation.getPosition().getY(), String.valueOf(baseLocation.getGroundDistance(mySpawn))); 
////        	System.out.println("Base location #" + (++i) + ". Printing location's region polygon:");
////        	for(Position position : baseLocation.getRegion().getPolygon().getPoints()){
////        		System.out.print(position + ", ");
////        	}
////        	System.out.println();
//        }
        // GetClosestEmptyBase();
    }
    
    @Override
    public void onUnitDiscover(Unit unit) {
    	
    }
    
    @Override
    public void onUnitCreate(Unit unit) {
    	WorkersManager.onWorkerCreate(unit);
		BuildingsManager.buildingCreated(unit);
    }

    @Override
    public void onUnitDestroy(Unit unit) {
    	BuildingsManager.buildingDestroyed(unit);
    	WorkersManager.onWorkerDestroy(unit);
    	UnitsManager.onUnitDestroy(unit);
    }
    
    @Override
    public void onUnitComplete(Unit unit) {
    	BuildingsManager.buildingComplete(unit);
    	UnitsManager.onUnitComplete(unit);
    }
    
    @Override
    public void onFrame() {
    	DrawingHelper.resetTextPos();
        
        //game.setTextSize(10);
//        drawTextOnScreen("Workers " + Workers.size());
//        drawTextOnScreen("Command Centers " + CommandCenters.size());
        //drawTextOnScreen("Military Buildings " + MilitaryBuildings.size());
        // game.drawTextScreen(10, 10, "" + getCurrentMinerals());
    	DrawingHelper.drawTextOnScreen("Current Minerals " + String.valueOf(ResourcesManager.getCurrentMinerals()));
    	DrawingHelper.drawTextOnScreen("MineralsInReserve " + String.valueOf(ResourcesManager.MineralsInReserve));
    	DrawingHelper.drawTextOnScreen("BuildingsUnderConstruction " + String.valueOf(BuildingsManager.BuildingsUnderConstruction.size()));
    	
    	for (Building b : BuildingsManager.BuildingsUnderConstruction) {
			DrawingHelper.drawTextOnUnit(b._builder, "Is building " + b._buildingType + " at " + b._buildingReservedPosition);
    		StringBuilder debugDetails = new StringBuilder(String.valueOf(b._buildingReservedPosition + "\n"));
    		debugDetails.append(String.valueOf(b._builder.canBuild()) + "\n");
    		debugDetails.append(String.valueOf(b._builder.isConstructing()) + "\n");
    		DrawingHelper.drawTextAt(b._buildingReservedPosition.toPosition(), debugDetails.toString());	
    	}
    	
    	UnitsManager.attackUnits();
    	

    	
    	// drawTextOnScreen("Military Mineral Production Cost " + String.valueOf(MilitaryMineralUnitCost));
    	// game.drawTextScreen(10, 50, "Amount of workers: " + Workers.size());
        //StringBuilder units = new StringBuilder("My units:\n");
//    	if (self.supplyUsed() / 2 < 14) {
//    		game.setLocalSpeed(0);
//    	}else {
//    		game.setLocalSpeed(30);
//    	}
    	//storeEnemyBuidlings();
    	BuildingsManager.CheckBuildingProgress();
    	WorkersManager.SendIdleWorkersToMinerals();
    	//DrawingHelper.drawTextOnScreen("shouldBuildDepo " + String.valueOf(ResourcesManager.isDepoRequired()));
    	//build depos
    	BuildingsManager.GetClosestEmptyBase(null);
    	if (ResourcesManager.isDepoRequired()) {
    		while (ResourcesManager.isDepoRequired() && ResourcesManager.getCurrentMinerals() >= UnitType.Terran_Supply_Depot.mineralPrice() && WorkersManager.GetWorker() != null) {
        		BuildingsManager.BuildingsUnderConstruction.add(new Building(WorkersManager.GetWorker(), UnitType.Terran_Supply_Depot));
    		}	
    	} else {
    		//build units
    		int maxWorkers = Math.min(70, BuildingsManager.CommandCenters.size() * 22);
    		for (Unit myUnit : BuildingsManager.CommandCenters) {
    			if (myUnit.getTrainingQueue().size() < 2 && ResourcesManager.getCurrentMinerals() >= UnitType.Terran_SCV.mineralPrice() && WorkersManager.Workers.size() < maxWorkers) {
                    myUnit.train(UnitType.Terran_SCV);
                }
    		}
    		for (Unit myUnit : BuildingsManager.MilitaryBuildings) {
    			if (myUnit.getType() == UnitType.Terran_Barracks && myUnit.getTrainingQueue().size() < 1 && ResourcesManager.getCurrentMinerals() >= UnitType.Terran_Marine.mineralPrice()) {
                    myUnit.train(UnitType.Terran_Marine);
                }
    		}

    		Unit worker = WorkersManager.GetWorker();
        	//construct military buildings
    		if (worker != null) {
    			if (ResourcesManager.getCurrentMinerals() >= UnitType.Terran_Command_Center.mineralPrice() && BuildingsManager.CommandCenters.size() < 4) {
    				BaseLocation bl = BuildingsManager.GetClosestEmptyBase(worker);
    				if (bl != null && !BuildingsManager.isTileReserved(bl.getTilePosition(), UnitType.Terran_Command_Center)) {
    					BuildingsManager.BuildingsUnderConstruction.add(new Building(worker, UnitType.Terran_Command_Center, bl.getTilePosition()));
    					worker = null;
    				}
    			}
        		if (worker == null) {
        			worker = WorkersManager.GetWorker();	
        		}
    			if (BuildingsManager.CommandCenters.size() > 1) {
    				// build a barracks if we can afford it
    				if (ResourcesManager.getCurrentMineralsIncludingMilitary() >= UnitType.Terran_Barracks.mineralPrice() && BuildingsManager.BarracksCount < BuildingsManager.CommandCenters.size() * 2.5) {
    					BuildingsManager.BuildingsUnderConstruction.add(new Building(worker, UnitType.Terran_Barracks));
    					BuildingsManager.BarracksCount++;
    					ResourcesManager.MilitaryMineralUnitCost += 50;
    				}						
    			}	
    		}
    		
    	}
    	 
//      
        //draw my units on screen
        //game.drawTextScreen(10, 25, units.toString());
    }
    

    
//    public void storeEnemyBuidlings() {
//    	//always loop over all currently visible enemy units (even though this set is usually empty)
//    	for (Unit u : game.enemy().getUnits()) {
//    		//if this unit is in fact a building
//    		if (u.getType().isBuilding()) {
//    			//check if we have it's position in memory and add it if we don't
//    			if (!enemyBuildingMemory.contains(u.getPosition())) enemyBuildingMemory.add(u.getPosition());
//    		}
//    	}
//
//    	//loop over all the positions that we remember
//    	for (final Position p : enemyBuildingMemory) {
//    		// compute the TilePosition corresponding to our remembered Position p
//    		TilePosition tileCorrespondingToP = new TilePosition(p.getX()/32 , p.getY()/32);
//
//    		//if that tile is currently visible to us...
//    		if (game.isVisible(tileCorrespondingToP)) {
//
//    			//loop over all the visible enemy buildings and find out if at least
//    			//one of them is still at that remembered position
//    			boolean buildingStillThere = false;
//    			for (Unit u : game.enemy().getUnits()) {
//    				if ((u.getType().isBuilding()) && (u.getPosition().equals(p))) {
//    					buildingStillThere = true;
//    					break;
//    				}
//    			}
//
//    			//if there is no more any building, remove that position from our memory
//    			if (buildingStillThere == false) {
//    				enemyBuildingMemory.remove(p);
//    				break;
//    			}else {
//		    		self.getUnits().stream().filter(new Predicate<Unit>() {
//						@Override
//						public boolean test(Unit u) {
//							return !u.isAttacking();
//						}
//					}).forEach(new Consumer<Unit>() {
//						@Override
//						public void accept(Unit attackUnit) {
//							if (attackUnit.getType() == UnitType.Terran_Marine) {
//								attackUnit.attack(p);
//							}
//						}
//					});
//    			}
//    		}
//    	}
//    }
    
    @Override
    public void onEnd(boolean arg0) {
    	ProcessHelper.killStarcraftProcess();
    	ProcessHelper.killChaosLauncherProcess();
    	System.exit(0);
    }
    public static void main(String[] args) {
    	ProcessHelper.killStarcraftProcess();
    	ProcessHelper.killChaosLauncherProcess();
    	ProcessHelper.startChaosLauncherProcess();
        new start().run();
    }
}