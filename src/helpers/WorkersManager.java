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
import models.Building;
import models.CustomBaseLocation;
import models.Worker;

public class WorkersManager {
	
	public static void Init() {
		Workers = new ArrayList<Worker>();
	}
	
    public static List<Worker> Workers;
    
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
			if (!w.isBuilding && !w.isScout && !w.unit.isCarryingGas() && !w.unit.isCarryingMinerals()) {
				if (w.miningFrom != null && w.miningFrom.getType() == UnitType.Terran_Refinery) continue;
				return w;
			}
		}	
    	return null;
    }
    
    public static void SendIdleWorkersToMinerals() {
    	List<Unit> mineralFieldsSelected = new ArrayList<Unit>();
    	for (Worker w : Workers) {
    		// get closest base to worker
    		final Unit myUnit = w.unit;
    		if (myUnit.isIdle() && myUnit.canMove() && !w.isBuilding && !w.isScout) {
    			
    			// go back to the unit the worker was previously mining from
    			if (w.miningFrom != null) {
    				myUnit.gather(w.miningFrom, false);
    				continue;
    			}
    			
    			CustomBaseLocation freeBase = BaseManager.GetBaseThatHasFreeSpaceForWorkers(myUnit);
                

                //find the closest mineral
                if (freeBase != null) {
                	SendWorkerToClosestMineral(freeBase, w, mineralFieldsSelected);
                }
            }
    	}
    }
    
    public static void SendWorkerToClosestMineral(CustomBaseLocation cbl, Worker w) {
    	Unit myUnit = w.unit;
    	Unit closestMineral = null;
    	for (Unit mineralField : cbl.baseLocation.getMinerals()) {
            if (mineralField.getType().isMineralField()) {
                if ((closestMineral == null || myUnit.getDistance(mineralField) < myUnit.getDistance(closestMineral))) {
                    closestMineral = mineralField;
                }
            }
        }

        //if a mineral patch was found, send the worker to gather it
        if (closestMineral != null) {
        	w.miningFrom = closestMineral;
            myUnit.gather(closestMineral, false);
        }	
    }
    
    public static void SendWorkerToClosestMineral(CustomBaseLocation cbl, Worker w, List<Unit> mineralFieldsSelected) {
    	Unit myUnit = w.unit;
    	Unit closestMineral = null;
    	for (Unit mineralField : cbl.baseLocation.getMinerals()) {
            if (mineralField.getType().isMineralField()) {
                if ((closestMineral == null || myUnit.getDistance(mineralField) < myUnit.getDistance(closestMineral)) && !mineralFieldsSelected.contains(mineralField)) {
                    closestMineral = mineralField;
                }
            }
        }

        //if a mineral patch was found, send the worker to gather it
        if (closestMineral != null) {
        	mineralFieldsSelected.add(closestMineral);
        	w.miningFrom = closestMineral;
            myUnit.gather(closestMineral, false);
        }	
    }
}