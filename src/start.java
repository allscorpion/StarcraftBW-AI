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
import helpers.BuildingsManager;
import helpers.ResourcesManager;
import helpers.ProcessHelper;
import models.Building;

public class start extends DefaultBWListener {

    private Mirror mirror = new Mirror();

    private Game game;

    private Player self;
    
    private BuildingsManager buildingsManager;
    private ResourcesManager resourcesManager;
    private HashSet<Position> enemyBuildingMemory = new HashSet<Position>();
    private List<Unit> Workers = new ArrayList<Unit>();
    private List<Unit> MilitaryUnits = new ArrayList<Unit>();
    private int xTextPos = 10;
    private int yTextPos = 10;
    public void run() {
        mirror.getModule().setEventListener(this);
        mirror.startGame();
    }

    @Override
    public void onStart() {
        game = mirror.getGame();
        self = game.self();
        resourcesManager = new ResourcesManager(game, self);
        buildingsManager = new BuildingsManager(game, self, resourcesManager);
        game.setLocalSpeed(5);
        //Use BWTA to analyze map
        //This may take a few minutes if the map is processed first time!
        //System.out.println("Analyzing map...");
        BWTA.readMap();
        BWTA.analyze();
        //System.out.println("Map data ready");
        //int i = 0;
        
        // reveal entire map
//        game.sendText("black sheep wall");
        
        BaseLocation mySpawn = BWTA.getStartLocation(self);
        for(BaseLocation baseLocation : BWTA.getBaseLocations()){
        	StringBuilder baseText = new StringBuilder(String.valueOf(baseLocation.getGroundDistance(mySpawn)) + "\n");
        	baseText.append(String.valueOf(baseLocation.getAirDistance(mySpawn)));
        	//game.drawTextMap(baseLocation.getPosition().getX(), baseLocation.getPosition().getY(), baseText.toString()); 
        	for(Chokepoint cp: baseLocation.getRegion().getChokepoints()) {
        		//game.drawTextMap(cp.getCenter().getX(), cp.getCenter().getY(), String.valueOf(cp.getWidth())); 
        		//find the closest mineral
        		if (cp.getWidth() < 150) {
                    for (Unit neutralUnit : game.getUnitsInRadius(cp.getPoint(), (int)cp.getWidth())) {
                        if (neutralUnit.getType().isMineralField()) {
                        	buildingsManager.InaccessibleChokepoints.add(cp);
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
    	if (unit.getType() == UnitType.Terran_SCV) {
    		Workers.add(unit);
    	}
		buildingsManager.buildingCreated(unit);
    }

    @Override
    public void onUnitDestroy(Unit unit) {
    	buildingsManager.buildingDestroyed(unit);
    	if (unit.getType() == UnitType.Terran_SCV) {
    		Workers.remove(unit);
    	}
    	else if (unit.getType() == UnitType.Terran_Barracks) {
    		resourcesManager.MilitaryMineralUnitCost -= 50;
		}
    	else if (unit.getType() == UnitType.Terran_Marine) {
    		MilitaryUnits.remove(unit);
    	}
    }
    
    @Override
    public void onUnitComplete(Unit unit) {
    	buildingsManager.buildingComplete(unit);
    	if (unit.getType() == UnitType.Terran_Marine) {
    		MilitaryUnits.add(unit);
    		//unit.attack(new Position(GetClosestEmptyBase(unit).getPosition().getX(), GetClosestEmptyBase(unit).getPosition().getY() + 20));
    	}
    }
    
    @Override
    public void onFrame() {
    	xTextPos = 10;
        yTextPos = 10;
        
        //game.setTextSize(10);
//        drawTextOnScreen("Workers " + Workers.size());
//        drawTextOnScreen("Command Centers " + CommandCenters.size());
        //drawTextOnScreen("Military Buildings " + MilitaryBuildings.size());
        // game.drawTextScreen(10, 10, "" + getCurrentMinerals());
    	drawTextOnScreen("Current Minerals " + String.valueOf(resourcesManager.getCurrentMinerals()));
    	drawTextOnScreen("MineralsInReserve " + String.valueOf(resourcesManager.MineralsInReserve));
    	drawTextOnScreen("BuildingsUnderConstruction " + String.valueOf(buildingsManager.BuildingsUnderConstruction.size()));
    	
    	if (game.enemy().getUnits().size() > 0) {
			for (Unit attackUnit : MilitaryUnits) {
				if (attackUnit.getType() == UnitType.Terran_Marine && attackUnit.isIdle()) {
					for (Unit u : game.enemy().getUnits()) {
						if (u.getType().canAttack()) {
							attackUnit.attack(u);							
						}
			    	}
				}
			}
		} else {
			if (self.allUnitCount(UnitType.Terran_Marine) < 50) {
//				for (Unit attackUnit : MilitaryUnits) {
//					Position rallyLocation = new Position(GetClosestEmptyBase().getPosition().getX(), GetClosestEmptyBase().getPosition().getY() + 20);
//					if (attackUnit.getPosition().getDistance(rallyLocation) > 10) {
//						attackUnit.attack(rallyLocation);
//					}
//				} 
			} else {
	    		for (Unit attackUnit : MilitaryUnits) {
	    			if (attackUnit.isIdle()) {
	    				for (BaseLocation b : BWTA.getBaseLocations()) {
	            			// If this is a possible start location,
	            			if (b.isStartLocation() && b.getTilePosition().getDistance(self.getStartLocation()) > 0) {
	            				// do something. For example send some unit to attack that position:
	            				attackUnit.attack(b.getPosition());
	            			}
	            		}	
	    			}
				}
			}
	    	
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
    	buildingsManager.CheckBuildingProgress();
    	SendIdleWorkersToMinerals();
    	boolean shouldBuildDepo = resourcesManager.isDepoRequired();
    	drawTextOnScreen("shouldBuildDepo " + String.valueOf(shouldBuildDepo));
    	//build depos
    	buildingsManager.GetClosestEmptyBase(null);
    	if (shouldBuildDepo) {
    		for (Unit myUnit : Workers) {
    			// get money back to build depos faster
//    			if (myUnit.getType().isBuilding() && myUnit.isTraining() && myUnit.getTrainingQueue().size() > 1) {
//                    myUnit.cancelTrain();
//                }
    			// check if we still need more depos 
    			// we don't want to build multiple depos if we dont need to
    			if (resourcesManager.isDepoRequired()) {
    				if (myUnit.canBuild()) {
    					// build a supply depot if we need one and have the money to build
    					if (resourcesManager.getCurrentMinerals() >= UnitType.Terran_Supply_Depot.mineralPrice()) {
    						buildingsManager.BuildingsUnderConstruction.add(new Building(game, self, buildingsManager, resourcesManager, myUnit, UnitType.Terran_Supply_Depot));
    					}
    				}	
    			}
    		}	
    	} else {
    		//build units
    		int maxWorkers = Math.min(70, buildingsManager.CommandCenters.size() * 20);
    		for (Unit myUnit : buildingsManager.CommandCenters) {
    			if (myUnit.getTrainingQueue().size() < 2 && resourcesManager.getCurrentMinerals() >= UnitType.Terran_SCV.mineralPrice() && Workers.size() < maxWorkers) {
                    myUnit.train(UnitType.Terran_SCV);
                }
    		}
    		for (Unit myUnit : buildingsManager.MilitaryBuildings) {
    			if (myUnit.getType() == UnitType.Terran_Barracks && myUnit.getTrainingQueue().size() < 1 && resourcesManager.getCurrentMinerals() >= UnitType.Terran_Marine.mineralPrice()) {
                    myUnit.train(UnitType.Terran_Marine);
                }
    		}
            
        	//construct military buildings
        	for (Unit myUnit : Workers) {
				if (myUnit.canBuild()) {
					if (resourcesManager.getCurrentMinerals() >= UnitType.Terran_Command_Center.mineralPrice() && buildingsManager.CommandCenters.size() < 6) {
						BaseLocation bl = buildingsManager.GetClosestEmptyBase(myUnit);
						if (bl != null) {
							buildingsManager.BuildingsUnderConstruction.add(new Building(game, self, buildingsManager, resourcesManager, myUnit, UnitType.Terran_Command_Center, bl.getTilePosition()));
							break;
						}
					}
					if (buildingsManager.CommandCenters.size() > 1) {
						// build a barracks if we can afford it
						if (resourcesManager.getCurrentMineralsIncludingMilitary() >= UnitType.Terran_Barracks.mineralPrice() && buildingsManager.BaracksCount < buildingsManager.CommandCenters.size() * 2.5) {
							buildingsManager.BuildingsUnderConstruction.add(new Building(game, self, buildingsManager, resourcesManager, myUnit, UnitType.Terran_Barracks));
							buildingsManager.BaracksCount++;
						}						
					}
				}
    		} 
    	}
    	 
//      
        //draw my units on screen
        //game.drawTextScreen(10, 25, units.toString());
    }
    
    public void drawTextOnUnit(Unit myUnit, String text) {
    	game.drawTextMap(myUnit.getPosition().getX(), myUnit.getPosition().getY(), text);
    }
    
    public void drawTextOnScreen(String text) {
    	game.drawTextScreen(xTextPos, yTextPos, text);
    	yTextPos += 20;
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
    
    public void SendIdleWorkersToMinerals() {
    	for (Unit myUnit : Workers) {
    		if (myUnit.isIdle()) {
                Unit closestMineral = null;

                //find the closest mineral
                for (Unit neutralUnit : game.neutral().getUnits()) {
                    if (neutralUnit.getType().isMineralField()) {
                        if (closestMineral == null || myUnit.getDistance(neutralUnit) < myUnit.getDistance(closestMineral)) {
                            closestMineral = neutralUnit;
                        }
                    }
                }

                //if a mineral patch was found, send the worker to gather it
                if (closestMineral != null) {
                    myUnit.gather(closestMineral, false);
                }
            }
    	}
    }
    
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