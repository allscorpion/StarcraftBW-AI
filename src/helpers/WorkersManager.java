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
import models.Building;
import models.Mineral;
import models.Worker;

public class WorkersManager {
	
    public static List<Worker> Workers = new ArrayList<Worker>();
    
    public static void onWorkerCreate(Unit unit) {
    	if (unit.getType() == UnitType.Terran_SCV) {
    		Workers.add(new Worker(unit));
    	}
    }
    
    public static Worker GetWorkerFromUnit(Unit unit) {
    	for (Worker w : Workers) {
			if (w.unit.getID() == unit.getID()) {
    			return w;	
    		}	
		}	
    	return null;		
    }

    public static void onWorkerDestroy(Unit unit) {
    	if (unit.getType() == UnitType.Terran_SCV) {
    		Worker w = GetWorkerFromUnit(unit);
    		if (w != null) {
        		if (w.isBuilding) {
        			Building workerBuilding = BuildingsManager.GetBuildingFromWorker(w.unit);
            		if (workerBuilding != null) {
            			workerBuilding._builder = null;	
            		}else {
            			DrawingHelper.writeTextMessage("Unable to find building from dead worker");
            		}
        		}	
        		Workers.remove(w);
    		}
    	}
    }
   
    public static Worker GetWorker() {
    	for (Worker w : Workers) {
			if (!w.isBuilding && !w.isScout) {
				return w;
			}
		}	
    	return null;
    }
    
    public static void SendIdleWorkersToMinerals(Position position) {
    	List<Unit> mineralFieldsSelected = new ArrayList<Unit>();
    	for (Worker w : Workers) {
    		Unit myUnit = w.unit;
    		if (myUnit.isIdle() && myUnit.canMove()) {
                Unit closestMineral = null;

                //find the closest mineral
                for (Unit mineralField : StarCraftInstance.game.getUnitsInRadius(position, 300)) {
                    if (mineralField.getType().isMineralField()) {
                    	Mineral m = MineralsHelper.GetMineralFromUnit(mineralField);
                    	if (m != null && m.amountOfWorkersAssignedToMineral >= 2) continue;
                        if ((closestMineral == null || myUnit.getDistance(mineralField) < myUnit.getDistance(closestMineral)) && !mineralFieldsSelected.contains(mineralField)) {
                            closestMineral = mineralField;
                        }
                    }
                }

                //if a mineral patch was found, send the worker to gather it
                if (closestMineral != null) {
                	mineralFieldsSelected.add(closestMineral);
                	Mineral m = MineralsHelper.GetMineralFromUnit(closestMineral);
                	if (m == null) {
                		m = new Mineral(closestMineral);
                		m.amountOfWorkersAssignedToMineral++;
                		MineralsHelper.minerals.add(m);
                	}else {
                		m.amountOfWorkersAssignedToMineral++;
                	}
                    myUnit.gather(closestMineral, false);
                }
            }
    	}
    }
}