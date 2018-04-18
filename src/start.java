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
import helpers.BaseManager;
import helpers.BuildingsManager;
import helpers.ConstructionManager;
import helpers.DrawingHelper;
import helpers.MapAnalyser;
import helpers.MiningHelper;
import helpers.PathingManager;
import helpers.ResourcesManager;
import helpers.ProcessHelper;
import helpers.WorkersManager;
import helpers.UnitsManager;
import helpers.ScoutsManager;
import models.Building;
import models.CommandCenter;
import models.CustomBaseLocation;
import models.MilitaryUnit;
import models.ReservedTile;
import models.Worker;

public class start extends DefaultBWListener {
	
    private static Mirror mirror  = new Mirror();
    private static boolean scoutSent;
    public void run() {
		mirror.getModule().setEventListener(this);
        mirror.startGame();	
    }

    @Override
    public void onStart() {
    	scoutSent = false;
    	StarCraftInstance.Start(mirror);
        StarCraftInstance.game.setLocalSpeed(15);
        //Use BWTA to analyze map
        //This may take a few minutes if the map is processed first time!
        //System.out.println("Analyzing map...");
        BWTA.readMap();
        BWTA.analyze();
        
        Commander.Init();
        
        //System.out.println("Map data ready");
        //int i = 0;
        
        // reveal entire map
      //StarCraftInstance.game.sendText("black sheep wall");
        // 10,000 Minerals and Gas
        //StarCraftInstance.game.sendText("show me the money");
        StarCraftInstance.game.enableFlag(1);
        BaseLocation mySpawn = BaseManager.mySpawn;
        for(CustomBaseLocation cbl : BaseManager.baseLocations){
        	// StringBuilder baseText = new StringBuilder(String.valueOf(baseLocation.getGroundDistance(mySpawn)) + "\n");
        	//baseText.append(String.valueOf(baseLocation.getAirDistance(mySpawn)));
        	//game.drawTextMap(baseLocation.getPosition().getX(), baseLocation.getPosition().getY(), baseText.toString()); 
//        	for(Chokepoint cp: cbl.baseLocation.getRegion().getChokepoints()) {
//        		//game.drawTextMap(cp.getCenter().getX(), cp.getCenter().getY(), String.valueOf(cp.getWidth())); 
//        		//find the closest mineral
//        		if (cp.getWidth() < 150) {
//                    for (Unit neutralUnit : StarCraftInstance.game.getUnitsInRadius(cp.getPoint(), (int)cp.getWidth())) {
//                        if (neutralUnit.getType().isMineralField()) {
//                        	BuildingsManager.InaccessibleChokepoints.add(cp);
//                        	//game.drawCircleMap(cp.getPoint().getX(), cp.getPoint().getY(), (int)cp.getWidth(), Color.Green);
//                        	break;
//                        }
//                    }        			
//        		}
//        	}
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
    public void onUnitMorph(Unit unit) {
    	if (!isMyUnit(unit)) return;
    	if (unit.getType() == UnitType.Terran_Refinery) {
    		BuildingsManager.buildingCreated(unit);	
    	}
    }
    
    
    @Override
    public void onUnitCreate(Unit unit) {
    	if (!isMyUnit(unit)) return;
    	StarCraftInstance.allMyUnits.add(unit);
		WorkersManager.onWorkerCreate(unit);
		BuildingsManager.buildingCreated(unit);	
    }

    @Override
    public void onUnitDestroy(Unit unit) {
    	if (unit.getType() == UnitType.Resource_Mineral_Field) {
    		boolean test = true;
    	}
    	if (!StarCraftInstance.allMyUnits.contains(unit)) return;
    	StarCraftInstance.allMyUnits.remove(unit);
    	BuildingsManager.buildingDestroyed(unit);
    	WorkersManager.onWorkerDestroy(unit);
    	UnitsManager.onUnitDestroy(unit);
    }
    
    @Override
    public void onUnitComplete(Unit unit) {
    	if (!isMyUnit(unit)) return;
    	BuildingsManager.buildingComplete(unit);
    	UnitsManager.onUnitComplete(unit);
    }
    
    @Override
    public void onFrame() {
    	
    	/* 
    	 * 
    	 * 
    	 * DEBUG STARTING 
    	 * 
    	 * 
    	 * 
    	 * */
    	
    	
    	DrawingHelper.resetTextPos();
        
        //game.setTextSize(10);
    	StarCraftInstance.game.drawTextScreen(450, 20, String.valueOf(ResourcesManager.getCurrentMinerals()));
    	StarCraftInstance.game.drawTextScreen(560, 20, "Supply " + StarCraftInstance.self.supplyUsed() / 2 + "/" + ResourcesManager.PotentialSupply / 2);
    	StarCraftInstance.game.drawTextScreen(560, 40, "APM " + StarCraftInstance.game.getAPM());
        //DrawingHelper.drawTextOnScreen("Workers " + WorkersManager.Workers.size());
    	DrawingHelper.drawTextOnScreen("Game time " + StarCraftInstance.game.elapsedTime() / 60 + ":" + (StarCraftInstance.game.elapsedTime() % 60 < 10 ? "0": "") + StarCraftInstance.game.elapsedTime() % 60);
    	DrawingHelper.drawTextOnScreen("Frames " + StarCraftInstance.game.getFrameCount());
        DrawingHelper.drawTextOnScreen("Amount of enemy buildings scouted " + BuildingsManager.enemyBuildingMemory.size());
        
        
//        drawTextOnScreen("Command Centers " + CommandCenters.size());
        //drawTextOnScreen("Military Buildings " + MilitaryBuildings.size());
        // game.drawTextScreen(10, 10, "" + getCurrentMinerals());
//    	DrawingHelper.drawTextOnScreen("Current Minerals " + String.valueOf(ResourcesManager.getCurrentMinerals()));
//    	DrawingHelper.drawTextOnScreen("MineralsInReserve " + String.valueOf(ResourcesManager.MineralsInReserve));
    	// DrawingHelper.drawTextOnScreen("BuildingsUnderConstruction " + String.valueOf(BuildingsManager.BuildingsUnderConstruction.size()));
    	for (CustomBaseLocation cbl : BaseManager.baseLocations) {
//    		StarCraftInstance.game.drawTextMap(cbl.baseLocation.getPosition(), String.valueOf(cbl.hashCode()));
//    		for (Chokepoint c: cbl.baseLocation.getRegion().getChokepoints()) {
//    			StarCraftInstance.game.drawTextMap(c.getCenter(), String.valueOf(cbl.hashCode()));
//    		}
    		//DrawingHelper.drawTextOnScreen(String.valueOf(cbl.baseLocation.getPosition()));
    		//BaseManager.GetAmountOfWorkersAssignedToCommandCenter(cbl);
//    		for(Position position : cbl.baseLocation.getRegion().getPolygon().getPoints()){
//        		StarCraftInstance.game.drawBoxMap(position, new Position(position.getX() + 10, position.getY() + 10), Color.Grey);
//	    	}
    	}
    	
    	for (Building b : BuildingsManager.BuildingsUnderConstruction) {
    		if (b._buildingReservedPosition != null && b._buildingType != null) {
    			DrawingHelper.drawTextOnScreen(b._buildingType + " is " + ((b._structure != null) ? "building" : "starting") + " at " + b._buildingReservedPosition.tilePositionTopLeft);
    		}
    		if (b._builder != null && b._buildingReservedPosition != null) {
    			StringBuilder debugDetails = new StringBuilder(String.valueOf(b._buildingReservedPosition.tilePositionTopLeft + "\n"));
    			debugDetails.append(String.valueOf(b._builder.unit.canBuild()) + "\n");
        		debugDetails.append(String.valueOf(b._builder.unit.isConstructing()) + "\n");
        		DrawingHelper.drawTextAt(b._buildingReservedPosition.tilePositionTopLeft.toPosition(), debugDetails.toString());	
    			StringBuilder builderDetails = new StringBuilder("Can build " + String.valueOf(b._builder.unit.canBuild() + "\n"));
    			builderDetails.append("Constructing " + b._builder.unit.isConstructing());
    			DrawingHelper.drawTextOnUnit(b._builder.unit, builderDetails.toString());
    		}
    	}
    	
    	//PathingManager.FindPath(BaseManager.mySpawn.getPosition(), new Position(0, 0));
    	
    	/* 
    	 * 
    	 * 
    	 * DEBUG ENDING 
    	 * 
    	 * 
    	 * 
    	 * */
    	
    	//MapAnalyser.ScanMap();
    	UnitsManager.attackUnits();	
    	
    	if (!scoutSent && StarCraftInstance.self.supplyUsed() / 2 >= 8) {
    		scoutSent = true;
    		Worker w = WorkersManager.GetWorker();
    		w.isScout = true;
    		w.miningFrom = null;
    		ScoutsManager.ScoutEnemyBase(w.unit);
    	}
    	
    	for (ReservedTile rt : BuildingsManager.ReservedTiles) {
    		StarCraftInstance.game.drawBoxMap(rt.tilePositionTopLeft.toPosition(), rt.tilePositionBottomRight.toPosition(), Color.Red);
    	}
    	
    	for (Building b : BuildingsManager.BuildingsUnderConstruction) {
    		if (b._builder != null) {
    			StarCraftInstance.game.drawLineMap(b._builder.unit.getPosition(), b._builder.unit.getOrderTargetPosition(), Color.Black);	
    		}
    	}
    	
    	for (MilitaryUnit mu : UnitsManager.MilitaryUnits) {
    		if (!mu.unit.isIdle()) {
    			StarCraftInstance.game.drawLineMap(mu.unit.getPosition(), mu.unit.getOrderTargetPosition(), Color.Black);	
    		}
    		//DrawingHelper.drawTextOnUnit(mu.unit, String.valueOf(mu.LastOrderFrame));
//    		StarCraftInstance.game.drawCircleMap(mu.unit.getPosition(), UnitType.Terran_Marine.groundWeapon().maxRange(), Color.Green);
//    		StarCraftInstance.game.drawCircleMap(mu.unit.getPosition(), (int) (UnitType.Terran_Marine.groundWeapon().maxRange() * 0.66), Color.Red);
//    		Position unitPos = mu.unit.getPosition();
//    		StarCraftInstance.game.drawBoxMap(
//    				new Position (unitPos.getX() - (mu.unit.getType().width() / 2),
//    						unitPos.getY() - (mu.unit.getType().height() / 2)), 
//				new Position(
//						unitPos.getX() + mu.unit.getType().width(), 
//						unitPos.getY() + mu.unit.getType().height()
//				), 
//				Color.Red
//			);
    	} 
    	
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
    	BuildingsManager.storeEnemyBuidlings();
    	//BaseManager.TransferAdditionalWorkersToFreeBase();
    	//DrawingHelper.drawTextOnScreen("shouldBuildDepo " + String.valueOf(ResourcesManager.isDepoRequired()));
    	//build depos
    	//BuildingsManager.GetClosestEmptyBase(null);
    	
    	ConstructionManager.StartConstructionQueue();
    	if (BuildingsManager.Academy != null) {
			if (!BuildingsManager.Academy.isResearching()) {
				if (BuildingsManager.Academy.canResearch(TechType.Stim_Packs)) {
    				BuildingsManager.Academy.research(TechType.Stim_Packs);
    			}else if (BuildingsManager.Academy.canUpgrade(UpgradeType.U_238_Shells)) {
    				BuildingsManager.Academy.upgrade(UpgradeType.U_238_Shells);
    			}
			}
		}
//    	if (ResourcesManager.isDepoRequired()) {
//    		while (ResourcesManager.isDepoRequired() && ResourcesManager.getCurrentMinerals() >= UnitType.Terran_Supply_Depot.mineralPrice() && WorkersManager.GetWorker() != null) {
//        		BuildingsManager.BuildingsUnderConstruction.add(new Building(WorkersManager.GetWorker(), UnitType.Terran_Supply_Depot));
//    		}	
//    	} else {
//    		//build units
//        	for (CustomBaseLocation cbl : BaseManager.baseLocations) {
//        		if (cbl.commandCenter != null) {
//        			if (BuildingsManager.Academy != null && cbl.commandCenter.unit.getAddon() == null && StarCraftInstance.game.canMake(UnitType.Terran_Comsat_Station)) {
//        				cbl.commandCenter.unit.buildAddon(UnitType.Terran_Comsat_Station);
//        			}else {
//            			// keep constant scv production if we can afford it
//        				if (ResourcesManager.getCurrentMinerals() >= UnitType.Terran_SCV.mineralPrice()) {
//            				if (cbl.commandCenter.unit.getTrainingQueue().size() < 2 && WorkersManager.Workers.size() < BaseManager.TotalWorkersAllCommandCenters() - BaseManager.GetTotalAmountOfCommandCenters()) {
//            					cbl.commandCenter.unit.train(UnitType.Terran_SCV);
//            				}
//                        }	
//        			}
//        			//&& BaseManager.GetTotalAmountOfCommandCenters() > 1
//        			if (BuildingsManager.BarracksCount > 1 && cbl.baseLocation.getGeysers().size() > 0 && (BaseManager.GetAmountOfWorkersAssignedToCommandCenter(cbl) >= BaseManager.GetCommandCenterMaxWorkers(cbl) / 2) && !cbl.commandCenter.hasGasStructure && ResourcesManager.getCurrentMinerals() >= UnitType.Terran_Refinery.mineralPrice() ) {
//        				cbl.commandCenter.hasGasStructure = true;
//        				BuildingsManager.BuildingsUnderConstruction.add(new Building(WorkersManager.GetWorker(), UnitType.Terran_Refinery));
//        			}
//        		}
//        	}
//    		
//    		if (BuildingsManager.Academy != null) {
//    			if (!BuildingsManager.Academy.isResearching()) {
//    				if (BuildingsManager.Academy.canResearch(TechType.Stim_Packs)) {
//        				BuildingsManager.Academy.research(TechType.Stim_Packs);
//        			}else if (BuildingsManager.Academy.canUpgrade(UpgradeType.U_238_Shells)) {
//        				BuildingsManager.Academy.upgrade(UpgradeType.U_238_Shells);
//        			}
//    			}
//    		}
//    		
//    		//Worker worker = WorkersManager.GetWorker();
////    			if (ResourcesManager.getCurrentMinerals() >= UnitType.Terran_Command_Center.mineralPrice() && BaseManager.GetTotalAmountOfCommandCenters() < 4) {
////    				BaseLocation bl = BuildingsManager.GetClosestEmptyBase(worker.unit);
////    				if (bl != null && !BuildingsManager.isTileReserved(bl.getTilePosition(), UnitType.Terran_Command_Center)) {
////    					BuildingsManager.BuildingsUnderConstruction.add(new Building(worker, UnitType.Terran_Command_Center, bl.getTilePosition()));
////    					worker.miningFrom = null;
////    					worker = null;
////    				}
////    			}
//    		
//    	}
    	 
//      
        //draw my units on screen
        //game.drawTextScreen(10, 25, units.toString());
    }
    
    private boolean isMyUnit (Unit unit) {
    	boolean isMyUnit = false;
    	for (Unit u : StarCraftInstance.self.getUnits()) {
    		if (u.getID() == unit.getID()) {
    			isMyUnit = true;
    			break;
    		}
    	}
    	return isMyUnit;
    }
    
    @Override
    public void onEnd(boolean arg0) {
    	//StarCraftInstance.game.restartGame();
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