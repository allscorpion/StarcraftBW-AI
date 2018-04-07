package helpers;

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

public class units extends DefaultBWListener {

    private Mirror mirror = new Mirror();

    private Game game;

    private Player self;
    
    private int MineralsInReserve = 0;
    private int MilitaryMineralUnitCost = 0;
    private int PotentialSupply = 0;
    private HashSet<TilePosition> ReservedTiles = new HashSet<TilePosition>();
    private int BaracksCount = 0;
    private List<Building> BuildingsUnderConstruction = new ArrayList<Building>();
    private HashSet<Position> enemyBuildingMemory = new HashSet<Position>();
    private List<Unit> Workers = new ArrayList<Unit>();
    private List<Unit> CommandCenters = new ArrayList<Unit>();
    private List<Unit> MilitaryBuildings = new ArrayList<Unit>();
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
        game.setLocalSpeed(5);
        //Use BWTA to analyze map
        //This may take a few minutes if the map is processed first time!
        //System.out.println("Analyzing map...");
        BWTA.readMap();
        BWTA.analyze();
        //System.out.println("Map data ready");
        PotentialSupply = self.supplyTotal();
        //int i = 0;
//        for(BaseLocation baseLocation : BWTA.getBaseLocations()){
//        	System.out.println("Base location #" + (++i) + ". Printing location's region polygon:");
//        	for(Position position : baseLocation.getRegion().getPolygon().getPoints()){
//        		System.out.print(position + ", ");
//        	}
//        	System.out.println();
//        }
        //GetClosestEmptyBase();
    }
    
    @Override
    public void onUnitDiscover(Unit unit) {
    	
    }
    
    @Override
    public void onUnitCreate(Unit unit) {
    	if (unit.getType() == UnitType.Terran_SCV) {
    		Workers.add(unit);
    	}
    	else if (unit.getType().isBuilding()) {
    		if (unit.getType() == UnitType.Terran_Supply_Depot) {
    			//game.printf("Created depo at " + (self.supplyUsed() / 2) + " Supply");	
    		}
    		else if (unit.getType() == UnitType.Terran_Command_Center) {
        		CommandCenters.add(unit);
        	}
        	else if (unit.getType() == UnitType.Terran_Barracks) {
        		MilitaryBuildings.add(unit);
    			MilitaryMineralUnitCost += 50;
    		}
    		if (BuildingsUnderConstruction.size() > 0) {
    			Building constructingBuidling = GetBuildingFromUnit(unit);
        		if (constructingBuidling == null) {
        			game.printf("Unable to find building");
        		}else {
        			constructingBuidling._structure = unit;
        			BuildingStartedConstruction(constructingBuidling);
        		}	
    		}
    	}
    }

    @Override
    public void onUnitDestroy(Unit unit) {
    	if (unit.getType() == UnitType.Terran_SCV) {
    		Workers.remove(unit);
    	}
    	else if (unit.getType() == UnitType.Terran_Command_Center) {
    		CommandCenters.remove(unit);
    	}
    	else if (unit.getType() == UnitType.Terran_Barracks) {
    		MilitaryBuildings.remove(unit);
			MilitaryMineralUnitCost -= 50;
		}
    	else if (unit.getType() == UnitType.Terran_Marine) {
    		MilitaryUnits.remove(unit);
    	}
    }
    
    @Override
    public void onUnitComplete(Unit unit) {
    	if (unit.getType().isBuilding()) {
    		if (BuildingsUnderConstruction.size() > 0) {
    			Building finishedBuilding = GetBuildingFromUnit(unit);
        		if (finishedBuilding == null) {
        			game.printf("Unable to find building");
        		}else {
        			BuildingFinishedConstruction(finishedBuilding);
        		}	
    		}
    	}
    	if (unit.getType() == UnitType.Terran_Marine) {
    		MilitaryUnits.add(unit);
    		unit.attack(new Position(GetClosestEmptyBase(unit).getPosition().getX(), GetClosestEmptyBase(unit).getPosition().getY() + 20));
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
    	drawTextOnScreen("Current Minerals " + String.valueOf(getCurrentMinerals()));
    	drawTextOnScreen("MineralsInReserve " + String.valueOf(MineralsInReserve));
    	drawTextOnScreen("BuildingsUnderConstruction " + String.valueOf(BuildingsUnderConstruction.size()));
    	
    	if (game.enemy().getUnits().size() > 0) {
			for (Unit attackUnit : MilitaryUnits) {
				if (attackUnit.getType() == UnitType.Terran_Marine && attackUnit.isIdle()) {
					for (Unit u : game.enemy().getUnits()) {
						attackUnit.attack(u);
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
    	CheckBuildingProgress();
    	SendIdleWorkersToMinerals();
    	boolean shouldBuildDepo = isDepoRequired();
    	drawTextOnScreen("shouldBuildDepo " + String.valueOf(shouldBuildDepo));
    	//build depos
    	
    	if (shouldBuildDepo) {
    		for (Unit myUnit : Workers) {
    			// get money back to build depos faster
//    			if (myUnit.getType().isBuilding() && myUnit.isTraining() && myUnit.getTrainingQueue().size() > 1) {
//                    myUnit.cancelTrain();
//                }
    			// check if we still need more depos 
    			// we don't want to build multiple depos if we dont need to
    			if (isDepoRequired()) {
    				if (myUnit.canBuild()) {
    					// build a supply depot if we need one and have the money to build
    					if (getCurrentMinerals() >= UnitType.Terran_Supply_Depot.mineralPrice()) {
    						BuildingsUnderConstruction.add(new Building(myUnit, UnitType.Terran_Supply_Depot));
    					}
    				}	
    			}
    		}	
    	} else {
    		//build units
    		int maxWorkers = Math.min(70, CommandCenters.size() * 20);
    		for (Unit myUnit : CommandCenters) {
    			if (myUnit.getTrainingQueue().size() < 2 && getCurrentMinerals() >= UnitType.Terran_SCV.mineralPrice() && Workers.size() < maxWorkers) {
                    myUnit.train(UnitType.Terran_SCV);
                }
    		}
    		for (Unit myUnit : MilitaryBuildings) {
    			if (myUnit.getType() == UnitType.Terran_Barracks && myUnit.getTrainingQueue().size() < 1 && getCurrentMinerals() >= UnitType.Terran_Marine.mineralPrice()) {
                    myUnit.train(UnitType.Terran_Marine);
                }
    		}
            
        	//construct military buildings
        	for (Unit myUnit : Workers) {
				if (myUnit.canBuild()) {
					if (getCurrentMinerals() >= UnitType.Terran_Command_Center.mineralPrice() && CommandCenters.size() < 4) {
						BaseLocation bl = GetClosestEmptyBase(myUnit);
						if (bl != null) {
							BuildingsUnderConstruction.add(new Building(myUnit, UnitType.Terran_Command_Center, bl.getTilePosition()));
							break;
						}
					}
					if (CommandCenters.size() > 3) {
						// build a barracks if we can afford it
						if (getCurrentMineralsIncludingMilitary() >= UnitType.Terran_Barracks.mineralPrice() && BaracksCount < CommandCenters.size() * 3) {
							BuildingsUnderConstruction.add(new Building(myUnit, UnitType.Terran_Barracks));
							BaracksCount++;
						}						
					}
				}
    		} 
    	}
    	 
//      
        //draw my units on screen
        //game.drawTextScreen(10, 25, units.toString());
    }
    
    public int getCurrentMinerals() {
    	int total = self.minerals() - MineralsInReserve;
    	return total;
    }
    
    // so that we don't build any additional buildings without producing out of our current structures
    public int getCurrentMineralsIncludingMilitary() {
    	int total = self.minerals() - MineralsInReserve - MilitaryMineralUnitCost;
    	return total;
    }
    
    public boolean isDepoRequired() {
    	if (self.supplyTotal() / 2 == 200) return false;
    	int amountOfFreeSupply = PotentialSupply - self.supplyUsed();
    	int amountOfSupplyQueued = 0;
    	
    	for (Unit myUnit : self.getUnits()) {
    		if (myUnit.getType().isBuilding() && myUnit.isTraining()) {
    			amountOfSupplyQueued += myUnit.getTrainingQueue().get(0).supplyRequired();
    		}
    	} 
//    	drawTextOnScreen("amountOfFreeSupply " + String.valueOf(amountOfFreeSupply / 2));
//    	drawTextOnScreen("amountOfSupplyQueued " + String.valueOf(amountOfSupplyQueued / 2));
    	if (amountOfSupplyQueued > 0) {
    		int productionCyclesLeft = (amountOfFreeSupply - amountOfSupplyQueued) / amountOfSupplyQueued;
        	//drawTextOnScreen("productionCyclesLeft " + String.valueOf(productionCyclesLeft));
    		return productionCyclesLeft < 2 || amountOfFreeSupply <= 0;
    	}
		return amountOfFreeSupply <= 0;
    }
    
 // Returns a suitable TilePosition to build a given building type near
 // specified TilePosition aroundTile, or null if not found. (builder parameter is our worker)
    public TilePosition getBuildTile(Unit builder, UnitType buildingType, TilePosition aroundTile) {
	 	TilePosition ret = null;
	 	int maxDist = 3;
	 	int stopDist = 40;
	
	 	// Refinery, Assimilator, Extractor
	 	if (buildingType.isRefinery()) {
	 		for (Unit n : game.neutral().getUnits()) {
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
	 				if (!isTileReserved(availablePosition, buildingType) && game.canBuildHere(availablePosition, buildingType, builder, false)) {
	 					// units that are blocking the tile
	 					boolean unitsInWay = false;
	 					for (Unit u : game.getAllUnits()) {
	 						if (u.getID() == builder.getID()) continue;
	 						if ((Math.abs(u.getTilePosition().getX()-i) < 4) && (Math.abs(u.getTilePosition().getY()-j) < 4)) unitsInWay = true;
	 					}
	 					if (!unitsInWay) {
	 						// game.sendText("i: " + i + " j: " + j);
							ReservedTiles.add(availablePosition);
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
	 		game.printf("Unable to find suitable build position for "+ buildingType.toString());	
	 	};
	 	return ret;
	 }
    
    public boolean isTileReserved(TilePosition position, UnitType buildingType) {
    	boolean isReserved = false;
    	for (TilePosition reservedPosition : ReservedTiles) {
    		if (reservedPosition.getDistance(position) <= Math.max(buildingType.tileSize().getX(), buildingType.tileSize().getY())) {
    			isReserved = true;
    			break;
    		}
		}
    	return isReserved;
    }
    
    public void CheckBuildingProgress() {
    	for (Building building : BuildingsUnderConstruction) {
    		if (building._builder.canBuild() && !building._isConstructing) {
    			// build failed
    			building.RestartBuild();
    		}
		}
    }
    
    public Building GetBuildingFromUnit(Unit unit) {
    	for (Building building : BuildingsUnderConstruction) {
    		if (unit.getTilePosition().getDistance(building._buildingReservedPosition) == 0) {
    			return building;	
    		}
		}	
    	return null;		
    }
    
    public void BuildingStartedConstruction(Building building) {
    	building._isConstructing = true;
		MineralsInReserve -= building._buildingType.mineralPrice();
		RemoveBuildingReservedPosition(building);
    }
    
    public void RemoveBuildingReservedPosition(Building building) {
    	for (TilePosition position : ReservedTiles) {
			if (position.getDistance(building._buildingReservedPosition) == 0)  {
				ReservedTiles.remove(position);
				break;
			}
		}
    }
    
    public void BuildingFinishedConstruction(Building building) {
		BuildingsUnderConstruction.remove(building);
    }
    
    public class Building {
    	public Building(Unit builder, UnitType buildingType) {
    		Init(builder, buildingType);
    		SetBuildingPosition();
    		ConstructBuilding();
    	}
    	public Building(Unit builder, UnitType buildingType, TilePosition position) {
    		Init(builder, buildingType);
    		SetBuildingPosition(position);
    		ConstructBuilding();
    	}
    	public void Init(Unit builder, UnitType buildingType) {
    		_builder = builder;
    		_buildingType = buildingType;
    		_isConstructing = false;
    		_isBuilderMoving = false;
    		PotentialSupply += _buildingType.supplyProvided();
			MineralsInReserve += _buildingType.mineralPrice();
    	}
    	public void GetNewBuilder() {
    		if (_builder == null) {
    			// get new worker
    			game.printf("Unable to find worker for " + _buildingType);
    			for (Unit worker : Workers) {
    				if (worker.canBuild()) {
    					_builder = worker;
    				}
				}
    		}
    	}
    	public void SetBuildingPosition() {
			_buildingReservedPosition = getBuildTile(_builder, _buildingType, _builder.getTilePosition());	
    	}
    	public void SetBuildingPosition(TilePosition position) {
			_buildingReservedPosition = position;	
    	}
    	public void RestartBuild() {
    		GetNewBuilder();
    		if (_structure != null) {
				_builder.repair(_structure);
    		}else {
    			if (!_isBuilderMoving) {
        			RemoveBuildingReservedPosition(this);
        			SetBuildingPosition();	
        		}
    			ConstructBuilding();
    		}
    	}
    	public void ConstructBuilding() {
    		if (_buildingReservedPosition != null) {
    			if (_builder.canBuild(_buildingType, _buildingReservedPosition)) {
        			if (_builder.build(_buildingType, _buildingReservedPosition)) {
        				_isBuilderMoving = false;		
        			}
    			}else if (!_isBuilderMoving){
    				_isBuilderMoving = true;
    				_builder.move(_buildingReservedPosition.toPosition());
    			}
    		}
    	}
    	public Unit _builder;
    	public Unit _structure;
    	public UnitType _buildingType;
    	public TilePosition _buildingReservedPosition;
    	public boolean _isConstructing;
    	public boolean _isBuilderMoving;
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

	public BaseLocation GetClosestEmptyBase(Unit unit) {
    	BaseLocation mySpawn = BWTA.getStartLocation(self);
        BaseLocation closestBase = null;
        List<Position> baseLocationsTaken = new ArrayList<Position>();
        for (Unit commandCenter : CommandCenters) {
        	baseLocationsTaken.add(commandCenter.getPosition());
        }
        for (final BaseLocation baseLocation : BWTA.getBaseLocations()) {
			if (!baseLocationsTaken.contains(baseLocation.getPosition()) && mySpawn.getRegion().isReachable(baseLocation.getRegion()) && !baseLocation.isIsland()) 
			{
				if (closestBase == null) {
					closestBase = baseLocation;
				}
				else if (baseLocation.getGroundDistance(mySpawn) < closestBase.getGroundDistance(mySpawn)) {
					closestBase = baseLocation;	
				}
			}
		}
        return closestBase;
    }
    
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
    
    public static void main(String[] args) {
        new units().run();
    }
}